package vn.slmglobal.slmai.adapters.facebook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.slmglobal.slmai.common.util.HashUtil;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.conversation.repository.WebhookEventRepository;

/**
 * Tách một lô webhook của Page thành từng sự kiện và ghi vào su_kien_webhook (chống trùng theo ma_su_kien).
 * Chỉ ghi, không xử lý: worker xử lý bất đồng bộ để webhook trả 200 ngay (mục 54.3).
 */
@Service
@RequiredArgsConstructor
public class FacebookWebhookIngestService {

    private final WebhookEventRepository webhookEventRepository;
    private final ObjectMapper objectMapper;

    /** @return số sự kiện mới được ghi (không tính sự kiện trùng) */
    @Transactional
    public int ingest(JsonNode root) {
        if (!"page".equals(root.path("object").asText())) {
            return 0;
        }
        int stored = 0;
        for (JsonNode entry : root.path("entry")) {
            String pageId = entry.path("id").asText("");
            for (JsonNode event : entry.path("messaging")) {
                stored += store(ChannelCode.MESSENGER.name(), pageId, messagingKey(event), event);
            }
            // Bình luận trên Page (field "feed"): ghi lại trước, xử lý ở bước làm bình luận (mục 54.3)
            for (JsonNode change : entry.path("changes")) {
                if ("feed".equals(change.path("field").asText())) {
                    stored += store(ChannelCode.FACEBOOK_COMMENT.name(), pageId, "feed:" + digest(change), change);
                }
            }
        }
        return stored;
    }

    /** Khóa chống trùng: mid của tin nhắn (cả echo), mid của postback; sự kiện khác dùng băm nội dung. */
    static String messagingKey(JsonNode event) {
        String mid = event.path("message").path("mid").asText("");
        if (!mid.isEmpty()) {
            return "msg:" + mid;
        }
        String postbackMid = event.path("postback").path("mid").asText("");
        if (!postbackMid.isEmpty()) {
            return "postback:" + postbackMid;
        }
        String type = event.has("postback") ? "postback"
                : event.has("delivery") ? "delivery"
                : event.has("read") ? "read"
                : event.has("referral") ? "referral"
                : "event";
        return type + ":" + digest(event);
    }

    private int store(String channel, String accountId, String eventKey, JsonNode event) {
        try {
            return webhookEventRepository.insertIfAbsent(channel, accountId, eventKey,
                    objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Không ghi được sự kiện webhook", e);
        }
    }

    private static String digest(JsonNode node) {
        return HashUtil.sha256Hex(node.toString());
    }
}
