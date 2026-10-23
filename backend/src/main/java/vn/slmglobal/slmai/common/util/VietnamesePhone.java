package vn.slmglobal.slmai.common.util;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Số điện thoại di động Việt Nam: tách từ tin của khách, chuẩn hoá về dạng 0xxxxxxxxx, che khi đưa vào prompt.
 * Backend tự tách số, không dựa vào AI (mục 58.5: khách cho số thì luôn tạo lead).
 */
public final class VietnamesePhone {

    /** Đầu số di động 03, 05, 07, 08, 09; cho phép +84 / 84 và dấu cách, chấm, gạch giữa các chữ số */
    private static final Pattern MOBILE = Pattern.compile(
            "(?<![\\d+])(?:\\+?84|0)[\\s.\\-]?[35789](?:[\\s.\\-]?\\d){8}(?!\\d)");

    public static final String MASK = "[số điện thoại]";

    private VietnamesePhone() {
    }

    /** Số di động đầu tiên trong văn bản, đã chuẩn hoá. */
    public static Optional<String> findFirst(String text) {
        if (text == null || text.isEmpty()) {
            return Optional.empty();
        }
        Matcher m = MOBILE.matcher(text);
        return m.find() ? Optional.of(normalize(m.group())) : Optional.empty();
    }

    public static String mask(String text) {
        return text == null ? null : MOBILE.matcher(text).replaceAll(Matcher.quoteReplacement(MASK));
    }

    static String normalize(String raw) {
        String digits = raw.replaceAll("\\D", "");
        return digits.startsWith("84") ? "0" + digits.substring(2) : digits;
    }
}
