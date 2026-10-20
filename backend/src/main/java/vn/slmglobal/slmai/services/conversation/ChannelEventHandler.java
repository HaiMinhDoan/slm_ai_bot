package vn.slmglobal.slmai.services.conversation;

import vn.slmglobal.slmai.services.conversation.entity.WebhookEvent;

/**
 * Xử lý sự kiện webhook của một kênh. Mỗi adapter (Facebook, Zalo, TikTok) cài một bean.
 * Chạy trong transaction của {@link WebhookEventProcessor}: ném lỗi thì mọi thay đổi bị rollback
 * và sự kiện chuyển FAILED.
 */
public interface ChannelEventHandler {

    /** Giá trị cột su_kien_webhook.kenh mà handler này nhận, ví dụ MESSENGER */
    String channel();

    EventOutcome handle(WebhookEvent event);
}
