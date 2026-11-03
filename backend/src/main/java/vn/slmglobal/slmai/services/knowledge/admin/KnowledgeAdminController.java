package vn.slmglobal.slmai.services.knowledge.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.common.security.RequestContext;
import vn.slmglobal.slmai.common.security.RequireAuth;
import vn.slmglobal.slmai.common.security.RoleCode;
import vn.slmglobal.slmai.common.web.ApiResponses;
import vn.slmglobal.slmai.common.web.PageResponse;
import vn.slmglobal.slmai.common.web.ResponseData;
import vn.slmglobal.slmai.services.knowledge.KnowledgeDocType;
import vn.slmglobal.slmai.services.knowledge.rules.RuleCodec;
import vn.slmglobal.slmai.services.lead.ConsultSlots;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Kiến thức và quy tắc trên admin (mục 5.9, 5.3). Quyền: quản trị hệ thống và quản trị nội dung làm được tất cả
 * (người dùng chốt 2026-11-03).
 */
@Tag(name = "Quản trị - Kiến thức & quy tắc", description = "Tài liệu kiến thức, rule, phiên bản, phát hành")
@RestController
@RequestMapping("/api/v1/admin/knowledge")
@RequiredArgsConstructor
public class KnowledgeAdminController {

    private static final int MAX_PAGE_SIZE = 100;

    private final KnowledgeAdminService admin;
    private final KnowledgeReleaseService releases;

    // ------------------------------------------------------------------ tài liệu

    @Operation(summary = "Danh sách tài liệu; status = DRAFT (có nháp) / NOT_RELEASED / ARCHIVED")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @GetMapping("/docs")
    public ResponseEntity<ResponseData<PageResponse<KnowledgeAdminService.DocRow>>> list(
            @RequestParam(required = false) KnowledgeDocType type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(admin.list(new KnowledgeAdminService.Filter(type, keyword, status), Math.max(0, page),
                Math.min(Math.max(1, size), MAX_PAGE_SIZE)), "KNOWLEDGE_DOC_LIST_SUCCESS");
    }

    @Operation(summary = "Tạo tài liệu (bản nháp v1, nội dung khởi tạo từ mẫu guide/mau)")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/docs")
    public ResponseEntity<ResponseData<KnowledgeAdminService.DocDetail>> create(
            @Valid @RequestBody KnowledgeAdminService.CreateRequest request) {
        return ApiResponses.ok(admin.create(request, RequestContext.requireAuthInfo()), "KNOWLEDGE_DOC_CREATED");
    }

    @Operation(summary = "Chi tiết tài liệu: bản đang chạy, bản mới nhất, bản nháp, lịch sử")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @GetMapping("/docs/{id}")
    public ResponseEntity<ResponseData<KnowledgeAdminService.DocDetail>> get(@PathVariable Long id) {
        return ApiResponses.ok(admin.get(id), "KNOWLEDGE_DOC_SUCCESS");
    }

    @Operation(summary = "Chi tiết tài liệu theo mã (ví dụ prompts/customer-chat)")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @GetMapping("/docs/by-key")
    public ResponseEntity<ResponseData<KnowledgeAdminService.DocDetail>> getByKey(@RequestParam String key) {
        return ApiResponses.ok(admin.getByKey(key), "KNOWLEDGE_DOC_SUCCESS");
    }

    @Operation(summary = "Lưu bản nháp; 409 khi có phiên bản mới hơn hoặc người khác vừa lưu (force = true để ghi đè)")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PutMapping("/docs/{id}/draft")
    public ResponseEntity<ResponseData<KnowledgeAdminService.DocDetail>> saveDraft(
            @PathVariable Long id, @Valid @RequestBody KnowledgeAdminService.SaveDraftRequest request) {
        return ApiResponses.ok(admin.saveDraft(id, request, RequestContext.requireAuthInfo()),
                "KNOWLEDGE_DRAFT_SAVED");
    }

    @Operation(summary = "Bỏ bản nháp")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @DeleteMapping("/docs/{id}/draft")
    public ResponseEntity<ResponseData<KnowledgeAdminService.DocDetail>> discardDraft(@PathVariable Long id) {
        return ApiResponses.ok(admin.discardDraft(id, RequestContext.requireAuthInfo()), "KNOWLEDGE_DRAFT_DISCARDED");
    }

