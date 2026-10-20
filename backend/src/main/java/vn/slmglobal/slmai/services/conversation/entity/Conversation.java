package vn.slmglobal.slmai.services.conversation.entity;

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

/** Hội thoại với khách trên một kênh. Bảng hoi_thoai (V3, mục 19, 29). */
@Entity
@Table(name = "hoi_thoai")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "khach_hang_id", nullable = false)
    private UUID customerId;

    @Column(name = "dinh_danh_kenh_id", nullable = false)
    private UUID channelIdentityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "kenh", nullable = false)
    private ChannelCode channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "nguoi_phu_trach", nullable = false)
    @Builder.Default
    private Ownership ownership = Ownership.AI_ACTIVE;

    @Column(name = "nhan_vien_phu_trach_id")
    private String ownerStaffId;

    /** Lúc chuyển sang HUMAN_OWNED */
    @Column(name = "tiep_quan_luc")
    private OffsetDateTime takenOverAt;

    /** Tin cuối của nhân viên: để tự trả hội thoại lại cho AI (mục 19) */
    @Column(name = "nhan_vien_tin_cuoi_luc")
    private OffsetDateTime lastStaffMessageAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "che_do_tra_loi", nullable = false)
    @Builder.Default
    private ReplyMode replyMode = ReplyMode.AUTO;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    @Builder.Default
    private ConversationStatus status = ConversationStatus.OPEN;

    @Column(name = "y_dinh")
    private String intent;

    @Column(name = "tom_tat")
    private String summary;

    /** Tin cuối của khách: tính cửa sổ 24 giờ của Messenger */
    @Column(name = "tin_khach_cuoi_luc")
    private OffsetDateTime lastCustomerMessageAt;

    /** Gộp tin: worker xử lý hội thoại khi tới hạn; null = không có lượt nào đang chờ */
    @Column(name = "han_tra_loi_luc")
    private OffsetDateTime replyDueAt;

    @Column(name = "tao_luc", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /** Do trigger dat_sua_luc (V1) cập nhật */
    @Column(name = "sua_luc", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    /** Chuyển sang người phụ trách; huỷ lượt AI đang chờ để AI không chen vào. */
    public void handToHuman(String staffId, OffsetDateTime at) {
        if (ownership != Ownership.HUMAN_OWNED) {
            ownership = Ownership.HUMAN_OWNED;
            takenOverAt = at;
        }
        if (staffId != null) {
            ownerStaffId = staffId;
        }
        lastStaffMessageAt = at;
        replyDueAt = null;
    }
}
