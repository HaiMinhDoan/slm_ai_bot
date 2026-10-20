package vn.slmglobal.slmai.services.conversation;

import vn.slmglobal.slmai.services.conversation.entity.WebhookEventStatus;

/** Kết quả xử lý một sự kiện; {@code note} lưu vào su_kien_webhook.thong_bao_loi để tra cứu. */
public record EventOutcome(WebhookEventStatus status, String note) {

    public static EventOutcome processed(String note) {
        return new EventOutcome(WebhookEventStatus.PROCESSED, note);
    }

    public static EventOutcome ignored(String note) {
        return new EventOutcome(WebhookEventStatus.IGNORED, note);
    }
}
