package vn.slmglobal.slmai.services.knowledge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.common.security.AuthInfo;
import vn.slmglobal.slmai.common.security.RoleCode;
import vn.slmglobal.slmai.services.knowledge.admin.KnowledgeAdminService;
import vn.slmglobal.slmai.services.knowledge.admin.KnowledgeReleaseService;
import vn.slmglobal.slmai.services.knowledge.rules.RuleCodec;
import vn.slmglobal.slmai.services.settings.AiSettingsService;
import vn.slmglobal.slmai.services.sizing.PricingProperties;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Knowledge Editor trên PostgreSQL thật (mục 5.2, 5.3, 5.9, 33.2). */
@Testcontainers
@SpringBootTest(properties = {
        "app.jwt.signer-key=test-signer-key-0123456789-abcdefghijklmnop",
        "app.conversation.worker-enabled=false",
        "app.ai.worker-enabled=false"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KnowledgeAdminIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final AuthInfo ADMIN = new AuthInfo(null, "admin@test.local", "Quản trị",
            Set.of(RoleCode.CONTENT_ADMIN));
    private static final AuthInfo OTHER = new AuthInfo(null, "other@test.local", "Người khác",
            Set.of(RoleCode.CONTENT_ADMIN));

    @Autowired
    KnowledgeBase knowledge;
    @Autowired
    KnowledgeAdminService admin;
    @Autowired
    KnowledgeReleaseService releases;
    @Autowired
    AiSettingsService settings;
    @Autowired
    PricingProperties defaultPricing;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    @Order(1)
    void firstStartImportsRepoKnowledgeUnchanged() {
        KnowledgeSnapshot s = knowledge.current();

        assertThat(s.releaseId()).isNotNull();
        // Khối kiến thức bot đọc giống hệt bản dựng thẳng từ file trong repo: bot không đổi hành vi khi bật V10
        List<KnowledgeSnapshot.Doc> repo = knowledge.classpathDocs();
        KnowledgeSnapshot fromRepo = new KnowledgeSnapshot(null, "x", repo, knowledge.defaultRules());
        assertThat(knowledge.renderKnowledge(s)).isEqualTo(knowledge.renderKnowledge(fromRepo));
        assertThat(knowledge.renderPrompt(s)).isEqualTo(knowledge.renderPrompt(fromRepo)).isNotBlank();
        assertThat(s.botDocs()).extracting(KnowledgeSnapshot.Doc::key)
                .contains("core/identity", "core/voice", "core/guardrails", "playbooks/khach-hoi-gia")
                .doesNotContain("guide/huong-dan-viet", "guide/mau/faq", "prompts/customer-chat");
        assertThat(knowledge.renderKnowledge(s)).contains("<tai_lieu ma=\"core/identity\">")
                .doesNotContain("{{ten_tu_van_vien}}");
        assertThat(s.rules().pricing()).isEqualTo(defaultPricing);
        assertThat(admin.rules()).extracting(d -> d.doc().key()).containsExactlyElementsOf(RuleCodec.KEYS);
        assertThat(admin.rules()).allSatisfy(d -> assertThat(d.ruleErrors()).isEmpty());
    }

    @Test
    void draftReleaseActivateThenRollback() {
        KnowledgeAdminService.DocDetail created = admin.create(new KnowledgeAdminService.CreateRequest(
                KnowledgeDocType.POLICY, "policies/bao-hanh-thu", "Chính sách bảo hành thử", null), ADMIN);
        assertThat(created.draft().content()).startsWith("# Chính sách bảo hành thử").contains("## Áp dụng cho");
        Long docId = created.doc().id();
        admin.saveDraft(docId, new KnowledgeAdminService.SaveDraftRequest("Chính sách bảo hành thử",
                "# Chính sách bảo hành thử\n\nBảo hành theo hãng, nhân viên báo chi tiết khi khảo sát.\n", null,
                "Bản đầu", null, created.draft().updatedAt(), false), ADMIN);
        Long before = knowledge.current().releaseId();

        KnowledgeReleaseService.ReleaseRequest req = new KnowledgeReleaseService.ReleaseRequest(
                List.of(admin.get(docId).draft().id()), "Thêm chính sách bảo hành");
        KnowledgeReleaseService.Report report = releases.validate(req);
        assertThat(report.ok()).isTrue();
        assertThat(report.changes()).singleElement().satisfies(c -> {
            assertThat(c.key()).isEqualTo("policies/bao-hanh-thu");
            assertThat(c.action()).isEqualTo("ADDED");
        });
        assertThat(report.tokenTotal()).isPositive();

        KnowledgeReleaseService.ReleaseRow row = releases.create(req, ADMIN);
        assertThat(row.status()).isEqualTo("READY");
        assertThat(admin.get(docId).draft()).isNull();
        // Chưa kích hoạt: bot vẫn dùng bản cũ
        assertThat(knowledge.current().doc("policies/bao-hanh-thu")).isEmpty();

        releases.activate(row.id(), ADMIN);
        assertThat(knowledge.current().releaseId()).isEqualTo(row.id());
        assertThat(knowledge.renderKnowledge(knowledge.current())).contains("<tai_lieu ma=\"policies/bao-hanh-thu\">");
        assertThat(admin.get(docId).doc().activeVersionNo()).isEqualTo(1);

        // Quay về bản trước: tài liệu mới biến mất khỏi bot ngay
        releases.activate(before, ADMIN);
        assertThat(knowledge.current().releaseId()).isEqualTo(before);
        assertThat(knowledge.current().doc("policies/bao-hanh-thu")).isEmpty();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM nhat_ky_thao_tac WHERE hanh_dong = ?", Integer.class,
                "knowledge.release.rollback")).isEqualTo(1);
        // Kích hoạt lại bản vừa rời đi cũng là quay về; không tạo được bản trùng nội dung
        releases.activate(row.id(), ADMIN);
        assertThat(knowledge.current().releaseId()).isEqualTo(row.id());
    }

    @Test
    void conflictsAreDetected() {
        KnowledgeAdminService.DocDetail d = admin.create(new KnowledgeAdminService.CreateRequest(
                KnowledgeDocType.FAQ, "faq/xung-dot", "Xung đột", "# Xung đột\n\nBản đầu.\n"), ADMIN);
        Long docId = d.doc().id();
        // Mở trang khi chưa có nháp mới: người khác lưu nháp trước
        assertThatThrownBy(() -> admin.saveDraft(docId, new KnowledgeAdminService.SaveDraftRequest("Xung đột",
                "# Xung đột\n\nBản của tôi.\n", null, null, null, null, false), ADMIN))
                .isInstanceOfSatisfying(CommonException.class, e -> assertThat(e.getHttpStatus().value()).isEqualTo(409));

        // Phát hành v1, rồi A mở trang (gốc v1); B sửa và phát hành v2; A lưu → gốc đã đổi
        releases.create(new KnowledgeReleaseService.ReleaseRequest(List.of(d.draft().id()), null), ADMIN);
        Long v1 = admin.get(docId).latest().id();
        KnowledgeAdminService.DocDetail b = admin.saveDraft(docId, new KnowledgeAdminService.SaveDraftRequest(
                "Xung đột", "# Xung đột\n\nBản của B.\n", null, null, v1, null, false), OTHER);
        releases.create(new KnowledgeReleaseService.ReleaseRequest(List.of(b.draft().id()), null), OTHER);
        CommonException conflict = org.junit.jupiter.api.Assertions.assertThrows(CommonException.class,
                () -> admin.saveDraft(docId, new KnowledgeAdminService.SaveDraftRequest("Xung đột",
                        "# Xung đột\n\nBản của A.\n", null, null, v1, null, false), ADMIN));
        assertThat(conflict.getHttpStatus().value()).isEqualTo(409);
        assertThat(objectMapper.valueToTree(conflict.getData()).path("reason").asText()).isEqualTo("BASE_CHANGED");
        assertThat(objectMapper.valueToTree(conflict.getData()).path("diff").asText()).contains("-Bản của B.",
                "+Bản của A.");
        // Ghi đè có chủ ý
        KnowledgeAdminService.DocDetail forced = admin.saveDraft(docId, new KnowledgeAdminService.SaveDraftRequest(
                "Xung đột", "# Xung đột\n\nBản của A.\n", null, null, v1, null, true), ADMIN);
        assertThat(forced.draft().content()).contains("Bản của A.");
        assertThat(forced.draft().baseVersionId()).isEqualTo(admin.get(docId).latest().id());
        assertThat(admin.diff(docId, null, null).diff()).contains("+Bản của A.");
    }

    @Test
    void invalidRuleIsRejectedAndValidRuleTakesEffect() {
        KnowledgeAdminService.DocDetail rule = admin.getByKey(RuleCodec.LEAD_SCORING);
        ObjectNode bad = (ObjectNode) rule.value().deepCopy();
        bad.put("warm_answered_questions", 0);
        assertThatThrownBy(() -> admin.saveDraft(rule.doc().id(), new KnowledgeAdminService.SaveDraftRequest(
                rule.doc().title(), null, bad, null, rule.latest().id(), null, false), ADMIN))
                .isInstanceOfSatisfying(CommonException.class, e -> assertThat(e.getMessage())
                        .contains("warm_answered_questions"));

        ObjectNode good = (ObjectNode) rule.value().deepCopy();
        good.put("warm_answered_questions", 4);
        KnowledgeAdminService.DocDetail saved = admin.saveDraft(rule.doc().id(),
                new KnowledgeAdminService.SaveDraftRequest(rule.doc().title(), null, good, "Khắt khe hơn",
                        rule.latest().id(), null, false), ADMIN);
        assertThat(saved.draft().content()).contains("warm_answered_questions: 4");
        KnowledgeReleaseService.ReleaseRow row = releases.create(new KnowledgeReleaseService.ReleaseRequest(
                List.of(saved.draft().id()), null), ADMIN);
        releases.activate(row.id(), ADMIN);
        assertThat(knowledge.leadScoring().warmAnsweredQuestions()).isEqualTo(4);
    }

    @Test
    void releaseWithoutChangesIsRefused() {
        assertThatThrownBy(() -> releases.create(new KnowledgeReleaseService.ReleaseRequest(List.of(), null), ADMIN))
                .isInstanceOfSatisfying(CommonException.class, e -> assertThat(e.getMessage())
                        .contains("Không có thay đổi"));
    }

    @Test
    void killSwitchTakesEffectImmediatelyAndIsAudited() {
        assertThatThrownBy(() -> settings.setKillSwitch(true, " ", ADMIN)).isInstanceOf(CommonException.class);

        settings.setKillSwitch(true, "Kiểm tra bảng giá mới", ADMIN);
        assertThat(settings.ai().forceDraft()).isTrue();
        assertThat(settings.settings().killSwitch().reason()).isEqualTo("Kiểm tra bảng giá mới");

        settings.setKillSwitch(false, null, ADMIN);
        assertThat(settings.ai().forceDraft()).isFalse();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM nhat_ky_thao_tac WHERE hanh_dong LIKE 'ai.kill_switch.%'",
                Integer.class)).isEqualTo(2);
    }
}
