package vn.slmglobal.slmai.services.lead.entity;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Thông tin tư vấn đã thu và mức tiềm năng của một hội thoại. Bảng ho_so_tu_van (V3, V4; mục 58.3–58.5).
 * Khoá chính là hoi_thoai_id (gán tay, không sinh).
 */
@Entity
@Table(name = "ho_so_tu_van")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultProfile {

    @Id
    @Column(name = "hoi_thoai_id")
    private UUID conversationId;

    @Column(name = "khach_hang_id", nullable = false)
    private UUID customerId;

    /** Thông tin tối thiểu theo {@code ConsultSlots}: khu_vuc, so_pha, hoa_don_thang... */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "thong_tin_da_thu", nullable = false)
    @Builder.Default
    private ObjectNode slots = JsonNodeFactory.instance.objectNode();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "thong_tin_con_thieu", nullable = false)
    @Builder.Default
    private String[] missingSlots = new String[0];

    /** Số câu hỏi của AI mà khách đã trả lời (mục 58.5) */
    @Column(name = "so_cau_da_tra_loi", nullable = false)
    private int answeredQuestions;

    /** research_level, budget_vnd, timeline (mục 58.3) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "phan_loai", nullable = false)
    @Builder.Default
    private ObjectNode qualification = JsonNodeFactory.instance.objectNode();

    @Enumerated(EnumType.STRING)
    @Column(name = "muc_tiem_nang")
    private LeadLevel leadLevel;

    @Column(name = "ly_do_muc_tiem_nang")
    private String leadLevelReason;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tron_goi_de_xuat_ids", nullable = false)
    @Builder.Default
    private Integer[] recommendedPackageIds = new Integer[0];

    /** Giá gói rẻ nhất phù hợp đã báo cho khách (tool tim_goi_phu_hop, lượt đã gửi; V7): xét OFF_SEGMENT */
    @Column(name = "gia_thap_nhat_da_bao")
    private Long cheapestQuotedPrice;

    /** Câu AI vừa hỏi ở tin đã gửi (V4) */
    @Column(name = "cau_hoi_cuoi")
    private String lastQuestionAsked;

    /** Do trigger dat_sua_luc (V1) cập nhật */
    @Column(name = "sua_luc", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}
