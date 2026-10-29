package vn.slmglobal.slmai.services.sizing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.slmglobal.slmai.services.catalog.CatalogRepository;
import vn.slmglobal.slmai.services.catalog.CatalogRepository.CatalogPackage;
import vn.slmglobal.slmai.services.policy.NumberChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Công thức chọn gói tạm (mục 60.3) trên bộ gói giống slm_v2 và bảng PV out EVN 2026. */
class PackageAdvisorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CatalogRepository catalog = mock(CatalogRepository.class);
    private final PvoutService pvout = mock(PvoutService.class);
    private PackageAdvisor advisor;

    @BeforeEach
    void setUp() {
        PricingProperties pricing = new PricingProperties(3500, null, 0, 0, 0, 0,
                Map.of("BAC", "slmsolar.vn", "NAM", "slmsolar.com.vn"),
                "Đây là giá niêm yết của gói trọn gói; giá cuối cùng nhân viên báo sau khi khảo sát thực tế.",
                "Mái bằng cần thêm phần khung, chi phí khung nhân viên báo sau khi khảo sát.");
        advisor = new PackageAdvisor(catalog, new ProvinceDirectory(objectMapper), pvout, new SizingService(pricing),
                pricing, objectMapper);
        when(catalog.recommendablePackages("HN")).thenReturn(sorted(List.of(
                p(158, "HN", "Hệ On-Grid 5 kWp 1 pha", "ON_GRID", 1, 5.0, 0, 53_600_000, false),
                p(160, "HN", "Hệ On-Grid 9 kWp 1 pha", "ON_GRID", 1, 8.75, 0, 84_200_000, false),
                p(162, "HN", "Hệ On-Grid 11 kWp 1 pha", "ON_GRID", 1, 10.625, 0, 97_400_000, false),
                p(152, "HN", "Hệ On-Grid 11 kWp 3 pha", "ON_GRID", 3, 10.625, 0, 100_400_000, false),
                p(98, "HN", "Hệ Hy-Brid 5 kWp 1 pha - Lưu trữ 5.12 kWh", "HYBRID", 1, 5.0, 5.12, 90_800_000, true),
                p(99, "HN", "Hệ Hy-Brid 5 kWp 1 pha - Lưu trữ 10.24 kWh", "HYBRID", 1, 5.0, 10.24, 113_800_000, false),
                p(101, "HN", "Hệ Hy-Brid 9 kWp 1 pha - Lưu trữ 5.12 kWh", "HYBRID", 1, 8.75, 5.12, 113_200_000, true),
                p(104, "HN", "Hệ Hy-Brid 9 kWp 1 pha - Lưu trữ 10.24 kWh", "HYBRID", 1, 8.75, 10.24, 136_200_000, false),
                p(105, "HN", "Hệ Hy-Brid 9 kWp 1 pha - Lưu trữ 16 kWh", "HYBRID", 1, 8.75, 16, 144_200_000, false),
                p(111, "HN", "Hệ Hy-Brid 11 kWp 1 pha - Lưu trữ 16 kWh", "HYBRID", 1, 10.625, 16, 167_900_000, false),
                p(164, "HN", "Hệ cao cấp Hy-Brid Huawei 6.4 kWp 1 pha - Lưu trữ 7 kWh", "HYBRID", 1, 6.4, 7,
                        186_700_000, false),
                p(136, "HN", "Hệ Hy-Brid 11 kWp 3 pha áp thấp - Lưu trữ 5.12kWh", "HYBRID", 3, 10.625, 5.12,
                        156_700_000, false))));
        when(catalog.recommendablePackages("HCM")).thenReturn(sorted(List.of(
                p(159, "HCM", "Hệ On-Grid 5 kWp 1 pha", "ON_GRID", 1, 5.0, 0, 53_600_000, false),
                p(97, "HCM", "Hệ Hy-Brid 5 kWp 1 pha - Lưu trữ 5.12 kWh", "HYBRID", 1, 5.0, 5.12, 90_800_000, true))));
        when(catalog.domains("HN")).thenReturn(List.of("slmsolar.com", "slmsolar.vn"));
        when(catalog.domains("HCM")).thenReturn(List.of("slmsolar.com.vn"));
        when(catalog.hasRecommendablePackages()).thenReturn(true);
        when(pvout.current()).thenReturn(Optional.of(new PvoutRepository.Table(2026, "EVN 364/EVN-KDMBĐ", Map.of(
                "HA_NOI", new double[]{44, 33, 47, 63, 100, 105, 110, 104, 96, 85, 73, 52},
                "HUNG_YEN", new double[]{45, 31, 45, 62, 99, 106, 109, 103, 93, 84, 72, 52},
                "HO_CHI_MINH", new double[]{119, 110, 132, 126, 133, 120, 115, 123, 106, 116, 112, 114},
                "LAM_DONG", new double[]{139, 139, 161, 152, 142, 129, 118, 130, 115, 121, 117, 120}))));
    }

    @Test
    void planExampleHanoiTwoMillionEveningHeavy() {
        // Mục 60.3 bước 6: E ≈ 19 kWh/ngày, pin ≈ 14,8 kWh → 16 kWh, kWp ≈ 7,6 → gói 105 (144,2 triệu)
        PackageAdvisor.Advice a = advisor.advise(new PackageAdvisor.AdviceRequest("Hà Nội", 1, 2_000_000L,
                "DEM_NHIEU", "TON", null, null, null));
        JsonNode r = a.result();
        assertThat(r.path("che_do").asText()).isEqualTo("DE_XUAT");
        assertThat(r.path("goi_de_xuat")).hasSize(1);
        JsonNode best = r.path("goi_de_xuat").get(0);
        assertThat(best.path("goi_id").asInt()).isEqualTo(105);
        assertThat(best.path("gia").asText()).isEqualTo("144,2 triệu đồng");
        assertThat(best.path("gia_day_du").asText()).isEqualTo("144.200.000 đ");
        assertThat(best.path("cong_suat").asText()).isEqualTo("8,75 kWp");
        assertThat(best.path("luu_tru").asText()).isEqualTo("16 kWh");
        assertThat(best.path("link").asText()).isEqualTo("https://slmsolar.vn/products/105");
        assertThat(best.path("san_luong_uoc_tinh").path("trung_binh").asText()).isEqualTo("khoảng 665 kWh/tháng");
        assertThat(best.path("san_luong_uoc_tinh").path("thang_thap_nhat").asText()).contains("tháng 2");
        assertThat(best.path("tiet_kiem_uoc_tinh").path("hoan_von").asText()).startsWith("khoảng ");
        assertThat(r.path("uoc_tinh").path("dien_dung_ngay").asText()).isEqualTo("khoảng 19 kWh/ngày");
        assertThat(r.path("uoc_tinh").path("cong_suat_can").asText()).isEqualTo("khoảng 7,62 kWp");
        assertThat(r.path("uoc_tinh").path("pin_can_neu_dung_hybrid").asText()).isEqualTo("khoảng 14,81 kWh");
        assertThat(r.path("con_thieu")).isEmpty();
        assertThat(a.quotedPackageIds()).containsExactly(105);
        assertThat(a.cheapestQuotedVnd()).isEqualTo(144_200_000L);
    }

    @Test
    void dayHeavyGetsOnGridAndSmallBatteryHybrid() {
        PackageAdvisor.Advice a = advisor.advise(new PackageAdvisor.AdviceRequest("Hà Nội", 1, 2_000_000L,
                "NGAY_NHIEU", null, null, null, null));
        List<Integer> ids = new ArrayList<>();
        a.result().path("goi_de_xuat").forEach(o -> ids.add(o.path("goi_id").asInt()));
        // On-Grid 8,75 kWp; Hybrid 8,75 kWp với pin ≥ 6,35 kWh (10,24 kWh)
        assertThat(ids).containsExactly(160, 104);
        assertThat(a.cheapestQuotedVnd()).isEqualTo(84_200_000L);
        assertThat(a.result().path("khoang_gia").path("tu").asText()).isEqualTo("84,2 triệu đồng");
        assertThat(a.result().path("con_thieu").toString()).contains("loai_mai");
    }

    @Test
    void withoutBillGivesStartingPricesForTheCustomersRegion() {
        PackageAdvisor.Advice a = advisor.advise(new PackageAdvisor.AdviceRequest("Bình Dương", null, null, null,
                null, null, null, null));
        JsonNode r = a.result();
        assertThat(r.path("che_do").asText()).isEqualTo("GIA_KHOI_DIEM");
        assertThat(r.path("khu_vuc").path("tinh_cu").asText()).isEqualTo("Bình Dương (nay thuộc TP Hồ Chí Minh)");
        JsonNode first = r.path("gia_khoi_diem").get(0);
        assertThat(first.path("gia_tu").asText()).isEqualTo("53,6 triệu đồng");
        assertThat(first.path("goi_re_nhat").path("link").asText()).isEqualTo("https://slmsolar.com.vn/products/159");
        assertThat(r.path("gia_khoi_diem").get(1).path("goi_re_nhat").path("goi_id").asInt()).isEqualTo(97);
        assertThat(r.path("con_thieu").toString()).contains("hoa_don_thang");
        assertThat(a.quotedPackageIds()).isEmpty();
        assertThat(a.cheapestQuotedVnd()).isNull();
    }

    @Test
    void unknownProvinceGivesRangeWithoutLinks() {
        PackageAdvisor.Advice a = advisor.advise(new PackageAdvisor.AdviceRequest(null, null, 2_000_000L, null,
                null, null, null, null));
        JsonNode r = a.result();
        assertThat(r.path("uoc_tinh").path("cong_suat_can").asText()).contains(" đến ");
        assertThat(r.path("goi_de_xuat").size()).isGreaterThanOrEqualTo(2);
        r.path("goi_de_xuat").forEach(o -> assertThat(o.has("link")).isFalse());
        assertThat(r.toString()).doesNotContain("https://");
        assertThat(r.path("con_thieu").toString()).contains("khu_vuc");
    }

    @Test
    void roofAreaCapsCapacity() {
        // 30 m² → tối đa 6 kWp: không gói 8,75 kWp nào vừa mái
        PackageAdvisor.Advice a = advisor.advise(new PackageAdvisor.AdviceRequest("Hà Nội", 1, 2_000_000L,
                "DEM_NHIEU", "TON", 30.0, null, null));
        JsonNode best = a.result().path("goi_de_xuat").get(0);
        assertThat(best.path("cong_suat").asText()).isEqualTo("5 kWp");
        assertThat(best.path("luu_y").asText()).contains("Mái chỉ đủ");
    }

    @Test
    void flatRoofAddsFrameNote() {
        JsonNode r = advisor.advise(new PackageAdvisor.AdviceRequest("Hà Nội", 1, 1_500_000L, "CAN_BANG", "BANG",
                null, null, null)).result();
        assertThat(r.path("ghi_chu").toString()).contains("Mái bằng cần thêm phần khung");
    }

    @Test
    void replyBuiltFromToolStringsPassesNumberCheck() throws Exception {
        PackageAdvisor.Advice a = advisor.advise(new PackageAdvisor.AdviceRequest("Hà Nội", 1, 2_000_000L,
                "DEM_NHIEU", "TON", null, null, null));
        JsonNode best = a.result().path("goi_de_xuat").get(0);
        String reply = "Dạ nhà mình hợp hệ Hybrid " + best.path("cong_suat").asText() + " có pin "
                + best.path("luu_tru").asText() + ", giá " + best.path("gia").asText() + " (tức "
                + best.path("gia_day_du").asText() + "), sản lượng "
                + best.path("san_luong_uoc_tinh").path("trung_binh").asText() + ", tiết kiệm "
                + best.path("tiet_kiem_uoc_tinh").path("moi_thang_trung_binh").asText() + " mỗi tháng ạ.";
        NumberChecker.Result check = NumberChecker.check(reply, List.of(objectMapper.writeValueAsString(a.result())),
                List.of());
        assertThat(check.unverified()).isEmpty();
        assertThat(check.passed()).isTrue();

        NumberChecker.Result wrong = NumberChecker.check("Giá chỉ 120 triệu thôi ạ",
                List.of(objectMapper.writeValueAsString(a.result())), List.of());
        assertThat(wrong.passed()).isFalse();
    }

    @Test
    void detailMapsPackageToTheCustomersRegion() {
        CatalogPackage hn = p(158, "HN", "Hệ On-Grid 5 kWp 1 pha", "ON_GRID", 1, 5.0, 0, 53_600_000, false);
        CatalogPackage hcm = p(159, "HCM", "Hệ On-Grid 5 kWp 1 pha", "ON_GRID", 1, 5.0, 0, 53_600_000, false);
        when(catalog.findPackage(158)).thenReturn(Optional.of(hn));
        when(catalog.findTwin(hn, "HCM")).thenReturn(Optional.of(hcm));
        when(catalog.visibleItems(anyInt())).thenReturn(List.of(
                new CatalogRepository.CatalogItem("Tấm pin mặt trời JA 625 Wp", "TAM_PIN", "JA Solar", 8, "Tấm", 144)));
        JsonNode r = advisor.packageDetail(158, "Sài Gòn");
        assertThat(r.path("goi_id").asInt()).isEqualTo(159);
        assertThat(r.path("link").asText()).isEqualTo("https://slmsolar.com.vn/products/159");
        assertThat(r.path("thanh_phan").get(0).path("bao_hanh").asText()).isEqualTo("bảo hành 12 năm");
        assertThat(r.path("ghi_chu").toString()).contains("dùng gói 159");
    }

    private static List<CatalogPackage> sorted(List<CatalogPackage> list) {
        return list.stream().sorted((x, y) -> Double.compare(x.totalPrice(), y.totalPrice())).toList();
    }

    private static CatalogPackage p(int id, String branch, String name, String type, int phases, double kwp,
                                    double storage, double price, boolean bestSeller) {
        return new CatalogPackage(id, branch, 6, "JA Solar - Solis - Dyness", name, type, phases, kwp,
                (int) Math.round(kwp * 1000 / 625), 625.0, kwp, storage, price, 0, bestSeller, true, true, List.of(),
                "JA Solar", "Solis", "HYBRID".equals(type) ? "Dyness" : null, false, false, List.of(), null);
    }
}
