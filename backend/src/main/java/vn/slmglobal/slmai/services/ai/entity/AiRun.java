package vn.slmglobal.slmai.services.ai.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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

/** Một lần gọi Claude: kiểm tra lại, tính chi phí, làm bộ test. Bảng luot_goi_ai (V3, V4). */
@Entity
@Table(name = "luot_goi_ai")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "muc_dich", nullable = false)
    @Builder.Default
    private AiPurpose purpose = AiPurpose.CUSTOMER_CHAT;

    @Column(name = "hoi_thoai_id")
    private UUID conversationId;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "phien_ban_kt", nullable = false)
    private String kbVersion;

    @Column(name = "phien_ban_catalog", nullable = false)
    private String catalogVersion;

    @Column(name = "phien_ban_media", nullable = false)
    private String mediaVersion;

    @Column(name = "phien_ban_prompt")
    private String promptVersion;

    @Column(name = "token_dau_vao")
    private Integer inputTokens;

    @Column(name = "token_dau_ra")
    private Integer outputTokens;

    @Column(name = "token_doc_cache")
    private Integer cacheReadTokens;

    @Column(name = "token_ghi_cache")
    private Integer cacheWriteTokens;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cac_lan_goi_tool")
    private JsonNode toolCalls;

    /** Mã tài liệu kiến thức AI dẫn nguồn */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "nguon_tham_chieu")
    private String[] sources;

    @Column(name = "co_lo_hong_kt", nullable = false)
    private boolean knowledgeGap;

    /** Quyết định của Policy: hành động + lý do (mục 33, 35) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ket_qua_policy")
    private JsonNode policyResult;

    @Column(name = "do_tre_ms")
    private Integer latencyMs;

    /** Kết quả có cấu trúc của AI (mục 18), V4 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ket_qua_ai")
    private JsonNode output;

    @Column(name = "ly_do_dung")
    private String stopReason;

    @Column(name = "loi")
    private String error;

    @Column(name = "tao_luc", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
