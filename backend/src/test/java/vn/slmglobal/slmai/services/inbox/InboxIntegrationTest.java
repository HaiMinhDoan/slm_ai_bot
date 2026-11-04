package vn.slmglobal.slmai.services.inbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.common.security.AuthInfo;
import vn.slmglobal.slmai.common.security.RoleCode;
import vn.slmglobal.slmai.services.conversation.ConversationService;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.conversation.entity.Message;
import vn.slmglobal.slmai.services.conversation.entity.MessageDirection;
import vn.slmglobal.slmai.services.conversation.entity.MessageStatus;
import vn.slmglobal.slmai.services.conversation.entity.SenderType;
import vn.slmglobal.slmai.services.conversation.repository.MessageRepository;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Trang chat với khách: danh sách theo kênh, gửi / sửa / bỏ nháp qua Send API giả, chuyển nhân viên / AI. */
@Testcontainers
@SpringBootTest(properties = {
        "app.jwt.signer-key=test-signer-key-0123456789-abcdefghijklmnop",
        "app.conversation.worker-enabled=false",
        "app.ai.worker-enabled=false",
        "app.facebook.page-access-token=test-page-token"
})
class InboxIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final ObjectMapper JSON = new ObjectMapper();
    /** Tin Send API giả nhận được; tin có chữ "LOI" bị trả lỗi như Graph API */
    private static final List<JsonNode> SENT = new CopyOnWriteArrayList<>();
    private static final AtomicInteger IDS = new AtomicInteger();
    private static final HttpServer GRAPH = fakeGraph();
    private static final AuthInfo STAFF = new AuthInfo(null, "sale@test.local", "Sale Một", Set.of(RoleCode.STAFF));

    @DynamicPropertySource
    static void graph(DynamicPropertyRegistry r) {
        r.add("app.facebook.graph-api-base-url", () -> "http://localhost:" + GRAPH.getAddress().getPort());
    }

    @AfterAll
    static void stop() {
        GRAPH.stop(0);
    }

    @Autowired
    InboxService inbox;
    @Autowired
    ConversationService conversations;
    @Autowired
    MessageRepository messages;
    @Autowired
    JdbcTemplate jdbc;

    @Test
    void listsByChannelAndSendsEditedDraft() {
        UUID conv = customerSays("psid-1", "Lắp hệ 5kW hết bao nhiêu?");
        UUID draft = aiDraft(conv, "Dạ hệ hòa lưới từ 80 triệu ạ.");

        InboxRepository.Row row = inbox.list(new InboxRepository.Filter("MESSENGER", InboxRepository.View.DRAFT,
                null), 0, 20).items().stream().filter(r -> r.id().equals(conv)).findFirst().orElseThrow();
        assertThat(row.draftCount()).isEqualTo(1);
        assertThat(row.customerWaiting()).isTrue();
        assertThat(inbox.list(new InboxRepository.Filter("ZALO_OA", null, null), 0, 20).items()).isEmpty();
        assertThat(inbox.stats()).anySatisfy(s -> {
            assertThat(s.channel()).isEqualTo("MESSENGER");
            assertThat(s.drafts()).isGreaterThanOrEqualTo(1);
        });

        InboxService.Detail d = inbox.sendDraft(draft, new InboxService.SendDraftRequest(
                "Dạ hệ hòa lưới bên em có nhiều mức giá, anh cho em xin tiền điện mỗi tháng ạ?"), STAFF);
        InboxService.MessageView sent = d.messages().stream().filter(m -> m.id().equals(draft)).findFirst()
                .orElseThrow();
        assertThat(sent.status()).isEqualTo("SENT");
        assertThat(sent.staffId()).isEqualTo("sale@test.local");
        assertThat(SENT.get(SENT.size() - 1).path("message").path("text").asText()).startsWith("Dạ hệ hòa lưới bên em");
        assertThat(SENT.get(SENT.size() - 1).path("messaging_type").asText()).isEqualTo("RESPONSE");
        assertThat(jdbc.queryForObject("SELECT noi_dung_ai FROM kt_sua_ban_nhap WHERE tin_nhan_id = ?",
                String.class, draft)).isEqualTo("Dạ hệ hòa lưới từ 80 triệu ạ.");
        assertThat(inbox.list(new InboxRepository.Filter("MESSENGER", null, "psid"), 0, 20).items()).isEmpty();

        // Đã gửi rồi thì không gửi lại được (hai nhân viên bấm cùng lúc)
        assertThatThrownBy(() -> inbox.sendDraft(draft, null, STAFF)).isInstanceOfSatisfying(CommonException.class,
                e -> assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void failedSendKeepsDraftAndClosedWindowBlocks() {
        UUID conv = customerSays("psid-2", "Chào shop");
        UUID draft = aiDraft(conv, "LOI: tin này Send API giả sẽ từ chối");
        int before = SENT.size();
        assertThatThrownBy(() -> inbox.sendDraft(draft, null, STAFF)).isInstanceOfSatisfying(CommonException.class,
                e -> assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.BAD_GATEWAY));
        assertThat(messages.findById(draft).orElseThrow().getStatus()).isEqualTo(MessageStatus.DRAFT);

        // Quá 24 giờ kể từ tin cuối của khách: không gọi Send API
        jdbc.update("UPDATE hoi_thoai SET tin_khach_cuoi_luc = now() - interval '2 days' WHERE id = ?", conv);
        assertThat(inbox.detail(conv).window().open()).isFalse();
        assertThatThrownBy(() -> inbox.sendDraft(draft, new InboxService.SendDraftRequest("Dạ em chào anh"), STAFF))
                .isInstanceOfSatisfying(CommonException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
        assertThat(SENT.size()).isEqualTo(before + 1);

        InboxService.Detail d = inbox.discardDraft(draft, STAFF);
        assertThat(d.messages()).filteredOn(m -> m.id().equals(draft)).extracting(InboxService.MessageView::status)
                .containsExactly("DISCARDED");
    }

    @Test
    void handToStaffThenBackToAi() {
        UUID conv = customerSays("psid-3", "Cho em gặp nhân viên");
        UUID draft = aiDraft(conv, "Dạ em chuyển nhân viên ạ.");

        InboxService.Detail d = inbox.toStaff(conv, new InboxService.ToStaffRequest("Khách muốn gặp người"), STAFF);
        assertThat(d.conversation().ownership()).isEqualTo("HUMAN_OWNED");
        assertThat(count("STAFF_HANDOFF", conv)).isEqualTo(1);
        assertThatThrownBy(() -> inbox.toStaff(conv, null, STAFF)).isInstanceOf(CommonException.class);

        // Nhân viên gửi nháp; sau đó khách nhắn: báo một lần cho cả đợt chờ, AI không chen vào
        inbox.sendDraft(draft, null, STAFF);
        assertThat(jdbc.queryForObject("SELECT nhan_vien_tin_cuoi_luc FROM hoi_thoai WHERE id = ?",
                OffsetDateTime.class, conv)).isNotNull();
        customerSays("psid-3", "Alo shop ơi");
        customerSays("psid-3", "Có ai không ạ");
        assertThat(count("CUSTOMER_WAITING", conv)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT han_tra_loi_luc FROM hoi_thoai WHERE id = ?", OffsetDateTime.class,
                conv)).isNull();

        // aiDraft đánh dấu mọi tin của khách đã xử lý, như sau một lượt AI chỉ lưu nháp
        UUID stale = aiDraft(conv, "Nháp cũ");
        d = inbox.toAi(conv, STAFF);
        assertThat(d.conversation().ownership()).isEqualTo("AI_ACTIVE");
        assertThat(d.conversation().aiReplying()).isTrue();
        assertThat(messages.findById(stale).orElseThrow().getStatus()).isEqualTo(MessageStatus.DISCARDED);
        // Hai tin nhắn sau tin trang đã gửi là tin chờ để AI trả lời ngay; tin trước đó coi như đã xử lý
        assertThat(jdbc.queryForList("SELECT noi_dung FROM tin_nhan WHERE hoi_thoai_id = ? AND chieu = 'IN' "
                + "AND trang_thai = 'RECEIVED' ORDER BY tao_luc", String.class, conv))
                .containsExactly("Alo shop ơi", "Có ai không ạ");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM nhat_ky_thao_tac WHERE hanh_dong IN "
                + "('conversation.to_staff', 'conversation.to_ai')", Integer.class)).isGreaterThanOrEqualTo(2);
    }

    private UUID customerSays(String psid, String text) {
        return conversations.recordInbound(new ConversationService.InboundMessage(ChannelCode.MESSENGER, "page-1",
                psid, "mid." + UUID.randomUUID(), text, null, OffsetDateTime.now())).conversationId();
    }

    private UUID aiDraft(UUID conversationId, String text) {
        jdbc.update("UPDATE tin_nhan SET trang_thai = 'PROCESSED' WHERE hoi_thoai_id = ? AND chieu = 'IN'",
                conversationId);
        return messages.save(Message.builder().conversationId(conversationId).direction(MessageDirection.OUT)
                .senderType(SenderType.AI).content(text).status(MessageStatus.DRAFT).build()).getId();
    }

    private int count(String type, UUID conversationId) {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM thong_bao WHERE loai = ? AND hoi_thoai_id = ?",
                Integer.class, type, conversationId);
        return n == null ? 0 : n;
    }

    // ------------------------------------------------------------------ Graph API giả

    private static HttpServer fakeGraph() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/", exchange -> {
                JsonNode body = JSON.readTree(exchange.getRequestBody());
                boolean fail = body.path("message").path("text").asText().contains("LOI");
                SENT.add(body);
                String out = fail
                        ? "{\"error\":{\"message\":\"(#100) Lỗi giả\",\"type\":\"OAuthException\",\"code\":100}}"
                        : "{\"recipient_id\":\"" + body.path("recipient").path("id").asText()
                        + "\",\"message_id\":\"mid.out." + IDS.incrementAndGet() + "\"}";
                byte[] bytes = out.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(fail ? 400 : 200, bytes.length);
                try (OutputStream o = exchange.getResponseBody()) {
                    o.write(bytes);
                }
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
