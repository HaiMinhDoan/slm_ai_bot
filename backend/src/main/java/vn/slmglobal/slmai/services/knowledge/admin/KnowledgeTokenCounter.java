package vn.slmglobal.slmai.services.knowledge.admin;

import com.anthropic.models.messages.MessageCountTokensParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.slmglobal.slmai.services.ai.ClaudeChatClient;
import vn.slmglobal.slmai.services.settings.AiSettingsService;

/**
 * Đếm token khối cố định của prompt (hướng dẫn + kiến thức, mục 5.4) bằng API count_tokens (miễn phí). Chưa có khoá
 * API hoặc gọi lỗi thì ước tính theo số ký tự, ghi rõ là ước tính.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeTokenCounter {

    /** Ngân sách khối kiến thức (mục 5.4): vượt thì chặn phát hành */
    public static final long BUDGET = 40_000;
    /** Tiếng Việt có dấu: khoảng 3 ký tự một token */
    private static final double CHARS_PER_TOKEN = 3.0;

    private final ClaudeChatClient claude;
    private final AiSettingsService settings;

    /**
     * @param estimated true khi là ước tính theo số ký tự (chưa có khoá API hoặc API lỗi)
     */
    public record Count(long tokens, long budget, double percent, boolean estimated) {
    }

    public Count count(String system) {
        long tokens;
        boolean estimated = false;
        try {
            if (!settings.ai().hasApiKey()) {
                throw new IllegalStateException("chưa có ANTHROPIC_API_KEY");
            }
            tokens = claude.sdk().messages().countTokens(MessageCountTokensParams.builder()
                    .model(settings.ai().model())
                    .system(system)
                    .addUserMessage(".")
                    .build()).inputTokens();
        } catch (RuntimeException e) {
            log.debug("Đếm token bằng API không được, ước tính theo ký tự: {}", e.getMessage());
            tokens = Math.round(system.length() / CHARS_PER_TOKEN);
            estimated = true;
        }
        return new Count(tokens, BUDGET, Math.round(tokens * 1000.0 / BUDGET) / 10.0, estimated);
    }
}
