package vn.slmglobal.slmai.services.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Thông báo cho nhân viên: ghi trong cùng transaction với nghiệp vụ (lượt AI, lead...), đẩy qua SSE sau khi commit
 * ({@link NotificationSseHub}). Transaction rollback thì không có thông báo nào được ghi hay đẩy đi.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;
    private final ApplicationEventPublisher events;
    private final NotificationProperties properties;

    /** Ghi thông báo (trùng khoá thì bỏ qua). Bắt buộc gọi trong transaction của nghiệp vụ. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void notify(NotificationDraft draft) {
        repository.insertIfAbsent(draft).ifPresent(row -> {
            log.info("Thông báo {} #{}: {}", row.type(), row.id(), row.title());
            events.publishEvent(new NotificationCreated(row.id(), row.type(), row.severity(), row.title(),
                    row.body(), row.recipientId(), row.conversationId(), row.createdAt()));
        });
    }

    @Transactional(readOnly = true)
    public List<NotificationRepository.Row> list(UUID userId, boolean unreadOnly, NotificationType type,
                                                 int page, int size) {
        return repository.list(userId, since(), unreadOnly, type, size, page * size);
    }

    @Transactional(readOnly = true)
    public long count(UUID userId, boolean unreadOnly, NotificationType type) {
        return repository.count(userId, since(), unreadOnly, type);
    }

    @Transactional(readOnly = true)
    public Optional<NotificationRepository.Row> get(UUID userId, long id) {
        return repository.findVisible(userId, since(), id);
    }

    @Transactional
    public boolean markRead(UUID userId, long id) {
        return repository.markRead(userId, since(), id);
    }

    @Transactional
    public int markAllRead(UUID userId, long maxId) {
        return repository.markAllRead(userId, since(), maxId);
    }

    private OffsetDateTime since() {
        return OffsetDateTime.now().minus(properties.window());
    }
}
