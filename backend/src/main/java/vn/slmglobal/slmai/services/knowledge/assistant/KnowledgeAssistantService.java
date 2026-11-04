package vn.slmglobal.slmai.services.knowledge.assistant;

import com.anthropic.core.RequestOptions;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.helpers.MessageAccumulator;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.JsonOutputFormat;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.RawMessageStreamEvent;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.TextBlock;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.ThinkingConfigAdaptive;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PreDestroy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.common.security.AuthInfo;
import vn.slmglobal.slmai.common.security.RoleCode;
import vn.slmglobal.slmai.common.util.HashUtil;
import vn.slmglobal.slmai.services.ai.ClaudeChatClient;
import vn.slmglobal.slmai.services.ai.entity.AiPurpose;
import vn.slmglobal.slmai.services.ai.entity.AiRun;
import vn.slmglobal.slmai.services.ai.repository.AiRunRepository;
import vn.slmglobal.slmai.services.auth.AuditLogService;
import vn.slmglobal.slmai.services.knowledge.KnowledgeBase;
import vn.slmglobal.slmai.services.knowledge.KnowledgeDocType;
import vn.slmglobal.slmai.services.knowledge.KnowledgeSnapshot;
import vn.slmglobal.slmai.services.knowledge.admin.KnowledgeAdminService;
import vn.slmglobal.slmai.services.knowledge.admin.KnowledgeDiff;
import vn.slmglobal.slmai.services.knowledge.admin.KnowledgeLinter;
import vn.slmglobal.slmai.services.knowledge.entity.KnowledgeDoc;
import vn.slmglobal.slmai.services.knowledge.entity.KnowledgeVersion;
import vn.slmglobal.slmai.services.knowledge.repository.KnowledgeDocRepository;
import vn.slmglobal.slmai.services.knowledge.repository.KnowledgeVersionRepository;
import vn.slmglobal.slmai.services.settings.AiSettingsService;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI hỗ trợ soạn kiến thức (mục 5.10 B): quản trị viên nhắn yêu cầu thô, Claude đọc hướng dẫn viết + toàn bộ kiến
 * thức đang chạy + bản nháp, trả đề xuất sửa / tạo / ngừng tài liệu; người duyệt xem diff rồi duyệt thành bản nháp.
 * AI không bao giờ tự phát hành.
 *
 * <p>Mỗi lượt chạy nền (Opus có thể mất 30–90 giây, lâu hơn thời gian chờ của nginx-proxy-manager); trang admin hỏi
 * lại phiên cho tới khi có trả lời. Mỗi lượt ghi luot_goi_ai với muc_dich = KNOWLEDGE_ASSISTANT.
 */
@Slf4j
@Service
public class KnowledgeAssistantService {

    static final String PROMPT_LOCATION = "prompts/knowledge-assistant.md";
    private static final String CATALOG_NONE = HashUtil.sha256Hex("catalog:none");
    private static final String MEDIA_NONE = HashUtil.sha256Hex("media:none");
    private static final Pattern KEY = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*(/[a-z0-9]+(-[a-z0-9]+)*)+");
    private static final Set<KnowledgeDocType> ALLOWED = Set.of(KnowledgeDocType.CORE, KnowledgeDocType.PLAYBOOK,
            KnowledgeDocType.POLICY, KnowledgeDocType.FAQ, KnowledgeDocType.EXAMPLE, KnowledgeDocType.PROMPT,
            KnowledgeDocType.GUIDE, KnowledgeDocType.TEMPLATE);
    private static final long MAX_TOKENS = 16_000;
    private static final Duration TIMEOUT = Duration.ofMinutes(5);

