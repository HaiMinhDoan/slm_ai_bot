package vn.slmglobal.slmai.services.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Cấu hình thông báo nhân viên, khối {@code app.notification:} trong application.yml.
 *
 * @param window                chỉ hiện thông báo trong khoảng này (nhân viên mới không thấy hàng trăm tin cũ)
 * @param heartbeat             chu kỳ gửi ping trên kết nối SSE, đồng thời kiểm tra token đã thu hồi / hết hạn
 * @param streamTimeout         thời gian sống tối đa của một kết nối SSE; frontend tự kết nối lại
 * @param maxConnectionsPerUser số tab / thiết bị tối đa mỗi người; vượt thì đóng kết nối cũ nhất
 */
@ConfigurationProperties(prefix = "app.notification")
public record NotificationProperties(Duration window, Duration heartbeat, Duration streamTimeout,
                                     int maxConnectionsPerUser) {
}
