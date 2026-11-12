package vn.slmglobal.slmai.services.media.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

/** Chủ đề cố định gắn cho media và tài liệu kiến thức (mục 6.2), sửa trên admin. Bảng chu_de (V14). */
@Entity
@Table(name = "chu_de")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Topic {

    /** Chữ thường không dấu, gạch nối: pin-luu-tru */
    @Id
    @Column(name = "ma")
    private String code;

    @Column(name = "ten", nullable = false)
    private String name;

    @Column(name = "mo_ta")
    private String description;

    @Column(name = "thu_tu", nullable = false)
    private int sortOrder;

    /** Tắt thì không chọn mới được; media đã gắn giữ nguyên */
    @Column(name = "dang_dung", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "sua_boi")
    private String updatedBy;

    @Column(name = "tao_luc", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /** Do trigger dat_sua_luc (V1) cập nhật */
    @Column(name = "sua_luc", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}
