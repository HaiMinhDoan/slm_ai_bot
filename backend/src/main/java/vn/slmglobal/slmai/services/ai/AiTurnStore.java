package vn.slmglobal.slmai.services.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.slmglobal.slmai.services.ai.entity.AiRun;
import vn.slmglobal.slmai.services.ai.repository.AiRunRepository;
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
import vn.slmglobal.slmai.services.lead.ConsultProfileService;
import vn.slmglobal.slmai.services.lead.ConsultSlots;
import vn.slmglobal.slmai.services.lead.LeadService;
import vn.slmglobal.slmai.services.lead.entity.ConsultProfile;
import vn.slmglobal.slmai.services.policy.ReplyPolicy;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Các bước có transaction của một lượt AI (mục 19). Gọi Claude nằm ngoài transaction (AiTurnService), nên một lượt
 * gồm: {@link #claim} → gọi Claude → {@link #commit} → gửi tin → {@link #markSent} / {@link #markFailed}.
 *
 * <p>Giữ lượt bằng hạn thuê: lúc nhận lượt, han_tra_loi_luc được đặt thành {@code leaseUntil}; tiến trình chết giữa
 * chừng thì hết hạn này lượt tự chạy lại. Khách nhắn thêm thì ConversationService đặt lại han_tra_loi_luc và
 * lượt đang chạy bị bỏ khi chốt.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiTurnStore {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ChannelIdentityRepository channelIdentityRepository;
    private final CustomerRepository customerRepository;
    private final AiRunRepository aiRunRepository;
    private final ConsultProfileService consultProfileService;
    private final LeadService leadService;
    private final TurnNotifier notifier;
    private final AiProperties properties;

    /** Tin cần gửi ra kênh sau khi chốt */
    public record Outgoing(UUID messageId, String text) {
    }

    /** Kết quả báo giá của lượt (tool tim_goi_phu_hop): gói đề xuất và giá thấp nhất */
    public record Quote(List<Integer> packageIds, Long cheapestVnd) {
    }

    public record Commit(boolean applied, String discardReason, List<Outgoing> outgoing) {

        static Commit discarded(String reason) {
            return new Commit(false, reason, List.of());
        }
    }

    public static final String DISCARD_HUMAN_OWNED = "HUMAN_OWNED";
    public static final String DISCARD_NEWER_MESSAGE = "NEWER_CUSTOMER_MESSAGE";

    @Transactional
    public Optional<TurnSnapshot> claim(UUID conversationId) {
        Conversation c = conversationRepository.lockDue(conversationId).orElse(null);
        if (c == null) {
            return Optional.empty();
        }
        // Cắt về micro giây: PostgreSQL lưu tới micro giây, lúc so lại phải khớp tuyệt đối
        OffsetDateTime lease = OffsetDateTime.now().plus(properties.turnLease()).truncatedTo(ChronoUnit.MICROS);
        c.setReplyDueAt(lease);

        ChannelIdentity identity = channelIdentityRepository.findById(c.getChannelIdentityId()).orElseThrow();
        Customer customer = customerRepository.findById(c.getCustomerId()).orElseThrow();
        List<Message> history = new ArrayList<>(messageRepository.findByConversationIdOrderByCreatedAtDesc(
                c.getId(), PageRequest.of(0, Math.max(1, properties.historyLimit()))));
        Collections.reverse(history);
        List<Message> pending = messageRepository.findByConversationIdAndDirectionAndStatusOrderByCreatedAtAsc(
                c.getId(), MessageDirection.IN, MessageStatus.RECEIVED);
        long inboundCount = messageRepository.countByConversationIdAndDirection(c.getId(), MessageDirection.IN);
        ConsultProfile profile = consultProfileService.get(c.getId());
        JsonNode prefs = customer.getPreferences();

        return Optional.of(new TurnSnapshot(
                c.getId(), c.getCustomerId(), c.getChannel(), identity, lease, inboundCount,
                List.copyOf(history), List.copyOf(pending), c.getReplyMode(), c.getAutoHandbackAt() != null,
                c.getLastCustomerMessageAt(), profile, customer.getPhone(),
                customer.getName() != null ? customer.getName() : identity.getDisplayName(),
                prefs != null && prefs.hasNonNull(ConsultSlots.DANH_XUNG) ? prefs.get(ConsultSlots.DANH_XUNG).asText()
                        : null));
    }

    /** Không có tin nào cần trả lời: bỏ hạn thuê để hội thoại không bị quét lại. */
    @Transactional
    public void release(TurnSnapshot s) {
        conversationRepository.findByIdForUpdate(s.conversationId()).ifPresent(c -> {
            if (holdsLease(c, s)) {
                c.setReplyDueAt(null);
            }
        });
    }

    /** Gọi Claude lỗi, còn lượt thử: ghi luot_goi_ai và hẹn chạy lại (trừ khi khách đã nhắn thêm, đã có hạn mới). */
    @Transactional
    public void recordFailure(TurnSnapshot s, AiRun run, OffsetDateTime retryAt) {
        aiRunRepository.save(run);
        conversationRepository.findByIdForUpdate(s.conversationId()).ifPresent(c -> {
            if (holdsLease(c, s)) {
                c.setReplyDueAt(retryAt);
            }
        });
    }

    /**
     * Chốt lượt (mục 19): bỏ nếu nhân viên đã nhận hoặc khách vừa nhắn thêm; nếu không thì trong cùng transaction ghi
     * luot_goi_ai, tin_nhan (OUT), ho_so_tu_van, khach_tiem_nang và trạng thái hội thoại.
     *
     * @param reply null khi lượt không dùng được kết quả AI (lỗi, từ chối); khi đó decision là HANDOFF
     * @param quote kết quả báo giá của lượt; null khi không gọi tool
     */
    @Transactional
    public Commit commit(TurnSnapshot s, AiRun run, AiReply reply, ReplyPolicy.Decision decision, Quote quote,
                         boolean businessOpen, boolean windowOpen, boolean channelSupported) {
        Conversation c = conversationRepository.findByIdForUpdate(s.conversationId()).orElseThrow();
        String discard = null;
        if (c.getStatus() != ConversationStatus.OPEN || c.getOwnership() != Ownership.AI_ACTIVE) {
            discard = DISCARD_HUMAN_OWNED;
        } else if (messageRepository.countByConversationIdAndDirection(c.getId(), MessageDirection.IN)
                != s.inboundCount()) {
            discard = DISCARD_NEWER_MESSAGE;
        }
        run.setPolicyResult(policyJson(decision, discard, businessOpen, windowOpen));
        aiRunRepository.save(run);
        if (discard != null) {
            log.debug("Bỏ lượt AI của hội thoại {}: {}", c.getId(), discard);
            return Commit.discarded(discard);
        }

        OffsetDateTime now = OffsetDateTime.now();
        c.setReplyDueAt(null);
        c.setAutoHandbackAt(null);
        if (reply != null && reply.intent() != null) {
            c.setIntent(reply.intent());
        }
        for (Message m : messageRepository.findByIdIn(s.pendingInboundIds())) {
            if (m.getStatus() == MessageStatus.RECEIVED) {
                m.setStatus(MessageStatus.PROCESSED);
            }
        }

        // Số điện thoại do backend tự tách từ tin của khách, không dựa vào AI
        Customer customer = customerRepository.findById(c.getCustomerId()).orElseThrow();
        String phoneThisTurn = s.phoneInPendingMessages().orElse(null);
        if (phoneThisTurn != null && customer.getPhone() == null) {
            if (customerRepository.existsByPhoneAndIdNot(phoneThisTurn, customer.getId())) {
                // sdt là UNIQUE: số đã thuộc khách khác thì không gán, chờ nhân viên gộp khách
                // (khach_hang_lich_su_gop, mục 29); lead vẫn được tạo với số này
                log.info("Hội thoại {}: số điện thoại trùng với khách khác, cần gộp khách", c.getId());
                notifier.duplicatePhone(s, c, phoneThisTurn);
            } else {
                customer.setPhone(phoneThisTurn);
            }
        }
        applyCustomerDetails(customer, reply);
        String phone = customer.getPhone() != null ? customer.getPhone() : phoneThisTurn;

        ConsultProfile profile = consultProfileService.apply(c.getId(), c.getCustomerId(),
                new ConsultProfileService.Update(reply, phoneThisTurn != null, phone != null,
                        decision.action() == ReplyPolicy.Action.AUTO,
                        quote == null ? List.of() : quote.packageIds(), quote == null ? null : quote.cheapestVnd()));
        if (phone != null) {
            leadService.upsert(c, phone, profile);
        }

        List<Outgoing> outgoing = new ArrayList<>();
        Message aiMessage = null;
        if (reply != null && reply.hasReply()) {
            // Lượt AUTO cũng ghi DRAFT trước, gửi xong mới đổi SENT: tiến trình chết giữa chừng thì nhân viên vẫn thấy nháp
            aiMessage = messageRepository.save(Message.builder()
                    .conversationId(c.getId())
                    .direction(MessageDirection.OUT)
                    .senderType(SenderType.AI)
                    .content(reply.reply().strip())
                    .status(MessageStatus.DRAFT)
                    .aiRunId(run.getId())
                    .build());
            if (decision.action() == ReplyPolicy.Action.AUTO) {
                outgoing.add(new Outgoing(aiMessage.getId(), aiMessage.getContent()));
            }
        }
        if (decision.action() == ReplyPolicy.Action.HANDOFF) {
            c.requestHuman(now);
            log.info("Hội thoại {} chuyển nhân viên: {}", c.getId(), decision.reasons());
        } else if (decision.action() == ReplyPolicy.Action.DRAFT) {
            log.info("Hội thoại {} có nháp AI chờ duyệt: {}", c.getId(), decision.reasons());
        }
        // Báo nhân viên trên trang admin (chuông, âm thanh, thông báo trình duyệt)
        notifier.turnOutcome(s, c, run, reply, decision, aiMessage);

        String ack = ackText(decision.action(), businessOpen);
        if (ack != null && windowOpen && channelSupported && !recentlyAcked(c.getId(), now)) {
            Message ackMessage = messageRepository.save(Message.builder()
                    .conversationId(c.getId())
                    .direction(MessageDirection.OUT)
                    .senderType(SenderType.SYSTEM)
                    .content(ack)
                    .status(MessageStatus.DRAFT)
                    .build());
            outgoing.add(new Outgoing(ackMessage.getId(), ack));
        }
        return new Commit(true, null, List.copyOf(outgoing));
    }

    /** Tên hiển thị của khách lấy từ kênh (Messenger User Profile API) */
    @Transactional
    public void saveDisplayName(UUID channelIdentityId, String displayName) {
        channelIdentityRepository.findById(channelIdentityId).ifPresent(i -> {
            if (i.getDisplayName() == null) {
                i.setDisplayName(displayName);
            }
        });
    }

    @Transactional
    public void markSent(UUID messageId, String externalMessageId) {
        messageRepository.findById(messageId).ifPresent(m -> {
            m.setStatus(MessageStatus.SENT);
            m.setSentAt(OffsetDateTime.now());
            m.setExternalMessageId(externalMessageId);
        });
    }

    @Transactional
    public void markFailed(UUID messageId, String error) {
        messageRepository.findById(messageId).ifPresent(m -> {
            m.setStatus(MessageStatus.FAILED);
            notifier.sendFailed(m, error);
        });
    }

    private boolean holdsLease(Conversation c, TurnSnapshot s) {
        return c.getReplyDueAt() != null && c.getReplyDueAt().isEqual(s.leaseUntil());
    }

    /**
     * Câu báo nhận cố định (mục 35): lượt DRAFT ngoài giờ, hoặc chuyển nhân viên. Không qua Claude nên vẫn gửi khi
     * công tắc dừng toàn cục đang bật.
     */
    private String ackText(ReplyPolicy.Action action, boolean businessOpen) {
        AiProperties.Templates t = properties.templates();
        if (t == null || action == ReplyPolicy.Action.AUTO) {
            return null;
        }
        String text = !businessOpen ? t.outOfHoursAck() : action == ReplyPolicy.Action.HANDOFF ? t.handoffAck() : null;
        return text == null || text.isBlank() ? null : text.strip();
    }

    private boolean recentlyAcked(UUID conversationId, OffsetDateTime now) {
        AiProperties.Templates t = properties.templates();
        if (t == null || t.ackCooldown() == null || t.ackCooldown().isZero()) {
            return false;
        }
        return messageRepository.existsByConversationIdAndSenderTypeAndStatusAndCreatedAtAfter(
                conversationId, SenderType.SYSTEM, MessageStatus.SENT, now.minus(t.ackCooldown()));
    }

    private static void applyCustomerDetails(Customer customer, AiReply reply) {
        if (reply == null || reply.collected() == null) {
            return;
        }
        AiReply.Collected c = reply.collected();
        if (customer.getName() == null && c.customerName() != null && !c.customerName().isBlank()) {
            customer.setName(c.customerName().strip());
        }
        if (c.salutation() != null) {
            ObjectNode prefs = customer.getPreferences() != null && customer.getPreferences().isObject()
                    ? ((ObjectNode) customer.getPreferences()).deepCopy()
                    : JsonNodeFactory.instance.objectNode();
            prefs.put(ConsultSlots.DANH_XUNG, c.salutation());
            customer.setPreferences(prefs);
        }
    }

    private static ObjectNode policyJson(ReplyPolicy.Decision d, String discard, boolean businessOpen,
                                         boolean windowOpen) {
        ObjectNode o = JsonNodeFactory.instance.objectNode();
        o.put("action", d.action().name());
        d.reasons().forEach(o.putArray("reasons")::add);
        if (!d.unverifiedNumbers().isEmpty()) {
            d.unverifiedNumbers().forEach(o.putArray("unverified_numbers")::add);
        }
        o.put("business_hours", businessOpen);
        o.put("window_open", windowOpen);
        if (discard != null) {
            o.put("discarded", discard);
        }
        return o;
    }
}
