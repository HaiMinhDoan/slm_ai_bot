package vn.slmglobal.slmai.services.ai;

import org.junit.jupiter.api.Test;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.conversation.entity.ReplyMode;
import vn.slmglobal.slmai.services.lead.ConsultSlots;
import vn.slmglobal.slmai.services.lead.entity.ConsultProfile;
import vn.slmglobal.slmai.services.settings.AiSettingsService;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Thứ tự hỏi thông tin tối thiểu trong ngữ cảnh lượt (2026-11-12: ngày / đêm hỏi sau cùng, mái hỏi kèm diện tích) */
class CustomerChatPromptBuilderTest {

    private static final ZonedDateTime NOW = ZonedDateTime.parse("2026-11-12T10:00:00+07:00[Asia/Ho_Chi_Minh]");

    private final CustomerChatPromptBuilder builder = builder();

    @Test
    void dayNightIsAskedLastAndRoofTogetherWithArea() {
        String ctx = context(ConsultProfile.builder()
                .missingSlots(new String[]{ConsultSlots.LOAI_MAI, ConsultSlots.TY_LE_NGAY_DEM}).build());

        assertThat(ctx).contains("Thông tin tối thiểu còn thiếu: loai_mai, ty_le_ngay_dem (ty_le_ngay_dem hỏi sau cùng")
                .contains("Hỏi loại mái thì hỏi kèm diện tích mái trong cùng một câu");
        assertThat(context(null)).contains("hoa_don_thang, loai_mai, ty_le_ngay_dem (ty_le_ngay_dem hỏi sau cùng")
                .contains("hỏi kèm diện tích mái");
    }

    @Test
    void noOrderOrAreaHintWhenNotNeeded() {
        String onlyDayNight = context(ConsultProfile.builder()
                .missingSlots(new String[]{ConsultSlots.TY_LE_NGAY_DEM}).build());
        assertThat(onlyDayNight).contains("Thông tin tối thiểu còn thiếu: ty_le_ngay_dem.")
                .doesNotContain("diện tích mái");

        ConsultProfile areaKnown = ConsultProfile.builder()
                .missingSlots(new String[]{ConsultSlots.LOAI_MAI}).build();
        areaKnown.getSlots().put(ConsultSlots.DIEN_TICH_MAI, 60);
        assertThat(context(areaKnown)).contains("Thông tin tối thiểu còn thiếu: loai_mai.")
                .doesNotContain("diện tích mái");
    }

    private String context(ConsultProfile profile) {
        TurnSnapshot s = new TurnSnapshot(UUID.randomUUID(), UUID.randomUUID(), ChannelCode.MESSENGER, null, null, 1,
                List.of(), List.of(), ReplyMode.AUTO, false, false, null, profile, null, "Lê Huy Sĩ", "ANH");
        return builder.turnContext(s, NOW, true, true);
    }

    private static CustomerChatPromptBuilder builder() {
        AiProperties ai = mock(AiProperties.class);
        when(ai.businessHours()).thenReturn(new AiProperties.BusinessHours("Asia/Ho_Chi_Minh", "08:00", "17:30",
                List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)));
        AiSettingsService settings = mock(AiSettingsService.class);
        when(settings.ai()).thenReturn(ai);
        return new CustomerChatPromptBuilder(settings, null);
    }
}
