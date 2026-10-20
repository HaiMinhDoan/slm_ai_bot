package vn.slmglobal.slmai.services.conversation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.slmglobal.slmai.services.conversation.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    boolean existsByConversationIdAndExternalMessageId(UUID conversationId, String externalMessageId);

    List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);
}
