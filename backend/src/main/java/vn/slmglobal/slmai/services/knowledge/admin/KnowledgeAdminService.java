package vn.slmglobal.slmai.services.knowledge.admin;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.common.security.AuthInfo;
import vn.slmglobal.slmai.common.web.PageResponse;
import vn.slmglobal.slmai.services.auth.AuditLogService;
import vn.slmglobal.slmai.services.knowledge.KnowledgeBase;
import vn.slmglobal.slmai.services.knowledge.KnowledgeDocType;
import vn.slmglobal.slmai.services.knowledge.KnowledgeHash;
import vn.slmglobal.slmai.services.knowledge.KnowledgeSnapshot;
import vn.slmglobal.slmai.services.knowledge.entity.KnowledgeDoc;
import vn.slmglobal.slmai.services.knowledge.entity.KnowledgeRelease;
import vn.slmglobal.slmai.services.knowledge.entity.KnowledgeReleaseItem;
import vn.slmglobal.slmai.services.knowledge.entity.KnowledgeVersion;
import vn.slmglobal.slmai.services.knowledge.repository.KnowledgeDocRepository;
import vn.slmglobal.slmai.services.knowledge.repository.KnowledgeReleaseItemRepository;
import vn.slmglobal.slmai.services.knowledge.repository.KnowledgeReleaseRepository;
import vn.slmglobal.slmai.services.knowledge.repository.KnowledgeVersionRepository;
import vn.slmglobal.slmai.services.knowledge.rules.RuleCodec;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Soạn kiến thức trên admin (mục 5.9): danh sách tài liệu, tạo, lưu nháp, bỏ nháp, lưu trữ, lịch sử phiên bản,
 * diff, khôi phục bản cũ, rule YAML sửa bằng form. Mỗi tài liệu tối đa một bản nháp dùng chung; bot chỉ đổi khi phát
 * hành (KnowledgeReleaseService).
 *
 * <p>Chống ghi đè: bản nháp nhớ bản gốc đã sửa từ đó (phien_ban_goc_id = phiên bản mới nhất lúc bắt đầu sửa). Khi
 * lưu, nếu đã có phiên bản mới hơn được đóng băng, hoặc người khác vừa lưu bản nháp, trả 409 kèm nội dung mới để
 * xem diff; gửi lại với {@code force = true} để ghi đè.
 */
@Service
@RequiredArgsConstructor
public class KnowledgeAdminService {

    static final String TARGET = "knowledge_doc";
    private static final Pattern KEY = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*(/[a-z0-9]+(-[a-z0-9]+)*)+");
    private static final int MAX_CONTENT = 200_000;
    private static final String TERMS_KEY = "guide/thuat-ngu";
    private static final Map<KnowledgeDocType, String> TEMPLATES = Map.of(
            KnowledgeDocType.FAQ, "guide/mau/faq",
            KnowledgeDocType.PLAYBOOK, "guide/mau/playbook",
            KnowledgeDocType.POLICY, "guide/mau/policy",
            KnowledgeDocType.EXAMPLE, "guide/mau/example");

    private final KnowledgeDocRepository docs;
    private final KnowledgeVersionRepository versions;
    private final KnowledgeReleaseRepository releases;
    private final KnowledgeReleaseItemRepository items;
    private final KnowledgeBase knowledge;
    private final KnowledgeTokenCounter tokens;
    private final AuditLogService audit;

    // ------------------------------------------------------------------ DTO

    /**
     * @param activeVersionNo phiên bản bot đang dùng (bản phát hành ACTIVE); null khi chưa phát hành
     * @param latestVersionNo phiên bản đóng băng mới nhất (có thể nằm trong bản phát hành chưa kích hoạt)
     */
    public record DocRow(Long id, String key, KnowledgeDocType type, String typeLabel, String format, String title,
                         Integer activeVersionNo, Integer latestVersionNo, Long draftVersionId, String draftAuthor,
                         OffsetDateTime draftUpdatedAt, boolean archived, boolean botReads) {
    }

