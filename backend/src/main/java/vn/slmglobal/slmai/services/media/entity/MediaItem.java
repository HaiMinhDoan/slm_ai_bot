package vn.slmglobal.slmai.services.media.entity;

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

/** Một media trong thư viện: nội dung và metadata để AI chọn (mục 6, 7). Bảng media (V6). */
@Entity
@Table(name = "media")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_media", nullable = false)
    private MediaType type;

    @Column(name = "tieu_de", nullable = false)
    private String title;

    /** Mã ngắn trong danh mục media (M07), cấp khi duyệt */
    @Column(name = "ma_danh_muc")
    private String indexCode;

    @Column(name = "tom_tat")
    private String summary;

    @Column(name = "dung_khi")
    private String useWhen;

    @Column(name = "tranh_khi")
    private String avoidWhen;

    @Column(name = "mo_ta")
    private String description;

    @Column(name = "loi_thoai")
    private String transcript;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "chu_de", nullable = false)
    @Builder.Default
    private String[] topics = new String[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "cac_giai_doan_khach", nullable = false)
    @Builder.Default
    private String[] customerStages = new String[0];

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "kenh_cho_phep", nullable = false)
    @Builder.Default
    private String[] allowedChannels = new String[]{"MESSENGER"};

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    @Builder.Default
    private MediaStatus status = MediaStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "quyen_su_dung", nullable = false)
    @Builder.Default
    private RightsStatus rights = RightsStatus.NEEDS_REVIEW;

    @Column(name = "nguoi_tao")
    private String createdBy;

    @Column(name = "nguoi_duyet")
    private String approvedBy;

    @Column(name = "duyet_luc")
    private OffsetDateTime approvedAt;

    @Column(name = "tao_luc", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /** Do trigger dat_sua_luc (V1) cập nhật */
    @Column(name = "sua_luc", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}
