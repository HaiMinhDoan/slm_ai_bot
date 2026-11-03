package vn.slmglobal.slmai.services.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Giới hạn độ phức tạp schema của structured outputs: tối đa {@link AiReplySchema#MAX_UNION_PARAMS} tham số
 * {@code anyOf} gộp mọi schema strict trong một request. Vượt là lỗi 400 và mọi lượt AI chuyển nhân viên
 * (sự cố 2026-11-03: schema kết quả 16 + hai tool báo giá strict 9 = 25).
 */
class AiReplySchemaTest {

    @Test
    void strictSchemasStayUnderUnionLimit() {
        // Tool báo giá không strict nên không tính vào giới hạn; tool strict mới thì phải cộng vào đây
        CustomerChatTools tools = new CustomerChatTools(null, new ObjectMapper());
        assertThat(tools.definitions()).allSatisfy(t -> assertThat(t.strict().orElse(false)).isFalse());

        assertThat(unions(AiReplySchema.schema())).isLessThanOrEqualTo(AiReplySchema.MAX_UNION_PARAMS);
    }

    @Test
    void replyParsesEmptyStringsAndUnknownBudget() throws Exception {
        AiReply reply = new ObjectMapper().readValue("""
                {"goal":"greet","intent":"CHAO_HOI","reply":"Dạ em chào anh","collected":{},
                 "question_asked":null,"qualification":{"research_level":null,
                 "budget_vnd":{"min":null,"max":null},"timeline":null},
                 "sources":[],"knowledge_gap":"","handoff":{"needed":false,"reason":""}}""", AiReply.class);

        assertThat(reply.qualification().budgetVnd().isKnown()).isFalse();
        assertThat(reply.knowledgeGap()).isBlank();
        assertThat(reply.handoffNeeded()).isFalse();
    }

    private static int unions(Object node) {
        int n = 0;
        if (node instanceof Map<?, ?> map) {
            if (map.containsKey("anyOf")) {
                n++;
            }
            for (Object v : map.values()) {
                n += unions(v);
            }
        } else if (node instanceof Collection<?> list) {
            for (Object v : list) {
                n += unions(v);
            }
        }
        return n;
    }
}
