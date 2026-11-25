package vn.slmglobal.slmai.services.leadnurturing.entity;

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

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Một lần nhắc khách im lặng, hoặc một lần báo nhân viên khi cửa sổ 24 giờ đã đóng (mục 56.9). Bảng lich_nhac_khach
 * (V17).
 */
@Entity
@Table(name = "lich_nhac_khach")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowUp {

    public enum Kind {
        /** Bot nhắc khách trong cửa sổ 24 giờ */
        NHAC_KHACH,
        /** Cửa sổ đã đóng mà khách vẫn im lặng: báo nhân viên */
        BAO_NHAN_VIEN
    }

    public enum Status {
        /** Đã lập lịch, chờ worker AI chạy lượt nhắc */
        PENDING,
        SENT,
        /** Tin nhắc thành nháp chờ nhân viên duyệt */
        DRAFT,
        /** AI thấy không nên nhắc, hoặc Policy không cho: không nhắc nữa trong cửa sổ này */
        SKIPPED,
        /** Khách nhắn lại, nhân viên nhắn, hoặc điều kiện đã đổi trước khi chạy */
        CANCELLED,
        FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hoi_thoai_id", nullable = false)
    private UUID conversationId;

    @Column(name = "khach_hang_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai", nullable = false)
    private Kind kind;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    /** Lần nhắc thứ mấy trong cửa sổ */
    @Column(name = "lan_thu", nullable = false)
    @Builder.Default
    private int attemptNo = 1;

    /** tin_khach_cuoi_luc lúc lập lịch: các lần nhắc cùng mốc thuộc cùng một cửa sổ 24 giờ */
    @Column(name = "moc_tin_khach", nullable = false)
    private OffsetDateTime windowAnchor;

    @Column(name = "khoa_chong_trung", nullable = false)
    private String idempotencyKey;

    @Column(name = "tin_nhan_id")
    private UUID messageId;

    @Column(name = "luot_goi_ai_id")
    private UUID aiRunId;

    @Column(name = "ly_do")
    private String reason;

    @Column(name = "xu_ly_luc")
    private OffsetDateTime processedAt;

    @Column(name = "tao_luc", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /** Do trigger dat_sua_luc (V1) cập nhật */
    @Column(name = "sua_luc", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    /** Chốt kết quả (không đổi lịch đã chốt) */
    public void finish(Status result, String why) {
        status = result;
        reason = why;
        processedAt = OffsetDateTime.now();
    }
}
