package vn.slmglobal.slmai.services.knowledge;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import vn.slmglobal.slmai.services.knowledge.rules.RuleCodec;
import vn.slmglobal.slmai.services.knowledge.rules.RuleSet;
import vn.slmglobal.slmai.services.lead.LeadScoringRule;
import vn.slmglobal.slmai.services.policy.ReplyPolicyRule;
import vn.slmglobal.slmai.services.sizing.PricingProperties;
import vn.slmglobal.slmai.services.sizing.ProvinceDirectory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Kiến thức gốc trong repo: {@code knowledge/**}/*.md, {@code prompts/customer-chat.md} và rule sinh từ cấu hình
 * đang chạy. Dùng để nhập bản phát hành #1 khi CSDL còn trống (mục 5.2: "nhập lần đầu từ repo") và làm dự phòng khi
 * chưa đọc được CSDL. Sau lần nhập, sửa file trong repo không còn tác dụng với bot.
 */
public final class ClasspathKnowledge {

    static final String PROMPT_LOCATION = "prompts/customer-chat.md";
    static final String PROMPT_TITLE = "Hướng dẫn hệ thống cho AI trả lời khách";

    private ClasspathKnowledge() {
    }

    /** Tài liệu Markdown trong repo kèm 4 rule mặc định, xếp theo mã */
    public static List<KnowledgeSnapshot.Doc> docs(PricingProperties pricing, ObjectMapper objectMapper) {
        List<KnowledgeSnapshot.Doc> out = new ArrayList<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            for (Resource r : resolver.getResources("classpath*:knowledge/**/*.md")) {
                String key = keyOf(r);
                if (key == null) {
                    continue;
                }
                KnowledgeDocType type = KnowledgeDocType.ofKey(key).orElse(null);
                if (type == null || type.yaml()) {
                    continue;
                }
                String content = r.getContentAsString(StandardCharsets.UTF_8);
                String title = type == KnowledgeDocType.TEMPLATE
                        ? "Mẫu " + key.substring(key.lastIndexOf('/') + 1)
                        : titleOf(content, key);
                out.add(new KnowledgeSnapshot.Doc(key, type, title, content));
            }
            Resource prompt = resolver.getResource("classpath:" + PROMPT_LOCATION);
            out.add(new KnowledgeSnapshot.Doc(KnowledgeSnapshot.PROMPT_KEY, KnowledgeDocType.PROMPT, PROMPT_TITLE,
                    prompt.getContentAsString(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            throw new UncheckedIOException("Không đọc được kiến thức trong repo", e);
        }
        RuleSet rules = defaultRules(pricing, objectMapper);
        out.add(rule(RuleCodec.PRICING, rules.pricing()));
        out.add(rule(RuleCodec.LEAD_SCORING, rules.leadScoring()));
        out.add(rule(RuleCodec.REGIONS, rules.regions()));
        out.add(rule(RuleCodec.REPLY_POLICY, rules.replyPolicy()));
        out.sort(Comparator.comparing(KnowledgeSnapshot.Doc::key));
        return out;
    }

    /** Rule đang chạy trước khi có kiến thức trong CSDL: application.yml, hằng số trong code, data/tinh-thanh.json */
    public static RuleSet defaultRules(PricingProperties pricing, ObjectMapper objectMapper) {
        return new RuleSet(pricing, LeadScoringRule.DEFAULT, ProvinceDirectory.loadDefault(objectMapper),
                ReplyPolicyRule.DEFAULT);
    }

    private static KnowledgeSnapshot.Doc rule(String key, Object value) {
        return new KnowledgeSnapshot.Doc(key, KnowledgeDocType.RULE, RuleCodec.TITLES.get(key),
                RuleCodec.write(key, value));
    }

    /** Tiêu đề = dòng "# ..." đầu tiên; không có thì dùng mã */
    static String titleOf(String content, String key) {
        for (String line : content.split("\n")) {
            String l = line.strip();
            if (l.startsWith("# ")) {
                return l.substring(2).strip();
            }
            if (!l.isEmpty()) {
                break;
            }
        }
        return key;
    }

    /** Mã tài liệu = đường dẫn dưới knowledge/, bỏ đuôi .md (ví dụ playbooks/khach-hoi-gia) */
    private static String keyOf(Resource r) throws IOException {
        String url = r.getURL().toString();
        int i = url.lastIndexOf("/knowledge/");
        if (i < 0 || !url.endsWith(".md")) {
            return null;
        }
        return url.substring(i + "/knowledge/".length(), url.length() - ".md".length());
    }
}
