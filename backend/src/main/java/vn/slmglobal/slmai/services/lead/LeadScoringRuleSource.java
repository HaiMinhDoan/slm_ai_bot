package vn.slmglobal.slmai.services.lead;

/** Ngưỡng phân loại khách hiện hành: rule {@code rules/phan-loai-khach} của bản phát hành đang chạy. */
@FunctionalInterface
public interface LeadScoringRuleSource {

    LeadScoringRule leadScoring();
}
