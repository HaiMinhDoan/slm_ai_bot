package vn.slmglobal.slmai.services.knowledge.entity;

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
 * Một phiên bản của tài liệu (kt_phien_ban, V10). Mỗi tài liệu tối đa một bản DRAFT; tạo bản phát hành thì các bản
 * nháp được chọn chuyển sang FROZEN và không sửa được nữa.
 */
@Entity
@Table(name = "kt_phien_ban")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeVersion {

    public enum Status {
        DRAFT,
        FROZEN,
        DISCARDED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tai_lieu_id", nullable = false)
    private Long docId;

    @Column(name = "so_phien_ban", nullable = false)
    private int versionNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private Status status;

    @Column(name = "tieu_de", nullable = false)
    private String title;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "chu_de", nullable = false)
    @Builder.Default
    private String[] topics = new String[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "cac_kenh", nullable = false)
    @Builder.Default
    private String[] channels = new String[0];

    @Column(name = "noi_dung", nullable = false)
    private String content;

    /** sha256 của nội dung */
    @Column(name = "ma_bam", nullable = false)
    private String contentHash;

    @Column(name = "so_token")
    private Integer tokenCount;

    /** Bản đang chạy lúc bắt đầu sửa: phát hiện xung đột khi lưu, so diff */
    @Column(name = "phien_ban_goc_id")
    private Long baseVersionId;

    @Column(name = "ghi_chu_thay_doi")
    private String changeNote;

    @Column(name = "tac_gia", nullable = false)
    private String author;

    /** Tạo từ đề xuất của AI hỗ trợ soạn (mục 5.10) */
    @Column(name = "co_ai_ho_tro", nullable = false)
    private boolean aiAssisted;

    @Column(name = "tao_luc", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "sua_luc", nullable = false)
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
