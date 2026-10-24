package vn.slmglobal.slmai.services.ai;

import vn.slmglobal.slmai.services.conversation.ChannelSender;
import vn.slmglobal.slmai.services.customer.entity.ChannelIdentity;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Giữ "đang soạn tin" trong lúc chờ Claude: Messenger tự tắt sau khoảng 20 giây, nên bật lại theo chu kỳ.
 * {@link #stop()} chờ lần bật đang chạy (nếu có) xong mới trả về, để không có typing_on nào tới sau tin trả lời.
 */
final class TypingIndicator {

    /** Ngắn hơn 20 giây Messenger tự tắt */
    static final Duration REFRESH = Duration.ofSeconds(10);

    private final ChannelSender sender;
    private final ChannelIdentity recipient;
    private final ScheduledFuture<?> refresher;
    private boolean stopped;

    private TypingIndicator(ChannelSender sender, ChannelIdentity recipient, ScheduledExecutorService scheduler) {
        this.sender = sender;
        this.recipient = recipient;
        sender.markSeen(recipient);
        this.refresher = scheduler.scheduleAtFixedRate(this::refresh, 0, REFRESH.toMillis(), TimeUnit.MILLISECONDS);
    }

    static TypingIndicator start(ChannelSender sender, ChannelIdentity recipient, ScheduledExecutorService scheduler) {
        return new TypingIndicator(sender, recipient, scheduler);
    }

    private synchronized void refresh() {
        if (!stopped) {
            sender.typing(recipient);
        }
    }

    /** Dừng bật lại; gọi trước khi gửi tin trả lời. */
    synchronized void stop() {
        stopped = true;
        refresher.cancel(false);
    }
}
