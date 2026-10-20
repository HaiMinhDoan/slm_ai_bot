package vn.slmglobal.slmai.services.conversation.entity;

/** Phải khớp CHECK của cột su_kien_webhook.trang_thai. */
public enum WebhookEventStatus {
    RECEIVED,
    PROCESSED,
    IGNORED,
    FAILED
}
