package vn.slmglobal.slmai.services.media;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import vn.slmglobal.slmai.common.security.AuthInfo;
import vn.slmglobal.slmai.common.security.RequestContext;
import vn.slmglobal.slmai.common.security.RequireAuth;
import vn.slmglobal.slmai.common.security.RoleCode;
import vn.slmglobal.slmai.common.storage.StorageService;
import vn.slmglobal.slmai.common.web.ApiResponses;
import vn.slmglobal.slmai.common.web.PageResponse;
import vn.slmglobal.slmai.common.web.ResponseData;
import vn.slmglobal.slmai.services.media.MediaAdminService.MediaFilter;
import vn.slmglobal.slmai.services.media.MediaAdminService.MediaResponse;
import vn.slmglobal.slmai.services.media.entity.MediaSource;
import vn.slmglobal.slmai.services.media.repository.MediaSourceRepository;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/** Quản trị thư viện media: nhập ảnh / video combo, danh sách, sửa, duyệt, xoá mềm, xem ảnh (mục 6, 34). */
@Tag(name = "Quản trị - Media", description = "Thư viện ảnh, video; nhập ảnh combo từ Google Drive")
@RestController
@RequestMapping("/api/v1/admin/media")
@RequiredArgsConstructor
public class MediaAdminController {

    private static final int MAX_PAGE_SIZE = 100;

    private final PackageMediaImportService importService;
    private final MediaAdminService adminService;
    private final MediaSourceRepository sources;
    private final StorageService storage;

    @Operation(summary = "Nhập ảnh combo (Google Drive) và video Facebook, liên kết với gói slm_v2; tối đa 10 mục mỗi lần")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/imports/package-images")
    public ResponseEntity<ResponseData<List<PackageMediaImportService.ItemResult>>> importPackageImages(
            @Valid @RequestBody PackageMediaImportService.Request request) {
        AuthInfo actor = RequestContext.requireAuthInfo();
        return ApiResponses.ok(importService.importPackages(request, actor.id(), actor.email()),
                request.dryRun() ? "MEDIA_IMPORT_DRY_RUN_SUCCESS" : "MEDIA_IMPORT_SUCCESS");
    }

    @Operation(summary = "Danh sách media (mới trước); mặc định ẩn mục đã xoá, status=DELETED để xem chúng")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @GetMapping
    public ResponseEntity<ResponseData<PageResponse<MediaResponse>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) vn.slmglobal.slmai.services.media.entity.MediaType type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer packageId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), MAX_PAGE_SIZE));
        return ApiResponses.ok(adminService.list(new MediaFilter(keyword, type, status, packageId), pageable),
                "MEDIA_LIST_SUCCESS");
    }

    @Operation(summary = "Chi tiết một media (mở form sửa)")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<MediaResponse>> get(@PathVariable UUID id) {
        return ApiResponses.ok(adminService.get(id), "MEDIA_GET_SUCCESS");
    }

    @Operation(summary = "Sửa tiêu đề, metadata cho AI, quyền sử dụng, trạng thái duyệt")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PutMapping("/{id}")
    public ResponseEntity<ResponseData<MediaResponse>> update(@PathVariable UUID id,
                                                              @Valid @RequestBody MediaAdminService.UpdateRequest request) {
        return ApiResponses.ok(adminService.update(RequestContext.requireAuthInfo(), id, request),
                "MEDIA_UPDATE_SUCCESS");
    }

    @Operation(summary = "Xoá mềm: ẩn khỏi danh sách và AI, file và liên kết giữ nguyên")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseData<Void>> delete(@PathVariable UUID id) {
        adminService.delete(RequestContext.requireAuthInfo(), id);
        return ApiResponses.ok(null, "MEDIA_DELETE_SUCCESS");
    }

    @Operation(summary = "Khôi phục media đã xoá mềm")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/{id}/restore")
    public ResponseEntity<ResponseData<MediaResponse>> restore(@PathVariable UUID id) {
        return ApiResponses.ok(adminService.restore(RequestContext.requireAuthInfo(), id), "MEDIA_RESTORE_SUCCESS");
    }

    /**
     * Nội dung file đã lưu, đi qua backend: MinIO nội bộ chạy http và URL presign ký theo host nội bộ, trình duyệt
     * không dùng được. Frontend tải dạng blob kèm Bearer token.
     */
    @Operation(summary = "Tải nội dung file của một nguồn media (ảnh xem trước)")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @GetMapping("/{mediaId}/sources/{sourceId}/content")
    public ResponseEntity<InputStreamResource> content(@PathVariable UUID mediaId, @PathVariable UUID sourceId) {
        MediaSource source = sources.findByIdAndMediaId(sourceId, mediaId)
                .orElseThrow(() -> CommonException.notFound("Không tìm thấy nguồn media"));
        if (source.getObjectKey() == null) {
            throw CommonException.notFound("Nguồn này là link ngoài, không có file lưu trong hệ thống");
        }
        try {
            ResponseEntity.BodyBuilder ok = ResponseEntity.ok()
                    .contentType(source.getMimeType() == null ? MediaType.APPLICATION_OCTET_STREAM
                            : MediaType.parseMediaType(source.getMimeType()))
                    .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
                    .header("X-Content-Type-Options", "nosniff");
            if (source.getSha256() != null) {
                ok.eTag("\"" + source.getSha256() + "\"");
            }
            if (source.getSizeBytes() != null) {
                ok.contentLength(source.getSizeBytes());
            }
            return ok.body(new InputStreamResource(storage.get(source.getObjectKey())));
        } catch (Exception e) {
            throw new CommonException(HttpStatus.SERVICE_UNAVAILABLE, "Không đọc được file từ MinIO: " + e.getMessage());
        }
    }
}
