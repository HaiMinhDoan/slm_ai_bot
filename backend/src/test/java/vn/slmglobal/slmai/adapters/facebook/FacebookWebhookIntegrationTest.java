package vn.slmglobal.slmai.adapters.facebook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import vn.slmglobal.slmai.services.conversation.WebhookEventWorker;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Luồng webhook Messenger trên PostgreSQL thật (Testcontainers), gồm cả migration Flyway V1–V3:
 * webhook → su_kien_webhook → worker → khach_hang / dinh_danh_kenh / hoi_thoai / tin_nhan.
 */
@Testcontainers
@SpringBootTest(properties = {
        "app.jwt.signer-key=test-signer-key-0123456789-abcdefghijklmnop",
        "app.facebook.app-id=APP_SELF",
        "app.facebook.app-secret=test-secret",
        "app.facebook.verify-token=vt-123",
        "app.facebook.page-id=PAGE_1",
        "app.conversation.worker-enabled=false"
})
@AutoConfigureMockMvc
class FacebookWebhookIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final String URL = "/api/v1/webhooks/facebook";

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    WebhookEventWorker worker;
    @Autowired
    FacebookSignatureVerifier verifier;

    @BeforeEach
    void cleanDatabase() {
        jdbc.execute("TRUNCATE su_kien_webhook, tin_nhan, ho_so_tu_van, khach_tiem_nang, hoi_thoai, "
                + "dinh_danh_kenh, khach_hang_lich_su_gop, khach_hang RESTART IDENTITY CASCADE");
    }

    // ---------- xác minh webhook ----------

    @Test
    void verifyReturnsChallengeWhenTokenMatches() throws Exception {
        mvc.perform(get(URL).param("hub.mode", "subscribe").param("hub.verify_token", "vt-123")
                        .param("hub.challenge", "CHALLENGE_42"))
                .andExpect(status().isOk())
                .andExpect(content().string("CHALLENGE_42"));
    }

    @Test
    void verifyRejectsWrongToken() throws Exception {
        mvc.perform(get(URL).param("hub.mode", "subscribe").param("hub.verify_token", "sai")
                        .param("hub.challenge", "X"))
                .andExpect(status().isForbidden());
    }

    // ---------- nhận sự kiện ----------

    @Test
    void rejectsWrongSignatureAndStoresNothing() throws Exception {
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .header("X-Hub-Signature-256", "sha256=" + "00".repeat(32))
                        .content(inbound("m_1", "Xin chào")))
                .andExpect(status().isForbidden());
        assertThat(count("su_kien_webhook")).isZero();
    }

    @Test
    void inboundMessageCreatesCustomerConversationAndPendingAiTurn() throws Exception {
        send(inbound("m_1", "Nhà em tiền điện 2 triệu, lắp hết bao nhiêu?"));
        send(inbound("m_1", "Nhà em tiền điện 2 triệu, lắp hết bao nhiêu?")); // Meta gửi lại: không ghi trùng
        assertThat(count("su_kien_webhook")).isEqualTo(1);

        assertThat(worker.drain()).isEqualTo(1);

        assertThat(count("khach_hang")).isEqualTo(1);
        Map<String, Object> identity = jdbc.queryForMap("SELECT * FROM dinh_danh_kenh");
        assertThat(identity.get("kenh")).isEqualTo("MESSENGER");
        assertThat(identity.get("tai_khoan_id")).isEqualTo("PAGE_1");
        assertThat(identity.get("id_nguoi_dung_ngoai")).isEqualTo("PSID_1");
        assertThat(identity.get("tin_den_cuoi_luc")).isNotNull();

        Map<String, Object> conversation = jdbc.queryForMap("SELECT * FROM hoi_thoai");
        assertThat(conversation.get("nguoi_phu_trach")).isEqualTo("AI_ACTIVE");
        assertThat(conversation.get("han_tra_loi_luc")).as("đã đặt hạn gộp tin").isNotNull();

        Map<String, Object> message = jdbc.queryForMap("SELECT * FROM tin_nhan");
        assertThat(message.get("chieu")).isEqualTo("IN");
        assertThat(message.get("nguoi_gui")).isEqualTo("CUSTOMER");
        assertThat(message.get("id_tin_nhan_ngoai")).isEqualTo("m_1");
        assertThat(message.get("noi_dung")).isEqualTo("Nhà em tiền điện 2 triệu, lắp hết bao nhiêu?");
        assertThat(jdbc.queryForObject("SELECT trang_thai FROM su_kien_webhook", String.class)).isEqualTo("PROCESSED");
    }

    @Test
    void secondMessageReusesConversation() throws Exception {
        send(inbound("m_1", "Chào em"));
        send(inbound("m_2", "Cho anh hỏi giá"));
        assertThat(worker.drain()).isEqualTo(2);

        assertThat(count("hoi_thoai")).isEqualTo(1);
        assertThat(count("tin_nhan")).isEqualTo(2);
    }

    @Test
    void echoOfOwnAppIsIgnored() throws Exception {
        send(inbound("m_1", "Chào em"));
        send(echo("m_out_1", "APP_SELF", "Dạ em chào anh"));
        worker.drain();

        assertThat(jdbc.queryForObject("SELECT nguoi_phu_trach FROM hoi_thoai", String.class)).isEqualTo("AI_ACTIVE");
        assertThat(count("tin_nhan")).as("tin OUT của app được ghi lúc gửi, không ghi lại từ echo").isEqualTo(1);
        assertThat(jdbc.queryForObject(
                "SELECT trang_thai FROM su_kien_webhook WHERE ma_su_kien = 'msg:m_out_1'", String.class))
                .isEqualTo("IGNORED");
    }

    @Test
    void staffReplyFromPageInboxHandsConversationToHuman() throws Exception {
        send(inbound("m_1", "Chào em"));
        send(echo("m_staff_1", "263902037430900", "Dạ anh để lại số giúp em nhé"));
        worker.drain();

        Map<String, Object> conversation = jdbc.queryForMap("SELECT * FROM hoi_thoai");
        assertThat(conversation.get("nguoi_phu_trach")).isEqualTo("HUMAN_OWNED");
        assertThat(conversation.get("tiep_quan_luc")).isNotNull();
        assertThat(conversation.get("han_tra_loi_luc")).as("lượt AI đang chờ bị huỷ").isNull();

        Map<String, Object> staffMessage = jdbc.queryForMap("SELECT * FROM tin_nhan WHERE chieu = 'OUT'");
        assertThat(staffMessage.get("nguoi_gui")).isEqualTo("STAFF");
        assertThat(staffMessage.get("trang_thai")).isEqualTo("SENT");
    }

    @Test
    void customerMessageWhileHumanOwnedDoesNotScheduleAi() throws Exception {
        send(inbound("m_1", "Chào em"));
        send(echo("m_staff_1", "263902037430900", "Dạ em nghe"));
        worker.drain();
        send(inbound("m_2", "Anh muốn khảo sát mái"));
        worker.drain();

        assertThat(jdbc.queryForObject("SELECT han_tra_loi_luc IS NULL FROM hoi_thoai", Boolean.class)).isTrue();
        assertThat(count("tin_nhan")).isEqualTo(3);
    }

    @Test
    void deliveryAndOtherPageEventsAreIgnored() throws Exception {
        send("""
                {"object":"page","entry":[
                  {"id":"PAGE_1","time":1,"messaging":[
                    {"sender":{"id":"PSID_1"},"recipient":{"id":"PAGE_1"},"timestamp":1760000000000,
                     "delivery":{"mids":["m_x"],"watermark":1760000000000}}]},
                  {"id":"PAGE_OTHER","time":1,"messaging":[
                    {"sender":{"id":"PSID_9"},"recipient":{"id":"PAGE_OTHER"},"timestamp":1760000000000,
                     "message":{"mid":"m_other","text":"hi"}}]}
                ]}
                """);
        assertThat(worker.drain()).isEqualTo(2);

        assertThat(count("khach_hang")).isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM su_kien_webhook WHERE trang_thai = 'IGNORED'", Integer.class))
                .isEqualTo(2);
    }

    @Test
    void postbackIsRecordedAsCustomerMessage() throws Exception {
        send("""
                {"object":"page","entry":[{"id":"PAGE_1","time":1,"messaging":[
                  {"sender":{"id":"PSID_1"},"recipient":{"id":"PAGE_1"},"timestamp":1760000000000,
                   "postback":{"mid":"m_pb_1","title":"Tư vấn lắp đặt","payload":"TU_VAN"}}]}]}
                """);
        worker.drain();

        Map<String, Object> message = jdbc.queryForMap("SELECT * FROM tin_nhan");
        assertThat(message.get("noi_dung")).isEqualTo("Tư vấn lắp đặt");
        assertThat(message.get("dinh_kem").toString()).contains("TU_VAN");
    }

    // ---------- tiện ích ----------

    private void send(String json) throws Exception {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        mvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON)
                        .header("X-Hub-Signature-256", "sha256=" + HexFormat.of().formatHex(verifier.sign(body)))
                        .content(body))
                .andExpect(status().isOk());
    }

    private int count(String table) {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM " + table, Integer.class);
        return n == null ? 0 : n;
    }

    private static String inbound(String mid, String text) {
        return """
                {"object":"page","entry":[{"id":"PAGE_1","time":1760000000000,"messaging":[
                  {"sender":{"id":"PSID_1"},"recipient":{"id":"PAGE_1"},"timestamp":1760000000000,
                   "message":{"mid":"%s","text":"%s"}}]}]}
                """.formatted(mid, text);
    }

    /** Echo: sender là Page, recipient là khách */
    private static String echo(String mid, String appId, String text) {
        return """
                {"object":"page","entry":[{"id":"PAGE_1","time":1760000001000,"messaging":[
                  {"sender":{"id":"PAGE_1"},"recipient":{"id":"PSID_1"},"timestamp":1760000001000,
                   "message":{"is_echo":true,"app_id":%s,"mid":"%s","text":"%s"}}]}]}
                """.formatted(appId.chars().allMatch(Character::isDigit) ? appId : "\"" + appId + "\"", mid, text);
    }
}
