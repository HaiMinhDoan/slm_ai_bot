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
    void longParagraphIsCutAtSentenceEndsNotInsideNumbers() {
        String sentence = "Gói Hybrid 3 pha 10,63 kWp giá 156.700.000 đ, lưu trữ 5,12 kWh, bảo hành 5 năm ạ. ";
        String reply = sentence.repeat(8).strip();

        var parts = ReplyParts.split(reply);
        assertThat(parts).hasSizeGreaterThan(1).allSatisfy(p -> {
            assertThat(p.length()).isLessThanOrEqualTo(ReplyParts.LONG_PART);
            assertThat(p).startsWith("Gói Hybrid").endsWith("ạ.");
        });
        assertThat(String.join(" ", parts)).isEqualTo(reply);
    }

    @Test
    void linkIsSentAloneSoMessengerShowsItsPreview() {
        // Link giữa câu: câu giữ ý, link thành tin riêng ngay sau
        assertThat(ReplyParts.split("Dạ chị xem chi tiết gói tại https://slmsolar.vn/products/156 ạ."))
                .containsExactly("Dạ chị xem chi tiết gói tại link bên dưới ạ.", "https://slmsolar.vn/products/156");
        // Link sau dấu hai chấm hoặc đứng riêng dòng: bỏ khỏi câu
        assertThat(ReplyParts.split("Chi tiết gói chị xem ở đây:\nhttps://slmsolar.vn/products/156"))
                .containsExactly("Chi tiết gói chị xem ở đây:", "https://slmsolar.vn/products/156");
        assertThat(ReplyParts.split("Anh bấm vào link https://slmsolar.com.vn/products/157 để xem nhé."))
                .containsExactly("Anh bấm vào link bên dưới để xem nhé.", "https://slmsolar.com.vn/products/157");
        // Tin chỉ có link giữ nguyên; dấu câu dính sau link không thuộc link
        assertThat(ReplyParts.split("https://slmsolar.vn/products/156")).containsExactly(
                "https://slmsolar.vn/products/156");
        assertThat(ReplyParts.split("Dạ giá trọn gói 154.300.000 đ ạ.\n\nhttps://slmsolar.vn/products/156."))
                .containsExactly("Dạ giá trọn gói 154.300.000 đ ạ.", "https://slmsolar.vn/products/156");
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
