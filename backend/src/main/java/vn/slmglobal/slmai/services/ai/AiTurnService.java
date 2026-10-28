package vn.slmglobal.slmai.services.ai;

import com.anthropic.errors.AnthropicServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.slmglobal.slmai.common.util.HashUtil;
import vn.slmglobal.slmai.common.util.VietnamesePhone;
import vn.slmglobal.slmai.services.ai.entity.AiPurpose;
import vn.slmglobal.slmai.services.ai.entity.AiRun;
import vn.slmglobal.slmai.services.catalog.CatalogSyncService;
import vn.slmglobal.slmai.services.conversation.ChannelSender;
import vn.slmglobal.slmai.services.conversation.ConversationProperties;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.conversation.entity.Message;
import vn.slmglobal.slmai.services.conversation.entity.MessageDirection;
import vn.slmglobal.slmai.services.conversation.entity.MessageStatus;
import vn.slmglobal.slmai.services.knowledge.KnowledgeBase;
import vn.slmglobal.slmai.services.lead.ConsultSlots;
import vn.slmglobal.slmai.services.lead.entity.ConsultProfile;
import vn.slmglobal.slmai.services.policy.NumberChecker;
import vn.slmglobal.slmai.services.policy.ReplyPolicy;
import vn.slmglobal.slmai.services.sizing.PricingProperties;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Một lượt AI trả lời khách (mục 19): nhận lượt → ghép prompt → Claude → Policy → chốt → gửi.
 * Gọi Claude và gửi tin nằm ngoài transaction; các bước ghi CSDL ở {@link AiTurnStore}.
 */
@Slf4j
@Service
public class AiTurnService {

    /** Chưa đồng bộ catalog / chưa có Media Library: phiên bản cố định để luot_goi_ai vẫn đủ cột (mục 4.2, 6) */
    static final String CATALOG_VERSION_NONE = HashUtil.sha256Hex("catalog:none");
    static final String MEDIA_VERSION_NONE = HashUtil.sha256Hex("media:none");

    /** Lỗi do request sai hoặc khoá API sai: thử lại cũng vậy */
    private static final Set<Integer> NON_RETRYABLE_STATUS = Set.of(400, 401, 403, 404, 413, 422);

    private final AiTurnStore store;
    private final CustomerChatPromptBuilder promptBuilder;
    private final ClaudeChatClient claude;
    private final ReplyPolicy policy;
    private final KnowledgeBase knowledgeBase;
    private final CustomerChatTools tools;
    private final CatalogSyncService catalogSync;
    private final PricingProperties pricing;
    private final AiProperties properties;
    private final ConversationProperties conversationProperties;
    private final ObjectMapper objectMapper;
    private final Map<ChannelCode, ChannelSender> senders = new EnumMap<>(ChannelCode.class);
    /** Số lần gọi Claude lỗi liên tiếp của từng hội thoại (chỉ trong tiến trình này) */
    private final Map<UUID, Integer> failures = new ConcurrentHashMap<>();
    /** Định danh kênh đã thử lấy tên, để không gọi lại API hồ sơ mỗi lượt khi không lấy được */
    private final Set<UUID> nameLookups = ConcurrentHashMap.newKeySet();
    /** Bật lại "đang soạn tin" theo chu kỳ trong lúc chờ Claude */
    private final ScheduledExecutorService typingScheduler = Executors.newScheduledThreadPool(2, r -> {
        Thread t = new Thread(r, "ai-typing");
        t.setDaemon(true);
        return t;
    });

    public AiTurnService(AiTurnStore store, CustomerChatPromptBuilder promptBuilder, ClaudeChatClient claude,
                         ReplyPolicy policy, KnowledgeBase knowledgeBase, CustomerChatTools tools,
                         CatalogSyncService catalogSync, PricingProperties pricing, AiProperties properties,
                         ConversationProperties conversationProperties, ObjectMapper objectMapper,
                         List<ChannelSender> channelSenders) {
        this.store = store;
        this.promptBuilder = promptBuilder;
        this.claude = claude;
        this.policy = policy;
        this.knowledgeBase = knowledgeBase;
        this.tools = tools;
        this.catalogSync = catalogSync;
        this.pricing = pricing;
        this.properties = properties;
        this.conversationProperties = conversationProperties;
        this.objectMapper = objectMapper;
        channelSenders.forEach(s -> senders.put(s.channel(), s));
    }

