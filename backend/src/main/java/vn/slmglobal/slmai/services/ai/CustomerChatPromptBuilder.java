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
import vn.slmglobal.slmai.services.knowledge.KnowledgeSnapshot;
import vn.slmglobal.slmai.services.lead.ConsultSlots;
import vn.slmglobal.slmai.services.lead.entity.ConsultProfile;
import vn.slmglobal.slmai.services.media.MediaCatalog;
import vn.slmglobal.slmai.services.media.MediaPlanner;
import vn.slmglobal.slmai.services.settings.AiSettingsService;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Ghép prompt cho một lượt trả lời khách (mục 5.4, 32):
 * <ol>
 *   <li>Khối cố định (cache): hướng dẫn prompts/customer-chat + toàn bộ tài liệu kiến thức của bản phát hành
 *   đang chạy (mục 5.3) + cách gửi media và danh mục media (mục 7.1); dựng lại khi kích hoạt bản mới, đổi tên công
 *   ty / tư vấn viên hoặc danh mục media đổi.</li>
 *   <li>Khối ngữ cảnh lượt (không cache): thời điểm, giờ làm việc, hồ sơ tư vấn, câu hỏi trước.</li>
 *   <li>Lịch sử hội thoại: tin khách là user, tin của trang là assistant; tin liền nhau cùng phía được gộp.</li>
 * </ol>
 * Số điện thoại bị che trước khi gửi Claude; ảnh / tệp thay bằng mô tả, media trang đã gửi ghi [Đã gửi kèm: …].
 */
@Component
public class CustomerChatPromptBuilder {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    /** Khách chờ từ mức này (tính từ tin đầu tiên chưa được trả lời) thì báo AI để mở đầu bằng câu xin lỗi riêng */
    static final Duration LONG_WAIT = Duration.ofMinutes(2);
    private static final Map<DayOfWeek, String> DAY_NAMES = Map.of(
            DayOfWeek.MONDAY, "thứ Hai", DayOfWeek.TUESDAY, "thứ Ba", DayOfWeek.WEDNESDAY, "thứ Tư",
            DayOfWeek.THURSDAY, "thứ Năm", DayOfWeek.FRIDAY, "thứ Sáu", DayOfWeek.SATURDAY, "thứ Bảy",
            DayOfWeek.SUNDAY, "Chủ nhật");

    private final AiSettingsService settings;
    private final KnowledgeBase knowledgeBase;
    /** Khối cố định đã ghép gần nhất; giữ nguyên từng byte giữa các lượt để prompt cache trúng */
    private final AtomicReference<SystemBlock> system = new AtomicReference<>();

    /**
     * @param key     phiên bản kiến thức + tên công ty + tên tư vấn viên + phiên bản danh mục media
     * @param text    hướng dẫn + khối kiến thức + khối gửi media
     * @param version luot_goi_ai.phien_ban_prompt
     */
    private record SystemBlock(String key, String text, String version) {
    }

    public CustomerChatPromptBuilder(AiSettingsService settings, KnowledgeBase knowledgeBase) {
        this.settings = settings;
        this.knowledgeBase = knowledgeBase;
    }

    /** luot_goi_ai.phien_ban_prompt của khối cố định ghép từ snapshot này */
    public String promptVersion(KnowledgeSnapshot k, MediaCatalog.Block media) {
        return system(k, media).version();
    }

    /**
     * Rỗng khi không có tin nào của khách cần trả lời (tin cuối thuộc về trang).
     *
     * @param k            kiến thức của lượt này (một snapshot cho cả lượt)
     * @param media        khối gửi media của kênh (MediaCatalog), null khi không đọc được thư viện
     * @param pricingTools có công cụ báo giá (catalog đã đồng bộ); tool do AiTurnService gắn vào request
     */
    public Optional<ClaudeChatClient.ChatRequest> build(TurnSnapshot s, KnowledgeSnapshot k, MediaCatalog.Block media,
                                                        ZonedDateTime now, boolean businessOpen,
                                                        boolean pricingTools) {
        List<ClaudeChatClient.ChatTurn> turns = turns(s.history());
        if (turns.isEmpty() || !turns.get(turns.size() - 1).customer()) {
            return Optional.empty();
        }
        return Optional.of(new ClaudeChatClient.ChatRequest(system(k, media).text(),
                turnContext(s, now, businessOpen, pricingTools), turns, AiReplySchema.schema()));
    }

