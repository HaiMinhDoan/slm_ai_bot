package vn.slmglobal.slmai.services.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Kiểm tra con số trong câu trả lời của AI (mục 33.1). Chỉ xét các loại số rủi ro: tiền, công suất, điện năng,
 * phần trăm, thời hạn bảo hành. Mỗi số phải khớp (cho phép làm tròn) với một số trong nguồn hợp lệ: tin của
 * khách, tài liệu kiến thức đang dùng, và (khi có) kết quả tool của catalog.
 *
 * <p>Không cố hiểu mọi cách viết: thà chặn nhầm sang DRAFT còn hơn để lọt số sai. Tỷ lệ chặn nhầm theo dõi qua
 * luot_goi_ai.ket_qua_policy để chỉnh dần.
 */
public final class NumberChecker {

    public enum Kind {
        /** đồng */
        MONEY,
        /** W */
        POWER,
        /** Wh */
        ENERGY,
        PERCENT,
        /** tháng */
        WARRANTY
    }

    public record Quantity(Kind kind, double value, String text) {
    }

    public record Result(List<Quantity> unverified, List<String> spelledOut) {

        public boolean passed() {
            return unverified.isEmpty() && spelledOut.isEmpty();
        }
    }

    private static final String NUM = "(\\d+(?:[.,]\\d+)*)";
    private static final String END = "(?![\\p{L}\\d])";
    private static final String RANGE_SEP = "\\s*(?:-|–|—|~|đến|tới|den|toi)\\s*";

    private static final String MONEY_UNIT = "(tỷ|tỉ|ty|triệu|trieu|tr|nghìn|ngàn|nghin|ngan|k|vnđ|vnd|đồng|dong|đ)";
    private static final String POWER_UNIT = "(kwp|kw|wp|w)";
    private static final String ENERGY_UNIT = "(kwh|wh|số điện|số)";

