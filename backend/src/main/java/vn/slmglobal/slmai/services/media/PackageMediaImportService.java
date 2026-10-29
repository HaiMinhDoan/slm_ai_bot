package vn.slmglobal.slmai.services.media;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.common.storage.StorageService;
import vn.slmglobal.slmai.services.auth.AuditLogService;
import vn.slmglobal.slmai.services.media.entity.AvailabilityStatus;
import vn.slmglobal.slmai.services.media.entity.LinkRole;
import vn.slmglobal.slmai.services.media.entity.LinkTargetType;
import vn.slmglobal.slmai.services.media.entity.MediaItem;
import vn.slmglobal.slmai.services.media.entity.MediaSource;
import vn.slmglobal.slmai.services.media.entity.MediaStatus;
import vn.slmglobal.slmai.services.media.entity.MediaType;
import vn.slmglobal.slmai.services.media.entity.ProcessingStatus;
import vn.slmglobal.slmai.services.media.entity.RightsStatus;
import vn.slmglobal.slmai.services.media.entity.SourceType;
import vn.slmglobal.slmai.services.media.repository.MediaItemRepository;
import vn.slmglobal.slmai.services.media.repository.MediaLinkRepository;
import vn.slmglobal.slmai.services.media.repository.MediaSourceRepository;