    /**
     * Ngữ cảnh của một lượt nhắc khách (mục 56.9).
     *
     * @param attemptNo  lần nhắc thứ mấy trong cửa sổ 24 giờ
     * @param silentFor  khách im lặng bao lâu kể từ tin cuối của trang
     * @param windowLeft còn bao lâu trước khi hết cửa sổ 24 giờ
     * @param lastChance lần nhắc sau sẽ rơi ra ngoài cửa sổ hoặc khung giờ: có thể là lần cuối
     * @param attached   media hệ thống sẽ tự gửi kèm (mô tả cho AI); rỗng là không có
     */
    public record FollowUpContext(int attemptNo, Duration silentFor, Duration windowLeft, boolean lastChance,
                                  List<String> attached) {
    }

    /**
     * Lượt nhắc khách: cùng khối cố định (cache trúng), ngữ cảnh lượt thêm mục nhắc khách, lịch sử thêm một lượt user
     * cuối là ghi chú hệ thống (Messages API cần kết thúc bằng lượt user). Rỗng khi tin cuối không phải của trang.
     */
    public Optional<ClaudeChatClient.ChatRequest> buildFollowUp(TurnSnapshot s, KnowledgeSnapshot k,
                                                                MediaCatalog.Block media, ZonedDateTime now,
                                                                boolean businessOpen, boolean pricingTools,
                                                                FollowUpContext f) {
        List<ClaudeChatClient.ChatTurn> turns = turns(s.history());
        if (turns.isEmpty() || turns.get(turns.size() - 1).customer()) {
            return Optional.empty();
        }
        turns.add(new ClaudeChatClient.ChatTurn(true, followUpNote(f.silentFor())));
        String base = turnContext(s, now, businessOpen, pricingTools);
        String context = base.substring(0, base.length() - CONTEXT_END.length()) + followUpContext(f) + CONTEXT_END;
        return Optional.of(new ClaudeChatClient.ChatRequest(system(k, media).text(), context, turns,
                AiReplySchema.schema()));
    }

    /** Lượt user cuối của lượt nhắc */
    static String followUpNote(Duration silentFor) {
        return "[Khách chưa trả lời sau " + waitText(silentFor) + "]";
    }

