package vn.slmglobal.slmai.services.knowledge.rules;

import com.fasterxml.jackson.databind.JsonNode;
import vn.slmglobal.slmai.services.lead.LeadScoringRule;
import vn.slmglobal.slmai.services.policy.ReplyPolicyRule;
import vn.slmglobal.slmai.services.sizing.PricingProperties;

/**
 * Các rule của bản phát hành đang chạy (mục 5.2: rules/ cho code đọc, không đưa vào prompt).
 *
 * @param pricing     rules/bao-gia: thông số chọn gói và báo giá
 * @param leadScoring rules/phan-loai-khach: ngưỡng HOT / WARM / OFF_SEGMENT
 * @param regions     rules/tinh-mien: 34 tỉnh, tên cũ, miền giá, cơ sở giá (cùng cấu trúc data/tinh-thanh.json)
 * @param replyPolicy rules/kiem-tra-truoc-khi-gui: luật kiểm tra tin trước khi gửi
 */
public record RuleSet(PricingProperties pricing, LeadScoringRule leadScoring, JsonNode regions,
                      ReplyPolicyRule replyPolicy) {
}