    @Operation(summary = "Lưu trữ / bỏ lưu trữ: tài liệu lưu trữ không có trong bản phát hành sau")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/docs/{id}/archive")
    public ResponseEntity<ResponseData<KnowledgeAdminService.DocDetail>> archive(
            @PathVariable Long id, @RequestParam boolean archived) {
        return ApiResponses.ok(admin.archive(id, archived, RequestContext.requireAuthInfo()),
                "KNOWLEDGE_DOC_ARCHIVED");
    }

    @Operation(summary = "Khôi phục bản này: tạo bản nháp bằng nội dung một phiên bản cũ")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/docs/{id}/restore/{versionId}")
    public ResponseEntity<ResponseData<KnowledgeAdminService.DocDetail>> restore(
            @PathVariable Long id, @PathVariable Long versionId) {
        return ApiResponses.ok(admin.restore(id, versionId, RequestContext.requireAuthInfo()),
                "KNOWLEDGE_DRAFT_RESTORED");
    }

    @Operation(summary = "So sánh hai phiên bản (mặc định: bản đang chạy → bản nháp)")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @GetMapping("/docs/{id}/diff")
    public ResponseEntity<ResponseData<KnowledgeAdminService.Diff>> diff(
            @PathVariable Long id, @RequestParam(required = false) Long from,
            @RequestParam(required = false) Long to) {
        return ApiResponses.ok(admin.diff(id, from, to), "KNOWLEDGE_DIFF_SUCCESS");
    }

    @Operation(summary = "Nội dung một phiên bản")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @GetMapping("/versions/{versionId}")
    public ResponseEntity<ResponseData<KnowledgeAdminService.VersionDto>> version(@PathVariable Long versionId) {
        return ApiResponses.ok(admin.version(versionId), "KNOWLEDGE_VERSION_SUCCESS");
    }

    @Operation(summary = "Kiểm tra tức thì nội dung đang soạn (mục 5.10 A); rule YAML kiểm tra theo schema")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/lint")
    public ResponseEntity<ResponseData<List<KnowledgeLinter.Issue>>> lint(
            @RequestBody KnowledgeAdminService.LintRequest request) {
        return ApiResponses.ok(admin.lint(request), "KNOWLEDGE_LINT_SUCCESS");
    }

    @Operation(summary = "Đếm token khối kiến thức nếu tài liệu có nội dung như đang soạn (ngân sách 40K)")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/count-tokens")
    public ResponseEntity<ResponseData<KnowledgeTokenCounter.Count>> countTokens(
            @Valid @RequestBody KnowledgeAdminService.TokenRequest request) {
        return ApiResponses.ok(admin.countTokens(request), "KNOWLEDGE_TOKENS_SUCCESS");
    }

    // ------------------------------------------------------------------ rule

    @Operation(summary = "4 rule YAML: báo giá, phân loại khách, tỉnh → miền, kiểm tra trước khi gửi")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @GetMapping("/rules")
    public ResponseEntity<ResponseData<List<KnowledgeAdminService.DocDetail>>> rules() {
        return ApiResponses.ok(admin.rules(), "KNOWLEDGE_RULES_SUCCESS");
    }

    @Operation(summary = "Lưu nháp một rule từ form (value) hoặc YAML (content); kiểm tra schema trước khi lưu")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PutMapping("/rules/{name}")
    public ResponseEntity<ResponseData<KnowledgeAdminService.DocDetail>> saveRule(
            @PathVariable String name, @Valid @RequestBody KnowledgeAdminService.SaveDraftRequest request) {
        String key = "rules/" + name;
        if (!RuleCodec.KEYS.contains(key)) {
            throw CommonException.notFound("Không có rule " + key);
        }
        Long docId = admin.getByKey(key).doc().id();
        return ApiResponses.ok(admin.saveDraft(docId, request, RequestContext.requireAuthInfo()),
                "KNOWLEDGE_RULE_SAVED");
    }

