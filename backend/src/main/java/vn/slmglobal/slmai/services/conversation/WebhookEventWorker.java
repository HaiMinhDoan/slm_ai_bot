package vn.slmglobal.slmai.services.conversation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.slmglobal.slmai.services.conversation.repository.WebhookEventRepository;

import java.util.List;

/**
 * Quét su_kien_webhook chưa xử lý mỗi giây. Webhook chỉ ghi sự kiện rồi trả 200 ngay;
 * phần nhận diện khách, ghi tin, chuyển người phụ trách chạy ở đây (mục 19).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookEventWorker {

    private final WebhookEventRepository webhookEventRepository;
    private final WebhookEventProcessor processor;
    private final ConversationProperties properties;

    @Scheduled(fixedDelayString = "${app.conversation.worker-poll-interval-ms:1000}")
    public void poll() {
        if (properties.workerEnabled()) {
            drain();
        }
    }

    /** Xử lý một lô; trả số sự kiện đã xử lý. Test gọi trực tiếp hàm này. */
    public int drain() {
        List<Long> ids = webhookEventRepository.findPendingIds(Math.max(1, properties.workerBatchSize()));
        int processed = 0;
        for (Long id : ids) {
            try {
                if (processor.processOne(id)) {
                    processed++;
                }
            } catch (Exception e) {
                log.warn("Xử lý sự kiện webhook {} thất bại: {}", id, e.toString());
                processor.markFailed(id, e.toString());
            }
        }
        return processed;
    }
}
