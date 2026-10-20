package vn.slmglobal.slmai.services.conversation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.slmglobal.slmai.services.conversation.entity.Conversation;
import vn.slmglobal.slmai.services.conversation.entity.ConversationStatus;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    /** Hội thoại đang mở gần nhất của một định danh kênh (index idx_hoi_thoai_dinh_danh_kenh_id_mo). */
    Optional<Conversation> findFirstByChannelIdentityIdAndStatusOrderByCreatedAtDesc(UUID channelIdentityId,
                                                                                     ConversationStatus status);
}
