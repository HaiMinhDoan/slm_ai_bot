package vn.slmglobal.slmai.services.conversation.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.slmglobal.slmai.services.conversation.entity.Message;
import vn.slmglobal.slmai.services.conversation.entity.MessageDirection;
import vn.slmglobal.slmai.services.conversation.entity.MessageStatus;
import vn.slmglobal.slmai.services.conversation.entity.SenderType;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    boolean existsByConversationIdAndExternalMessageId(UUID conversationId, String externalMessageId);

    List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);

    /** Các tin gần nhất (mới trước) để ghép ngữ cảnh cho AI */
    List<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

    List<Message> findByConversationIdAndDirectionAndStatusOrderByCreatedAtAsc(UUID conversationId,
                                                                               MessageDirection direction,
                                                                               MessageStatus status);

    /** Đếm tin của khách: lúc chốt lượt mà số này tăng nghĩa là khách vừa nhắn thêm, bỏ kết quả cũ (mục 19). */
    long countByConversationIdAndDirection(UUID conversationId, MessageDirection direction);

    List<Message> findByIdIn(Collection<UUID> ids);

    /** Hội thoại của một tin: đọc trước khi khoá hội thoại rồi mới nạp tin (tránh tin cũ trong bộ nhớ đệm JPA) */
    @Query("SELECT m.conversationId FROM Message m WHERE m.id = :id")
    Optional<UUID> findConversationIdById(UUID id);

    /**
     * Số tin của khách (trừ {@code exclude}) nhắn sau tin cuối trang đã gửi: bằng 0 nghĩa là tin vừa nhận mở đầu một
     * đợt khách chờ trả lời, để chỉ báo nhân viên một lần cho mỗi đợt.
     */
    @Query(nativeQuery = true, value = """
            SELECT count(*) FROM tin_nhan t
            WHERE t.hoi_thoai_id = :conversationId AND t.chieu = 'IN' AND t.id <> :exclude
              AND t.tao_luc > COALESCE((SELECT max(o.tao_luc) FROM tin_nhan o
                                        WHERE o.hoi_thoai_id = :conversationId AND o.chieu = 'OUT'
                                          AND o.trang_thai = 'SENT'), '-infinity')""")
    long countWaitingInbound(UUID conversationId, UUID exclude);

    /** Chống gửi câu báo nhận liên tục khi khách nhắn nhiều tin ngoài giờ. */
    boolean existsByConversationIdAndSenderTypeAndStatusAndCreatedAtAfter(UUID conversationId, SenderType senderType,
                                                                          MessageStatus status, OffsetDateTime after);
}
