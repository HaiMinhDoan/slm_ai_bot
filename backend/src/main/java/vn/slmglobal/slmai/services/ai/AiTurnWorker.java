package vn.slmglobal.slmai.services.ai;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.slmglobal.slmai.services.conversation.repository.ConversationRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Quét hội thoại đã gộp tin xong (han_tra_loi_luc tới hạn) và chạy lượt AI (mục 19). Mỗi lượt chạy trên một luồng
 * riêng vì gọi Claude mất vài giây; khoá dòng trong AiTurnStore.claim bảo đảm mỗi hội thoại chỉ một lượt tại một lúc,
 * kể cả khi chạy nhiều instance.
 */
@Slf4j
@Component
public class AiTurnWorker {

    private final ConversationRepository conversationRepository;
    private final AiTurnService turnService;
    private final AiProperties properties;
    private final ExecutorService executor;
    private final int concurrency;
    private final Set<UUID> inFlight = ConcurrentHashMap.newKeySet();

    public AiTurnWorker(ConversationRepository conversationRepository, AiTurnService turnService,
                        AiProperties properties) {
        this.conversationRepository = conversationRepository;
        this.turnService = turnService;
        this.properties = properties;
        this.concurrency = Math.max(1, properties.workerConcurrency());
        AtomicInteger n = new AtomicInteger();
        this.executor = Executors.newFixedThreadPool(concurrency, r -> {
            Thread t = new Thread(r, "ai-turn-" + n.incrementAndGet());
            t.setDaemon(true);
            return t;
        });
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warnIfDisabled() {
        if (properties.workerEnabled() && !properties.hasApiKey()) {
            log.warn("Chưa cấu hình ANTHROPIC_API_KEY: AI không trả lời khách, tin nhắn vẫn được lưu");
        } else if (properties.workerEnabled()) {
            log.info("AI trả lời khách: model {}, effort {}{}", properties.model(), properties.effort(),
                    properties.forceDraft() ? ", công tắc dừng đang BẬT (mọi lượt chỉ lưu nháp)" : "");
        }
    }

    @Scheduled(fixedDelayString = "${app.ai.worker-poll-interval-ms:1000}")
    public void poll() {
        if (!properties.workerEnabled() || !properties.hasApiKey()) {
            return;
        }
        int free = concurrency - inFlight.size();
        if (free <= 0) {
            return;
        }
        List<UUID> due = conversationRepository.findDueIds(free);
        for (UUID id : due) {
            if (!inFlight.add(id)) {
                continue;
            }
            executor.execute(() -> {
                try {
                    turnService.process(id);
                } catch (Exception e) {
                    log.error("Lượt AI của hội thoại {} lỗi", id, e);
                } finally {
                    inFlight.remove(id);
                }
            });
        }
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        executor.shutdown();
        if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
    }
}
