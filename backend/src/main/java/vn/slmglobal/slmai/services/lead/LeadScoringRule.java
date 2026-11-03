package vn.slmglobal.slmai.services.lead;

import java.util.List;
import java.util.Set;

/**
 * Ngưỡng phân loại mức tiềm năng (rule {@code rules/phan-loai-khach}, mục 58.5), sửa trên admin và phát hành cùng
 * kiến thức. Giá trị mặc định là bản nháp đầu đã chạy từ 2026-10-29.
 *
 * @param soonTimelines         thời điểm lắp coi là "sớm" (HOT khi đã có số điện thoại); thuộc ConsultSlots.TIMELINES
 * @param warmAnsweredQuestions trả lời đủ số câu hỏi này thì thành WARM
 * @param lowBudgetRatio        ngân sách tối đa dưới tỷ lệ này × giá gói rẻ nhất đã báo thì là ngân sách thấp
 */
public record LeadScoringRule(Set<String> soonTimelines, int warmAnsweredQuestions, double lowBudgetRatio) {

    public static final LeadScoringRule DEFAULT = new LeadScoringRule(Set.of("TUAN_TOI", "THANG_TOI"), 3, 0.8);

    public LeadScoringRule {
        soonTimelines = soonTimelines == null ? Set.of() : Set.copyOf(soonTimelines);
    }

    /** Lỗi kiểm tra khi phát hành, rỗng nếu hợp lệ */
    public List<String> validate() {
        List<String> errors = new java.util.ArrayList<>();
        for (String t : soonTimelines) {
            if (!ConsultSlots.TIMELINES.contains(t)) {
                errors.add("soon_timelines: \"" + t + "\" không phải thời điểm hợp lệ " + ConsultSlots.TIMELINES);
            }
        }
        if (warmAnsweredQuestions < 1 || warmAnsweredQuestions > 10) {
            errors.add("warm_answered_questions: phải từ 1 đến 10");
        }
        if (lowBudgetRatio <= 0 || lowBudgetRatio > 1) {
            errors.add("low_budget_ratio: phải lớn hơn 0 và không quá 1");
        }
        return errors;
    }
}