    private static final Pattern MONEY = Pattern.compile(NUM + "\\s*" + MONEY_UNIT + END,
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    /** "2tr5" = 2,5 triệu */
    private static final Pattern MONEY_TR_SHORT = Pattern.compile("(\\d+)\\s*(?:tr|triệu)\\s*(\\d)" + END,
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern POWER = Pattern.compile(NUM + "\\s*" + POWER_UNIT + END,
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern ENERGY = Pattern.compile(NUM + "\\s*" + ENERGY_UNIT + END,
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern PERCENT = Pattern.compile(NUM + "\\s*%");
    /** Khoảng "80-100 triệu", "30 – 50%": số đầu mang đơn vị của số sau */
    private static final Pattern RANGE = Pattern.compile(NUM + RANGE_SEP + NUM + "\\s*("
            + MONEY_UNIT.substring(1, MONEY_UNIT.length() - 1) + "|" + POWER_UNIT.substring(1, POWER_UNIT.length() - 1)
            + "|" + ENERGY_UNIT.substring(1, ENERGY_UNIT.length() - 1) + "|%)" + "(?![\\p{L}\\d])",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern WARRANTY_AFTER = Pattern.compile(
            "bảo hành[^.!?\\n\\d]{0,40}?(\\d+)\\s*(năm|tháng)" + END, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern WARRANTY_BEFORE = Pattern.compile(
            "(\\d+)\\s*(năm|tháng)\\s*bảo hành", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    /** Số viết bằng chữ trước đơn vị tiền: bị cấm trong văn phong (core/voice), coi là lỗi */
    private static final Pattern SPELLED_MONEY = Pattern.compile(
            "(?<![\\p{L}])(?:một|hai|ba|bốn|tư|năm|lăm|sáu|bảy|bẩy|tám|chín|mười|mươi|trăm)\\s+(?:tỷ|tỉ|triệu|nghìn|ngàn)"
                    + END, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    /** Số trơn trong tin của khách ("tiền điện 2000000", "2.000.000"): coi là tiền */
    private static final Pattern PLAIN_BIG_NUMBER = Pattern.compile(
            "(?<![\\d.,])(\\d{1,3}(?:[.,]\\d{3})+|\\d{4,})(?![\\d.,])");

    /** Sai lệch tương đối cho phép khi làm tròn (90.800.000 ↔ "khoảng 91 triệu") */
    private static final double TOLERANCE = 0.01;

    private NumberChecker() {
    }

    public static Result check(String reply, Collection<String> sourceTexts, Collection<Quantity> extraSources) {
        List<Quantity> sources = new ArrayList<>(extraSources);
        for (String s : sourceTexts) {
            sources.addAll(extract(s));
            sources.addAll(plainNumbers(s));
        }
        List<Quantity> unverified = new ArrayList<>();
        for (Quantity q : extract(reply)) {
            if (sources.stream().noneMatch(s -> matches(q, s))) {
                unverified.add(q);
            }
        }
        List<String> spelled = new ArrayList<>();
        Matcher m = SPELLED_MONEY.matcher(reply == null ? "" : reply);
        while (m.find()) {
            spelled.add(m.group());
        }
        return new Result(List.copyOf(unverified), List.copyOf(spelled));
    }

    /** Các số rủi ro trong một đoạn văn bản. */
    public static List<Quantity> extract(String text) {
        List<Quantity> out = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return out;
        }
        Matcher m = MONEY_TR_SHORT.matcher(text);
        while (m.find()) {
            double v = Double.parseDouble(m.group(1)) + Double.parseDouble(m.group(2)) / 10.0;
            out.add(new Quantity(Kind.MONEY, v * 1_000_000, m.group()));
        }
        m = RANGE.matcher(text);
        while (m.find()) {
            Unit unit = unitOf(m.group(3));
            if (unit != null) {
                Double first = parseNumber(m.group(1), unit.multiplier() > 1);
                if (first != null) {
                    out.add(new Quantity(unit.kind(), first * unit.multiplier(), m.group()));
                }
            }
        }
        addMatches(out, MONEY.matcher(text));
        addMatches(out, POWER.matcher(text));
        addMatches(out, ENERGY.matcher(text));
        m = PERCENT.matcher(text);
        while (m.find()) {
            Double v = parseNumber(m.group(1), true);
            if (v != null) {
                out.add(new Quantity(Kind.PERCENT, v, m.group()));
            }
        }
        for (Pattern p : List.of(WARRANTY_AFTER, WARRANTY_BEFORE)) {
            m = p.matcher(text);
            while (m.find()) {
                double v = Double.parseDouble(m.group(1));
                boolean years = m.group(2).toLowerCase(Locale.ROOT).startsWith("n");
                out.add(new Quantity(Kind.WARRANTY, years ? v * 12 : v, m.group()));
            }
        }
        return out;
    }

    private static List<Quantity> plainNumbers(String text) {
        List<Quantity> out = new ArrayList<>();
        if (text == null) {
            return out;
        }
        Matcher m = PLAIN_BIG_NUMBER.matcher(text);
        while (m.find()) {
            Double v = parseNumber(m.group(1), false);
            if (v != null) {
                out.add(new Quantity(Kind.MONEY, v, m.group()));
            }
        }
        return out;
    }

    private static void addMatches(List<Quantity> out, Matcher m) {
        while (m.find()) {
            Unit unit = unitOf(m.group(2));
            if (unit == null) {
                continue;
            }
            Double v = parseNumber(m.group(1), unit.multiplier() > 1);
            if (v != null) {
                out.add(new Quantity(unit.kind(), v * unit.multiplier(), m.group()));
            }
        }
    }

    private static boolean matches(Quantity q, Quantity source) {
        if (q.kind() != source.kind()) {
            return false;
        }
        if (q.kind() == Kind.PERCENT || q.kind() == Kind.WARRANTY) {
            return Math.abs(q.value() - source.value()) < 1e-6;
        }
        double base = Math.max(Math.abs(source.value()), 1e-9);
        return Math.abs(q.value() - source.value()) / base <= TOLERANCE;
    }

    private record Unit(Kind kind, double multiplier) {
    }

    private static Unit unitOf(String raw) {
        String u = raw.toLowerCase(Locale.ROOT).strip();
        return switch (u) {
            case "tỷ", "tỉ", "ty" -> new Unit(Kind.MONEY, 1_000_000_000d);
            case "triệu", "trieu", "tr" -> new Unit(Kind.MONEY, 1_000_000d);
            case "nghìn", "ngàn", "nghin", "ngan", "k" -> new Unit(Kind.MONEY, 1_000d);
            case "vnđ", "vnd", "đồng", "dong", "đ" -> new Unit(Kind.MONEY, 1d);
            case "kwp", "kw" -> new Unit(Kind.POWER, 1_000d);
            case "wp", "w" -> new Unit(Kind.POWER, 1d);
            case "kwh", "số điện", "số" -> new Unit(Kind.ENERGY, 1_000d);
            case "wh" -> new Unit(Kind.ENERGY, 1d);
            case "%" -> new Unit(Kind.PERCENT, 1d);
            default -> null;
        };
    }

    /**
     * Đọc số viết kiểu Việt Nam. Dấu chấm / phẩy là phân cách nghìn khi đứng trước đúng 3 chữ số và không có đơn vị
     * nhân ("90.800.000đ"); ngược lại là dấu thập phân ("90,8 triệu", "5.5 kWp").
     */
    static Double parseNumber(String raw, boolean hasMultiplier) {
        String s = raw.strip();
        try {
            boolean dot = s.indexOf('.') >= 0;
            boolean comma = s.indexOf(',') >= 0;
            if (dot && comma) {
                if (s.lastIndexOf(',') > s.lastIndexOf('.')) {
                    s = s.replace(".", "").replace(',', '.');
                } else {
                    s = s.replace(",", "");
                }
            } else if (comma) {
                s = !hasMultiplier && s.matches("\\d{1,3}(,\\d{3})+") ? s.replace(",", "") : s.replace(',', '.');
            } else if (dot) {
                s = !hasMultiplier && s.matches("\\d{1,3}(\\.\\d{3})+") ? s.replace(".", "") : s;
            }
            if (s.chars().filter(ch -> ch == '.').count() > 1) {
                return null;
            }
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
