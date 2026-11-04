package vn.slmglobal.slmai.services.inbox;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.common.security.AuthInfo;
import vn.slmglobal.slmai.common.web.PageResponse;
import vn.slmglobal.slmai.services.ai.TurnNotifier;
import vn.slmglobal.slmai.services.ai.entity.AiRun;
import vn.slmglobal.slmai.services.ai.repository.AiRunRepository;
import vn.slmglobal.slmai.services.auth.AuditLogService;
import vn.slmglobal.slmai.services.conversation.ChannelSender;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.conversation.entity.Conversation;
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
import vn.slmglobal.slmai.services.lead.ConsultSlots;
import vn.slmglobal.slmai.services.lead.entity.ConsultProfile;
import vn.slmglobal.slmai.services.lead.repository.ConsultProfileRepository;
import vn.slmglobal.slmai.services.notification.NotificationDraft;
import vn.slmglobal.slmai.services.notification.NotificationService;
import vn.slmglobal.slmai.services.notification.NotificationSeverity;
import vn.slmglobal.slmai.services.notification.NotificationType;
import vn.slmglobal.slmai.services.settings.AiSettingsService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Trang chat với khách (inbox admin, mục 19, 34): xem hội thoại theo kênh, chuyển cho nhân viên (AI dừng) hoặc trả
 * lại AI, gửi / sửa / bỏ nháp AI. Nhân viên tự soạn tin nhắn khách trên trang để sau (mục 60.2).
 */
@Slf4j
@Service
public class InboxService {

    private static final String TARGET = "conversation";
    private static final int MESSAGE_LIMIT = 500;

    private final InboxRepository inbox;
    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final ChannelIdentityRepository identities;
    private final CustomerRepository customers;
    private final ConsultProfileRepository profiles;
    private final AiRunRepository aiRuns;
    private final DraftEditRepository draftEdits;
    private final NotificationService notifications;
    private final AuditLogService audit;
    private final AiSettingsService settings;
    private final Map<ChannelCode, ChannelSender> senders = new EnumMap<>(ChannelCode.class);

    public InboxService(InboxRepository inbox, ConversationRepository conversations, MessageRepository messages,
                        ChannelIdentityRepository identities, CustomerRepository customers,
                        ConsultProfileRepository profiles, AiRunRepository aiRuns, DraftEditRepository draftEdits,
                        NotificationService notifications, AuditLogService audit, AiSettingsService settings,
                        List<ChannelSender> channelSenders) {
        this.inbox = inbox;
        this.conversations = conversations;
        this.messages = messages;
        this.identities = identities;
        this.customers = customers;
        this.profiles = profiles;
        this.aiRuns = aiRuns;
        this.draftEdits = draftEdits;
        this.notifications = notifications;
        this.audit = audit;
        this.settings = settings;
        channelSenders.forEach(s -> senders.put(s.channel(), s));
    }

    public record ConversationView(UUID id, ChannelCode channel, String ownership, String status, String intent,
                                   String summary, OffsetDateTime takenOverAt, OffsetDateTime autoHandbackAt,
                                   OffsetDateTime createdAt, boolean aiReplying) {
    }

    public record CustomerView(UUID id, String name, String displayName, String phone, String email,
                               String salutation, String pageId) {
    }

    public record PackageView(int id, String name) {
    }

    public record ProfileView(JsonNode slots, List<String> missing, int answeredQuestions, JsonNode qualification,
                              String leadLevel, String leadLevelReason, List<PackageView> packages,
                              Long cheapestQuotedPrice, String lastQuestion) {
    }

    /**
     * @param reasons lý do AI không gửi tự động (nháp) theo nhãn tiếng Việt; rỗng với tin thường
     */
    public record MessageView(UUID id, String direction, String senderType, String staffId, String content,
                              JsonNode attachments, String status, OffsetDateTime createdAt, OffsetDateTime sentAt,
                              List<String> reasons) {
    }

    /**
     * Gửi được tin ra kênh không: kênh có adapter gửi và còn trong cửa sổ nhắn tự do (Messenger: 24 giờ từ tin cuối
     * của khách).
     */
    public record WindowView(boolean channelSupported, boolean open, OffsetDateTime endsAt, int maxLength) {
    }

    public record Detail(ConversationView conversation, CustomerView customer, ProfileView profile,
                         Map<String, Object> lead, WindowView window, List<MessageView> messages) {
    }

    public record ToStaffRequest(@Size(max = 500) String note) {
    }

    /** text rỗng = gửi nguyên nháp; khác nháp thì lưu bản sửa (kt_sua_ban_nhap) */
    public record SendDraftRequest(@Size(max = 5000) String text) {
    }

