package vn.slmglobal.slmai.services.leadnurturing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import vn.slmglobal.slmai.adapters.facebook.MessengerClient;
import vn.slmglobal.slmai.common.storage.StorageService;
import vn.slmglobal.slmai.services.ai.AiTurnService;
import vn.slmglobal.slmai.services.ai.ClaudeChatClient;
import vn.slmglobal.slmai.services.catalog.PackageQuoteImageService;
import vn.slmglobal.slmai.services.conversation.ConversationService;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.settings.AiSettingsService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Nhắc khách khi khách im lặng (mục 56.9) trên PostgreSQL thật: quét lập lịch → lượt AI nhắc kèm video của gói đã đề
 * xuất → gửi qua Messenger; dừng khi khách nhắn, AI bỏ, nhân viên phụ trách; báo nhân viên khi hết cửa sổ 24 giờ.
 * Claude và Graph API được giả lập; khung giờ nhắc mở cả ngày để test không phụ thuộc giờ chạy.
 */
@Testcontainers
@SpringBootTest(properties = {
        "app.jwt.signer-key=test-signer-key-0123456789-abcdefghijklmnop",
        "app.conversation.worker-enabled=false",
        "app.ai.api-key=test-key",
        "app.ai.worker-poll-interval-ms=3600000",
        "app.ai.typing-indicator=false",
        "app.ai.typing-max-delay=0s",
        "app.follow-up.start=00:00",
        "app.follow-up.end=23:59",
        "app.follow-up.scan-interval-ms=3600000"
})
class FollowUpIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String VIDEO_URL = "https://www.facebook.com/share/v/19niuBUNZc/";
    private static final String NUDGE =
            "Dạ anh ơi, em gửi anh video công trình thực tế bên dưới để anh tham khảo thêm ạ.";

    @MockitoBean
    ClaudeChatClient claude;
    @MockitoBean
    MessengerClient messenger;
    @MockitoBean
    StorageService storage;
    @MockitoBean
    PackageQuoteImageService quoteImages;

    @Autowired
    FollowUpScheduler scheduler;
    @Autowired
    AiTurnService turns;
    @Autowired
    ConversationService conversations;
    @Autowired
    AiSettingsService settings;
    @Autowired
    JdbcTemplate jdbc;

    private UUID conversation;

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE lich_nhac_khach, thong_bao, luot_goi_ai, tin_nhan, ho_so_tu_van, hoi_thoai, "
                + "dinh_danh_kenh, khach_hang, media, media_nguon, media_lien_ket, cai_dat_ai CASCADE");
        settings.reload();
        when(messenger.sendText(anyString(), anyString(), anyBoolean()))
                .thenAnswer(i -> new MessengerClient.SendResult("mid." + UUID.randomUUID(), "psid-1"));
        when(quoteImages.canRender(anyInt())).thenReturn(false);
        claudeReplies(NUDGE);

        UUID video = jdbc.queryForObject("""
                INSERT INTO media (loai_media, tieu_de, quyen_su_dung, trang_thai, tom_tat, dung_khi, ma_danh_muc)
                VALUES ('VIDEO', 'Video: Hệ Hy-Brid 16 kWp 3 pha', 'LINK_ONLY', 'APPROVED', 'Công trình thực tế',
                        'Khách muốn xem công trình', 'M01') RETURNING id""", UUID.class);
        jdbc.update("""
                INSERT INTO media_nguon (media_id, loai_nguon, url_nguon, id_ngoai, trang_thai_xu_ly)
                VALUES (?, 'FACEBOOK', ?, '19niuBUNZc', 'READY')""", video, VIDEO_URL);
        jdbc.update("INSERT INTO media_lien_ket (media_id, loai_doi_tuong, ma_doi_tuong, vai_tro) "
                + "VALUES (?, 'PACKAGE', '132', 'RELATED')", video);

        // Khách nhắn 4 giờ trước, bot đề xuất gói 132 lúc 3 giờ 10 phút trước, khách chưa trả lời
        conversation = conversation(OffsetDateTime.now().minusHours(4), OffsetDateTime.now().minusMinutes(190));
    }

    @Test
    void silentCustomerGetsNudgeWithVideoThenAgainAfterInterval() {
        assertThat(scheduler.scan(OffsetDateTime.now())).isEqualTo(1);
        assertThat(followUp("trang_thai")).isEqualTo("PENDING");

        turns.process(conversation);

        ArgumentCaptor<ClaudeChatClient.ChatRequest> request =
                ArgumentCaptor.forClass(ClaudeChatClient.ChatRequest.class);
        verify(claude).complete(request.capture());
        List<ClaudeChatClient.ChatTurn> history = request.getValue().turns();
        assertThat(history.get(history.size() - 1).text()).startsWith("[Khách chưa trả lời sau 3 giờ");
        assertThat(request.getValue().turnContext()).contains("lượt NHẮC KHÁCH").contains("lần nhắc thứ 1")
                .contains("video M01 Video: Hệ Hy-Brid 16 kWp 3 pha");
        verify(messenger).sendText("psid-1", NUDGE, false);
        verify(messenger).sendText("psid-1", VIDEO_URL, false);

        Map<String, Object> f = jdbc.queryForMap("SELECT * FROM lich_nhac_khach WHERE hoi_thoai_id = ?", conversation);
        assertThat(f.get("trang_thai")).isEqualTo("SENT");
        assertThat(f.get("lan_thu")).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT trang_thai FROM tin_nhan WHERE id = ?", String.class,
                f.get("tin_nhan_id"))).isEqualTo("SENT");
        assertThat(jdbc.queryForObject("SELECT muc_dich FROM luot_goi_ai WHERE id = ?", String.class,
                f.get("luot_goi_ai_id"))).isEqualTo("FOLLOW_UP");
        assertThat(jdbc.queryForObject("SELECT han_tra_loi_luc FROM hoi_thoai WHERE id = ?", OffsetDateTime.class,
                conversation)).isNull();

        // Vừa nhắc xong: chưa tới lần sau
        assertThat(scheduler.scan(OffsetDateTime.now())).isZero();
        // Lùi tin nhắc về 3 giờ 10 phút trước: nhắc lần 2, video đã gửi nên không gửi lại
        jdbc.update("UPDATE tin_nhan SET gui_luc = now() - interval '190 minutes' WHERE id = ?", f.get("tin_nhan_id"));
        clearInvocations(messenger);
        assertThat(scheduler.scan(OffsetDateTime.now())).isEqualTo(1);
        turns.process(conversation);
        assertThat(jdbc.queryForObject("SELECT max(lan_thu) FROM lich_nhac_khach WHERE hoi_thoai_id = ?",
                Integer.class, conversation)).isEqualTo(2);
        verify(messenger).sendText("psid-1", NUDGE, false);
        verify(messenger, never()).sendText("psid-1", VIDEO_URL, false);
    }

    @Test
    void customerReplyCancelsPendingNudge() {
        assertThat(scheduler.scan(OffsetDateTime.now())).isEqualTo(1);

        conversations.recordInbound(new ConversationService.InboundMessage(ChannelCode.MESSENGER, "page-1",
                "psid-1", "m-new", "Anh đang xem", null, OffsetDateTime.now()));

        assertThat(followUp("trang_thai")).isEqualTo("CANCELLED");
        assertThat(followUp("ly_do")).isEqualTo("Khách nhắn lại");
    }

    @Test
    void emptyReplySkipsAndStopsNudgingThisWindow() {
        claudeReplies("");
        scheduler.scan(OffsetDateTime.now());
        turns.process(conversation);

        assertThat(followUp("trang_thai")).isEqualTo("SKIPPED");
        verify(messenger, never()).sendText(anyString(), anyString(), anyBoolean());
        assertThat(scheduler.scan(OffsetDateTime.now())).isZero();
    }

    @Test
    void autoSendOffKeepsNudgeAsDraftForStaff() {
        jdbc.update("INSERT INTO cai_dat_ai (khoa, gia_tri) VALUES ('follow_up_auto_send', 'false'::jsonb)");
        settings.reload();
        scheduler.scan(OffsetDateTime.now());
        turns.process(conversation);

        assertThat(followUp("trang_thai")).isEqualTo("DRAFT");
        assertThat(jdbc.queryForObject("SELECT trang_thai FROM tin_nhan WHERE id = ?", String.class,
                followUp("tin_nhan_id"))).isEqualTo("DRAFT");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM thong_bao WHERE loai = 'AI_DRAFT'", Integer.class))
                .isEqualTo(1);
        verify(messenger, never()).sendText(anyString(), anyString(), anyBoolean());
        // Nháp chờ duyệt: không lập thêm lịch
        jdbc.update("UPDATE tin_nhan SET tao_luc = now() - interval '190 minutes' WHERE id = ?",
                followUp("tin_nhan_id"));
        assertThat(scheduler.scan(OffsetDateTime.now())).isZero();
    }

    @Test
    void noNudgeWhenWindowNearlyClosedStaffOwnedOrKillSwitchOn() {
        jdbc.update("UPDATE hoi_thoai SET tin_khach_cuoi_luc = now() - interval '23 hours 45 minutes' WHERE id = ?",
                conversation);
        jdbc.update("UPDATE tin_nhan SET tao_luc = now() - interval '23 hours 44 minutes', "
                + "gui_luc = now() - interval '23 hours 44 minutes' WHERE hoi_thoai_id = ? AND chieu = 'OUT'",
                conversation);
        assertThat(scheduler.scan(OffsetDateTime.now())).isZero();

        UUID other = conversation(OffsetDateTime.now().minusHours(4), OffsetDateTime.now().minusMinutes(190));
        jdbc.update("UPDATE hoi_thoai SET nguoi_phu_trach = 'HUMAN_OWNED' WHERE id = ?", other);
        assertThat(scheduler.scan(OffsetDateTime.now())).isZero();

        jdbc.update("UPDATE hoi_thoai SET nguoi_phu_trach = 'AI_ACTIVE' WHERE id = ?", other);
        jdbc.update("INSERT INTO cai_dat_ai (khoa, gia_tri) VALUES ('kill_switch', '{\"on\": true}'::jsonb)");
        settings.reload();
        assertThat(scheduler.scan(OffsetDateTime.now())).isZero();
    }

    @Test
    void staffIsToldOnceWhenWindowClosesOnSilentLead() {
        jdbc.update("UPDATE hoi_thoai SET tin_khach_cuoi_luc = now() - interval '24 hours 10 minutes' WHERE id = ?",
                conversation);
        jdbc.update("UPDATE tin_nhan SET tao_luc = now() - interval '24 hours 9 minutes', "
                + "gui_luc = now() - interval '24 hours 9 minutes' WHERE hoi_thoai_id = ? AND chieu = 'OUT'",
                conversation);

        assertThat(scheduler.scan(OffsetDateTime.now())).isZero();
        scheduler.scan(OffsetDateTime.now());

        assertThat(jdbc.queryForObject("SELECT count(*) FROM thong_bao WHERE loai = 'CUSTOMER_SILENT'", Integer.class))
                .isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT loai FROM lich_nhac_khach WHERE hoi_thoai_id = ?", String.class,
                conversation)).isEqualTo("BAO_NHAN_VIEN");
    }

    /** Hội thoại có tin khách lúc {@code customerAt} và tin AI đề xuất gói 132 lúc {@code botAt} */
    private UUID conversation(OffsetDateTime customerAt, OffsetDateTime botAt) {
        String psid = jdbc.queryForObject("SELECT count(*) FROM khach_hang", Integer.class) == 0 ? "psid-1"
                : "psid-" + UUID.randomUUID();
        UUID customer = jdbc.queryForObject("INSERT INTO khach_hang DEFAULT VALUES RETURNING id", UUID.class);
        UUID identity = jdbc.queryForObject("""
                INSERT INTO dinh_danh_kenh (khach_hang_id, kenh, tai_khoan_id, id_nguoi_dung_ngoai, ten_hien_thi)
                VALUES (?, 'MESSENGER', 'page-1', ?, 'Anh Test') RETURNING id""", UUID.class, customer, psid);
        UUID id = jdbc.queryForObject("""
                INSERT INTO hoi_thoai (khach_hang_id, dinh_danh_kenh_id, kenh, tin_khach_cuoi_luc)
                VALUES (?, ?, 'MESSENGER', ?) RETURNING id""", UUID.class, customer, identity, customerAt);
        jdbc.update("""
                INSERT INTO tin_nhan (hoi_thoai_id, chieu, nguoi_gui, noi_dung, trang_thai, tao_luc, gui_luc)
                VALUES (?, 'IN', 'CUSTOMER', 'Nhà anh ở Bắc Ninh, 1 pha, mái tôn, tiền điện khoảng 3 triệu',
                        'PROCESSED', ?, ?)""", id, customerAt, customerAt);
        jdbc.update("""
                INSERT INTO tin_nhan (hoi_thoai_id, chieu, nguoi_gui, noi_dung, trang_thai, tao_luc, gui_luc)
                VALUES (?, 'OUT', 'AI', 'Dạ em đề xuất cho nhà mình gói Hybrid ạ. Anh thấy gói này thế nào ạ?',
                        'SENT', ?, ?)""", id, botAt, botAt);
        jdbc.update("INSERT INTO ho_so_tu_van (hoi_thoai_id, khach_hang_id, tron_goi_de_xuat_ids, so_cau_da_tra_loi) "
                + "VALUES (?, ?, '{132}', 3)", id, customer);
        return id;
    }

    private Object followUp(String column) {
        return jdbc.queryForMap("SELECT * FROM lich_nhac_khach WHERE hoi_thoai_id = ? AND loai = 'NHAC_KHACH' "
                + "ORDER BY tao_luc DESC LIMIT 1", conversation).get(column);
    }

    private void claudeReplies(String reply) {
        ObjectNode out = JSON.createObjectNode()
                .put("goal", "other")
                .put("intent", "TU_VAN_LAP_MOI")
                .put("reply", reply)
                .put("knowledge_gap", "");
        out.putArray("media");
        out.putArray("sources");
        out.putNull("collected");
        out.putNull("question_asked");
        out.putNull("qualification");
        out.putObject("handoff").put("needed", false).put("reason", "");
        when(claude.complete(any())).thenReturn(new ClaudeChatClient.ChatCompletion(out.toString(), "end_turn", null,
                "claude-sonnet-5-5", 100, 50, 0, 0, 10, 1));
    }
}