    public record VersionSummary(Long id, int versionNo, String status, String title, String contentHash,
                                 Long baseVersionId, String changeNote, String author, boolean aiAssisted,
                                 OffsetDateTime createdAt, OffsetDateTime updatedAt, boolean active) {
    }

    public record VersionDto(Long id, Long docId, int versionNo, String status, String title, String content,
                             String contentHash, Long baseVersionId, String changeNote, String author,
                             boolean aiAssisted, OffsetDateTime createdAt, OffsetDateTime updatedAt, boolean active) {
    }

    /**
     * @param value      rule YAML đọc thành JSON (bản nháp nếu có, không thì bản mới nhất) cho form; null với Markdown
     * @param ruleErrors lỗi của rule đó (YAML sửa tay sai)
     */
    public record DocDetail(DocRow doc, VersionDto active, VersionDto latest, VersionDto draft,
                            List<VersionSummary> versions, JsonNode value, List<String> ruleErrors) {
    }

    public record Filter(KnowledgeDocType type, String keyword, String status) {
    }

    public record CreateRequest(
            @NotNull(message = "Chọn loại tài liệu") KnowledgeDocType type,
            @NotBlank(message = "Nhập mã tài liệu") @Size(max = 200) String key,
            @NotBlank(message = "Nhập tiêu đề") @Size(max = 300) String title,
            String content) {
    }

    /**
     * @param content                Markdown, hoặc YAML của rule
     * @param value                  giá trị rule từ form (thay cho content với tài liệu YAML)
     * @param baseVersionId          phiên bản mới nhất lúc mở trang soạn (khi chưa có nháp)
     * @param expectedDraftUpdatedAt sua_luc của bản nháp lúc mở trang soạn; null khi lúc đó chưa có nháp
     * @param force                  bỏ qua cảnh báo xung đột, ghi đè
     */
    public record SaveDraftRequest(
            @NotBlank(message = "Nhập tiêu đề") @Size(max = 300) String title,
            String content,
            JsonNode value,
            @Size(max = 1000) String changeNote,
            Long baseVersionId,
            OffsetDateTime expectedDraftUpdatedAt,
            boolean force) {
    }

    public record Diff(VersionSummary from, VersionSummary to, String diff) {
    }

    public record LintRequest(KnowledgeDocType type, String key, String content) {
    }

    public record TokenRequest(@NotBlank String key, String content) {
    }

    // ------------------------------------------------------------------ đọc

    @Transactional(readOnly = true)
    public PageResponse<DocRow> list(Filter f, int page, int size) {
        Map<Long, KnowledgeVersion> all = versions.findAll().stream()
                .collect(Collectors.toMap(KnowledgeVersion::getId, Function.identity()));
        Map<Long, Long> active = activeVersionByDoc();
        String kw = f.keyword() == null ? "" : f.keyword().strip().toLowerCase(Locale.ROOT);
        List<DocRow> rows = new ArrayList<>();
        for (KnowledgeDoc d : docs.findAllByOrderByKeyAsc()) {
            DocRow r = row(d, all.values().stream().filter(v -> v.getDocId().equals(d.getId())).toList(),
                    active.get(d.getId()) == null ? null : all.get(active.get(d.getId())));
            boolean archivedView = "ARCHIVED".equals(f.status());
            if (r.archived() != archivedView) {
                continue;
            }
            if (f.type() != null && r.type() != f.type()) {
                continue;
            }
            if ("DRAFT".equals(f.status()) && r.draftVersionId() == null) {
                continue;
            }
            if ("NOT_RELEASED".equals(f.status()) && r.activeVersionNo() != null) {
                continue;
            }
            if (!kw.isEmpty() && !r.key().contains(kw)
                    && (r.title() == null || !r.title().toLowerCase(Locale.ROOT).contains(kw))) {
                continue;
            }
            rows.add(r);
        }
        int from = Math.min(page * size, rows.size());
        int to = Math.min(from + size, rows.size());
        return new PageResponse<>(rows.subList(from, to), rows.size(), page, size,
                (int) Math.ceil(rows.size() / (double) Math.max(1, size)));
    }

