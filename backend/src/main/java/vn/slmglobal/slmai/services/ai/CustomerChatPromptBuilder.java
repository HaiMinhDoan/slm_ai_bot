package vn.slmglobal.slmai.services.ai;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import vn.slmglobal.slmai.common.util.HashUtil;
import vn.slmglobal.slmai.common.util.VietnamesePhone;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.conversation.entity.Message;
import vn.slmglobal.slmai.services.conversation.entity.MessageDirection;
import vn.slmglobal.slmai.services.conversation.entity.MessageStatus;
import vn.slmglobal.slmai.services.knowledge.KnowledgeBase;
import vn.slmglobal.slmai.services.lead.ConsultSlots;
import vn.slmglobal.slmai.services.lead.entity.ConsultProfile;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Ghép prompt cho một lượt trả lời khách (mục 5.4, 32):
 * <ol>
 *   <li>Khối cố định (cache): hướng dẫn prompts/customer-chat.md + toàn bộ tài liệu kiến thức.</li>
 *   <li>Khối ngữ cảnh lượt (không cache): thời điểm, giờ làm việc, hồ sơ tư vấn, câu hỏi trước.</li>
 *   <li>Lịch sử hội thoại: tin khách là user, tin của trang là assistant; tin liền nhau cùng phía được gộp.</li>
 * </ol>
 * Số điện thoại bị che trước khi gửi Claude; ảnh / tệp thay bằng mô tả.
 */
@Component
public class CustomerChatPromptBuilder {

    static final String PROMPT_LOCATION = "prompts/customer-chat.md";

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Map<DayOfWeek, String> DAY_NAMES = Map.of(
            DayOfWeek.MONDAY, "thứ Hai", DayOfWeek.TUESDAY, "thứ Ba", DayOfWeek.WEDNESDAY, "thứ Tư",
            DayOfWeek.THURSDAY, "thứ Năm", DayOfWeek.FRIDAY, "thứ Sáu", DayOfWeek.SATURDAY, "thứ Bảy",
            DayOfWeek.SUNDAY, "Chủ nhật");

    private final AiProperties properties;
    private final String cachedSystem;
    private final String promptVersion;

    public CustomerChatPromptBuilder(AiProperties properties, KnowledgeBase knowledgeBase) {
        this.properties = properties;
        String instructions = KnowledgeBase.readClasspathText(PROMPT_LOCATION, properties.companyName()).strip();
        this.cachedSystem = instructions + "\n\n# Tài liệu kiến thức\n\n" + knowledgeBase.rendered();
        this.promptVersion = "customer-chat:" + HashUtil.sha256Hex(instructions).substring(0, 16);
    }

    /** luot_goi_ai.phien_ban_prompt */
    public String promptVersion() {
        return promptVersion;
    }

    /** Rỗng khi không có tin nào của khách cần trả lời (tin cuối thuộc về trang). */
    public Optional<ClaudeChatClient.ChatRequest> build(TurnSnapshot s, ZonedDateTime now, boolean businessOpen) {
        List<ClaudeChatClient.ChatTurn> turns = turns(s.history());
        if (turns.isEmpty() || !turns.get(turns.size() - 1).customer()) {
            return Optional.empty();
        }
        return Optional.of(new ClaudeChatClient.ChatRequest(cachedSystem, turnContext(s, now, businessOpen), turns,
                AiReplySchema.schema()));
    }

    String turnContext(TurnSnapshot s, ZonedDateTime now, boolean businessOpen) {
        AiProperties.BusinessHours hours = properties.businessHours();
        ZonedDateTime local = now.withZoneSameInstant(hours.zoneId());
        ConsultProfile p = s.profile();
        StringBuilder sb = new StringBuilder("<ngu_canh_luot>\n");
        sb.append("Thời điểm: ").append(DAY_NAMES.get(local.getDayOfWeek())).append(", ")
                .append(TIME.format(local)).append(" giờ Việt Nam; ")
                .append(businessOpen ? "trong" : "ngoài").append(" giờ làm việc của nhân viên (")
                .append(hours.start()).append("–").append(hours.end()).append(", ").append(dayList(hours.days()))
                .append(").\n");
        sb.append("Kênh: ").append(channelName(s.channel())).append(".\n");
        sb.append("Tên khách: ").append(s.customerName() == null ? "chưa biết" : s.customerName())
                .append(". Danh xưng: ").append(salutation(s.salutation())).append(".\n");
        sb.append("Khách đã cho số điện thoại: ").append(s.hasPhone() ? "có" : "chưa").append(".\n");
        sb.append("Hồ sơ tư vấn đã thu: ").append(p == null ? "chưa có" : slots(p.getSlots())).append(".\n");
        List<String> missing = p == null ? ConsultSlots.REQUIRED : List.of(p.getMissingSlots());
        sb.append("Thông tin tối thiểu còn thiếu: ")
                .append(missing.isEmpty() ? "đã đủ" : String.join(", ", missing)).append(".\n");
        sb.append("Câu bạn hỏi ở tin trước: ")
                .append(p == null || p.getLastQuestionAsked() == null ? "không có" : p.getLastQuestionAsked())
                .append(".\n");
        sb.append("Số câu hỏi khách đã trả lời: ").append(p == null ? 0 : p.getAnsweredQuestions()).append(".\n");
        sb.append("Phân loại đến lượt trước: ")
                .append(p == null || p.getQualification().isEmpty() ? "chưa có" : p.getQualification().toString())
                .append(".\n");
        if (s.firstTurnAfterHandback()) {
            sb.append("Hội thoại vừa được trả lại cho bạn sau khi nhân viên theo: đọc kỹ các tin nhân viên đã nhắn, ")
                    .append("không lặp lại hoặc nói trái với nhân viên.\n");
        }
        sb.append("Bảng giá và công cụ tính công suất: chưa có, không đưa con số giá (playbooks/khach-hoi-gia).\n");
        sb.append("</ngu_canh_luot>");
        return sb.toString();
    }

