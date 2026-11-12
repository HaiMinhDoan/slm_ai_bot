package vn.slmglobal.slmai.services.ai;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Tách câu trả lời thành nhiều tin và thời gian "đang soạn" theo tốc độ gõ (2026-11-12) */
class ReplyPartsTest {

    @Test
    void blankLinesSeparateMessagesAndListsStayTogether() {
        String reply = """
                Dạ em đây ạ, em xin lỗi đã để anh đợi lâu.

                Bên em chưa có gói đúng 12 tấm, hai gói gần nhất là:
                - Hybrid 5 kWp (8 tấm), pin 5,12 kWh: 90,8 triệu đồng
                - Hybrid 8,75 kWp (14 tấm), pin 5,12 kWh: 113,2 triệu đồng
                \s
                Anh muốn em báo chi tiết gói nào ạ?""";

        assertThat(ReplyParts.split(reply)).containsExactly(
                "Dạ em đây ạ, em xin lỗi đã để anh đợi lâu.",
                """
                        Bên em chưa có gói đúng 12 tấm, hai gói gần nhất là:
                        - Hybrid 5 kWp (8 tấm), pin 5,12 kWh: 90,8 triệu đồng
                        - Hybrid 8,75 kWp (14 tấm), pin 5,12 kWh: 113,2 triệu đồng""",
                "Anh muốn em báo chi tiết gói nào ạ?");
        assertThat(ReplyParts.split("Dạ mái ngói lắp được anh nhé.")).containsExactly("Dạ mái ngói lắp được anh nhé.");
        assertThat(ReplyParts.split("  ")).isEmpty();
    }

    @Test
    void longParagraphIsCutAtSentenceEndsNotInsideNumbersOrLinks() {
        String sentence = "Gói Hybrid 3 pha 10,63 kWp giá 156.700.000 đ, xem tại https://slmsolar.vn/products/136 ạ. ";
        String reply = sentence.repeat(8).strip();

        var parts = ReplyParts.split(reply);
        assertThat(parts).hasSizeGreaterThan(1).allSatisfy(p -> {
            assertThat(p.length()).isLessThanOrEqualTo(ReplyParts.LONG_PART);
            assertThat(p).startsWith("Gói Hybrid").endsWith("ạ.");
        });
        assertThat(String.join(" ", parts)).isEqualTo(reply);
    }

    @Test
    void extraPartsAreMergedIntoTheLast() {
        var parts = ReplyParts.split("Một.\n\nHai.\n\nBa.\n\nBốn.\n\nNăm.\n\nSáu.\n\nBảy.");
        assertThat(parts).hasSize(ReplyParts.MAX_PARTS).endsWith("Năm.\n\nSáu.\n\nBảy.");
    }

    @Test
    void typingTimeFollowsCharactersPerSecondWithinLimits() {
        AiProperties p = mock(AiProperties.class);
        when(p.typingCharsPerSecond()).thenReturn(5.0);
        when(p.typingMaxDelay()).thenReturn(Duration.ofSeconds(30));

        // "Dạ em đây ạ" 11 ký tự / 5 ký tự mỗi giây
        assertThat(AiTurnService.typingDelay("Dạ em đây ạ", p)).isEqualTo(Duration.ofMillis(2200));
        assertThat(AiTurnService.typingDelay("Dạ", p)).isEqualTo(AiTurnService.MIN_TYPING);
        assertThat(AiTurnService.typingDelay("x".repeat(400), p)).isEqualTo(Duration.ofSeconds(30));

        when(p.typingMaxDelay()).thenReturn(Duration.ZERO);
        assertThat(AiTurnService.typingDelay("Dạ em đây ạ", p)).isZero();
    }

    @Test
    void waitIsWrittenInMinutesHoursOrDays() {
        assertThat(CustomerChatPromptBuilder.waitText(Duration.ofMinutes(5))).isEqualTo("5 phút");
        assertThat(CustomerChatPromptBuilder.waitText(Duration.ofMinutes(130))).isEqualTo("2 giờ 10 phút");
        assertThat(CustomerChatPromptBuilder.waitText(Duration.ofHours(50))).isEqualTo("2 ngày");
    }
}
