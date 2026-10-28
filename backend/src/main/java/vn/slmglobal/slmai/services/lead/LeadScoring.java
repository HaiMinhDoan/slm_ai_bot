package vn.slmglobal.slmai.services.lead;

import vn.slmglobal.slmai.services.lead.entity.LeadLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Tính mức tiềm năng theo bản nháp rules/lead-scoring.yaml (mục 58.5), tất định. Xét theo thứ tự
 * HOT → OFF_SEGMENT → WARM → COLD. AI chỉ trích tín hiệu; mức do backend tính.
 */
public final class LeadScoring {

    static final Set<String> SOON = Set.of("TUAN_TOI", "THANG_TOI");
    static final int WARM_ANSWERED_QUESTIONS = 3;
    static final double LOW_BUDGET_RATIO = 0.8;

    /**
     * @param cheapestMatchVnd giá gói rẻ nhất phù hợp mà khách đã được báo (tool tim_goi_phu_hop, tin đã gửi);
     *                         null khi chưa báo giá theo hoá đơn của khách
     */
    public record Signals(boolean hasPhone, String timeline, boolean minimumInfoComplete, int answeredQuestions,
                          Long budgetMaxVnd, Long cheapestMatchVnd) {
    }

    public record Score(LeadLevel level, String reason, boolean lowBudget) {
    }

    private LeadScoring() {
    }

    public static Score score(Signals s) {
        boolean lowBudget = s.budgetMaxVnd() != null && s.cheapestMatchVnd() != null
                && s.budgetMaxVnd() < s.cheapestMatchVnd() * LOW_BUDGET_RATIO;

        if (s.hasPhone() && (SOON.contains(s.timeline()) || s.minimumInfoComplete())) {
            List<String> why = new ArrayList<>();
            why.add("Đã cho số điện thoại");
            if (SOON.contains(s.timeline())) {
                why.add("TUAN_TOI".equals(s.timeline()) ? "dự định lắp trong tuần tới" : "dự định lắp trong tháng tới");
            }
            if (s.minimumInfoComplete()) {
                why.add("đã đủ thông tin tối thiểu");
            }
            if (lowBudget) {
                why.add("ngân sách thấp hơn gói rẻ nhất phù hợp");
            }
            return new Score(LeadLevel.HOT, String.join("; ", why), lowBudget);
        }
        if (lowBudget) {
            return new Score(LeadLevel.OFF_SEGMENT, "Ngân sách thấp hơn rõ rệt gói rẻ nhất phù hợp đã báo", true);
        }
        if (s.answeredQuestions() >= WARM_ANSWERED_QUESTIONS) {
            return new Score(LeadLevel.WARM, "Đã trả lời " + s.answeredQuestions() + " câu hỏi"
                    + (s.hasPhone() ? "; đã cho số điện thoại" : ""), false);
        }
        if (s.hasPhone()) {
            return new Score(LeadLevel.WARM, "Đã cho số điện thoại, chưa rõ dự định lắp sớm", false);
        }
        return new Score(LeadLevel.COLD, "Mới trả lời " + s.answeredQuestions() + " câu hỏi, chưa cho số điện thoại",
                false);
    }
}
