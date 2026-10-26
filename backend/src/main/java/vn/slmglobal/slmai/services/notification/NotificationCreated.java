package vn.slmglobal.slmai.services.notification;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Sự kiện phát khi một thông báo mới được ghi; NotificationSseHub đẩy đi sau khi transaction commit.
 * Chỉ mang thông tin tối thiểu: số điện thoại, nội dung nháp lấy qua REST.
 */
public record NotificationCreated(long id, NotificationType type, NotificationSeverity severity, String title,
                                  String body, UUID recipientId, UUID conversationId, OffsetDateTime createdAt) {
}
