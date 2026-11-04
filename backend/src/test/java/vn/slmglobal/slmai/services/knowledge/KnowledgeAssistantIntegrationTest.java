package vn.slmglobal.slmai.services.knowledge;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
import vn.slmglobal.slmai.services.knowledge.admin.KnowledgeAdminService;
import vn.slmglobal.slmai.services.knowledge.assistant.KnowledgeAssistantService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** AI hỗ trợ soạn (mục 5.10 B) với máy chủ Claude giả trả streaming: đề xuất → kiểm tra → duyệt thành bản nháp. */
@Testcontainers
@SpringBootTest(properties = {
        "app.jwt.signer-key=test-signer-key-0123456789-abcdefghijklmnop",
        "app.conversation.worker-enabled=false",
        "app.ai.worker-enabled=false",
        "app.ai.api-key=test-key"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KnowledgeAssistantIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final List<JsonNode> REQUESTS = new CopyOnWriteArrayList<>();
    private static final HttpServer SERVER = fakeClaude();
    private static final AuthInfo ADMIN = new AuthInfo(null, "admin@test.local", "Quản trị",
            Set.of(RoleCode.CONTENT_ADMIN));

    @DynamicPropertySource
    static void claude(DynamicPropertyRegistry r) {
        r.add("app.ai.base-url", () -> "http://localhost:" + SERVER.getAddress().getPort());
    }

    @AfterAll
    static void stop() {
        SERVER.stop(0);
    }

    @Autowired
    KnowledgeAssistantService assistant;
    @Autowired
    KnowledgeAdminService admin;
    @Autowired
    JdbcTemplate jdbc;

    @Test
    @Order(1)
    void proposalsAreCheckedAndAcceptedAsDraft() throws Exception {
        KnowledgeAssistantService.SessionView s = assistant.start(new KnowledgeAssistantService.StartRequest(null,
                null), ADMIN);
        KnowledgeAssistantService.SessionView sent = assistant.send(s.id(), new KnowledgeAssistantService.SendRequest(
                "Mái bằng thì báo khách phát sinh phần khung, nhân viên báo giá sau khảo sát", null), ADMIN);
        assertThat(sent.messages()).hasSize(1);

        KnowledgeAssistantService.SessionView done = waitDone(s.id());
        assertThat(done.messages()).extracting(KnowledgeAssistantService.MessageView::role)
                .containsExactly("USER", "ASSISTANT");
        assertThat(done.messages().get(1).text()).contains("Em đề xuất");
        assertThat(done.proposals()).hasSize(2);

        KnowledgeAssistantService.ProposalView good = done.proposals().stream()
                .filter(p -> p.docKey().equals("playbooks/khach-hoi-gia")).findFirst().orElseThrow();
        assertThat(good.action()).isEqualTo("UPDATE");
        assertThat(good.canAccept()).isTrue();
        assertThat(good.diff()).contains("+## Mái bằng");
        KnowledgeAssistantService.ProposalView bad = done.proposals().stream()
                .filter(p -> p.docKey().equals("rules/bao-gia")).findFirst().orElseThrow();
        assertThat(bad.canAccept()).isFalse();
        assertThatThrownBy(() -> assistant.accept(bad.id(), false, ADMIN)).isInstanceOf(CommonException.class);

        // Request gửi Claude: model AI hỗ trợ soạn, khuôn kết quả, kiến thức đang chạy trong khối cache
        JsonNode req = REQUESTS.get(0);
        assertThat(req.path("model").asText()).isEqualTo("claude-opus-5-5");
        assertThat(req.path("stream").asBoolean()).isTrue();
        assertThat(req.path("system").get(0).path("text").asText())
                .contains("<tai_lieu ma=\"playbooks/khach-hoi-gia\"", "<tai_lieu ma=\"guide/huong-dan-viet\"");
        assertThat(req.path("system").get(0).has("cache_control")).isTrue();
        assertThat(req.path("output_config").path("format").path("schema").path("required").toString())
                .contains("proposals");

        KnowledgeAssistantService.ProposalView accepted = assistant.accept(good.id(), false, ADMIN);
        assertThat(accepted.status()).isEqualTo("ACCEPTED");
        KnowledgeAdminService.DocDetail doc = admin.getByKey("playbooks/khach-hoi-gia");
        assertThat(doc.draft()).isNotNull();
        assertThat(doc.draft().aiAssisted()).isTrue();
        assertThat(doc.draft().content()).contains("## Mái bằng");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM luot_goi_ai WHERE muc_dich = 'KNOWLEDGE_ASSISTANT'",
                Integer.class)).isEqualTo(1);
    }

    @Test
    @Order(2)
    void onlyDraftsSavedAfterTheProposalNeedConfirmation() throws Exception {
        // Bản nháp có trước khi nhắn: AI đọc và soạn tiếp trên đó, duyệt thẳng không hỏi
        saveDraft("Bản nháp người soạn lưu trước khi nhờ AI");
        KnowledgeAssistantService.SessionView s = assistant.start(new KnowledgeAssistantService.StartRequest(null,
                null), ADMIN);
        assistant.send(s.id(), new KnowledgeAssistantService.SendRequest("Thêm mục mái bằng", null), ADMIN);
        KnowledgeAssistantService.ProposalView first = waitDone(s.id(), 2).proposals().stream()
                .filter(p -> p.docKey().equals("playbooks/khach-hoi-gia")).findFirst().orElseThrow();
        assertThat(assistant.accept(first.id(), false, ADMIN).status()).isEqualTo("ACCEPTED");

        // Bản nháp lưu sau khi AI đề xuất: duyệt sẽ mất phần AI chưa thấy nên phải xác nhận (force)
        assistant.send(s.id(), new KnowledgeAssistantService.SendRequest("Viết lại ngắn hơn", null), ADMIN);
        KnowledgeAssistantService.ProposalView second = waitDone(s.id(), 4).proposals().stream()
                .filter(p -> p.docKey().equals("playbooks/khach-hoi-gia")
                        && p.status().equals("PROPOSED")).findFirst().orElseThrow();
        saveDraft("Người khác vừa sửa tay sau khi AI đề xuất");
        assertThatThrownBy(() -> assistant.accept(second.id(), false, ADMIN))
                .isInstanceOfSatisfying(CommonException.class,
                        e -> assertThat(e.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT));
        assertThat(assistant.accept(second.id(), true, ADMIN).status()).isEqualTo("ACCEPTED");
    }

    private void saveDraft(String content) {
        KnowledgeAdminService.DocDetail d = admin.getByKey("playbooks/khach-hoi-gia");
        admin.saveDraft(d.doc().id(), new KnowledgeAdminService.SaveDraftRequest(d.doc().title(), content, null,
                null, null, null, true), ADMIN);
    }

    private KnowledgeAssistantService.SessionView waitDone(java.util.UUID id) throws InterruptedException {
        return waitDone(id, 2);
    }

    private KnowledgeAssistantService.SessionView waitDone(java.util.UUID id, int messageCount)
            throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            KnowledgeAssistantService.SessionView v = assistant.get(id, ADMIN);
            if (!v.pending() && v.messages().size() >= messageCount) {
                return v;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("AI hỗ trợ soạn không trả lời");
    }

    // ------------------------------------------------------------------ Claude giả (SSE)

    private static HttpServer fakeClaude() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/v1/messages", exchange -> {
                REQUESTS.add(JSON.readTree(exchange.getRequestBody()));
                String current = REQUESTS.get(0).path("system").get(0).path("text").asText();
                String body = sse(reply(current));
                exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
                exchange.sendResponseHeaders(200, 0);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(body.getBytes(StandardCharsets.UTF_8));
                }
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Đề xuất sửa playbook đang có (nội dung cũ + một mục) và một đề xuất sai (rule không sửa qua đây) */
    private static String reply(String system) throws IOException {
        int start = system.indexOf("<tai_lieu ma=\"playbooks/khach-hoi-gia\"");
        String block = system.substring(system.indexOf('\n', start) + 1, system.indexOf("</tai_lieu>", start)).strip();
        ObjectNode out = JSON.createObjectNode();
        out.put("reply", "Em đề xuất thêm mục mái bằng vào playbook khách hỏi giá.");
        ObjectNode p = out.putArray("proposals").addObject();
        p.put("action", "UPDATE").put("doc_key", "playbooks/khach-hoi-gia").put("doc_type", "PLAYBOOK")
                .put("title", "Playbook: Khách hỏi giá")
                .put("content", block + "\n\n## Mái bằng\n\nBáo khách mái bằng cần thêm phần khung; nhân viên báo giá "
                        + "phần khung sau khi khảo sát.\n")
                .put("change_summary", "Thêm mục mái bằng").put("reason", "Theo yêu cầu").putArray("conflicts");
        ObjectNode q = ((com.fasterxml.jackson.databind.node.ArrayNode) out.get("proposals")).addObject();
        q.put("action", "UPDATE").put("doc_key", "rules/bao-gia").put("doc_type", "RULE").put("title", "x")
                .put("content", "price_per_kwh: 4000").put("change_summary", "x").put("reason", "x")
                .putArray("conflicts");
        return JSON.writeValueAsString(out);
    }

    private static String sse(String text) throws IOException {
        StringBuilder sb = new StringBuilder();
        event(sb, "message_start", "{\"type\":\"message_start\",\"message\":{\"id\":\"msg_1\",\"type\":\"message\","
                + "\"role\":\"assistant\",\"model\":\"claude-opus-5-5\",\"content\":[],\"stop_reason\":null,"
                + "\"stop_sequence\":null,\"usage\":{\"input_tokens\":1200,\"output_tokens\":1}}}");
        event(sb, "content_block_start",
                "{\"type\":\"content_block_start\",\"index\":0,\"content_block\":{\"type\":\"text\",\"text\":\"\"}}");
        ObjectNode delta = JSON.createObjectNode();
        delta.put("type", "content_block_delta").put("index", 0);
        delta.putObject("delta").put("type", "text_delta").put("text", text);
        event(sb, "content_block_delta", JSON.writeValueAsString(delta));
        event(sb, "content_block_stop", "{\"type\":\"content_block_stop\",\"index\":0}");
        event(sb, "message_delta", "{\"type\":\"message_delta\",\"delta\":{\"stop_reason\":\"end_turn\","
                + "\"stop_sequence\":null},\"usage\":{\"output_tokens\":300}}");
        event(sb, "message_stop", "{\"type\":\"message_stop\"}");
        return sb.toString();
    }

    private static void event(StringBuilder sb, String name, String data) {
        sb.append("event: ").append(name).append('\n').append("data: ").append(data).append("\n\n");
    }
}
