package vn.slmglobal.slmai.services.conversation.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Một tin trong hội thoại. Bảng tin_nhan (V3, mục 29). */
@Entity
@Table(name = "tin_nhan")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "hoi_thoai_id", nullable = false)
    private UUID conversationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "chieu", nullable = false)
    private MessageDirection direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "nguoi_gui", nullable = false)
    private SenderType senderType;

    @Column(name = "nhan_vien_id")
    private String staffId;

    /** mid của Messenger; duy nhất trong một hội thoại */
    @Column(name = "id_tin_nhan_ngoai")
    private String externalMessageId;

    @Column(name = "noi_dung")
    private String content;

    /** Ảnh, file, quick reply, postback... giữ nguyên cấu trúc của nền tảng */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dinh_kem", nullable = false)
    @Builder.Default
    private JsonNode attachments = JsonNodeFactory.instance.arrayNode();

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private MessageStatus status;

    @Column(name = "luot_goi_ai_id")
    private UUID aiRunId;

    @Column(name = "tao_luc", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "gui_luc")
    private OffsetDateTime sentAt;
}
