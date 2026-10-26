package vn.slmglobal.slmai.services.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import vn.slmglobal.slmai.common.security.AuthInfo;
import vn.slmglobal.slmai.common.security.JwtService;
import vn.slmglobal.slmai.common.security.RoleCode;
import vn.slmglobal.slmai.services.auth.UserAdminService;
import vn.slmglobal.slmai.services.auth.dto.CreateUserRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Thông báo nhân viên trên PostgreSQL thật: chống trùng, rollback, quyền xem, đã đọc, xác thực stream SSE. */
@Testcontainers
@SpringBootTest(properties = {
        "app.jwt.signer-key=test-signer-key-0123456789-abcdefghijklmnop",
        "app.conversation.worker-enabled=false",
        "app.ai.worker-enabled=false"
})
@AutoConfigureMockMvc
class NotificationIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final String URL = "/api/v1/notifications";

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    NotificationService notificationService;
    @Autowired
    TransactionTemplate tx;
    @Autowired
    UserAdminService users;
    @Autowired
    JwtService jwt;
    @Autowired
    NotificationSseHub hub;
    @Autowired
    ObjectMapper objectMapper;

    private UUID staffId;
    private String staffToken;
    private String otherStaffToken;
    private UUID otherStaffId;
    private String editorToken;

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE thong_bao, thong_bao_da_doc RESTART IDENTITY CASCADE");
        staffId = createUser(RoleCode.STAFF);
        staffToken = token(staffId, RoleCode.STAFF);
        otherStaffId = createUser(RoleCode.STAFF);
        otherStaffToken = token(otherStaffId, RoleCode.STAFF);
        editorToken = token(createUser(RoleCode.KNOWLEDGE_EDITOR), RoleCode.KNOWLEDGE_EDITOR);
    }

    // ---------- ghi thông báo ----------

    @Test
    void sameDedupeKeyTwiceKeepsOneRowAndDoesNotAbortTransaction() {
        tx.executeWithoutResult(st -> {
            notificationService.notify(draft("K1", null));
            notificationService.notify(draft("K1", null));
            // Transaction vẫn dùng được sau lần trùng khoá (ON CONFLICT DO NOTHING, không phải lỗi bị bắt)
            jdbc.update("INSERT INTO khach_hang (ten) VALUES ('khach-test-k1')");
        });

        assertThat(count("SELECT count(*) FROM thong_bao WHERE khoa_chong_trung = 'K1'")).isEqualTo(1);
        assertThat(count("SELECT count(*) FROM khach_hang WHERE ten = 'khach-test-k1'")).isEqualTo(1);
    }

    @Test
    void rollbackLeavesNoNotification() {
        tx.executeWithoutResult(st -> {
            notificationService.notify(draft("K2", null));
            st.setRollbackOnly();
        });

        assertThat(count("SELECT count(*) FROM thong_bao")).isZero();
    }

    @Test
    void notifyRequiresBusinessTransaction() {
        assertThatThrownBy(() -> notificationService.notify(draft("K3", null)))
                .isInstanceOf(IllegalTransactionStateException.class);
    }

    // ---------- REST ----------

    @Test
    void listShowsBroadcastAndOwnTargetedOnly() throws Exception {
        tx.executeWithoutResult(st -> {
            notificationService.notify(draft("A", null));
            notificationService.notify(draft("B", null));
            notificationService.notify(draft("C", staffId));
            notificationService.notify(draft("D", otherStaffId));
        });

        JsonNode page = data(mvc.perform(get(URL).header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk()).andReturn());
        assertThat(page.path("total").asLong()).isEqualTo(3);
        assertThat(page.path("items").get(0).path("read").asBoolean()).isFalse();
        // Chi tiết có data (nháp, số điện thoại) chỉ trả qua REST
        assertThat(page.path("items").get(0).path("data").path("draftText").asText()).isEqualTo("nháp");

        JsonNode other = data(mvc.perform(get(URL).header("Authorization", "Bearer " + otherStaffToken))
                .andExpect(status().isOk()).andReturn());
        assertThat(other.path("total").asLong()).isEqualTo(3);
    }

    @Test
    void markReadAndReadAllUpToMaxId() throws Exception {
        tx.executeWithoutResult(st -> {
            for (String k : List.of("R1", "R2", "R3")) {
                notificationService.notify(draft(k, null));
            }
        });
        List<Long> ids = jdbc.queryForList("SELECT id FROM thong_bao ORDER BY id", Long.class);

        mvc.perform(post(URL + "/" + ids.get(0) + "/read").header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk());
        assertThat(unread(staffToken)).isEqualTo(2);

        mvc.perform(post(URL + "/read-all").param("maxId", String.valueOf(ids.get(1)))
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk());
        assertThat(unread(staffToken)).as("thông báo sau maxId vẫn chưa đọc").isEqualTo(1);
        assertThat(unread(otherStaffToken)).as("trạng thái đọc theo từng người").isEqualTo(3);
    }

    @Test
    void cannotReadOthersTargetedNotification() throws Exception {
        tx.executeWithoutResult(st -> notificationService.notify(draft("T", otherStaffId)));
        long id = jdbc.queryForObject("SELECT id FROM thong_bao", Long.class);

        mvc.perform(get(URL + "/" + id).header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isNotFound());
        mvc.perform(post(URL + "/" + id + "/read").header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isNotFound());
    }

    // ---------- stream ----------

    @Test
    void streamRequiresStaffRole() throws Exception {
        mvc.perform(get(URL + "/stream")).andExpect(status().isUnauthorized());
        mvc.perform(get(URL + "/stream").header("Authorization", "Bearer " + editorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void streamOpensAsyncAndSendsReady() throws Exception {
        int before = hub.openConnections();
        MvcResult result = mvc.perform(get(URL + "/stream").header("Authorization", "Bearer " + staffToken))
                .andExpect(request().asyncStarted())
                .andReturn();

        assertThat(hub.openConnections()).isEqualTo(before + 1);
        assertThat(result.getResponse().getHeader("X-Accel-Buffering")).isEqualTo("no");
        assertThat(result.getResponse().getContentAsString()).contains("event:ready");
    }

    // ---------- tiện ích ----------

    private NotificationDraft draft(String key, UUID recipient) {
        return NotificationDraft.builder()
                .type(NotificationType.AI_DRAFT)
                .severity(NotificationSeverity.WARNING)
                .title("Nháp AI chờ duyệt: " + key)
                .body("Chưa gửi khách vì loại câu hỏi không được gửi tự động.")
                .data(Map.of("draftText", "nháp", "phone", "0912345678"))
                .recipientId(recipient)
                .dedupeKey(key)
                .build();
    }

    private UUID createUser(String role) {
        String email = "u-" + UUID.randomUUID() + "@test.local";
        return users.create(null, new CreateUserRequest(email, "Người thử", "Test@123456789", List.of(role))).id();
    }

    private String token(UUID userId, String role) {
        return jwt.generateAccessToken(new AuthInfo(userId, "x@test.local", "Người thử", Set.of(role)));
    }

    private long unread(String token) throws Exception {
        return data(mvc.perform(get(URL + "/unread-count").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn()).path("count").asLong();
    }

    private JsonNode data(MvcResult r) throws Exception {
        return objectMapper.readTree(r.getResponse().getContentAsString()).path("data");
    }

    private long count(String sql) {
        Long n = jdbc.queryForObject(sql, Long.class);
        return n == null ? 0 : n;
    }
}
