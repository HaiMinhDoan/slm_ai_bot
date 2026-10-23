package vn.slmglobal.slmai.services.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.CacheControlEphemeral;
import com.anthropic.models.messages.JsonOutputFormat;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.RefusalStopDetails;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.TextBlock;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.ThinkingConfigAdaptive;
import com.anthropic.models.messages.Usage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gọi Claude Messages API cho một lượt trả lời khách: một request, kết quả có cấu trúc (mục 18), không tool
 * (tool tim_goi_phu_hop / tim_media thêm khi có catalog và Media Library).
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

    public record ChatRequest(String cachedSystem, String turnContext, List<ChatTurn> turns,
                              Map<String, Object> outputSchema) {
    }

    public record ChatCompletion(String text, String stopReason, String refusalCategory, String model,
                                 Integer inputTokens, Integer outputTokens, Integer cacheReadTokens,
                                 Integer cacheWriteTokens, int latencyMs) {

        public boolean refused() {
            return StopReason.REFUSAL.asString().equals(stopReason);
        }

        public boolean truncated() {
            return StopReason.MAX_TOKENS.asString().equals(stopReason);
        }
    }

    /** Lỗi gọi API (mạng, 429, 5xx...) ném ra nguyên dạng {@code AnthropicException} để tầng trên thử lại. */
    public ChatCompletion complete(ChatRequest request) {
        MessageCreateParams.Builder params = MessageCreateParams.builder()
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
        for (ChatTurn turn : request.turns()) {
            if (turn.customer()) {
                params.addUserMessage(turn.text());
            } else {
                params.addAssistantMessage(turn.text());
            }
        }
        if (properties.serverSideFallback()) {
            params.putAdditionalHeader("anthropic-beta", FALLBACK_BETA)
                    .putAdditionalBodyProperty("fallbacks", JsonValue.from("default"));
        }

        long started = System.nanoTime();
        Message response = client().messages().create(params.build());
        int latencyMs = (int) ((System.nanoTime() - started) / 1_000_000);

        String text = response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(TextBlock::text)
                .collect(Collectors.joining());
        Usage usage = response.usage();
        return new ChatCompletion(
                text,
                response.stopReason().map(StopReason::asString).orElse(null),
                response.stopDetails().flatMap(RefusalStopDetails::category)
                        .map(RefusalStopDetails.Category::asString).orElse(null),
                response.model().asString(),
                (int) usage.inputTokens(),
                (int) usage.outputTokens(),
                usage.cacheReadInputTokens().map(Long::intValue).orElse(null),
                usage.cacheCreationInputTokens().map(Long::intValue).orElse(null),
                latencyMs);
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
                    c = AnthropicOkHttpClient.builder()
                            .apiKey(properties.apiKey())
                            .timeout(properties.timeout())
                            .maxRetries(2)
                            .build();
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
