package vn.slmglobal.slmai.services.knowledge.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.common.security.AuthInfo;
import vn.slmglobal.slmai.common.web.PageResponse;
import vn.slmglobal.slmai.services.auth.AuditLogService;
import vn.slmglobal.slmai.services.catalog.CatalogRepository;
import vn.slmglobal.slmai.services.knowledge.KnowledgeBase;
import vn.slmglobal.slmai.services.knowledge.KnowledgeDocType;
import vn.slmglobal.slmai.services.knowledge.KnowledgeHash;
import vn.slmglobal.slmai.services.knowledge.KnowledgeReleaseActivated;
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
import vn.slmglobal.slmai.services.notification.NotificationDraft;
import vn.slmglobal.slmai.services.notification.NotificationService;
import vn.slmglobal.slmai.services.notification.NotificationSeverity;
import vn.slmglobal.slmai.services.notification.NotificationType;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Phát hành kiến thức (mục 5.3): gom bản nháp thành một bản phát hành bất biến, kiểm tra, rồi kích hoạt. Quay về bản
 * cũ là kích hoạt lại bản đó. Kiểm tra chạy TRƯỚC khi đóng băng bản nháp: không đạt thì không tạo gì, bản nháp vẫn
 * sửa tiếp được.
 *
 * <p>Các bước kiểm tra: (1) mã tài liệu, tài liệu bắt buộc; (2) rule YAML theo schema, mỗi miền trỏ tới cơ sở có
 * gói trong catalog; (3) hướng dẫn hệ thống không rỗng, không có chỗ thay lạ; (4) token trong ngân sách; (5) lint của
 * tài liệu đổi (chỉ cảnh báo). Bước chạy bộ test (mục 35) bật khi có bộ test.
 */
@Service
@RequiredArgsConstructor
public class KnowledgeReleaseService {

