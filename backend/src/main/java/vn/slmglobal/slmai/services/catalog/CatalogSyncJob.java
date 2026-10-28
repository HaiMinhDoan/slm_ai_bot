package vn.slmglobal.slmai.services.catalog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.slmglobal.slmai.common.exception.CommonException;

/**
 * Đồng bộ catalog theo lịch (mặc định 10 phút một lần, lần đầu sau khi khởi động 30 giây). Chưa cấu hình slm_v2
 * hoặc tắt bằng {@code app.catalog.sync-enabled=false} thì bỏ qua.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogSyncJob {

    private final CatalogSyncService syncService;

    @Value("${app.catalog.sync-enabled:true}")
    private boolean enabled;

    private volatile boolean warnedNotConfigured;

    @Scheduled(initialDelayString = "${app.catalog.initial-delay-ms:30000}",
            fixedDelayString = "${app.catalog.sync-interval-ms:600000}")
    public void run() {
        if (!enabled) {
            return;
        }
        if (!syncService.isConfigured()) {
            if (!warnedNotConfigured) {
                log.info("Chưa cấu hình SLM_V2_DB_*: không đồng bộ catalog, bot chưa có công cụ báo giá");
                warnedNotConfigured = true;
            }
            return;
        }
        try {
            syncService.sync(CatalogSyncService.TRIGGER_SCHEDULED, null);
        } catch (CommonException e) {
            log.debug("Bỏ lượt đồng bộ theo lịch: {}", e.getMessage());
        }
    }
}
