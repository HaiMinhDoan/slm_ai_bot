package vn.slmglobal.slmai.services.knowledge.assistant;

import vn.slmglobal.slmai.services.knowledge.KnowledgeDocType;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Khuôn kết quả của AI hỗ trợ soạn (structured outputs). Không có trường null nào nên không dính giới hạn 16 tham số
 * union của API; "không có" dùng chuỗi rỗng / danh sách rỗng.
 */
final class AssistantSchema {

    private static final Map<String, Object> SCHEMA = build();

    private AssistantSchema() {
    }

    static Map<String, Object> schema() {
        return SCHEMA;
    }

    private static Map<String, Object> build() {
        Map<String, Object> proposal = object(ordered(
                "action", enumOf(List.of("CREATE", "UPDATE", "RETIRE")),
                "doc_key", type("string"),
                "doc_type", enumOf(Arrays.stream(KnowledgeDocType.values()).map(Enum::name).toList()),
                "title", type("string"),
                "content", type("string"),
                "change_summary", type("string"),
                "reason", type("string"),
                "conflicts", ordered("type", "array", "items", type("string"))));
        return object(ordered(
                "reply", type("string"),
                "proposals", ordered("type", "array", "items", proposal)));
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

    private static Map<String, Object> ordered(Object... keyValues) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            m.put((String) keyValues[i], keyValues[i + 1]);
        }
        return m;
    }
}
