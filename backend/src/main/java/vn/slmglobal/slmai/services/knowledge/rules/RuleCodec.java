package vn.slmglobal.slmai.services.knowledge.rules;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import vn.slmglobal.slmai.services.lead.ConsultSlots;
import vn.slmglobal.slmai.services.lead.LeadScoringRule;
import vn.slmglobal.slmai.services.policy.ReplyPolicyRule;
import vn.slmglobal.slmai.services.sizing.PricingProperties;
import vn.slmglobal.slmai.services.sizing.ProvinceDirectory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Đọc, ghi và kiểm tra 4 rule YAML (mục 5.2, 5.3). Khoá YAML giữ tiếng Anh như kế hoạch (mục 29, quy ước đặt tên),
 * riêng rules/tinh-mien giữ cấu trúc data/tinh-thanh.json. Kiểm tra trên cây YAML thô để báo đúng ô sai, không để
 * giá trị mặc định của record che mất lỗi.
 */
public final class RuleCodec {

    public static final String PRICING = "rules/bao-gia";
    public static final String LEAD_SCORING = "rules/phan-loai-khach";
    public static final String REGIONS = "rules/tinh-mien";
    public static final String REPLY_POLICY = "rules/kiem-tra-truoc-khi-gui";
    public static final List<String> KEYS = List.of(PRICING, LEAD_SCORING, REGIONS, REPLY_POLICY);

    public static final Map<String, String> TITLES = Map.of(
            PRICING, "Thông số báo giá",
            LEAD_SCORING, "Ngưỡng phân loại khách",
            REGIONS, "Tỉnh → miền → bảng giá",
            REPLY_POLICY, "Kiểm tra trước khi gửi");

    private static final YAMLMapper YAML = YAMLMapper.builder(YAMLFactory.builder()
                    .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                    .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
                    .enable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
                    .build())
            .build();

    private RuleCodec() {
    }

    // ------------------------------------------------------------------ ghi

