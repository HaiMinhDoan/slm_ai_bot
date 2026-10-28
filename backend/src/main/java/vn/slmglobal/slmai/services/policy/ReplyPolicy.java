package vn.slmglobal.slmai.services.policy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.slmglobal.slmai.services.ai.AiProperties;
import vn.slmglobal.slmai.services.ai.AiReply;
import vn.slmglobal.slmai.services.conversation.entity.ReplyMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Policy cho một lượt AI trả lời khách (mục 33, 35): gửi ngay (AUTO), lưu nháp chờ duyệt (DRAFT), hay chuyển
 * nhân viên (HANDOFF). Chế độ tính theo lượt; một lượt thuộc loại AUTO vẫn rơi về DRAFT khi gặp bất kỳ lý do nào
 * dưới đây. Ngoài cửa sổ nhắn tin cũng là DRAFT (không gửi được), lý do OUTSIDE_WINDOW.
 *
 * <p>Link: chỉ được gửi link có trong danh sách cho phép của lượt (link trong kết quả tool, link trang đã gửi
 * trước đó, trang chủ website theo miền); link khác (AI tự nghĩ ra, email) thì DRAFT, lý do LINK_NOT_ALLOWED.
 */
@Component
@RequiredArgsConstructor
public class ReplyPolicy {

    public enum Action {
        AUTO,
        DRAFT,
        HANDOFF
    }

    /** Mã lý do ghi vào luot_goi_ai.ket_qua_policy */
    public static final String HANDOFF_INTENT = "HANDOFF_INTENT";
    public static final String HANDOFF_REQUESTED = "HANDOFF_REQUESTED";
    public static final String EMPTY_REPLY = "EMPTY_REPLY";
    public static final String FORCE_DRAFT = "FORCE_DRAFT";
    public static final String CONVERSATION_DRAFT_MODE = "CONVERSATION_DRAFT_MODE";
    public static final String FIRST_TURN_AFTER_HANDBACK = "FIRST_TURN_AFTER_HANDBACK";
    public static final String INTENT_NOT_AUTO = "INTENT_NOT_AUTO";
    public static final String NUMBER_CHECK_FAILED = "NUMBER_CHECK_FAILED";
    public static final String LINK_NOT_ALLOWED = "LINK_NOT_ALLOWED";
    public static final String TOO_LONG = "TOO_LONG";
    public static final String MULTIPLE_QUESTIONS = "MULTIPLE_QUESTIONS";
    public static final String CHANNEL_NOT_SUPPORTED = "CHANNEL_NOT_SUPPORTED";
    public static final String OUTSIDE_WINDOW = "OUTSIDE_WINDOW";

    private static final Pattern LINK = Pattern.compile("(?i)(https?://|www\\.|\\b[\\w-]+\\.(com|vn|net|org)\\b)");
    /** Một link đầy đủ: có hoặc không có https://, www., đường dẫn */
    private static final Pattern LINK_TOKEN = Pattern.compile(
            "(?i)(?:https?://)?(?:www\\.)?(?:[a-z0-9-]+\\.)+(?:com|vn|net|org)\\b(?:/[^\\s]*)?");
    private static final Pattern TRAILING_PUNCTUATION = Pattern.compile("[.,;:!?)\\]}'\"]+$");

    private final AiProperties properties;

    public record Input(AiReply reply, ReplyMode replyMode, boolean firstTurnAfterHandback, boolean windowOpen,
                        boolean channelSupported, int maxTextLength, Collection<String> numberSourceTexts,
                        Collection<NumberChecker.Quantity> numberSourceValues, Collection<String> allowedLinks) {
    }

    public record Decision(Action action, List<String> reasons, List<String> unverifiedNumbers) {

        public static Decision handoff(String reason) {
            return new Decision(Action.HANDOFF, List.of(reason), List.of());
        }
    }

    public Decision evaluate(Input in) {
        AiReply reply = in.reply();
        if (properties.handoffIntents() != null && properties.handoffIntents().contains(reply.intent())) {
            return Decision.handoff(HANDOFF_INTENT);
        }
        if (reply.handoffNeeded()) {
            return Decision.handoff(HANDOFF_REQUESTED);
        }
        if (!reply.hasReply()) {
            return Decision.handoff(EMPTY_REPLY);
        }

        List<String> reasons = new ArrayList<>();
        if (properties.forceDraft()) {
            reasons.add(FORCE_DRAFT);
        }
        if (in.replyMode() == ReplyMode.DRAFT) {
            reasons.add(CONVERSATION_DRAFT_MODE);
        }
        if (in.firstTurnAfterHandback()) {
            reasons.add(FIRST_TURN_AFTER_HANDBACK);
        }
        if (properties.autoIntents() == null || !properties.autoIntents().contains(reply.intent())) {
            reasons.add(INTENT_NOT_AUTO);
        }
        NumberChecker.Result numbers = NumberChecker.check(reply.reply(), in.numberSourceTexts(),
                in.numberSourceValues());
        List<String> unverified = new ArrayList<>();
        numbers.unverified().forEach(q -> unverified.add(q.text()));
        unverified.addAll(numbers.spelledOut());
        if (!numbers.passed()) {
            reasons.add(NUMBER_CHECK_FAILED);
        }
        if (!linksAllowed(reply.reply(), in.allowedLinks())) {
            reasons.add(LINK_NOT_ALLOWED);
        }
        if (reply.reply().length() > in.maxTextLength()) {
            reasons.add(TOO_LONG);
        }
        if (countQuestions(reply.reply()) > 1) {
            reasons.add(MULTIPLE_QUESTIONS);
        }
        if (!in.channelSupported()) {
            reasons.add(CHANNEL_NOT_SUPPORTED);
        }
        if (!in.windowOpen()) {
            reasons.add(OUTSIDE_WINDOW);
        }
        return new Decision(reasons.isEmpty() ? Action.AUTO : Action.DRAFT, List.copyOf(reasons),
                List.copyOf(unverified));
    }

    /** Mọi link trong tin đều thuộc danh sách cho phép (so sau khi bỏ https://, www., dấu câu và / ở cuối). */
    static boolean linksAllowed(String text, Collection<String> allowed) {
        List<String> found = links(text);
        if (found.isEmpty()) {
            // "www." hay "http://" lẻ mà không thành link đầy đủ: vẫn coi là link lạ
            return !LINK.matcher(text).find();
        }
        if (allowed == null || allowed.isEmpty()) {
            return false;
        }
        List<String> ok = allowed.stream().map(ReplyPolicy::normalizeLink).toList();
        return found.stream().map(ReplyPolicy::normalizeLink).allMatch(ok::contains);
    }

    /** Các link trong một đoạn văn bản, đã bỏ dấu câu dính ở cuối */
    public static List<String> links(String text) {
        List<String> out = new ArrayList<>();
        if (text == null) {
            return out;
        }
        Matcher m = LINK_TOKEN.matcher(text);
        while (m.find()) {
            out.add(TRAILING_PUNCTUATION.matcher(m.group()).replaceAll(""));
        }
        return out;
    }

    static String normalizeLink(String link) {
        String s = TRAILING_PUNCTUATION.matcher(link.strip().toLowerCase()).replaceAll("");
        s = s.replaceFirst("^https?://", "").replaceFirst("^www\\.", "");
        while (s.endsWith("/")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    /** Mục 58.2: mỗi tin chỉ một câu hỏi. Đếm dấu "?" là đủ cho tin chat; câu hỏi không có "?" không bị bắt. */
    static int countQuestions(String text) {
        return (int) text.replaceAll("\\?+", "?").chars().filter(ch -> ch == '?').count();
    }
}
