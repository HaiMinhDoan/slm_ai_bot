package vn.slmglobal.slmai.services.lead;

import org.junit.jupiter.api.Test;
import vn.slmglobal.slmai.services.lead.entity.LeadLevel;

import static org.assertj.core.api.Assertions.assertThat;

/** Phân loại khách (mục 58.5). */
class LeadScoringTest {

    @Test
    void phoneWithoutTimelineDoesNotFail() {
        // Khách cho số mà chưa nói thời điểm lắp: từng ném NPE (Set.of().contains(null)) làm hỏng cả lượt AI
        LeadScoring.Score s = LeadScoring.score(new LeadScoring.Signals(true, null, false, 1, null, null));
        assertThat(s.level()).isEqualTo(LeadLevel.WARM);
        assertThat(LeadScoring.score(new LeadScoring.Signals(true, null, true, 5, null, null)).level())
                .isEqualTo(LeadLevel.HOT);
    }

    @Test
    void soonTimelineWithPhoneIsHot() {
        LeadScoring.Score s = LeadScoring.score(new LeadScoring.Signals(true, "TUAN_TOI", false, 1, null, null));
        assertThat(s.level()).isEqualTo(LeadLevel.HOT);
        assertThat(s.reason()).contains("tuần tới");
        assertThat(LeadScoring.score(new LeadScoring.Signals(false, null, false, 0, null, null)).level())
                .isEqualTo(LeadLevel.COLD);
    }
}
