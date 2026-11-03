package vn.slmglobal.slmai.services.knowledge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import vn.slmglobal.slmai.services.knowledge.rules.RuleCodec;
import vn.slmglobal.slmai.services.lead.LeadScoring;
import vn.slmglobal.slmai.services.lead.LeadScoringRule;
import vn.slmglobal.slmai.services.lead.entity.LeadLevel;
import vn.slmglobal.slmai.services.policy.ReplyPolicyRule;
import vn.slmglobal.slmai.services.sizing.PricingProperties;
import vn.slmglobal.slmai.services.sizing.ProvinceDirectory;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Đọc / ghi / kiểm tra 4 rule YAML (mục 5.2, 5.3). */
class RuleCodecTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final PricingProperties pricing = new PricingProperties(3500,
            Map.of("NGAY_NHIEU", 0.3, "CAN_BANG", 0.5, "DEM_NHIEU", 0.7), 0.9, 0.9, 5, 30,
            Map.of("BAC", "slmsolar.vn", "NAM", "slmsolar.com.vn"), "Giá đã gồm VAT.", "Mái bằng cần thêm khung.");

    @Test
    void pricingRoundTrip() {
        String yaml = RuleCodec.write(RuleCodec.PRICING, pricing);
        assertThat(yaml).contains("price_per_kwh: 3500", "NGAY_NHIEU: 0.3", "BAC: slmsolar.vn");

        RuleCodec.Parsed<PricingProperties> p = RuleCodec.pricing(yaml);
        assertThat(p.errors()).isEmpty();
        assertThat(p.value()).isEqualTo(pricing);
        // Ghi lại ra đúng chuỗi cũ: nội dung tất định, mã băm không đổi giữa các lần nhập
        assertThat(RuleCodec.write(RuleCodec.PRICING, p.value())).isEqualTo(yaml);
    }

    @Test
    void pricingReportsEachBadField() {
        String yaml = RuleCodec.write(RuleCodec.PRICING, pricing)
                .replace("price_per_kwh: 3500", "price_per_kwh: 0")
                .replace("DEM_NHIEU: 0.7", "DEM_NHIEU: 0.2")
                .replace("BAC: slmsolar.vn", "BAC: https://slmsolar.vn/products")
                + "gia_thue: 1\n";

        List<String> errors = RuleCodec.validate(RuleCodec.PRICING, yaml);

        assertThat(errors).anySatisfy(e -> assertThat(e).startsWith("price_per_kwh"));
        assertThat(errors).anySatisfy(e -> assertThat(e).startsWith("night_share"));
        assertThat(errors).anySatisfy(e -> assertThat(e).startsWith("product_link_domains.BAC"));
        assertThat(errors).anySatisfy(e -> assertThat(e).startsWith("gia_thue"));
    }

    @Test
    void brokenYamlIsAnError() {
        assertThat(RuleCodec.validate(RuleCodec.PRICING, "price_per_kwh: [3500")).singleElement()
                .satisfies(e -> assertThat(e).startsWith("YAML sai cú pháp"));
    }

    @Test
    void leadScoringRuleDrivesScore() {
        String yaml = RuleCodec.write(RuleCodec.LEAD_SCORING, LeadScoringRule.DEFAULT);
        assertThat(yaml).contains("- TUAN_TOI", "- THANG_TOI", "warm_answered_questions: 3");
        assertThat(RuleCodec.leadScoring(yaml).value()).isEqualTo(LeadScoringRule.DEFAULT);

        LeadScoringRule strict = RuleCodec.leadScoring(yaml.replace("warm_answered_questions: 3",
                "warm_answered_questions: 5")).value();
        LeadScoring.Signals threeAnswers = new LeadScoring.Signals(false, null, false, 3, null, null);
        assertThat(LeadScoring.score(threeAnswers).level()).isEqualTo(LeadLevel.WARM);
        assertThat(LeadScoring.score(threeAnswers, strict).level()).isEqualTo(LeadLevel.COLD);

        assertThat(RuleCodec.validate(RuleCodec.LEAD_SCORING, yaml.replace("THANG_TOI", "SANG_NAY")))
                .anySatisfy(e -> assertThat(e).contains("SANG_NAY"));
        assertThat(new LeadScoringRule(Set.of(), 0, 2).validate()).hasSize(2);
    }

    @Test
    void regionsFromRepoDataAreValidAndDuplicatesAreCaught() {
        String yaml = RuleCodec.write(RuleCodec.REGIONS, ProvinceDirectory.loadDefault(objectMapper));
        assertThat(RuleCodec.validate(RuleCodec.REGIONS, yaml)).isEmpty();

        // Đổi Hải Phòng sang mã Hà Nội: trùng mã tỉnh
        String duplicated = yaml.replaceFirst("ma: HAI_PHONG", "ma: HA_NOI");
        assertThat(RuleCodec.validate(RuleCodec.REGIONS, duplicated)).isNotEmpty();
        assertThat(RuleCodec.validate(RuleCodec.REGIONS, yaml.replaceFirst("mien: NAM", "mien: TRUNG")))
                .anySatisfy(e -> assertThat(e).contains("BAC hoặc NAM"));
    }

    @Test
    void replyPolicyRule() {
        String yaml = RuleCodec.write(RuleCodec.REPLY_POLICY, ReplyPolicyRule.DEFAULT);
        assertThat(RuleCodec.replyPolicy(yaml).value()).isEqualTo(ReplyPolicyRule.DEFAULT);

        String custom = yaml.replace("max_length: 2000", "max_length: 800")
                .replace("extra_allowed_links: []", "extra_allowed_links:\n- slmsolar.vn/lien-he");
        RuleCodec.Parsed<ReplyPolicyRule> parsed = RuleCodec.replyPolicy(custom);
        assertThat(parsed.errors()).isEmpty();
        assertThat(parsed.value().maxLength()).isEqualTo(800);
        assertThat(parsed.value().extraAllowedLinks()).containsExactly("slmsolar.vn/lien-he");
        // Chỉ nhận link có đuôi .com / .vn / .net / .org như bộ kiểm tra link (ReplyPolicy)
        assertThat(RuleCodec.validate(RuleCodec.REPLY_POLICY, custom.replace("slmsolar.vn/lien-he", "zalo.me/x")))
                .anySatisfy(e -> assertThat(e).startsWith("extra_allowed_links"));

        assertThat(RuleCodec.validate(RuleCodec.REPLY_POLICY, yaml.replace("max_length: 2000", "max_length: 5000")))
                .anySatisfy(e -> assertThat(e).startsWith("max_length"));
    }

    @Test
    void docTypeFromKey() {
        assertThat(KnowledgeDocType.ofKey("guide/mau/faq")).contains(KnowledgeDocType.TEMPLATE);
        assertThat(KnowledgeDocType.ofKey("guide/thuat-ngu")).contains(KnowledgeDocType.GUIDE);
        assertThat(KnowledgeDocType.ofKey("policies/bao-hanh")).contains(KnowledgeDocType.POLICY);
        assertThat(KnowledgeDocType.ofKey("bao-hanh")).isEmpty();
    }
}
