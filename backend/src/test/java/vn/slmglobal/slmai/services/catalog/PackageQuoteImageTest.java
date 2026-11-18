package vn.slmglobal.slmai.services.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.web.client.RestClient;
import vn.slmglobal.slmai.adapters.gotenberg.GotenbergClient;
import vn.slmglobal.slmai.adapters.gotenberg.GotenbergProperties;
import vn.slmglobal.slmai.services.catalog.CatalogRepository.CatalogPackage;
import vn.slmglobal.slmai.services.catalog.PackageQuoteImageService.Line;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import vn.slmglobal.slmai.common.storage.StorageService;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Ảnh báo giá tự tạo: trang HTML dựng đúng theo mẫu từ số liệu gói (số liệu lấy từ catalog thật ngày 2026-11-18).
 * Đặt GOTENBERG_TEST_URL (vd http://host.docker.internal:3123) để chụp thật thành target/quote-*.jpg mà xem.
 */
class PackageQuoteImageTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final String MINIO = "http://171.244.142.43:9000/slm/";

    @Test
    void onGridPackageUsesRedTemplateWithPanelInverterAndHiddenParts() {
        String html = QuoteImageTemplate.html(PackageQuoteImageService.page(onGrid19(), new double[]{1200, 1400},
                onGrid19Lines(), (url, prefix) -> url == null ? null : prefix + ".png"));

        assertThat(html).contains("theme-red", "header-red.png", "footer-red.png")
                .contains("HỆ ĐIỆN MẶT TRỜI<br>BÁM TẢI <span class=\"hi\">19 kWp</span>")
                .contains("Sản lượng trung bình 1.200-1.400 kWh/tháng")
                .contains("<div class=\"n\">3</div>")
                .contains("Tấm pin JA Solar", "Công suất: <b>625 Wp</b>", "Hiệu suất: <b>23,1%</b>")
                // Công suất biến tần theo tên vật tư (thông số slm_v2 ghi sai 15 kW, 1 pha)
                .contains("Biến tần Auxsol</span> <span class=\"light\">| 20 kW</span>",
                        "Hệ Inverter: <b>On-Grid | 3 pha</b>")
                .contains("<b>12</b><span>NĂM</span>", "<b>5</b><span>NĂM</span>")
                .contains("2382x1134<br>x30mm", "<span class=\"big\">33,1</span>")
                .contains("2.600.000<sup>đ</sup>", "154.300.000<sup>đ</sup>")
                .contains("Tấm quang năng 625 Wp", "30 tấm", "Biến tần On-Grid 20 kW - 3 pha", "01 bộ")
                .contains("Hệ ray nhôm đỡ tấm quang năng", "Bộ tủ điện Năng lượng mặt trời")
                .doesNotContain("Kẹp biên", "Dây điện DC");
        assertThat(html.indexOf("Hệ ray nhôm")).isLessThan(html.indexOf("Bộ tủ điện"));
    }

    @Test
    void hybridPackageUsesGreenTemplateWithBatteryAndVoltage() {
        String html = QuoteImageTemplate.html(PackageQuoteImageService.page(hybrid16(), new double[]{1200, 1500},
                hybrid16Lines(), (url, prefix) -> url == null ? null : prefix + ".png"));

        assertThat(html).contains("theme-green")
                .contains("HỆ HY-BRID <span class=\"hi\">16 kWp</span><br>LƯU TRỮ <span class=\"hi\">15,36 kWh - ÁP CAO"
                        + "</span>")
                // Dòng tiêu đề dài: chữ nhỏ lại để không xuống dòng
                .contains("<h1 class=\"long\">")
                .contains("Pin Dyness", "| 5,12 kWh", "Lắp đặt: <b>Xếp chồng không dây</b>",
                        "Số lượng: <b>03 bộ</b>")
                .contains("Biến tần Hy-Brid 10 kW - 3 pha", "Pin lưu trữ 5,12 kWh")
                .contains("256.100.000")
                // Phụ kiện pin để ẩn với khách thì không lên thẻ
                .doesNotContain("Bộ điều khiển");
    }

    @Test
    void visibleLabourBecomesOneTurnkeyRow() {
        CatalogPackage p = pkg(184, "Hệ On-Grid 29.4 kWp 3 pha", "ON_GRID", 3, 29.375, 0, 252_700_000d);
        List<Line> lines = List.of(
                line("Tấm pin mặt trời JA 625 Wp", "TAM_PIN", "JA Solar", 47, 144, 2_600_000d, true, PANEL_SPECS),
                line("Inverter Auxsol On-Grid 3 pha 30 kW", "BIEN_TAN", "Auxsol", 1, 60, 37_000_000d, true, "{}"),
                line("Nhân công lắp đặt, vận chuyển", "TRON_GOI_LAP_DAT", "SLM", 1, 0, 0d, true, "{}"),
                line("Nhân công lắp đặt, vận chuyển", "TRON_GOI_LAP_DAT", "SLM", 1, 0, 0d, true, "{}"));
        String html = QuoteImageTemplate.html(PackageQuoteImageService.page(p, null, lines, (u, x) -> null));

        assertThat(html).contains("BÁM TẢI <span class=\"hi\">29,4 kWp</span>", "47 tấm", "Trọn gói")
                .doesNotContain("Sản lượng trung bình");
        assertThat(html.split("Nhân công lắp đặt", -1)).hasSize(2);
        // Không tải được logo: in tên hãng
        assertThat(html).contains("<span class=\"brand-text\">JA Solar</span>");
    }

    @Test
    void premiumHuaweiPackageNamesTheBrandInTheTitle() {
        CatalogPackage p = new CatalogPackage(194, "HN", null, null, "Hệ cao cấp On-Grid Huawei 30 kWp 3 pha",
                "ON_GRID", 3, 29.375, 47, 625d, 30d, 0, 297_500_000d, 0, false, true, true, List.of(), "JA Solar",
                "Huawei", null, false, false, List.of(), null);
        String html = QuoteImageTemplate.html(PackageQuoteImageService.page(p, null, List.of(), (u, x) -> null));
        assertThat(html).contains("HỆ CAO CẤP HUAWEI<br>BÁM TẢI <span class=\"hi\">30 kWp</span>");
    }

    @Test
    void specsSkipPlaceholdersAndKeepUnits() {
        JsonNode specs = json("""
                {"cong_suat": {"unit": "kW", "value": null}, "hieu_suat": {"unit": "%", "value": 23.1},
                 "khoi_luong": {"unit": "kg", "value": "abc"}, "kich_thuoc": {"unit": "mm", "value": "600 × 500 × 230"},
                 "cach_lap_dat": {"unit": "", "value": "Treo tường"}}""");
        assertThat(PackageQuoteImageService.spec(specs, "cong_suat")).isEmpty();
        assertThat(PackageQuoteImageService.spec(specs, "hieu_suat")).contains("23,1%");
        assertThat(PackageQuoteImageService.spec(specs, "khoi_luong")).isEmpty();
        assertThat(PackageQuoteImageService.spec(specs, "cach_lap_dat")).contains("Treo tường");
        assertThat(PackageQuoteImageService.size(specs)).isEqualTo("600x500\nx230mm");
        assertThat(PackageQuoteImageService.size(json("{\"kich_thuoc\": {\"value\": \"4x10/4x16mm\"}}"))).isNull();
        assertThat(PackageQuoteImageService.thousands(154_300_000)).isEqualTo("154.300.000");
    }

    @Test
    void htmlEscapesCatalogText() {
        assertThat(QuoteImageTemplate.esc("Hệ <b>\"A\" & B</b>")).isEqualTo("Hệ &lt;b&gt;&quot;A&quot; &amp; B&lt;/b&gt;");
    }

    /** Ảnh đã dựng lưu MinIO theo mã băm nội dung: lần sau dùng lại, Gotenberg lỗi thì tạm không hứa gửi ảnh */
    @Test
    @SuppressWarnings("unchecked")
    void renderedImageIsStoredAndReusedAndFailurePausesRendering() throws Exception {
        CatalogRepository catalog = mock(CatalogRepository.class);
        NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);
        GotenbergClient gotenberg = mock(GotenbergClient.class);
        StorageService storage = mock(StorageService.class);
        when(catalog.findPackage(156)).thenReturn(Optional.of(onGrid19()));
        when(catalog.findPackage(1)).thenReturn(Optional.of(pkg(1, "Gói không giá", "ON_GRID", 1, 5, 0, null)));
        when(gotenberg.enabled()).thenReturn(true);
        when(storage.isAvailable()).thenReturn(true);
        when(storage.get(anyString())).thenThrow(new IllegalStateException("NoSuchKey"));
        when(gotenberg.screenshotJpeg(anyString(), anyMap(), anyInt())).thenReturn(new byte[]{1, 2, 3});
        PackageQuoteImageService service = new PackageQuoteImageService(catalog, jdbc, new CatalogImageService(null),
                gotenberg, storage, JSON);

        assertThat(service.canRender(156)).isTrue();
        assertThat(service.canRender(1)).isFalse();
        PackageQuoteImageService.QuoteImage first = service.image(156).orElseThrow();
        assertThat(first.objectKey()).matches("quotes/[0-9a-f]{2}/[0-9a-f]{64}\\.jpg");
        verify(storage).put(eq(first.objectKey()), any(), eq(3L), eq("image/jpeg"));

        // Cùng số liệu: đọc lại ảnh đã lưu, không chụp lại
        doAnswer(i -> new ByteArrayInputStream(new byte[]{1, 2, 3})).when(storage).get(first.objectKey());
        assertThat(service.image(156).orElseThrow().objectKey()).isEqualTo(first.objectKey());
        verify(gotenberg, times(1)).screenshotJpeg(anyString(), anyMap(), anyInt());

        // Giá đổi: mã băm đổi, dựng ảnh mới
        when(catalog.findPackage(156)).thenReturn(Optional.of(pkg(156, "Hệ On-Grid 19 kWp 3 pha", "ON_GRID", 3,
                18.75, 0, 149_000_000d)));
        doThrow(new IllegalStateException("down")).when(gotenberg).screenshotJpeg(anyString(), anyMap(), anyInt());
        assertThat(service.image(156)).isEmpty();
        assertThat(service.canRender(156)).isFalse();
        verify(storage, never()).put(eq("quotes/none.jpg"), any(), anyLong(), any());
    }

    /** Chụp thật ba gói mẫu qua Gotenberg, ảnh vật tư / logo lấy từ MinIO của slm_v2 (chỉ đọc) */
    @Test
    @EnabledIfEnvironmentVariable(named = "GOTENBERG_TEST_URL", matches = "https?://.+")
    void rendersRealImagesThroughGotenberg() throws Exception {
        GotenbergClient gotenberg = new GotenbergClient(RestClient.builder(),
                new GotenbergProperties(System.getenv("GOTENBERG_TEST_URL"), Duration.ofSeconds(60)));
        PackageQuoteImageService service = new PackageQuoteImageService(null, null,
                new CatalogImageService(null), gotenberg, null, JSON);
        Map<String, Object[]> cases = new LinkedHashMap<>();
        cases.put("ongrid-19", new Object[]{onGrid19(), new double[]{1200, 1400}, onGrid19Lines()});
        cases.put("hybrid-16", new Object[]{hybrid16(), new double[]{1200, 1500}, hybrid16Lines()});
        for (Map.Entry<String, Object[]> c : cases.entrySet()) {
            Map<String, byte[]> files = new LinkedHashMap<>(service.staticFiles());
            @SuppressWarnings("unchecked")
            String html = QuoteImageTemplate.html(PackageQuoteImageService.page((CatalogPackage) c.getValue()[0],
                    (double[]) c.getValue()[1], (List<Line>) c.getValue()[2], service.new Assets(files)));
            byte[] jpg = gotenberg.screenshotJpeg(html, files, PackageQuoteImageService.WIDTH);
            assertThat(jpg).hasSizeGreaterThan(50_000);
            Files.write(Path.of("target", "quote-" + c.getKey() + ".jpg"), jpg);
        }
    }

    // ------------------------------------------------------------------ số liệu mẫu

    private static final String PANEL_SPECS = """
            {"cong_nghe": {"ten": "Công nghệ", "unit": "", "value": null},
             "cong_suat": {"ten": "Công suất", "unit": "Wp", "value": 625},
             "hieu_suat": {"ten": "Hiệu suất", "unit": "%", "value": 23.1},
             "khoi_luong": {"ten": "Khối lượng", "unit": "kg", "value": 33.1},
             "kich_thuoc": {"ten": "Kích thước", "unit": "mm", "value": "2382 x 1134 x 30"}}""";

    private static CatalogPackage onGrid19() {
        return pkg(156, "Hệ On-Grid 19 kWp 3 pha", "ON_GRID", 3, 18.75, 0, 154_300_000d);
    }

    private static List<Line> onGrid19Lines() {
        return List.of(
                line("Tấm pin mặt trời JA 625 Wp", "TAM_PIN", "JA Solar", 30, 144, 2_600_000d, true, PANEL_SPECS,
                        MINIO + "quang_cao_ja_solar_1772814525673",
                        MINIO + "vat_tu_anh_tam_pin_mat_troi_ja_625_wp_1790135615506_1"),
                line("Inverter Auxsol On-Grid 3 pha 20 kW", "BIEN_TAN", "Auxsol", 1, 60, 30_000_000d, true, """
                                {"so_pha": {"unit": "", "value": "1 pha"}, "cong_suat": {"unit": "kW", "value": 15},
                                 "phan_loai": {"unit": "", "value": "Ongrid"},
                                 "khoi_luong": {"unit": "kg", "value": 25},
                                 "kich_thuoc": {"unit": "mm", "value": "448 x 336 x 174"}}""",
                        MINIO + "quang_cao_auxsol_1772814172287",
                        MINIO + "vat_tu_anh_inverter_auxsol_ongrid_3_pha_20_kw_1790137481409_1"),
                line("Kẹp biên", "HE_KHUNG_NHOM", "SLM", 28, 60, 11_000d, false, "{}"),
                line("Dây điện DC 4.0 đỏ", "HE_DAY_DIEN", "Leader/ Tương đương", 150, 60, 18_000d, false, "{}"),
                line("Tủ điện NLMT On-Grid 20 kW 3 pha", "TU_DIEN", "SLM", 1, 12, 7_000_000d, false, "{}"));
    }

    private static CatalogPackage hybrid16() {
        return pkg(132, "Hệ Hy-Brid 16 kWp 3 pha áp cao - Lưu trữ 15.36 kWh", "HYBRID", 3, 15.625, 15.36,
                256_100_000d);
    }

    private static List<Line> hybrid16Lines() {
        return List.of(
                line("Tấm pin mặt trời JA 625 Wp", "TAM_PIN", "JA Solar", 25, 144, 2_600_000d, true, PANEL_SPECS,
                        MINIO + "quang_cao_ja_solar_1772814525673",
                        MINIO + "vat_tu_anh_tam_pin_mat_troi_ja_625_wp_1790135615506_1"),
                line("Inverter Solis Hy-Brid 3 pha áp cao 10 kW", "BIEN_TAN", "Solis", 1, 60, 38_200_000d, true, """
                                {"so_pha": {"unit": "", "value": "3 pha"}, "cong_suat": {"unit": "kW", "value": 10},
                                 "khoi_luong": {"unit": "kg", "value": 30.2},
                                 "kich_thuoc": {"unit": "mm", "value": "600 × 500 × 230"}}""",
                        MINIO + "quang_cao_solis_1772814325050",
                        MINIO + "vat_tu_anh_inverter_solis_hybrid_3_pha_ap_cao_10_kw_1790136706812_1"),
                line("Bộ điều khiển và đế đỡ pin đồng bộ hệ", "PIN_LUU_TRU", "Dyness", 1, 24, 14_000_000d, false,
                        "{}"),
                line("Pin lithium Dyness áp cao 5.12 kWh", "PIN_LUU_TRU", "Dyness", 3, 60, 25_000_000d, true, """
                                {"dung_luong": {"unit": "kWh", "value": 5.12}, "khoi_luong": {"unit": "kg", "value": 47},
                                 "kich_thuoc": {"unit": "mm", "value": "591 x 390 x 125"},
                                 "cach_lap_dat": {"unit": "", "value": "Xếp chồng không dây"}}""",
                        MINIO + "quang_cao_dyness_1772814425610",
                        MINIO + "vat_tu_anh_pin_lithium_dyness_ap_cao_512_kwh_1790147178554_1"),
                line("Thanh full-rail", "HE_KHUNG_NHOM", "SLM", 15, 60, 360_000d, false, "{}"),
                line("Tủ điện NLMT Hybrid 15 kW 3 pha", "TU_DIEN", "SLM", 1, 12, 8_000_000d, false, "{}"));
    }

    private static CatalogPackage pkg(int id, String name, String type, int phases, double kwp, double storage,
                                      Double price) {
        return new CatalogPackage(id, "HN", null, null, name, type, phases, kwp, null, 625d, null, storage, price, 0,
                false, true, true, List.of(), "JA Solar", null, null, false, false, List.of(), null);
    }

    private static Line line(String name, String group, String brand, int qty, int warranty, Double price,
                             boolean visible, String specs) {
        return line(name, group, brand, qty, warranty, price, visible, specs, "logo://" + brand, null);
    }

    private static Line line(String name, String group, String brand, int qty, int warranty, Double price,
                             boolean visible, String specs, String logo, String image) {
        String unit = "TAM_PIN".equals(group) ? "Tấm" : "Bộ";
        return new Line(name, group, brand, logo, unit, qty, warranty, price, visible, json(specs), image);
    }

    private static JsonNode json(String s) {
        try {
            return JSON.readTree(s);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
