package vn.slmglobal.slmai.services.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Cấu hình AI trả lời khách, khối {@code app.ai:} trong application.yml (mục 18–20, 33, 35).
 *
 * @param apiKey             ANTHROPIC_API_KEY; để trống thì worker AI không chạy, tin khách vẫn được lưu
 * @param model              một model cho mọi hội thoại với khách, chọn bằng bộ test (mục 20.1)
 * @param effort             low / medium / high / xhigh / max; giảm chi phí, độ trễ thì hạ mức này trước
 * @param maxTokens          trần token đầu ra của một lượt (gồm cả phần suy nghĩ)
 * @param timeout            thời gian chờ tối đa một lần gọi Claude
 * @param serverSideFallback khi model từ chối do bộ lọc an toàn, để API tự chạy lại trên model dự phòng
 * @param forceDraft         công tắc dừng toàn cục (mục 33.2): bật thì mọi lượt đi theo DRAFT
 * @param workerEnabled      bật worker AI; tắt trong test
 * @param workerConcurrency  số hội thoại xử lý song song
 * @param historyLimit       số tin gần nhất đưa vào ngữ cảnh
 * @param maxAttempts        gọi Claude lỗi quá số lần này thì chuyển nhân viên
 * @param retryBackoff       chờ trước lần thử lại đầu tiên; các lần sau gấp đôi
 * @param turnLease          giữ lượt trong lúc gọi Claude; tiến trình chết giữa chừng thì hết hạn này lượt được chạy lại
 * @param typingIndicator    gửi "đang soạn tin" trước khi gọi Claude
 * @param companyName        tên công ty AI dùng khi tự giới thiệu
 * @param personaName        tên tư vấn viên AI dùng khi tự giới thiệu (core/identity); khách hỏi thẳng thì vẫn nói là AI
 * @param autoIntents        loại câu hỏi được gửi tự động (mục 35)
 * @param handoffIntents     loại câu hỏi luôn chuyển nhân viên (mục 35)
 */
@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(
        String apiKey,
        String model,
        String effort,
        long maxTokens,
        Duration timeout,
        boolean serverSideFallback,
        boolean forceDraft,
        boolean workerEnabled,
        int workerConcurrency,
        int historyLimit,
        int maxAttempts,
        Duration retryBackoff,
        Duration turnLease,
        boolean typingIndicator,
        String companyName,
        String personaName,
        List<String> autoIntents,
        List<String> handoffIntents,
        BusinessHours businessHours,
        Templates templates
) {

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Giờ làm việc của nhân viên: lượt DRAFT ngoài giờ thì gửi câu báo nhận (mục 35).
     *
     * @param zone  múi giờ, ví dụ Asia/Ho_Chi_Minh
     * @param start giờ bắt đầu HH:mm
     * @param end   giờ kết thúc HH:mm
     * @param days  các ngày làm việc
     */
    public record BusinessHours(String zone, String start, String end, List<DayOfWeek> days) {

        public ZoneId zoneId() {
            return ZoneId.of(zone == null || zone.isBlank() ? "Asia/Ho_Chi_Minh" : zone);
        }

        public boolean isOpen(ZonedDateTime at) {
            ZonedDateTime local = at.withZoneSameInstant(zoneId());
            if (days != null && !days.isEmpty() && !days.contains(local.getDayOfWeek())) {
                return false;
            }
            LocalTime t = local.toLocalTime();
            return !t.isBefore(LocalTime.parse(start)) && t.isBefore(LocalTime.parse(end));
        }
    }

    /**
     * Câu cố định hệ thống gửi thay AI (không qua Claude). Để trống thì không gửi.
     *
     * @param outOfHoursAck lượt DRAFT hoặc chuyển nhân viên ngoài giờ làm việc
     * @param handoffAck    chuyển nhân viên trong giờ làm việc
     * @param ackCooldown   không gửi lại câu báo nhận trong khoảng này
     */
    public record Templates(String outOfHoursAck, String handoffAck, Duration ackCooldown) {
    }
}
