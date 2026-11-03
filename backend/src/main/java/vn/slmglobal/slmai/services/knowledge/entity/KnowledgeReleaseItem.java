package vn.slmglobal.slmai.services.knowledge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** Bản phát hành gồm phiên bản nào của tài liệu nào (kt_phat_hanh_chi_tiet, V10). */
@Entity
@Table(name = "kt_phat_hanh_chi_tiet")
@IdClass(KnowledgeReleaseItem.Key.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeReleaseItem {

    @Id
    @Column(name = "phat_hanh_id")
    private Long releaseId;

    @Id
    @Column(name = "tai_lieu_id")
    private Long docId;

    @Column(name = "phien_ban_id", nullable = false)
    private Long versionId;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Key implements Serializable {
        private Long releaseId;
        private Long docId;
    }
}