    @PreDestroy
    public void shutdown() {
        typingScheduler.shutdownNow();
    }

    public void process(UUID conversationId) {
        TurnSnapshot s = store.claim(conversationId).orElse(null);
        if (s == null) {
            return;
        }
        ZonedDateTime now = ZonedDateTime.now(properties.businessHours().zoneId());
        boolean businessOpen = properties.businessHours().isOpen(now);
        boolean windowOpen = isWindowOpen(s);
        ChannelSender sender = senders.get(s.channel());
        s = withChannelName(s, sender);

        boolean pricingTools = tools.available();
        ClaudeChatClient.ChatRequest request = promptBuilder.build(s, now, businessOpen, pricingTools).orElse(null);
        if (request == null) {
            store.release(s);
            return;
        }
        CustomerChatTools.Session toolSession = null;
        if (pricingTools) {
            toolSession = tools.session(s.profile());
            request = request.withTools(tools.definitions(), toolSession);
        }
        TypingIndicator typing = properties.typingIndicator() && sender != null && windowOpen
                ? TypingIndicator.start(sender, s.identity(), typingScheduler)
                : null;

        AiRun run = newRun(s);
        ClaudeChatClient.ChatCompletion completion = null;
        RuntimeException failure = null;
        try {
            completion = claude.complete(request);
        } catch (RuntimeException e) {
            failure = e;
        } finally {
            if (typing != null) {
                typing.stop();
            }
        }
        if (toolSession != null) {
            run.setToolCalls(toolCallsJson(toolSession.calls()));
        }
        int sent = failure != null
                ? onCallFailed(s, run, failure, businessOpen, windowOpen, sender)
                : finish(s, run, completion, toolSession, businessOpen, windowOpen, sender);
        // Không gửi gì (nháp, bỏ lượt, chờ thử lại): tắt bong bóng thay vì để khách thấy "đang soạn" thêm 20 giây
        if (typing != null && sent == 0) {
            sender.stopTyping(s.identity());
        }
    }

    /** Phần sau khi Claude trả lời: đọc kết quả, Policy, chốt, gửi. Trả số tin đã gửi ra kênh. */
    private int finish(TurnSnapshot s, AiRun run, ClaudeChatClient.ChatCompletion completion,
                       CustomerChatTools.Session toolSession, boolean businessOpen, boolean windowOpen,
                       ChannelSender sender) {
        UUID conversationId = s.conversationId();
        failures.remove(conversationId);
        fillUsage(run, completion);

        AiReply reply = parse(run, completion);
        ReplyPolicy.Decision decision;
        if (reply == null) {
            decision = ReplyPolicy.Decision.handoff("AI_OUTPUT_UNUSABLE");
        } else {
            run.setSources(reply.sources() == null ? new String[0] : reply.sources().toArray(String[]::new));
            run.setKnowledgeGap(reply.knowledgeGap() != null && !reply.knowledgeGap().isBlank());
            decision = policy.evaluate(new ReplyPolicy.Input(
                    reply, s.replyMode(), s.firstTurnAfterHandback(), windowOpen, sender != null,
                    sender != null ? sender.maxTextLength() : 2000,
                    numberSourceTexts(s, toolSession), numberSourceValues(s.profile()),
                    allowedLinks(s, toolSession)));
        }
        AiTurnStore.Quote quote = toolSession == null ? null
                : new AiTurnStore.Quote(toolSession.quotedPackageIds(), toolSession.cheapestQuotedVnd());
        AiTurnStore.Commit commit = store.commit(s, run, reply, decision, quote, businessOpen, windowOpen,
                sender != null);
        if (commit.applied()) {
            log.info("Lượt AI hội thoại {}: {} {} ({} ms, {} request, tool {}, cache đọc {} / ghi {})",
                    conversationId, decision.action(), decision.reasons(), run.getLatencyMs(), completion.apiCalls(),
                    toolSession == null ? 0 : toolSession.calls().size(), run.getCacheReadTokens(),
                    run.getCacheWriteTokens());
        }
        return deliver(s, sender, commit.outgoing());
    }