    @Operation(summary = "Thông tin AI cần hỏi khách (consult-slots): chỉ xem, đổi phải sửa code")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @GetMapping("/consult-slots")
    public ResponseEntity<ResponseData<Map<String, Object>>> consultSlots() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("slots", List.of(
                slot(ConsultSlots.KHU_VUC, "Tỉnh / thành phố", true, List.of()),
                slot(ConsultSlots.SO_PHA, "Điện 1 pha hay 3 pha", true, List.of("1", "3")),
                slot(ConsultSlots.HOA_DON_THANG, "Tiền điện mỗi tháng (đồng)", true, List.of()),
                slot(ConsultSlots.TY_LE_NGAY_DEM, "Dùng điện ngày hay đêm nhiều", true,
                        List.of("NGAY_NHIEU: ban ngày nhiều", "CAN_BANG: cân bằng", "DEM_NHIEU: ban đêm nhiều")),
                slot(ConsultSlots.LOAI_MAI, "Loại mái", true, List.of("TON: mái tôn", "NGOI: mái ngói",
                        "BANG: mái bằng")),
                slot(ConsultSlots.DIEN_TICH_MAI, "Diện tích mái (m²)", false, List.of()),
                slot(ConsultSlots.TEN_KHACH, "Tên khách", false, List.of()),
                slot(ConsultSlots.DANH_XUNG, "Cách gọi khách", false, List.of("ANH", "CHI"))));
        out.put("qualification", List.of(
                slot("research_level", "Mức tìm hiểu", false, List.of("CHUA: chưa tìm hiểu",
                        "SO_BO: tìm hiểu sơ bộ", "KY: tìm hiểu kỹ")),
                slot("budget_vnd", "Ngân sách (đồng)", false, List.of()),
                slot("timeline", "Thời điểm lắp", false, List.of("TUAN_TOI: tuần tới", "THANG_TOI: tháng tới",
                        "VAI_THANG: vài tháng nữa", "NAM_TOI: năm tới", "CHUA_RO: chưa rõ"))));
        out.put("note", "Danh sách này quyết định khuôn kết quả AI và công cụ báo giá, nên đổi phải sửa code "
                + "(ConsultSlots, AiReplySchema) để giữ đúng giới hạn 16 trường null của API.");
        return ApiResponses.ok(out, "KNOWLEDGE_CONSULT_SLOTS_SUCCESS");
    }

    // ------------------------------------------------------------------ phát hành

    @Operation(summary = "Các bản nháp chờ phát hành")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @GetMapping("/releases/drafts")
    public ResponseEntity<ResponseData<List<KnowledgeReleaseService.DraftRow>>> drafts() {
        return ApiResponses.ok(releases.drafts(), "KNOWLEDGE_DRAFTS_SUCCESS");
    }

    @Operation(summary = "Danh sách bản phát hành (mới trước)")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @GetMapping("/releases")
    public ResponseEntity<ResponseData<PageResponse<KnowledgeReleaseService.ReleaseRow>>> releases(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(releases.list(Math.max(0, page), Math.min(Math.max(1, size), MAX_PAGE_SIZE)),
                "KNOWLEDGE_RELEASES_SUCCESS");
    }

    @Operation(summary = "Chi tiết bản phát hành: tài liệu, báo cáo kiểm tra")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @GetMapping("/releases/{id}")
    public ResponseEntity<ResponseData<KnowledgeReleaseService.ReleaseDetail>> release(@PathVariable Long id) {
        return ApiResponses.ok(releases.get(id), "KNOWLEDGE_RELEASE_SUCCESS");
    }

    @Operation(summary = "Kiểm tra thử một bản phát hành (không tạo gì)")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/releases/validate")
    public ResponseEntity<ResponseData<KnowledgeReleaseService.Report>> validate(
            @Valid @RequestBody KnowledgeReleaseService.ReleaseRequest request) {
        return ApiResponses.ok(releases.validate(request), "KNOWLEDGE_RELEASE_VALIDATED");
    }

    @Operation(summary = "Tạo bản phát hành từ các bản nháp (đóng băng); kiểm tra không đạt thì 400 kèm báo cáo")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/releases")
    public ResponseEntity<ResponseData<KnowledgeReleaseService.ReleaseRow>> createRelease(
            @Valid @RequestBody KnowledgeReleaseService.ReleaseRequest request) {
        return ApiResponses.ok(releases.create(request, RequestContext.requireAuthInfo()),
                "KNOWLEDGE_RELEASE_CREATED");
    }

    @Operation(summary = "Kích hoạt một bản phát hành, hoặc quay về một bản cũ; bot dùng ngay")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/releases/{id}/activate")
    public ResponseEntity<ResponseData<KnowledgeReleaseService.ReleaseRow>> activate(@PathVariable Long id) {
        return ApiResponses.ok(releases.activate(id, RequestContext.requireAuthInfo()),
                "KNOWLEDGE_RELEASE_ACTIVATED");
    }

    private static Map<String, Object> slot(String key, String label, boolean required, List<String> values) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("key", key);
        m.put("label", label);
        m.put("required", required);
        m.put("values", values);
        return m;
    }
}
