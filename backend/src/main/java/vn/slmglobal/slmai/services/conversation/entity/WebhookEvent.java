package vn.slmglobal.slmai.services.conversation.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

/**
 * Một sự kiện webhook thô, ghi ngay khi nhận rồi mới xử lý bất đồng bộ (mục 19).
 * Bảng su_kien_webhook (V3). Ghi bằng {@code WebhookEventRepository.insertIfAbsent} để chống trùng.
 */
@Entity
@Table(name = "su_kien_webhook")
@Getter
@Setter
@NoArgsConstructor
public class WebhookEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã kênh (mục 54.1): MESSENGER, FACEBOOK_COMMENT... */
    @Column(name = "kenh", nullable = false)
    private String channel;

    @Column(name = "tai_khoan_id", nullable = false)
    private String accountId;

    @Column(name = "ma_su_kien", nullable = false)
    private String eventKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "du_lieu_tho", nullable = false)
    private JsonNode payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private WebhookEventStatus status;

    @Column(name = "thong_bao_loi")
    private String errorMessage;

    @Column(name = "nhan_luc", nullable = false, updatable = false)
    private OffsetDateTime receivedAt;

    @Column(name = "xu_ly_luc")
    private OffsetDateTime processedAt;

    public void finish(WebhookEventStatus newStatus, String note) {
        status = newStatus;
        errorMessage = note;
        processedAt = OffsetDateTime.now();
    }
}
