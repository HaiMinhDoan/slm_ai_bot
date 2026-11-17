package vn.slmglobal.slmai.services.media;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
import org.springframework.transaction.support.TransactionTemplate;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.common.security.AuthInfo;
import vn.slmglobal.slmai.common.web.PageResponse;
import vn.slmglobal.slmai.services.auth.AuditLogService;
import vn.slmglobal.slmai.services.auth.repository.AuditLogRepository;
import vn.slmglobal.slmai.services.media.entity.LinkRole;
import vn.slmglobal.slmai.services.media.entity.LinkTargetType;
import vn.slmglobal.slmai.services.media.entity.MediaItem;
import vn.slmglobal.slmai.services.media.entity.MediaSource;
import vn.slmglobal.slmai.services.media.entity.MediaStatus;
import vn.slmglobal.slmai.services.media.entity.MediaType;
import vn.slmglobal.slmai.services.media.entity.RightsStatus;
import vn.slmglobal.slmai.services.media.entity.Topic;
import vn.slmglobal.slmai.services.media.repository.MediaItemRepository;
import vn.slmglobal.slmai.services.media.repository.MediaLinkRepository;
import vn.slmglobal.slmai.services.media.repository.MediaSourceRepository;
import vn.slmglobal.slmai.services.media.repository.MediaTargetRepository;
import vn.slmglobal.slmai.services.media.repository.TopicRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Danh sách, sửa, duyệt, xoá mềm media trên admin (mục 6.5). Xoá mềm giữ file MinIO, nguồn và liên kết để
 * khôi phục được. Duyệt (APPROVED) cần tóm tắt, "dùng khi" và quyền sử dụng đã rõ; lần duyệt đầu cấp mã danh mục
 * M01, M02... (mục 7.1). Từ 2026-11-12 sửa được mọi trường nội dung (chủ đề, giai đoạn khách, kênh), liên kết với
 * gói / vật tư / hãng / tài liệu, xem lịch sử và thao tác hàng loạt. Mọi thao tác ghi nhật ký.
 */
@Service
@RequiredArgsConstructor
public class MediaAdminService {

    static final String TARGET = "media";
    /** Giá trị bộ lọc trạng thái: chỉ mục đã xoá mềm (mặc định danh sách ẩn chúng) */
    public static final String STATUS_DELETED = "DELETED";
    /** Giai đoạn khách (mục 4.1) media hợp để gửi; rỗng là mọi giai đoạn */
    public static final List<String> CUSTOMER_STAGES = List.of("LEAD", "CONSULTING", "WON", "INSTALLATION",
            "INSTALLED", "AFTER_SALES", "REFERRAL");
    /** Kênh nhắn tin gửi được media */
    public static final List<String> CHANNELS = List.of("MESSENGER", "ZALO_OA", "TIKTOK_DM");
    public static final int MAX_BULK = 100;
    private static final int HISTORY_LIMIT = 100;

    private final MediaItemRepository items;
    private final MediaSourceRepository sources;
    private final MediaLinkRepository links;
    private final MediaTargetRepository targets;
    private final TopicRepository topics;
    private final AuditLogService audit;
    private final AuditLogRepository auditLogs;
    private final TransactionTemplate tx;

    /**
     * @param hasThumbnail video tải lên có ảnh thu nhỏ lưu trong hệ thống
     * @param thumbnailUrl ảnh xem trước của link ngoài (YouTube, TikTok)
     */
    public record SourceResponse(UUID id, String sourceType, String sourceUrl, String externalId, String authorName,
                                 String mimeType, Long sizeBytes, Integer width, Integer height,
                                 Integer durationSeconds, String sha256, String thumbnailUrl, boolean hasThumbnail,
                                 String processingStatus, String availabilityStatus, OffsetDateTime lastCheckedAt,
                                 OffsetDateTime createdAt, boolean stored) {
    }

