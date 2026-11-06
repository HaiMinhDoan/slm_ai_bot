package vn.slmglobal.slmai.services.conversation;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.slmglobal.slmai.services.settings.AiSettingsService;

/**
 * Quét định kỳ khách chưa có tên kênh để lấy tên (lần đầu 1 phút sau khi khởi động, rồi 10 phút một lần). Chỉ chạy
 * khi worker hội thoại bật, như các job hội thoại khác.
 */
@Component
@RequiredArgsConstructor
public class ChannelNameBackfillJob {

    private final ChannelNameService channelNames;
    private final AiSettingsService settings;

    @Scheduled(initialDelayString = "${app.conversation.name-backfill-initial-delay-ms:60000}",
            fixedDelayString = "${app.conversation.name-backfill-interval-ms:600000}")
    public void run() {
        if (settings.conversation().workerEnabled()) {
            channelNames.backfill();
        }
    }
}
