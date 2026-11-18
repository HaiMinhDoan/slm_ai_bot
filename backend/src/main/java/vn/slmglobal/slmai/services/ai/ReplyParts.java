package vn.slmglobal.slmai.services.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Chia câu trả lời thành các tin gửi lần lượt (2026-11-12): mỗi đoạn AI viết cách nhau một dòng trống là một tin
 * (core/voice). Đoạn vẫn quá dài thì tách tiếp ở cuối câu hoặc cuối dòng, gom lại sao cho mỗi tin không quá
 * {@link #LONG_PART} ký tự; quá {@link #MAX_PARTS} tin thì phần thừa gộp vào tin cuối.
 *
 * <p>Link đứng riêng một tin (2026-11-18): Messenger chỉ hiện thẻ xem trước (ảnh, tên trang) khi tin chỉ có link.
 * Link nằm giữa câu thì câu được giữ lại, chỗ link thay bằng "link bên dưới", rồi link gửi ngay sau.
 */
public final class ReplyParts {

    /** Đoạn dài hơn mức này thì tách tiếp ở cuối câu */
    static final int LONG_PART = 400;
    static final int MAX_PARTS = 5;

    private static final Pattern PARAGRAPH = Pattern.compile("\\n[ \\t]*\\n+");
    /** Sau dấu kết câu có khoảng trắng, hoặc xuống dòng; "5,12", "144.200.000", link không bị cắt */
    private static final Pattern SENTENCE_END = Pattern.compile("(?<=[.!?…])[ \\t]+|\\n");
    private static final Pattern URL = Pattern.compile("https?://[^\\s<>\"]+");
    /** Dấu câu dính liền sau link không thuộc link */
    private static final String TRAILING = ".,;:!?)…»\"'";

    private ReplyParts() {
    }

    public static List<String> split(String text) {
        List<String> parts = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return parts;
        }
        for (String paragraph : PARAGRAPH.split(text.strip())) {
            String p = paragraph.strip();
            if (p.isEmpty()) {
                continue;
            }
            if (p.length() <= LONG_PART) {
                parts.add(p);
            } else {
                parts.addAll(pack(p));
            }
        }
        if (parts.size() > MAX_PARTS) {
            List<String> head = new ArrayList<>(parts.subList(0, MAX_PARTS - 1));
            head.add(String.join("\n\n", parts.subList(MAX_PARTS - 1, parts.size())));
            parts = head;
        }
        List<String> out = new ArrayList<>();
        parts.forEach(p -> out.addAll(isolateLinks(p)));
        return out;
    }

    /** Một tin có link và chữ: tin chữ (link thay bằng "link bên dưới" nếu nằm giữa câu) rồi mỗi link một tin */
    static List<String> isolateLinks(String part) {
        if (!URL.matcher(part).find() || isLink(part.strip())) {
            return List.of(part);
        }
        List<String> links = new ArrayList<>();
        List<String> lines = new ArrayList<>();
        for (String line : part.split("\n", -1)) {
            Matcher m = URL.matcher(line);
            StringBuilder sb = new StringBuilder();
            int from = 0;
            while (m.find()) {
                String url = m.group();
                int end = m.end();
                while (!url.isEmpty() && TRAILING.indexOf(url.charAt(url.length() - 1)) >= 0) {
                    url = url.substring(0, url.length() - 1);
                    end--;
                }
                if (!links.contains(url)) {
                    links.add(url);
                }
                sb.append(line, from, m.start());
                String before = sb.toString().stripTrailing();
                if (!before.isEmpty() && !before.endsWith(":")) {
                    sb.append(before.toLowerCase().endsWith("link") ? "bên dưới" : "link bên dưới");
                }
                from = end;
            }
            sb.append(line.substring(from));
            String cleaned = sb.toString().replaceAll("[ \\t]{2,}", " ").replaceAll(" +([.,;:!?])", "$1").strip();
            if (cleaned.chars().anyMatch(Character::isLetterOrDigit)) {
                lines.add(cleaned);
            }
        }
        List<String> out = new ArrayList<>();
        if (!lines.isEmpty()) {
            out.add(String.join("\n", lines));
        }
        out.addAll(links);
        return out;
    }

    /** Cả tin chỉ là một link, không dính dấu câu */
    private static boolean isLink(String s) {
        return URL.matcher(s).matches() && TRAILING.indexOf(s.charAt(s.length() - 1)) < 0;
    }

    /** Gom các câu / dòng của một đoạn dài thành các tin không quá LONG_PART ký tự (câu dài hơn thì để nguyên) */
    private static List<String> pack(String paragraph) {
        List<String> out = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int from = 0;
        var m = SENTENCE_END.matcher(paragraph);
        while (true) {
            boolean found = m.find();
            int end = found ? m.start() : paragraph.length();
            String unit = paragraph.substring(from, end).strip();
            String sep = found && m.group().contains("\n") ? "\n" : " ";
            if (!unit.isEmpty()) {
                if (!current.isEmpty() && current.length() + 1 + unit.length() > LONG_PART) {
                    out.add(current.toString().strip());
                    current.setLength(0);
                }
                current.append(unit).append(sep);
            }
            if (!found) {
                break;
            }
            from = m.end();
        }
        if (!current.isEmpty()) {
            out.add(current.toString().strip());
        }
        return out;
    }
}