import java.io.ByteArrayInputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Nhập ảnh combo (từ Google Drive) và video Facebook vào thư viện media, liên kết với gói slm_v2 (PACKAGE).
 * Dữ liệu đầu vào là danh sách JSON tạo từ bảng combo của doanh nghiệp (không commit bảng lên repo công khai).
 *
 * <ul>
 *   <li>Ghi media / nguồn bằng saveAndFlush: liên kết ghi bằng JDBC ngay sau đó cần dòng media đã có (khoá ngoại).</li>
 *   <li>Chạy lại nhiều lần an toàn: ảnh nhận diện theo {@code gdrive:<file_id>} và sha256, video theo mã chia sẻ.</li>
 *   <li>Mỗi mục xử lý riêng, lỗi mục này không chặn mục khác. Tải ảnh và lưu MinIO nằm ngoài transaction.</li>
 *   <li>Media mới ở trạng thái DRAFT: AI chưa dùng cho tới khi được duyệt (cần tóm tắt và "dùng khi").</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PackageMediaImportService {

    /** Mỗi lần gọi tối đa 10 mục: tải ảnh mất vài giây mỗi mục, tránh quá thời gian chờ của proxy */
    public static final int MAX_ITEMS = 10;

    private static final Pattern FACEBOOK_SHARE = Pattern.compile(
            "^https://(?:www\\.|m\\.)?facebook\\.com/share/([rv])/([A-Za-z0-9_-]{4,64})/?(?:[?#].*)?$");
    private static final Pattern WEB_PRODUCT = Pattern.compile("^https://(?:www\\.)?slmsolar\\.vn/products/(\\d+)/?$");

    private final DriveImageFetcher fetcher;
    private final StorageService storage;
    private final MediaItemRepository items;
    private final MediaSourceRepository sources;
    private final MediaLinkRepository links;
    private final AuditLogService audit;
    private final TransactionTemplate tx;

    /**
     * Một combo.
     *
     * @param tronGoiId    id gói slm_v2 (cơ sở HN)
     * @param tronGoiIdHcm id gói cùng tên ở cơ sở HCM, nếu có
     * @param webUrl       link sản phẩm trên web, chỉ dùng để đối chiếu id
     */
    public record Item(@NotNull Integer tronGoiId, Integer tronGoiIdHcm, @NotBlank @Size(max = 250) String title,
                       String driveUrl, String facebookUrl, String webUrl) {
    }

    /**
     * @param dryRun chạy thử: kiểm tra link và tải thử ảnh nhưng không lưu gì
     * @param rights quyền dùng ảnh; mặc định OWNED (ảnh của công ty)
     */
    public record Request(@NotEmpty @Size(max = MAX_ITEMS) @Valid List<Item> items, boolean dryRun,
                          RightsStatus rights) {
    }

    public enum Outcome { CREATED, EXISTS, WOULD_CREATE, SKIPPED, FAILED }

    public record PartResult(Outcome outcome, UUID mediaId, String note) {

        static PartResult of(Outcome outcome, UUID mediaId, String note) {
            return new PartResult(outcome, mediaId, note);
        }
    }

    public record ItemResult(Integer tronGoiId, Integer tronGoiIdHcm, PartResult image, PartResult video,
                             List<String> warnings) {
    }

    public List<ItemResult> importPackages(Request request, UUID actorId, String actorEmail) {
        if (!request.dryRun() && !storage.isAvailable()) {
            throw new CommonException(HttpStatus.SERVICE_UNAVAILABLE,
                    "MinIO chưa sẵn sàng: kiểm tra MINIO_ENDPOINT, MINIO_ACCESS_KEY, MINIO_SECRET_KEY và bucket "
                            + storage.bucket());
        }
        RightsStatus rights = request.rights() == null ? RightsStatus.OWNED : request.rights();
        List<ItemResult> results = new ArrayList<>();
        for (Item item : request.items()) {
            List<String> warnings = new ArrayList<>();
            checkWebUrl(item, warnings);
            PartResult image = importImage(item, request.dryRun(), rights, actorId, actorEmail, warnings);
            PartResult video = importFacebookVideo(item, request.dryRun(), actorId, actorEmail, warnings);
            results.add(new ItemResult(item.tronGoiId(), item.tronGoiIdHcm(), image, video, List.copyOf(warnings)));
        }
        return results;
    }

    // ---------- ảnh từ Drive ----------

    private PartResult importImage(Item item, boolean dryRun, RightsStatus rights, UUID actorId, String actorEmail,
                                   List<String> warnings) {
        if (isBlank(item.driveUrl())) {
            return PartResult.of(Outcome.SKIPPED, null, "Không có link Drive");
        }
        String fileId = DriveImageFetcher.extractFileId(item.driveUrl()).orElse(null);
        if (fileId == null) {
            return PartResult.of(Outcome.FAILED, null, "Link Drive không hợp lệ");
        }
        String externalId = "gdrive:" + fileId;
        Optional<MediaSource> existing = sources.findFirstBySourceTypeAndExternalId(SourceType.UPLOAD, externalId);
        if (existing.isPresent()) {
            return exists(existing.get().getMediaId(), item, LinkRole.MAIN_IMAGE, dryRun, warnings,
                    "Ảnh đã nhập trước đó");
        }

        DriveImageFetcher.Image img;
        try {
            img = fetcher.fetch(fileId);
        } catch (DriveImageFetcher.DriveFetchException e) {
            return PartResult.of(Outcome.FAILED, null, e.getMessage());
        }
        Optional<MediaSource> sameFile = sources.findFirstBySha256(img.sha256());
        if (sameFile.isPresent()) {
            return exists(sameFile.get().getMediaId(), item, LinkRole.MAIN_IMAGE, dryRun, warnings,
                    "Ảnh trùng nội dung với media đã có");
        }
        String summary = img.mimeType() + ", " + (img.bytes().length / 1024) + " KB"
                + (img.width() == null ? "" : ", " + img.width() + "x" + img.height());
        if (dryRun) {
            return PartResult.of(Outcome.WOULD_CREATE, null, summary);
        }

        // Key theo nội dung: chạy lại hay lỗi giữa chừng cũng không sinh file rác khác tên
        String objectKey = "media/images/" + img.sha256().substring(0, 2) + "/" + img.sha256() + "." + img.extension();
        try {
            storage.put(objectKey, new ByteArrayInputStream(img.bytes()), img.bytes().length, img.mimeType());
        } catch (Exception e) {
            return PartResult.of(Outcome.FAILED, null, "Không lưu được vào MinIO: " + e.getMessage());
        }
        try {
            UUID mediaId = tx.execute(st -> {
                MediaItem media = items.saveAndFlush(MediaItem.builder()
                        .type(MediaType.IMAGE)
                        .title(item.title().strip())
                        .status(MediaStatus.DRAFT)
                        .rights(rights)
                        .createdBy(actorEmail)
                        .build());
                sources.saveAndFlush(MediaSource.builder()
                        .mediaId(media.getId())
                        .sourceType(SourceType.UPLOAD)
                        .sourceUrl(item.driveUrl().strip())
                        .externalId(externalId)
                        .storageBucket(storage.bucket())
                        .objectKey(objectKey)
                        .mimeType(img.mimeType())
                        .sizeBytes((long) img.bytes().length)
                        .sha256(img.sha256())
                        .width(img.width())
                        .height(img.height())
                        .processingStatus(ProcessingStatus.READY)
                        .availabilityStatus(AvailabilityStatus.ACTIVE)
                        .lastCheckedAt(OffsetDateTime.now())
                        .build());
                linkPackages(media.getId(), item, LinkRole.MAIN_IMAGE, warnings);
                audit.record(actorId, "media.import", "media", media.getId(), null,
                        auditData(item, "GOOGLE_DRIVE", externalId, objectKey));
                return media.getId();
            });
            return PartResult.of(Outcome.CREATED, mediaId, summary);
        } catch (DataIntegrityViolationException e) {
            // Lần nhập khác vừa ghi cùng ảnh
            return sources.findFirstBySourceTypeAndExternalId(SourceType.UPLOAD, externalId)
                    .map(s -> PartResult.of(Outcome.EXISTS, s.getMediaId(), "Ảnh vừa được nhập bởi lần chạy khác"))
                    .orElse(PartResult.of(Outcome.FAILED, null, "Lỗi ghi CSDL: " + e.getMostSpecificCause().getMessage()));
        }
    }

    // ---------- video Facebook ----------

    private PartResult importFacebookVideo(Item item, boolean dryRun, UUID actorId, String actorEmail,
                                           List<String> warnings) {
        if (isBlank(item.facebookUrl())) {
            return PartResult.of(Outcome.SKIPPED, null, "Không có link Facebook");
        }
        Matcher m = FACEBOOK_SHARE.matcher(item.facebookUrl().strip());
        if (!m.matches()) {
            return PartResult.of(Outcome.FAILED, null,
                    "Chỉ nhận link chia sẻ dạng facebook.com/share/r/... (reel) hoặc /share/v/... (video)");
        }
        String kind = m.group(1);
        String externalId = "share-" + kind + ":" + m.group(2);
        String url = "https://www.facebook.com/share/" + kind + "/" + m.group(2) + "/";
        Optional<MediaSource> existing = sources.findFirstBySourceTypeAndExternalId(SourceType.FACEBOOK, externalId);
        if (existing.isPresent()) {
            return exists(existing.get().getMediaId(), item, LinkRole.RELATED, dryRun, warnings,
                    "Video đã nhập trước đó");
        }
        String note = "r".equals(kind) ? "Reel Facebook (chỉ lưu link)" : "Video Facebook (chỉ lưu link)";
        if (dryRun) {
            return PartResult.of(Outcome.WOULD_CREATE, null, note);
        }
        try {
            UUID mediaId = tx.execute(st -> {
                MediaItem media = items.saveAndFlush(MediaItem.builder()
                        .type(MediaType.VIDEO)
                        .title("Video: " + item.title().strip())
                        .status(MediaStatus.DRAFT)
                        .rights(RightsStatus.LINK_ONLY)
                        .createdBy(actorEmail)
                        .build());
                sources.saveAndFlush(MediaSource.builder()
                        .mediaId(media.getId())
                        .sourceType(SourceType.FACEBOOK)
                        .sourceUrl(url)
                        .externalId(externalId)
                        .processingStatus(ProcessingStatus.READY)
                        .availabilityStatus(AvailabilityStatus.UNKNOWN)
                        .build());
                linkPackages(media.getId(), item, LinkRole.RELATED, warnings);
                audit.record(actorId, "media.import", "media", media.getId(), null,
                        auditData(item, "FACEBOOK", externalId, null));
                return media.getId();
            });
            return PartResult.of(Outcome.CREATED, mediaId, note);
        } catch (DataIntegrityViolationException e) {
            return sources.findFirstBySourceTypeAndExternalId(SourceType.FACEBOOK, externalId)
                    .map(s -> PartResult.of(Outcome.EXISTS, s.getMediaId(), "Video vừa được nhập bởi lần chạy khác"))
                    .orElse(PartResult.of(Outcome.FAILED, null, "Lỗi ghi CSDL: " + e.getMostSpecificCause().getMessage()));
        }
    }

    // ---------- tiện ích ----------

    /** Media đã có: bổ sung liên kết còn thiếu, trừ khi media đó đã bị xoá mềm (giữ nguyên, báo lại). */
    private PartResult exists(UUID mediaId, Item item, LinkRole role, boolean dryRun, List<String> warnings,
                              String note) {
        if (items.findById(mediaId).map(MediaItem::isDeleted).orElse(false)) {
            return PartResult.of(Outcome.EXISTS, mediaId,
                    note + ", nhưng đã bị xoá trong Thư viện media: khôi phục (lọc Đã xoá) nếu muốn dùng lại");
        }
        if (!dryRun) {
            linkPackages(mediaId, item, role, warnings);
        }
        return PartResult.of(Outcome.EXISTS, mediaId, note);
    }

    /**
     * Liên kết media với gói HN và gói HCM. Gói đã có ảnh chính khác thì giữ nguyên và báo lại; riêng ảnh chính cũ
     * đã bị xoá mềm thì chuyển nó thành ảnh thêm (GALLERY) để ảnh mới làm ảnh chính.
     */
    private void linkPackages(UUID mediaId, Item item, LinkRole role, List<String> warnings) {
        tx.executeWithoutResult(st -> {
            for (Integer packageId : new Integer[]{item.tronGoiId(), item.tronGoiIdHcm()}) {
                if (packageId == null) {
                    continue;
                }
                String key = String.valueOf(packageId);
                if (role == LinkRole.MAIN_IMAGE) {
                    links.mainImageOf(LinkTargetType.PACKAGE, key)
                            .filter(other -> !other.equals(mediaId))
                            .filter(other -> items.findById(other).map(MediaItem::isDeleted).orElse(false))
                            .ifPresent(deleted -> links.demoteMainImage(deleted, LinkTargetType.PACKAGE, key));
                }
                boolean inserted = links.insertIfAbsent(mediaId, LinkTargetType.PACKAGE, key, role, 0);
                if (!inserted && role == LinkRole.MAIN_IMAGE) {
                    links.mainImageOf(LinkTargetType.PACKAGE, key)
                            .filter(other -> !other.equals(mediaId))
                            .ifPresent(other -> warnings.add("Gói " + key + " đã có ảnh chính khác, giữ nguyên"));
                }
            }
        });
    }

    private static void checkWebUrl(Item item, List<String> warnings) {
        if (isBlank(item.webUrl())) {
            return;
        }
        Matcher m = WEB_PRODUCT.matcher(item.webUrl().strip());
        if (!m.matches()) {
            warnings.add("Link web không đúng dạng slmsolar.vn/products/{id}");
        } else if (!m.group(1).equals(String.valueOf(item.tronGoiId()))) {
            warnings.add("Link web trỏ tới sản phẩm " + m.group(1) + ", khác gói " + item.tronGoiId());
        }
    }

    private static Map<String, Object> auditData(Item item, String origin, String externalId, String objectKey) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("origin", origin);
        data.put("externalId", externalId);
        data.put("tronGoiId", item.tronGoiId());
        data.put("tronGoiIdHcm", item.tronGoiIdHcm());
        if (objectKey != null) {
            data.put("objectKey", objectKey);
        }
        return data;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
