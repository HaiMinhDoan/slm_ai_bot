package vn.slmglobal.slmai.services.sizing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.slmglobal.slmai.services.catalog.CatalogMaterialRepository;
import vn.slmglobal.slmai.services.catalog.CatalogMaterialRepository.QuotableMaterial;
import vn.slmglobal.slmai.services.catalog.CatalogRepository;
import vn.slmglobal.slmai.services.catalog.CatalogRepository.CatalogPackage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Tool tra_cuu_vat_tu trên bộ vật tư giống slm_v2 (2026-11-06) */
class MaterialAdvisorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CatalogMaterialRepository materials = mock(CatalogMaterialRepository.class);
    private final CatalogRepository catalog = mock(CatalogRepository.class);
    private MaterialAdvisor advisor;

    @BeforeEach
    void setUp() throws Exception {
        PricingProperties pricing = new PricingProperties(3500, null, 0, 0, 0, 0,
                Map.of("BAC", "slmsolar.vn", "NAM", "slmsolar.com.vn"), "Giá trọn gói niêm yết.", null);
        advisor = new MaterialAdvisor(materials, catalog, new ProvinceDirectory(objectMapper), () -> pricing,
                objectMapper);
        when(materials.quotableMaterials(anyString())).thenReturn(List.of(
                m(233, "Tấm pin mặt trời JA 625 Wp", "TAM_PIN", "Tấm pin", true, "JA Solar", 144, 2_500_000.0, """
                        {"cong_nghe": {"ten": "Công nghệ", "unit": "", "value": null},
                         "cong_suat": {"ten": "Công suất", "unit": "Wp", "value": 625},
                         "hieu_suat": {"ten": "Hiệu suất", "unit": "%", "value": 23.1}}"""),
                m(62, "Inverter Solis Hy-Brid 1 pha 5 kW", "BIEN_TAN", "Biến tần", true, "Solis", 60, 21_200_000.0,
                        "{}"),
                m(72, "Inverter Solis Hy-Brid 1 pha 8 kW", "BIEN_TAN", "Biến tần", true, "Solis", 60, 31_700_000.0,
                        "{}"),
                // Giá giữ chỗ ở slm_v2
                m(220, "Inverter Invt On-Grid 3 pha 31 kW", "BIEN_TAN", "Biến tần", true, "Invt", 60, 1000.0, "{}"),
                m(57, "Pin lithium Dyness 5.12 kWh", "PIN_LUU_TRU", "Pin lưu trữ", true, "Dyness", 120, 23_000_000.0,
                        """
                        {"dung_luong": {"ten": "Dung lượng", "unit": "kWh", "value": 5.12},
                         "cach_lap_dat": {"ten": "Cách lắp đặt", "unit": "", "value": "Treo tường"}}"""),
                m(64, "Pin lithium Dyness áp cao 5.12 kWh", "PIN_LUU_TRU", "Pin lưu trữ", true, "Dyness", 120,
                        27_000_000.0, "{}"),
                m(60, "Pin lithium Dyness 14.3 kWh", "PIN_LUU_TRU", "Pin lưu trữ", true, "Dyness", 120, 51_000_000.0,
                        "{}"),
                m(70, "Dây từ nối Pin lithium lên Inverter", "PIN_LUU_TRU", "Pin lưu trữ", false, null, null,
                        1_000_000.0, "{}"),
                m(116, "Chân L mái ngói", "HE_KHUNG_NHOM", "Hệ khung nhôm", false, null, null, 50_000.0,
                        "{\"loai_mai\": {\"ten\": \"Loại mái\", \"unit\": \"\", \"value\": \"mái ngói\"}}")));
        when(materials.packagesUsing(any(), anyString())).thenReturn(Map.of(57, List.of(98, 999, 101, 97)));
        when(catalog.recommendablePackages("HN")).thenReturn(List.of(
                pkg(98, "Hệ Hy-Brid 5 kWp 1 pha - Lưu trữ 5.12 kWh", 90_800_000),
                pkg(101, "Hệ Hy-Brid 9 kWp 1 pha - Lưu trữ 5.12 kWh", 113_200_000)));
        when(catalog.recommendablePackages("HCM")).thenReturn(List.of(
                pkg(97, "Hệ Hy-Brid 5 kWp 1 pha - Lưu trữ 5.12 kWh", 90_800_000)));
        when(catalog.domains("HN")).thenReturn(List.of("slmsolar.com", "slmsolar.vn"));
        when(catalog.domains("HCM")).thenReturn(List.of("slmsolar.com.vn"));
    }

    @Test
    void batteryPriceSpecsLinkAndPackagesForNorthernCustomer() {
        JsonNode r = advisor.lookup("pin lưu trữ Dyness 5kWh", "PIN_LUU_TRU", "Hà Nội");

        assertThat(r.path("bang_gia").asText()).isEqualTo("giá miền Bắc");
        // Cả hai pin 5,12 kWh khớp, pin 14,3 kWh và dây nối thì không
        assertThat(ids(r)).containsExactly(57, 64);
        JsonNode pin = r.path("vat_tu").get(0);
        assertThat(pin.path("gia_niem_yet").asText()).isEqualTo("23 triệu đồng");
        assertThat(pin.path("gia_day_du").asText()).isEqualTo("23.000.000 đ");
        assertThat(pin.path("hang").asText()).isEqualTo("Dyness");
        assertThat(pin.path("bao_hanh").asText()).isEqualTo("bảo hành 10 năm");
        assertThat(pin.path("thong_so").path("Dung lượng").asText()).isEqualTo("5,12 kWh");
        assertThat(pin.path("thong_so").path("Cách lắp đặt").asText()).isEqualTo("Treo tường");
        assertThat(pin.path("link").asText()).isEqualTo("https://slmsolar.vn/devices/57");
        // Chỉ gói đang được báo giá của miền, rẻ trước
        assertThat(pin.path("co_trong_goi").findValues("goi_id")).extracting(JsonNode::asInt).containsExactly(98, 101);
        assertThat(r.path("dieu_kien_gia_le").asText()).contains("chưa gồm lắp đặt");
        assertThat(r.path("ghi_chu").toString()).contains("Không cộng giá lẻ");
    }

    @Test
    void southernCustomerGetsSouthernDomainAndPackages() {
        JsonNode r = advisor.lookup("Dyness 5.12 kWh", null, "Hồ Chí Minh");

        assertThat(r.path("bang_gia").asText()).isEqualTo("giá miền Nam");
        JsonNode pin = r.path("vat_tu").get(0);
        assertThat(pin.path("link").asText()).isEqualTo("https://slmsolar.com.vn/devices/57");
        assertThat(pin.path("co_trong_goi").findValues("goi_id")).extracting(JsonNode::asInt).containsExactly(97);
    }

    @Test
    void unknownProvinceUsesNorthernPriceWithoutLink() {
        JsonNode r = advisor.lookup("inverter Solis Hy-Brid 1 pha 8 kW", null, null);

        assertThat(ids(r)).containsExactly(72);
        assertThat(r.path("bang_gia").asText()).startsWith("tạm theo giá miền Bắc");
        assertThat(r.path("vat_tu").get(0).has("link")).isFalse();
        assertThat(r.path("vat_tu").get(0).path("gia_niem_yet").asText()).isEqualTo("31,7 triệu đồng");
    }

    @Test
    void groupListingAccessoriesAndPlaceholderPrices() {
        JsonNode batteries = advisor.lookup(null, "PIN_LUU_TRU", "Hà Nội");
        assertThat(ids(batteries)).containsExactly(57, 64, 60, 70);
        // Dây nối là phụ kiện: không báo giá lẻ dù slm_v2 có giá
        assertThat(batteries.path("vat_tu").get(3).path("gia_niem_yet").asText()).startsWith("không báo giá lẻ");
        assertThat(batteries.path("vat_tu").get(3).has("gia_day_du")).isFalse();

        JsonNode frame = advisor.lookup("khung mái ngói", "PHU_KIEN", "Hà Nội");
        assertThat(ids(frame)).containsExactly(116);
        assertThat(frame.path("vat_tu").get(0).path("thong_so").path("Loại mái").asText()).isEqualTo("mái ngói");

        JsonNode invt = advisor.lookup("Invt 31 kW", "BIEN_TAN", "Hà Nội");
        assertThat(invt.path("vat_tu").get(0).path("gia_niem_yet").asText()).startsWith("chưa có giá niêm yết");
        assertThat(invt.path("vat_tu").get(0).has("link")).isFalse();
    }

    @Test
    void noMatchSaysSoWithoutInventing() {
        JsonNode r = advisor.lookup("pin Tesla Powerwall", "PIN_LUU_TRU", "Hà Nội");

        // "pin" khớp mọi pin nên trả các pin đang bán: AI thấy không có Tesla, giới thiệu hãng đang có
        assertThat(ids(r)).contains(57, 60);
        JsonNode none = advisor.lookup("Tesla Powerwall", null, "Hà Nội");
        assertThat(none.path("vat_tu")).isEmpty();
        assertThat(none.path("ghi_chu").toString()).contains("không bịa");
    }

    @Test
    void tokensSplitNumbersAndUnits() {
        assertThat(MaterialMatcher.tokens("Pin 5,12kWh Hy-Brid")).containsExactly("pin", "5.12", "kwh", "hy", "brid");
    }

    private static List<Integer> ids(JsonNode r) {
        List<Integer> out = new ArrayList<>();
        r.path("vat_tu").forEach(v -> out.add(v.path("vat_tu_id").asInt()));
        return out;
    }

    private QuotableMaterial m(int id, String name, String group, String groupName, boolean main, String brand,
                               Integer warranty, Double price, String specs) throws Exception {
        return new QuotableMaterial(id, name, group, groupName, main, brand, "Cái", warranty,
                objectMapper.readTree(specs), price);
    }

    private static CatalogPackage pkg(int id, String name, double price) {
        return new CatalogPackage(id, "HN", 6, "Hy-Brid", name, "HYBRID", 1, 5.0, 8, 625.0, 5.0, 5.12, price, 0,
                false, true, true, List.of(), "JA Solar", "Solis", "Dyness", false, false, List.of(), null);
    }
}