    /** @param label tên đối tượng (gói, vật tư, hãng, tài liệu); null nếu đối tượng không còn */
    public record LinkResponse(String targetType, String targetKey, String role, int sortOrder, String label) {
    }

    public record MediaResponse(UUID id, String type, String title, String indexCode, String status, String rights,
                                String summary, String useWhen, String avoidWhen, String description,
                                String transcript, List<String> topics, List<String> customerStages,
                                List<String> allowedChannels, String createdBy, OffsetDateTime createdAt,
                                String updatedBy, OffsetDateTime updatedAt, String approvedBy,
                                OffsetDateTime approvedAt, boolean deleted, String deletedBy, OffsetDateTime deletedAt,
                                List<SourceResponse> sources, List<LinkResponse> links) {
    }

    /** @param status DRAFT, APPROVED, ARCHIVED hoặc DELETED; null = mọi mục chưa xoá */
    public record MediaFilter(String keyword, MediaType type, String status, Integer packageId) {
    }

    /**
     * Toàn bộ nội dung form sửa (form gửi cả trường không đổi). Ba danh sách cuối null là giữ nguyên.
     *
     * @param customerStages  giai đoạn khách hợp để gửi ({@link #CUSTOMER_STAGES}); rỗng là mọi giai đoạn
     * @param allowedChannels kênh được gửi ({@link #CHANNELS})
     */
    public record UpdateRequest(
            @NotBlank(message = "Chưa nhập tiêu đề") @Size(max = 300, message = "Tiêu đề tối đa 300 ký tự") String title,
            @NotNull(message = "Chưa chọn trạng thái") MediaStatus status,
            @NotNull(message = "Chưa chọn quyền sử dụng") RightsStatus rights,
            @Size(max = 2000, message = "Tóm tắt tối đa 2000 ký tự") String summary,
            @Size(max = 2000, message = "Dùng khi tối đa 2000 ký tự") String useWhen,
            @Size(max = 2000, message = "Tránh khi tối đa 2000 ký tự") String avoidWhen,
            @Size(max = 10000, message = "Mô tả tối đa 10000 ký tự") String description,
            @Size(max = 50000, message = "Lời thoại tối đa 50000 ký tự") String transcript,
            @Size(max = 20, message = "Tối đa 20 chủ đề") List<String> topics,
            List<String> customerStages,
            List<String> allowedChannels) {

        public UpdateRequest(String title, MediaStatus status, RightsStatus rights, String summary, String useWhen,
                             String avoidWhen, String description, String transcript) {
            this(title, status, rights, summary, useWhen, avoidWhen, description, transcript, null, null, null);
        }
    }

    /** Một liên kết trên form; {@code sortOrder} null là 0 */
    public record LinkInput(@NotNull(message = "Chưa chọn loại đối tượng") LinkTargetType targetType,
                            @NotBlank(message = "Chưa chọn đối tượng") @Size(max = 200) String targetKey,
                            @NotNull(message = "Chưa chọn vai trò") LinkRole role,
                            Integer sortOrder) {
    }

    /** @param notes thay đổi phụ cần báo admin, ví dụ ảnh chính cũ của gói chuyển thành ảnh thêm */
    public record LinksResult(MediaResponse media, List<String> notes) {
    }

    public record HistoryEntry(OffsetDateTime at, String actorName, String action, JsonNode before, JsonNode after) {
    }

