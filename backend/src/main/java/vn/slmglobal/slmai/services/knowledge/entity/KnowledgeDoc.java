package vn.slmglobal.slmai.services.knowledge.entity;

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
import vn.slmglobal.slmai.services.knowledge.KnowledgeDocType;

import java.time.OffsetDateTime;

/** Một tài liệu kiến thức; nội dung nằm ở các phiên bản (kt_tai_lieu, V10, mục 5.2). */
@Entity
@Table(name = "kt_tai_lieu")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeDoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** core/identity, playbooks/khach-hoi-gia, rules/bao-gia */
    @Column(name = "ma_tai_lieu", nullable = false, unique = true)
    private String key;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_tai_lieu", nullable = false)
    private KnowledgeDocType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "dinh_dang", nullable = false)
    private KnowledgeDocType.DocFormat format;

    /** Lưu trữ: không đưa vào bản phát hành mới */
    @Column(name = "da_luu_tru", nullable = false)
    private boolean archived;

    @Column(name = "nguoi_tao")
    private String createdBy;

    @Column(name = "tao_luc", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
