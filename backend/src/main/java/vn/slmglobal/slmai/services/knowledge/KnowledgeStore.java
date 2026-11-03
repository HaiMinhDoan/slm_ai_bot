package vn.slmglobal.slmai.services.knowledge;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.slmglobal.slmai.services.knowledge.entity.KnowledgeDoc;
import vn.slmglobal.slmai.services.knowledge.entity.KnowledgeRelease;
import vn.slmglobal.slmai.services.knowledge.entity.KnowledgeReleaseItem;
import vn.slmglobal.slmai.services.knowledge.entity.KnowledgeVersion;
import vn.slmglobal.slmai.services.knowledge.repository.KnowledgeDocRepository;
import vn.slmglobal.slmai.services.knowledge.repository.KnowledgeReleaseItemRepository;
import vn.slmglobal.slmai.services.knowledge.repository.KnowledgeReleaseRepository;
import vn.slmglobal.slmai.services.knowledge.repository.KnowledgeVersionRepository;
import vn.slmglobal.slmai.services.knowledge.rules.RuleCodec;
import vn.slmglobal.slmai.services.knowledge.rules.RuleSet;
import vn.slmglobal.slmai.services.lead.LeadScoringRule;
import vn.slmglobal.slmai.services.policy.ReplyPolicyRule;
import vn.slmglobal.slmai.services.sizing.PricingProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Đọc bản phát hành từ CSDL thành {@link KnowledgeSnapshot}. */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeStore {

    private final KnowledgeReleaseRepository releases;
    private final KnowledgeReleaseItemRepository items;
    private final KnowledgeVersionRepository versions;
    private final KnowledgeDocRepository docs;

    @Transactional(readOnly = true)
    public Optional<Long> activeReleaseId() {
        return releases.activeId();
    }

    @Transactional(readOnly = true)
    public Optional<KnowledgeSnapshot> loadActive(RuleSet fallbackRules) {
        return releases.findFirstByStatus(KnowledgeRelease.Status.ACTIVE).map(r -> load(r, fallbackRules));
    }

    /** Snapshot của một bản phát hành bất kỳ (kiểm tra trước khi kích hoạt, xem nội dung bản cũ) */
    @Transactional(readOnly = true)
    public KnowledgeSnapshot load(KnowledgeRelease release, RuleSet fallbackRules) {
        List<KnowledgeReleaseItem> rows = items.findByReleaseId(release.getId());
        Map<Long, KnowledgeVersion> byId = versions.findByIdIn(rows.stream().map(KnowledgeReleaseItem::getVersionId)
                .toList()).stream().collect(Collectors.toMap(KnowledgeVersion::getId, Function.identity()));
        Map<Long, KnowledgeDoc> docById = docs.findAllById(rows.stream().map(KnowledgeReleaseItem::getDocId).toList())
                .stream().collect(Collectors.toMap(KnowledgeDoc::getId, Function.identity()));
        List<KnowledgeSnapshot.Doc> out = new ArrayList<>();
        for (KnowledgeReleaseItem i : rows) {
            KnowledgeVersion v = byId.get(i.getVersionId());
            KnowledgeDoc d = docById.get(i.getDocId());
            if (v != null && d != null) {
                out.add(new KnowledgeSnapshot.Doc(d.getKey(), d.getType(), v.getTitle(), v.getContent()));
            }
        }
        return new KnowledgeSnapshot(release.getId(), release.getKbVersion(), out, rules(out, fallbackRules));
    }

    /**
     * Rule của bản phát hành. Rule đã qua kiểm tra khi phát hành nên không lỗi; nếu vẫn lỗi (sửa thẳng CSDL) thì
     * dùng giá trị dự phòng và ghi log, để bot vẫn chạy.
     */
    static RuleSet rules(List<KnowledgeSnapshot.Doc> docs, RuleSet fallback) {
        Map<String, String> byKey = docs.stream().filter(d -> d.type() == KnowledgeDocType.RULE)
                .collect(Collectors.toMap(KnowledgeSnapshot.Doc::key, KnowledgeSnapshot.Doc::content, (a, b) -> a));
        PricingProperties pricing = pick(byKey, RuleCodec.PRICING, RuleCodec::pricing, fallback.pricing());
        LeadScoringRule lead = pick(byKey, RuleCodec.LEAD_SCORING, RuleCodec::leadScoring, fallback.leadScoring());
        JsonNode regions = pick(byKey, RuleCodec.REGIONS, RuleCodec::regions, fallback.regions());
        ReplyPolicyRule reply = pick(byKey, RuleCodec.REPLY_POLICY, RuleCodec::replyPolicy, fallback.replyPolicy());
        return new RuleSet(pricing, lead, regions, reply);
    }

    private static <T> T pick(Map<String, String> byKey, String key, Function<String, RuleCodec.Parsed<T>> parser,
                              T fallback) {
        String yaml = byKey.get(key);
        if (yaml == null) {
            return fallback;
        }
        RuleCodec.Parsed<T> p = parser.apply(yaml);
        if (!p.ok()) {
            log.error("Rule {} của bản phát hành đang chạy không đọc được, dùng giá trị dự phòng: {}", key, p.errors());
            return fallback;
        }
        return p.value();
    }
}