    /**
     * Thông tin nhập cùng lúc thêm media (hộp "Thêm media", 2026-11-17): mọi trường của form sửa, áp cho từng
     * file / link trong lần thêm. Trường null giữ mặc định; {@code title} chỉ gửi khi thêm đúng một mục.
     *
     * @param aiDraft Claude viết nháp các ô cho AI sau khi thêm, chỉ điền ô còn trống; media giữ ở Nháp để duyệt
     */
    public record CreateInfo(
            @Size(max = 300, message = "Tiêu đề tối đa 300 ký tự") String title,
            MediaStatus status,
            RightsStatus rights,
            @Size(max = 2000, message = "Tóm tắt tối đa 2000 ký tự") String summary,
            @Size(max = 2000, message = "Dùng khi tối đa 2000 ký tự") String useWhen,
            @Size(max = 2000, message = "Tránh khi tối đa 2000 ký tự") String avoidWhen,
            @Size(max = 10000, message = "Mô tả tối đa 10000 ký tự") String description,
            @Size(max = 50000, message = "Lời thoại tối đa 50000 ký tự") String transcript,
            @Size(max = 20, message = "Tối đa 20 chủ đề") List<String> topics,
            List<String> customerStages,
            List<String> allowedChannels,
            @Valid List<LinkInput> links,
            boolean aiDraft) {

        /** Trạng thái sẽ ghi: có AI viết nháp thì luôn là Nháp (nội dung AI phải được xem lại trước khi duyệt) */
        MediaStatus effectiveStatus() {
            return aiDraft || status == null ? MediaStatus.DRAFT : status;
        }
    }

    public enum BulkAction { SET_STATUS, SET_RIGHTS, ADD_TOPICS, ADD_LINK, DELETE, RESTORE }

    /** Thao tác hàng loạt; trường đi kèm tuỳ {@code action} (SET_STATUS cần status, ADD_LINK cần đối tượng...) */
    public record BulkRequest(@NotEmpty(message = "Chưa chọn media") @Size(max = MAX_BULK) List<UUID> ids,
                              @NotNull(message = "Chưa chọn thao tác") BulkAction action,
                              MediaStatus status, RightsStatus rights, List<String> topics,
                              @Valid LinkInput link) {
    }

    public record BulkResult(UUID id, String title, boolean ok, String note) {
    }

