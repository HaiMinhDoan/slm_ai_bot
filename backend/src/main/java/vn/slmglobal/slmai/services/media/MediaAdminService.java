package vn.slmglobal.slmai.services.media;

import jakarta.persistence.criteria.Predicate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.common.security.AuthInfo;
import vn.slmglobal.slmai.common.web.PageResponse;
import vn.slmglobal.slmai.services.auth.AuditLogService;
import vn.slmglobal.slmai.services.media.entity.LinkTargetType;
import vn.slmglobal.slmai.services.media.entity.MediaItem;
import vn.slmglobal.slmai.services.media.entity.MediaSource;
import vn.slmglobal.slmai.services.media.entity.MediaStatus;
import vn.slmglobal.slmai.services.media.entity.MediaType;
import vn.slmglobal.slmai.services.media.entity.RightsStatus;
import vn.slmglobal.slmai.services.media.repository.MediaItemRepository;
import vn.slmglobal.slmai.services.media.repository.MediaLinkRepository;
import vn.slmglobal.slmai.services.media.repository.MediaSourceRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Danh sách, sửa, duyệt, xoá mềm media trên admin (mục 6.2). Xoá mềm giữ file MinIO, nguồn và liên kết để
 * khôi phục được. Duyệt (APPROVED) cần tóm tắt, "dùng khi" và quyền sử dụng đã rõ; lần duyệt đầu cấp mã danh mục
 * M01, M02... (mục 7.1). Mọi thao tác ghi nhật ký.
 */
@Service
@RequiredArgsConstructor
public class MediaAdminService {

    private static final String TARGET = "media";
    /** Giá trị bộ lọc trạng thái: chỉ mục đã xoá mềm (mặc định danh sách ẩn chúng) */
    public static final String STATUS_DELETED = "DELETED";

    private final MediaItemRepository items;
    private final MediaSourceRepository sources;
    private final MediaLinkRepository links;
    private final AuditLogService audit;

    public record SourceResponse(UUID id, String sourceType, String sourceUrl, String externalId, String mimeType,
                                 Long sizeBytes, Integer width, Integer height, String processingStatus,
                                 String availabilityStatus, boolean stored) {
    }

    public record LinkResponse(String targetType, String targetKey, String role) {
    }

    public record MediaResponse(UUID id, String type, String title, String indexCode, String status, String rights,
                                String summary, String useWhen, String avoidWhen, String description,
                                String transcript, String createdBy, OffsetDateTime createdAt, String updatedBy,
                                OffsetDateTime updatedAt, String approvedBy, OffsetDateTime approvedAt,
                                boolean deleted, String deletedBy, OffsetDateTime deletedAt,
                                List<SourceResponse> sources, List<LinkResponse> links) {
    }

    /** @param status DRAFT, APPROVED, ARCHIVED hoặc DELETED; null = mọi mục chưa xoá */
    public record MediaFilter(String keyword, MediaType type, String status, Integer packageId) {
    }

    /** Toàn bộ nội dung form sửa (form gửi cả trường không đổi) */
    public record UpdateRequest(
            @NotBlank(message = "Chưa nhập tiêu đề") @Size(max = 300, message = "Tiêu đề tối đa 300 ký tự") String title,
            @NotNull(message = "Chưa chọn trạng thái") MediaStatus status,
            @NotNull(message = "Chưa chọn quyền sử dụng") RightsStatus rights,
            @Size(max = 2000, message = "Tóm tắt tối đa 2000 ký tự") String summary,
            @Size(max = 2000, message = "Dùng khi tối đa 2000 ký tự") String useWhen,
            @Size(max = 2000, message = "Tránh khi tối đa 2000 ký tự") String avoidWhen,
            @Size(max = 10000, message = "Mô tả tối đa 10000 ký tự") String description,
            @Size(max = 50000, message = "Lời thoại tối đa 50000 ký tự") String transcript) {
    }