    static String followUpContext(FollowUpContext f) {
        StringBuilder sb = new StringBuilder();
        sb.append("Lượt này là lượt NHẮC KHÁCH, không phải trả lời: khách chưa trả lời từ ")
                .append(waitText(f.silentFor())).append(" sau tin cuối của bạn. Lượt user cuối \"").append(followUpNote(f.silentFor()))
                .append("\" là ghi chú của hệ thống, không phải lời khách.\n");
        sb.append("Đây là lần nhắc thứ ").append(f.attemptNo())
                .append(" kể từ tin cuối của khách; còn khoảng ").append(waitText(f.windowLeft()))
                .append(" nữa là hết thời gian được nhắn cho khách")
                .append(f.lastChance() ? ", lần này có thể là lần nhắc cuối" : "").append(".\n");
        sb.append("Cách nhắc (theo playbooks/nhac-khach nếu có): 1–2 câu ngắn, nhắc đúng điều khách đang quan ")
                .append("tâm hoặc câu khách còn bỏ ngỏ; không lặp lại tin trước hay lần nhắc trước, không ép chốt, ")
                .append("không hứa khuyến mãi, tối đa một câu hỏi.");
        if (f.lastChance()) {
            sb.append(" Lần cuối thì chào nhẹ, mời khách nhắn lại khi cần hoặc để lại số điện thoại để nhân viên gọi ")
                    .append("tư vấn.");
        }
        sb.append("\n");
        sb.append("Hệ thống sẽ tự gửi kèm sau tin nhắc: ")
                .append(f.attached().isEmpty() ? "không có media nào" : String.join("; ", f.attached()));
        if (!f.attached().isEmpty()) {
            sb.append(" (có thể nói ngắn \"em gửi anh/chị … bên dưới ạ\")");
        }
        sb.append(". Chỉ chọn thêm mã ở trường media khi thật sự liên quan, không chọn media đã gửi.\n");
        sb.append("Để reply là chuỗi rỗng \"\" (không nhắc) khi: khách đã từ chối hoặc nói chưa có nhu cầu, ")
                .append("bảo đừng nhắn nữa, đã chào kết thúc hoặc hẹn tự liên hệ lại, đang khiếu nại hay báo sự cố, ")
                .append("hoặc nhân viên đã hẹn gọi khách. Lượt nhắc không chuyển nhân viên (handoff.needed là false), ")
                .append("collected để null hết, intent theo nội dung tin nhắc (thường TU_VAN_LAP_MOI hoặc HOI_GIA).\n");
        return sb.toString();
    }

    private SystemBlock system(KnowledgeSnapshot k, MediaCatalog.Block media) {
        AiProperties ai = settings.ai();
        String key = k.version() + "|" + ai.companyName() + "|" + ai.personaName() + "|"
                + (media == null ? "" : media.version());
        SystemBlock cached = system.get();
        if (cached != null && cached.key().equals(key)) {
            return cached;
        }
        String instructions = knowledgeBase.renderPrompt(k);
        String text = instructions + "\n\n# Tài liệu kiến thức\n\n" + knowledgeBase.renderKnowledge(k)
                + (media == null ? "" : "\n\n" + media.text());
        SystemBlock built = new SystemBlock(key, text,
                "customer-chat:" + HashUtil.sha256Hex(instructions).substring(0, 16));
        system.set(built);
        return built;
    }

    private static final String CONTEXT_END = "</ngu_canh_luot>";

