package vn.slmglobal.slmai.services.sizing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Công thức chọn gói tạm (doanh nghiệp chốt 2026-10-26, mục 60.3). Tất định, không gọi AI:
 * <ol>
 *   <li>Điện dùng mỗi ngày {@code E = hoá đơn tháng / giá điện / 30} (giá phẳng 3.500 đ/kWh).</li>
 *   <li>Tỷ lệ dùng đêm theo ty_le_ngay_dem: ban ngày nhiều 30%, cân bằng 50%, buổi tối nhiều 70%.</li>
 *   <li>Pin cần {@code = E × tỷ lệ đêm / 0,9}.</li>
 *   <li>Công suất cần {@code kWp = E / (PV out năm của tỉnh / 365)}; biết diện tích mái thì tối đa {@code S / 5}.</li>
 * </ol>
 * Chưa biết tỉnh thì lấy khoảng theo PV out thấp nhất / cao nhất của cả nước; chưa biết ngày / đêm thì lấy khoảng
 * theo tỷ lệ đêm thấp nhất / cao nhất.
 */
@Service
@RequiredArgsConstructor
public class SizingService {

    private final PricingProperties properties;

    /** Khoảng giá trị; {@code min == max} khi đã đủ thông tin */
    public record Range(double min, double max) {

        public static Range of(double v) {
            return new Range(v, v);
        }

        public boolean exact() {
            return Math.abs(max - min) < 1e-9;
        }
    }

    /**
     * @param dailyKwh      điện dùng mỗi ngày
     * @param monthlyKwh    điện dùng mỗi tháng (30 ngày)
     * @param nightShare    tỷ lệ dùng đêm; khoảng khi chưa biết
     * @param batteryKwh    dung lượng pin cần
     * @param requiredKwp   công suất cần
     * @param maxKwpByRoof  công suất tối đa theo diện tích mái; null khi chưa biết diện tích
     * @param pvoutDaily    PV out trung bình mỗi ngày (kWh/kWp) dùng để tính
     */
    public record Sizing(double dailyKwh, double monthlyKwh, Range nightShare, Range batteryKwh, Range requiredKwp,
                         Double maxKwpByRoof, Range pvoutDaily) {
    }

    /**
     * @param monthlyBillVnd hoá đơn tiền điện mỗi tháng (đồng), > 0
     * @param dayNight       NGAY_NHIEU / CAN_BANG / DEM_NHIEU; null khi chưa biết
     * @param annualPvout    PV out cả năm của tỉnh khách; null khi chưa biết tỉnh
     * @param nationalPvout  PV out cả năm thấp nhất / cao nhất trong bảng (khi chưa biết tỉnh)
     * @param roofAreaM2     diện tích mái; null khi chưa biết
     */
    public Sizing size(long monthlyBillVnd, String dayNight, Double annualPvout, Range nationalPvout,
                       Double roofAreaM2) {
        double daily = monthlyBillVnd / (double) properties.pricePerKwh() / 30.0;
        Double share = dayNight == null ? null : properties.nightShare().get(dayNight);
        Range night = share != null ? Range.of(share)
                : new Range(properties.minNightShare(), properties.maxNightShare());
        Range battery = new Range(daily * night.min() / properties.batteryDepth(),
                daily * night.max() / properties.batteryDepth());
        Range pvDaily = annualPvout != null ? Range.of(annualPvout / 365.0)
                : new Range(nationalPvout.min() / 365.0, nationalPvout.max() / 365.0);
        // PV out thấp cần nhiều kWp hơn
        Range kwp = new Range(daily / pvDaily.max(), daily / pvDaily.min());
        Double roofCap = roofAreaM2 == null || roofAreaM2 <= 0 ? null : roofAreaM2 / properties.roofAreaPerKwp();
        return new Sizing(daily, daily * 30, night, battery, kwp, roofCap, pvDaily);
    }

    /** Sản lượng mỗi tháng của hệ {@code kwp} theo PV out tháng của tỉnh */
    public static double[] monthlyProduction(double kwp, double[] pvoutMonthly) {
        return Arrays.stream(pvoutMonthly).map(m -> kwp * m).toArray();
    }

    /**
     * Điện mặt trời tự dùng mỗi ngày (kWh), ước tính để tính tiền tiết kiệm: ban ngày dùng trực tiếp, phần dư sạc
     * pin (theo độ xả sâu) để dùng buổi tối; hệ On-Grid không có pin nên chỉ tính phần dùng ban ngày. Không tính
     * tiền bán điện dư lên lưới.
     *
     * @param solarDaily sản lượng mặt trời trung bình mỗi ngày của tháng đang tính
     */
    public double selfConsumedDaily(double solarDaily, double storageKwh, double dailyKwh, double nightShare) {
        double dayUse = dailyKwh * (1 - nightShare);
        double nightUse = dailyKwh * nightShare;
        double direct = Math.min(solarDaily, dayUse);
        double stored = Math.min(Math.max(0, solarDaily - direct),
                Math.min(storageKwh * properties.batteryDepth(), nightUse));
        return direct + stored;
    }

    /** Tiền điện tiết kiệm cả năm (đồng), cộng theo từng tháng bằng PV out tháng của tỉnh */
    public double annualSavings(double kwp, double storageKwh, double[] pvoutMonthly, double dailyKwh,
                                double nightShare) {
        double daysPerMonth = 365.0 / 12;
        double kwh = 0;
        for (double pv : pvoutMonthly) {
            kwh += selfConsumedDaily(kwp * pv / daysPerMonth, storageKwh, dailyKwh, nightShare) * daysPerMonth;
        }
        return kwh * properties.pricePerKwh();
    }
}
