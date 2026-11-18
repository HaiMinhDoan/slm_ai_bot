package vn.slmglobal.slmai.services.policy;

import org.junit.jupiter.api.Test;
import vn.slmglobal.slmai.services.ai.AiProperties;
import vn.slmglobal.slmai.services.ai.AiReply;
import vn.slmglobal.slmai.services.conversation.entity.ReplyMode;
import vn.slmglobal.slmai.services.settings.AiSettingsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AI đề nghị chuyển nhân viên ngay lượt đầu sau khi được giao lại: thành nháp, không chuyển (hội thoại Lê Huy Sĩ,
 * 2026-11-06: nhân viên bấm "Chuyển cho AI" 5 lần, lần nào AI cũng chuyển lại trong vài giây).
 */
class ReplyPolicyHandbackTest {

    private static final String WAITING = "Dạ em xin lỗi đã để anh chờ, em đã chuyển nhân viên phụ trách ạ.";

    private final AiSettingsService settings = mock(AiSettingsService.class);
    private final ReplyPolicy policy = new ReplyPolicy(settings);

    ReplyPolicyHandbackTest() {
        AiProperties ai = mock(AiProperties.class);
        when(ai.autoIntents()).thenReturn(List.of("HOI_GIA"));
        when(ai.handoffIntents()).thenReturn(List.of("KHIEU_NAI"));
        when(settings.ai()).thenReturn(ai);
    }

    @Test
    void handoffRequestOnOrdinaryTurnHandsOff() {
        ReplyPolicy.Decision d = policy.evaluate(input(reply("HOI_GIA", WAITING, true), false, false));

        assertThat(d.action()).isEqualTo(ReplyPolicy.Action.HANDOFF);
        assertThat(d.reasons()).containsExactly(ReplyPolicy.HANDOFF_REQUESTED);
    }

    @Test
    void handoffRequestRightAfterStaffHandbackBecomesDraft() {
        ReplyPolicy.Decision d = policy.evaluate(input(reply("HOI_GIA", WAITING, true), false, true));

        assertThat(d.action()).isEqualTo(ReplyPolicy.Action.DRAFT);
        assertThat(d.reasons()).containsExactly(ReplyPolicy.HANDOFF_AFTER_HANDBACK);
    }

    @Test
    void handoffRequestRightAfterAutoHandbackBecomesDraft() {
        ReplyPolicy.Decision d = policy.evaluate(input(reply("HOI_GIA", WAITING, true), true, false));

        assertThat(d.action()).isEqualTo(ReplyPolicy.Action.DRAFT);
        assertThat(d.reasons()).containsExactly(ReplyPolicy.HANDOFF_AFTER_HANDBACK,
                ReplyPolicy.FIRST_TURN_AFTER_HANDBACK);
    }

    @Test
    void staffHandbackTurnWithoutHandoffIsSentAsUsual() {
        ReplyPolicy.Decision d = policy.evaluate(input(reply("HOI_GIA", "Dạ mái ngói lắp được anh nhé.", false),
                false, true));

        assertThat(d.action()).isEqualTo(ReplyPolicy.Action.AUTO);
    }

    @Test
    void handoffIntentAndEmptyReplyStillHandOffAfterHandback() {
        assertThat(policy.evaluate(input(reply("KHIEU_NAI", WAITING, true), false, true)).reasons())
                .containsExactly(ReplyPolicy.HANDOFF_INTENT);
        assertThat(policy.evaluate(input(reply("HOI_GIA", "", true), false, true)).reasons())
                .containsExactly(ReplyPolicy.EMPTY_REPLY);
    }

    private static AiReply reply(String intent, String text, boolean handoff) {
        return new AiReply("other", intent, text, null, null, null, List.of(), null,
                new AiReply.Handoff(handoff, handoff ? "Khách sốt ruột, muốn gặp nhân viên" : ""), List.of());
    }

    private static ReplyPolicy.Input input(AiReply reply, boolean autoHandback, boolean staffHandback) {
        return new ReplyPolicy.Input(reply, ReplyMode.AUTO, autoHandback, staffHandback, true, true, 2000, List.of(),
                List.of(), List.of(), null);
    }
}