    @Transactional(readOnly = true)
    public DocDetail get(Long id) {
        return detail(doc(id));
    }

    @Transactional(readOnly = true)
    public DocDetail getByKey(String key) {
        return detail(docs.findByKey(key).orElseThrow(() -> CommonException.notFound("Không có tài liệu " + key)));
    }

    @Transactional(readOnly = true)
    public VersionDto version(Long versionId) {
        KnowledgeVersion v = versions.findById(versionId)
                .orElseThrow(() -> CommonException.notFound("Không có phiên bản " + versionId));
        return dto(v, activeVersionByDoc());
    }

    /** Diff hai phiên bản; mặc định bản đang chạy (hoặc mới nhất) → bản nháp (hoặc mới nhất) */
    @Transactional(readOnly = true)
    public Diff diff(Long docId, Long fromId, Long toId) {
        KnowledgeDoc d = doc(docId);
        List<KnowledgeVersion> vs = versions.findByDocIdOrderByVersionNoDesc(d.getId());
        Map<Long, Long> active = activeVersionByDoc();
        KnowledgeVersion from = fromId != null ? own(vs, fromId)
                : vs.stream().filter(v -> v.getId().equals(active.get(d.getId()))).findFirst()
                .orElse(latestFrozen(vs).orElse(null));
        KnowledgeVersion to = toId != null ? own(vs, toId)
                : draft(vs).orElse(latestFrozen(vs).orElse(null));
        String text = KnowledgeDiff.unified(name(d, from), from == null ? "" : from.getContent(), name(d, to),
                to == null ? "" : to.getContent());
        return new Diff(from == null ? null : summary(from, active), to == null ? null : summary(to, active), text);
    }

    /** 4 rule YAML (trang Quy tắc) */
    @Transactional(readOnly = true)
    public List<DocDetail> rules() {
        List<DocDetail> out = new ArrayList<>();
        for (String key : RuleCodec.KEYS) {
            docs.findByKey(key).ifPresent(d -> out.add(detail(d)));
        }
        return out;
    }

    public List<KnowledgeLinter.Issue> lint(LintRequest r) {
        KnowledgeDocType type = r.type() != null ? r.type() : KnowledgeDocType.ofKey(r.key()).orElse(null);
        if (type != null && type.yaml()) {
            return RuleCodec.validate(r.key(), r.content()).stream()
                    .map(e -> new KnowledgeLinter.Issue(KnowledgeLinter.Level.ERROR, "RULE", e, null)).toList();
        }
        String terms = knowledge.current().doc(TERMS_KEY).map(KnowledgeSnapshot.Doc::content).orElse("");
        return KnowledgeLinter.lint(type, r.content(), KnowledgeLinter.terms(terms));
    }

    /** Token của khối cố định (hướng dẫn + kiến thức bot đọc) nếu tài liệu này có nội dung như đang soạn */
    public KnowledgeTokenCounter.Count countTokens(TokenRequest r) {
        KnowledgeSnapshot s = knowledge.current();
        KnowledgeDocType type = KnowledgeDocType.ofKey(r.key()).orElse(KnowledgeDocType.GUIDE);
        List<KnowledgeSnapshot.Doc> list = new ArrayList<>(s.docs().stream()
                .filter(d -> !d.key().equals(r.key())).toList());
        list.add(new KnowledgeSnapshot.Doc(r.key(), type, r.key(), r.content() == null ? "" : r.content()));
        KnowledgeSnapshot trial = new KnowledgeSnapshot(s.releaseId(), s.version(), list, s.rules());
        return tokens.count(knowledge.renderPrompt(trial) + "\n\n# Tài liệu kiến thức\n\n"
                + knowledge.renderKnowledge(trial));
    }

    // ------------------------------------------------------------------ ghi

