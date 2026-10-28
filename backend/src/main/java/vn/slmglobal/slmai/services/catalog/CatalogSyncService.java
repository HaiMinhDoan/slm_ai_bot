package vn.slmglobal.slmai.services.catalog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import vn.slmglobal.slmai.adapters.slmv2.SlmV2CatalogReader;
import vn.slmglobal.slmai.adapters.slmv2.SlmV2Snapshot;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.services.notification.NotificationDraft;
import vn.slmglobal.slmai.services.notification.NotificationService;
import vn.slmglobal.slmai.services.notification.NotificationSeverity;
import vn.slmglobal.slmai.services.notification.NotificationType;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Đồng bộ catalog từ slm_v2 (mục 4.2.3): đọc mọi view trong một transaction chỉ đọc ở slm_v2 → chuẩn hóa → ghi
 * dm_* trong một transaction ở DB của bot. Lỗi giữa chừng thì rollback toàn bộ, bot vẫn dùng bản catalog trước đó,
 * quản trị nhận thông báo (tối đa một lần mỗi ngày).
 *
 * <p>Chặn ghi khi dữ liệu nguồn bất thường (không có gói nào, hoặc số gói đang bán giảm quá một nửa): thà giữ
 * bản cũ còn hơn ngừng bán hàng loạt vì một view lỗi.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogSyncService {

    public static final String TRIGGER_SCHEDULED = "SCHEDULED";
    public static final String TRIGGER_MANUAL = "MANUAL";
    static final double MAX_ACTIVE_DROP = 0.5;
    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final SlmV2CatalogReader reader;
    private final CatalogNormalizer normalizer;
    private final CatalogRepository repository;
    private final NotificationService notifications;
    private final TransactionTemplate tx;

    private final AtomicBoolean running = new AtomicBoolean();
    /** Phiên bản catalog đang dùng (luot_goi_ai.phien_ban_catalog); đọc lại từ lần đồng bộ thành công gần nhất */
    private final AtomicReference<String> version = new AtomicReference<>();

    public boolean isConfigured() {
        return reader.isConfigured();
    }

    /** Phiên bản của bản catalog đang dùng; rỗng khi chưa đồng bộ thành công lần nào */
    public String currentVersion() {
        String v = version.get();
        if (v == null) {
            v = repository.lastSuccess().map(CatalogRepository.SyncRun::version).orElse(null);
            if (v != null) {
                version.compareAndSet(null, v);
            }
        }
        return v;
    }

    /**
     * @param trigger     SCHEDULED hoặc MANUAL
     * @param triggeredBy email người bấm "Đồng bộ ngay"; null khi chạy theo lịch
     * @throws CommonException 409 khi đang có lần đồng bộ khác, 503 khi chưa cấu hình slm_v2
     */
    public CatalogRepository.SyncRun sync(String trigger, String triggeredBy) {
        if (!reader.isConfigured()) {
            throw new CommonException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Chưa cấu hình kết nối slm_v2 (SLM_V2_DB_URL, SLM_V2_DB_USERNAME, SLM_V2_DB_PASSWORD)");
        }
        if (!running.compareAndSet(false, true)) {
            throw new CommonException(HttpStatus.CONFLICT, "Đang đồng bộ catalog, thử lại sau ít phút");
        }
        try {
            repository.failStaleRuns();
            long runId = repository.startRun(trigger, triggeredBy);
            try {
                SlmV2Snapshot snapshot = reader.read();
                CatalogRows.Normalized catalog = normalizer.normalize(snapshot);
                CatalogRepository.Counts counts = tx.execute(status -> write(catalog));
                repository.finishRun(runId, "SUCCESS", counts, catalog.warnings(), null, catalog.version());
                version.set(catalog.version());
                if (counts != null && (counts.inserted() + counts.updated() + counts.deactivated()) > 0) {
                    log.info("Đồng bộ catalog: thêm {}, sửa {}, ngừng {}; phiên bản {}", counts.inserted(),
                            counts.updated(), counts.deactivated(), catalog.version().substring(0, 12));
                }
            } catch (Exception e) {
                String error = describe(e);
                log.warn("Đồng bộ catalog lỗi: {}", error);
                repository.finishRun(runId, "FAILED", null, null, error, null);
                notifyFailure(error);
            }
            return repository.runs(1).get(0);
        } finally {
            running.set(false);
        }
    }

    private CatalogRepository.Counts write(CatalogRows.Normalized c) {
        if (!repository.tryLock()) {
            throw new IllegalStateException("Một tiến trình khác đang đồng bộ catalog");
        }
        long activeIncoming = c.packages().stream().filter(CatalogRows.PackageRow::active).count();
        int activeNow = repository.countActivePackages();
        if (activeIncoming == 0) {
            throw new IllegalStateException("slm_v2 không trả gói nào đang bán; giữ nguyên catalog cũ");
        }
        if (activeNow > 0 && activeIncoming < activeNow * (1 - MAX_ACTIVE_DROP)) {
            throw new IllegalStateException("Số gói đang bán giảm bất thường (" + activeNow + " → " + activeIncoming
                    + "); giữ nguyên catalog cũ, kiểm tra slm_v2");
        }
        // Thứ tự theo khoá ngoại: thương hiệu → cơ sở → vật tư → gói → thành phần
        return repository.upsertBrands(c.brands())
                .plus(repository.upsertBranches(c.branches()))
                .plus(repository.upsertMaterials(c.materials()))
                .plus(repository.upsertPackages(c.packages()))
                .plus(repository.upsertItems(c.items()))
                .plus(repository.upsertDomains(c.domains()));
    }

    private void notifyFailure(String error) {
        try {
            tx.executeWithoutResult(status -> notifications.notifyAdmins(NotificationDraft.builder()
                    .type(NotificationType.CATALOG_SYNC_FAILED)
                    .severity(NotificationSeverity.WARNING)
                    .title("Đồng bộ catalog từ slm_v2 lỗi")
                    .body("Bot vẫn báo giá bằng bản catalog trước đó. Lỗi: " + truncate(error, 300))
                    .data(Map.of("error", truncate(error, 2000)))
                    .dedupeKey("CATALOG_SYNC_FAILED:" + LocalDate.now(ZONE))
                    .build()));
        } catch (RuntimeException e) {
            log.warn("Không ghi được thông báo đồng bộ lỗi: {}", e.getMessage());
        }
    }

    public List<CatalogRepository.SyncRun> runs(int limit) {
        return repository.runs(limit);
    }

    private static String describe(Exception e) {
        String m = e.getMessage();
        return e.getClass().getSimpleName() + (m == null ? "" : ": " + m);
    }

    private static String truncate(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }
}
