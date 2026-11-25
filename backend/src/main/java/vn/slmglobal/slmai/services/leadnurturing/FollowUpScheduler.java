package vn.slmglobal.slmai.services.leadnurturing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import vn.slmglobal.slmai.services.ai.AiProperties;
import vn.slmglobal.slmai.services.catalog.CatalogRepository;
import vn.slmglobal.slmai.services.catalog.CatalogRepository.CatalogPackage;
import vn.slmglobal.slmai.services.conversation.ChannelSender;
import vn.slmglobal.slmai.services.conversation.entity.Conversation;
import vn.slmglobal.slmai.services.conversation.repository.ConversationRepository;
import vn.slmglobal.slmai.services.customer.entity.Customer;
import vn.slmglobal.slmai.services.customer.repository.ChannelIdentityRepository;
import vn.slmglobal.slmai.services.customer.repository.CustomerRepository;
import vn.slmglobal.slmai.services.lead.ConsultProfileService;
import vn.slmglobal.slmai.services.lead.entity.ConsultProfile;
import vn.slmglobal.slmai.services.leadnurturing.entity.FollowUp;
import vn.slmglobal.slmai.services.leadnurturing.repository.FollowUpRepository;
import vn.slmglobal.slmai.services.notification.NotificationDraft;
import vn.slmglobal.slmai.services.notification.NotificationService;
import vn.slmglobal.slmai.services.notification.NotificationSeverity;
import vn.slmglobal.slmai.services.notification.NotificationType;
import vn.slmglobal.slmai.services.settings.AiSettingsService;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Nhắc khách khi khách im lặng (mục 56.9, luật cố định). Mỗi phút:
 * <ol>
 *   <li>Trong khung giờ nhắc: tìm hội thoại mà tin cuối là tin AI đã gửi, khách im lặng quá khoảng chờ, cửa sổ 24 giờ
 *   còn đủ lâu; lập lịch nhắc (lich_nhac_khach PENDING) và đặt han_tra_loi_luc để AiTurnWorker chạy lượt nhắc như
 *   một lượt AI thường (khoá lượt, Policy, gửi tin, media).</li>
 *   <li>Cửa sổ 24 giờ vừa đóng mà khách vẫn im lặng: báo nhân viên một lần (CUSTOMER_SILENT) nếu hội thoại đáng
 *   theo; nhân viên nhắn trong Page Inbox (trong 7 ngày) hoặc gọi điện.</li>
 * </ol>
 * Bot không nhắn ngoài cửa sổ 24 giờ và không dùng tag để lách (mục 56.7).
 */
@Slf4j
@Component
public class FollowUpScheduler {

    static final int BATCH = 50;
    /** Chỉ báo nhân viên cho cửa sổ vừa đóng trong khoảng này (máy dừng lâu hơn thì bỏ, tránh báo dồn) */
    static final Duration SILENT_LOOKBACK = Duration.ofHours(2);

    private final FollowUpRepository followUps;
    private final ConversationRepository conversations;
    private final CustomerRepository customers;
    private final ChannelIdentityRepository identities;
    private final ConsultProfileService profiles;
    private final CatalogRepository catalog;
    private final NotificationService notifications;
    private final AiSettingsService settings;
    private final TransactionTemplate tx;
    private final List<String> channels;

    public FollowUpScheduler(FollowUpRepository followUps, ConversationRepository conversations,
                             CustomerRepository customers, ChannelIdentityRepository identities,
                             ConsultProfileService profiles, CatalogRepository catalog,
                             NotificationService notifications, AiSettingsService settings, TransactionTemplate tx,
                             List<ChannelSender> senders) {
        this.followUps = followUps;
        this.conversations = conversations;
        this.customers = customers;
        this.identities = identities;
        this.profiles = profiles;
        this.catalog = catalog;
        this.notifications = notifications;
        this.settings = settings;
        this.tx = tx;
        this.channels = senders.stream().map(s -> s.channel().name()).distinct().toList();
    }

    @Scheduled(fixedDelayString = "${app.follow-up.scan-interval-ms:60000}",
            initialDelayString = "${app.follow-up.scan-interval-ms:60000}")
    public void poll() {
        try {
            scan(OffsetDateTime.now());
        } catch (RuntimeException e) {
            log.error("Quét nhắc khách lỗi", e);
        }
    }

    /** @return số lịch nhắc vừa lập */
    public int scan(OffsetDateTime now) {
        FollowUpProperties rules = settings.followUp();
        AiProperties ai = settings.ai();
        Duration window = settings.conversation().messagingWindow();
        if (window == null) {
            return 0;
        }
        if (rules.notifyStaff()) {
            notifySilent(now, window);
        }
        ZoneId zone = ai.businessHours().zoneId();
        if (!rules.enabled() || !ai.workerEnabled() || !ai.hasApiKey() || ai.forceDraft() || channels.isEmpty()
                || !rules.isInHours(now.atZoneSameInstant(zone), zone)) {
            return 0;
        }
        List<UUID> due = followUps.findDueForNudge(channels, now.minus(window).plus(rules.windowMargin()),
                now.minus(rules.interval()), rules.maxCount(), BATCH);
        int created = 0;
        for (UUID id : due) {
            try {
                if (Boolean.TRUE.equals(tx.execute(st -> schedule(id, now)))) {
                    created++;
                }
            } catch (DataIntegrityViolationException e) {
                log.warn("Hội thoại {}: lịch nhắc đã có (chạy song song?): {}", id, e.getMessage());
            }
        }
        if (created > 0) {
            log.info("Lập {} lịch nhắc khách", created);
        }
        return created;
    }