    /** Chuyển tin_nhan thành các lượt user / assistant xen kẽ, bắt đầu bằng user. */
    static List<ClaudeChatClient.ChatTurn> turns(List<Message> history) {
        List<ClaudeChatClient.ChatTurn> out = new ArrayList<>();
        for (Message m : history) {
            boolean customer = m.getDirection() == MessageDirection.IN;
            if (customer && m.getStatus() == MessageStatus.DISCARDED) {
                continue;
            }
            // Tin của trang chỉ tính tin đã thật sự gửi; nháp, lỗi, bị bỏ thì khách chưa thấy
            if (!customer && m.getStatus() != MessageStatus.SENT) {
                continue;
            }
            String text = customer ? describeInbound(m) : VietnamesePhone.mask(m.getContent());
            if (text == null || text.isBlank()) {
                continue;
            }
            if (!out.isEmpty() && out.get(out.size() - 1).customer() == customer) {
                ClaudeChatClient.ChatTurn last = out.remove(out.size() - 1);
                out.add(new ClaudeChatClient.ChatTurn(customer, last.text() + "\n" + text));
            } else {
                out.add(new ClaudeChatClient.ChatTurn(customer, text));
            }
        }
        // Messages API yêu cầu bắt đầu bằng user
        Iterator<ClaudeChatClient.ChatTurn> it = out.iterator();
        while (it.hasNext() && !it.next().customer()) {
            it.remove();
        }
        return out;
    }

    private static String describeInbound(Message m) {
        List<String> parts = new ArrayList<>();
        if (m.getContent() != null && !m.getContent().isBlank()) {
            parts.add(VietnamesePhone.mask(m.getContent().strip()));
        }
        JsonNode attachments = m.getAttachments();
        if (attachments != null && attachments.isArray()) {
            for (JsonNode a : attachments) {
                String d = describeAttachment(a);
                if (d != null) {
                    parts.add(d);
                }
            }
        }
        return String.join(" ", parts);
    }

    private static String describeAttachment(JsonNode a) {
        String type = a.path("type").asText("");
        if (a.path("payload").hasNonNull("sticker_id")) {
            return "[Khách gửi nhãn dán]";
        }
        return switch (type) {
            case "image" -> "[Khách gửi ảnh]";
            case "video" -> "[Khách gửi video]";
            case "audio" -> "[Khách gửi tin nhắn thoại]";
            case "file" -> "[Khách gửi tệp]";
            case "location" -> "[Khách gửi vị trí]";
            case "fallback", "template" -> "[Khách chia sẻ một liên kết hoặc bài viết]";
            // postback: tiêu đề nút đã nằm trong nội dung; quick_reply: nội dung là chữ trên nút
            default -> null;
        };
    }

    private static String slots(JsonNode slots) {
        if (slots == null || slots.isEmpty()) {
            return "chưa có";
        }
        List<String> parts = new ArrayList<>();
        slots.fields().forEachRemaining(e -> parts.add(e.getKey() + " = " + e.getValue().asText()));
        return String.join("; ", parts);
    }

    private static String dayList(List<DayOfWeek> days) {
        if (days == null || days.isEmpty()) {
            return "mọi ngày";
        }
        return days.stream().sorted().map(DAY_NAMES::get).collect(Collectors.joining(", "));
    }

    private static String salutation(String s) {
        if ("ANH".equals(s)) {
            return "anh";
        }
        return "CHI".equals(s) ? "chị" : "chưa rõ (dùng anh/chị hoặc mình)";
    }

    private static String channelName(ChannelCode channel) {
        return switch (channel) {
            case MESSENGER -> "Facebook Messenger";
            case FACEBOOK_COMMENT -> "bình luận Facebook (công khai)";
            case ZALO_OA, ZALO_PERSONAL -> "Zalo";
            case TIKTOK_DM -> "tin nhắn TikTok";
            case TIKTOK_COMMENT -> "bình luận TikTok (công khai)";
            case FACEBOOK_GROUP -> "nhóm Facebook";
        };
    }
}
