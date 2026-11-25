package vn.slmglobal.slmai.services.ai;

import com.anthropic.errors.AnthropicServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.slmglobal.slmai.common.util.HashUtil;
import vn.slmglobal.slmai.common.util.VietnamesePhone;
import vn.slmglobal.slmai.services.ai.entity.AiPurpose;
import vn.slmglobal.slmai.services.ai.entity.AiRun;
import vn.slmglobal.slmai.services.catalog.CatalogSyncService;
import vn.slmglobal.slmai.services.conversation.ChannelNameService;
import vn.slmglobal.slmai.services.conversation.ChannelSender;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.conversation.entity.Message;
import vn.slmglobal.slmai.services.conversation.entity.MessageDirection;
import vn.slmglobal.slmai.services.conversation.entity.MessageStatus;
import vn.slmglobal.slmai.services.conversation.entity.SenderType;
import vn.slmglobal.slmai.services.knowledge.KnowledgeBase;
import vn.slmglobal.slmai.services.knowledge.KnowledgeSnapshot;
import vn.slmglobal.slmai.services.lead.ConsultSlots;
import vn.slmglobal.slmai.services.lead.entity.ConsultProfile;
import vn.slmglobal.slmai.services.leadnurturing.FollowUpProperties;
import vn.slmglobal.slmai.services.leadnurturing.entity.FollowUp;
import vn.slmglobal.slmai.services.media.MediaCatalog;
import vn.slmglobal.slmai.services.media.MediaDelivery;
import vn.slmglobal.slmai.services.media.MediaPlanner;
import vn.slmglobal.slmai.services.policy.NumberChecker;
import vn.slmglobal.slmai.services.policy.ReplyPolicy;
import vn.slmglobal.slmai.services.settings.AiSettingsService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    /** Tin ngắn nhất cũng chờ chừng này giữa hai tin, như người gõ */
    static final Duration MIN_TYPING = Duration.ofSeconds(1);

    private final AiTurnStore store;
    private final CustomerChatPromptBuilder promptBuilder;
    private final ClaudeChatClient claude;
    private final ReplyPolicy policy;
    private final KnowledgeBase knowledgeBase;
    private final CustomerChatTools tools;
    private final CatalogSyncService catalogSync;
    private final AiSettingsService settings;
    private final ObjectMapper objectMapper;
    private final ChannelNameService channelNames;
    private final MediaCatalog mediaCatalog;
    private final MediaPlanner mediaPlanner;
    private final MediaDelivery mediaDelivery;
    private final Map<ChannelCode, ChannelSender> senders = new EnumMap<>(ChannelCode.class);
    /** Số lần gọi Claude lỗi liên tiếp của từng hội thoại (chỉ trong tiến trình này) */
    private final Map<UUID, Integer> failures = new ConcurrentHashMap<>();
    /** Bật lại "đang soạn tin" theo chu kỳ trong lúc chờ Claude */
    private final ScheduledExecutorService typingScheduler = Executors.newScheduledThreadPool(2, r -> {
        Thread t = new Thread(r, "ai-typing");
        t.setDaemon(true);
        return t;
    });

    public AiTurnService(AiTurnStore store, CustomerChatPromptBuilder promptBuilder, ClaudeChatClient claude,
                         ReplyPolicy policy, KnowledgeBase knowledgeBase, CustomerChatTools tools,
                         CatalogSyncService catalogSync, AiSettingsService settings, ObjectMapper objectMapper,
                         ChannelNameService channelNames, MediaCatalog mediaCatalog, MediaPlanner mediaPlanner,
                         MediaDelivery mediaDelivery, List<ChannelSender> channelSenders) {
        this.store = store;
        this.promptBuilder = promptBuilder;
        this.claude = claude;
        this.policy = policy;
        this.knowledgeBase = knowledgeBase;
        this.tools = tools;
        this.catalogSync = catalogSync;
        this.settings = settings;
        this.objectMapper = objectMapper;
        this.channelNames = channelNames;
        this.mediaCatalog = mediaCatalog;
        this.mediaPlanner = mediaPlanner;
        this.mediaDelivery = mediaDelivery;
        channelSenders.forEach(s -> senders.put(s.channel(), s));
    }

    @PreDestroy
    public void shutdown() {
        typingScheduler.shutdownNow();
    }

    public void process(UUID conversationId) {
        long started = System.nanoTime();
        TurnSnapshot s = store.claim(conversationId).orElse(null);
        if (s == null) {
            return;
        }
        if (s.isFollowUp()) {
            processFollowUp(s, started);
            return;
        }
        // Một cài đặt và một bản kiến thức cho cả lượt, kể cả khi admin vừa đổi giữa chừng
        AiProperties properties = settings.ai();
        KnowledgeSnapshot knowledge = knowledgeBase.current();
        ZonedDateTime now = ZonedDateTime.now(properties.businessHours().zoneId());
        boolean businessOpen = properties.businessHours().isOpen(now);
        boolean windowOpen = isWindowOpen(s);
        ChannelSender sender = senders.get(s.channel());
        s = withChannelName(s);

        boolean pricingTools = tools.available();
        MediaCatalog.Block media = mediaBlock(s.channel());
        ClaudeChatClient.ChatRequest request = promptBuilder.build(s, knowledge, media, now, businessOpen,
                pricingTools).orElse(null);
        if (request == null) {
            store.release(s);
            return;
        }
        CustomerChatTools.Session toolSession = null;
        if (pricingTools) {
            toolSession = tools.session(s.profile(), s.channel());
            request = request.withTools(tools.definitions(), toolSession);
        }
        TypingIndicator typing = properties.typingIndicator() && sender != null && windowOpen
                ? TypingIndicator.start(sender, s.identity(), typingScheduler)
                : null;

        AiRun run = newRun(s, knowledge, media, properties);
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
                ? onCallFailed(s, run, failure, businessOpen, windowOpen, sender, started)
                : finish(s, knowledge, run, completion, toolSession, businessOpen, windowOpen, sender, started);
        // Không gửi gì (nháp, bỏ lượt, chờ thử lại): tắt bong bóng thay vì để khách thấy "đang soạn" thêm 20 giây
        if (typing != null && sent == 0) {
            sender.stopTyping(s.identity());
        }
    }

    /**
     * Phần sau khi Claude trả lời: đọc kết quả, Policy, chốt, gửi. Trả số tin đã gửi ra kênh.
     *
     * @param started lúc bắt đầu lượt (System.nanoTime): thời gian chờ AI tính vào thời gian gõ tin đầu
     */
    private int finish(TurnSnapshot s, KnowledgeSnapshot knowledge, AiRun run,
                       ClaudeChatClient.ChatCompletion completion,
                       CustomerChatTools.Session toolSession, boolean businessOpen, boolean windowOpen,
                       ChannelSender sender, long started) {
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
                    reply, s.replyMode(), s.firstTurnAfterHandback(), s.handedBackByStaff(), windowOpen,
                    sender != null, sender != null ? sender.maxTextLength() : 2000,
                    numberSourceTexts(s, knowledge, toolSession), numberSourceValues(s.profile()),
                    allowedLinks(s, knowledge, toolSession), knowledge.rules().replyPolicy()));
        }
        AiTurnStore.Quote quote = toolSession == null ? null
                : new AiTurnStore.Quote(toolSession.quotedPackageIds(), toolSession.cheapestQuotedVnd());
        AiTurnStore.Commit commit = store.commit(s, run, reply, decision, quote, attachments(s, reply), businessOpen,
                windowOpen, sender != null);
        if (commit.applied()) {
            log.info("Lượt AI hội thoại {}: {} {} ({} ms, {} request, tool {}, cache đọc {} / ghi {})",
                    conversationId, decision.action(), decision.reasons(), run.getLatencyMs(), completion.apiCalls(),
                    toolSession == null ? 0 : toolSession.calls().size(), run.getCacheReadTokens(),
                    run.getCacheWriteTokens());
        }
        return deliver(s, sender, commit.outgoing(), started);
    }

    /**
     * Lượt nhắc khách im lặng (mục 56.9): kiểm tra lại điều kiện, AI viết tin nhắc kèm media chọn sẵn, Policy như tin
     * thường. Không chuyển nhân viên, không gửi câu báo nhận, gọi AI lỗi thì bỏ lần nhắc này.
     */
    private void processFollowUp(TurnSnapshot s, long started) {
        AiProperties properties = settings.ai();
        FollowUpProperties rules = settings.followUp();
        KnowledgeSnapshot knowledge = knowledgeBase.current();
        ZonedDateTime now = ZonedDateTime.now(properties.businessHours().zoneId());
        ChannelSender sender = senders.get(s.channel());
        Duration window = settings.conversation().messagingWindow();
        Message lastSent = lastSentOutbound(s);
        String blocked = followUpBlocked(s, rules, properties, sender, window, lastSent, now);
        if (blocked != null) {
            log.info("Hội thoại {}: bỏ lần nhắc {}: {}", s.conversationId(), s.followUp().getAttemptNo(), blocked);
            store.finishFollowUp(s, FollowUp.Status.CANCELLED, blocked, null);
            return;
        }
        s = withChannelName(s);
        boolean businessOpen = properties.businessHours().isOpen(now);
        boolean pricingTools = tools.available();
        MediaCatalog.Block media = mediaBlock(s.channel());
        ObjectNode extra = followUpExtra(s);
        ZonedDateTime windowEnd = s.lastCustomerMessageAt().plus(window).atZoneSameInstant(now.getZone());
        CustomerChatPromptBuilder.FollowUpContext context = new CustomerChatPromptBuilder.FollowUpContext(
                s.followUp().getAttemptNo(),
                Duration.between(lastSent.getSentAt(), now.toOffsetDateTime()),
                Duration.between(now, windowEnd),
                !rules.allows(s.followUp().getAttemptNo() + 1) || !rules.hasSlotBetween(now.plus(rules.interval()),
                        windowEnd.minus(rules.windowMargin()), now.getZone()),
                extra == null ? List.of() : List.of(MediaPlanner.describe(extra)));
        ClaudeChatClient.ChatRequest request = promptBuilder.buildFollowUp(s, knowledge, media, now, businessOpen,
                pricingTools, context).orElse(null);
        if (request == null) {
            store.finishFollowUp(s, FollowUp.Status.CANCELLED, "Tin cuối không phải của trang", null);
            return;
        }
        CustomerChatTools.Session toolSession = null;
        if (pricingTools) {
            toolSession = tools.session(s.profile(), s.channel());
            request = request.withTools(tools.definitions(), toolSession);
        }

        AiRun run = newRun(s, knowledge, media, properties);
        run.setPurpose(AiPurpose.FOLLOW_UP);
        ClaudeChatClient.ChatCompletion completion;
        try {
            completion = claude.complete(request);
        } catch (RuntimeException e) {
            run.setError(truncate(describe(e)));
            log.warn("Hội thoại {}: gọi Claude cho lần nhắc lỗi, bỏ lần nhắc: {}", s.conversationId(), run.getError());
            store.finishFollowUp(s, FollowUp.Status.FAILED, run.getError(), run);
            return;
        }
        if (toolSession != null) {
            run.setToolCalls(toolCallsJson(toolSession.calls()));
        }
        fillUsage(run, completion);
        AiReply reply = parse(run, completion);
        if (reply == null) {
            store.finishFollowUp(s, FollowUp.Status.FAILED, run.getError(), run);
            return;
        }
        run.setSources(reply.sources() == null ? new String[0] : reply.sources().toArray(String[]::new));
        run.setKnowledgeGap(reply.knowledgeGap() != null && !reply.knowledgeGap().isBlank());
        if (!reply.hasReply()) {
            store.finishFollowUp(s, FollowUp.Status.SKIPPED, "AI thấy không nên nhắc", run);
            return;
        }
        if (reply.handoffNeeded() || (properties.handoffIntents() != null
                && properties.handoffIntents().contains(reply.intent()))) {
            store.finishFollowUp(s, FollowUp.Status.SKIPPED, "AI muốn chuyển nhân viên"
                    + (reply.handoffNeeded() ? ": " + reply.handoff().reason() : ""), run);
            return;
        }
        boolean windowOpen = isWindowOpen(s);
        ReplyPolicy.Decision decision = policy.evaluate(new ReplyPolicy.Input(
                reply, s.replyMode(), false, false, windowOpen, true, sender.maxTextLength(),
                numberSourceTexts(s, knowledge, toolSession), numberSourceValues(s.profile()),
                allowedLinks(s, knowledge, toolSession), knowledge.rules().replyPolicy()));
        if (decision.action() == ReplyPolicy.Action.HANDOFF) {
            store.finishFollowUp(s, FollowUp.Status.SKIPPED, String.join(", ", decision.reasons()), run);
            return;
        }
        if (decision.action() == ReplyPolicy.Action.AUTO && !rules.autoSend()) {
            decision = new ReplyPolicy.Decision(ReplyPolicy.Action.DRAFT,
                    List.of(ReplyPolicy.FOLLOW_UP_DRAFT_MODE), List.of());
        }
        JsonNode attachments = followUpAttachments(s, reply, extra);
        AiTurnStore.Commit commit = store.commitFollowUp(s, run, reply, decision, attachments,
                rules.isInHours(now, now.getZone()), windowOpen);
        deliver(s, sender, commit.outgoing(), started);
    }

    /**
     * Lý do không nhắc nữa (điều kiện đã đổi kể từ lúc lập lịch); null là nhắc được. Cùng điều kiện với
     * FollowUpScheduler, kiểm lại lúc chạy vì lịch có thể chờ worker một lúc.
     */
    private static String followUpBlocked(TurnSnapshot s, FollowUpProperties rules, AiProperties properties,
                                          ChannelSender sender, Duration window, Message lastSent,
                                          ZonedDateTime now) {
        if (sender == null) {
            return "Kênh chưa hỗ trợ gửi tin";
        }
        if (!rules.enabled()) {
            return "Đã tắt nhắc khách";
        }
        if (properties.forceDraft()) {
            return "Công tắc dừng AI đang bật";
        }
        if (s.followUp().getCreatedAt().plus(rules.staleAfter()).isBefore(now.toOffsetDateTime())) {
            return "Lịch nhắc chờ quá lâu";
        }
        if (!rules.isInHours(now, now.getZone())) {
            return "Ngoài khung giờ nhắc";
        }
        if (s.lastCustomerMessageAt() == null || window == null || s.lastCustomerMessageAt().plus(window)
                .minus(rules.windowMargin()).isBefore(now.toOffsetDateTime())) {
            return "Sắp hết 24 giờ kể từ tin cuối của khách";
        }
        if (lastSent == null || lastSent.getSenderType() != SenderType.AI || lastSent.getSentAt() == null
                || lastSent.getSentAt().isBefore(s.lastCustomerMessageAt())) {
            return "Tin cuối không phải tin AI đã gửi sau tin của khách";
        }
        return null;
    }

    /** Tin trang đã gửi gần nhất trong lịch sử (theo lúc gửi) */
    private static Message lastSentOutbound(TurnSnapshot s) {
        return s.history().stream()
                .filter(m -> m.getDirection() == MessageDirection.OUT && m.getStatus() == MessageStatus.SENT
                        && m.getSentAt() != null)
                .max(Comparator.comparing(Message::getSentAt))
                .orElse(null);
    }

    /** Media chọn sẵn cho tin nhắc: của gói đã đề xuất, chưa gửi; lỗi thì nhắc không kèm media */
    private ObjectNode followUpExtra(TurnSnapshot s) {
        if (s.profile() == null || s.profile().getRecommendedPackageIds() == null) {
            return null;
        }
        try {
            List<Integer> ids = Arrays.stream(s.profile().getRecommendedPackageIds()).filter(Objects::nonNull)
                    .toList();
            return mediaPlanner.followUpExtra(s.channel(), s.history(), ids).orElse(null);
        } catch (RuntimeException e) {
            log.warn("Hội thoại {}: không chọn được media cho tin nhắc: {}", s.conversationId(), e.toString());
            return null;
        }
    }

    private JsonNode followUpAttachments(TurnSnapshot s, AiReply reply, ObjectNode extra) {
        try {
            MediaPlanner.Plan plan = mediaPlanner.plan(s.channel(), reply.reply(), reply.mediaCodes(), s.history(),
                    extra);
            if (!plan.dropped().isEmpty()) {
                log.info("Hội thoại {}: tin nhắc bỏ media AI chọn {}", s.conversationId(), plan.dropped());
            }
            return plan.attachments();
        } catch (RuntimeException e) {
            log.warn("Hội thoại {}: không chọn được media gửi kèm tin nhắc: {}", s.conversationId(), e.toString());
            return JsonNodeFactory.instance.arrayNode();
        }
    }

    /** Lấy tên Facebook của khách một lần (để AI chọn anh / chị theo core/voice), lưu vào dinh_danh_kenh. */
    private TurnSnapshot withChannelName(TurnSnapshot s) {
        if (s.identity().getDisplayName() != null) {
            return s;
        }
        String name = channelNames.fetchOnce(s.identity()).orElse(null);
        return name != null && s.customerName() == null ? s.withCustomerName(name) : s;
    }

    /** Trả số tin đã gửi ra kênh (câu báo nhận khi chuyển nhân viên); 0 khi hẹn thử lại. */
    private int onCallFailed(TurnSnapshot s, AiRun run, RuntimeException e, boolean businessOpen,
                             boolean windowOpen, ChannelSender sender, long started) {
        AiProperties properties = settings.ai();
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
                null, businessOpen, windowOpen, sender != null);
        return deliver(s, sender, commit.outgoing(), started);
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

    /**
     * Gửi các tin đã chốt; trả số tin gửi thành công. Câu trả lời của AI tách thành nhiều tin ({@link ReplyParts});
     * trước mỗi tin hiện "đang soạn tin" và chờ theo tốc độ gõ, tin đầu trừ đi thời gian đã chờ AI. Trong lúc này
     * hội thoại vẫn nằm trong lượt của worker nên lượt sau (khách nhắn thêm) chờ gửi xong mới chạy.
     */
    private int deliver(TurnSnapshot s, ChannelSender sender, List<AiTurnStore.Outgoing> outgoing, long started) {
        if (sender == null) {
            return 0;
        }
        AiProperties properties = settings.ai();
        int sent = 0;
        for (AiTurnStore.Outgoing o : outgoing) {
            List<String> parts = o.aiReply() ? ReplyParts.split(o.text()) : List.of(o.text());
            int done = 0;
            String externalId = null;
            try {
                for (String part : parts) {
                    if (o.aiReply()) {
                        Duration wait = typingDelay(part, properties);
                        pause(sender, s, done == 0 ? wait.minusNanos(System.nanoTime() - started) : wait,
                                properties.typingIndicator());
                    }
                    externalId = sender.sendText(s.identity(), part);
                    done++;
                    sent++;
                }
                JsonNode attachments = null;
                if (o.attachments() != null && !o.attachments().isEmpty()) {
                    // Ảnh / video sau phần chữ, mỗi mục chờ một nhịp như người gửi
                    MediaDelivery.Result media = mediaDelivery.send(sender, s.identity(), o.attachments(),
                            () -> pause(sender, s, MIN_TYPING, properties.typingIndicator()));
                    attachments = media.attachments();
                    sent += media.sent();
                }
                store.markSent(o.messageId(), externalId, attachments);
            } catch (RuntimeException e) {
                log.warn("Gửi tin {} của hội thoại {} thất bại sau {}/{} đoạn: {}", o.messageId(), s.conversationId(),
                        done, parts.size(), e.getMessage());
                if (done == 0) {
                    store.markFailed(o.messageId(), e.getMessage());
                } else {
                    store.markPartlySent(o.messageId(), String.join("\n\n", parts.subList(0, done)), externalId,
                            String.join("\n\n", parts.subList(done, parts.size())), e.getMessage());
                }
            }
        }
        return sent;
    }

    /** Thời gian gõ một tin: số ký tự / tốc độ gõ, ít nhất {@link #MIN_TYPING}, nhiều nhất typingMaxDelay */
    static Duration typingDelay(String text, AiProperties properties) {
        long ms = Math.round(text.length() * 1000.0 / properties.typingCharsPerSecond());
        return Duration.ofMillis(Math.min(Math.max(ms, MIN_TYPING.toMillis()), properties.typingMaxDelay().toMillis()));
    }

    /** Chờ trước một tin, hiện "đang soạn tin" nếu bật; bị ngắt (tắt máy) thì gửi luôn */
    private void pause(ChannelSender sender, TurnSnapshot s, Duration wait, boolean typing) {
        if (wait.isNegative() || wait.isZero()) {
            return;
        }
        TypingIndicator indicator = typing ? TypingIndicator.resume(sender, s.identity(), typingScheduler) : null;
        try {
            Thread.sleep(wait.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (indicator != null) {
                indicator.stop();
            }
        }
    }

    private AiRun newRun(TurnSnapshot s, KnowledgeSnapshot knowledge, MediaCatalog.Block media,
                         AiProperties properties) {
        return AiRun.builder()
                .purpose(AiPurpose.CUSTOMER_CHAT)
                .conversationId(s.conversationId())
                .model(properties.model())
                .kbVersion(knowledge.version())
                .catalogVersion(catalogVersion())
                .mediaVersion(media == null ? MEDIA_VERSION_NONE : media.version())
                .promptVersion(promptBuilder.promptVersion(knowledge, media))
                .build();
    }

    /** Khối gửi media của kênh; thư viện lỗi thì lượt vẫn chạy, chỉ không có danh mục */
    private MediaCatalog.Block mediaBlock(ChannelCode channel) {
        try {
            return mediaCatalog.block(channel);
        } catch (RuntimeException e) {
            log.warn("Không đọc được danh mục media: {}", e.toString());
            return null;
        }
    }

    /** Media gửi kèm câu trả lời (MediaPlanner); lỗi thì gửi phần chữ, không kèm media */
    private JsonNode attachments(TurnSnapshot s, AiReply reply) {
        if (reply == null || !reply.hasReply()) {
            return null;
        }
        try {
            MediaPlanner.Plan plan = mediaPlanner.plan(s.channel(), reply.reply(), reply.mediaCodes(), s.history());
            if (!plan.dropped().isEmpty()) {
                log.info("Hội thoại {}: bỏ media AI chọn {}", s.conversationId(), plan.dropped());
            }
            return plan.attachments();
        } catch (RuntimeException e) {
            log.warn("Hội thoại {}: không chọn được media gửi kèm: {}", s.conversationId(), e.toString());
            return JsonNodeFactory.instance.arrayNode();
        }
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
        Duration window = settings.conversation().messagingWindow();
        return s.lastCustomerMessageAt() != null && window != null
                && s.lastCustomerMessageAt().plus(window).minusMinutes(1).isAfter(OffsetDateTime.now());
    }

    /**
     * Nguồn hợp lệ cho kiểm tra con số (mục 33.1): tài liệu kiến thức, tin của khách, kết quả tool của lượt này, và
     * tin trang đã gửi khách (đã qua kiểm tra lúc gửi, hoặc do nhân viên gửi).
     */
    private List<String> numberSourceTexts(TurnSnapshot s, KnowledgeSnapshot knowledge,
                                           CustomerChatTools.Session toolSession) {
        List<String> texts = new ArrayList<>();
        texts.add(knowledgeBase.renderKnowledge(knowledge));
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
    private List<String> allowedLinks(TurnSnapshot s, KnowledgeSnapshot knowledge,
                                      CustomerChatTools.Session toolSession) {
        List<String> links = new ArrayList<>(knowledge.rules().pricing().productLinkDomains().values());
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