    /** Lấy tên Facebook của khách một lần (để AI chọn anh / chị theo core/voice), lưu vào dinh_danh_kenh. */
    private TurnSnapshot withChannelName(TurnSnapshot s, ChannelSender sender) {
        if (sender == null || s.identity().getDisplayName() != null || !nameLookups.add(s.identity().getId())) {
            return s;
        }
        String name = sender.fetchDisplayName(s.identity()).orElse(null);
        if (name == null) {
            return s;
        }
        store.saveDisplayName(s.identity().getId(), name);
        return s.customerName() == null ? s.withCustomerName(name) : s;
    }

    /** Trả số tin đã gửi ra kênh (câu báo nhận khi chuyển nhân viên); 0 khi hẹn thử lại. */
    private int onCallFailed(TurnSnapshot s, AiRun run, RuntimeException e, boolean businessOpen,
                             boolean windowOpen, ChannelSender sender) {
        int attempt = failures.merge(s.conversationId(), 1, Integer::sum);
        run.setError(truncate(describe(e)));
        boolean retryable = !(e instanceof AnthropicServiceException ase
                && NON_RETRYABLE_STATUS.contains(ase.statusCode()))
                && !(e instanceof IllegalStateException);
        if (retryable && attempt < Math.max(1, properties.maxAttempts())) {
            Duration wait = properties.retryBackoff().multipliedBy(1L << Math.min(attempt - 1, 6));
            log.warn("Gọi Claude lỗi (hội thoại {}, lần {}), thử lại sau {}: {}", s.conversationId(), attempt, wait,
                    run.getError());
            store.recordFailure(s, run, OffsetDateTime.now().plus(wait));
            return 0;
        }
        failures.remove(s.conversationId());
        log.error("Gọi Claude lỗi (hội thoại {}, lần {}), chuyển nhân viên: {}", s.conversationId(), attempt,
                run.getError());
        AiTurnStore.Commit commit = store.commit(s, run, null, ReplyPolicy.Decision.handoff("AI_ERROR"), null,
                businessOpen, windowOpen, sender != null);
        return deliver(s, sender, commit.outgoing());
    }

    /** null khi không dùng được: model từ chối, hết token giữa chừng, JSON hỏng. Lý do ghi vào luot_goi_ai.loi. */
    private AiReply parse(AiRun run, ClaudeChatClient.ChatCompletion completion) {
        if (completion.refused()) {
            run.setError("REFUSAL" + (completion.refusalCategory() != null ? ": " + completion.refusalCategory() : ""));
            return null;
        }
        if (completion.truncated()) {
            run.setError("MAX_TOKENS");
            return null;
        }
        try {
            JsonNode json = objectMapper.readTree(completion.text());
            run.setOutput(json);
            return objectMapper.treeToValue(json, AiReply.class);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            run.setError(truncate("INVALID_JSON: " + e.getMessage()));
            return null;
        }
    }

    /** Gửi các tin đã chốt; trả số tin gửi thành công. */
    private int deliver(TurnSnapshot s, ChannelSender sender, List<AiTurnStore.Outgoing> outgoing) {
        if (sender == null) {
            return 0;
        }
        int sent = 0;
        for (AiTurnStore.Outgoing o : outgoing) {
            try {
                String externalId = sender.sendText(s.identity(), o.text());
                store.markSent(o.messageId(), externalId);
                sent++;
            } catch (RuntimeException e) {
                log.warn("Gửi tin {} của hội thoại {} thất bại: {}", o.messageId(), s.conversationId(), e.getMessage());
                store.markFailed(o.messageId(), e.getMessage());
            }
        }
        return sent;
    }

    private AiRun newRun(TurnSnapshot s) {
        return AiRun.builder()
                .purpose(AiPurpose.CUSTOMER_CHAT)
                .conversationId(s.conversationId())
                .model(properties.model())
                .kbVersion(knowledgeBase.version())
                .catalogVersion(catalogVersion())
                .mediaVersion(MEDIA_VERSION_NONE)
                .promptVersion(promptBuilder.promptVersion())
                .build();
    }

    private static void fillUsage(AiRun run, ClaudeChatClient.ChatCompletion c) {
        if (c.model() != null) {
            run.setModel(c.model());
        }
        run.setInputTokens(c.inputTokens());
        run.setOutputTokens(c.outputTokens());
        run.setCacheReadTokens(c.cacheReadTokens());
        run.setCacheWriteTokens(c.cacheWriteTokens());
        run.setLatencyMs(c.latencyMs());
        run.setStopReason(c.stopReason());
    }