    /** Lập một lịch nhắc và đặt hạn để worker AI chạy ngay; false khi hội thoại vừa có lượt khác giữ */
    private boolean schedule(UUID conversationId, OffsetDateTime now) {
        Conversation c = conversations.lockForFollowUp(conversationId).orElse(null);
        if (c == null || c.getLastCustomerMessageAt() == null) {
            return false;
        }
        OffsetDateTime anchor = c.getLastCustomerMessageAt();
        long done = followUps.countByConversationIdAndKindAndWindowAnchorAndStatusIn(c.getId(),
                FollowUp.Kind.NHAC_KHACH, anchor, List.of(FollowUp.Status.SENT, FollowUp.Status.DRAFT));
        long all = followUps.countByConversationIdAndKindAndWindowAnchor(c.getId(), FollowUp.Kind.NHAC_KHACH, anchor);
        followUps.save(FollowUp.builder()
                .conversationId(c.getId())
                .customerId(c.getCustomerId())
                .kind(FollowUp.Kind.NHAC_KHACH)
                .attemptNo((int) done + 1)
                .windowAnchor(anchor)
                .idempotencyKey(key(c.getId(), anchor, FollowUp.Kind.NHAC_KHACH, all + 1))
                .build());
        c.setReplyDueAt(now);
        return true;
    }

    /** Cửa sổ vừa đóng mà khách vẫn im lặng: báo nhân viên một lần mỗi cửa sổ */
    private void notifySilent(OffsetDateTime now, Duration window) {
        OffsetDateTime closedBefore = now.minus(window);
        for (UUID id : followUps.findSilentAfterWindow(closedBefore, closedBefore.minus(SILENT_LOOKBACK), BATCH)) {
            try {
                tx.executeWithoutResult(st -> notifyOne(id));
            } catch (DataIntegrityViolationException e) {
                log.warn("Hội thoại {}: đã báo nhân viên khách im lặng (chạy song song?)", id);
            }
        }
    }

    private void notifyOne(UUID conversationId) {
        Conversation c = conversations.findById(conversationId).orElse(null);
        if (c == null || c.getLastCustomerMessageAt() == null) {
            return;
        }
        OffsetDateTime anchor = c.getLastCustomerMessageAt();
        long nudges = followUps.countByConversationIdAndKindAndWindowAnchorAndStatusIn(c.getId(),
                FollowUp.Kind.NHAC_KHACH, anchor, List.of(FollowUp.Status.SENT));
        followUps.save(FollowUp.builder()
                .conversationId(c.getId())
                .customerId(c.getCustomerId())
                .kind(FollowUp.Kind.BAO_NHAN_VIEN)
                .status(FollowUp.Status.SENT)
                .attemptNo((int) nudges)
                .windowAnchor(anchor)
                .idempotencyKey(key(c.getId(), anchor, FollowUp.Kind.BAO_NHAN_VIEN, 1))
                .processedAt(OffsetDateTime.now())
                .build());

        Customer customer = customers.findById(c.getCustomerId()).orElse(null);
        String name = customer != null && customer.getName() != null && !customer.getName().isBlank()
                ? customer.getName().strip()
                : identities.findById(c.getChannelIdentityId()).map(i -> i.getDisplayName()).filter(Objects::nonNull)
                .orElse("khách Messenger");
        ConsultProfile p = profiles.get(c.getId());
        List<String> packages = p == null || p.getRecommendedPackageIds() == null ? List.of()
                : Arrays.stream(p.getRecommendedPackageIds()).filter(Objects::nonNull)
                .map(i -> catalog.findPackage(i).map(CatalogPackage::name).orElse("Gói #" + i)).toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("customerName", name);
        data.put("channel", c.getChannel().name());
        identities.findById(c.getChannelIdentityId()).ifPresent(i -> data.put("pageId", i.getAccountId()));
        data.put("nudges", nudges);
        data.put("packages", packages);
        data.put("leadLevel", p == null || p.getLeadLevel() == null ? null : p.getLeadLevel().name());
        data.put("phone", customer == null ? null : customer.getPhone());
        data.put("lastCustomerMessageAt", anchor.toString());
        notifications.notify(NotificationDraft.builder()
                .type(NotificationType.CUSTOMER_SILENT)
                .severity(NotificationSeverity.WARNING)
                .title("Khách im lặng quá 24 giờ: " + name)
                .body("Khách chưa trả lời sau tin của bot" + (nudges > 0 ? " (đã nhắc " + nudges + " lần)" : "")
                        + ", bot không nhắn được nữa. Nhân viên nhắn trong hộp thư Meta (trong 7 ngày)"
                        + (customer != null && customer.getPhone() != null ? " hoặc gọi điện." : "."))
                .data(data)
                .conversationId(c.getId())
                .customerId(c.getCustomerId())
                .dedupeKey("CUSTOMER_SILENT:" + c.getId() + ":" + anchor.toInstant().toEpochMilli())
                .build());
    }

    static String key(UUID conversationId, OffsetDateTime anchor, FollowUp.Kind kind, long seq) {
        return conversationId + ":" + anchor.toInstant().toEpochMilli() + ":" + kind.name() + ":" + seq;
    }
}
