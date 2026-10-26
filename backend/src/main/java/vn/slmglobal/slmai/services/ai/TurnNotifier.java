package vn.slmglobal.slmai.services.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.slmglobal.slmai.common.util.HashUtil;
import vn.slmglobal.slmai.services.ai.entity.AiRun;
import vn.slmglobal.slmai.services.conversation.entity.Conversation;
import vn.slmglobal.slmai.services.conversation.entity.Message;
import vn.slmglobal.slmai.services.notification.NotificationDraft;
import vn.slmglobal.slmai.services.notification.NotificationService;
import vn.slmglobal.slmai.services.notification.NotificationSeverity;
import vn.slmglobal.slmai.services.notification.NotificationType;
import vn.slmglobal.slmai.services.policy.ReplyPolicy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thông báo cho nhân viên từ lượt AI (chuyển nhân viên, nháp chờ duyệt, khách nhắn lại sau khi trả AI, trùng số,
 * gửi tin lỗi). Gọi trong transaction chốt lượt của {@link AiTurnStore}.
 *
 * <p>Nội dung ngắn ({@code body}) hiện cả ở thông báo trình duyệt nên không chứa số điện thoại hay lời khách;
 * chi tiết (nháp AI, tin cuối của khách, số điện thoại) để trong {@code data}, chỉ trả qua REST.
 */
@Component
@RequiredArgsConstructor
public class TurnNotifier {

    /** Nhãn tiếng Việt của mã lý do Policy / lỗi AI */
    static final Map<String, String> REASON_LABELS = Map.ofEntries(
            Map.entry(ReplyPolicy.HANDOFF_INTENT, "khách báo sự cố hoặc khiếu nại"),
            Map.entry(ReplyPolicy.HANDOFF_REQUESTED, "AI đề nghị chuyển nhân viên"),
            Map.entry(ReplyPolicy.EMPTY_REPLY, "AI không soạn được câu trả lời"),
            Map.entry(ReplyPolicy.FORCE_DRAFT, "công tắc dừng AI đang bật"),
            Map.entry(ReplyPolicy.CONVERSATION_DRAFT_MODE, "hội thoại đang ở chế độ nháp"),
            Map.entry(ReplyPolicy.FIRST_TURN_AFTER_HANDBACK, "lượt đầu sau khi trả lại AI"),
            Map.entry(ReplyPolicy.INTENT_NOT_AUTO, "loại câu hỏi không được gửi tự động"),
            Map.entry(ReplyPolicy.NUMBER_CHECK_FAILED, "có con số chưa có nguồn"),
            Map.entry(ReplyPolicy.LINK_NOT_ALLOWED, "có đường link"),
            Map.entry(ReplyPolicy.TOO_LONG, "tin quá dài"),
            Map.entry(ReplyPolicy.MULTIPLE_QUESTIONS, "hỏi hơn một câu"),
            Map.entry(ReplyPolicy.CHANNEL_NOT_SUPPORTED, "kênh chưa hỗ trợ gửi tự động"),
            Map.entry(ReplyPolicy.OUTSIDE_WINDOW, "đã quá 24 giờ kể từ tin cuối của khách"),
            Map.entry("AI_ERROR", "AI gặp lỗi khi gọi Claude"),
            Map.entry("AI_OUTPUT_UNUSABLE", "AI trả kết quả không dùng được"));

    private final NotificationService notifications;

