package vn.slmglobal.slmai.services.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.slmglobal.slmai.services.auth.repository.RefreshTokenRepository;

import java.time.OffsetDateTime;

/**
 * Dọn refresh token đã hết hạn hoặc đã thu hồi quá 7 ngày, mỗi đêm.
 * Việc dọn dẹp nhẹ nên dùng @Scheduled; các job nghiệp vụ (gộp tin, follow-up, đăng bài) dùng job lưu trong PostgreSQL (mục 30).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupJob {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 30 3 * * *", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void cleanup() {
        int deleted = refreshTokenRepository.deleteExpiredBefore(OffsetDateTime.now().minusDays(7));
        if (deleted > 0) {
            log.info("Đã xóa {} refresh token cũ", deleted);
        }
    }
}