    static final String TARGET = "knowledge_release";
    private static final Pattern KEY = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*(/[a-z0-9]+(-[a-z0-9]+)*)+");
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*[^}]*}}");
    private static final List<String> REQUIRED = List.of(KnowledgeSnapshot.PROMPT_KEY, RuleCodec.PRICING,
            RuleCodec.LEAD_SCORING, RuleCodec.REGIONS, RuleCodec.REPLY_POLICY);

    private final KnowledgeDocRepository docs;
    private final KnowledgeVersionRepository versions;
    private final KnowledgeReleaseRepository releases;
    private final KnowledgeReleaseItemRepository items;
    private final KnowledgeBase knowledge;
    private final KnowledgeAdminService admin;
    private final KnowledgeTokenCounter tokens;
    private final CatalogRepository catalog;
    private final AuditLogService audit;
    private final NotificationService notifications;
    private final ApplicationEventPublisher events;
    private final ObjectMapper objectMapper;

    // ------------------------------------------------------------------ DTO

    public record DraftRow(Long versionId, Long docId, String key, String typeLabel, String title, int versionNo,
                           Integer activeVersionNo, String author, OffsetDateTime updatedAt, String changeNote,
                           boolean aiAssisted, boolean archived) {
    }

    /**
     * @param versionIds bản nháp đưa vào; null = mọi bản nháp của tài liệu chưa lưu trữ
     */
    public record ReleaseRequest(List<Long> versionIds, @Size(max = 1000) String notes) {
    }

    /** @param level ERROR (chặn), WARNING, OK */
    public record Check(String step, String level, String docKey, String message) {
    }

    /** @param action ADDED / UPDATED / REMOVED (lưu trữ) */
    public record Change(String key, String title, String action, Integer fromVersionNo, Integer toVersionNo) {
    }

    public record Report(boolean ok, List<Check> checks, List<Change> changes, Long tokenTotal, long tokenBudget,
                         boolean tokenEstimated, String kbVersion) {
    }

    public record ReleaseRow(Long id, String kbVersion, String status, String notes, Integer tokenTotal,
                             String createdBy, OffsetDateTime createdAt, String activatedBy,
                             OffsetDateTime activatedAt, int docCount) {
    }

    public record ItemRow(Long docId, String key, String typeLabel, String title, int versionNo, Long versionId) {
    }

    public record ReleaseDetail(ReleaseRow release, JsonNode report, List<ItemRow> items) {
    }

    /** Thành phần một bản phát hành dự kiến: docId → phiên bản */
    private record Plan(Map<Long, KnowledgeVersion> byDoc, List<KnowledgeVersion> drafts, List<Change> changes,
                        Map<Long, KnowledgeDoc> docById) {
    }

    // ------------------------------------------------------------------ đọc

    @Transactional(readOnly = true)
    public List<DraftRow> drafts() {
        Map<Long, KnowledgeDoc> docById = docs.findAll().stream()
                .collect(Collectors.toMap(KnowledgeDoc::getId, Function.identity()));
        Map<Long, Long> active = admin.activeVersionByDoc();
        Map<Long, KnowledgeVersion> activeVersions = versions.findByIdIn(active.values()).stream()
                .collect(Collectors.toMap(KnowledgeVersion::getId, Function.identity()));
        return versions.findByStatus(KnowledgeVersion.Status.DRAFT).stream()
                .map(v -> {
                    KnowledgeDoc d = docById.get(v.getDocId());
                    KnowledgeVersion a = activeVersions.get(active.get(v.getDocId()));
                    return new DraftRow(v.getId(), d.getId(), d.getKey(), d.getType().label(), v.getTitle(),
                            v.getVersionNo(), a == null ? null : a.getVersionNo(), v.getAuthor(), v.getUpdatedAt(),
                            v.getChangeNote(), v.isAiAssisted(), d.isArchived());
                })
                .sorted(Comparator.comparing(DraftRow::key))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ReleaseRow> list(int page, int size) {
        return PageResponse.of(releases.findAllByOrderByIdDesc(PageRequest.of(page, size)), this::row);
    }

    @Transactional(readOnly = true)
    public ReleaseDetail get(Long id) {
        KnowledgeRelease r = release(id);
        Map<Long, KnowledgeDoc> docById = docs.findAll().stream()
                .collect(Collectors.toMap(KnowledgeDoc::getId, Function.identity()));
        List<KnowledgeReleaseItem> rows = items.findByReleaseId(r.getId());
        Map<Long, KnowledgeVersion> vs = versions.findByIdIn(rows.stream().map(KnowledgeReleaseItem::getVersionId)
                .toList()).stream().collect(Collectors.toMap(KnowledgeVersion::getId, Function.identity()));
        List<ItemRow> out = rows.stream().map(i -> {
            KnowledgeDoc d = docById.get(i.getDocId());
            KnowledgeVersion v = vs.get(i.getVersionId());
            return new ItemRow(d.getId(), d.getKey(), d.getType().label(), v.getTitle(), v.getVersionNo(), v.getId());
        }).sorted(Comparator.comparing(ItemRow::key)).toList();
        return new ReleaseDetail(row(r), r.getValidationReport(), out);
    }

    /** Kiểm tra thử (không tạo gì): trang Phát hành hiện báo cáo trước khi bấm Tạo */
    @Transactional(readOnly = true)
    public Report validate(ReleaseRequest request) {
        return check(plan(request));
    }

    // ------------------------------------------------------------------ ghi

    @Transactional
    public ReleaseRow create(ReleaseRequest request, AuthInfo actor) {
        Plan plan = plan(request);
        Report report = check(plan);
        if (!report.ok()) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Chưa tạo được bản phát hành: "
                    + report.checks().stream().filter(c -> "ERROR".equals(c.level()))
                    .map(c -> (c.docKey() == null ? "" : c.docKey() + ": ") + c.message())
                    .collect(Collectors.joining("; ")), report);
        }
        OffsetDateTime now = OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
        for (KnowledgeVersion v : plan.drafts()) {
            v.setStatus(KnowledgeVersion.Status.FROZEN);
            v.setUpdatedAt(now);
            versions.save(v);
        }
        KnowledgeRelease release = releases.save(KnowledgeRelease.builder()
                .kbVersion(report.kbVersion())
                .status(KnowledgeRelease.Status.READY)
                .notes(request.notes() == null || request.notes().isBlank() ? null : request.notes().strip())
                .tokenTotal(report.tokenTotal() == null ? null : report.tokenTotal().intValue())
                .validationReport(objectMapper.valueToTree(report))
                .createdBy(actor.email())
                .build());
        plan.byDoc().forEach((docId, v) -> items.save(new KnowledgeReleaseItem(release.getId(), docId, v.getId())));
        audit.record(actor.id(), "knowledge.release.create", TARGET, release.getId(), null,
                Map.of("kbVersion", release.getKbVersion(), "changes", report.changes()));
        return row(release);
    }

    /** Kích hoạt một bản READY, hoặc quay về một bản cũ (SUPERSEDED). Bot nạp bản mới ngay sau commit. */
    @Transactional
    public ReleaseRow activate(Long id, AuthInfo actor) {
        KnowledgeRelease target = release(id);
        if (target.getStatus() == KnowledgeRelease.Status.ACTIVE) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Bản #" + id + " đang chạy");
        }
        if (target.getStatus() != KnowledgeRelease.Status.READY
                && target.getStatus() != KnowledgeRelease.Status.SUPERSEDED) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Chỉ kích hoạt được bản đã kiểm tra đạt hoặc bản cũ");
        }
        KnowledgeRelease previous = releases.findFirstByStatus(KnowledgeRelease.Status.ACTIVE).orElse(null);
        if (previous != null) {
            previous.setStatus(KnowledgeRelease.Status.SUPERSEDED);
            releases.saveAndFlush(previous); // bỏ ACTIVE trước: chỉ mục chỉ cho một bản ACTIVE
        }
        boolean rollback = target.getStatus() == KnowledgeRelease.Status.SUPERSEDED;
        target.setStatus(KnowledgeRelease.Status.ACTIVE);
        target.setActivatedBy(actor.email());
        target.setActivatedAt(OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS));
        releases.saveAndFlush(target);
        audit.record(actor.id(), rollback ? "knowledge.release.rollback" : "knowledge.release.activate", TARGET, id,
                previous == null ? null : Map.of("activeRelease", previous.getId()), Map.of("activeRelease", id));
        notifications.notifyAdmins(NotificationDraft.builder()
                .type(NotificationType.KNOWLEDGE_RELEASE)
                .severity(NotificationSeverity.INFO)
                .title((rollback ? "Đã quay về bản kiến thức #" : "Đã kích hoạt bản kiến thức #") + id)
                .body("Bot dùng bản #" + id + (previous == null ? "" : " thay cho bản #" + previous.getId())
                        + ". Người thao tác: " + actor.email())
                .data(Map.of("releaseId", id, "previousReleaseId", previous == null ? 0 : previous.getId(),
                        "rollback", rollback))
                .dedupeKey("KNOWLEDGE_RELEASE:" + id + ":" + target.getActivatedAt())
                .build());
        events.publishEvent(new KnowledgeReleaseActivated(id));
        return row(target);
    }

    // ------------------------------------------------------------------ dựng + kiểm tra

    private Plan plan(ReleaseRequest request) {
        Map<Long, KnowledgeDoc> docById = docs.findAll().stream()
                .collect(Collectors.toMap(KnowledgeDoc::getId, Function.identity()));
        Map<Long, Long> active = admin.activeVersionByDoc();
        Map<Long, KnowledgeVersion> activeVersions = versions.findByIdIn(active.values()).stream()
                .collect(Collectors.toMap(KnowledgeVersion::getId, Function.identity()));
        Map<Long, KnowledgeVersion> byDoc = new LinkedHashMap<>();
        active.forEach((docId, versionId) -> {
            KnowledgeDoc d = docById.get(docId);
            if (d != null && !d.isArchived()) {
                byDoc.put(docId, activeVersions.get(versionId));
            }
        });
        Set<Long> wanted = request.versionIds() == null ? null : Set.copyOf(request.versionIds());
        List<KnowledgeVersion> drafts = versions.findByStatus(KnowledgeVersion.Status.DRAFT).stream()
                .filter(v -> wanted == null || wanted.contains(v.getId()))
                .filter(v -> !docById.get(v.getDocId()).isArchived())
                .toList();
        if (wanted != null && drafts.size() != wanted.size()) {
            throw new CommonException(HttpStatus.BAD_REQUEST,
                    "Có bản nháp đã được phát hành, bỏ hoặc thuộc tài liệu lưu trữ; tải lại danh sách");
        }
        drafts.forEach(v -> byDoc.put(v.getDocId(), v));

        List<Change> changes = new ArrayList<>();
        for (KnowledgeVersion v : drafts) {
            KnowledgeVersion a = activeVersions.get(active.get(v.getDocId()));
            changes.add(new Change(docById.get(v.getDocId()).getKey(), v.getTitle(), a == null ? "ADDED" : "UPDATED",
                    a == null ? null : a.getVersionNo(), v.getVersionNo()));
        }
        active.forEach((docId, versionId) -> {
            KnowledgeDoc d = docById.get(docId);
            if (d != null && d.isArchived()) {
                KnowledgeVersion a = activeVersions.get(versionId);
                changes.add(new Change(d.getKey(), a.getTitle(), "REMOVED", a.getVersionNo(), null));
            }
        });
        changes.sort(Comparator.comparing(Change::key));
        return new Plan(byDoc, drafts, changes, docById);
    }

    private Report check(Plan plan) {
        List<Check> checks = new ArrayList<>();
        List<KnowledgeSnapshot.Doc> snapshotDocs = new ArrayList<>();
        Map<String, String> hashes = new LinkedHashMap<>();
        plan.byDoc().forEach((docId, v) -> {
            KnowledgeDoc d = plan.docById().get(docId);
            snapshotDocs.add(new KnowledgeSnapshot.Doc(d.getKey(), d.getType(), v.getTitle(), v.getContent()));
            hashes.put(d.getKey(), v.getContentHash());
        });
        String kbVersion = KnowledgeHash.release(hashes);

        // 1. Mã tài liệu, tài liệu bắt buộc, có thay đổi không
        if (plan.changes().isEmpty()) {
            checks.add(new Check("1", "ERROR", null, "Không có thay đổi nào so với bản đang chạy"));
        }
        for (KnowledgeSnapshot.Doc d : snapshotDocs) {
            if (!KEY.matcher(d.key()).matches() || KnowledgeDocType.ofKey(d.key()).orElse(null) != d.type()) {
                checks.add(new Check("1", "ERROR", d.key(), "Mã tài liệu sai định dạng hoặc không khớp loại"));
            }
            if (d.title() == null || d.title().isBlank()) {
                checks.add(new Check("1", "ERROR", d.key(), "Thiếu tiêu đề"));
            }
        }
        for (String key : REQUIRED) {
            if (snapshotDocs.stream().noneMatch(d -> d.key().equals(key))) {
                checks.add(new Check("1", "ERROR", key, "Thiếu tài liệu bắt buộc"));
            }
        }
        releases.findByKbVersion(kbVersion).ifPresent(same -> checks.add(new Check("1", "ERROR", null,
                same.getStatus() == KnowledgeRelease.Status.ACTIVE
                        ? "Nội dung giống hệt bản đang chạy #" + same.getId()
                        : "Nội dung giống hệt bản #" + same.getId() + ": kích hoạt lại bản đó thay vì tạo mới")));

        for (KnowledgeRelease waiting : releases.findByStatus(KnowledgeRelease.Status.READY)) {
            checks.add(new Check("1", "WARNING", null, "Bản #" + waiting.getId() + " đã kiểm tra nhưng chưa kích hoạt; "
                    + "thay đổi trong bản đó không có trong bản mới này (bản mới dựng từ bản đang chạy)"));
        }

        // 2. Rule YAML
        for (KnowledgeSnapshot.Doc d : snapshotDocs) {
            if (d.type() == KnowledgeDocType.RULE) {
                RuleCodec.validate(d.key(), d.content())
                        .forEach(e -> checks.add(new Check("2", "ERROR", d.key(), e)));
            }
        }
        snapshotDocs.stream().filter(d -> d.key().equals(RuleCodec.REGIONS)).findFirst()
                .map(d -> RuleCodec.regions(d.content()).value())
                .ifPresent(regions -> checkBranches(regions, checks));

        // 3. Hướng dẫn hệ thống
        snapshotDocs.stream().filter(d -> d.key().equals(KnowledgeSnapshot.PROMPT_KEY)).findFirst().ifPresent(p -> {
            if (p.content().strip().length() < 200) {
                checks.add(new Check("3", "ERROR", p.key(), "Hướng dẫn hệ thống quá ngắn hoặc trống"));
            }
        });
        for (KnowledgeSnapshot.Doc d : snapshotDocs) {
            if (d.type().botReads() || d.type() == KnowledgeDocType.PROMPT) {
                Matcher m = PLACEHOLDER.matcher(d.content());
                while (m.find()) {
                    if (!m.group().equals(KnowledgeBase.COMPANY_PLACEHOLDER)
                            && !m.group().equals(KnowledgeBase.PERSONA_PLACEHOLDER)) {
                        checks.add(new Check("3", "ERROR", d.key(), "Chỗ thay " + m.group() + " không tồn tại"));
                    }
                }
            }
        }

        // 4. Token
        KnowledgeSnapshot trial = new KnowledgeSnapshot(null, kbVersion, snapshotDocs,
                knowledge.current().rules());
        KnowledgeTokenCounter.Count count = tokens.count(knowledge.renderPrompt(trial)
                + "\n\n# Tài liệu kiến thức\n\n" + knowledge.renderKnowledge(trial));
        if (count.tokens() > count.budget()) {
            checks.add(new Check("4", "ERROR", null, "Khối kiến thức " + count.tokens() + " token, vượt ngân sách "
                    + count.budget() + " (mục 5.4): rút gọn hoặc lưu trữ bớt tài liệu"));
        }

        // 5. Lint các tài liệu đổi (chỉ cảnh báo)
        Set<String> changed = plan.changes().stream().map(Change::key).collect(Collectors.toSet());
        for (KnowledgeSnapshot.Doc d : snapshotDocs) {
            if (changed.contains(d.key()) && !d.type().yaml()) {
                admin.lint(new KnowledgeAdminService.LintRequest(d.type(), d.key(), d.content())).stream()
                        .filter(i -> i.level() == KnowledgeLinter.Level.WARNING)
                        .forEach(i -> checks.add(new Check("5", "WARNING", d.key(),
                                (i.line() == null ? "" : "dòng " + i.line() + ": ") + i.message())));
            }
        }
        boolean ok = checks.stream().noneMatch(c -> "ERROR".equals(c.level()));
        return new Report(ok, checks, plan.changes(), count.tokens(), count.budget(), count.estimated(), kbVersion);
    }

    /** Mỗi miền trỏ tới một cơ sở giá có gói trong catalog (bỏ qua khi catalog chưa đồng bộ) */
    private void checkBranches(JsonNode regions, List<Check> checks) {
        if (!catalog.hasRecommendablePackages()) {
            checks.add(new Check("2", "WARNING", RuleCodec.REGIONS,
                    "Catalog chưa đồng bộ, chưa kiểm tra được cơ sở giá của từng miền"));
            return;
        }
        regions.path("mien").fields().forEachRemaining(e -> {
            String branch = e.getValue().path("co_so").asText();
            if (catalog.recommendablePackages(branch).isEmpty()) {
                checks.add(new Check("2", "ERROR", RuleCodec.REGIONS, "mien." + e.getKey() + ".co_so: cơ sở giá \""
                        + branch + "\" không có gói nào đang bán trong catalog"));
            }
        });
    }

    private KnowledgeRelease release(Long id) {
        return releases.findById(id).orElseThrow(() -> CommonException.notFound("Không có bản phát hành " + id));
    }

    private ReleaseRow row(KnowledgeRelease r) {
        return new ReleaseRow(r.getId(), r.getKbVersion().substring(0, 12), r.getStatus().name(), r.getNotes(),
                r.getTokenTotal(), r.getCreatedBy(), r.getCreatedAt(), r.getActivatedBy(), r.getActivatedAt(),
                items.findByReleaseId(r.getId()).size());
    }
}
