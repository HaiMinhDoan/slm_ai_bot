package vn.slmglobal.slmai.services.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import vn.slmglobal.slmai.common.security.AuthInfo;
import vn.slmglobal.slmai.common.security.RequestContext;
import vn.slmglobal.slmai.common.security.RequireAuth;
import vn.slmglobal.slmai.common.security.RoleCode;
import vn.slmglobal.slmai.common.web.ApiResponses;
import vn.slmglobal.slmai.common.web.PageResponse;
import vn.slmglobal.slmai.common.web.ResponseData;
import vn.slmglobal.slmai.services.catalog.CatalogRepository.CatalogPackage;
import vn.slmglobal.slmai.services.sizing.PackageAdvisor;

import java.util.List;
import java.util.Map;

/**
 * Catalog đồng bộ từ slm_v2 và thử báo giá (mục 4.2, 34). Xem và thử báo giá: nhân viên trực chat cũng dùng được;
 * bấm đồng bộ: quản trị hệ thống, quản trị nội dung.
 */
@Tag(name = "Quản trị - Catalog & báo giá", description = "Đồng bộ gói từ slm_v2, danh sách gói, thử công cụ báo giá")
@RestController
@RequestMapping("/api/v1/admin/catalog")
@RequiredArgsConstructor
public class CatalogAdminController {

    private static final int MAX_PAGE_SIZE = 100;

    private final CatalogSyncService syncService;
    private final CatalogRepository repository;
    private final PackageAdvisor advisor;
    private final CatalogPackageAdminService packageAdmin;

    public record StatusResponse(boolean configured, String version, CatalogRepository.SyncRun lastSuccess,
                                 List<CatalogRepository.SyncRun> runs) {
    }

    @Operation(summary = "Trạng thái đồng bộ: cấu hình, phiên bản catalog, các lần đồng bộ gần nhất")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN, RoleCode.STAFF})
    @GetMapping("/status")
    public ResponseEntity<ResponseData<StatusResponse>> status(@RequestParam(defaultValue = "20") int limit) {
        return ApiResponses.ok(new StatusResponse(syncService.isConfigured(), syncService.currentVersion(),
                repository.lastSuccess().orElse(null), syncService.runs(Math.min(Math.max(1, limit), 100))),
                "CATALOG_STATUS_SUCCESS");
    }

    @Operation(summary = "Đồng bộ ngay từ slm_v2 (chờ tới khi xong, thường vài giây)")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/sync")
    public ResponseEntity<ResponseData<CatalogRepository.SyncRun>> sync() {
        AuthInfo actor = RequestContext.requireAuthInfo();
        return ApiResponses.ok(syncService.sync(CatalogSyncService.TRIGGER_MANUAL, actor.email()),
                "CATALOG_SYNC_DONE");
    }

    @Operation(summary = "Gói đã đồng bộ (cả gói ngừng, gói bị chặn gợi ý) kèm cảnh báo dữ liệu, có lọc và phân trang")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN, RoleCode.STAFF})
    @GetMapping("/packages")
    public ResponseEntity<ResponseData<PageResponse<CatalogPackage>>> packages(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String branchCode,
            @RequestParam(required = false) String systemType,
            @RequestParam(required = false) Integer phases,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), MAX_PAGE_SIZE));
        Page<CatalogPackage> result = repository.findPackages(
                new CatalogRepository.PackageFilter(keyword, branchCode, systemType, phases, status), pageable);
        return ApiResponses.ok(PageResponse.of(result, p -> p), "CATALOG_PACKAGES_SUCCESS");
    }

    @Operation(summary = "Một gói kèm số liệu slm_v2 và điều chỉnh của bot, để mở form sửa")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN, RoleCode.STAFF})
    @GetMapping("/packages/{id}")
    public ResponseEntity<ResponseData<CatalogPackageAdminService.PackageEdit>> packageEdit(@PathVariable int id) {
        return ApiResponses.ok(packageAdmin.edit(id), "CATALOG_PACKAGE_SUCCESS");
    }

    @Operation(summary = "Sửa gói", description = "Chỉ áp dụng cho bot: slm_v2 không đổi, đồng bộ không đè mất. "
            + "Trường để trống hoặc trùng số liệu slm_v2 thì bot dùng số liệu slm_v2.")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PutMapping("/packages/{id}")
    public ResponseEntity<ResponseData<CatalogPackage>> updatePackage(
            @PathVariable int id, @Valid @RequestBody CatalogPackageAdminService.UpdateRequest request) {
        return ApiResponses.ok(packageAdmin.update(RequestContext.requireAuthInfo(), id, request),
                "CATALOG_PACKAGE_UPDATED");
    }

    @Operation(summary = "Bật / tắt nhiều gói trong bot (gói tắt không được gợi ý, không báo giá)")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/packages/set-active")
    public ResponseEntity<ResponseData<Map<String, Integer>>> setPackagesActive(
            @Valid @RequestBody CatalogPackageAdminService.SetActiveRequest request) {
        int affected = packageAdmin.setActive(RequestContext.requireAuthInfo(), request);
        return ApiResponses.ok(Map.of("affected", affected),
                request.active() ? "CATALOG_PACKAGES_ENABLED" : "CATALOG_PACKAGES_DISABLED");
    }

    @Operation(summary = "Xoá mềm gói: ẩn khỏi danh sách và bot, khôi phục được")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @DeleteMapping("/packages/{id}")
    public ResponseEntity<ResponseData<Void>> deletePackage(@PathVariable int id) {
        packageAdmin.delete(RequestContext.requireAuthInfo(), id);
        return ApiResponses.ok(null, "CATALOG_PACKAGE_DELETED");
    }

    @Operation(summary = "Khôi phục gói đã xoá mềm")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/packages/{id}/restore")
    public ResponseEntity<ResponseData<CatalogPackage>> restorePackage(@PathVariable int id) {
        return ApiResponses.ok(packageAdmin.restore(RequestContext.requireAuthInfo(), id),
                "CATALOG_PACKAGE_RESTORED");
    }

    @Operation(summary = "Nhãn tiếng Việt của mã cảnh báo dữ liệu")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN, RoleCode.STAFF})
    @GetMapping("/warning-labels")
    public ResponseEntity<ResponseData<Map<String, Object>>> warningLabels() {
        Map<String, Object> out = new java.util.LinkedHashMap<>();
        for (CatalogWarning w : CatalogWarning.values()) {
            out.put(w.name(), Map.of("label", w.label(), "blocking", w.blocking()));
        }
        return ApiResponses.ok(out, "CATALOG_WARNING_LABELS_SUCCESS");
    }

    @Operation(summary = "Thử tool tim_goi_phu_hop: đúng kết quả AI nhận được khi tư vấn khách")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN, RoleCode.STAFF})
    @PostMapping("/advise")
    public ResponseEntity<ResponseData<JsonNode>> advise(@RequestBody PackageAdvisor.AdviceRequest request) {
        return ApiResponses.ok(advisor.advise(request).result(), "CATALOG_ADVISE_SUCCESS");
    }

    @Operation(summary = "Thử tool chi_tiet_goi")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN, RoleCode.STAFF})
    @GetMapping("/packages/{id}/detail")
    public ResponseEntity<ResponseData<JsonNode>> detail(@PathVariable int id,
                                                         @RequestParam(name = "khuVuc", required = false) String province) {
        return ApiResponses.ok(advisor.packageDetail(id, province), "CATALOG_PACKAGE_DETAIL_SUCCESS");
    }
}
