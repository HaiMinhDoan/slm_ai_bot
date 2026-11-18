package vn.slmglobal.slmai.services.media;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.slmglobal.slmai.common.storage.StorageService;
import vn.slmglobal.slmai.services.catalog.PackageQuoteImageService;
import vn.slmglobal.slmai.services.conversation.ChannelSender;
import vn.slmglobal.slmai.services.customer.entity.ChannelIdentity;
import vn.slmglobal.slmai.services.media.entity.MediaItem;
import vn.slmglobal.slmai.services.media.entity.MediaSource;
import vn.slmglobal.slmai.services.media.entity.MediaStatus;
import vn.slmglobal.slmai.services.media.entity.MediaType;
import vn.slmglobal.slmai.services.media.entity.SourceType;
import vn.slmglobal.slmai.services.media.repository.MediaItemRepository;
import vn.slmglobal.slmai.services.media.repository.MediaSourceRepository;

import java.io.InputStream;
import java.util.UUID;

/**
 * Gửi các media đã chọn của một tin ({@link MediaPlanner}) ra kênh, sau phần chữ (mục 6.4): ảnh (tải lên hoặc ảnh
 * báo giá tự tạo) gửi dạng tệp đính kèm, không gửi link ảnh; video có link Facebook / YouTube / TikTok gửi link thành
 * một tin riêng để kênh hiện thẻ xem trước, video tải lên gửi dạng tệp. Mỗi mục ghi lại trạng thái SENT / FAILED /
 * SKIPPED; gửi lại một tin thì mục đã SENT được bỏ qua.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaDelivery {

    private final MediaItemRepository items;
    private final MediaSourceRepository sources;
    private final StorageService storage;
    private final PackageQuoteImageService quoteImages;
    private final ObjectMapper objectMapper;

    /**
     * @param attachments danh sách đã cập nhật trạng thái từng mục
     * @param sent        số tin đã tới khách
     * @param failed      số mục gửi lỗi (không tính mục bỏ qua)
     */
    public record Result(JsonNode attachments, int sent, int failed) {
    }

    /** Mục không gửi được vì lý do cố định (kênh không hỗ trợ, media đã bỏ duyệt): không coi là lỗi gửi */
    private static final class Skip extends RuntimeException {
        Skip(String message) {
            super(message);
        }
    }

    /**
     * @param pause chạy trước mỗi mục gửi (hiện "đang soạn tin" như người gửi); null là không chờ
     */
    public Result send(ChannelSender sender, ChannelIdentity to, JsonNode attachments, Runnable pause) {
        ArrayNode out = objectMapper.createArrayNode();
        int sent = 0;
        int failed = 0;
        if (attachments == null || !attachments.isArray()) {
            return new Result(out, 0, 0);
        }
        for (JsonNode a : attachments) {
            ObjectNode item = a.isObject() ? ((ObjectNode) a).deepCopy() : objectMapper.createObjectNode();
            out.add(item);
            String kind = item.path(MediaPlanner.KIND).asText("");
            if (!MediaPlanner.KIND_MEDIA.equals(kind) && !MediaPlanner.KIND_QUOTE.equals(kind)
                    || MediaPlanner.STATUS_SENT.equals(item.path(MediaPlanner.STATUS).asText())) {
                continue;
            }
            try {
                if (pause != null) {
                    pause.run();
                }
                String externalId = MediaPlanner.KIND_QUOTE.equals(kind) ? sendQuote(sender, to, item)
                        : sendMedia(sender, to, item);
                item.put(MediaPlanner.STATUS, MediaPlanner.STATUS_SENT);
                item.put("external_id", externalId);
                item.remove("error");
                sent++;
            } catch (Skip e) {
                item.put(MediaPlanner.STATUS, "SKIPPED");
                item.put("error", e.getMessage());
            } catch (RuntimeException e) {
                log.warn("Gửi media {} cho khách lỗi: {}", item.path("title").asText(), e.getMessage());
                item.put(MediaPlanner.STATUS, "FAILED");
                item.put("error", truncate(e.getMessage()));
                failed++;
            }
        }
        return new Result(out, sent, failed);
    }

    private String sendQuote(ChannelSender sender, ChannelIdentity to, ObjectNode item) {
        if (!sender.supportsFiles()) {
            throw new Skip("Kênh chưa gửi được ảnh");
        }
        int packageId = item.path("package_id").asInt();
        PackageQuoteImageService.QuoteImage img = quoteImages.image(packageId)
                .orElseThrow(() -> new Skip("Không tạo được ảnh báo giá gói " + packageId));
        return sender.sendFile(to, new ChannelSender.OutgoingFile(img.sha256(), "image", "image/jpeg",
                "bao-gia-" + packageId + ".jpg", img::bytes));
    }

    private String sendMedia(ChannelSender sender, ChannelIdentity to, ObjectNode item) {
        UUID mediaId = uuid(item.path("media_id").asText());
        MediaItem m = mediaId == null ? null : items.findById(mediaId).orElse(null);
        if (m == null || m.isDeleted() || m.getStatus() != MediaStatus.APPROVED) {
            throw new Skip("Media đã bỏ duyệt hoặc đã xoá");
        }
        UUID sourceId = uuid(item.path("source_id").asText());
        MediaSource src = sourceId == null ? null : sources.findById(sourceId)
                .filter(s -> s.getMediaId().equals(m.getId())).orElse(null);
        if (src == null) {
            throw new Skip("Nguồn của media không còn");
        }
        if (src.getSourceType() != SourceType.UPLOAD) {
            if (src.getSourceUrl() == null || src.getSourceUrl().isBlank()) {
                throw new Skip("Nguồn không có link");
            }
            // Link đứng riêng một tin: kênh hiện thẻ xem trước video
            return sender.sendText(to, src.getSourceUrl());
        }
        if (!sender.supportsFiles()) {
            throw new Skip("Kênh chưa gửi được tệp đính kèm");
        }
        if (src.getObjectKey() == null || (src.getStorageBucket() != null
                && !src.getStorageBucket().equals(storage.bucket()))) {
            throw new Skip("Tệp không nằm trong kho media của bot");
        }
        String kind = m.getType() == MediaType.VIDEO ? "video" : "image";
        String mime = src.getMimeType() != null ? src.getMimeType()
                : kind.equals("video") ? "video/mp4" : "image/jpeg";
        String key = src.getSha256() != null ? src.getSha256() : src.getObjectKey();
        String fileName = src.getObjectKey().substring(src.getObjectKey().lastIndexOf('/') + 1);
        return sender.sendFile(to, new ChannelSender.OutgoingFile(key, kind, mime, fileName,
                () -> read(src.getObjectKey())));
    }

    private byte[] read(String objectKey) {
        try (InputStream in = storage.get(objectKey)) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new IllegalStateException("Không đọc được tệp " + objectKey + " từ MinIO: " + e.getMessage(), e);
        }
    }

    private static UUID uuid(String s) {
        try {
            return s == null || s.isBlank() ? null : UUID.fromString(s);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String truncate(String s) {
        return s == null || s.length() <= 300 ? s : s.substring(0, 300);
    }
}
