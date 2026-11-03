package vn.slmglobal.slmai.services.knowledge.admin;

import vn.slmglobal.slmai.common.util.VietnameseTextUtil;
import vn.slmglobal.slmai.services.knowledge.KnowledgeBase;
import vn.slmglobal.slmai.services.knowledge.KnowledgeDocType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kiểm tra tức thì khi soạn (mục 5.10 A): chạy bằng code, không gọi AI, chỉ cảnh báo / gợi ý. Lỗi chặn phát hành
 * (mã, rule, token) nằm ở bước kiểm tra khi phát hành.
 */
public final class KnowledgeLinter {

    public enum Level {
        /** Chặn lưu / phát hành (rule YAML sai) */
        ERROR,
        WARNING,
        INFO
    }

    /** @param line dòng (đánh số từ 1); null khi áp cho cả tài liệu */
    public record Issue(Level level, String code, String message, Integer line) {
    }

    private static final Pattern MONEY_OR_POWER = Pattern.compile(
            "(?iu)\\b\\d[\\d.,]*\\s*(triệu|tr|nghìn|ngàn|đồng|đ|vnđ|vnd|kwp|kw|kwh|wp)\\b");
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*([^}]*)\\s*}}");
    private static final Pattern SENTENCE_END = Pattern.compile("[.!?…](\\s|$)");
    /** Từ tiếng Việt hay gặp khi gõ không dấu */
    private static final Set<String> UNACCENTED = Set.of("khong", "duoc", "cua", "nhung", "khach", "nguoi", "chung",
            "toi", "minh", "dien", "mat", "troi", "lap", "dat", "hang", "nhan", "vien", "neu", "thi", "cho", "nhe");
    private static final Map<KnowledgeDocType, List<String>> REQUIRED_SECTIONS = Map.of(
            KnowledgeDocType.FAQ, List.of("## Trả lời ngắn", "## Chuyển nhân viên khi"),
            KnowledgeDocType.PLAYBOOK, List.of("## Mục tiêu", "## Các bước"));

    private KnowledgeLinter() {
    }

    /**
     * @param terms bảng thuật ngữ (guide/thuat-ngu): từ nên tránh → từ nên dùng
     */
    public static List<Issue> lint(KnowledgeDocType type, String content, Map<String, String> terms) {
        List<Issue> out = new ArrayList<>();
        if (content == null || content.isBlank()) {
            out.add(new Issue(Level.WARNING, "EMPTY", "Tài liệu đang trống", null));
            return out;
        }
        if (type != null && type.yaml()) {
            return out; // rule YAML: kiểm tra bằng schema (RuleCodec)
        }
        String[] lines = content.split("\n", -1);
        boolean inCode = false;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.strip().startsWith("```")) {
                inCode = !inCode;
                continue;
            }
            if (inCode) {
                continue;
            }
            int no = i + 1;
            if (type != null && type.botReads()) {
                Matcher m = MONEY_OR_POWER.matcher(line);
                if (m.find()) {
                    out.add(new Issue(Level.WARNING, "HARD_NUMBER", "Có con số \"" + m.group().strip()
                            + "\": giá, công suất phải lấy từ bảng giá qua công cụ báo giá, không ghi trong tài liệu",
                            no));
                }
            }
            Matcher p = PLACEHOLDER.matcher(line);
            while (p.find()) {
                String full = p.group();
                if (!full.equals(KnowledgeBase.COMPANY_PLACEHOLDER) && !full.equals(KnowledgeBase.PERSONA_PLACEHOLDER)) {
                    out.add(new Issue(Level.WARNING, "UNKNOWN_PLACEHOLDER", "Chỗ thay " + full
                            + " không tồn tại; chỉ dùng {{ten_cong_ty}} và {{ten_tu_van_vien}}", no));
                }
            }
            if (looksUnaccented(line)) {
                out.add(new Issue(Level.WARNING, "NO_DIACRITICS", "Dòng này có vẻ viết không dấu", no));
            }
            if (!line.strip().startsWith("|") && sentences(line) > 5) {
                out.add(new Issue(Level.INFO, "LONG_PARAGRAPH", "Đoạn văn dài hơn 5 câu, nên tách nhỏ", no));
            }
            String lower = line.toLowerCase(Locale.ROOT);
            for (Map.Entry<String, String> t : terms.entrySet()) {
                if (containsWord(lower, t.getKey())) {
                    out.add(new Issue(Level.INFO, "TERM", "Nên dùng \"" + t.getValue() + "\" thay cho \"" + t.getKey()
                            + "\" (guide/thuat-ngu)", no));
                }
            }
        }
        for (String section : REQUIRED_SECTIONS.getOrDefault(type, List.of())) {
            if (!content.contains(section)) {
                out.add(new Issue(Level.INFO, "MISSING_SECTION", "Thiếu mục \"" + section + "\" của mẫu", null));
            }
        }
        return out;
    }

    /**
     * Đọc bảng thuật ngữ Markdown (cột 1 "Dùng", cột 2 "Tránh", nhiều từ cách nhau bằng dấu phẩy) thành từ nên tránh →
     * từ nên dùng. Bỏ phần trong ngoặc của cột "Dùng".
     */
    public static Map<String, String> terms(String table) {
        Map<String, String> out = new LinkedHashMap<>();
        if (table == null) {
            return out;
        }
        for (String row : table.split("\n")) {
            String r = row.strip();
            if (!r.startsWith("|") || r.matches("\\|[\\s:|-]+\\|?")) {
                continue;
            }
            String[] cells = r.substring(1).split("\\|");
            if (cells.length < 2 || cells[0].strip().equalsIgnoreCase("Dùng")) {
                continue;
            }
            String use = cells[0].replaceAll("\\(.*?\\)", "").strip();
            for (String avoid : cells[1].split(",")) {
                String a = avoid.replaceAll("\\(.*?\\)", "").strip().toLowerCase(Locale.ROOT);
                if (!a.isEmpty() && !use.isEmpty()) {
                    out.put(a, use);
                }
            }
        }
        return out;
    }

    private static boolean containsWord(String lowerLine, String term) {
        int at = lowerLine.indexOf(term);
        while (at >= 0) {
            boolean startOk = at == 0 || !Character.isLetterOrDigit(lowerLine.charAt(at - 1));
            int end = at + term.length();
            boolean endOk = end >= lowerLine.length() || !Character.isLetterOrDigit(lowerLine.charAt(end));
            if (startOk && endOk) {
                return true;
            }
            at = lowerLine.indexOf(term, at + 1);
        }
        return false;
    }

    /** Dòng ≥ 6 từ, không có chữ có dấu nào, và có ít nhất 2 từ tiếng Việt quen thuộc viết không dấu */
    private static boolean looksUnaccented(String line) {
        String text = line.replaceAll("`[^`]*`", " ").replaceAll("https?://\\S+", " ");
        String[] words = text.toLowerCase(Locale.ROOT).split("[^\\p{L}]+");
        // unaccent() đổi cả chữ hoa thành thường: so với bản chữ thường để chỉ còn khác ở dấu
        if (words.length < 6 || !VietnameseTextUtil.unaccent(text).equals(text.toLowerCase(Locale.ROOT).trim())) {
            return false;
        }
        int hits = 0;
        for (String w : words) {
            if (UNACCENTED.contains(w)) {
                hits++;
            }
        }
        return hits >= 2;
    }

    private static int sentences(String line) {
        Matcher m = SENTENCE_END.matcher(line);
        int n = 0;
        while (m.find()) {
            n++;
        }
        return n;
    }
}
