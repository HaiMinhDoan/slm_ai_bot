package vn.slmglobal.slmai.services.sizing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Viết số theo kiểu Việt Nam cho kết quả tool ("144,2 triệu đồng", "8,75 kWp", "90.800.000 đ"). AI chép nguyên các
 * chuỗi này vào câu trả lời, và Policy đối chiếu con số với chính các chuỗi đó (mục 33.1).
 */
public final class VnFormat {

    private static final DecimalFormatSymbols VN = DecimalFormatSymbols.getInstance(Locale.forLanguageTag("vi-VN"));

    private VnFormat() {
    }

    /** 144200000 → "144,2 triệu đồng"; 850000 → "850 nghìn đồng"; 1250000000 → "1,25 tỷ đồng" */
    public static String money(double vnd) {
        if (Math.abs(vnd) >= 1_000_000_000) {
            return decimal(vnd / 1_000_000_000, 2) + " tỷ đồng";
        }
        if (Math.abs(vnd) >= 1_000_000) {
            return decimal(vnd / 1_000_000, 2) + " triệu đồng";
        }
        return decimal(vnd / 1_000, 0) + " nghìn đồng";
    }

    /** Làm tròn tới 100 nghìn (ước tính tiền tiết kiệm): 1428000 → "1,4 triệu đồng" */
    public static String moneyApprox(double vnd) {
        if (Math.abs(vnd) >= 1_000_000) {
            return decimal(vnd / 1_000_000, 1) + " triệu đồng";
        }
        return decimal(Math.round(vnd / 10_000) * 10.0, 0) + " nghìn đồng";
    }

    /** 90800000 → "90.800.000 đ" */
    public static String moneyExact(double vnd) {
        DecimalFormat f = new DecimalFormat("#,##0", VN);
        return f.format(Math.round(vnd)) + " đ";
    }

    public static String kwp(double v) {
        return decimal(v, 2) + " kWp";
    }

    public static String kwh(double v) {
        return decimal(v, 2) + " kWh";
    }

    /** Điện năng làm tròn số nguyên: 571.4 → "571 kWh" */
    public static String kwhRounded(double v) {
        return decimal(v, v < 10 ? 1 : 0) + " kWh";
    }

    public static String percent(double share) {
        return decimal(share * 100, 0) + "%";
    }

    /** Bảo hành: 144 tháng → "12 năm"; 18 → "18 tháng" */
    public static String warranty(int months) {
        return months % 12 == 0 ? (months / 12) + " năm" : months + " tháng";
    }

    /**
     * Số thập phân dấu phẩy, bỏ số 0 thừa: 8.750 → "8,75", 16.0 → "16". Không phân cách nghìn: "1.426 kWh" sẽ bị
     * bộ kiểm tra con số đọc là 1,426 kWh (dấu chấm trước đơn vị nhân là dấu thập phân).
     */
    public static String decimal(double v, int maxFractionDigits) {
        BigDecimal d = BigDecimal.valueOf(v).setScale(maxFractionDigits, RoundingMode.HALF_UP).stripTrailingZeros();
        if (d.scale() < 0) {
            d = d.setScale(0, RoundingMode.UNNECESSARY);
        }
        return d.toPlainString().replace('.', ',');
    }
}
