package vn.slmglobal.slmai.services.auth.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

/** Nhật ký thao tác nhạy cảm của người dùng nội bộ. Bảng nhat_ky_thao_tac (V2). */
@Entity
@Table(name = "nhat_ky_thao_tac")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** null = hệ thống tự thực hiện */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_thuc_hien_id")
    private User actor;

    @Column(name = "hanh_dong", nullable = false)
    private String action;

    @Column(name = "loai_doi_tuong")
    private String targetType;

    @Column(name = "ma_doi_tuong")
    private String targetKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "du_lieu_truoc")
    private JsonNode beforeData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "du_lieu_sau")
    private JsonNode afterData;

    @Column(name = "dia_chi_ip")
    private String ipAddress;

    @Column(name = "tao_luc", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
