package vn.slmglobal.slmai.services.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.assertj.core.api.Assertions.assertThat;

/** Vòng lặp tool của ClaudeChatClient trên server HTTP giả: request gửi đúng định dạng Messages API. */
class ClaudeChatClientToolLoopTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<JsonNode> requests = new ArrayList<>();
    private final Deque<String> responses = new ConcurrentLinkedDeque<>();
    private HttpServer server;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/messages", exchange -> {
            requests.add(objectMapper.readTree(exchange.getRequestBody()));
            byte[] body = responses.pop().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    @Test
    void runsToolThenReturnsFinalAnswer() throws Exception {
        responses.add(toolUseResponse());
        responses.add(finalResponse());
        List<String> handled = new ArrayList<>();
        ClaudeChatClient client = client(3);

        ClaudeChatClient.ChatCompletion c = client.complete(request((name, input) -> {
            handled.add(name + ":" + input.path("khu_vuc").asText());
            return new ClaudeChatClient.ToolOutput("{\"gia\":\"144,2 triệu đồng\"}", false);
        }));

        assertThat(handled).containsExactly("tim_goi_phu_hop:Hà Nội");
        assertThat(c.apiCalls()).isEqualTo(2);
        assertThat(c.stopReason()).isEqualTo("end_turn");
        assertThat(c.text()).isEqualTo("{\"reply\":\"ok\"}");
        assertThat(c.inputTokens()).isEqualTo(300);
        assertThat(c.outputTokens()).isEqualTo(70);
        assertThat(c.cacheReadTokens()).isEqualTo(150);

        JsonNode first = requests.get(0);
        assertThat(first.path("tools").get(0).path("name").asText()).isEqualTo("tim_goi_phu_hop");
        assertThat(first.path("tools").get(0).path("strict").asBoolean()).isTrue();
        assertThat(first.path("tools").get(0).path("input_schema").path("additionalProperties").asBoolean()).isFalse();
        assertThat(first.has("tool_choice")).isFalse();

        JsonNode messages = requests.get(1).path("messages");
        assertThat(messages).hasSize(3);
        JsonNode assistant = messages.get(1);
        assertThat(assistant.path("role").asText()).isEqualTo("assistant");
        assertThat(assistant.path("content").toString()).contains("\"type\":\"thinking\"", "\"signature\":\"sig-1\"",
                "\"type\":\"tool_use\"", "toolu_1");
        JsonNode result = messages.get(2).path("content").get(0);
        assertThat(result.path("type").asText()).isEqualTo("tool_result");
        assertThat(result.path("tool_use_id").asText()).isEqualTo("toolu_1");
        assertThat(result.path("content").toString()).contains("144,2 triệu đồng");
        assertThat(result.path("is_error").asBoolean()).isFalse();
    }

    @Test
    void lastRoundDisablesTools() {
        responses.add(toolUseResponse());
        responses.add(finalResponse());
        ClaudeChatClient.ChatCompletion c = client(1).complete(request(
                (name, input) -> new ClaudeChatClient.ToolOutput("{}", false)));

        assertThat(c.apiCalls()).isEqualTo(2);
        assertThat(requests.get(0).has("tool_choice")).isFalse();
        assertThat(requests.get(1).path("tool_choice").path("type").asText()).isEqualTo("none");
    }

    @Test
    void noToolsMeansSingleRequest() {
        responses.add(finalResponse());
        ClaudeChatClient.ChatCompletion c = client(3).complete(new ClaudeChatClient.ChatRequest("hệ thống", "ngữ cảnh",
                List.of(new ClaudeChatClient.ChatTurn(true, "Chào em")), Map.of("type", "object")));
        assertThat(c.apiCalls()).isEqualTo(1);
        assertThat(requests.get(0).has("tools")).isFalse();
    }

    private ClaudeChatClient.ChatRequest request(ClaudeChatClient.ToolHandler handler) {
        CustomerChatTools tools = new CustomerChatTools(null, objectMapper);
        return new ClaudeChatClient.ChatRequest("hệ thống", "ngữ cảnh",
                List.of(new ClaudeChatClient.ChatTurn(true, "Lắp hết bao nhiêu em?")),
                Map.of("type", "object")).withTools(tools.definitions(), handler);
    }

    private ClaudeChatClient client(int maxToolRounds) {
        AiProperties p = new AiProperties("test-key", "claude-sonnet-5-5", "low", 2000, Duration.ofSeconds(10),
                false, false, false, 1, 40, 3, Duration.ofSeconds(1), Duration.ofMinutes(1), false, "SolarMax",
                "Huyền", List.of(), List.of(), null, null, maxToolRounds,
                "http://127.0.0.1:" + server.getAddress().getPort());
        return new ClaudeChatClient(p);
    }

    private static String toolUseResponse() {
        return """
                {"id":"msg_1","type":"message","role":"assistant","model":"claude-sonnet-5-5",
                 "content":[{"type":"thinking","thinking":"cần tra giá","signature":"sig-1"},
                            {"type":"tool_use","id":"toolu_1","name":"tim_goi_phu_hop",
                             "input":{"khu_vuc":"Hà Nội","so_pha":1,"hoa_don_thang":2000000,"ty_le_ngay_dem":null,
                                      "loai_mai":null,"dien_tich_mai":null,"loai_he_thong":null,
                                      "can_du_phong_mat_dien":null}}],
                 "stop_reason":"tool_use","stop_sequence":null,
                 "usage":{"input_tokens":100,"output_tokens":30,"cache_read_input_tokens":50,
                          "cache_creation_input_tokens":0}}""";
    }

    private static String finalResponse() {
        return """
                {"id":"msg_2","type":"message","role":"assistant","model":"claude-sonnet-5-5",
                 "content":[{"type":"text","text":"{\\"reply\\":\\"ok\\"}"}],
                 "stop_reason":"end_turn","stop_sequence":null,
                 "usage":{"input_tokens":200,"output_tokens":40,"cache_read_input_tokens":100,
                          "cache_creation_input_tokens":0}}""";
    }
}
