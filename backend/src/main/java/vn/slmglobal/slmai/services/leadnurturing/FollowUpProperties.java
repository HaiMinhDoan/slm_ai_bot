package vn.slmglobal.slmai.services.leadnurturing;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Nhắc khách khi khách im lặng (mục 56.9), khối {@code app.follow-up:} trong application.yml. Các giá trị sửa được
 * trên trang Cài đặt AI (AiSettingsService.followUp()); windowMargin và staleAfter chỉ đổi trong application.yml.
 *
 * @param enabled      bật nhắc khách
 * @param interval     khách im lặng quá khoảng này sau tin cuối của bot (kể cả tin nhắc trước) thì nhắc
 * @param maxCount     số lần nhắc tối đa trong một cửa sổ 24 giờ; 0 = không giới hạn (nhắc tới khi hết cửa sổ)
 * @param autoSend     tin nhắc tự gửi; tắt thì thành nháp chờ nhân viên duyệt
 * @param start        đầu khung giờ được nhắc HH:mm (giờ Việt Nam)
 * @param end          cuối khung giờ được nhắc HH:mm; nhỏ hơn start nghĩa là qua nửa đêm
 * @param notifyStaff  cửa sổ 24 giờ đã đóng mà khách vẫn im lặng thì báo nhân viên
 * @param windowMargin cửa sổ còn ít hơn khoảng này thì không nhắc nữa (đủ thời gian gọi AI và gửi)
 * @param staleAfter   lịch nhắc chờ lâu hơn khoảng này mới tới lượt chạy thì bỏ (điều kiện có thể đã đổi)
 */
@ConfigurationProperties(prefix = "app.follow-up")
public record FollowUpProperties(
        boolean enabled,
        Duration interval,
        int maxCount,
        boolean autoSend,
        String start,
        String end,
        boolean notifyStaff,
        Duration windowMargin,
        Duration staleAfter
) {

    /** Đang trong khung giờ được nhắc không (theo múi giờ {@code zone}) */
    public boolean isInHours(ZonedDateTime at, ZoneId zone) {
        LocalTime t = at.withZoneSameInstant(zone).toLocalTime();
        LocalTime from = LocalTime.parse(start);
        LocalTime to = LocalTime.parse(end);
        return from.isBefore(to)
                ? !t.isBefore(from) && t.isBefore(to)
                : !t.isBefore(from) || t.isBefore(to);
    }

    /** Còn được nhắc thêm lần thứ {@code next} không */
    public boolean allows(int next) {
        return maxCount <= 0 || next <= maxCount;
    }

    /**
     * Có thời điểm nào trong khung giờ nhắc nằm trong [{@code from}, {@code until}] không: dùng để biết lần nhắc này
     * có thể là lần cuối (lần sau sẽ rơi ra ngoài cửa sổ 24 giờ hoặc chỉ còn giờ đêm).
     */
    public boolean hasSlotBetween(ZonedDateTime from, ZonedDateTime until, ZoneId zone) {
        if (from.isAfter(until)) {
            return false;
        }
        if (isInHours(from, zone)) {
            return true;
        }
        ZonedDateTime local = from.withZoneSameInstant(zone);
        ZonedDateTime opens = local.with(LocalTime.parse(start));
        if (opens.isBefore(local)) {
            opens = opens.plusDays(1);
        }
        return !opens.isAfter(until);
    }
}
