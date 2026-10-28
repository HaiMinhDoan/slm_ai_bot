package vn.slmglobal.slmai.services.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.JsonOutputFormat;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.RefusalStopDetails;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.TextBlock;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.ThinkingConfigAdaptive;
import com.anthropic.models.messages.Tool;
import com.anthropic.models.messages.ToolChoiceNone;
import com.anthropic.models.messages.ToolResultBlockParam;
import com.anthropic.models.messages.ToolUseBlock;
import com.anthropic.models.messages.Usage;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gọi Claude Messages API cho một lượt trả lời khách, kết quả có cấu trúc (mục 18). Có tool (báo giá, mục 4.2.6)
 * thì chạy vòng lặp: Claude gọi tool → backend trả kết quả → Claude trả lời; tối đa {@code app.ai.max-tool-rounds}
 * vòng, vòng cuối tắt tool để Claude buộc phải trả lời.
 *
 * <ul>
 *   <li>Khối hướng dẫn + kiến thức ổn định đặt trước, gắn cache 1 giờ (mục 5.4): lượng tin thấp, các lượt cách nhau
 *       lâu hơn 5 phút nên cache 1 giờ đỡ phải ghi lại nhiều lần.</li>
 *   <li>Ngữ cảnh từng lượt (thời điểm, hồ sơ tư vấn) đặt sau điểm cache để không làm hỏng cache.</li>
 *   <li>Suy nghĩ thích ứng (adaptive); độ sâu điều khiển bằng effort.</li>
 *   <li>Khi model từ chối do bộ lọc an toàn, API tự chạy lại trên model dự phòng (server-side fallback).</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class ClaudeChatClient implements DisposableBean {

    static final String FALLBACK_BETA = "server-side-fallback-2026-07-01";

    private final AiProperties properties;
    private volatile AnthropicClient client;

    /** Một lượt trong lịch sử: {@code customer} = true là tin của khách (role user), false là tin của trang. */
    public record ChatTurn(boolean customer, String text) {
    }

    /** Kết quả trả về cho Claude của một lần gọi tool */
    public record ToolOutput(String content, boolean error) {
    }

    /** Backend thực thi tool Claude gọi */
    @FunctionalInterface
    public interface ToolHandler {
        ToolOutput handle(String name, JsonNode input);
    }

    /** Một lần gọi tool, do ToolHandler ghi lại (luot_goi_ai.cac_lan_goi_tool) */
    public record ToolCall(String name, JsonNode input, String output, boolean error, int latencyMs) {
    }

    /**
     * @param tools       rỗng = không tool
     * @param toolHandler bắt buộc khi có tool
     */
    public record ChatRequest(String cachedSystem, String turnContext, List<ChatTurn> turns,
                              Map<String, Object> outputSchema, List<Tool> tools, ToolHandler toolHandler) {

        public ChatRequest(String cachedSystem, String turnContext, List<ChatTurn> turns,
                           Map<String, Object> outputSchema) {
            this(cachedSystem, turnContext, turns, outputSchema, List.of(), null);
        }

        public ChatRequest withTools(List<Tool> tools, ToolHandler handler) {
            return new ChatRequest(cachedSystem, turnContext, turns, outputSchema, tools, handler);
        }

        boolean hasTools() {
            return tools != null && !tools.isEmpty() && toolHandler != null;
        }
    }

    /**
     * Token và độ trễ cộng dồn qua mọi request của lượt (một request mỗi vòng tool).
     *
     * @param apiCalls số request đã gửi
     */
    public record ChatCompletion(String text, String stopReason, String refusalCategory, String model,
                                 Integer inputTokens, Integer outputTokens, Integer cacheReadTokens,
                                 Integer cacheWriteTokens, int latencyMs, int apiCalls) {

        public boolean refused() {
            return StopReason.REFUSAL.asString().equals(stopReason);
        }

        public boolean truncated() {
            return StopReason.MAX_TOKENS.asString().equals(stopReason);
        }
    }

    /** Lỗi gọi API (mạng, 429, 5xx...) ném ra nguyên dạng {@code AnthropicException} để tầng trên thử lại. */
    public ChatCompletion complete(ChatRequest request) {
        MessageCreateParams.Builder base = MessageCreateParams.builder()
                .model(properties.model())
                .maxTokens(properties.maxTokens())
                .thinking(ThinkingConfigAdaptive.builder().build())
                .outputConfig(OutputConfig.builder()
                        .effort(OutputConfig.Effort.of(properties.effort()))
                        .format(JsonOutputFormat.builder().schema(schemaOf(request.outputSchema())).build())
                        .build())
                .systemOfTextBlockParams(List.of(
                        TextBlockParam.builder()
                                .text(request.cachedSystem())
                                .cacheControl(CacheControlEphemeral.builder()
                                        .ttl(CacheControlEphemeral.Ttl.TTL_1H)
                                        .build())
                                .build(),
                        TextBlockParam.builder().text(request.turnContext()).build()));
        if (request.hasTools()) {
            request.tools().forEach(base::addTool);
        }
        if (properties.serverSideFallback()) {
            base.putAdditionalHeader("anthropic-beta", FALLBACK_BETA)
                    .putAdditionalBodyProperty("fallbacks", JsonValue.from("default"));
        }
        List<MessageParam> messages = new ArrayList<>();
        for (ChatTurn turn : request.turns()) {
            messages.add(MessageParam.builder()
                    .role(turn.customer() ? MessageParam.Role.USER : MessageParam.Role.ASSISTANT)
                    .content(turn.text())
                    .build());
        }
        base.messages(messages);
        MessageCreateParams first = base.build();

        long input = 0;
        long output = 0;
        Long cacheRead = null;
        Long cacheWrite = null;
        int apiCalls = 0;
        long started = System.nanoTime();
        Message response;
        for (int round = 0; ; round++) {
            boolean lastRound = round >= Math.max(0, properties.maxToolRounds());
            MessageCreateParams.Builder params = first.toBuilder().messages(messages);
            if (request.hasTools() && lastRound) {
                params.toolChoice(ToolChoiceNone.builder().build());
            }
            response = client().messages().create(params.build());
            apiCalls++;
            Usage usage = response.usage();
            input += usage.inputTokens();
            output += usage.outputTokens();
            cacheRead = add(cacheRead, usage.cacheReadInputTokens().orElse(null));
            cacheWrite = add(cacheWrite, usage.cacheCreationInputTokens().orElse(null));

            List<ToolUseBlock> uses = response.content().stream()
                    .flatMap(block -> block.toolUse().stream())
                    .toList();
            boolean wantsTools = response.stopReason().map(StopReason.TOOL_USE::equals).orElse(false);
            if (!request.hasTools() || lastRound || !wantsTools || uses.isEmpty()) {
                break;
            }
            messages.add(response.toParam());
            List<ContentBlockParam> results = new ArrayList<>();
            for (ToolUseBlock use : uses) {
                JsonNode args = use._input().convert(JsonNode.class);
                ToolOutput out = request.toolHandler().handle(use.name(), args);
                results.add(ContentBlockParam.ofToolResult(ToolResultBlockParam.builder()
                        .toolUseId(use.id())
                        .content(out.content())
                        .isError(out.error())
                        .build()));
            }
            messages.add(MessageParam.builder().role(MessageParam.Role.USER).contentOfBlockParams(results).build());
        }
        int latencyMs = (int) ((System.nanoTime() - started) / 1_000_000);

        String text = response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(TextBlock::text)
                .collect(Collectors.joining());
        return new ChatCompletion(
                text,
                response.stopReason().map(StopReason::asString).orElse(null),
                response.stopDetails().flatMap(RefusalStopDetails::category)
                        .map(RefusalStopDetails.Category::asString).orElse(null),
                response.model().asString(),
                (int) input,
                (int) output,
                cacheRead == null ? null : cacheRead.intValue(),
                cacheWrite == null ? null : cacheWrite.intValue(),
                latencyMs,
                apiCalls);
    }

    private static Long add(Long total, Long value) {
        if (value == null) {
            return total;
        }
        return total == null ? value : total + value;
    }

    private static JsonOutputFormat.Schema schemaOf(Map<String, Object> schema) {
        JsonOutputFormat.Schema.Builder b = JsonOutputFormat.Schema.builder();
        schema.forEach((k, v) -> b.putAdditionalProperty(k, JsonValue.from(v)));
        return b.build();
    }

    private AnthropicClient client() {
        AnthropicClient c = client;
        if (c == null) {
            synchronized (this) {
                c = client;
                if (c == null) {
                    if (!properties.hasApiKey()) {
                        throw new IllegalStateException("Chưa cấu hình ANTHROPIC_API_KEY");
                    }
                    AnthropicOkHttpClient.Builder b = AnthropicOkHttpClient.builder()
                            .apiKey(properties.apiKey())
                            .timeout(properties.timeout())
                            .maxRetries(2);
                    if (properties.baseUrl() != null && !properties.baseUrl().isBlank()) {
                        b.baseUrl(properties.baseUrl());
                    }
                    c = b.build();
                    client = c;
                }
            }
        }
        return c;
    }

    @Override
    public void destroy() {
        AnthropicClient c = client;
        if (c != null) {
            c.close();
        }
    }
}