    @Transactional
    public DocDetail create(CreateRequest r, AuthInfo actor) {
        String key = r.key().strip();
        if (!KEY.matcher(key).matches()) {
            throw new CommonException(HttpStatus.BAD_REQUEST,
                    "Mã tài liệu viết thường, không dấu, nối bằng gạch ngang, có thư mục: ví dụ policies/bao-hanh");
        }
        if (r.type() == KnowledgeDocType.RULE || r.type() == KnowledgeDocType.PROMPT) {
            throw new CommonException(HttpStatus.BAD_REQUEST,
                    "Rule và hướng dẫn hệ thống là bộ cố định, sửa ở trang Quy tắc, không tạo thêm");
        }
        if (KnowledgeDocType.ofKey(key).orElse(null) != r.type()) {
            throw new CommonException(HttpStatus.BAD_REQUEST,
                    "Tài liệu loại " + r.type().label() + " phải có mã bắt đầu bằng " + r.type().folder() + "/");
        }
        if (docs.existsByKey(key)) {
            throw CommonException.conflict("Đã có tài liệu " + key);
        }
        String content = r.content() != null && !r.content().isBlank() ? r.content() : starter(r.type(), r.title());
        checkSize(content);
        KnowledgeDoc d = docs.save(KnowledgeDoc.builder().key(key).type(r.type()).format(r.type().format())
                .createdBy(actor.email()).build());
        KnowledgeVersion v = versions.save(KnowledgeVersion.builder()
                .docId(d.getId()).versionNo(1).status(KnowledgeVersion.Status.DRAFT)
                .title(r.title().strip()).content(content).contentHash(KnowledgeHash.content(content))
                .changeNote("Tạo mới").author(actor.email()).updatedAt(now()).build());
        audit.record(actor.id(), "knowledge.doc.create", TARGET, key, null,
                Map.of("type", r.type(), "title", v.getTitle()));
        return detail(d);
    }

    @Transactional
    public DocDetail saveDraft(Long docId, SaveDraftRequest r, AuthInfo actor) {
        KnowledgeDoc d = doc(docId);
        if (d.isArchived()) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Tài liệu đã lưu trữ, bỏ lưu trữ trước khi sửa");
        }
        String content = content(d, r);
        List<KnowledgeVersion> vs = versions.findByDocIdOrderByVersionNoDesc(d.getId());
        KnowledgeVersion latest = latestFrozen(vs).orElse(null);
        Long latestId = latest == null ? null : latest.getId();
        KnowledgeVersion draft = draft(vs).orElse(null);
        Map<Long, Long> active = activeVersionByDoc();

