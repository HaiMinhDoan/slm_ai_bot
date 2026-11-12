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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
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
import vn.slmglobal.slmai.services.media.entity.LinkTargetType;
import vn.slmglobal.slmai.services.media.entity.MediaSource;
import vn.slmglobal.slmai.services.media.entity.RightsStatus;
import vn.slmglobal.slmai.services.media.repository.MediaTargetRepository;
import vn.slmglobal.slmai.services.media.repository.MediaSourceRepository;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Quản trị thư viện media (mục 6, 34): nhập combo, thêm file / link, danh sách, sửa mọi trường, nguồn, liên kết,
 * duyệt, xoá mềm, lịch sử, thao tác hàng loạt, AI viết nháp, bộ chủ đề, xem ảnh.
 */
@Tag(name = "Quản trị - Media", description = "Thư viện ảnh, video; nhập ảnh combo từ Google Drive")
@RestController
@RequestMapping("/api/v1/admin/media")
@RequiredArgsConstructor
public class MediaAdminController {

    private static final int MAX_PAGE_SIZE = 100;

    private final PackageMediaImportService importService;
    private final MediaAdminService adminService;
    private final MediaSourceService sourceService;
    private final MediaDraftService draftService;
    private final MediaTopicService topicService;
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

    @Operation(summary = "Thêm một media từ file tải lên (ảnh JPEG / PNG / WebP tối đa 15 MB, video MP4 / MOV / WebM "
            + "tối đa 100 MB); video kèm ảnh thu nhỏ và thông số trình duyệt đọc được")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseData<MediaSourceService.CreateResult>> upload(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(required = false) Integer width,
            @RequestParam(required = false) Integer height,
            @RequestParam(required = false) Integer durationSeconds,
            @RequestParam(required = false) RightsStatus rights) {
        return ApiResponses.ok(sourceService.createFromFile(RequestContext.requireAuthInfo(), file, thumbnail,
                new MediaSourceService.FileMeta(width, height, durationSeconds), rights), "MEDIA_UPLOAD_SUCCESS");
    }

    @Operation(summary = "Thêm một media từ link YouTube / Facebook / TikTok (lưu link) hoặc ảnh Google Drive (tải về)")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/links")
    public ResponseEntity<ResponseData<MediaSourceService.CreateResult>> addByLink(
            @Valid @RequestBody MediaSourceService.LinkRequest request) {
        return ApiResponses.ok(sourceService.createFromLink(RequestContext.requireAuthInfo(), request),
                "MEDIA_LINK_SUCCESS");
    }

    @Operation(summary = "Thao tác hàng loạt: đổi trạng thái, quyền, thêm chủ đề, gắn đối tượng, xoá, khôi phục")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/bulk")
    public ResponseEntity<ResponseData<List<MediaAdminService.BulkResult>>> bulk(
            @Valid @RequestBody MediaAdminService.BulkRequest request) {
        return ApiResponses.ok(adminService.bulk(RequestContext.requireAuthInfo(), request), "MEDIA_BULK_SUCCESS");
    }

    @Operation(summary = "Tìm gói / vật tư / hãng / tài liệu để gắn media")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @GetMapping("/link-targets")
    public ResponseEntity<ResponseData<List<MediaTargetRepository.Target>>> linkTargets(
            @RequestParam LinkTargetType type, @RequestParam(required = false) String keyword) {
        return ApiResponses.ok(adminService.searchTargets(type, keyword), "MEDIA_LINK_TARGETS_SUCCESS");
    }

    @Operation(summary = "Bộ chủ đề (cả chủ đề đã tắt)")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @GetMapping("/topics")
    public ResponseEntity<ResponseData<List<MediaTopicService.TopicResponse>>> topics() {
        return ApiResponses.ok(topicService.list(), "MEDIA_TOPIC_LIST_SUCCESS");
    }

    @Operation(summary = "Thêm chủ đề")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/topics")
    public ResponseEntity<ResponseData<MediaTopicService.TopicResponse>> createTopic(
            @Valid @RequestBody MediaTopicService.TopicRequest request) {
        return ApiResponses.ok(topicService.create(RequestContext.requireAuthInfo(), request),
                "MEDIA_TOPIC_CREATE_SUCCESS");
    }

