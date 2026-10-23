package vn.slmglobal.slmai.services.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Kết quả có cấu trúc của một lượt AI trả lời khách (mục 18). Khoá JSON giữ tiếng Anh như kế hoạch; khoá của
 * {@code collected} theo tên thông tin tư vấn (consult-slots, mục 58.7). Schema gửi Claude: {@link AiReplySchema}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiReply(
        String goal,
        String intent,
        String reply,
        Collected collected,
        @JsonProperty("question_asked") String questionAsked,
        Qualification qualification,
        List<String> sources,
        @JsonProperty("knowledge_gap") String knowledgeGap,
        Handoff handoff
) {

    public boolean handoffNeeded() {
        return handoff != null && handoff.needed();
    }

    public boolean hasReply() {
        return reply != null && !reply.isBlank();
    }

    /** Thông tin khách vừa cung cấp; trường khách không nói là null */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Collected(
            @JsonProperty("khu_vuc") String province,
            @JsonProperty("so_pha") Integer phases,
            @JsonProperty("hoa_don_thang") Long monthlyBillVnd,
            @JsonProperty("ty_le_ngay_dem") String dayNightUsage,
            @JsonProperty("loai_mai") String roofType,
            @JsonProperty("dien_tich_mai") Double roofAreaM2,
            @JsonProperty("ten_khach") String customerName,
            @JsonProperty("danh_xung") String salutation
    ) {
    }

    /** Ba câu hỏi phân loại sau tư vấn (mục 58.3) */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Qualification(
            @JsonProperty("research_level") ResearchLevel researchLevel,
            @JsonProperty("budget_vnd") Budget budgetVnd,
            String timeline
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResearchLevel(String value, String evidence) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Budget(Long min, Long max) {

        public boolean isKnown() {
            return min != null || max != null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Handoff(boolean needed, String reason) {
    }
}