    String turnContext(TurnSnapshot s, ZonedDateTime now, boolean businessOpen, boolean pricingTools) {
        AiProperties.BusinessHours hours = settings.ai().businessHours();
        ZonedDateTime local = now.withZoneSameInstant(hours.zoneId());
        ConsultProfile p = s.profile();
        StringBuilder sb = new StringBuilder("<ngu_canh_luot>\n");
        sb.append("Thời điểm: ").append(DAY_NAMES.get(local.getDayOfWeek())).append(", ")
                .append(TIME.format(local)).append(" giờ Việt Nam; ")
                .append(businessOpen ? "trong" : "ngoài").append(" giờ làm việc của nhân viên (")
                .append(hours.start()).append("–").append(hours.end()).append(", ").append(dayList(hours.days()))
                .append(").\n");
        sb.append("Kênh: ").append(channelName(s.channel())).append(".\n");
        sb.append("Tên khách (khách tự giới thiệu hoặc tên Facebook): ")
                .append(s.customerName() == null ? "chưa biết" : s.customerName()).append(".\n");
        sb.append("Cách gọi khách đã xác định: ").append(salutation(s.salutation())).append(".\n");
        sb.append("Khách đã cho số điện thoại: ").append(s.hasPhone() ? "có" : "chưa").append(".\n");
        sb.append("Hồ sơ tư vấn đã thu: ").append(p == null ? "chưa có" : slots(p.getSlots())).append(".\n");
        List<String> missing = p == null ? ConsultSlots.REQUIRED : List.of(p.getMissingSlots());
        sb.append("Thông tin tối thiểu còn thiếu: ").append(missing.isEmpty() ? "đã đủ" : String.join(", ", missing));
        if (missing.size() > 1 && missing.contains(ConsultSlots.TY_LE_NGAY_DEM)) {
            sb.append(" (ty_le_ngay_dem hỏi sau cùng, khi đã hỏi xong các thông tin kia)");
        }
        sb.append(".\n");
        if (missing.contains(ConsultSlots.LOAI_MAI)
                && (p == null || !p.getSlots().hasNonNull(ConsultSlots.DIEN_TICH_MAI))) {
            sb.append("Hỏi loại mái thì hỏi kèm diện tích mái trong cùng một câu (ví dụ \"Mái nhà mình là mái tôn, ")
                    .append("ngói hay mái bằng, rộng khoảng bao nhiêu m2 ạ?\"); khách không biết diện tích thì thôi, ")
                    .append("không hỏi lại.\n");
        }
        sb.append("Câu bạn hỏi ở tin trước: ")
                .append(p == null || p.getLastQuestionAsked() == null ? "không có" : p.getLastQuestionAsked())
                .append(".\n");
        sb.append("Số câu hỏi khách đã trả lời: ").append(p == null ? 0 : p.getAnsweredQuestions()).append(".\n");
        sb.append("Phân loại đến lượt trước: ")
                .append(p == null || p.getQualification().isEmpty() ? "chưa có" : p.getQualification().toString())
                .append(".\n");
        if (s.handedBackByStaff()) {
            sb.append("Nhân viên vừa chủ động giao lại hội thoại cho bạn: lần chuyển nhân viên trước đã xong, không ")
                    .append("nhắc lại là đã chuyển nhân viên. Tự trả lời câu khách còn đang chờ trong lịch sử; khách ")
                    .append("giục (alo, đâu rồi, trả lời đi) chỉ là đang chờ câu trả lời. Chỉ đặt handoff.needed khi ")
                    .append("khách nêu yêu cầu mới thuộc mục chuyển nhân viên hoặc nói rõ muốn gặp, gọi điện với ")
                    .append("người.\n");
        } else if (s.firstTurnAfterHandback()) {
            sb.append("Hội thoại vừa được trả lại cho bạn sau khi nhân viên theo: đọc kỹ các tin nhân viên đã nhắn, ")
                    .append("không lặp lại hoặc nói trái với nhân viên. Tự trả lời câu khách còn đang chờ; khách nhắn ")
                    .append("lại không có nghĩa là muốn gặp nhân viên.\n");
        }
        if (!s.pendingInbound().isEmpty() && s.pendingInbound().get(0).getCreatedAt() != null) {
            Duration waited = Duration.between(s.pendingInbound().get(0).getCreatedAt(), now);
            if (waited.compareTo(LONG_WAIT) >= 0) {
                sb.append("Khách đã chờ khoảng ").append(waitText(waited))
                        .append(" kể từ tin đầu tiên chưa được trả lời.\n");
            }
        }
        sb.append("Cách gửi: mỗi đoạn cách nhau một dòng trống được gửi thành một tin nhắn riêng, giữa các tin có hiện ")
                .append("đang soạn tin. Trả lời dài (giải thích, báo giá kèm điều kiện) thì chia 2–3 đoạn ngắn, mỗi đoạn ")
                .append("một ý, câu hỏi ở đoạn cuối; các dòng liệt kê để chung một đoạn. Khách giục hoặc đã chờ lâu thì ")
                .append("đoạn đầu là một câu ngắn lịch sự đứng riêng (ví dụ \"Dạ em đây ạ\", \"Em xin lỗi đã để anh đợi ")
                .append("lâu ạ\"), rồi mới tới nội dung. Link (gói, trang thiết bị) để ở một đoạn riêng, chỉ có ")
                .append("link: hệ thống gửi link thành tin riêng để khách thấy ảnh xem trước của trang.\n");
        if (pricingTools) {
            sb.append("Công cụ báo giá: có (tim_goi_phu_hop, chi_tiet_goi, tra_cuu_vat_tu). Chưa đủ thông tin tối ")
                    .append("thiểu thì chưa đề xuất gói: hỏi lần lượt thông tin còn thiếu, mỗi tin một câu; khách hỏi ")
                    .append("giá thì gọi tim_goi_phu_hop và chỉ nói giá khởi điểm một lần. Vừa đủ thông tin tối thiểu ")
                    .append("thì gọi tim_goi_phu_hop để đề xuất gói. Đã đề xuất rồi thì không nhắc lại gói ở mỗi tin, ")
                    .append("chỉ báo lại khi khách hỏi hoặc khách đổi thông tin làm đổi gói. Chỉ dùng con số và link ")
                    .append("trong kết quả công cụ (playbooks/khach-hoi-gia). Khách hỏi ")
                    .append("về một thiết bị (giá riêng tấm pin / biến tần / pin lưu trữ, có hãng hay loại nào khác, ")
                    .append("thông số, bảo hành, gói nào có thiết bị đó) hoặc gửi link trang thiết bị (/devices/…) ")
                    .append("thì gọi tra_cuu_vat_tu và trả lời theo kết quả, không chuyển nhân viên chỉ vì khách hỏi ")
                    .append("giá thiết bị lẻ. Khách hỏi giá một cấu hình tự nêu (số tấm, dung lượng pin) thì gọi ")
                    .append("tim_goi_phu_hop với so_tam_pin / luu_tru_kwh và báo gói đúng hoặc gần nhất.\n");
        } else {
            sb.append("Công cụ báo giá: chưa có (bảng giá chưa đồng bộ). Không đưa con số giá, công suất, sản lượng; ")
                    .append("mời khách để lại số điện thoại để nhân viên gửi báo giá.\n");
        }
        sb.append(CONTEXT_END);
        return sb.toString();
    }

