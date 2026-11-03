package vn.slmglobal.slmai.services.knowledge.assistant;

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
import vn.slmglobal.slmai.services.knowledge.KnowledgeDocType;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Đề xuất thay đổi của AI hỗ trợ soạn (kt_de_xuat_sua, mục 5.10 B). Duyệt thì thành bản nháp, không tự phát hành. */
@Entity
@Table(name = "kt_de_xuat_sua")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeProposal {

    public enum Action {
        CREATE,
        UPDATE,
        RETIRE
    }

    public enum Status {
        PROPOSED,
        ACCEPTED,
        REJECTED,
        SUPERSEDED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phien_chat_id", nullable = false)
    private UUID sessionId;

    /** Tài liệu có sẵn (UPDATE, RETIRE); null khi tạo mới */
    @Column(name = "tai_lieu_id")
    private Long docId;

    @Column(name = "ma_tai_lieu_de_xuat")
    private String docKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_tai_lieu_de_xuat")
    private KnowledgeDocType docType;

    @Column(name = "tieu_de_de_xuat")
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "hanh_dong", nullable = false)
    private Action action;

    /** Phiên bản mới nhất của tài liệu lúc AI đề xuất */
    @Column(name = "phien_ban_goc_id")
    private Long baseVersionId;

    @Column(name = "noi_dung_de_xuat")
    private String content;

    @Column(name = "tom_tat_thay_doi", nullable = false)
    private String summary;

    @Column(name = "ly_do")
    private String reason;

    /** Lint + kiểm tra mã / loại; có mục ERROR thì không duyệt được */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bao_cao_kiem_tra_nhanh")
    private JsonNode lintReport;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "mau_thuan")
    private JsonNode conflicts;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    @Builder.Default
    private Status status = Status.PROPOSED;

    @Column(name = "phien_ban_tao_ra_id")
    private Long resultVersionId;

    @Column(name = "nguoi_quyet_dinh")
    private String decidedBy;

    @Column(name = "quyet_dinh_luc")
    private OffsetDateTime decidedAt;

    @Column(name = "tao_luc", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
