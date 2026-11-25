package vn.slmglobal.slmai.services.settings;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.DayOfWeek;
import java.util.List;

/**
 * Cài đặt vận hành AI sửa trên admin, có hiệu lực ngay, không qua phát hành (kế hoạch 33.2, 35). Mỗi trường là một
 * dòng của bảng {@code cai_dat_ai} khi bị ghi đè (khoá = tên trường snake_case); thiếu thì dùng giá trị trong
 * application.yml / .env.
 *
 * @param killSwitch           công tắc dừng toàn cục: bật thì mọi lượt chỉ lưu nháp
 * @param autoIntents          loại câu hỏi được gửi tự động
 * @param handoffIntents       loại câu hỏi luôn chuyển nhân viên
 * @param businessStart        giờ bắt đầu làm việc HH:mm
 * @param businessEnd          giờ kết thúc HH:mm
 * @param businessDays         các ngày làm việc
 * @param outOfHoursAck        câu báo nhận ngoài giờ (để trống = không gửi)
 * @param handoffAck           câu báo nhận khi chuyển nhân viên trong giờ (để trống = không gửi)
 * @param ackCooldownHours     không gửi lại câu báo nhận trong khoảng này
 * @param personaName          tên tư vấn viên AI tự xưng
 * @param companyName          tên công ty
 * @param typingIndicator      gửi "đang soạn tin" trong lúc chờ AI
 * @param typingCharsPerSecond tốc độ gõ giả lập: trước mỗi tin chờ số ký tự / tốc độ này
 * @param typingMaxSeconds     chờ tối đa trước mỗi tin (giây)
 * @param debounceSeconds      gộp tin: chờ thêm khoảng này sau tin mới nhất của khách
 * @param autoHandbackHours    tự trả hội thoại lại cho AI sau khoảng này không có tin của nhân viên (0 = không tự trả)
 * @param model                model trả lời khách
 * @param effort               mức suy nghĩ: low / medium / high / xhigh / max
 * @param assistantModel       model của AI hỗ trợ soạn kiến thức (mục 5.10)
 * @param maxAttempts          gọi AI lỗi quá số lần này thì chuyển nhân viên
 * @param retryBackoffSeconds  chờ trước lần thử lại đầu tiên (các lần sau gấp đôi)
 * @param followUpEnabled      nhắc khách khi khách im lặng (mục 56.9)
 * @param followUpIntervalHours khách im lặng quá khoảng này sau tin cuối của bot thì nhắc (giờ)
 * @param followUpMaxCount     số lần nhắc tối đa trong một cửa sổ 24 giờ (0 = không giới hạn)
 * @param followUpAutoSend     tin nhắc tự gửi; tắt thì thành nháp chờ duyệt
 * @param followUpStart        đầu khung giờ được nhắc HH:mm
 * @param followUpEnd          cuối khung giờ được nhắc HH:mm
 * @param followUpNotifyStaff  hết cửa sổ 24 giờ mà khách vẫn im lặng thì báo nhân viên
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiSettings(
        KillSwitch killSwitch,
        List<String> autoIntents,
        List<String> handoffIntents,
        String businessStart,
        String businessEnd,
        List<DayOfWeek> businessDays,
        String outOfHoursAck,
        String handoffAck,
        double ackCooldownHours,
        String personaName,
        String companyName,
        boolean typingIndicator,
        double typingCharsPerSecond,
        double typingMaxSeconds,
        double debounceSeconds,
        double autoHandbackHours,
        String model,
        String effort,
        String assistantModel,
        int maxAttempts,
        double retryBackoffSeconds,
        boolean followUpEnabled,
        double followUpIntervalHours,
        int followUpMaxCount,
        boolean followUpAutoSend,
        String followUpStart,
        String followUpEnd,
        boolean followUpNotifyStaff
) {

    /**
     * @param on        đang bật
     * @param reason    lý do bật (bắt buộc khi bật)
     * @param updatedBy email người bật / tắt gần nhất
     * @param updatedAt thời điểm ISO-8601
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KillSwitch(boolean on, String reason, String updatedBy, String updatedAt) {
    }
}
