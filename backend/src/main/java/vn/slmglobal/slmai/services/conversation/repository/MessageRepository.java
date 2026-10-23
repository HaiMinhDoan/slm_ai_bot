package vn.slmglobal.slmai.services.conversation.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.slmglobal.slmai.services.conversation.entity.Message;
import vn.slmglobal.slmai.services.conversation.entity.MessageDirection;
import vn.slmglobal.slmai.services.conversation.entity.MessageStatus;
import vn.slmglobal.slmai.services.conversation.entity.SenderType;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
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

    /** Chống gửi câu báo nhận liên tục khi khách nhắn nhiều tin ngoài giờ. */
    boolean existsByConversationIdAndSenderTypeAndStatusAndCreatedAtAfter(UUID conversationId, SenderType senderType,
                                                                          MessageStatus status, OffsetDateTime after);
}
