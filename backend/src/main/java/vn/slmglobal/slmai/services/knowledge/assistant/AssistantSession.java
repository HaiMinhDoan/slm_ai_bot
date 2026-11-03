package vn.slmglobal.slmai.services.knowledge.assistant;

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

/** Phiên chat của AI hỗ trợ soạn kiến thức (tl_phien_chat, loai_tro_ly = KNOWLEDGE; mục 5.10 B). */
@Entity
@Table(name = "tl_phien_chat")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantSession {

    public static final String KNOWLEDGE = "KNOWLEDGE";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Email admin */
    @Column(name = "nguoi_dung_id", nullable = false)
    private String userId;

    @Column(name = "loai_tro_ly", nullable = false)
    @Builder.Default
    private String type = KNOWLEDGE;

    @Column(name = "tieu_de")
    private String title;

    @Column(name = "tao_luc", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "tin_cuoi_luc")
    private OffsetDateTime lastMessageAt;
}
