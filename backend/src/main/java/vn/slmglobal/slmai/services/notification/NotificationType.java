package vn.slmglobal.slmai.services.notification;

/** Loại thông báo cho nhân viên (thong_bao.loai). */
public enum NotificationType {
    /** AI chuyển hội thoại cho nhân viên */
    AI_HANDOFF,
    /** Nháp AI chờ nhân viên duyệt */
    AI_DRAFT,
    /** Khách nhắn lại sau khi hội thoại tự trả về AI: lượt đầu là nháp, báo nhân viên trước */
    HANDBACK_REPLY,
    /** Khách vừa thành HOT */
    LEAD_HOT,
    /** Gửi tin AI / tin hệ thống ra kênh thất bại */
    SEND_FAILED,
    /** Số điện thoại khách vừa gửi đã thuộc khách khác: cần gộp khách */
    DUPLICATE_PHONE
}
