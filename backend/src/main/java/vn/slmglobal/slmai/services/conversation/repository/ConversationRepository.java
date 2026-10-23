package vn.slmglobal.slmai.services.conversation.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vn.slmglobal.slmai.services.conversation.entity.Conversation;
import vn.slmglobal.slmai.services.conversation.entity.ConversationStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    /** Hội thoại đang mở gần nhất của một định danh kênh (index idx_hoi_thoai_dinh_danh_kenh_id_mo). */
    Optional<Conversation> findFirstByChannelIdentityIdAndStatusOrderByCreatedAtDesc(UUID channelIdentityId,
                                                                                     ConversationStatus status);

    /** Hội thoại đã tới hạn trả lời (gộp tin xong) mà AI đang phụ trách (index idx_hoi_thoai_han_tra_loi_luc). */
    @Query(nativeQuery = true, value = """
            SELECT id FROM hoi_thoai
            WHERE han_tra_loi_luc <= now() AND trang_thai = 'OPEN' AND nguoi_phu_trach = 'AI_ACTIVE'
            ORDER BY han_tra_loi_luc
            LIMIT :limit""")
    List<UUID> findDueIds(int limit);

    /** Khoá một hội thoại tới hạn; tiến trình khác đang giữ thì bỏ qua (mỗi hội thoại chỉ một lượt AI tại một lúc). */
    @Query(nativeQuery = true, value = """
            SELECT * FROM hoi_thoai
            WHERE id = :id AND han_tra_loi_luc <= now() AND trang_thai = 'OPEN' AND nguoi_phu_trach = 'AI_ACTIVE'
            FOR UPDATE SKIP LOCKED""")
    Optional<Conversation> lockDue(UUID id);

    /** Khoá để chốt lượt: chờ nếu đang có transaction khác ghi tin vào hội thoại này. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Conversation c WHERE c.id = :id")
    Optional<Conversation> findByIdForUpdate(UUID id);

    /**
     * Tự trả hội thoại lại cho AI sau N giờ không có tin của nhân viên (mục 19). Giữ người phụ trách để báo họ
     * khi khách nhắn lại; lượt đầu tiên sau đó đi theo DRAFT nhờ cột tu_tra_lai_luc.
     */
    @Modifying
    @Query(nativeQuery = true, value = """
            UPDATE hoi_thoai
            SET nguoi_phu_trach = 'AI_ACTIVE', tu_tra_lai_luc = now()
            WHERE nguoi_phu_trach = 'HUMAN_OWNED' AND trang_thai = 'OPEN'
              AND COALESCE(nhan_vien_tin_cuoi_luc, tiep_quan_luc, sua_luc) < :cutoff""")
    int handBackIdleSince(OffsetDateTime cutoff);
}
