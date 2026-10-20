package vn.slmglobal.slmai.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Bỏ dấu tiếng Việt ở tầng Java, đối xứng với hàm {@code f_unaccent()} của PostgreSQL (V1).
 * Dùng khi so khớp tên tỉnh (rules/regions.yaml, mục 4.2.9), tìm kiếm không dấu, sinh slug.
 */
public final class VietnameseTextUtil {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NON_SLUG_CHARS = Pattern.compile("[^a-z0-9\\s-]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern REPEATED_DASH = Pattern.compile("-{2,}");
    private static final Pattern EDGE_DASH = Pattern.compile("^-|-$");

    private VietnameseTextUtil() {
    }

    /**
     * Bỏ dấu và chuyển về chữ thường: "Hà Nội" → "ha noi".
     * Chữ đ/Đ không phải dấu tổ hợp nên Normalizer không tách được, phải thay riêng.
     */
    public static String unaccent(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String stripped = DIACRITICS.matcher(normalized).replaceAll("");
        return stripped.replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    /** "Chính sách bảo hành" → "chinh-sach-bao-hanh" */
    public static String toSlug(String input) {
        String base = unaccent(input);
        if (base.isEmpty()) {
            return "";
        }
        String slug = NON_SLUG_CHARS.matcher(base).replaceAll("");
        slug = WHITESPACE.matcher(slug).replaceAll("-");
        slug = REPEATED_DASH.matcher(slug).replaceAll("-");
        return EDGE_DASH.matcher(slug).replaceAll("");
    }
}