    @Operation(summary = "Sửa, tắt / bật chủ đề (mã không đổi được)")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PutMapping("/topics/{code}")
    public ResponseEntity<ResponseData<MediaTopicService.TopicResponse>> updateTopic(
            @PathVariable String code, @Valid @RequestBody MediaTopicService.TopicRequest request) {
        return ApiResponses.ok(topicService.update(RequestContext.requireAuthInfo(), code, request),
                "MEDIA_TOPIC_UPDATE_SUCCESS");
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

    @Operation(summary = "Lịch sử thao tác trên media (mới trước)")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @GetMapping("/{id}/history")
    public ResponseEntity<ResponseData<List<MediaAdminService.HistoryEntry>>> history(@PathVariable UUID id) {
        return ApiResponses.ok(adminService.history(id), "MEDIA_HISTORY_SUCCESS");
    }

    @Operation(summary = "Ghi lại toàn bộ liên kết với gói / vật tư / hãng / tài liệu")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PutMapping("/{id}/links")
    public ResponseEntity<ResponseData<MediaAdminService.LinksResult>> updateLinks(
            @PathVariable UUID id, @Valid @RequestBody List<MediaAdminService.LinkInput> request) {
        return ApiResponses.ok(adminService.updateLinks(RequestContext.requireAuthInfo(), id, request),
                "MEDIA_LINKS_UPDATE_SUCCESS");
    }

    @Operation(summary = "AI viết nháp tóm tắt, dùng khi, tránh khi, mô tả, chủ đề (không lưu, chỉ điền form)")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/{id}/ai-draft")
    public ResponseEntity<ResponseData<MediaDraftService.Draft>> aiDraft(@PathVariable UUID id) {
        return ApiResponses.ok(draftService.draft(RequestContext.requireAuthInfo(), id), "MEDIA_AI_DRAFT_SUCCESS");
    }

    @Operation(summary = "Thêm file vào media; replaceSourceId để thay file của một nguồn")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping(value = "/{id}/sources/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseData<MediaResponse>> addFile(
            @PathVariable UUID id,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(required = false) Integer width,
            @RequestParam(required = false) Integer height,
            @RequestParam(required = false) Integer durationSeconds,
            @RequestParam(required = false) UUID replaceSourceId) {
        return ApiResponses.ok(sourceService.addFile(RequestContext.requireAuthInfo(), id, file, thumbnail,
                new MediaSourceService.FileMeta(width, height, durationSeconds), replaceSourceId),
                "MEDIA_SOURCE_ADD_SUCCESS");
    }

    @Operation(summary = "Thêm link vào media; replaceSourceId để sửa link của một nguồn")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/{id}/sources/link")
    public ResponseEntity<ResponseData<MediaResponse>> addLink(
            @PathVariable UUID id, @Valid @RequestBody MediaSourceService.LinkRequest request) {
        return ApiResponses.ok(sourceService.addLink(RequestContext.requireAuthInfo(), id, request),
                "MEDIA_SOURCE_ADD_SUCCESS");
    }

    @Operation(summary = "Sửa trạng thái truy cập, tác giả của một nguồn")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PutMapping("/{id}/sources/{sourceId}")
    public ResponseEntity<ResponseData<MediaResponse>> updateSource(
            @PathVariable UUID id, @PathVariable UUID sourceId,
            @Valid @RequestBody MediaSourceService.SourceUpdate request) {
        return ApiResponses.ok(sourceService.updateSource(RequestContext.requireAuthInfo(), id, sourceId, request),
                "MEDIA_SOURCE_UPDATE_SUCCESS");
    }

    @Operation(summary = "Bỏ một nguồn (file trong MinIO giữ nguyên); media phải còn ít nhất một nguồn")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @DeleteMapping("/{id}/sources/{sourceId}")
    public ResponseEntity<ResponseData<MediaResponse>> deleteSource(@PathVariable UUID id,
                                                                    @PathVariable UUID sourceId) {
        return ApiResponses.ok(sourceService.deleteSource(RequestContext.requireAuthInfo(), id, sourceId),
                "MEDIA_SOURCE_DELETE_SUCCESS");
    }

    @Operation(summary = "Sửa nội dung, chủ đề, giai đoạn khách, kênh, quyền sử dụng, trạng thái duyệt")
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

    @Operation(summary = "Ảnh thu nhỏ của video tải lên")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @GetMapping("/{mediaId}/sources/{sourceId}/thumbnail")
    public ResponseEntity<InputStreamResource> thumbnail(@PathVariable UUID mediaId, @PathVariable UUID sourceId) {
        MediaSource source = sourceService.thumbnailSource(mediaId, sourceId)
                .orElseThrow(() -> CommonException.notFound("Nguồn này không có ảnh thu nhỏ"));
        String key = source.getThumbnailKey();
        MediaType type = key.endsWith(".png") ? MediaType.IMAGE_PNG
                : key.endsWith(".webp") ? MediaType.parseMediaType("image/webp") : MediaType.IMAGE_JPEG;
        try {
            return ResponseEntity.ok().contentType(type)
                    .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
                    .header("X-Content-Type-Options", "nosniff")
                    .body(new InputStreamResource(storage.get(key)));
        } catch (Exception e) {
            throw new CommonException(HttpStatus.SERVICE_UNAVAILABLE, "Không đọc được file từ MinIO: " + e.getMessage());
        }
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