    // ------------------------------------------------------------------ đọc

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
        Map<UUID, List<LinkResponse>> linksByMedia = linkResponses(links.findByMediaIds(ids));
        return PageResponse.of(page, m -> toResponse(m, sourcesByMedia.getOrDefault(m.getId(), List.of()),
                linksByMedia.getOrDefault(m.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public MediaResponse get(UUID id) {
        return toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public List<HistoryEntry> history(UUID id) {
        find(id);
        return auditLogs.findByTarget(TARGET, id.toString(), PageRequest.of(0, HISTORY_LIMIT)).stream()
                .map(a -> new HistoryEntry(a.getCreatedAt(), a.getActor() == null ? null : a.getActor().getFullName(),
                        a.getAction(), a.getBeforeData(), a.getAfterData()))
                .toList();
    }

    /** Gói / vật tư / hãng / tài liệu để chọn khi gắn media */
    @Transactional(readOnly = true)
    public List<MediaTargetRepository.Target> searchTargets(LinkTargetType type, String keyword) {
        return targets.search(type, keyword, 30);
    }

    // ------------------------------------------------------------------ sửa

    @Transactional
    public MediaResponse update(AuthInfo actor, UUID id, UpdateRequest r) {
        MediaItem m = editable(id);
        Map<String, Object> before = snapshot(m);
        if (r.topics() != null) {
            m.setTopics(checkedTopics(r.topics(), m.getTopics()).toArray(String[]::new));
        }
        if (r.customerStages() != null) {
            m.setCustomerStages(checked(r.customerStages(), CUSTOMER_STAGES, "Giai đoạn khách").toArray(String[]::new));
        }
        if (r.allowedChannels() != null) {
            m.setAllowedChannels(checked(r.allowedChannels(), CHANNELS, "Kênh").toArray(String[]::new));
        }
        m.setTitle(r.title().strip());
        m.setSummary(blankToNull(r.summary()));
        m.setUseWhen(blankToNull(r.useWhen()));
        m.setAvoidWhen(blankToNull(r.avoidWhen()));
        m.setDescription(blankToNull(r.description()));
        m.setTranscript(blankToNull(r.transcript()));
        m.setRights(r.rights());
        applyStatus(actor, m, r.status());
        m.setUpdatedBy(actor.email());
        items.saveAndFlush(m);
        audit.record(actor.id(), "media.update", TARGET, id, before, snapshot(m));
        return toResponse(m);
    }

    /**
     * Ghi lại toàn bộ liên kết của một media. Ảnh chính của một đối tượng chỉ có một: gắn ảnh này làm ảnh chính thì
     * ảnh chính cũ của đối tượng đó chuyển thành ảnh thêm.
     */
    @Transactional
    public LinksResult updateLinks(AuthInfo actor, UUID id, List<LinkInput> input) {
        MediaItem m = editable(id);
        List<LinkInput> wanted = checkedLinks(m.getType(), input == null ? List.of() : input);
        List<MediaLinkRepository.Link> before = links.findByMediaIds(List.of(id));
        links.deleteByMedia(id);
        List<String> notes = new ArrayList<>();
        for (LinkInput l : wanted) {
            notes.addAll(insertLink(id, l));
        }
        m.setUpdatedBy(actor.email());
        items.saveAndFlush(m);
        audit.record(actor.id(), "media.links", TARGET, id, linkSnapshot(before),
                linkSnapshot(links.findByMediaIds(List.of(id))));
        return new LinksResult(toResponse(m), notes);
    }

    @Transactional
    public void delete(AuthInfo actor, UUID id) {
        softDelete(actor, find(id));
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

    // ------------------------------------------------------------------ thông tin nhập lúc thêm

    /**
     * Kiểm tra thông tin nhập lúc thêm trước khi lưu file: sai thì báo lỗi, không tạo media và không ghi gì vào MinIO.
     *
     * @param rights quyền sẽ ghi (mặc định theo loại nguồn nếu admin không chọn)
     */
    void checkCreateInfo(CreateInfo info, MediaType type, RightsStatus rights) {
        if (info == null) {
            return;
        }
        checkedTopics(info.topics() == null ? List.of() : info.topics(), new String[0]);
        checked(info.customerStages() == null ? List.of() : info.customerStages(), CUSTOMER_STAGES, "Giai đoạn khách");
        checked(info.allowedChannels() == null ? List.of() : info.allowedChannels(), CHANNELS, "Kênh");
        checkedLinks(type, info.links() == null ? List.of() : info.links());
        if (info.effectiveStatus() == MediaStatus.APPROVED) {
            require(blankToNull(info.summary()) != null && blankToNull(info.useWhen()) != null,
                    "Muốn duyệt thì điền Tóm tắt và Dùng khi: AI chọn media dựa vào hai ô này");
            require(rights != RightsStatus.NEEDS_REVIEW, "Chọn quyền sử dụng trước khi duyệt");
        }
    }

    /**
     * Ghi thông tin nhập lúc thêm vào media vừa tạo, trong transaction tạo media (media và nguồn đã ghi). Trả ghi chú
     * cho admin (ví dụ ảnh chính cũ của gói chuyển thành ảnh thêm).
     */
    List<String> applyCreateInfo(AuthInfo actor, MediaItem m, CreateInfo info) {
        if (info == null) {
            return List.of();
        }
        m.setSummary(blankToNull(info.summary()));
        m.setUseWhen(blankToNull(info.useWhen()));
        m.setAvoidWhen(blankToNull(info.avoidWhen()));
        m.setDescription(blankToNull(info.description()));
        m.setTranscript(m.getType() == MediaType.VIDEO ? blankToNull(info.transcript()) : null);
        if (info.topics() != null) {
            m.setTopics(checkedTopics(info.topics(), new String[0]).toArray(String[]::new));
        }
        if (info.customerStages() != null) {
            m.setCustomerStages(checked(info.customerStages(), CUSTOMER_STAGES, "Giai đoạn khách")
                    .toArray(String[]::new));
        }
        if (info.allowedChannels() != null) {
            m.setAllowedChannels(checked(info.allowedChannels(), CHANNELS, "Kênh").toArray(String[]::new));
        }
        applyStatus(actor, m, info.effectiveStatus());
        m.setUpdatedBy(actor.email());
        items.saveAndFlush(m);
        List<String> notes = new ArrayList<>();
        for (LinkInput l : checkedLinks(m.getType(), info.links() == null ? List.of() : info.links())) {
            notes.addAll(insertLink(m.getId(), l));
        }
        audit.record(actor.id(), "media.update", TARGET, m.getId(), null, snapshot(m));
        if (info.links() != null && !info.links().isEmpty()) {
            audit.record(actor.id(), "media.links", TARGET, m.getId(), List.of(),
                    linkSnapshot(links.findByMediaIds(List.of(m.getId()))));
        }
        return notes;
    }

    /** Nháp của AI lúc thêm media: chỉ điền ô còn trống (giữ nội dung admin đã nhập), chủ đề gộp thêm */
    @Transactional
    public void fillDraft(AuthInfo actor, UUID id, String summary, String useWhen, String avoidWhen,
                          String description, List<String> draftTopics) {
        MediaItem m = editable(id);
        Map<String, Object> before = snapshot(m);
        if (m.getSummary() == null) {
            m.setSummary(blankToNull(summary));
        }
        if (m.getUseWhen() == null) {
            m.setUseWhen(blankToNull(useWhen));
        }
        if (m.getAvoidWhen() == null) {
            m.setAvoidWhen(blankToNull(avoidWhen));
        }
        if (m.getDescription() == null) {
            m.setDescription(blankToNull(description));
        }
        Set<String> all = new LinkedHashSet<>(List.of(m.getTopics()));
        Set<String> active = topics.findAll().stream().filter(Topic::isActive).map(Topic::getCode)
                .collect(Collectors.toSet());
        draftTopics.stream().filter(active::contains).forEach(all::add);
        m.setTopics(all.toArray(String[]::new));
        m.setUpdatedBy(actor.email());
        items.saveAndFlush(m);
        audit.record(actor.id(), "media.update", TARGET, id, before, snapshot(m));
    }

    // ------------------------------------------------------------------ hàng loạt

    /** Mỗi media xử lý trong transaction riêng: mục lỗi (ví dụ duyệt khi chưa có tóm tắt) không chặn mục khác */
    public List<BulkResult> bulk(AuthInfo actor, BulkRequest r) {
        switch (r.action()) {
            case SET_STATUS -> require(r.status() != null, "Chưa chọn trạng thái");
            case SET_RIGHTS -> require(r.rights() != null, "Chưa chọn quyền sử dụng");
            case ADD_TOPICS -> require(r.topics() != null && !r.topics().isEmpty(), "Chưa chọn chủ đề");
            case ADD_LINK -> {
                require(r.link() != null, "Chưa chọn đối tượng để gắn");
                require(r.link().role() != LinkRole.MAIN_IMAGE || r.ids().size() == 1,
                        "Mỗi đối tượng chỉ có một ảnh chính: gắn nhiều ảnh thì chọn vai trò Ảnh thêm");
                require(!targets.labels(r.link().targetType(), List.of(r.link().targetKey().strip())).isEmpty(),
                        "Không tìm thấy đối tượng " + r.link().targetKey());
            }
            default -> {
            }
        }
        List<BulkResult> results = new ArrayList<>();
        for (UUID id : new LinkedHashSet<>(r.ids())) {
            try {
                String note = tx.execute(st -> applyBulk(actor, id, r));
                results.add(new BulkResult(id, items.findById(id).map(MediaItem::getTitle).orElse(null), true, note));
            } catch (CommonException e) {
                results.add(new BulkResult(id, items.findById(id).map(MediaItem::getTitle).orElse(null), false,
                        e.getMessage()));
            }
        }
        return results;
    }

    private String applyBulk(AuthInfo actor, UUID id, BulkRequest r) {
        if (r.action() == BulkAction.DELETE) {
            softDelete(actor, find(id));
            return null;
        }
        if (r.action() == BulkAction.RESTORE) {
            restore(actor, id);
            return null;
        }
        MediaItem m = editable(id);
        if (r.action() == BulkAction.ADD_LINK) {
            LinkInput l = checkedLinks(m.getType(), List.of(r.link())).get(0);
            List<MediaLinkRepository.Link> before = links.findByMediaIds(List.of(id));
            List<String> notes = insertLink(id, l);
            audit.record(actor.id(), "media.links", TARGET, id, linkSnapshot(before),
                    linkSnapshot(links.findByMediaIds(List.of(id))));
            return notes.isEmpty() ? null : String.join("; ", notes);
        }
        Map<String, Object> before = snapshot(m);
        switch (r.action()) {
            case SET_STATUS -> applyStatus(actor, m, r.status());
            case SET_RIGHTS -> {
                m.setRights(r.rights());
                applyStatus(actor, m, m.getStatus());
            }
            case ADD_TOPICS -> {
                Set<String> all = new LinkedHashSet<>(List.of(m.getTopics()));
                all.addAll(checkedTopics(r.topics(), m.getTopics()));
                m.setTopics(all.toArray(String[]::new));
            }
            default -> throw new IllegalStateException(r.action().name());
        }
        m.setUpdatedBy(actor.email());
        items.saveAndFlush(m);
        audit.record(actor.id(), "media.update", TARGET, id, before, snapshot(m));
        return null;
    }

    // ------------------------------------------------------------------ kiểm tra

    /**
     * Đổi trạng thái. Duyệt cần tóm tắt, "dùng khi" và quyền sử dụng đã rõ; lần duyệt đầu cấp mã danh mục, mã giữ
     * nguyên suốt đời media (kể cả khi bỏ duyệt) để danh mục cho AI ổn định giữa các lượt.
     */
    private void applyStatus(AuthInfo actor, MediaItem m, MediaStatus status) {
        if (status == MediaStatus.APPROVED) {
            if (m.getSummary() == null || m.getUseWhen() == null) {
                throw new CommonException(HttpStatus.BAD_REQUEST,
                        "Muốn duyệt thì điền Tóm tắt và Dùng khi: AI chọn media dựa vào hai ô này");
            }
            if (m.getRights() == RightsStatus.NEEDS_REVIEW) {
                throw new CommonException(HttpStatus.BAD_REQUEST, "Chọn quyền sử dụng trước khi duyệt");
            }
            if (m.getStatus() != MediaStatus.APPROVED) {
                m.setApprovedBy(actor.email());
                m.setApprovedAt(OffsetDateTime.now());
            }
            if (m.getIndexCode() == null) {
                m.setIndexCode(String.format("M%02d", items.maxIndexNumber() + 1));
            }
        } else {
            m.setApprovedBy(null);
            m.setApprovedAt(null);
        }
        m.setStatus(status);
    }

    /** Chủ đề phải có trong danh sách; chủ đề đã tắt chỉ giữ được nếu media vốn đã có */
    private List<String> checkedTopics(List<String> codes, String[] current) {
        Map<String, Topic> all = topics.findAll().stream().collect(Collectors.toMap(Topic::getCode, t -> t));
        Set<String> had = new HashSet<>(List.of(current));
        List<String> out = new ArrayList<>();
        for (String raw : codes) {
            String code = raw == null ? "" : raw.strip();
            Topic t = all.get(code);
            if (t == null || (!t.isActive() && !had.contains(code))) {
                throw new CommonException(HttpStatus.BAD_REQUEST, "Chủ đề không có hoặc đã tắt: " + code);
            }
            if (!out.contains(code)) {
                out.add(code);
            }
        }
        return out;
    }

    private static List<String> checked(List<String> values, List<String> allowed, String label) {
        List<String> out = new ArrayList<>();
        for (String raw : values) {
            String v = raw == null ? "" : raw.strip();
            if (!allowed.contains(v)) {
                throw new CommonException(HttpStatus.BAD_REQUEST, label + " không hợp lệ: " + v);
            }
            if (!out.contains(v)) {
                out.add(v);
            }
        }
        // Theo thứ tự cố định: nội dung tất định, nhật ký so trước / sau không lệch vì thứ tự chọn
        out.sort((a, b) -> allowed.indexOf(a) - allowed.indexOf(b));
        return out;
    }

    private List<LinkInput> checkedLinks(MediaType type, List<LinkInput> input) {
        List<LinkInput> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        Map<LinkTargetType, Set<String>> keysByType = new LinkedHashMap<>();
        for (LinkInput l : input) {
            String key = l.targetKey() == null ? "" : l.targetKey().strip();
            if (key.isEmpty() || l.targetType() == null || l.role() == null) {
                throw new CommonException(HttpStatus.BAD_REQUEST, "Liên kết thiếu đối tượng hoặc vai trò");
            }
            if ((l.role() == LinkRole.MAIN_IMAGE || l.role() == LinkRole.GALLERY) && type != MediaType.IMAGE) {
                throw new CommonException(HttpStatus.BAD_REQUEST, "Chỉ ảnh mới làm ảnh chính hoặc ảnh thêm; video chọn "
                        + "vai trò Giải thích, Công trình thực tế, Hướng dẫn hoặc Liên quan");
            }
            if (!seen.add(l.targetType() + ":" + key)) {
                throw new CommonException(HttpStatus.BAD_REQUEST, "Gắn trùng một đối tượng: " + key);
            }
            keysByType.computeIfAbsent(l.targetType(), t -> new LinkedHashSet<>()).add(key);
            out.add(new LinkInput(l.targetType(), key, l.role(), l.sortOrder() == null ? 0 : l.sortOrder()));
        }
        keysByType.forEach((targetType, keys) -> {
            Set<String> found = targets.labels(targetType, keys).keySet();
            List<String> missing = keys.stream().filter(k -> !found.contains(k)).toList();
            if (!missing.isEmpty()) {
                throw new CommonException(HttpStatus.BAD_REQUEST, "Không tìm thấy " + targetName(targetType) + " "
                        + String.join(", ", missing));
            }
        });
        return out;
    }

    /** Ghi một liên kết; ảnh chính thì chuyển ảnh chính cũ của đối tượng thành ảnh thêm. Trả ghi chú cho admin. */
    private List<String> insertLink(UUID mediaId, LinkInput l) {
        List<String> notes = new ArrayList<>();
        if (l.role() == LinkRole.MAIN_IMAGE) {
            Optional<UUID> previous = links.mainImageOf(l.targetType(), l.targetKey())
                    .filter(other -> !other.equals(mediaId));
            previous.ifPresent(other -> {
                links.demoteMainImage(other, l.targetType(), l.targetKey());
                String name = items.findById(other).map(o -> o.getIndexCode() != null ? o.getIndexCode()
                        : "\"" + o.getTitle() + "\"").orElse("cũ");
                notes.add(capitalize(targetName(l.targetType())) + " " + l.targetKey() + ": ảnh chính " + name
                        + " chuyển thành ảnh thêm");
            });
        }
        links.insertIfAbsent(mediaId, l.targetType(), l.targetKey(), l.role(), l.sortOrder());
        return notes;
    }

    private MediaItem editable(UUID id) {
        MediaItem m = find(id);
        if (m.isDeleted()) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Media đã xoá, khôi phục trước khi sửa");
        }
        return m;
    }

    private void softDelete(AuthInfo actor, MediaItem m) {
        if (m.isDeleted()) {
            return;
        }
        m.setDeletedAt(OffsetDateTime.now());
        m.setDeletedBy(actor.email());
        m.setUpdatedBy(actor.email());
        items.saveAndFlush(m);
        audit.record(actor.id(), "media.delete", TARGET, m.getId(), null, Map.of("title", m.getTitle()));
    }

    private static void require(boolean ok, String message) {
        if (!ok) {
            throw new CommonException(HttpStatus.BAD_REQUEST, message);
        }
    }

    // ------------------------------------------------------------------ dựng kết quả

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

    MediaItem find(UUID id) {
        return items.findById(id).orElseThrow(() -> CommonException.notFound("Không có media " + id));
    }

    private MediaResponse toResponse(MediaItem m) {
        List<SourceResponse> src = sources.findByMediaIdIn(List.of(m.getId())).stream()
                .map(MediaAdminService::toResponse).toList();
        return toResponse(m, src, linkResponses(links.findByMediaIds(List.of(m.getId())))
                .getOrDefault(m.getId(), List.of()));
    }

    /** Liên kết kèm tên đối tượng, mỗi loại đối tượng một câu truy vấn */
    private Map<UUID, List<LinkResponse>> linkResponses(List<MediaLinkRepository.Link> all) {
        Map<LinkTargetType, Map<String, String>> labels = new LinkedHashMap<>();
        all.stream().collect(Collectors.groupingBy(MediaLinkRepository.Link::targetType,
                        Collectors.mapping(MediaLinkRepository.Link::targetKey, Collectors.toSet())))
                .forEach((type, keys) -> labels.put(type, targets.labels(type, keys)));
        return all.stream().collect(Collectors.groupingBy(MediaLinkRepository.Link::mediaId, LinkedHashMap::new,
                Collectors.mapping(l -> new LinkResponse(l.targetType().name(), l.targetKey(), l.role().name(),
                                l.sortOrder(), labels.getOrDefault(l.targetType(), Map.of()).get(l.targetKey())),
                        Collectors.toList())));
    }

    private static MediaResponse toResponse(MediaItem m, List<SourceResponse> src, List<LinkResponse> lnk) {
        return new MediaResponse(m.getId(), m.getType().name(), m.getTitle(), m.getIndexCode(), m.getStatus().name(),
                m.getRights().name(), m.getSummary(), m.getUseWhen(), m.getAvoidWhen(), m.getDescription(),
                m.getTranscript(), List.of(m.getTopics()), List.of(m.getCustomerStages()),
                List.of(m.getAllowedChannels()), m.getCreatedBy(), m.getCreatedAt(), m.getUpdatedBy(),
                m.getUpdatedAt(), m.getApprovedBy(), m.getApprovedAt(), m.isDeleted(), m.getDeletedBy(),
                m.getDeletedAt(), src, lnk);
    }

    static SourceResponse toResponse(MediaSource s) {
        return new SourceResponse(s.getId(), s.getSourceType().name(), s.getSourceUrl(), s.getExternalId(),
                s.getAuthorName(), s.getMimeType(), s.getSizeBytes(), s.getWidth(), s.getHeight(),
                s.getDurationSeconds(), s.getSha256(), s.getThumbnailUrl(), s.getThumbnailKey() != null,
                s.getProcessingStatus().name(), s.getAvailabilityStatus().name(), s.getLastCheckedAt(),
                s.getCreatedAt(), s.getObjectKey() != null);
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
        data.put("topics", List.of(m.getTopics()));
        data.put("customerStages", List.of(m.getCustomerStages()));
        data.put("allowedChannels", List.of(m.getAllowedChannels()));
        return data;
    }

    private static List<String> linkSnapshot(List<MediaLinkRepository.Link> list) {
        return list.stream().map(l -> l.targetType() + ":" + l.targetKey() + ":" + l.role() + ":" + l.sortOrder())
                .toList();
    }

    static String targetName(LinkTargetType type) {
        return switch (type) {
            case PACKAGE -> "gói";
            case MATERIAL -> "vật tư";
            case BRAND -> "hãng";
            case KNOWLEDGE_DOC -> "tài liệu";
        };
    }

    private static String capitalize(String s) {
        return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
