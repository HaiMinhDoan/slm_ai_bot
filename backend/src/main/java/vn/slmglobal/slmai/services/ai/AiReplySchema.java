package vn.slmglobal.slmai.services.ai;

import vn.slmglobal.slmai.services.lead.ConsultSlots;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON schema của {@link AiReply} cho structured outputs ({@code output_config.format}). Viết tay để kiểm soát
 * đúng enum và trường null; structured outputs yêu cầu mọi object có {@code additionalProperties: false}, mọi
 * thuộc tính đều required, không hỗ trợ ràng buộc độ dài / min / max (độ dài tin kiểm ở Policy).
 * Dùng LinkedHashMap để thứ tự khoá cố định giữa các lần khởi động (schema là một phần của prompt được cache).
 *
 * <p>API giới hạn {@value #MAX_UNION_PARAMS} tham số có {@code anyOf} (trường null được) cho TẤT CẢ schema strict
 * của một request gộp lại (schema này + tool strict); vượt là lỗi 400 "too many parameters with union types" và
 * mọi lượt AI chuyển nhân viên. Chỉ cho null ở chỗ thật sự cần; chuỗi "không có" dùng {@code ""}.
 */
public final class AiReplySchema {

    /** Giới hạn tham số kiểu union của structured outputs, tính gộp mọi schema strict trong một request */
    public static final int MAX_UNION_PARAMS = 16;

    /** Loại câu hỏi (mục 35) */
    public static final List<String> INTENTS = List.of(
            "CHAO_HOI", "TU_VAN_LAP_MOI", "HOI_GIA", "GIAI_THICH", "CHINH_SACH", "KY_THUAT_SU_CO", "KHIEU_NAI", "KHAC");

    public static final List<String> GOALS = List.of(
            "greet", "qualify_lead", "explain", "recommend_package", "ask_contact", "handoff", "other");

    private static final Map<String, Object> SCHEMA = build();

    private AiReplySchema() {
    }

    public static Map<String, Object> schema() {
        return SCHEMA;
    }

    private static Map<String, Object> build() {
        Map<String, Object> collected = object(ordered(
                ConsultSlots.KHU_VUC, nullable(type("string")),
                ConsultSlots.SO_PHA, nullable(ordered("type", "integer", "enum", List.of(1, 3))),
                ConsultSlots.HOA_DON_THANG, nullable(type("integer")),
                ConsultSlots.TY_LE_NGAY_DEM, nullable(enumOf(ConsultSlots.DAY_NIGHT_VALUES)),
                ConsultSlots.LOAI_MAI, nullable(enumOf(ConsultSlots.ROOF_VALUES)),
                ConsultSlots.DIEN_TICH_MAI, nullable(type("number")),
                ConsultSlots.TEN_KHACH, nullable(type("string")),
                ConsultSlots.DANH_XUNG, nullable(enumOf(ConsultSlots.SALUTATION_VALUES))));

        Map<String, Object> researchLevel = object(ordered(
                "value", enumOf(ConsultSlots.RESEARCH_LEVELS),
                "evidence", type("string")));
        Map<String, Object> budget = object(ordered(
                "min", nullable(type("integer")),
                "max", nullable(type("integer"))));
        // budget_vnd luôn có, chưa biết thì min và max cùng null (Budget.isKnown)
        Map<String, Object> qualification = object(ordered(
                "research_level", nullable(researchLevel),
                "budget_vnd", budget,
                "timeline", nullable(enumOf(ConsultSlots.TIMELINES))));

        Map<String, Object> handoff = object(ordered(
                "needed", type("boolean"),
                "reason", type("string")));

        return object(ordered(
                "goal", enumOf(GOALS),
                "intent", enumOf(INTENTS),
                "reply", type("string"),
                "collected", collected,
                "question_asked", nullable(enumOf(ConsultSlots.questionKeys())),
                "qualification", qualification,
                "sources", ordered("type", "array", "items", type("string")),
                "knowledge_gap", type("string"),
                "handoff", handoff));
    }

    private static Map<String, Object> object(Map<String, Object> properties) {
        Map<String, Object> o = new LinkedHashMap<>();
        o.put("type", "object");
        o.put("properties", properties);
        o.put("required", List.copyOf(properties.keySet()));
        o.put("additionalProperties", false);
        return o;
    }

    private static Map<String, Object> type(String type) {
        return ordered("type", type);
    }

    private static Map<String, Object> enumOf(List<String> values) {
        return ordered("type", "string", "enum", values);
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
