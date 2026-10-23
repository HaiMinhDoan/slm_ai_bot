package vn.slmglobal.slmai.services.conversation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.slmglobal.slmai.services.conversation.repository.ConversationRepository;

import java.time.OffsetDateTime;

/**
 * Tự trả hội thoại lại cho AI khi đã quá {@code app.conversation.auto-handback} kể từ tin cuối của nhân viên (mục 19).
 * Không tự trả lời ngay: AI chỉ chạy khi khách nhắn tiếp, và lượt đó đi theo DRAFT.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoHandbackJob {

    private final ConversationRepository conversationRepository;
    private final ConversationProperties properties;

    @Scheduled(fixedDelayString = "${app.conversation.handback-check-interval-ms:300000}")
    @Transactional
    public void run() {
        if (!properties.workerEnabled() || properties.autoHandback() == null || properties.autoHandback().isZero()) {
            return;
        }
        int n = conversationRepository.handBackIdleSince(OffsetDateTime.now().minus(properties.autoHandback()));
        if (n > 0) {
            log.info("Tự trả {} hội thoại lại cho AI sau {} không có tin của nhân viên", n, properties.autoHandback());
        }
    }
}
