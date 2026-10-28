package vn.slmglobal.slmai.services.sizing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import vn.slmglobal.slmai.services.notification.NotificationDraft;
import vn.slmglobal.slmai.services.notification.NotificationService;
import vn.slmglobal.slmai.services.notification.NotificationSeverity;
import vn.slmglobal.slmai.services.notification.NotificationType;

import java.time.LocalDate;
import java.time.Month;
import java.util.Map;

/**
 * Nhắc quản trị nạp bảng PV out EVN năm mới (mục 60.3). EVN công bố bảng của năm sau vào khoảng tháng 1 (bảng 2026:
 * công văn ngày 20/1/2026). Mỗi ngày 8 giờ và lúc khởi động:
 * <ul>
 *   <li>từ 1/12: chưa có bảng năm sau → nhắc chuẩn bị (INFO), một lần mỗi năm;</li>
 *   <li>từ 1/2: vẫn chưa có bảng năm nay → bot đang dùng bảng năm trước (WARNING), một lần mỗi năm.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PvoutReminderJob {

    private final PvoutRepository repository;
    private final NotificationService notifications;
    private final TransactionTemplate tx;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        try {
            check(LocalDate.now(PvoutService.ZONE));
        } catch (RuntimeException e) {
            log.warn("Không kiểm tra được hạn nạp bảng PV out: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Ho_Chi_Minh")
    public void daily() {
        check(LocalDate.now(PvoutService.ZONE));
    }

    /** Gọi từ chính bean này nên mở transaction bằng TransactionTemplate (thông báo phải ghi trong transaction). */
    public void check(LocalDate today) {
        tx.executeWithoutResult(status -> remind(today));
    }

    private void remind(LocalDate today) {
        int year = today.getYear();
        if (today.getMonth() == Month.DECEMBER && !repository.exists(year + 1)) {
            notifications.notifyAdmins(NotificationDraft.builder()
                    .type(NotificationType.DATA_UPDATE_DUE)
                    .severity(NotificationSeverity.INFO)
                    .title("Sắp đến hạn nạp bảng PV out năm " + (year + 1))
                    .body("EVN thường công bố hệ số PV out của năm sau vào tháng 1. Khi có công văn, nạp bảng mới ở "
                            + "trang Catalog & báo giá → PV out EVN; tới lúc đó bot vẫn dùng bảng " + year + ".")
                    .data(Map.of("kind", "PVOUT", "year", year + 1))
                    .dedupeKey("PVOUT_DUE:" + (year + 1))
                    .build());
        }
        if (today.getMonthValue() >= 2 && !repository.exists(year)) {
            int using = repository.latestYearUpTo(year).orElse(year - 1);
            notifications.notifyAdmins(NotificationDraft.builder()
                    .type(NotificationType.DATA_UPDATE_DUE)
                    .severity(NotificationSeverity.WARNING)
                    .title("Chưa có bảng PV out năm " + year)
                    .body("Bot đang tính sản lượng theo bảng PV out năm " + using + ". Nạp bảng " + year
                            + " của EVN ở trang Catalog & báo giá → PV out EVN.")
                    .data(Map.of("kind", "PVOUT", "year", year, "usingYear", using))
                    .dedupeKey("PVOUT_OVERDUE:" + year)
                    .build());
        }
    }
}
