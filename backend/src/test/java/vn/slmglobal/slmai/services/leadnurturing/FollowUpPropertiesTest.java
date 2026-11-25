package vn.slmglobal.slmai.services.leadnurturing;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** Khung giờ nhắc khách và "lần nhắc cuối" (mục 56.9). */
class FollowUpPropertiesTest {

    private static final ZoneId VN = ZoneId.of("Asia/Ho_Chi_Minh");

    private static FollowUpProperties rules(String start, String end, int maxCount) {
        return new FollowUpProperties(true, Duration.ofHours(3), maxCount, true, start, end, true,
                Duration.ofMinutes(30), Duration.ofMinutes(15));
    }

    private static ZonedDateTime at(int day, int hour, int minute) {
        return ZonedDateTime.of(2026, 11, day, hour, minute, 0, 0, VN);
    }

    @Test
    void hoursIncludeStartExcludeEnd() {
        FollowUpProperties r = rules("08:00", "21:00", 0);
        assertThat(r.isInHours(at(25, 8, 0), VN)).isTrue();
        assertThat(r.isInHours(at(25, 20, 59), VN)).isTrue();
        assertThat(r.isInHours(at(25, 21, 0), VN)).isFalse();
        assertThat(r.isInHours(at(25, 7, 59), VN)).isFalse();
        // Giờ UTC vẫn tính theo giờ Việt Nam: 01:30 UTC = 08:30
        assertThat(r.isInHours(ZonedDateTime.of(2026, 11, 25, 1, 30, 0, 0, ZoneId.of("UTC")), VN)).isTrue();
    }

    @Test
    void hoursCanCrossMidnight() {
        FollowUpProperties r = rules("20:00", "02:00", 0);
        assertThat(r.isInHours(at(25, 23, 0), VN)).isTrue();
        assertThat(r.isInHours(at(25, 1, 0), VN)).isTrue();
        assertThat(r.isInHours(at(25, 12, 0), VN)).isFalse();
    }

    @Test
    void slotBetweenFindsNextMorningInsideWindow() {
        FollowUpProperties r = rules("08:00", "21:00", 0);
        // 22:00 → cửa sổ tới 15:00 hôm sau: còn được nhắc lúc 08:00
        assertThat(r.hasSlotBetween(at(25, 22, 0), at(26, 15, 0), VN)).isTrue();
        // 22:00 → cửa sổ tới 07:30 hôm sau: không còn giờ nhắc nào
        assertThat(r.hasSlotBetween(at(25, 22, 0), at(26, 7, 30), VN)).isFalse();
        assertThat(r.hasSlotBetween(at(25, 10, 0), at(25, 11, 0), VN)).isTrue();
        assertThat(r.hasSlotBetween(at(25, 12, 0), at(25, 11, 0), VN)).isFalse();
    }

    @Test
    void maxCountZeroMeansUnlimited() {
        assertThat(rules("08:00", "21:00", 0).allows(9)).isTrue();
        assertThat(rules("08:00", "21:00", 2).allows(2)).isTrue();
        assertThat(rules("08:00", "21:00", 2).allows(3)).isFalse();
    }
}
