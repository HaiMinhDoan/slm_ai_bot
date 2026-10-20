package vn.slmglobal.slmai.services.conversation.entity;

/** Phải khớp CHECK của cột tin_nhan.trang_thai. */
public enum MessageStatus {
    RECEIVED,
    PROCESSED,
    DRAFT,
    SENT,
    FAILED,
    DISCARDED
}
