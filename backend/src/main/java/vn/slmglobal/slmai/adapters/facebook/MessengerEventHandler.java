package vn.slmglobal.slmai.adapters.facebook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.slmglobal.slmai.services.conversation.ChannelEventHandler;
import vn.slmglobal.slmai.services.conversation.ConversationService;
import vn.slmglobal.slmai.services.conversation.EventOutcome;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.conversation.entity.WebhookEvent;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Xử lý một sự kiện "messaging" của Messenger (mục 54.3):
 * <ul>
 *   <li>tin của khách, quick reply, postback → ghi tin IN, đặt hạn gộp tin;</li>
 *   <li>echo do chính ứng dụng gửi (cùng app_id) → bỏ qua, tin OUT đã được ghi lúc gửi;</li>
 *   <li>echo khác (nhân viên trả lời trong Page Inbox / Meta Business Suite) → hội thoại chuyển HUMAN_OWNED;</li>
 *   <li>delivery, read, referral... → bỏ qua (vẫn giữ sự kiện thô để tra cứu).</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class MessengerEventHandler implements ChannelEventHandler {

    private final FacebookProperties properties;
    private final ConversationService conversationService;

    @Override
    public String channel() {
        return ChannelCode.MESSENGER.name();
    }

    @Override
    public EventOutcome handle(WebhookEvent event) {
        String pageId = event.getAccountId();
        if (!properties.isOwnPage(pageId)) {
            return EventOutcome.ignored("Sự kiện của Page khác: " + pageId);
        }
        JsonNode ev = event.getPayload();
        OffsetDateTime at = timestamp(ev);
        JsonNode message = ev.path("message");

        if (message.isObject()) {
            if (message.path("is_echo").asBoolean(false)) {
                return handleEcho(ev, message, pageId, at);
            }
            ConversationService.Recorded r = conversationService.recordInbound(new ConversationService.InboundMessage(
                    ChannelCode.MESSENGER, pageId, ev.path("sender").path("id").asText(),
                    textOrNull(message.path("mid")), textOrNull(message.path("text")),
                    inboundAttachments(message), at));
            return EventOutcome.processed(r.duplicate() ? "Tin trùng" : null);
        }
        if (ev.has("postback")) {
            JsonNode postback = ev.path("postback");
            ArrayNode attachments = JsonNodeFactory.instance.arrayNode();
            attachments.addObject().put("type", "postback").put("payload", postback.path("payload").asText(""));
            ConversationService.Recorded r = conversationService.recordInbound(new ConversationService.InboundMessage(
                    ChannelCode.MESSENGER, pageId, ev.path("sender").path("id").asText(),
                    textOrNull(postback.path("mid")), textOrNull(postback.path("title")), attachments, at));
            return EventOutcome.processed(r.duplicate() ? "Postback trùng" : null);
        }
        if (ev.has("delivery") || ev.has("read")) {
            return EventOutcome.ignored("Báo đã nhận / đã xem");
        }
        if (ev.has("referral")) {
            return EventOutcome.ignored("Referral (m.me?ref=): chưa xử lý ở Phase 1");
        }
        return EventOutcome.ignored("Loại sự kiện Messenger chưa hỗ trợ");
    }

    private EventOutcome handleEcho(JsonNode ev, JsonNode message, String pageId, OffsetDateTime at) {
        if (!properties.hasAppId()) {
            // Không phân biệt được echo của mình với nhân viên: thà bỏ qua còn hơn tự chuyển hội thoại sang người
            return EventOutcome.ignored("Chưa cấu hình FB_APP_ID nên không phân biệt được echo");
        }
        if (properties.appId().equals(message.path("app_id").asText())) {
            return EventOutcome.ignored("Echo của tin do ứng dụng gửi");
        }
        // Echo: sender là Page, recipient là khách
        ConversationService.Recorded r = conversationService.recordStaffEcho(new ConversationService.StaffEcho(
                ChannelCode.MESSENGER, pageId, ev.path("recipient").path("id").asText(),
                textOrNull(message.path("mid")), textOrNull(message.path("text")),
                message.path("attachments").isArray() ? message.path("attachments") : null, at));
        return EventOutcome.processed(r.duplicate() ? "Echo trùng" : "Nhân viên trả lời ngoài admin: chuyển HUMAN_OWNED");
    }

    /** Ảnh / file của khách giữ nguyên; quick reply thêm vào cuối để worker biết khách bấm nút nào. */
    private static JsonNode inboundAttachments(JsonNode message) {
        ArrayNode out = JsonNodeFactory.instance.arrayNode();
        if (message.path("attachments").isArray()) {
            out.addAll((ArrayNode) message.path("attachments"));
        }
        if (message.path("quick_reply").isObject()) {
            out.addObject().put("type", "quick_reply").put("payload", message.path("quick_reply").path("payload").asText(""));
        }
        return out;
    }

    private static OffsetDateTime timestamp(JsonNode ev) {
        long millis = ev.path("timestamp").asLong(0);
        return millis > 0 ? Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC) : OffsetDateTime.now(ZoneOffset.UTC);
    }

    private static String textOrNull(JsonNode node) {
        return node.isTextual() && !node.asText().isEmpty() ? node.asText() : null;
    }
}
