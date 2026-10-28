package vn.slmglobal.slmai.services.sizing;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Số liệu và công thức chọn gói tạm (doanh nghiệp chốt 2026-10-26, kế hoạch mục 60.2, 60.3), khối
 * {@code app.pricing:} trong application.yml.
 *
 * @param pricePerKwh          giá phẳng quy đổi hoá đơn → kWh (đồng/kWh), không dùng biểu giá bậc thang
 * @param nightShare           tỷ lệ điện dùng ban đêm theo ty_le_ngay_dem (NGAY_NHIEU, CAN_BANG, DEM_NHIEU)
 * @param batteryDepth         độ xả sâu của pin lưu trữ: pin cần = điện dùng đêm / batteryDepth
 * @param capacityTolerance    chọn gói có kWp ≥ capacityTolerance × công suất cần
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
        double capacityTolerance,
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
        if (capacityTolerance <= 0 || capacityTolerance > 1) {
            capacityTolerance = 0.9;
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
}
