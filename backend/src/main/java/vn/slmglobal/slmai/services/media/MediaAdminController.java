package vn.slmglobal.slmai.services.media;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import vn.slmglobal.slmai.services.media.entity.MediaItem;
import vn.slmglobal.slmai.services.media.entity.MediaSource;
import vn.slmglobal.slmai.services.media.repository.MediaItemRepository;
import vn.slmglobal.slmai.services.media.repository.MediaLinkRepository;
import vn.slmglobal.slmai.services.media.repository.MediaSourceRepository;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Quản trị thư viện media: nhập ảnh / video combo, danh sách, xem ảnh (mục 6, 34). */
@Tag(name = "Quản trị - Media", description = "Thư viện ảnh, video; nhập ảnh combo từ Google Drive")
@RestController
@RequestMapping("/api/v1/admin/media")
@RequiredArgsConstructor
public class MediaAdminController {

    private static final int MAX_PAGE_SIZE = 100;

    private final PackageMediaImportService importService;
    private final MediaItemRepository items;
    private final MediaSourceRepository sources;
    private final MediaLinkRepository links;
    private final StorageService storage;

    public record SourceResponse(UUID id, String sourceType, String sourceUrl, String externalId, String mimeType,
                                 Long sizeBytes, Integer width, Integer height, String processingStatus,
                                 String availabilityStatus, boolean stored) {
    }

    public record LinkResponse(String targetType, String targetKey, String role) {
    }

    public record MediaResponse(UUID id, String type, String title, String status, String rights, String createdBy,
                                OffsetDateTime createdAt, List<SourceResponse> sources, List<LinkResponse> links) {
    }

    @Operation(summary = "Nhập ảnh combo (Google Drive) và video Facebook, liên kết với gói slm_v2; tối đa 10 mục mỗi lần")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @PostMapping("/imports/package-images")
    public ResponseEntity<ResponseData<List<PackageMediaImportService.ItemResult>>> importPackageImages(
            @Valid @RequestBody PackageMediaImportService.Request request) {
        AuthInfo actor = RequestContext.requireAuthInfo();
        return ApiResponses.ok(importService.importPackages(request, actor.id(), actor.email()),
                request.dryRun() ? "MEDIA_IMPORT_DRY_RUN_SUCCESS" : "MEDIA_IMPORT_SUCCESS");
    }

    @Operation(summary = "Danh sách media (mới trước)")
    @RequireAuth(roles = {RoleCode.SYSTEM_ADMIN, RoleCode.CONTENT_ADMIN})
    @GetMapping
    public ResponseEntity<ResponseData<PageResponse<MediaResponse>>> list(
            @RequestParam(required = false) vn.slmglobal.slmai.services.media.entity.MediaType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), MAX_PAGE_SIZE));
        Page<MediaItem> result = type == null
                ? items.findAllByOrderByCreatedAtDesc(pageable)
                : items.findByTypeOrderByCreatedAtDesc(type, pageable);
        List<UUID> ids = result.getContent().stream().map(MediaItem::getId).toList();
        Map<UUID, List<SourceResponse>> sourcesByMedia = sources.findByMediaIdIn(ids).stream()
                .collect(Collectors.groupingBy(MediaSource::getMediaId,
                        Collectors.mapping(MediaAdminController::toResponse, Collectors.toList())));
        Map<UUID, List<LinkResponse>> linksByMedia = links.findByMediaIds(ids).stream()
                .collect(Collectors.groupingBy(MediaLinkRepository.Link::mediaId,
                        Collectors.mapping(l -> new LinkResponse(l.targetType().name(), l.targetKey(), l.role().name()),
                                Collectors.toList())));
        return ApiResponses.ok(PageResponse.of(result, m -> new MediaResponse(m.getId(), m.getType().name(),
                m.getTitle(), m.getStatus().name(), m.getRights().name(), m.getCreatedBy(), m.getCreatedAt(),
                sourcesByMedia.getOrDefault(m.getId(), List.of()), linksByMedia.getOrDefault(m.getId(), List.of()))),
                "MEDIA_LIST_SUCCESS");
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

    private static SourceResponse toResponse(MediaSource s) {
        return new SourceResponse(s.getId(), s.getSourceType().name(), s.getSourceUrl(), s.getExternalId(),
                s.getMimeType(), s.getSizeBytes(), s.getWidth(), s.getHeight(), s.getProcessingStatus().name(),
                s.getAvailabilityStatus().name(), s.getObjectKey() != null);
    }
}
