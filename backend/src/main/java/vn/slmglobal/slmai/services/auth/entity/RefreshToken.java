package vn.slmglobal.slmai.services.auth.entity;

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

import java.time.OffsetDateTime;
import java.util.UUID;

/** Refresh token, chỉ lưu SHA-256. Bảng token_lam_moi (V2). */
@Entity
@Table(name = "token_lam_moi")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_dung_id", nullable = false)
    private User user;

    @Column(name = "ma_bam_token", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "het_han_luc", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "thu_hoi_luc")
    private OffsetDateTime revokedAt;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "dia_chi_ip")
    private String ipAddress;

    @Column(name = "tao_luc", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