    // ------------------------------------------------------------------ xem

    @Transactional(readOnly = true)
    public PageResponse<InboxRepository.Row> list(InboxRepository.Filter f, int page, int size) {
        long total = inbox.count(f);
        return new PageResponse<>(inbox.list(f, page, size), total, page, size,
                (int) Math.ceil(total / (double) Math.max(1, size)));
    }

    @Transactional(readOnly = true)
    public List<InboxRepository.ChannelStats> stats() {
        return inbox.stats();
    }

    @Transactional(readOnly = true)
    public Detail detail(UUID id) {
        return detail(conversation(id));
    }

    // ------------------------------------------------------------------ chuyển người phụ trách

    /** "Chuyển cho sale": AI dừng, hội thoại chờ nhân viên; báo mọi nhân viên (không gán cho ai cụ thể). */
    @Transactional
    public Detail toStaff(UUID id, ToStaffRequest r, AuthInfo actor) {
        Conversation c = conversations.findByIdForUpdate(id).orElseThrow(() -> notFound(id));
        if (c.getOwnership() == Ownership.HUMAN_OWNED) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Hội thoại đã do nhân viên phụ trách");
        }
        OffsetDateTime now = now();
        c.requestHuman(now);
        String note = r == null || r.note() == null || r.note().isBlank() ? null : r.note().strip();
        Map<String, Object> after = new LinkedHashMap<>();
        after.put("ownership", Ownership.HUMAN_OWNED.name());
        after.put("note", note);
        audit.record(actor.id(), "conversation.to_staff", TARGET, id, Map.of("ownership", Ownership.AI_ACTIVE.name()),
                after);

