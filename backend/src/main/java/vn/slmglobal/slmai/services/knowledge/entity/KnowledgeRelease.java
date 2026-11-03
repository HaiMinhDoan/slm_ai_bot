package vn.slmglobal.slmai.services.knowledge.entity;

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

/**
 * Bản phát hành kiến thức: ảnh chụp bất biến các phiên bản (kt_phat_hanh, V10, mục 5.3). Tối đa một bản ACTIVE;
 * quay về bản cũ là kích hoạt lại bản SUPERSEDED đó.
 */
@Entity
@Table(name = "kt_phat_hanh")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeRelease {

    public enum Status {
        VALIDATING,
        READY,
        ACTIVE,
        FAILED,
        SUPERSEDED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** sha256 của các cặp (mã tài liệu, mã băm nội dung) trong bản phát hành */
    @Column(name = "phien_ban_kt", nullable = false, unique = true)
    private String kbVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private Status status;

    @Column(name = "ghi_chu")
    private String notes;

    /** Token của khối kiến thức bot đọc */
    @Column(name = "tong_token")
    private Integer tokenTotal;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bao_cao_kiem_tra")
    private JsonNode validationReport;

    @Column(name = "nguoi_tao", nullable = false)
    private String createdBy;

    @Column(name = "nguoi_kich_hoat")
    private String activatedBy;

    @Column(name = "tao_luc", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "kich_hoat_luc")
    private OffsetDateTime activatedAt;
}
