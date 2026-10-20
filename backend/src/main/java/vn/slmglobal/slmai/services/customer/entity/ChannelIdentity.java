package vn.slmglobal.slmai.services.customer.entity;

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
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Định danh của khách trên một kênh: PSID theo Page, Zalo user_id theo OA... Bảng dinh_danh_kenh (V3, mục 29).
 * Duy nhất theo (kenh, tai_khoan_id, id_nguoi_dung_ngoai).
 */
@Entity
@Table(name = "dinh_danh_kenh")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "khach_hang_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "kenh", nullable = false)
    private ChannelCode channel;

    /** page_id / oa_id / tài khoản TikTok của công ty */
    @Column(name = "tai_khoan_id", nullable = false)
    private String accountId;

    /** PSID / Zalo user_id / ID người dùng TikTok */
    @Column(name = "id_nguoi_dung_ngoai", nullable = false)
    private String externalUserId;

    @Column(name = "ten_hien_thi")
    private String displayName;

    @Column(name = "da_quan_tam")
    private Boolean follower;

    @Column(name = "tin_den_cuoi_luc")
    private OffsetDateTime lastInboundAt;

    /** AUTO, PHONE_MATCH, HUMAN */
    @Column(name = "lien_ket_boi", nullable = false)
    @Builder.Default
    private String linkedBy = "AUTO";

    @Column(name = "tao_luc", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
