package vn.slmglobal.slmai.services.knowledge.assistant;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Một tin trong phiên AI hỗ trợ soạn (tl_tin_nhan), chỉ nối thêm. */
@Entity
@Table(name = "tl_tin_nhan")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssistantMessage {

    public enum Role {
        USER,
        ASSISTANT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phien_chat_id", nullable = false)
    private UUID sessionId;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "vai_tro", nullable = false)
    private Role role;

    /** USER: {text}; ASSISTANT: {reply, proposal_ids} */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "noi_dung", nullable = false)
    private JsonNode content;

    @Column(name = "luot_goi_ai_id")
    private UUID aiRunId;

    @Column(name = "tao_luc", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