    @Transactional(readOnly = true)
    public PageResponse<MediaResponse> list(MediaFilter filter, Pageable pageable) {
        if (filter.status() != null && !filter.status().isBlank() && !STATUS_DELETED.equals(filter.status())
                && Arrays.stream(MediaStatus.values()).noneMatch(v -> v.name().equals(filter.status()))) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Trạng thái lọc không hợp lệ: " + filter.status());
        }
        Specification<MediaItem> spec = specOf(filter);
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id")));
        Page<MediaItem> page = spec == null ? Page.empty(sorted) : items.findAll(spec, sorted);
        List<UUID> ids = page.getContent().stream().map(MediaItem::getId).toList();
        Map<UUID, List<SourceResponse>> sourcesByMedia = sources.findByMediaIdIn(ids).stream()
                .collect(Collectors.groupingBy(MediaSource::getMediaId,
                        Collectors.mapping(MediaAdminService::toResponse, Collectors.toList())));
        Map<UUID, List<LinkResponse>> linksByMedia = links.findByMediaIds(ids).stream()
                .collect(Collectors.groupingBy(MediaLinkRepository.Link::mediaId,
                        Collectors.mapping(l -> new LinkResponse(l.targetType().name(), l.targetKey(), l.role().name()),
                                Collectors.toList())));
        return PageResponse.of(page, m -> toResponse(m, sourcesByMedia.getOrDefault(m.getId(), List.of()),
                linksByMedia.getOrDefault(m.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public MediaResponse get(UUID id) {
        return toResponse(find(id));
    }

    @Transactional
    public MediaResponse update(AuthInfo actor, UUID id, UpdateRequest r) {
        MediaItem m = find(id);
        if (m.isDeleted()) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Media đã xoá, khôi phục trước khi sửa");
        }
        String summary = blankToNull(r.summary());
        String useWhen = blankToNull(r.useWhen());
        if (r.status() == MediaStatus.APPROVED) {
            if (summary == null || useWhen == null) {
                throw new CommonException(HttpStatus.BAD_REQUEST,
                        "Muốn duyệt thì điền Tóm tắt và Dùng khi: AI chọn media dựa vào hai ô này");
            }
            if (r.rights() == RightsStatus.NEEDS_REVIEW) {
                throw new CommonException(HttpStatus.BAD_REQUEST, "Chọn quyền sử dụng trước khi duyệt");
            }
        }
        Map<String, Object> before = snapshot(m);
        if (r.status() == MediaStatus.APPROVED && m.getStatus() != MediaStatus.APPROVED) {
            m.setApprovedBy(actor.email());
            m.setApprovedAt(OffsetDateTime.now());
            if (m.getIndexCode() == null) {
                // Mã giữ nguyên suốt đời media (kể cả khi bỏ duyệt) để danh mục cho AI ổn định giữa các lượt
                m.setIndexCode(String.format("M%02d", items.maxIndexNumber() + 1));
            }
        } else if (r.status() != MediaStatus.APPROVED) {
            m.setApprovedBy(null);
            m.setApprovedAt(null);
        }
        m.setTitle(r.title().strip());
        m.setStatus(r.status());
        m.setRights(r.rights());
        m.setSummary(summary);
        m.setUseWhen(useWhen);
        m.setAvoidWhen(blankToNull(r.avoidWhen()));
        m.setDescription(blankToNull(r.description()));
        m.setTranscript(blankToNull(r.transcript()));
        m.setUpdatedBy(actor.email());
        items.saveAndFlush(m);
        audit.record(actor.id(), "media.update", TARGET, id, before, snapshot(m));
        return toResponse(m);
    }

    @Transactional
    public void delete(AuthInfo actor, UUID id) {
        MediaItem m = find(id);
        if (m.isDeleted()) {
            return;
        }
        m.setDeletedAt(OffsetDateTime.now());
        m.setDeletedBy(actor.email());
        m.setUpdatedBy(actor.email());
        items.saveAndFlush(m);
        audit.record(actor.id(), "media.delete", TARGET, id, null, Map.of("title", m.getTitle()));
    }

    @Transactional
    public MediaResponse restore(AuthInfo actor, UUID id) {
        MediaItem m = find(id);
        if (m.isDeleted()) {
            m.setDeletedAt(null);
            m.setDeletedBy(null);
            m.setUpdatedBy(actor.email());
            items.saveAndFlush(m);
            audit.record(actor.id(), "media.restore", TARGET, id, null, Map.of("title", m.getTitle()));
        }
        return toResponse(m);
    }

    /** null = chắc chắn không có kết quả (lọc theo gói không có media nào) */
    private Specification<MediaItem> specOf(MediaFilter f) {
        List<UUID> linked = f.packageId() == null ? null
                : links.mediaIdsOf(LinkTargetType.PACKAGE, String.valueOf(f.packageId()));
        if (linked != null && linked.isEmpty()) {
            return null;
        }
        return (root, query, cb) -> {
            List<Predicate> where = new ArrayList<>();
            if (STATUS_DELETED.equals(f.status())) {
                where.add(cb.isNotNull(root.get("deletedAt")));
            } else {
                where.add(cb.isNull(root.get("deletedAt")));
                if (f.status() != null && !f.status().isBlank()) {
                    where.add(cb.equal(root.get("status"), MediaStatus.valueOf(f.status())));
                }
            }
            if (f.type() != null) {
                where.add(cb.equal(root.get("type"), f.type()));
            }
            String keyword = blankToNull(f.keyword());
            if (keyword != null) {
                where.add(cb.or(
                        cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase(Locale.ROOT) + "%"),
                        cb.equal(root.get("indexCode"), keyword.toUpperCase(Locale.ROOT))));
            }
            if (linked != null) {
                where.add(root.get("id").in(linked));
            }
            return cb.and(where.toArray(Predicate[]::new));
        };
    }

    private MediaItem find(UUID id) {
        return items.findById(id).orElseThrow(() -> CommonException.notFound("Không có media " + id));
    }

    private MediaResponse toResponse(MediaItem m) {
        List<SourceResponse> src = sources.findByMediaIdIn(List.of(m.getId())).stream()
                .map(MediaAdminService::toResponse).toList();
        List<LinkResponse> lnk = links.findByMediaIds(List.of(m.getId())).stream()
                .map(l -> new LinkResponse(l.targetType().name(), l.targetKey(), l.role().name())).toList();
        return toResponse(m, src, lnk);
    }

    private static MediaResponse toResponse(MediaItem m, List<SourceResponse> src, List<LinkResponse> lnk) {
        return new MediaResponse(m.getId(), m.getType().name(), m.getTitle(), m.getIndexCode(), m.getStatus().name(),
                m.getRights().name(), m.getSummary(), m.getUseWhen(), m.getAvoidWhen(), m.getDescription(),
                m.getTranscript(), m.getCreatedBy(), m.getCreatedAt(), m.getUpdatedBy(), m.getUpdatedAt(),
                m.getApprovedBy(), m.getApprovedAt(), m.isDeleted(), m.getDeletedBy(), m.getDeletedAt(), src, lnk);
    }

    private static SourceResponse toResponse(MediaSource s) {
        return new SourceResponse(s.getId(), s.getSourceType().name(), s.getSourceUrl(), s.getExternalId(),
                s.getMimeType(), s.getSizeBytes(), s.getWidth(), s.getHeight(), s.getProcessingStatus().name(),
                s.getAvailabilityStatus().name(), s.getObjectKey() != null);
    }

    /** Các trường sửa được, để nhật ký ghi trước / sau */
    private static Map<String, Object> snapshot(MediaItem m) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("title", m.getTitle());
        data.put("status", m.getStatus());
        data.put("rights", m.getRights());
        data.put("indexCode", m.getIndexCode());
        data.put("summary", m.getSummary());
        data.put("useWhen", m.getUseWhen());
        data.put("avoidWhen", m.getAvoidWhen());
        data.put("description", m.getDescription());
        data.put("transcript", m.getTranscript());
        return data;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