        String customer = customerLabel(c);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("customerName", customer);
        data.put("channel", c.getChannel().name());
        data.put("by", actor.fullName() != null ? actor.fullName() : actor.email());
        data.put("note", note);
        notifications.notify(NotificationDraft.builder()
                .type(NotificationType.STAFF_HANDOFF)
                .severity(NotificationSeverity.URGENT)
                .title("Chuyển cho nhân viên: " + customer)
                .body((actor.fullName() != null ? actor.fullName() : "Nhân viên")
                        + " đã chuyển hội thoại cho nhân viên, AI dừng trả lời.")
                .data(data)
                .conversationId(c.getId())
                .customerId(c.getCustomerId())
                .dedupeKey("STAFF_HANDOFF:" + c.getId() + ":" + now.toInstant().toEpochMilli())
                .build());
        return detail(c);
    }

    /**
     * "Chuyển cho AI": AI phụ trách lại ngay, không đi theo nháp như khi tự trả lại. Nháp / tin lỗi chưa gửi bị bỏ
     * (đã cũ). Tin khách nhắn sau tin cuối trang đã gửi là tin còn chờ: AI trả lời ngay; tin cũ hơn coi như đã xử lý.
     */
    @Transactional
    public Detail toAi(UUID id, AuthInfo actor) {
        Conversation c = conversations.findByIdForUpdate(id).orElseThrow(() -> notFound(id));
        if (c.getOwnership() == Ownership.AI_ACTIVE) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "AI đang phụ trách hội thoại này");
        }
        List<Message> all = messages.findByConversationIdOrderByCreatedAtAsc(c.getId());
        int discarded = 0;
        OffsetDateTime lastSent = null;
        for (Message m : all) {
            if (m.getDirection() == MessageDirection.OUT) {
                if (m.getStatus() == MessageStatus.DRAFT || m.getStatus() == MessageStatus.FAILED) {
                    m.setStatus(MessageStatus.DISCARDED);
                    discarded++;
                } else if (m.getStatus() == MessageStatus.SENT) {
                    lastSent = m.getCreatedAt();
                }
            }
        }
        int waiting = 0;
        for (Message m : all) {
            if (m.getDirection() != MessageDirection.IN) {
                continue;
            }
            if (lastSent == null || m.getCreatedAt().isAfter(lastSent)) {
                m.setStatus(MessageStatus.RECEIVED);
                waiting++;
            } else if (m.getStatus() == MessageStatus.RECEIVED) {
                m.setStatus(MessageStatus.PROCESSED);
            }
        }
        c.setOwnership(Ownership.AI_ACTIVE);
        c.setAutoHandbackAt(null);
        c.setReplyDueAt(waiting > 0 ? now() : null);
        audit.record(actor.id(), "conversation.to_ai", TARGET, id, Map.of("ownership", Ownership.HUMAN_OWNED.name()),
                Map.of("ownership", Ownership.AI_ACTIVE.name(), "waitingMessages", waiting,
                        "discardedDrafts", discarded));
        return detail(c);
    }

    // ------------------------------------------------------------------ nháp

    /**
     * Gửi nháp AI (hoặc gửi lại tin lỗi) ra kênh, có thể sửa trước. Giữ khoá hội thoại trong lúc gọi API kênh để hai
     * nhân viên không gửi trùng một nháp; lỗi gửi thì không đổi gì, nháp vẫn còn.
     */
    @Transactional
    public Detail sendDraft(UUID messageId, SendDraftRequest r, AuthInfo actor) {
        UUID conversationId = messages.findConversationIdById(messageId)
                .orElseThrow(() -> CommonException.notFound("Không có tin " + messageId));
        Conversation c = conversations.findByIdForUpdate(conversationId).orElseThrow(() -> notFound(conversationId));
        Message m = pendingDraft(messageId);
        ChannelSender sender = senders.get(c.getChannel());
        if (sender == null) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Kênh này chưa gửi được tin từ trang admin");
        }
        WindowView w = window(c);
        if (!w.open()) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Đã quá " + hours(settings.conversation()
                    .messagingWindow()) + " giờ kể từ tin cuối của khách nên kênh không cho gửi; liên hệ khách qua "
                    + "số điện thoại, hoặc chờ khách nhắn lại");
        }
        String text = r == null || r.text() == null || r.text().isBlank() ? m.getContent() : r.text().strip();
        if (text == null || text.isBlank()) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Tin trống");
        }
        if (text.length() > sender.maxTextLength()) {
            throw new CommonException(HttpStatus.BAD_REQUEST, "Tin dài " + text.length() + " ký tự, kênh chỉ nhận tối "
                    + "đa " + sender.maxTextLength());
        }
        ChannelIdentity identity = identities.findById(c.getChannelIdentityId()).orElseThrow();
        String externalId;
        try {
            externalId = sender.sendText(identity, text);
        } catch (RuntimeException e) {
            log.warn("Gửi nháp {} của hội thoại {} lỗi: {}", messageId, c.getId(), e.getMessage());
            throw new CommonException(HttpStatus.BAD_GATEWAY, "Gửi không thành công: " + e.getMessage());
        }
        boolean edited = !text.equals(m.getContent());
        if (edited && m.getSenderType() == SenderType.AI && m.getContent() != null) {
            draftEdits.save(DraftEdit.builder()
                    .messageId(m.getId())
                    .aiRunId(m.getAiRunId())
                    .aiText(m.getContent())
                    .humanText(text)
                    .editedBy(actor.email())
                    .build());
        }
        OffsetDateTime now = now();
        m.setContent(text);
        m.setStatus(MessageStatus.SENT);
        m.setSentAt(now);
        m.setExternalMessageId(externalId);
        m.setStaffId(actor.email());
        if (c.getOwnership() == Ownership.HUMAN_OWNED) {
            c.setLastStaffMessageAt(now);
        }
        audit.record(actor.id(), "conversation.draft.send", TARGET, c.getId(), null,
                Map.of("messageId", m.getId(), "edited", edited));
        return detail(c);
    }

    @Transactional
    public Detail discardDraft(UUID messageId, AuthInfo actor) {
        UUID conversationId = messages.findConversationIdById(messageId)
                .orElseThrow(() -> CommonException.notFound("Không có tin " + messageId));
        Conversation c = conversations.findByIdForUpdate(conversationId).orElseThrow(() -> notFound(conversationId));
        Message m = pendingDraft(messageId);
        m.setStatus(MessageStatus.DISCARDED);
        audit.record(actor.id(), "conversation.draft.discard", TARGET, c.getId(), null,
                Map.of("messageId", m.getId()));
        return detail(c);
    }

    // ------------------------------------------------------------------ dựng dữ liệu

    private Detail detail(Conversation c) {
        ChannelIdentity identity = identities.findById(c.getChannelIdentityId()).orElseThrow();
        Customer customer = customers.findById(c.getCustomerId()).orElseThrow();
        JsonNode prefs = customer.getPreferences();
        CustomerView cv = new CustomerView(customer.getId(), customer.getName(), identity.getDisplayName(),
                customer.getPhone(), customer.getEmail(),
                prefs != null && prefs.hasNonNull(ConsultSlots.DANH_XUNG) ? prefs.get(ConsultSlots.DANH_XUNG).asText()
                        : null,
                identity.getAccountId());

        ProfileView pv = profiles.findById(c.getId()).map(this::profile).orElse(null);

        // Xếp theo lúc khách nhận (nháp gửi sau thì nằm sau các tin gửi trước nó), nháp chưa gửi theo lúc tạo
        List<Message> all = messages.findByConversationIdOrderByCreatedAtAsc(c.getId()).stream()
                .sorted(Comparator.comparing((Message m) -> m.getSentAt() != null ? m.getSentAt() : m.getCreatedAt()))
                .toList();
        List<Message> shown = all.size() > MESSAGE_LIMIT ? all.subList(all.size() - MESSAGE_LIMIT, all.size()) : all;
        Set<UUID> draftRuns = shown.stream()
                .filter(m -> m.getStatus() == MessageStatus.DRAFT && m.getAiRunId() != null)
                .map(Message::getAiRunId).collect(Collectors.toSet());
        Map<UUID, AiRun> runs = aiRuns.findAllById(draftRuns).stream()
                .collect(Collectors.toMap(AiRun::getId, Function.identity()));
        List<MessageView> mv = shown.stream().map(m -> new MessageView(m.getId(), m.getDirection().name(),
                m.getSenderType().name(), m.getStaffId(), m.getContent(), m.getAttachments(), m.getStatus().name(),
                m.getCreatedAt(), m.getSentAt(), m.getStatus() == MessageStatus.DRAFT
                ? reasons(runs.get(m.getAiRunId())) : List.of())).toList();

        ConversationView conv = new ConversationView(c.getId(), c.getChannel(), c.getOwnership().name(),
                c.getStatus().name(), c.getIntent(), c.getSummary(), c.getTakenOverAt(), c.getAutoHandbackAt(),
                c.getCreatedAt(), c.getOwnership() == Ownership.AI_ACTIVE && c.getReplyDueAt() != null);
        return new Detail(conv, cv, pv, inbox.latestLead(c.getId()), window(c), mv);
    }

    private ProfileView profile(ConsultProfile p) {
        List<Integer> ids = Arrays.stream(p.getRecommendedPackageIds()).filter(Objects::nonNull).toList();
        Map<Integer, String> names = inbox.packageNames(ids);
        return new ProfileView(p.getSlots(), Arrays.asList(p.getMissingSlots()), p.getAnsweredQuestions(),
                p.getQualification(), p.getLeadLevel() == null ? null : p.getLeadLevel().name(),
                p.getLeadLevelReason(),
                ids.stream().map(i -> new PackageView(i, names.getOrDefault(i, "Gói #" + i))).toList(),
                p.getCheapestQuotedPrice(), p.getLastQuestionAsked());
    }

    private WindowView window(Conversation c) {
        ChannelSender sender = senders.get(c.getChannel());
        Duration window = settings.conversation().messagingWindow();
        OffsetDateTime endsAt = c.getLastCustomerMessageAt() == null || window == null ? null
                : c.getLastCustomerMessageAt().plus(window);
        // Trừ hao 1 phút cho thời gian gửi, như lượt AI
        boolean open = endsAt != null && endsAt.minusMinutes(1).isAfter(OffsetDateTime.now());
        return new WindowView(sender != null, open, endsAt, sender == null ? 0 : sender.maxTextLength());
    }

    private static List<String> reasons(AiRun run) {
        if (run == null || run.getPolicyResult() == null) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        run.getPolicyResult().path("reasons")
                .forEach(r -> out.add(TurnNotifier.REASON_LABELS.getOrDefault(r.asText(), r.asText())));
        return out;
    }

    private Message pendingDraft(UUID messageId) {
        Message m = messages.findById(messageId).orElseThrow(() -> CommonException.notFound("Không có tin " + messageId));
        if (m.getDirection() != MessageDirection.OUT
                || (m.getStatus() != MessageStatus.DRAFT && m.getStatus() != MessageStatus.FAILED)) {
            throw new CommonException(HttpStatus.CONFLICT, "Tin này đã được gửi hoặc bỏ, tải lại hội thoại");
        }
        return m;
    }

    private Conversation conversation(UUID id) {
        return conversations.findById(id).orElseThrow(() -> notFound(id));
    }

    private String customerLabel(Conversation c) {
        Customer customer = customers.findById(c.getCustomerId()).orElse(null);
        if (customer != null && customer.getName() != null && !customer.getName().isBlank()) {
            return customer.getName().strip();
        }
        return identities.findById(c.getChannelIdentityId()).map(ChannelIdentity::getDisplayName)
                .filter(n -> n != null && !n.isBlank()).orElse("khách");
    }

    private static CommonException notFound(UUID id) {
        return CommonException.notFound("Không có hội thoại " + id);
    }

    private static long hours(Duration d) {
        return d == null ? 24 : d.toHours();
    }

    private static OffsetDateTime now() {
        return OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS);
    }
}