        if (!r.force()) {
            if (draft != null && (r.expectedDraftUpdatedAt() == null
                    || !sameInstant(draft.getUpdatedAt(), r.expectedDraftUpdatedAt()))) {
                throw conflict("DRAFT_CHANGED", "Bản nháp vừa được " + draft.getAuthor()
                        + " lưu sau khi bạn mở trang. Xem khác biệt rồi lưu đè hoặc tải lại.", d, draft, content,
                        active);
            }
            Long base = draft != null ? draft.getBaseVersionId() : r.baseVersionId();
            if (!Objects.equals(base, latestId)) {
                throw conflict("BASE_CHANGED", "Đã có phiên bản " + (latest == null ? "" : "v" + latest.getVersionNo())
                        + " mới hơn bản bạn bắt đầu sửa. Xem khác biệt rồi lưu đè hoặc tải lại.", d, latest, content,
                        active);
            }
        }
        String before = draft == null ? null : draft.getContentHash();
        if (draft == null) {
            draft = KnowledgeVersion.builder()
                    .docId(d.getId())
                    .versionNo(versions.maxVersionNo(d.getId()) + 1)
                    .status(KnowledgeVersion.Status.DRAFT)
                    .build();
        }
        draft.setTitle(r.title().strip());
        draft.setContent(content);
        draft.setContentHash(KnowledgeHash.content(content));
        draft.setChangeNote(blankToNull(r.changeNote()));
        draft.setAuthor(actor.email());
        draft.setBaseVersionId(latestId);
        draft.setUpdatedAt(now());
        versions.save(draft);
        audit.record(actor.id(), "knowledge.draft.save", TARGET, d.getKey(), before == null ? null
                : Map.of("hash", before), Map.of("version", draft.getVersionNo(), "hash", draft.getContentHash()));
        return detail(d);
    }

    /** Lưu nháp từ một bản đề xuất của AI hỗ trợ soạn (mục 5.10 B): ghi đè nháp đang có, đánh dấu co_ai_ho_tro */
    @Transactional
    public KnowledgeVersion saveAiDraft(KnowledgeDoc d, String title, String content, String changeNote,
                                        AuthInfo actor) {
        checkSize(content);
        List<KnowledgeVersion> vs = versions.findByDocIdOrderByVersionNoDesc(d.getId());
        KnowledgeVersion draft = draft(vs).orElseGet(() -> KnowledgeVersion.builder().docId(d.getId())
                .versionNo(versions.maxVersionNo(d.getId()) + 1).status(KnowledgeVersion.Status.DRAFT).build());
        draft.setTitle(title);
        draft.setContent(content);
        draft.setContentHash(KnowledgeHash.content(content));
        draft.setChangeNote(changeNote);
        draft.setAuthor(actor.email());
        draft.setAiAssisted(true);
        draft.setBaseVersionId(latestFrozen(vs).map(KnowledgeVersion::getId).orElse(null));
        draft.setUpdatedAt(now());
        KnowledgeVersion saved = versions.save(draft);
        audit.record(actor.id(), "knowledge.draft.ai_accept", TARGET, d.getKey(), null,
                Map.of("version", saved.getVersionNo(), "hash", saved.getContentHash()));
        return saved;
    }

    @Transactional
    public DocDetail discardDraft(Long docId, AuthInfo actor) {
        KnowledgeDoc d = doc(docId);
        KnowledgeVersion draft = versions.findByDocIdAndStatus(d.getId(), KnowledgeVersion.Status.DRAFT)
                .orElseThrow(() -> new CommonException(HttpStatus.BAD_REQUEST, "Tài liệu không có bản nháp"));
        draft.setStatus(KnowledgeVersion.Status.DISCARDED);
        draft.setUpdatedAt(now());
        versions.save(draft);
        audit.record(actor.id(), "knowledge.draft.discard", TARGET, d.getKey(),
                Map.of("version", draft.getVersionNo()), null);
        return detail(d);
    }

    @Transactional
    public DocDetail archive(Long docId, boolean archived, AuthInfo actor) {
        KnowledgeDoc d = doc(docId);
        if (archived && (d.getType() == KnowledgeDocType.RULE || d.getType() == KnowledgeDocType.PROMPT)) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Rule và hướng dẫn hệ thống luôn phải có, không lưu trữ");
        }
        if (d.isArchived() != archived) {
            d.setArchived(archived);
            docs.save(d);
            audit.record(actor.id(), archived ? "knowledge.doc.archive" : "knowledge.doc.unarchive", TARGET,
                    d.getKey(), Map.of("archived", !archived), Map.of("archived", archived));
        }
        return detail(d);
    }

    /** "Khôi phục bản này": tạo (hoặc ghi đè) bản nháp bằng nội dung một phiên bản cũ */
    @Transactional
    public DocDetail restore(Long docId, Long versionId, AuthInfo actor) {
        KnowledgeDoc d = doc(docId);
        List<KnowledgeVersion> vs = versions.findByDocIdOrderByVersionNoDesc(d.getId());
        KnowledgeVersion source = own(vs, versionId);
        if (source.getStatus() == KnowledgeVersion.Status.DRAFT) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Đây đang là bản nháp");
        }
        KnowledgeVersion draft = draft(vs).orElseGet(() -> KnowledgeVersion.builder().docId(d.getId())
                .versionNo(versions.maxVersionNo(d.getId()) + 1).status(KnowledgeVersion.Status.DRAFT).build());
        draft.setTitle(source.getTitle());
        draft.setContent(source.getContent());
        draft.setContentHash(source.getContentHash());
        draft.setChangeNote("Khôi phục nội dung phiên bản v" + source.getVersionNo());
        draft.setAuthor(actor.email());
        draft.setAiAssisted(false);
        draft.setBaseVersionId(latestFrozen(vs).map(KnowledgeVersion::getId).orElse(null));
        draft.setUpdatedAt(now());
        versions.save(draft);
        audit.record(actor.id(), "knowledge.draft.restore", TARGET, d.getKey(), null,
                Map.of("fromVersion", source.getVersionNo(), "draftVersion", draft.getVersionNo()));
        return detail(d);
    }

    // ------------------------------------------------------------------ tiện ích

    KnowledgeDoc doc(Long id) {
        return docs.findById(id).orElseThrow(() -> CommonException.notFound("Không có tài liệu " + id));
    }

    /** docId → id phiên bản trong bản phát hành đang chạy */
    Map<Long, Long> activeVersionByDoc() {
        return releases.findFirstByStatus(KnowledgeRelease.Status.ACTIVE)
                .map(r -> items.findByReleaseId(r.getId()).stream()
                        .collect(Collectors.toMap(KnowledgeReleaseItem::getDocId, KnowledgeReleaseItem::getVersionId)))
                .orElse(Map.of());
    }

    static Optional<KnowledgeVersion> latestFrozen(List<KnowledgeVersion> vs) {
        return vs.stream().filter(v -> v.getStatus() == KnowledgeVersion.Status.FROZEN)
                .max(Comparator.comparingInt(KnowledgeVersion::getVersionNo));
    }

    static Optional<KnowledgeVersion> draft(List<KnowledgeVersion> vs) {
        return vs.stream().filter(v -> v.getStatus() == KnowledgeVersion.Status.DRAFT).findFirst();
    }

    private DocDetail detail(KnowledgeDoc d) {
        List<KnowledgeVersion> vs = versions.findByDocIdOrderByVersionNoDesc(d.getId());
        Map<Long, Long> active = activeVersionByDoc();
        KnowledgeVersion activeVersion = vs.stream().filter(v -> v.getId().equals(active.get(d.getId())))
                .findFirst().orElse(null);
        KnowledgeVersion latest = latestFrozen(vs).orElse(null);
        KnowledgeVersion draft = draft(vs).orElse(null);
        JsonNode value = null;
        List<String> ruleErrors = List.of();
        KnowledgeVersion shown = draft != null ? draft : latest;
        if (d.getFormat() == KnowledgeDocType.DocFormat.YAML && shown != null) {
            value = RuleCodec.toJson(shown.getContent());
            ruleErrors = RuleCodec.validate(d.getKey(), shown.getContent());
        }
        return new DocDetail(row(d, vs, activeVersion),
                activeVersion == null ? null : dto(activeVersion, active),
                latest == null ? null : dto(latest, active),
                draft == null ? null : dto(draft, active),
                vs.stream().filter(v -> v.getStatus() != KnowledgeVersion.Status.DISCARDED)
                        .map(v -> summary(v, active)).toList(),
                value, ruleErrors);
    }

    private DocRow row(KnowledgeDoc d, List<KnowledgeVersion> vs, KnowledgeVersion activeVersion) {
        KnowledgeVersion latest = latestFrozen(vs).orElse(null);
        KnowledgeVersion draft = draft(vs).orElse(null);
        KnowledgeVersion shown = draft != null ? draft : latest;
        return new DocRow(d.getId(), d.getKey(), d.getType(), d.getType().label(), d.getFormat().name(),
                shown == null ? d.getKey() : shown.getTitle(),
                activeVersion == null ? null : activeVersion.getVersionNo(),
                latest == null ? null : latest.getVersionNo(),
                draft == null ? null : draft.getId(),
                draft == null ? null : draft.getAuthor(),
                draft == null ? null : draft.getUpdatedAt(),
                d.isArchived(), d.getType().botReads());
    }

    private static VersionSummary summary(KnowledgeVersion v, Map<Long, Long> active) {
        return new VersionSummary(v.getId(), v.getVersionNo(), v.getStatus().name(), v.getTitle(), v.getContentHash(),
                v.getBaseVersionId(), v.getChangeNote(), v.getAuthor(), v.isAiAssisted(), v.getCreatedAt(),
                v.getUpdatedAt(), v.getId().equals(active.get(v.getDocId())));
    }

    private static VersionDto dto(KnowledgeVersion v, Map<Long, Long> active) {
        return new VersionDto(v.getId(), v.getDocId(), v.getVersionNo(), v.getStatus().name(), v.getTitle(),
                v.getContent(), v.getContentHash(), v.getBaseVersionId(), v.getChangeNote(), v.getAuthor(),
                v.isAiAssisted(), v.getCreatedAt(), v.getUpdatedAt(), v.getId().equals(active.get(v.getDocId())));
    }

    private static KnowledgeVersion own(List<KnowledgeVersion> vs, Long id) {
        return vs.stream().filter(v -> v.getId().equals(id)).findFirst()
                .orElseThrow(() -> CommonException.notFound("Phiên bản " + id + " không thuộc tài liệu này"));
    }

    private String content(KnowledgeDoc d, SaveDraftRequest r) {
        if (d.getFormat() == KnowledgeDocType.DocFormat.YAML) {
            RuleCodec.Parsed<String> c = r.value() != null ? RuleCodec.canonical(d.getKey(), r.value())
                    : RuleCodec.canonical(d.getKey(), r.content());
            if (!c.ok()) {
                throw new CommonException(HttpStatus.BAD_REQUEST, "Rule chưa hợp lệ: " + String.join("; ", c.errors()),
                        Map.of("errors", c.errors()));
            }
            return c.value();
        }
        if (r.content() == null) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Thiếu nội dung");
        }
        checkSize(r.content());
        return r.content();
    }

    private CommonException conflict(String reason, String message, KnowledgeDoc d, KnowledgeVersion current,
                                     String yours, Map<Long, Long> active) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reason", reason);
        data.put("current", current == null ? null : dto(current, active));
        data.put("diff", KnowledgeDiff.unified(name(d, current), current == null ? "" : current.getContent(),
                d.getKey() + " (bản của bạn)", yours));
        return new CommonException(HttpStatus.CONFLICT, message, data);
    }

    private String starter(KnowledgeDocType type, String title) {
        String template = Optional.ofNullable(TEMPLATES.get(type))
                .flatMap(k -> knowledge.current().doc(k)).map(KnowledgeSnapshot.Doc::content).orElse(null);
        if (template == null) {
            return "# " + title.strip() + "\n";
        }
        String[] lines = template.split("\n", 2);
        return lines[0].startsWith("# ") ? "# " + title.strip() + "\n" + (lines.length > 1 ? lines[1] : "")
                : template;
    }

    private static String name(KnowledgeDoc d, KnowledgeVersion v) {
        return v == null ? d.getKey() + " (trống)" : d.getKey() + " v" + v.getVersionNo()
                + (v.getStatus() == KnowledgeVersion.Status.DRAFT ? " (nháp)" : "");
    }

    private static void checkSize(String content) {
        if (content != null && content.length() > MAX_CONTENT) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Nội dung quá dài (tối đa " + MAX_CONTENT + " ký tự)");
        }
    }

    private static boolean sameInstant(OffsetDateTime a, OffsetDateTime b) {
        return a != null && b != null
                && a.toInstant().truncatedTo(ChronoUnit.MILLIS).equals(b.toInstant().truncatedTo(ChronoUnit.MILLIS));
    }

    private static OffsetDateTime now() {
        return OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.strip();
    }
}
