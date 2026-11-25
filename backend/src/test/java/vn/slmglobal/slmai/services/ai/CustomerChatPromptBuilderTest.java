package vn.slmglobal.slmai.services.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.Test;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.conversation.entity.Message;
import vn.slmglobal.slmai.services.conversation.entity.MessageDirection;
import vn.slmglobal.slmai.services.conversation.entity.MessageStatus;
import vn.slmglobal.slmai.services.conversation.entity.SenderType;
import vn.slmglobal.slmai.services.conversation.entity.ReplyMode;
import vn.slmglobal.slmai.services.knowledge.KnowledgeBase;
import vn.slmglobal.slmai.services.knowledge.KnowledgeSnapshot;
import vn.slmglobal.slmai.services.lead.ConsultSlots;
import vn.slmglobal.slmai.services.lead.entity.ConsultProfile;
import vn.slmglobal.slmai.services.settings.AiSettingsService;

import java.time.DayOfWeek;
import java.time.Duration;
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

    /** Media trang đã gửi hiện trong lịch sử để AI không gửi lại và nói đúng về chúng; mục chưa tới khách thì không */
    @Test
    void sentMediaIsNotedInHistory() {
        ArrayNode media = new ObjectMapper().createArrayNode();
        media.addObject().put("kind", "QUOTE").put("title", "Ảnh báo giá Hệ On-Grid 29.4 kWp 3 pha")
                .put("status", "SENT");
        media.addObject().put("kind", "MEDIA").put("media_type", "VIDEO").put("code", "M25")
                .put("title", "Video: Hệ On-Grid 5 kWp 1 pha").put("status", "SENT");
        media.addObject().put("kind", "MEDIA").put("media_type", "IMAGE").put("code", "M03").put("title", "Ảnh lỗi")
                .put("status", "FAILED");
        List<ClaudeChatClient.ChatTurn> turns = CustomerChatPromptBuilder.turns(List.of(
                Message.builder().direction(MessageDirection.IN).senderType(SenderType.CUSTOMER)
                        .status(MessageStatus.PROCESSED).content("Gửi anh gói 30 kWp").build(),
                Message.builder().direction(MessageDirection.OUT).senderType(SenderType.AI).status(MessageStatus.SENT)
                        .content("Dạ gói đây ạ").attachments(media).build(),
                Message.builder().direction(MessageDirection.IN).senderType(SenderType.CUSTOMER)
                        .status(MessageStatus.RECEIVED).content("Ok em").build()));

        assertThat(turns.get(1).text()).isEqualTo("Dạ gói đây ạ\n[Đã gửi kèm: Ảnh báo giá Hệ On-Grid 29.4 kWp 3 pha; "
                + "video M25 Video: Hệ On-Grid 5 kWp 1 pha]");
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

    /** Lượt nhắc khách (mục 56.9): kết thúc bằng ghi chú hệ thống, ngữ cảnh nói rõ là lượt nhắc */
    @Test
    void followUpEndsWithSystemNoteAndExplainsTheNudge() {
        KnowledgeBase kb = mock(KnowledgeBase.class);
        KnowledgeSnapshot k = new KnowledgeSnapshot(1L, "v1", List.of(), null);
        when(kb.renderPrompt(k)).thenReturn("Hướng dẫn");
        when(kb.renderKnowledge(k)).thenReturn("Kiến thức");
        AiSettingsService settings = mock(AiSettingsService.class);
        AiProperties ai = mock(AiProperties.class);
        when(ai.businessHours()).thenReturn(new AiProperties.BusinessHours("Asia/Ho_Chi_Minh", "08:00", "17:30",
                List.of()));
        when(settings.ai()).thenReturn(ai);
        CustomerChatPromptBuilder b = new CustomerChatPromptBuilder(settings, kb);
        Message customer = Message.builder().direction(MessageDirection.IN).senderType(SenderType.CUSTOMER)
                .status(MessageStatus.PROCESSED).content("Nhà anh ở Bắc Ninh, tiền điện 2 triệu").build();
        Message bot = Message.builder().direction(MessageDirection.OUT).senderType(SenderType.AI)
                .status(MessageStatus.SENT).content("Dạ em đề xuất gói Hybrid ạ").build();
        CustomerChatPromptBuilder.FollowUpContext f = new CustomerChatPromptBuilder.FollowUpContext(2,
                Duration.ofHours(3), Duration.ofHours(5).plusMinutes(10), true, List.of("video M27 Video: Hệ Hy-Brid"));

        ClaudeChatClient.ChatRequest r = b.buildFollowUp(snapshot(List.of(customer, bot)), k, null, NOW, true, true,
                f).orElseThrow();

        assertThat(r.turns()).hasSize(3);
        assertThat(r.turns().get(2).customer()).isTrue();
        assertThat(r.turns().get(2).text()).isEqualTo("[Khách chưa trả lời sau 3 giờ]");
        assertThat(r.turnContext()).contains("lượt NHẮC KHÁCH").contains("lần nhắc thứ 2")
                .contains("còn khoảng 5 giờ 10 phút").contains("có thể là lần nhắc cuối")
                .contains("Hệ thống sẽ tự gửi kèm sau tin nhắc: video M27 Video: Hệ Hy-Brid")
                .contains("Để reply là chuỗi rỗng").endsWith("</ngu_canh_luot>");
        assertThat(r.cachedSystem()).startsWith("Hướng dẫn");

        // Tin cuối là của khách: không phải lúc nhắc
        assertThat(b.buildFollowUp(snapshot(List.of(bot, customer)), k, null, NOW, true, true, f)).isEmpty();
    }

    private static TurnSnapshot snapshot(List<Message> history) {
        return new TurnSnapshot(UUID.randomUUID(), UUID.randomUUID(), ChannelCode.MESSENGER, null, null, 1,
                history, List.of(), ReplyMode.AUTO, false, false, null, null, null, "Lê Huy Sĩ", "ANH");
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