    /** "5 phút", "2 giờ 10 phút", "3 ngày" */
    static String waitText(Duration d) {
        if (d.toDays() >= 1) {
            return d.toDays() + " ngày";
        }
        if (d.toHours() >= 1) {
            return d.toHours() + " giờ" + (d.toMinutesPart() > 0 ? " " + d.toMinutesPart() + " phút" : "");
        }
        return d.toMinutes() + " phút";
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
            String text = customer ? describeInbound(m) : describeOutbound(m);
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

    /** Tin trang đã gửi, kèm media đã tới khách để AI không gửi lại và nói đúng về chúng */
    private static String describeOutbound(Message m) {
        String text = m.getContent() == null ? "" : VietnamesePhone.mask(m.getContent());
        List<String> sent = new ArrayList<>();
        JsonNode attachments = m.getAttachments();
        if (attachments != null && attachments.isArray()) {
            for (JsonNode a : attachments) {
                if (!MediaPlanner.STATUS_SENT.equals(a.path(MediaPlanner.STATUS).asText())) {
                    continue;
                }
                String code = a.path("code").asText("");
                String title = a.path("title").asText("");
                if (MediaPlanner.KIND_QUOTE.equals(a.path(MediaPlanner.KIND).asText())) {
                    sent.add(title.isEmpty() ? "ảnh báo giá" : title);
                } else if (MediaPlanner.KIND_MEDIA.equals(a.path(MediaPlanner.KIND).asText())) {
                    sent.add(("VIDEO".equals(a.path("media_type").asText()) ? "video " : "ảnh ")
                            + (code.isEmpty() ? "" : code + " ") + title);
                }
            }
        }
        if (sent.isEmpty()) {
            return text;
        }
        return (text.isBlank() ? "" : text + "\n") + "[Đã gửi kèm: " + String.join("; ", sent) + "]";
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
        return "CHI".equals(s) ? "chị" : "chưa xác định (đoán từ tên theo core/voice; không chắc thì dùng anh/chị)";
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
