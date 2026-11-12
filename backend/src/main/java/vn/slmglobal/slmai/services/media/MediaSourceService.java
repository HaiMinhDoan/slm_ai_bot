package vn.slmglobal.slmai.services.media;

import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.common.security.AuthInfo;
import vn.slmglobal.slmai.common.storage.StorageService;
import vn.slmglobal.slmai.services.auth.AuditLogService;
import vn.slmglobal.slmai.services.media.MediaAdminService.MediaResponse;
import vn.slmglobal.slmai.services.media.entity.AvailabilityStatus;
import vn.slmglobal.slmai.services.media.entity.MediaItem;
import vn.slmglobal.slmai.services.media.entity.MediaSource;
import vn.slmglobal.slmai.services.media.entity.MediaStatus;
import vn.slmglobal.slmai.services.media.entity.MediaType;
import vn.slmglobal.slmai.services.media.entity.ProcessingStatus;
import vn.slmglobal.slmai.services.media.entity.RightsStatus;
import vn.slmglobal.slmai.services.media.entity.SourceType;
import vn.slmglobal.slmai.services.media.repository.MediaItemRepository;
import vn.slmglobal.slmai.services.media.repository.MediaSourceRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Thêm media mới và sửa nguồn của media trên trang admin (mục 6.3 A, B; 2026-11-12).
 *
 * <ul>
 *   <li>File tải lên đi qua backend (MinIO nội bộ không cho trình duyệt tải thẳng): nhận diện định dạng thật theo
 *       magic bytes, tính sha256 để chống trùng, lưu theo nội dung {@code media/images|videos/<2 ký tự>/<sha256>}.
 *       Video không giải mã được trên server (chưa có ffmpeg): trình duyệt đọc thời lượng, kích thước và cắt một
 *       khung hình làm ảnh thu nhỏ rồi gửi kèm.</li>
 *   <li>Link YouTube / Facebook / TikTok lưu là nguồn ngoài, quyền mặc định LINK_ONLY (chỉ gửi link, không tải về
 *       đăng lại); link ảnh Google Drive thì tải ảnh về như lúc nhập combo.</li>
 *   <li>Xoá nguồn chỉ xoá dòng trong CSDL, file trong MinIO giữ nguyên (nhật ký ghi lại đường dẫn) như xoá mềm
 *       media; mỗi media giữ ít nhất một nguồn.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaSourceService {

    public static final long MAX_IMAGE_BYTES = DriveImageFetcher.MAX_BYTES;
    /** Khớp spring.servlet.multipart.max-file-size; Messenger chỉ gửi kèm video khoảng 25 MB (mục 6.4) */
    public static final long MAX_VIDEO_BYTES = 100L * 1024 * 1024;
    static final long MAX_THUMBNAIL_BYTES = 2L * 1024 * 1024;

    private final StorageService storage;
    private final DriveImageFetcher drive;
    private final LinkPreviewClient previews;
    private final MediaItemRepository items;
    private final MediaSourceRepository sources;
    private final MediaAdminService admin;
    private final AuditLogService audit;
    private final TransactionTemplate tx;

    public enum Outcome { CREATED, EXISTS, FAILED }

    /** Kết quả thêm một file / một link trong hộp "Thêm media"; {@code name} là tên file hoặc link đã dán */
    public record CreateResult(String name, Outcome outcome, UUID mediaId, String note) {
    }

    /** Thông số video trình duyệt đọc được lúc chọn file; ảnh thì server tự đọc */
    public record FileMeta(Integer width, Integer height, Integer durationSeconds) {
    }

    public record LinkRequest(@Size(max = 2000) String url, @Size(max = 300) String title, RightsStatus rights,
                              UUID replaceSourceId) {
    }

    public record SourceUpdate(AvailabilityStatus availabilityStatus,
                               @Size(max = 200, message = "Tên tác giả tối đa 200 ký tự") String authorName) {
    }

    /** File đã kiểm tra và lưu vào MinIO, chưa ghi CSDL */
    private record Stored(MediaFiles.Format format, String sha256, String objectKey, long size, Integer width,
                          Integer height, Integer durationSeconds, String thumbnailKey) {
    }

    // ------------------------------------------------------------------ thêm media mới

    public CreateResult createFromFile(AuthInfo actor, MultipartFile file, MultipartFile thumbnail, FileMeta meta,
                                       RightsStatus rights) {
        String name = file == null || file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        try {
            requireStorage();
            Inspected in = inspect(file);
            Optional<MediaSource> same = sources.findFirstBySha256(in.sha256());
            if (same.isPresent()) {
                return new CreateResult(name, Outcome.EXISTS, same.get().getMediaId(), existsNote(same.get()));
            }
            Stored s = store(in, file, thumbnail, meta);
            UUID id = tx.execute(st -> {
                MediaItem m = items.saveAndFlush(MediaItem.builder()
                        .type(s.format().mediaType)
                        .title(titleFromFileName(name))
                        .status(MediaStatus.DRAFT)
                        .rights(rights == null ? RightsStatus.OWNED : rights)
                        .createdBy(actor.email())
                        .build());
                MediaSource src = sources.saveAndFlush(uploadSource(m.getId(), s, null, null));
                audit.record(actor.id(), "media.create", MediaAdminService.TARGET, m.getId(), null,
                        sourceAudit("UPLOAD", name, src));
                return m.getId();
            });
            return new CreateResult(name, Outcome.CREATED, id, describe(s));
        } catch (CommonException e) {
            return new CreateResult(name, Outcome.FAILED, null, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return new CreateResult(name, Outcome.EXISTS, null, "File vừa được thêm ở lần tải khác");
        }
    }

    public CreateResult createFromLink(AuthInfo actor, LinkRequest r) {
        String url = r.url() == null ? "" : r.url().strip();
        try {
            MediaLinkParser.Parsed link = parse(url);
            Optional<MediaSource> same = sources.findFirstBySourceTypeAndExternalId(
                    link.drive() ? SourceType.UPLOAD : link.type(), link.externalId());
            if (same.isPresent()) {
                return new CreateResult(url, Outcome.EXISTS, same.get().getMediaId(), existsNote(same.get()));
            }
            if (link.drive()) {
                return createFromDrive(actor, url, link, r);
            }
            Optional<LinkPreviewClient.Preview> preview = previews.preview(link);
            String title = firstNonBlank(r.title(), preview.map(LinkPreviewClient.Preview::title).orElse(null),
                    "Video " + platform(link.type()));
            UUID id = tx.execute(st -> {
                MediaItem m = items.saveAndFlush(MediaItem.builder()
                        .type(MediaType.VIDEO)
                        .title(truncate(title, 300))
                        .status(MediaStatus.DRAFT)
                        .rights(r.rights() == null ? RightsStatus.LINK_ONLY : r.rights())
                        .createdBy(actor.email())
                        .build());
                MediaSource src = sources.saveAndFlush(linkSource(m.getId(), link, preview.orElse(null)));
                audit.record(actor.id(), "media.create", MediaAdminService.TARGET, m.getId(), null,
                        sourceAudit(link.type().name(), url, src));
                return m.getId();
            });
            return new CreateResult(url, Outcome.CREATED, id, platform(link.type())
                    + (preview.isPresent() ? ", đã lấy tiêu đề" : ""));
        } catch (CommonException e) {
            return new CreateResult(url, Outcome.FAILED, null, e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return new CreateResult(url, Outcome.EXISTS, null, "Link vừa được thêm ở lần khác");
        }
    }

    private CreateResult createFromDrive(AuthInfo actor, String url, MediaLinkParser.Parsed link, LinkRequest r) {
        requireStorage();
        DriveImageFetcher.Image img = fetchDrive(link);
        Optional<MediaSource> same = sources.findFirstBySha256(img.sha256());
        if (same.isPresent()) {
            return new CreateResult(url, Outcome.EXISTS, same.get().getMediaId(), existsNote(same.get()));
        }
        Stored s = storeDrive(img);
        UUID id = tx.execute(st -> {
            MediaItem m = items.saveAndFlush(MediaItem.builder()
                    .type(MediaType.IMAGE)
                    .title(truncate(firstNonBlank(r.title(), null, "Ảnh từ Google Drive"), 300))
                    .status(MediaStatus.DRAFT)
                    .rights(r.rights() == null ? RightsStatus.OWNED : r.rights())
                    .createdBy(actor.email())
                    .build());
            MediaSource src = sources.saveAndFlush(uploadSource(m.getId(), s, link.url(), link.externalId()));
            audit.record(actor.id(), "media.create", MediaAdminService.TARGET, m.getId(), null,
                    sourceAudit("GOOGLE_DRIVE", url, src));
            return m.getId();
        });
        return new CreateResult(url, Outcome.CREATED, id, "Ảnh Google Drive, " + describe(s));
    }

    // ------------------------------------------------------------------ sửa nguồn của media có sẵn

    /** Thêm file vào media; có {@code replaceSourceId} thì bỏ nguồn đó (thay file) */
    public MediaResponse addFile(AuthInfo actor, UUID mediaId, MultipartFile file, MultipartFile thumbnail,
                                 FileMeta meta, UUID replaceSourceId) {
        requireStorage();
        MediaItem m = editable(mediaId);
        Inspected in = inspect(file);
        if (in.format().mediaType != m.getType()) {
            throw new CommonException(HttpStatus.BAD_REQUEST, m.getType() == MediaType.IMAGE
                    ? "Media này là ảnh, không thêm được file video" : "Media này là video, không thêm được file ảnh");
        }
        sources.findFirstBySha256(in.sha256()).ifPresent(same -> {
            throw new CommonException(HttpStatus.CONFLICT, "File này đã có: " + existsNote(same));
        });
        Stored s = store(in, file, thumbnail, meta);
        String name = file.getOriginalFilename();
        return saveSource(actor, m, uploadSource(mediaId, s, null, null), replaceSourceId, "UPLOAD", name);
    }

    /** Thêm link vào media; có {@code replaceSourceId} thì bỏ nguồn đó (sửa link) */
    public MediaResponse addLink(AuthInfo actor, UUID mediaId, LinkRequest r) {
        String url = r.url() == null ? "" : r.url().strip();
        MediaItem m = editable(mediaId);
        MediaLinkParser.Parsed link = parse(url);
        Optional<MediaSource> same = sources.findFirstBySourceTypeAndExternalId(
                link.drive() ? SourceType.UPLOAD : link.type(), link.externalId());
        if (same.isPresent()) {
            throw new CommonException(HttpStatus.CONFLICT, "Link này đã có: " + existsNote(same.get()));
        }
        if (link.drive()) {
            if (m.getType() != MediaType.IMAGE) {
                throw new CommonException(HttpStatus.BAD_REQUEST, "Link Drive là ảnh, media này là video");
            }
            requireStorage();
            DriveImageFetcher.Image img = fetchDrive(link);
            sources.findFirstBySha256(img.sha256()).ifPresent(dup -> {
                throw new CommonException(HttpStatus.CONFLICT, "Ảnh này đã có: " + existsNote(dup));
            });
            Stored s = storeDrive(img);
            return saveSource(actor, m, uploadSource(mediaId, s, link.url(), link.externalId()), r.replaceSourceId(),
                    "GOOGLE_DRIVE", url);
        }
        if (m.getType() != MediaType.VIDEO) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Media này là ảnh, link " + platform(link.type())
                    + " là video");
        }
        return saveSource(actor, m, linkSource(mediaId, link, previews.preview(link).orElse(null)),
                r.replaceSourceId(), link.type().name(), url);
    }

    public MediaResponse updateSource(AuthInfo actor, UUID mediaId, UUID sourceId, SourceUpdate r) {
        tx.executeWithoutResult(st -> {
            editable(mediaId);
            MediaSource s = source(mediaId, sourceId);
            Map<String, Object> before = Map.of("availability", s.getAvailabilityStatus().name(),
                    "author", String.valueOf(s.getAuthorName()));
            if (r.availabilityStatus() != null) {
                s.setAvailabilityStatus(r.availabilityStatus());
                s.setLastCheckedAt(OffsetDateTime.now());
            }
            s.setAuthorName(MediaAdminService.blankToNull(r.authorName()));
            sources.saveAndFlush(s);
            audit.record(actor.id(), "media.source.update", MediaAdminService.TARGET, mediaId, before,
                    Map.of("availability", s.getAvailabilityStatus().name(), "author", String.valueOf(s.getAuthorName())));
        });
        return admin.get(mediaId);
    }

    public MediaResponse deleteSource(AuthInfo actor, UUID mediaId, UUID sourceId) {
        tx.executeWithoutResult(st -> {
            editable(mediaId);
            MediaSource s = source(mediaId, sourceId);
            if (sources.findByMediaIdIn(List.of(mediaId)).size() <= 1) {
                throw new CommonException(HttpStatus.BAD_REQUEST,
                        "Media phải còn ít nhất một nguồn; muốn bỏ hẳn thì xoá media");
            }
            sources.delete(s);
            sources.flush();
            audit.record(actor.id(), "media.source.delete", MediaAdminService.TARGET, mediaId,
                    sourceAudit(s.getSourceType().name(), s.getSourceUrl(), s), null);
        });
        return admin.get(mediaId);
    }

    /** Ảnh thu nhỏ của video tải lên */
    public Optional<MediaSource> thumbnailSource(UUID mediaId, UUID sourceId) {
        return sources.findByIdAndMediaId(sourceId, mediaId).filter(s -> s.getThumbnailKey() != null);
    }

    // ------------------------------------------------------------------ tiện ích

    private MediaResponse saveSource(AuthInfo actor, MediaItem m, MediaSource src, UUID replaceSourceId,
                                     String origin, String name) {
        try {
            tx.executeWithoutResult(st -> {
                MediaSource old = replaceSourceId == null ? null : source(m.getId(), replaceSourceId);
                if (old != null) {
                    sources.delete(old);
                    sources.flush();
                }
                MediaSource saved = sources.saveAndFlush(src);
                audit.record(actor.id(), old == null ? "media.source.add" : "media.source.replace",
                        MediaAdminService.TARGET, m.getId(),
                        old == null ? null : sourceAudit(old.getSourceType().name(), old.getSourceUrl(), old),
                        sourceAudit(origin, name, saved));
            });
        } catch (DataIntegrityViolationException e) {
            throw new CommonException(HttpStatus.CONFLICT, "Nguồn này vừa được thêm ở lần khác");
        }
        return admin.get(m.getId());
    }

    private record Inspected(MediaFiles.Format format, String sha256, long size) {
    }

    private Inspected inspect(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Chưa chọn file hoặc file rỗng");
        }
        try {
            byte[] head;
            try (InputStream in = file.getInputStream()) {
                head = in.readNBytes(MediaFiles.HEAD_BYTES);
            }
            MediaFiles.Format format = MediaFiles.sniff(head).orElseThrow(() -> new CommonException(
                    HttpStatus.BAD_REQUEST, "Chỉ nhận ảnh JPEG, PNG, WebP hoặc video MP4, MOV, WebM"));
            long max = format.mediaType == MediaType.IMAGE ? MAX_IMAGE_BYTES : MAX_VIDEO_BYTES;
            if (file.getSize() > max) {
                throw new CommonException(HttpStatus.BAD_REQUEST, (format.mediaType == MediaType.IMAGE ? "Ảnh" : "Video")
                        + " lớn hơn " + (max / 1024 / 1024) + " MB");
            }
            String sha;
            try (InputStream in = file.getInputStream()) {
                sha = MediaFiles.sha256(in);
            }
            return new Inspected(format, sha, file.getSize());
        } catch (IOException e) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Không đọc được file: " + e.getMessage());
        }
    }

    private Stored store(Inspected in, MultipartFile file, MultipartFile thumbnail, FileMeta meta) {
        boolean image = in.format().mediaType == MediaType.IMAGE;
        String key = "media/" + (image ? "images" : "videos") + "/" + in.sha256().substring(0, 2) + "/" + in.sha256()
                + "." + in.format().extension;
        Integer width = null;
        Integer height = null;
        try {
            if (image) {
                int[] size = MediaFiles.dimensions(file.getBytes());
                width = size == null ? null : size[0];
                height = size == null ? null : size[1];
            } else if (meta != null) {
                width = positive(meta.width());
                height = positive(meta.height());
            }
            try (InputStream body = file.getInputStream()) {
                storage.put(key, body, in.size(), in.format().mimeType);
            }
        } catch (IOException e) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Không đọc được file: " + e.getMessage());
        } catch (Exception e) {
            throw new CommonException(HttpStatus.SERVICE_UNAVAILABLE, "Không lưu được vào MinIO: " + e.getMessage());
        }
        String thumbKey = image ? null : storeThumbnail(in.sha256(), thumbnail);
        return new Stored(in.format(), in.sha256(), key, in.size(), width, height,
                image || meta == null ? null : positive(meta.durationSeconds()), thumbKey);
    }

    /** Ảnh thu nhỏ trình duyệt cắt từ video; sai định dạng hay lỗi lưu thì bỏ qua, video vẫn được thêm */
    private String storeThumbnail(String videoSha, MultipartFile thumbnail) {
        if (thumbnail == null || thumbnail.isEmpty() || thumbnail.getSize() > MAX_THUMBNAIL_BYTES) {
            return null;
        }
        try {
            byte[] bytes = thumbnail.getBytes();
            Optional<MediaFiles.Format> f = MediaFiles.sniff(bytes).filter(x -> x.mediaType == MediaType.IMAGE);
            if (f.isEmpty()) {
                return null;
            }
            String key = "media/thumbs/" + videoSha.substring(0, 2) + "/" + videoSha + "." + f.get().extension;
            storage.put(key, new ByteArrayInputStream(bytes), bytes.length, f.get().mimeType);
            return key;
        } catch (Exception e) {
            log.warn("Không lưu được ảnh thu nhỏ của video {}: {}", videoSha, e.getMessage());
            return null;
        }
    }

    private DriveImageFetcher.Image fetchDrive(MediaLinkParser.Parsed link) {
        try {
            return drive.fetch(link.driveFileId());
        } catch (DriveImageFetcher.DriveFetchException e) {
            throw new CommonException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private Stored storeDrive(DriveImageFetcher.Image img) {
        String key = "media/images/" + img.sha256().substring(0, 2) + "/" + img.sha256() + "." + img.extension();
        try {
            storage.put(key, new ByteArrayInputStream(img.bytes()), img.bytes().length, img.mimeType());
        } catch (Exception e) {
            throw new CommonException(HttpStatus.SERVICE_UNAVAILABLE, "Không lưu được vào MinIO: " + e.getMessage());
        }
        MediaFiles.Format format = MediaFiles.sniff(img.bytes()).orElse(MediaFiles.Format.JPEG);
        return new Stored(format, img.sha256(), key, img.bytes().length, img.width(), img.height(), null, null);
    }

    private MediaSource uploadSource(UUID mediaId, Stored s, String sourceUrl, String externalId) {
        return MediaSource.builder()
                .mediaId(mediaId)
                .sourceType(SourceType.UPLOAD)
                .sourceUrl(sourceUrl)
                .externalId(externalId)
                .storageBucket(storage.bucket())
                .objectKey(s.objectKey())
                .mimeType(s.format().mimeType)
                .sizeBytes(s.size())
                .sha256(s.sha256())
                .width(s.width())
                .height(s.height())
                .durationSeconds(s.durationSeconds())
                .thumbnailKey(s.thumbnailKey())
                .processingStatus(ProcessingStatus.READY)
                .availabilityStatus(AvailabilityStatus.ACTIVE)
                .lastCheckedAt(OffsetDateTime.now())
                .build();
    }

    private static MediaSource linkSource(UUID mediaId, MediaLinkParser.Parsed link,
                                          LinkPreviewClient.Preview preview) {
        return MediaSource.builder()
                .mediaId(mediaId)
                .sourceType(link.type())
                .sourceUrl(link.url())
                .externalId(link.externalId())
                .authorName(preview == null ? null : preview.authorName())
                .thumbnailUrl(preview == null ? null : preview.thumbnailUrl())
                .processingStatus(ProcessingStatus.READY)
                // oEmbed trả về được nghĩa là video còn xem công khai
                .availabilityStatus(preview == null ? AvailabilityStatus.UNKNOWN : AvailabilityStatus.ACTIVE)
                .lastCheckedAt(preview == null ? null : OffsetDateTime.now())
                .build();
    }

    private static MediaLinkParser.Parsed parse(String url) {
        return MediaLinkParser.parse(url).orElseThrow(() -> new CommonException(HttpStatus.BAD_REQUEST,
                "Không nhận ra link: chỉ nhận video YouTube, Facebook, TikTok hoặc ảnh Google Drive"));
    }

    private MediaItem editable(UUID mediaId) {
        MediaItem m = admin.find(mediaId);
        if (m.isDeleted()) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Media đã xoá, khôi phục trước khi sửa");
        }
        return m;
    }

    private MediaSource source(UUID mediaId, UUID sourceId) {
        return sources.findByIdAndMediaId(sourceId, mediaId)
                .orElseThrow(() -> CommonException.notFound("Không tìm thấy nguồn media"));
    }

    private void requireStorage() {
        if (!storage.isAvailable()) {
            throw new CommonException(HttpStatus.SERVICE_UNAVAILABLE,
                    "MinIO chưa sẵn sàng: kiểm tra MINIO_ENDPOINT, MINIO_ACCESS_KEY, MINIO_SECRET_KEY và bucket "
                            + storage.bucket());
        }
    }

    private String existsNote(MediaSource same) {
        return items.findById(same.getMediaId()).map(m -> {
            String name = (m.getIndexCode() == null ? "" : m.getIndexCode() + " ") + "\"" + m.getTitle() + "\"";
            return m.isDeleted() ? "đã có ở media " + name + " nhưng media đó đã bị xoá: khôi phục (lọc Đã xoá) nếu muốn "
                    + "dùng lại" : "đã có ở media " + name;
        }).orElse("đã có trong thư viện");
    }

    private static String describe(Stored s) {
        return s.format().mimeType + ", " + (s.size() >= 1024 * 1024 ? (s.size() / 1024 / 1024) + " MB"
                : (s.size() / 1024) + " KB") + (s.width() == null ? "" : ", " + s.width() + "×" + s.height());
    }

    /** "anh_he_5kWp-mai-ton.jpg" → "anh he 5kWp mai ton" */
    static String titleFromFileName(String fileName) {
        String base = fileName.replaceAll("^.*[/\\\\]", "").replaceAll("\\.[A-Za-z0-9]{1,5}$", "");
        String title = base.replaceAll("[_\\-]+", " ").replaceAll("\\s+", " ").strip();
        return truncate(title.isEmpty() ? "File tải lên" : title, 300);
    }

    private static String platform(SourceType type) {
        return switch (type) {
            case YOUTUBE -> "YouTube";
            case FACEBOOK -> "Facebook";
            case TIKTOK -> "TikTok";
            default -> type.name().toLowerCase(Locale.ROOT);
        };
    }

    private static Map<String, Object> sourceAudit(String origin, String name, MediaSource s) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("origin", origin);
        data.put("name", name);
        data.put("sourceId", s.getId());
        data.put("externalId", s.getExternalId());
        data.put("objectKey", s.getObjectKey());
        data.put("sha256", s.getSha256());
        return data;
    }

    private static Integer positive(Integer v) {
        return v == null || v <= 0 ? null : v;
    }

    private static String firstNonBlank(String a, String b, String fallback) {
        if (a != null && !a.isBlank()) {
            return a.strip();
        }
        return b != null && !b.isBlank() ? b.strip() : fallback;
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