    /** Cửa sổ 24 giờ tính từ tin cuối của khách; trừ hao 1 phút cho thời gian gửi. */
    private boolean isWindowOpen(TurnSnapshot s) {
        Duration window = conversationProperties.messagingWindow();
        return s.lastCustomerMessageAt() != null && window != null
                && s.lastCustomerMessageAt().plus(window).minusMinutes(1).isAfter(OffsetDateTime.now());
    }

    /**
     * Nguồn hợp lệ cho kiểm tra con số (mục 33.1): tài liệu kiến thức, tin của khách, kết quả tool của lượt này, và
     * tin trang đã gửi khách (đã qua kiểm tra lúc gửi, hoặc do nhân viên gửi).
     */
    private List<String> numberSourceTexts(TurnSnapshot s, CustomerChatTools.Session toolSession) {
        List<String> texts = new ArrayList<>();
        texts.add(knowledgeBase.rendered());
        for (Message m : s.history()) {
            if (m.getContent() == null) {
                continue;
            }
            if (m.getDirection() == MessageDirection.IN
                    || (m.getDirection() == MessageDirection.OUT && m.getStatus() == MessageStatus.SENT)) {
                texts.add(VietnamesePhone.mask(m.getContent()));
            }
        }
        for (Message m : s.pendingInbound()) {
            if (m.getContent() != null) {
                texts.add(VietnamesePhone.mask(m.getContent()));
            }
        }
        if (toolSession != null) {
            texts.addAll(toolSession.outputs());
        }
        return texts;
    }

    /** Link AI được gửi: link trong kết quả tool, link trang đã gửi trước đó, trang chủ website theo miền */
    private List<String> allowedLinks(TurnSnapshot s, CustomerChatTools.Session toolSession) {
        List<String> links = new ArrayList<>(pricing.productLinkDomains().values());
        if (toolSession != null) {
            links.addAll(toolSession.links());
        }
        for (Message m : s.history()) {
            if (m.getDirection() == MessageDirection.OUT && m.getStatus() == MessageStatus.SENT
                    && m.getContent() != null) {
                links.addAll(ReplyPolicy.links(m.getContent()));
            }
        }
        return links;
    }

    private String catalogVersion() {
        String v = catalogSync.currentVersion();
        return v == null ? CATALOG_VERSION_NONE : v;
    }

    private JsonNode toolCallsJson(List<ClaudeChatClient.ToolCall> calls) {
        ArrayNode a = objectMapper.createArrayNode();
        for (ClaudeChatClient.ToolCall c : calls) {
            ObjectNode o = a.addObject();
            o.put("name", c.name());
            o.set("input", c.input());
            try {
                o.set("output", objectMapper.readTree(c.output()));
            } catch (JsonProcessingException | IllegalArgumentException e) {
                o.put("output", c.output());
            }
            o.put("error", c.error());
            o.put("latency_ms", c.latencyMs());
        }
        return a;
    }

    /** Số khách đã nêu ở các lượt trước, có thể đã trôi khỏi lịch sử: hoá đơn, ngân sách. */
    private static List<NumberChecker.Quantity> numberSourceValues(ConsultProfile p) {
        List<NumberChecker.Quantity> out = new ArrayList<>();
        if (p == null) {
            return out;
        }
        JsonNode bill = p.getSlots().get(ConsultSlots.HOA_DON_THANG);
        if (bill != null && bill.isNumber()) {
            out.add(new NumberChecker.Quantity(NumberChecker.Kind.MONEY, bill.asDouble(), "hoa_don_thang"));
        }
        JsonNode budget = p.getQualification().path("budget_vnd");
        for (String k : List.of("min", "max")) {
            if (budget.path(k).isNumber()) {
                out.add(new NumberChecker.Quantity(NumberChecker.Kind.MONEY, budget.get(k).asDouble(), "budget_vnd"));
            }
        }
        return out;
    }

    private static String describe(RuntimeException e) {
        if (e instanceof AnthropicServiceException ase) {
            return "HTTP " + ase.statusCode() + ase.errorType().map(t -> " " + t).orElse("") + ": " + e.getMessage();
        }
        return e.toString();
    }

    private static String truncate(String s) {
        return s == null || s.length() <= 2000 ? s : s.substring(0, 2000);
    }
}