    public static String write(String key, Object value) {
        Object tree = switch (key) {
            case PRICING -> pricingTree((PricingProperties) value);
            case LEAD_SCORING -> leadScoringTree((LeadScoringRule) value);
            case REPLY_POLICY -> replyPolicyTree((ReplyPolicyRule) value);
            case REGIONS -> value;
            default -> throw new IllegalArgumentException("Không có rule " + key);
        };
        try {
            return YAML.writeValueAsString(tree);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Map<String, Object> pricingTree(PricingProperties p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("price_per_kwh", p.pricePerKwh());
        Map<String, Object> night = new LinkedHashMap<>();
        for (String k : ConsultSlots.DAY_NIGHT_VALUES) {
            if (p.nightShare().containsKey(k)) {
                night.put(k, p.nightShare().get(k));
            }
        }
        m.put("night_share", night);
        m.put("battery_depth", p.batteryDepth());
        m.put("coverage", p.coverage());
        m.put("cheaper_coverage", p.cheaperCoverage());
        Map<String, Object> battery = new LinkedHashMap<>();
        for (String k : ConsultSlots.DAY_NIGHT_VALUES) {
            if (p.batteryPerMillion().containsKey(k)) {
                battery.put(k, p.batteryPerMillion().get(k));
            }
        }
        m.put("battery_per_million", battery);
        m.put("roof_area_per_kwp", p.roofAreaPerKwp());
        m.put("large_system_kwp", p.largeSystemKwp());
        Map<String, Object> domains = new LinkedHashMap<>();
        for (String r : List.of("BAC", "NAM")) {
            if (p.productLinkDomains().containsKey(r)) {
                domains.put(r, p.productLinkDomains().get(r));
            }
        }
        m.put("product_link_domains", domains);
        m.put("quote_conditions", p.quoteConditions());
        m.put("flat_roof_note", p.flatRoofNote());
        return m;
    }

    private static Map<String, Object> leadScoringTree(LeadScoringRule r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("soon_timelines", ConsultSlots.TIMELINES.stream().filter(r.soonTimelines()::contains).toList());
        m.put("warm_answered_questions", r.warmAnsweredQuestions());
        m.put("low_budget_ratio", r.lowBudgetRatio());
        return m;
    }

    private static Map<String, Object> replyPolicyTree(ReplyPolicyRule r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("max_length", r.maxLength());
        m.put("one_question_only", r.oneQuestionOnly());
        m.put("number_tolerance", r.numberTolerance());
        m.put("extra_allowed_links", r.extraAllowedLinks());
        return m;
    }

    // ------------------------------------------------------------------ đọc + kiểm tra

    /** Kết quả đọc một rule: giá trị (null khi có lỗi) và danh sách lỗi */
    public record Parsed<T>(T value, List<String> errors) {

        public boolean ok() {
            return errors.isEmpty();
        }
    }

    /** Lỗi của nội dung YAML một rule; rỗng nếu dùng được */
    public static List<String> validate(String key, String yaml) {
        return switch (key) {
            case PRICING -> pricing(yaml).errors();
            case LEAD_SCORING -> leadScoring(yaml).errors();
            case REGIONS -> regions(yaml).errors();
            case REPLY_POLICY -> replyPolicy(yaml).errors();
            default -> List.of("Không có rule " + key + "; các rule được hỗ trợ: " + KEYS);
        };
    }

    /** Đọc thành cây JSON (cho form trên admin) */
    public static Parsed<JsonNode> tree(String yaml) {
        try {
            JsonNode n = YAML.readTree(yaml == null ? "" : yaml);
            if (n == null || !n.isObject()) {
                return new Parsed<>(null, List.of("Nội dung phải là một khối YAML dạng khoá: giá trị"));
            }
            return new Parsed<>(n, List.of());
        } catch (JsonProcessingException e) {
            return new Parsed<>(null, List.of("YAML sai cú pháp: " + e.getOriginalMessage()));
        }
    }

    public static Parsed<PricingProperties> pricing(String yaml) {
        Parsed<JsonNode> t = tree(yaml);
        if (!t.ok()) {
            return new Parsed<>(null, t.errors());
        }
        JsonNode n = t.value();
        List<String> errors = new ArrayList<>();
        // capacity_tolerance: bỏ từ 2026-11-12 (chọn gói gần nhất); bản phát hành cũ còn trường này vẫn đọc được
        unknown(n, Set.of("price_per_kwh", "night_share", "battery_depth", "coverage", "cheaper_coverage",
                "battery_per_million", "capacity_tolerance", "roof_area_per_kwp", "large_system_kwp",
                "product_link_domains", "quote_conditions", "flat_roof_note"), errors);
        double price = number(n, "price_per_kwh", 500, 20_000, errors);
        Map<String, Double> night = new LinkedHashMap<>();
        JsonNode ns = n.path("night_share");
        for (String k : ConsultSlots.DAY_NIGHT_VALUES) {
            double v = number(ns, k, 0.01, 0.99, errors, "night_share." + k);
            night.put(k, v);
        }
        unknown(ns, Set.copyOf(ConsultSlots.DAY_NIGHT_VALUES), errors, "night_share");
        boolean nightNumbers = night.values().stream().noneMatch(v -> Double.isNaN(v));
        if (nightNumbers && !(night.get("NGAY_NHIEU") <= night.get("CAN_BANG")
                && night.get("CAN_BANG") <= night.get("DEM_NHIEU"))) {
            errors.add("night_share: tỷ lệ điện đêm phải tăng dần: khách dùng ngày nhiều ≤ cân bằng ≤ dùng đêm nhiều");
        }
        double depth = number(n, "battery_depth", 0.1, 1, errors);
        // Ba trường dưới có từ 2026-11-12: bản phát hành cũ thiếu thì dùng mặc định của PricingProperties
        double coverage = n.has("coverage") ? number(n, "coverage", 0.3, 1.5, errors) : 0;
        double cheaper = n.has("cheaper_coverage") ? number(n, "cheaper_coverage", 0.3, 1.5, errors) : 0;
        if (!Double.isNaN(coverage) && !Double.isNaN(cheaper)
                && (cheaper > 0 ? cheaper : 0.8) >= (coverage > 0 ? coverage : 1)) {
            errors.add("cheaper_coverage: mức bù khi khách chê đắt phải nhỏ hơn mức bù của đề xuất đầu");
        }
        Map<String, Double> battery = null;
        if (n.has("battery_per_million")) {
            battery = new LinkedHashMap<>();
            JsonNode bp = n.path("battery_per_million");
            for (String k : ConsultSlots.DAY_NIGHT_VALUES) {
                battery.put(k, number(bp, k, 0, 20, errors, "battery_per_million." + k));
            }
            unknown(bp, Set.copyOf(ConsultSlots.DAY_NIGHT_VALUES), errors, "battery_per_million");
            if (battery.values().stream().noneMatch(v -> Double.isNaN(v))
                    && !(battery.get("NGAY_NHIEU") <= battery.get("CAN_BANG")
                    && battery.get("CAN_BANG") <= battery.get("DEM_NHIEU"))) {
                errors.add("battery_per_million: pin phải tăng dần: khách dùng ngày nhiều ≤ cân bằng ≤ dùng đêm "
                        + "nhiều");
            }
        }
        double roof = number(n, "roof_area_per_kwp", 1, 20, errors);
        double large = number(n, "large_system_kwp", 1, 1000, errors);
        Map<String, String> domains = new LinkedHashMap<>();
        JsonNode d = n.path("product_link_domains");
        for (String r : List.of("BAC", "NAM")) {
            String v = text(d, r, true, 200, errors, "product_link_domains." + r);
            if (v != null && !v.matches("(?i)[a-z0-9-]+(\\.[a-z0-9-]+)+")) {
                errors.add("product_link_domains." + r + ": chỉ ghi tên miền, ví dụ slmsolar.vn");
            }
            domains.put(r, v);
        }
        unknown(d, Set.of("BAC", "NAM"), errors, "product_link_domains");
        String conditions = text(n, "quote_conditions", true, 1000, errors, "quote_conditions");
        String flatRoof = text(n, "flat_roof_note", false, 1000, errors, "flat_roof_note");
        if (!errors.isEmpty()) {
            return new Parsed<>(null, errors);
        }
        return new Parsed<>(new PricingProperties((long) price, night, depth, coverage, cheaper, battery, roof, large,
                domains, conditions, flatRoof), List.of());
    }

    public static Parsed<LeadScoringRule> leadScoring(String yaml) {
        Parsed<JsonNode> t = tree(yaml);
        if (!t.ok()) {
            return new Parsed<>(null, t.errors());
        }
        JsonNode n = t.value();
        List<String> errors = new ArrayList<>();
        unknown(n, Set.of("soon_timelines", "warm_answered_questions", "low_budget_ratio"), errors);
        Set<String> soon = new LinkedHashSet<>();
        JsonNode st = n.path("soon_timelines");
        if (!st.isArray()) {
            errors.add("soon_timelines: phải là danh sách");
        } else {
            st.forEach(x -> soon.add(x.asText()));
        }
        double warm = number(n, "warm_answered_questions", 1, 10, errors);
        if (warm != Math.rint(warm)) {
            errors.add("warm_answered_questions: phải là số nguyên");
        }
        double ratio = number(n, "low_budget_ratio", 0.01, 1, errors);
        if (!errors.isEmpty()) {
            return new Parsed<>(null, errors);
        }
        LeadScoringRule rule = new LeadScoringRule(soon, (int) warm, ratio);
        List<String> more = rule.validate();
        return more.isEmpty() ? new Parsed<>(rule, List.of()) : new Parsed<>(null, more);
    }

    public static Parsed<JsonNode> regions(String yaml) {
        Parsed<JsonNode> t = tree(yaml);
        if (!t.ok()) {
            return t;
        }
        List<String> errors = new ArrayList<>(ProvinceDirectory.validate(t.value()));
        unknown(t.value(), Set.of("nguon", "mien", "tinh"), errors);
        return errors.isEmpty() ? t : new Parsed<>(null, errors);
    }

    public static Parsed<ReplyPolicyRule> replyPolicy(String yaml) {
        Parsed<JsonNode> t = tree(yaml);
        if (!t.ok()) {
            return new Parsed<>(null, t.errors());
        }
        JsonNode n = t.value();
        List<String> errors = new ArrayList<>();
        unknown(n, Set.of("max_length", "one_question_only", "number_tolerance", "extra_allowed_links"), errors);
        double max = number(n, "max_length", 100, 2000, errors);
        if (!n.path("one_question_only").isBoolean()) {
            errors.add("one_question_only: phải là true hoặc false");
        }
        double tolerance = number(n, "number_tolerance", 0, 0.1, errors);
        List<String> links = new ArrayList<>();
        JsonNode l = n.path("extra_allowed_links");
        if (!l.isMissingNode() && !l.isNull() && !l.isArray()) {
            errors.add("extra_allowed_links: phải là danh sách");
        } else {
            l.forEach(x -> links.add(x.asText()));
        }
        if (!errors.isEmpty()) {
            return new Parsed<>(null, errors);
        }
        ReplyPolicyRule rule = new ReplyPolicyRule((int) max, n.path("one_question_only").asBoolean(), tolerance,
                links);
        List<String> more = rule.validate();
        return more.isEmpty() ? new Parsed<>(rule, List.of()) : new Parsed<>(null, more);
    }

    /**
     * Kiểm tra rồi ghi lại theo dạng chuẩn (thứ tự khoá, cách viết số cố định): cùng giá trị thì cùng nội dung, cùng
     * mã băm, dù sửa bằng form hay gõ YAML.
     */
    public static Parsed<String> canonical(String key, String yaml) {
        Parsed<?> p = switch (key) {
            case PRICING -> pricing(yaml);
            case LEAD_SCORING -> leadScoring(yaml);
            case REGIONS -> regions(yaml);
            case REPLY_POLICY -> replyPolicy(yaml);
            default -> new Parsed<>(null, List.of("Không có rule " + key));
        };
        return p.ok() ? new Parsed<>(write(key, p.value()), List.of()) : new Parsed<>(null, p.errors());
    }

    /** Như {@link #canonical(String, String)}, nhận giá trị từ form (JSON) */
    public static Parsed<String> canonical(String key, JsonNode json) {
        try {
            return canonical(key, YAML.writeValueAsString(json));
        } catch (JsonProcessingException e) {
            return new Parsed<>(null, List.of("Giá trị không hợp lệ: " + e.getOriginalMessage()));
        }
    }

    /** YAML → JSON để form trên admin đọc; YAML sai trả null */
    public static JsonNode toJson(String yaml) {
        Parsed<JsonNode> t = tree(yaml);
        return t.ok() ? t.value() : null;
    }

    /** JSON từ form → YAML để lưu nháp */
    public static String fromJson(JsonNode json, ObjectMapper mapper) {
        try {
            return YAML.writeValueAsString(mapper.treeToValue(json, Object.class));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getOriginalMessage());
        }
    }

    // ------------------------------------------------------------------ tiện ích

    private static void unknown(JsonNode n, Set<String> allowed, List<String> errors) {
        unknown(n, allowed, errors, null);
    }

    private static void unknown(JsonNode n, Set<String> allowed, List<String> errors, String prefix) {
        if (!(n instanceof ObjectNode o)) {
            return;
        }
        for (Iterator<String> it = o.fieldNames(); it.hasNext(); ) {
            String k = it.next();
            if (!allowed.contains(k)) {
                errors.add((prefix == null ? "" : prefix + ".") + k + ": trường không được hỗ trợ");
            }
        }
    }

    private static double number(JsonNode n, String field, double min, double max, List<String> errors) {
        return number(n, field, min, max, errors, field);
    }

    private static double number(JsonNode n, String field, double min, double max, List<String> errors,
                                 String label) {
        JsonNode v = n.path(field);
        double d;
        if (v.isNumber()) {
            d = v.asDouble();
        } else if (v.isTextual() && v.asText().strip().replace(',', '.').matches("-?\\d+(\\.\\d+)?")) {
            d = Double.parseDouble(v.asText().strip().replace(',', '.'));
        } else {
            errors.add(label + ": thiếu hoặc không phải số");
            return Double.NaN;
        }
        if (d < min || d > max) {
            errors.add(label + ": phải từ " + plain(min) + " đến " + plain(max));
        }
        return d;
    }

    private static String text(JsonNode n, String field, boolean required, int maxLength, List<String> errors,
                               String label) {
        JsonNode v = n.path(field);
        if (v.isMissingNode() || v.isNull() || v.asText().isBlank()) {
            if (required) {
                errors.add(label + ": bắt buộc");
            }
            return null;
        }
        String s = v.asText().strip();
        if (s.length() > maxLength) {
            errors.add(label + ": tối đa " + maxLength + " ký tự");
        }
        return s;
    }

    private static String plain(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
    }
}
