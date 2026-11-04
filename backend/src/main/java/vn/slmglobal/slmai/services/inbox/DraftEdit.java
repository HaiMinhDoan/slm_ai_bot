package vn.slmglobal.slmai.services.inbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/** Nhân viên sửa nháp AI trước khi gửi (kt_sua_ban_nhap, V11): dữ liệu để biết AI hay sai gì, đưa vào bộ test. */
@Entity
@Table(name = "kt_sua_ban_nhap")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DraftEdit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tin_nhan_id", nullable = false)
    private UUID messageId;

    @Column(name = "luot_goi_ai_id")
    private UUID aiRunId;

    @Column(name = "noi_dung_ai", nullable = false)
    private String aiText;

    @Column(name = "noi_dung_nguoi_sua", nullable = false)
    private String humanText;

    @Column(name = "nguoi_sua", nullable = false)
    private String editedBy;

    @Column(name = "da_dua_vao_vi_du", nullable = false)
    private boolean promoted;

    @Column(name = "tao_luc", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
