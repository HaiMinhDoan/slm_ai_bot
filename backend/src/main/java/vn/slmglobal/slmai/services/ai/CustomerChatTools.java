package vn.slmglobal.slmai.services.ai;

import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.Tool;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.slmglobal.slmai.services.lead.ConsultSlots;
import vn.slmglobal.slmai.services.lead.entity.ConsultProfile;
import vn.slmglobal.slmai.services.sizing.PackageAdvisor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tool báo giá cho lượt trả lời khách (mục 4.2.6): {@code tim_goi_phu_hop}, {@code chi_tiet_goi}. Định nghĩa cố định
 * (thứ tự khoá không đổi) vì tool nằm đầu prefix được cache. Schema strict: Claude luôn gửi đủ trường, chưa biết thì
 * null; trường null được điền từ hồ sơ tư vấn đã thu.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerChatTools {

    public static final String FIND_PACKAGES = "tim_goi_phu_hop";
    public static final String PACKAGE_DETAIL = "chi_tiet_goi";

    private static final Pattern URL = Pattern.compile("https?://[^\\s\"<>]+");

    private final PackageAdvisor advisor;
    private final ObjectMapper objectMapper;

    private final List<Tool> definitions = buildDefinitions();

    /** Có gói được gợi ý trong catalog: chưa đồng bộ lần nào thì không đưa tool vào prompt */
    public boolean available() {
        return advisor.available();
    }

    public List<Tool> definitions() {
        return definitions;
    }

    /** Phiên tool của một lượt: gắn hồ sơ tư vấn, ghi lại mọi lần gọi */
    public Session session(ConsultProfile profile) {
        return new Session(profile);
    }

    /** Các lần gọi tool trong một lượt; cũng là nguồn hợp lệ cho kiểm tra con số và link (mục 33.1) */
    public final class Session implements ClaudeChatClient.ToolHandler {

        private final ConsultProfile profile;
        private final List<ClaudeChatClient.ToolCall> calls = new ArrayList<>();
        private final List<Integer> quotedPackageIds = new ArrayList<>();
        private Long cheapestQuotedVnd;

        private Session(ConsultProfile profile) {
            this.profile = profile;
        }

        @Override
        public ClaudeChatClient.ToolOutput handle(String name, JsonNode input) {
            long started = System.nanoTime();
            ClaudeChatClient.ToolOutput out;
            try {
                out = switch (name) {
                    case FIND_PACKAGES -> findPackages(input);
                    case PACKAGE_DETAIL -> packageDetail(input);
                    default -> new ClaudeChatClient.ToolOutput("Không có công cụ " + name, true);
                };
            } catch (RuntimeException e) {
                log.warn("Tool {} lỗi: {}", name, e.toString());
                out = new ClaudeChatClient.ToolOutput("Tra cứu lỗi, không đưa con số; mời khách để lại số điện "
                        + "thoại để nhân viên báo giá.", true);
            }
            calls.add(new ClaudeChatClient.ToolCall(name, input, out.content(), out.error(),
                    (int) ((System.nanoTime() - started) / 1_000_000)));
            return out;
        }

        public List<ClaudeChatClient.ToolCall> calls() {
            return List.copyOf(calls);
        }

        /** Kết quả tool (JSON) dùng làm nguồn con số */
        public List<String> outputs() {
            return calls.stream().filter(c -> !c.error()).map(ClaudeChatClient.ToolCall::output).toList();
        }

        /** Link có trong kết quả tool: AI chỉ được gửi các link này */
        public List<String> links() {
            List<String> out = new ArrayList<>();
            for (String o : outputs()) {
                Matcher m = URL.matcher(o);
                while (m.find()) {
                    out.add(m.group());
                }
            }
            return out;
        }

        public List<Integer> quotedPackageIds() {
            return List.copyOf(quotedPackageIds);
        }

        public Long cheapestQuotedVnd() {
            return cheapestQuotedVnd;
        }

        private ClaudeChatClient.ToolOutput findPackages(JsonNode in) {
            JsonNode slots = profile == null ? objectMapper.createObjectNode() : profile.getSlots();
            PackageAdvisor.Advice advice = advisor.advise(new PackageAdvisor.AdviceRequest(
                    text(in, ConsultSlots.KHU_VUC, slots),
                    integer(in, ConsultSlots.SO_PHA, slots),
                    longValue(in, ConsultSlots.HOA_DON_THANG, slots),
                    text(in, ConsultSlots.TY_LE_NGAY_DEM, slots),
                    text(in, ConsultSlots.LOAI_MAI, slots),
                    number(in, ConsultSlots.DIEN_TICH_MAI, slots),
                    text(in, "loai_he_thong", null),
                    in.path("can_du_phong_mat_dien").isBoolean() ? in.get("can_du_phong_mat_dien").asBoolean() : null));
            advice.quotedPackageIds().forEach(id -> {
                if (!quotedPackageIds.contains(id)) {
                    quotedPackageIds.add(id);
                }
            });
            if (advice.cheapestQuotedVnd() != null && (cheapestQuotedVnd == null
                    || advice.cheapestQuotedVnd() < cheapestQuotedVnd)) {
                cheapestQuotedVnd = advice.cheapestQuotedVnd();
            }
            return new ClaudeChatClient.ToolOutput(json(advice.result()), false);
        }

        private ClaudeChatClient.ToolOutput packageDetail(JsonNode in) {
            if (!in.path("goi_id").canConvertToInt()) {
                return new ClaudeChatClient.ToolOutput("Thiếu goi_id", true);
            }
            JsonNode slots = profile == null ? objectMapper.createObjectNode() : profile.getSlots();
            ObjectNode out = advisor.packageDetail(in.get("goi_id").asInt(), text(in, ConsultSlots.KHU_VUC, slots));
            return new ClaudeChatClient.ToolOutput(json(out), out.has("loi"));
        }
    }

    private String json(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String text(JsonNode in, String key, JsonNode fallback) {
        JsonNode v = in.path(key);
        if (v.isTextual() && !v.asText().isBlank()) {
            return v.asText().strip();
        }
        JsonNode f = fallback == null ? null : fallback.get(key);
        return f != null && f.isTextual() && !f.asText().isBlank() ? f.asText().strip() : null;
    }

    private static Integer integer(JsonNode in, String key, JsonNode fallback) {
        JsonNode v = in.path(key).isNumber() ? in.get(key) : fallback == null ? null : fallback.get(key);
        return v != null && v.isNumber() ? v.asInt() : null;
    }

    private static Long longValue(JsonNode in, String key, JsonNode fallback) {
        JsonNode v = in.path(key).isNumber() ? in.get(key) : fallback == null ? null : fallback.get(key);
        return v != null && v.isNumber() ? v.asLong() : null;
    }

    private static Double number(JsonNode in, String key, JsonNode fallback) {
        JsonNode v = in.path(key).isNumber() ? in.get(key) : fallback == null ? null : fallback.get(key);
        return v != null && v.isNumber() ? v.asDouble() : null;
    }

    // ------------------------------------------------------------------ định nghĩa tool

    private static List<Tool> buildDefinitions() {
        Map<String, Object> find = new LinkedHashMap<>();
        find.put(ConsultSlots.KHU_VUC, nullable(described(type("string"),
                "Tỉnh / thành phố khách nói, giữ nguyên tên khách dùng (tên mới hoặc tên tỉnh cũ trước 2025)")));
        find.put(ConsultSlots.SO_PHA, nullable(ordered("type", "integer", "enum", List.of(1, 3))));
        find.put(ConsultSlots.HOA_DON_THANG, nullable(described(type("integer"),
                "Tiền điện mỗi tháng, đơn vị đồng (2 triệu là 2000000)")));
        find.put(ConsultSlots.TY_LE_NGAY_DEM, nullable(enumOf(ConsultSlots.DAY_NIGHT_VALUES)));
        find.put(ConsultSlots.LOAI_MAI, nullable(enumOf(ConsultSlots.ROOF_VALUES)));
        find.put(ConsultSlots.DIEN_TICH_MAI, nullable(described(type("number"), "Diện tích mái, m2")));
        find.put("loai_he_thong", nullable(described(enumOf(List.of("ON_GRID", "HYBRID")),
                "Chỉ điền khi khách nói rõ muốn hệ hòa lưới (ON_GRID) hay hệ có pin lưu trữ (HYBRID)")));
        find.put("can_du_phong_mat_dien", nullable(described(type("boolean"),
                "true khi khách cần có điện lúc lưới mất điện")));

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("goi_id", described(type("integer"), "goi_id trong kết quả tim_goi_phu_hop"));
        detail.put(ConsultSlots.KHU_VUC, nullable(described(type("string"),
                "Tỉnh / thành phố của khách nếu đã biết, để chọn đúng bảng giá, link và tính sản lượng")));

        return List.of(
                tool(FIND_PACKAGES, """
                        Tra bảng giá và chọn gói điện mặt trời cho khách. Gọi khi khách hỏi giá, chi phí, nên lắp \
                        hệ nào, hoặc khi khách vừa cho thêm thông tin (tỉnh, tiền điện, 1 pha hay 3 pha, dùng điện \
                        ngày hay đêm, loại mái). Điền mọi thông tin đã biết từ hồ sơ tư vấn và hội thoại, chưa biết \
                        thì null. Chưa có tiền điện: trả giá khởi điểm của từng loại hệ. Có tiền điện: trả gói đề \
                        xuất, giá, sản lượng, tiết kiệm ước tính và link sản phẩm. Mọi con số giá, công suất, sản \
                        lượng, tiết kiệm nói với khách phải lấy từ kết quả này, chép đúng cách viết.""", find),
                tool(PACKAGE_DETAIL, """
                        Chi tiết một gói theo goi_id trong kết quả tim_goi_phu_hop: thành phần (tấm pin, inverter, \
                        pin lưu trữ, nhân công), hãng, bảo hành, giá, link, sản lượng theo tỉnh. Gọi khi khách hỏi \
                        gói gồm những gì, hãng nào, bảo hành bao lâu.""", detail));
    }

    private static Tool tool(String name, String description, Map<String, Object> properties) {
        Tool.InputSchema.Properties.Builder props = Tool.InputSchema.Properties.builder();
        properties.forEach((k, v) -> props.putAdditionalProperty(k, JsonValue.from(v)));
        return Tool.builder()
                .name(name)
                .description(description)
                .strict(true)
                .inputSchema(Tool.InputSchema.builder()
                        .properties(props.build())
                        .required(List.copyOf(properties.keySet()))
                        .putAdditionalProperty("additionalProperties", JsonValue.from(false))
                        .build())
                .build();
    }

    private static Map<String, Object> type(String type) {
        return ordered("type", type);
    }

    private static Map<String, Object> enumOf(List<String> values) {
        return ordered("type", "string", "enum", values);
    }

    private static Map<String, Object> described(Map<String, Object> schema, String description) {
        Map<String, Object> m = new LinkedHashMap<>(schema);
        m.put("description", description);
        return m;
    }

    private static Map<String, Object> nullable(Map<String, Object> schema) {
        return ordered("anyOf", List.of(schema, type("null")));
    }

    private static Map<String, Object> ordered(Object... keyValues) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            m.put((String) keyValues[i], keyValues[i + 1]);
        }
        return m;
    }
}