    private final AssistantSessionRepository sessions;
    private final AssistantMessageRepository messages;
    private final ChangeProposalRepository proposals;
    private final KnowledgeDocRepository docs;
    private final KnowledgeVersionRepository versions;
    private final KnowledgeBase knowledge;
    private final KnowledgeAdminService admin;
    private final ClaudeChatClient claude;
    private final AiSettingsService settings;
    private final AiRunRepository aiRuns;
    private final AuditLogService audit;
    private final TransactionTemplate tx;
    private final ObjectMapper objectMapper;
    private final String instructions;
    /** Phiên đang chờ AI trả lời (một instance) */
    private final Set<UUID> processing = ConcurrentHashMap.newKeySet();
    private final ExecutorService executor = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "knowledge-assistant");
        t.setDaemon(true);
        return t;
    });

    public KnowledgeAssistantService(AssistantSessionRepository sessions, AssistantMessageRepository messages,
                                     ChangeProposalRepository proposals, KnowledgeDocRepository docs,
                                     KnowledgeVersionRepository versions, KnowledgeBase knowledge,
                                     KnowledgeAdminService admin, ClaudeChatClient claude, AiSettingsService settings,
                                     AiRunRepository aiRuns, AuditLogService audit, TransactionTemplate tx,
                                     ObjectMapper objectMapper) {
        this.sessions = sessions;
        this.messages = messages;
        this.proposals = proposals;
        this.docs = docs;
        this.versions = versions;
        this.knowledge = knowledge;
        this.admin = admin;
        this.claude = claude;
        this.settings = settings;
        this.aiRuns = aiRuns;
        this.audit = audit;
        this.tx = tx;
        this.objectMapper = objectMapper;
        try {
            this.instructions = new ClassPathResource(PROMPT_LOCATION).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Không đọc được " + PROMPT_LOCATION, e);
        }
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }

    // ------------------------------------------------------------------ DTO

    public record SessionRow(UUID id, String title, OffsetDateTime createdAt, OffsetDateTime lastMessageAt) {
    }

    /** @param error lượt AI lỗi (thay cho reply) */
    public record MessageView(Long id, String role, String text, String error, List<Long> proposalIds,
                              OffsetDateTime createdAt) {
    }

    /**
     * @param diff          so với bản nháp (nếu có) hoặc bản mới nhất của tài liệu
     * @param hasOtherDraft tài liệu đang có bản nháp khác: duyệt sẽ ghi đè (phải xác nhận)
     */
    public record ProposalView(Long id, String action, Long docId, String docKey, String docType, String typeLabel,
                               String title, String content, String summary, String reason, List<String> conflicts,
                               JsonNode lint, String status, String diff, boolean canAccept, boolean hasOtherDraft,
                               Long resultVersionId, OffsetDateTime createdAt) {
    }

    /** @param pending đang chờ AI trả lời: trang admin hỏi lại sau vài giây */
    public record SessionView(UUID id, String title, OffsetDateTime createdAt, boolean pending,
                              List<MessageView> messages, List<ProposalView> proposals) {
    }

    public record StartRequest(@Size(max = 200) String title, Long docId) {
    }

    public record SendRequest(@NotBlank(message = "Nhập yêu cầu") @Size(max = 10_000) String text, Long docId) {
    }

    // ------------------------------------------------------------------ phiên

    @Transactional(readOnly = true)
    public List<SessionRow> list(AuthInfo actor) {
        return sessions.findByUserIdAndTypeOrderByCreatedAtDesc(actor.email(), AssistantSession.KNOWLEDGE,
                        PageRequest.of(0, 30)).stream()
                .map(s -> new SessionRow(s.getId(), s.getTitle(), s.getCreatedAt(), s.getLastMessageAt())).toList();
    }

    @Transactional
    public SessionView start(StartRequest r, AuthInfo actor) {
        String title = r.title() != null && !r.title().isBlank() ? r.title().strip()
                : r.docId() != null ? docs.findById(r.docId()).map(KnowledgeDoc::getKey).orElse("Phiên soạn mới")
                : "Phiên soạn mới";
        AssistantSession s = sessions.save(AssistantSession.builder().userId(actor.email()).title(title).build());
        return view(s);
    }

    @Transactional(readOnly = true)
    public SessionView get(UUID id, AuthInfo actor) {
        return view(own(id, actor));
    }

    /** Ghi tin của quản trị viên rồi gọi AI chạy nền; trả ngay phiên với pending = true. */
    public SessionView send(UUID id, SendRequest r, AuthInfo actor) {
        if (!settings.ai().hasApiKey()) {
            throw new CommonException(HttpStatus.SERVICE_UNAVAILABLE, "Chưa cấu hình ANTHROPIC_API_KEY");
        }
        if (!processing.add(id)) {
            throw new CommonException(HttpStatus.CONFLICT, "AI đang trả lời tin trước, chờ một chút");
        }
        try {
            tx.executeWithoutResult(st -> {
                AssistantSession s = own(id, actor);
                String text = r.text().strip();
                if (r.docId() != null) {
                    text = docs.findById(r.docId()).map(d -> "Tài liệu đích: " + d.getKey() + "\n\n").orElse("")
                            + text;
                }
                messages.save(AssistantMessage.builder().sessionId(id).role(AssistantMessage.Role.USER)
                        .content(objectMapper.createObjectNode().put("text", text)).build());
                s.setLastMessageAt(OffsetDateTime.now());
                sessions.save(s);
            });
            executor.submit(() -> {
                try {
                    respond(id);
                } catch (RuntimeException e) {
                    log.error("AI hỗ trợ soạn lỗi (phiên {}): {}", id, e.getMessage(), e);
                } finally {
                    processing.remove(id);
                }
            });
        } catch (RuntimeException e) {
            processing.remove(id);
            throw e;
        }
        return tx.execute(st -> view(own(id, actor)));
    }

    // ------------------------------------------------------------------ duyệt

    @Transactional
    public ProposalView accept(Long proposalId, boolean force, AuthInfo actor) {
        ChangeProposal p = proposal(proposalId, actor);
        if (p.getStatus() != ChangeProposal.Status.PROPOSED) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Đề xuất này đã được xử lý");
        }
        if (hasError(p)) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Đề xuất có lỗi, nhắn AI sửa lại trước khi duyệt");
        }
        Optional<KnowledgeDoc> existing = docs.findByKey(p.getDocKey());
        Long resultVersion = null;
        if (p.getAction() == ChangeProposal.Action.RETIRE) {
            KnowledgeDoc d = existing.orElseThrow(() -> CommonException.notFound("Không có tài liệu " + p.getDocKey()));
            admin.archive(d.getId(), true, actor);
        } else {
            KnowledgeDoc d;
            if (existing.isEmpty()) {
                Long id = admin.create(new KnowledgeAdminService.CreateRequest(p.getDocType(), p.getDocKey(),
                        p.getTitle(), p.getContent()), actor).doc().id();
                d = docs.findById(id).orElseThrow();
            } else {
                d = existing.get();
                if (!force && otherDraft(d.getId(), p).isPresent()) {
                    throw new CommonException(HttpStatus.CONFLICT, "Tài liệu " + d.getKey()
                            + " đang có bản nháp khác; duyệt sẽ ghi đè bản nháp đó");
                }
            }
            resultVersion = admin.saveAiDraft(d, p.getTitle(), p.getContent(),
                    "AI hỗ trợ soạn: " + p.getSummary(), actor).getId();
            p.setDocId(d.getId());
        }
        p.setStatus(ChangeProposal.Status.ACCEPTED);
        p.setResultVersionId(resultVersion);
        p.setDecidedBy(actor.email());
        p.setDecidedAt(OffsetDateTime.now());
        proposals.save(p);
        audit.record(actor.id(), "knowledge.proposal.accept", "knowledge_proposal", p.getId(), null,
                Map.of("action", p.getAction(), "docKey", p.getDocKey()));
        return proposalView(p);
    }

    @Transactional
    public ProposalView reject(Long proposalId, AuthInfo actor) {
        ChangeProposal p = proposal(proposalId, actor);
        if (p.getStatus() != ChangeProposal.Status.PROPOSED) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Đề xuất này đã được xử lý");
        }
        p.setStatus(ChangeProposal.Status.REJECTED);
        p.setDecidedBy(actor.email());
        p.setDecidedAt(OffsetDateTime.now());
        proposals.save(p);
        return proposalView(p);
    }

    // ------------------------------------------------------------------ gọi AI

    /** Một lượt trả lời (chạy nền). Lỗi thì ghi tin ASSISTANT có trường error để trang admin hiện. */
    void respond(UUID sessionId) {
        KnowledgeSnapshot snapshot = knowledge.current();
        String system = systemPrompt(snapshot);
        String drafts = tx.execute(st -> draftsBlock());
        List<MessageParam> history = tx.execute(st -> history(sessionId));
        String model = settings.settings().assistantModel();
        AiRun run = AiRun.builder()
                .purpose(AiPurpose.KNOWLEDGE_ASSISTANT)
                .model(model)
                .kbVersion(snapshot.version())
                .catalogVersion(CATALOG_NONE)
                .mediaVersion(MEDIA_NONE)
                .promptVersion("knowledge-assistant:" + HashUtil.sha256Hex(instructions).substring(0, 16))
                .build();
        long started = System.nanoTime();
        JsonNode output = null;
        String error = null;
        try {
            MessageCreateParams params = MessageCreateParams.builder()
                    .model(model)
                    .maxTokens(MAX_TOKENS)
                    .thinking(ThinkingConfigAdaptive.builder().build())
                    .outputConfig(OutputConfig.builder()
                            .effort(OutputConfig.Effort.of("medium"))
                            .format(JsonOutputFormat.builder()
                                    .schema(ClaudeChatClient.schemaOf(AssistantSchema.schema())).build())
                            .build())
                    .systemOfTextBlockParams(List.of(
                            TextBlockParam.builder().text(system)
                                    .cacheControl(CacheControlEphemeral.builder().build()).build(),
                            TextBlockParam.builder().text(drafts).build()))
                    .messages(history)
                    .build();
            MessageAccumulator acc = MessageAccumulator.create();
            try (StreamResponse<RawMessageStreamEvent> stream = claude.sdk().messages().createStreaming(params,
                    RequestOptions.builder().timeout(TIMEOUT).build())) {
                stream.stream().forEach(acc::accumulate);
            }
            Message m = acc.message();
            run.setModel(m.model().asString());
            run.setInputTokens((int) m.usage().inputTokens());
            run.setOutputTokens((int) m.usage().outputTokens());
            run.setCacheReadTokens(m.usage().cacheReadInputTokens().map(Long::intValue).orElse(null));
            run.setCacheWriteTokens(m.usage().cacheCreationInputTokens().map(Long::intValue).orElse(null));
            run.setStopReason(m.stopReason().map(StopReason::asString).orElse(null));
            String text = m.content().stream().flatMap(b -> b.text().stream()).map(TextBlock::text)
                    .collect(Collectors.joining());
            if (m.stopReason().map(StopReason.MAX_TOKENS::equals).orElse(false)) {
                error = "Câu trả lời quá dài, bị cắt giữa chừng; nhắn AI chia nhỏ yêu cầu";
            } else if (m.stopReason().map(StopReason.REFUSAL::equals).orElse(false)) {
                error = "AI từ chối yêu cầu này";
            } else {
                output = objectMapper.readTree(text);
            }
        } catch (Exception e) {
            error = "Gọi AI lỗi: " + e.getMessage();
            log.warn("AI hỗ trợ soạn lỗi (phiên {}): {}", sessionId, e.toString());
        }
        run.setLatencyMs((int) ((System.nanoTime() - started) / 1_000_000));
        run.setOutput(output);
        run.setError(error == null ? null : truncate(error));
        JsonNode result = output;
        String failure = error;
        tx.executeWithoutResult(st -> save(sessionId, run, result, failure));
    }

    private void save(UUID sessionId, AiRun run, JsonNode output, String error) {
        AiRun savedRun = aiRuns.save(run);
        ObjectNode content = objectMapper.createObjectNode();
        if (error != null || output == null) {
            content.put("error", error == null ? "AI không trả kết quả" : error);
        } else {
            content.put("reply", output.path("reply").asText(""));
            proposals.findBySessionIdAndStatus(sessionId, ChangeProposal.Status.PROPOSED).forEach(p -> {
                p.setStatus(ChangeProposal.Status.SUPERSEDED);
                proposals.save(p);
            });
            ArrayNode ids = content.putArray("proposal_ids");
            for (JsonNode p : output.path("proposals")) {
                ids.add(proposals.save(toProposal(sessionId, p)).getId());
            }
        }
        messages.save(AssistantMessage.builder().sessionId(sessionId).role(AssistantMessage.Role.ASSISTANT)
                .content(content).aiRunId(savedRun.getId()).build());
        sessions.findById(sessionId).ifPresent(s -> {
            s.setLastMessageAt(OffsetDateTime.now());
            sessions.save(s);
        });
    }

    private ChangeProposal toProposal(UUID sessionId, JsonNode p) {
        String key = p.path("doc_key").asText("").strip();
        KnowledgeDocType type = KnowledgeDocType.ofKey(key).orElse(null);
        ChangeProposal.Action action = ChangeProposal.Action.valueOf(p.path("action").asText("UPDATE"));
        Optional<KnowledgeDoc> doc = docs.findByKey(key);
        List<KnowledgeLinter.Issue> issues = new ArrayList<>();
        if (!KEY.matcher(key).matches() || type == null) {
            issues.add(error("Mã tài liệu \"" + key + "\" sai quy ước thư mục"));
        } else if (!type.name().equals(p.path("doc_type").asText())) {
            issues.add(error("Loại tài liệu không khớp thư mục của mã " + key));
        } else if (!ALLOWED.contains(type)) {
            issues.add(error("Loại " + type.label() + " không sửa qua AI hỗ trợ soạn (rule sửa ở trang Quy tắc)"));
        }
        if (action == ChangeProposal.Action.CREATE && doc.isPresent()) {
            action = ChangeProposal.Action.UPDATE; // AI tưởng chưa có: coi như sửa
        }
        if (action != ChangeProposal.Action.CREATE && doc.isEmpty()) {
            issues.add(error("Chưa có tài liệu " + key + " để sửa / ngừng dùng"));
        }
        if (type == KnowledgeDocType.PROMPT && action != ChangeProposal.Action.UPDATE) {
            issues.add(error("Hướng dẫn hệ thống chỉ được sửa, không tạo / ngừng dùng"));
        }
        String content = p.path("content").asText("");
        if (action != ChangeProposal.Action.RETIRE) {
            if (content.isBlank()) {
                issues.add(error("Đề xuất không có nội dung"));
            } else {
                issues.addAll(KnowledgeLinter.lint(type, content, KnowledgeLinter.terms(knowledge.current()
                        .doc("guide/thuat-ngu").map(KnowledgeSnapshot.Doc::content).orElse(""))));
            }
        }
        Long base = doc.flatMap(d -> versions.findByDocIdOrderByVersionNoDesc(d.getId()).stream()
                .filter(v -> v.getStatus() == KnowledgeVersion.Status.FROZEN).findFirst())
                .map(KnowledgeVersion::getId).orElse(null);
        return ChangeProposal.builder()
                .sessionId(sessionId)
                .docId(doc.map(KnowledgeDoc::getId).orElse(null))
                .docKey(key)
                .docType(type)
                .title(p.path("title").asText(key).strip())
                .action(action)
                .baseVersionId(base)
                .content(action == ChangeProposal.Action.RETIRE ? null : content)
                .summary(p.path("change_summary").asText("").isBlank() ? "(không có tóm tắt)"
                        : p.path("change_summary").asText().strip())
                .reason(p.path("reason").asText(null))
                .lintReport(objectMapper.valueToTree(issues))
                .conflicts(p.path("conflicts").isArray() ? p.get("conflicts") : objectMapper.createArrayNode())
                .build();
    }

    /** Khối cố định (cache): hướng dẫn + tài liệu hướng dẫn soạn + toàn bộ kiến thức đang chạy, nội dung gốc */
    private String systemPrompt(KnowledgeSnapshot s) {
        String company = settings.ai().companyName();
        StringBuilder sb = new StringBuilder(instructions.replace(KnowledgeBase.COMPANY_PLACEHOLDER,
                company == null || company.isBlank() ? "công ty" : company).strip());
        sb.append("\n\n# Hướng dẫn soạn\n\n");
        for (KnowledgeSnapshot.Doc d : s.docs()) {
            if (d.type() == KnowledgeDocType.GUIDE || d.type() == KnowledgeDocType.TEMPLATE) {
                sb.append(tag(d));
            }
        }
        sb.append("\n# Kiến thức đang chạy\n\n");
        for (KnowledgeSnapshot.Doc d : s.docs()) {
            if (d.type().botReads() || d.type() == KnowledgeDocType.PROMPT || d.type() == KnowledgeDocType.RULE) {
                sb.append(tag(d));
            }
        }
        return sb.toString().strip();
    }

    private String draftsBlock() {
        Map<Long, KnowledgeDoc> byId = docs.findAll().stream()
                .collect(Collectors.toMap(KnowledgeDoc::getId, Function.identity()));
        StringBuilder sb = new StringBuilder("# Bản nháp đang có (chưa phát hành)\n\n");
        List<KnowledgeVersion> drafts = versions.findByStatus(KnowledgeVersion.Status.DRAFT);
        if (drafts.isEmpty()) {
            sb.append("Không có bản nháp nào.");
        }
        for (KnowledgeVersion v : drafts) {
            KnowledgeDoc d = byId.get(v.getDocId());
            sb.append(tag(new KnowledgeSnapshot.Doc(d.getKey(), d.getType(), v.getTitle(), v.getContent())));
        }
        return sb.toString().strip();
    }

    private static String tag(KnowledgeSnapshot.Doc d) {
        return "<tai_lieu ma=\"" + d.key() + "\" loai=\"" + d.type().name() + "\" tieu_de=\""
                + d.title().replace("\"", "'") + "\">\n" + d.content().strip() + "\n</tai_lieu>\n\n";
    }

    /** Lịch sử phiên thành lượt user / assistant xen kẽ; tin liền nhau cùng phía được gộp */
    private List<MessageParam> history(UUID sessionId) {
        Map<Long, ChangeProposal> byId = proposals.findBySessionIdOrderByIdAsc(sessionId).stream()
                .collect(Collectors.toMap(ChangeProposal::getId, Function.identity()));
        List<MessageParam> out = new ArrayList<>();
        MessageParam.Role lastRole = null;
        StringBuilder buf = new StringBuilder();
        for (AssistantMessage m : messages.findBySessionIdOrderByIdAsc(sessionId)) {
            MessageParam.Role role = m.getRole() == AssistantMessage.Role.USER ? MessageParam.Role.USER
                    : MessageParam.Role.ASSISTANT;
            String text = m.getRole() == AssistantMessage.Role.USER ? m.getContent().path("text").asText()
                    : assistantText(m.getContent(), byId);
            if (text.isBlank()) {
                continue;
            }
            if (role != lastRole && lastRole != null) {
                out.add(MessageParam.builder().role(lastRole).content(buf.toString()).build());
                buf.setLength(0);
            }
            if (!buf.isEmpty()) {
                buf.append("\n\n");
            }
            buf.append(text);
            lastRole = role;
        }
        if (lastRole != null) {
            out.add(MessageParam.builder().role(lastRole).content(buf.toString()).build());
        }
        return out;
    }

    private static String assistantText(JsonNode content, Map<Long, ChangeProposal> byId) {
        if (content.has("error")) {
            return "";
        }
        StringBuilder sb = new StringBuilder(content.path("reply").asText());
        for (JsonNode id : content.path("proposal_ids")) {
            ChangeProposal p = byId.get(id.asLong());
            if (p != null) {
                sb.append("\n- Đề xuất ").append(p.getAction()).append(' ').append(p.getDocKey()).append(": ")
                        .append(p.getSummary()).append(" (").append(statusLabel(p.getStatus())).append(')');
            }
        }
        return sb.toString();
    }

    private static String statusLabel(ChangeProposal.Status s) {
        return switch (s) {
            case PROPOSED -> "chờ duyệt";
            case ACCEPTED -> "đã duyệt thành bản nháp";
            case REJECTED -> "bị từ chối";
            case SUPERSEDED -> "đã thay bằng đề xuất sau";
        };
    }

    // ------------------------------------------------------------------ xem

    private SessionView view(AssistantSession s) {
        // Xét cờ trước khi đọc: lượt AI lưu tin và đề xuất (commit) rồi mới bỏ cờ, nên cờ đã tắt thì các câu đọc dưới
        // thấy đủ. Đọc trước rồi mới xét cờ có thể ra tin AI mà thiếu đề xuất, trang ngừng hỏi lại và không hiện thẻ.
        boolean pending = processing.contains(s.getId());
        List<ChangeProposal> ps = proposals.findBySessionIdOrderByIdAsc(s.getId());
        List<MessageView> ms = messages.findBySessionIdOrderByIdAsc(s.getId()).stream().map(m -> {
            List<Long> ids = new ArrayList<>();
            m.getContent().path("proposal_ids").forEach(n -> ids.add(n.asLong()));
            String text = m.getRole() == AssistantMessage.Role.USER ? m.getContent().path("text").asText()
                    : m.getContent().path("reply").asText(null);
            return new MessageView(m.getId(), m.getRole().name(), text, m.getContent().path("error").asText(null),
                    ids, m.getCreatedAt());
        }).toList();
        return new SessionView(s.getId(), s.getTitle(), s.getCreatedAt(), pending, ms,
                ps.stream().map(this::proposalView).toList());
    }

    private ProposalView proposalView(ChangeProposal p) {
        String current = "";
        boolean otherDraft = false;
        if (p.getDocKey() != null) {
            Optional<KnowledgeDoc> d = docs.findByKey(p.getDocKey());
            if (d.isPresent()) {
                List<KnowledgeVersion> vs = versions.findByDocIdOrderByVersionNoDesc(d.get().getId());
                current = vs.stream().filter(v -> v.getStatus() == KnowledgeVersion.Status.DRAFT
                                || v.getStatus() == KnowledgeVersion.Status.FROZEN)
                        .findFirst().map(KnowledgeVersion::getContent).orElse("");
                otherDraft = otherDraft(d.get().getId(), p).isPresent();
            }
        }
        String diff = p.getStatus() == ChangeProposal.Status.PROPOSED
                ? KnowledgeDiff.unified(p.getDocKey() + " (hiện tại)", current, p.getDocKey() + " (đề xuất)",
                p.getContent() == null ? "" : p.getContent(), p.getDocType() == KnowledgeDocType.RULE)
                : null;
        List<String> conflicts = new ArrayList<>();
        if (p.getConflicts() != null) {
            p.getConflicts().forEach(c -> conflicts.add(c.asText()));
        }
        return new ProposalView(p.getId(), p.getAction().name(), p.getDocId(), p.getDocKey(),
                p.getDocType() == null ? null : p.getDocType().name(),
                p.getDocType() == null ? null : p.getDocType().label(), p.getTitle(), p.getContent(), p.getSummary(),
                p.getReason(), conflicts, p.getLintReport(), p.getStatus().name(), diff,
                p.getStatus() == ChangeProposal.Status.PROPOSED && !hasError(p), otherDraft, p.getResultVersionId(),
                p.getCreatedAt());
    }

    /**
     * Bản nháp được lưu sau lúc AI đọc các bản nháp để soạn đề xuất này: duyệt sẽ ghi đè phần AI chưa thấy nên phải
     * xác nhận. Bản nháp có từ trước thì AI đã đọc và soạn tiếp trên đó, duyệt thẳng.
     */
    private Optional<KnowledgeVersion> otherDraft(Long docId, ChangeProposal p) {
        OffsetDateTime seen = draftsSeenAt(p);
        return versions.findByDocIdAndStatus(docId, KnowledgeVersion.Status.DRAFT)
                .filter(v -> !v.getId().equals(p.getResultVersionId()))
                .filter(v -> seen == null || v.getUpdatedAt().isAfter(seen));
    }

    /** AI đọc các bản nháp ngay sau tin của người soạn: lấy tin gần nhất của người soạn trước đề xuất */
    private OffsetDateTime draftsSeenAt(ChangeProposal p) {
        OffsetDateTime seen = null;
        for (AssistantMessage m : messages.findBySessionIdOrderByIdAsc(p.getSessionId())) {
            if (m.getRole() == AssistantMessage.Role.USER && !m.getCreatedAt().isAfter(p.getCreatedAt())) {
                seen = m.getCreatedAt();
            }
        }
        return seen;
    }

    private static boolean hasError(ChangeProposal p) {
        if (p.getLintReport() == null) {
            return false;
        }
        for (JsonNode i : p.getLintReport()) {
            if ("ERROR".equals(i.path("level").asText())) {
                return true;
            }
        }
        return false;
    }

    private static KnowledgeLinter.Issue error(String message) {
        return new KnowledgeLinter.Issue(KnowledgeLinter.Level.ERROR, "PROPOSAL", message, null);
    }

    private AssistantSession own(UUID id, AuthInfo actor) {
        AssistantSession s = sessions.findById(id).orElseThrow(() -> CommonException.notFound("Không có phiên " + id));
        if (!s.getUserId().equals(actor.email()) && !actor.hasAnyRole(RoleCode.SYSTEM_ADMIN)) {
            throw CommonException.forbidden("Phiên của người khác");
        }
        return s;
    }

    private ChangeProposal proposal(Long id, AuthInfo actor) {
        ChangeProposal p = proposals.findById(id).orElseThrow(() -> CommonException.notFound("Không có đề xuất " + id));
        own(p.getSessionId(), actor);
        return p;
    }

    private static String truncate(String s) {
        return s.length() <= 2000 ? s : s.substring(0, 2000);
    }
}
