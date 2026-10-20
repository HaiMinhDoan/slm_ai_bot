package vn.slmglobal.slmai.services.conversation;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Cấu hình hội thoại, khối {@code app.conversation:} trong application.yml (mục 19).
 *
 * @param debounce          thời gian gộp tin: mỗi tin mới của khách lùi hạn trả lời thêm khoảng này
 * @param autoHandback      quá khoảng này kể từ tin cuối của nhân viên thì tự trả hội thoại lại cho AI
 * @param messagingWindow   cửa sổ nhắn tự do sau tin cuối của khách (Messenger: 24 giờ)
 * @param workerEnabled     bật worker xử lý sự kiện webhook; tắt trong test để gọi tay
 * @param workerBatchSize   số sự kiện tối đa mỗi lần quét
 */
@ConfigurationProperties(prefix = "app.conversation")
public record ConversationProperties(
        Duration debounce,
        Duration autoHandback,
        Duration messagingWindow,
        boolean workerEnabled,
        int workerBatchSize
) {
}
