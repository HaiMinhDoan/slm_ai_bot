package vn.slmglobal.slmai.services.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.Test;
import vn.slmglobal.slmai.services.ai.AiProperties;
import vn.slmglobal.slmai.services.conversation.ConversationProperties;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Cài đặt AI: giá trị trong application.yml là mặc định, cai_dat_ai ghi đè (mục 33.2). */
class AiSettingsServiceTest {

    private final AiProperties base = new AiProperties("k", "claude-sonnet-5-5", "low", 8000, Duration.ofSeconds(120),
            true, false, true, 4, 40, 3, Duration.ofSeconds(20), Duration.ofMinutes(3), true, "SolarMax", "Huyền",
            List.of("CHAO_HOI", "HOI_GIA"), List.of("KHIEU_NAI"),
            new AiProperties.BusinessHours("Asia/Ho_Chi_Minh", "08:00", "17:30",
                    List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)),
            new AiProperties.Templates("Ngoài giờ", "Chuyển nhân viên", Duration.ofHours(12)), 3, null);
    private final ConversationProperties conversation = new ConversationProperties(Duration.ofSeconds(6),
            Duration.ofHours(24), Duration.ofHours(24), true, 50);

    @Test
    void withoutOverridesEffectiveEqualsBase() {
        AiSettingsService s = AiSettingsService.fixed(base, conversation);

        assertThat(s.ai()).isEqualTo(base);
        assertThat(s.conversation()).isEqualTo(conversation);
        assertThat(s.view().overridden()).isEmpty();
    }

    @Test
    void overridesApplyAndBadRowsFallBackToDefault() {
        AiSettingRepository repo = mock(AiSettingRepository.class);
        JsonNodeFactory f = JsonNodeFactory.instance;
        Map<String, com.fasterxml.jackson.databind.JsonNode> rows = new LinkedHashMap<>();
        rows.put("kill_switch", f.objectNode().put("on", true).put("reason", "Kiểm tra giá"));
        rows.put("auto_intents", f.arrayNode().add("CHAO_HOI"));
        rows.put("persona_name", f.textNode("Lan"));
        rows.put("debounce_seconds", f.numberNode(10));
        rows.put("max_attempts", f.textNode("ba lần")); // sai kiểu: bỏ qua, giữ mặc định
        when(repo.findAll()).thenReturn(rows);

        AiSettingsService s = new AiSettingsService(base, conversation, repo, new ObjectMapper(), null, null, null);

        assertThat(s.ai().forceDraft()).isTrue();
        assertThat(s.ai().autoIntents()).containsExactly("CHAO_HOI");
        assertThat(s.ai().personaName()).isEqualTo("Lan");
        assertThat(s.ai().maxAttempts()).isEqualTo(3);
        assertThat(s.conversation().debounce()).isEqualTo(Duration.ofSeconds(10));
        assertThat(s.ai().apiKey()).isEqualTo("k");
        assertThat(s.view().overridden()).containsExactlyInAnyOrder("kill_switch", "auto_intents", "persona_name",
                "debounce_seconds");
    }

    @Test
    void validation() {
        AiSettings ok = AiSettingsService.fixed(base, conversation).settings();
        assertThat(AiSettingsService.validate(ok)).isEmpty();

        AiSettings bad = new AiSettings(ok.killSwitch(), List.of("CHAO_HOI", "KHONG_CO"), List.of("CHAO_HOI"), "18:00",
                "08:00", List.of(), null, null, 0, " ", "SolarMax", true, 500, 24, "gpt-5", "low", "claude-opus-5-5", 0,
                20);
        assertThat(AiSettingsService.validate(bad)).hasSizeGreaterThanOrEqualTo(7);
    }
}
