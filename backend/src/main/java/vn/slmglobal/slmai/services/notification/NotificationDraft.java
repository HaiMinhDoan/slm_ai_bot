package vn.slmglobal.slmai.services.notification;

import lombok.Builder;

import java.util.Map;
import java.util.UUID;

/**
 * Thông báo cần ghi.
 *
 * @param dedupeKey   khoá chống trùng (thong_bao.khoa_chong_trung), ví dụ {@code AI_DRAFT:<tin_nhan_id>}
 * @param body        nội dung ngắn, hiện cả ở thông báo trình duyệt: không đưa số điện thoại vào đây
 * @param data        chi tiết cho trang admin (lý do, nháp AI, số điện thoại...), chỉ trả qua REST
 * @param recipientId người nhận; null = mọi STAFF và SYSTEM_ADMIN
 */
@Builder
public record NotificationDraft(NotificationType type, NotificationSeverity severity, String title, String body,
                                Map<String, Object> data, UUID recipientId, UUID conversationId, UUID customerId,
                                String dedupeKey) {
}
