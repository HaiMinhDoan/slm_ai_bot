package vn.slmglobal.slmai.services.conversation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.slmglobal.slmai.services.conversation.entity.WebhookEvent;

import java.util.List;
import java.util.Optional;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {

    /**
     * Ghi sự kiện nếu chưa có. Nền tảng gửi lại cùng một sự kiện khi retry: UNIQUE (kenh, ma_su_kien)
     * làm lần ghi thứ hai không có tác dụng. Trả 1 nếu ghi mới, 0 nếu trùng.
     */
    @Modifying
    @Query(nativeQuery = true, value = """
            INSERT INTO su_kien_webhook (kenh, tai_khoan_id, ma_su_kien, du_lieu_tho)
            VALUES (:channel, :accountId, :eventKey, CAST(:payload AS jsonb))
            ON CONFLICT (kenh, ma_su_kien) DO NOTHING
            """)
    int insertIfAbsent(@Param("channel") String channel,
                       @Param("accountId") String accountId,
                       @Param("eventKey") String eventKey,
                       @Param("payload") String payloadJson);

    @Query(nativeQuery = true, value = """
            SELECT id FROM su_kien_webhook WHERE trang_thai = 'RECEIVED' ORDER BY id LIMIT :limit
            """)
    List<Long> findPendingIds(@Param("limit") int limit);

    /** Khóa một sự kiện chưa xử lý; instance khác đang giữ thì bỏ qua (SKIP LOCKED). */
    @Query(nativeQuery = true, value = """
            SELECT * FROM su_kien_webhook WHERE id = :id AND trang_thai = 'RECEIVED' FOR UPDATE SKIP LOCKED
            """)
    Optional<WebhookEvent> lockPending(@Param("id") Long id);
}
