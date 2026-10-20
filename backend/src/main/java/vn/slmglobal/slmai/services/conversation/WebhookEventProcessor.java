package vn.slmglobal.slmai.services.conversation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.slmglobal.slmai.services.conversation.entity.WebhookEvent;
import vn.slmglobal.slmai.services.conversation.entity.WebhookEventStatus;
import vn.slmglobal.slmai.services.conversation.repository.WebhookEventRepository;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Xử lý từng sự kiện webhook trong một transaction riêng (mục 19). */
@Slf4j
@Service
public class WebhookEventProcessor {

    private static final int MAX_NOTE_LENGTH = 2000;

    private final WebhookEventRepository webhookEventRepository;
    private final Map<String, ChannelEventHandler> handlers;

    public WebhookEventProcessor(WebhookEventRepository webhookEventRepository, List<ChannelEventHandler> handlers) {
        this.webhookEventRepository = webhookEventRepository;
        this.handlers = handlers.stream().collect(Collectors.toMap(ChannelEventHandler::channel, Function.identity()));
    }

    /** @return false nếu sự kiện đã được xử lý hoặc đang bị instance khác giữ */
    @Transactional
    public boolean processOne(Long id) {
        WebhookEvent event = webhookEventRepository.lockPending(id).orElse(null);
        if (event == null) {
            return false;
        }
        ChannelEventHandler handler = handlers.get(event.getChannel());
        if (handler == null) {
            event.finish(WebhookEventStatus.IGNORED, "Chưa hỗ trợ kênh " + event.getChannel());
            return true;
        }
        EventOutcome outcome = handler.handle(event);
        event.finish(outcome.status(), truncate(outcome.note()));
        return true;
    }

    /** Transaction riêng: transaction xử lý chính đã rollback nên phải ghi trạng thái lỗi ở ngoài. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long id, String reason) {
        webhookEventRepository.findById(id)
                .filter(e -> e.getStatus() == WebhookEventStatus.RECEIVED)
                .ifPresent(e -> e.finish(WebhookEventStatus.FAILED, truncate(reason)));
    }

    private static String truncate(String value) {
        if (value == null || value.length() <= MAX_NOTE_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_NOTE_LENGTH);
    }
}
