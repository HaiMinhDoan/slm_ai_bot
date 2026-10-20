package vn.slmglobal.slmai.services.auth.entity;

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

/** Người dùng nội bộ của trang quản trị. Bảng nguoi_dung (V2). */
@Entity
@Table(name = "nguoi_dung")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", nullable = false)
    private String email;

    /** Cột sinh ở DB (lower(email)), chỉ đọc */
    @Column(name = "email_chuan_hoa", insertable = false, updatable = false)
    private String emailNormalized;

    @Column(name = "mat_khau_bam", nullable = false)
    private String passwordHash;

    @Column(name = "ho_ten", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "ly_do_khoa")
    private String lockReason;

    @Column(name = "dang_nhap_cuoi_luc")
    private OffsetDateTime lastLoginAt;

    @Column(name = "tao_luc", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /** Trigger dat_sua_luc tự cập nhật khi UPDATE */
    @Column(name = "sua_luc", nullable = false)
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();
}
