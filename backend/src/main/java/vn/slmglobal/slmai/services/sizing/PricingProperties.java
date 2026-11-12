package vn.slmglobal.slmai.services.sizing;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Số liệu và công thức chọn gói (doanh nghiệp chốt 2026-10-26, đổi 2026-11-12; kế hoạch mục 60.2, 60.3), khối
 * {@code app.pricing:} trong application.yml.
 *
 * @param pricePerKwh          giá phẳng quy đổi hoá đơn → kWh (đồng/kWh), không dùng biểu giá bậc thang
 * @param nightShare           tỷ lệ điện dùng ban đêm theo ty_le_ngay_dem (NGAY_NHIEU, CAN_BANG, DEM_NHIEU); chỉ
 *                             dùng để ước tính tiền tiết kiệm và chọn loại hệ, không dùng để chọn công suất / pin
 * @param batteryDepth         độ xả sâu của pin lưu trữ (ước tính điện pin trữ được khi tính tiết kiệm)
 * @param coverage             đề xuất đầu: hệ làm ra phần này của điện nhà dùng (1 = 100%)
 * @param cheaperCoverage      khách chê đắt / ngân sách thấp: hệ làm ra phần này của điện nhà dùng (0,8 = 80%)
 * @param batteryPerMillion    pin cần (kWh) cho mỗi triệu tiền điện theo ty_le_ngay_dem; 0 là không cần pin
 * @param roofAreaPerKwp       m² mái cho 1 kWp: kWp tối đa = diện tích mái / roofAreaPerKwp
 * @param largeSystemKwp       công suất cần lớn hơn mức này: ghi chú để nhân viên khảo sát, báo giá riêng
 * @param productLinkDomains   tên miền website gửi khách theo miền (BAC, NAM); link {@code https://<tên miền>/products/<id gói>}
 * @param quoteConditions      câu điều kiện báo giá đi kèm mọi lần báo giá (mục 4.2.6, 58.2)
 * @param flatRoofNote         câu cho mái bằng: gia_khung_sat đang bằng 0 nên không đưa con số
 */
@ConfigurationProperties(prefix = "app.pricing")
public record PricingProperties(
        long pricePerKwh,
        Map<String, Double> nightShare,
        double batteryDepth,
        double coverage,
        double cheaperCoverage,
        Map<String, Double> batteryPerMillion,
        double roofAreaPerKwp,
        double largeSystemKwp,
        Map<String, String> productLinkDomains,
        String quoteConditions,
        String flatRoofNote
) {

    public PricingProperties {
        if (pricePerKwh <= 0) {
            pricePerKwh = 3500;
        }
        if (nightShare == null || nightShare.isEmpty()) {
            nightShare = Map.of("NGAY_NHIEU", 0.3, "CAN_BANG", 0.5, "DEM_NHIEU", 0.7);
        }
        if (batteryDepth <= 0 || batteryDepth > 1) {
            batteryDepth = 0.9;
        }
        if (coverage <= 0) {
            coverage = 1;
        }
        if (cheaperCoverage <= 0) {
            cheaperCoverage = 0.8;
        }
        if (batteryPerMillion == null || batteryPerMillion.isEmpty()) {
            batteryPerMillion = Map.of("NGAY_NHIEU", 0.0, "CAN_BANG", 2.5, "DEM_NHIEU", 4.0);
        }
        if (roofAreaPerKwp <= 0) {
            roofAreaPerKwp = 5;
        }
        if (largeSystemKwp <= 0) {
            largeSystemKwp = 30;
        }
        if (productLinkDomains == null) {
            productLinkDomains = Map.of();
        }
    }

    /** Tỷ lệ dùng đêm nhỏ nhất và lớn nhất trong cấu hình (khi khách chưa nói) */
    public double minNightShare() {
        return nightShare.values().stream().mapToDouble(Double::doubleValue).min().orElse(0.3);
    }

    public double maxNightShare() {
        return nightShare.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.7);
    }

    /** Pin mỗi triệu tiền điện nhỏ nhất và lớn nhất (khi khách chưa nói dùng ngày / đêm) */
    public double minBatteryPerMillion() {
        return batteryPerMillion.values().stream().mapToDouble(Double::doubleValue).min().orElse(0);
    }

    public double maxBatteryPerMillion() {
        return batteryPerMillion.values().stream().mapToDouble(Double::doubleValue).max().orElse(4);
    }
}