    /** Kết quả lượt AI cần nhân viên: chuyển nhân viên, lượt đầu sau khi trả AI, hoặc nháp chờ duyệt. */
    public void turnOutcome(TurnSnapshot s, Conversation c, AiRun run, AiReply reply,
                            ReplyPolicy.Decision decision, Message aiMessage) {
        String customer = customerLabel(s);
        Map<String, Object> data = turnData(s, run, reply, decision, aiMessage);
        String reasons = labels(decision.reasons());

        if (decision.action() == ReplyPolicy.Action.HANDOFF) {
            notifications.notify(NotificationDraft.builder()
                    .type(NotificationType.AI_HANDOFF)
                    .severity(NotificationSeverity.URGENT)
                    .title("Cần nhân viên: " + customer)
                    .body("AI đã chuyển hội thoại cho nhân viên: " + reasons + ".")
                    .data(data)
                    .conversationId(c.getId())
                    .customerId(c.getCustomerId())
                    .dedupeKey("AI_HANDOFF:" + run.getId())
                    .build());
        } else if (s.firstTurnAfterHandback()) {
            // Lượt này cũng là nháp: một thông báo kèm nháp, không báo thêm AI_DRAFT. Gửi mọi nhân viên (người phụ
            // trách ghi trong data): nhan_vien_phu_trach_id không chắc là id trong nguoi_dung, gán sai sẽ lỗi khoá ngoại
            data.put("ownerStaffId", c.getOwnerStaffId());
            notifications.notify(NotificationDraft.builder()
                    .type(NotificationType.HANDBACK_REPLY)
                    .severity(NotificationSeverity.WARNING)
                    .title("Khách nhắn lại sau khi trả AI: " + customer)
                    .body("Hội thoại vừa tự trả lại AI, nháp trả lời đang chờ nhân viên xem.")
                    .data(data)
                    .conversationId(c.getId())
                    .customerId(c.getCustomerId())
                    .dedupeKey("HANDBACK_REPLY:" + run.getId())
                    .build());
        } else if (decision.action() == ReplyPolicy.Action.DRAFT && aiMessage != null) {
            notifications.notify(NotificationDraft.builder()
                    .type(NotificationType.AI_DRAFT)
                    .severity(NotificationSeverity.WARNING)
                    .title("Nháp AI chờ duyệt: " + customer)
                    .body("Chưa gửi khách vì " + reasons + ".")
                    .data(data)
                    .conversationId(c.getId())
                    .customerId(c.getCustomerId())
                    .dedupeKey("AI_DRAFT:" + aiMessage.getId())
                    .build());
        }
    }

    /** Số khách vừa gửi đã thuộc khách khác (khach_hang.sdt UNIQUE): nhân viên cần gộp khách. */
    public void duplicatePhone(TurnSnapshot s, Conversation c, String phone) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("phone", phone);
        data.put("customerName", s.customerName());
        data.put("pageId", s.identity().getAccountId());
        notifications.notify(NotificationDraft.builder()
                .type(NotificationType.DUPLICATE_PHONE)
                .severity(NotificationSeverity.INFO)
                .title("Số điện thoại trùng với khách khác: " + customerLabel(s))
                .body("Khách vừa gửi một số đã có trong hệ thống, cần kiểm tra và gộp khách.")
                .data(data)
                .conversationId(c.getId())
                .customerId(c.getCustomerId())
                .dedupeKey("DUPLICATE_PHONE:" + c.getCustomerId() + ":" + HashUtil.sha256Hex(phone))
                .build());
    }

    /** Gửi tin ra kênh thất bại (tin đã ghi FAILED). */
    public void sendFailed(Message m, String error) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("messageId", m.getId());
        data.put("senderType", m.getSenderType().name());
        data.put("text", m.getContent());
        data.put("error", error);
        notifications.notify(NotificationDraft.builder()
                .type(NotificationType.SEND_FAILED)
                .severity(NotificationSeverity.WARNING)
                .title("Gửi tin cho khách thất bại")
                .body("Một tin " + (m.getSenderType().name().equals("AI") ? "của AI" : "báo nhận")
                        + " chưa tới được khách, cần nhân viên trả lời.")
                .data(data)
                .conversationId(m.getConversationId())
                .dedupeKey("SEND_FAILED:" + m.getId())
                .build());
    }

    private static Map<String, Object> turnData(TurnSnapshot s, AiRun run, AiReply reply,
                                                ReplyPolicy.Decision decision, Message aiMessage) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("customerName", s.customerName());
        data.put("channel", s.channel().name());
        data.put("pageId", s.identity().getAccountId());
        data.put("action", decision.action().name());
        data.put("reasons", decision.reasons());
        data.put("reasonLabels", decision.reasons().stream().map(r -> REASON_LABELS.getOrDefault(r, r)).toList());
        if (!decision.unverifiedNumbers().isEmpty()) {
            data.put("unverifiedNumbers", decision.unverifiedNumbers());
        }
        if (aiMessage != null) {
            data.put("draftMessageId", aiMessage.getId());
            data.put("draftText", aiMessage.getContent());
        }
        if (reply != null) {
            data.put("intent", reply.intent());
            if (reply.handoffNeeded()) {
                data.put("aiHandoffReason", reply.handoff().reason());
            }
        }
        if (!s.pendingInbound().isEmpty()) {
            data.put("lastCustomerMessage", s.pendingInbound().get(s.pendingInbound().size() - 1).getContent());
        }
        data.put("aiRunId", run.getId());
        return data;
    }

    private static String customerLabel(TurnSnapshot s) {
        return s.customerName() != null && !s.customerName().isBlank() ? s.customerName().strip() : "khách Messenger";
    }

    private static String labels(List<String> reasons) {
        return reasons.isEmpty() ? "không rõ lý do"
                : String.join(", ", reasons.stream().map(r -> REASON_LABELS.getOrDefault(r, r)).toList());
    }
}
