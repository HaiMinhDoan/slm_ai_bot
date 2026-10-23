package vn.slmglobal.slmai.services.lead.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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

/** Khách để lại số điện thoại, chờ gửi sang Sales API (mục 23). Bảng khach_tiem_nang (V3). */
@Entity
@Table(name = "khach_tiem_nang")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "khach_hang_id", nullable = false)
    private UUID customerId;

    @Column(name = "hoi_thoai_id")
    private UUID conversationId;

    /** Mã kênh (mục 54.1) */
    @Column(name = "kenh_nguon", nullable = false)
    private String sourceChannel;

    @Column(name = "tinh")
    private String province;

    /** BAC / NAM theo danh sách tỉnh của doanh nghiệp (mục 4.2.9); chưa có danh sách thì để trống */
    @Column(name = "mien")
    private String region;

    /** Đã chuẩn hoá dạng 0xxxxxxxxx */
    @Column(name = "sdt", nullable = false)
    private String phone;

    @Column(name = "tom_tat_nhu_cau")
    private String needSummary;

    @Enumerated(EnumType.STRING)
    @Column(name = "muc_tiem_nang")
    private LeadLevel leadLevel;

    @Column(name = "ly_do_muc_tiem_nang")
    private String leadLevelReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "phan_loai", nullable = false)
    @Builder.Default
    private JsonNode qualification = JsonNodeFactory.instance.objectNode();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tron_goi_de_xuat_ids", nullable = false)
    @Builder.Default
    private Integer[] recommendedPackageIds = new Integer[0];

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    @Builder.Default
    private LeadStatus status = LeadStatus.NEW;

    @Column(name = "ma_tham_chieu_sales_api")
    private String salesApiRef;

    @Column(name = "so_lan_thu", nullable = false)
    private int attemptCount;

    /** hoi_thoai_id + ":" + sdt (mục 19): chạy lại lượt không tạo lead trùng */
    @Column(name = "khoa_chong_trung", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "loi_gan_nhat")
    private String lastError;

    @Column(name = "tao_luc", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "gui_luc")
    private OffsetDateTime sentAt;
}
