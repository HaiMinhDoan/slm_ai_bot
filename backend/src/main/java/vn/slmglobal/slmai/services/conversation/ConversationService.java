package vn.slmglobal.slmai.services.conversation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.conversation.entity.Conversation;
import vn.slmglobal.slmai.services.conversation.entity.ConversationStatus;
import vn.slmglobal.slmai.services.conversation.entity.Message;
import vn.slmglobal.slmai.services.conversation.entity.MessageDirection;
import vn.slmglobal.slmai.services.conversation.entity.MessageStatus;
import vn.slmglobal.slmai.services.conversation.entity.Ownership;
import vn.slmglobal.slmai.services.conversation.entity.SenderType;
import vn.slmglobal.slmai.services.conversation.repository.ConversationRepository;
import vn.slmglobal.slmai.services.conversation.repository.MessageRepository;
import vn.slmglobal.slmai.services.customer.entity.ChannelIdentity;
import vn.slmglobal.slmai.services.customer.entity.Customer;
import vn.slmglobal.slmai.services.customer.repository.ChannelIdentityRepository;
import vn.slmglobal.slmai.services.customer.repository.CustomerRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Ghi tin vào hội thoại theo luồng mục 19: nhận diện khách qua dinh_danh_kenh, tìm hoặc mở hội thoại,
 * lưu tin, đặt hạn gộp tin. Không gọi AI ở đây: worker hội thoại xử lý khi tới hạn.
 */
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final CustomerRepository customerRepository;
    private final ChannelIdentityRepository channelIdentityRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ConversationProperties properties;

    /** Tin từ phía khách (tin nhắn, quick reply, postback). */
    public record InboundMessage(ChannelCode channel, String accountId, String externalUserId,
                                 String externalMessageId, String text, JsonNode attachments,
                                 OffsetDateTime occurredAt) {
    }

    /** Tin nhân viên trả lời ngoài hệ thống (ví dụ từ Page Inbox), nhận được qua echo. */
    public record StaffEcho(ChannelCode channel, String accountId, String externalUserId,
                            String externalMessageId, String text, JsonNode attachments,
                            OffsetDateTime occurredAt) {
    }

    public record Recorded(UUID conversationId, UUID messageId, boolean duplicate, boolean aiTurnPending) {
    }

    @Transactional
    public Recorded recordInbound(InboundMessage in) {
        ChannelIdentity identity = findOrCreateIdentity(in.channel(), in.accountId(), in.externalUserId());
        if (identity.getLastInboundAt() == null || in.occurredAt().isAfter(identity.getLastInboundAt())) {
            identity.setLastInboundAt(in.occurredAt());
        }
        Conversation conversation = findOrOpenConversation(identity);

        if (isDuplicate(conversation, in.externalMessageId())) {
            return new Recorded(conversation.getId(), null, true, conversation.getReplyDueAt() != null);
        }
        Message message = messageRepository.save(Message.builder()
                .conversationId(conversation.getId())
                .direction(MessageDirection.IN)
                .senderType(SenderType.CUSTOMER)
                .externalMessageId(in.externalMessageId())
                .content(in.text())
                .attachments(orEmptyArray(in.attachments()))
                .status(MessageStatus.RECEIVED)
                .sentAt(in.occurredAt())
                .build());

        if (conversation.getLastCustomerMessageAt() == null
                || in.occurredAt().isAfter(conversation.getLastCustomerMessageAt())) {
            conversation.setLastCustomerMessageAt(in.occurredAt());
        }
        // Gộp tin: mỗi tin mới lùi hạn; chỉ khi AI đang phụ trách (người đang theo thì AI không chen vào)
        if (conversation.getOwnership() == Ownership.AI_ACTIVE) {
            conversation.setReplyDueAt(OffsetDateTime.now().plus(properties.debounce()));
        }
        return new Recorded(conversation.getId(), message.getId(), false, conversation.getReplyDueAt() != null);
    }

    @Transactional
    public Recorded recordStaffEcho(StaffEcho echo) {
        ChannelIdentity identity = findOrCreateIdentity(echo.channel(), echo.accountId(), echo.externalUserId());
        Conversation conversation = findOrOpenConversation(identity);

        if (isDuplicate(conversation, echo.externalMessageId())) {
            return new Recorded(conversation.getId(), null, true, false);
        }
        Message message = messageRepository.save(Message.builder()
                .conversationId(conversation.getId())
                .direction(MessageDirection.OUT)
                .senderType(SenderType.STAFF)
                .externalMessageId(echo.externalMessageId())
                .content(echo.text())
                .attachments(orEmptyArray(echo.attachments()))
                .status(MessageStatus.SENT)
                .sentAt(echo.occurredAt())
                .build());
        // Nền tảng không cho biết nhân viên nào trả lời trong Page Inbox, nên chưa gán người phụ trách cụ thể
        conversation.handToHuman(null, echo.occurredAt());
        return new Recorded(conversation.getId(), message.getId(), false, false);
    }

    private ChannelIdentity findOrCreateIdentity(ChannelCode channel, String accountId, String externalUserId) {
        return channelIdentityRepository.findByChannelAndAccountIdAndExternalUserId(channel, accountId, externalUserId)
                .orElseGet(() -> {
                    Customer customer = customerRepository.save(Customer.builder().build());
                    return channelIdentityRepository.save(ChannelIdentity.builder()
                            .customerId(customer.getId())
                            .channel(channel)
                            .accountId(accountId)
                            .externalUserId(externalUserId)
                            .build());
                });
    }

    private Conversation findOrOpenConversation(ChannelIdentity identity) {
        return conversationRepository
                .findFirstByChannelIdentityIdAndStatusOrderByCreatedAtDesc(identity.getId(), ConversationStatus.OPEN)
                .orElseGet(() -> conversationRepository.save(Conversation.builder()
                        .customerId(identity.getCustomerId())
                        .channelIdentityId(identity.getId())
                        .channel(identity.getChannel())
                        .build()));
    }

    private boolean isDuplicate(Conversation conversation, String externalMessageId) {
        return externalMessageId != null
                && messageRepository.existsByConversationIdAndExternalMessageId(conversation.getId(), externalMessageId);
    }

    private static JsonNode orEmptyArray(JsonNode node) {
        return node == null || node.isNull() || node.isMissingNode() ? JsonNodeFactory.instance.arrayNode() : node;
    }
}
