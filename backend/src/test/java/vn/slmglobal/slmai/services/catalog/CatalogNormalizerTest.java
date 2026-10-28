package vn.slmglobal.slmai.services.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import vn.slmglobal.slmai.adapters.slmv2.SlmV2Snapshot;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogNormalizerTest {

    private final CatalogNormalizer normalizer = new CatalogNormalizer(new ObjectMapper());

    @Test
    void normalizesTypesPhasesAndSpecs() {
        assertThat(CatalogNormalizer.systemType("Hy-Brid")).isEqualTo("HYBRID");
        assertThat(CatalogNormalizer.systemType("Hybrid ")).isEqualTo("HYBRID");
        assertThat(CatalogNormalizer.systemType("On-Grid")).isEqualTo("ON_GRID");
        assertThat(CatalogNormalizer.systemType("Ongrid")).isEqualTo("ON_GRID");
        assertThat(CatalogNormalizer.systemType("abc")).isNull();
        assertThat(CatalogNormalizer.phases("1 pha")).isEqualTo(1);
        assertThat(CatalogNormalizer.phases("3 pha áp thấp")).isEqualTo(3);
        assertThat(CatalogNormalizer.phases("")).isNull();

        var specs = normalizer.specs("""
                {"dung_luong": {"ten": "Dung lượng", "donVi": "kWh", "giaTri": "5,12 "},
                 "cach_lap_dat": {"ten": "Cách lắp đặt", "donVi": "", "giaTri": "abc"},
                 "khoi_luong": {"Ten": "Khối lượng", "DonVi": "kg", "GiaTri": 0},
                 "kich_thuoc": {"ten": "Kích thước", "donVi": "mm", "giaTri": "558 x 545 x 150"}}""");
        assertThat(CatalogNormalizer.number(specs, "dung_luong")).isEqualTo(5.12);
        assertThat(specs.path("cach_lap_dat").path("value").isNull()).isTrue();
        assertThat(specs.path("khoi_luong").path("value").isNull()).isTrue();
        assertThat(specs.path("khoi_luong").path("unit").asText()).isEqualTo("kg");
        assertThat(specs.path("kich_thuoc").path("value").asText()).isEqualTo("558 x 545 x 150");
    }

    @Test
    void computesPanelsBatteryAndCapacityFromItems() {
        CatalogRows.Normalized n = normalizer.normalize(snapshot(
                pkg(98, 1, "Hệ Hy-Brid 5 kWp 1 pha - Lưu trữ 5.12 kWh", "Hy-Brid", "1 pha", 5000.0, 90_800_000.0)));
        CatalogRows.PackageRow p = n.packages().get(0);
        assertThat(p.systemType()).isEqualTo("HYBRID");
        assertThat(p.phases()).isEqualTo(1);
        assertThat(p.kwp()).isEqualTo(5.0);
        assertThat(p.panelCount()).isEqualTo(8);
        assertThat(p.panelWp()).isEqualTo(625.0);
        assertThat(p.storageKwh()).isEqualTo(5.12);
        assertThat(p.inverterKw()).isEqualTo(5.0);
        assertThat(p.branchCode()).isEqualTo("HN");
        assertThat(p.recommendable()).isTrue();
        assertThat(p.warnings()).isEmpty();
        assertThat(n.version()).hasSize(64);
    }

    @Test
    void wrongCapacityIsRecomputedFromPanels() {
        // Gói 101 ở slm_v2: cong_suat_he_thong = 9 (đúng ra 8750 W, 14 tấm × 625 Wp)
        SlmV2Snapshot s = snapshot(pkg(101, 1, "Hệ Hy-Brid 9 kWp 1 pha", "Hy-Brid", "1 pha", 9.0, 113_200_000.0));
        s = withPanels(s, 101, 14);
        CatalogRows.PackageRow p = normalizer.normalize(s).packages().get(0);
        assertThat(p.kwp()).isEqualTo(8.75);
        assertThat(p.warnings()).contains("CAPACITY_FROM_PANELS");
        assertThat(p.recommendable()).isTrue();
    }

    @Test
    void blockingWarningsStopRecommendation() {
        CatalogRows.Normalized n = normalizer.normalize(snapshot(
                pkg(200, 1, "Gói lạ", "abc", "1 pha", 5000.0, 50_000_000.0),
                pkg(201, 1, "Gói chưa có giá", "On-Grid", "1 pha", 5000.0, 0.0)));
        assertThat(n.packages().get(0).warnings()).contains("UNKNOWN_SYSTEM_TYPE");
        assertThat(n.packages().get(0).recommendable()).isFalse();
        assertThat(n.packages().get(1).warnings()).contains("MISSING_PRICE");
        assertThat(n.packages().get(1).recommendable()).isFalse();
        assertThat(n.warnings().get("MISSING_PRICE")).containsExactly(201);
    }

    @Test
    void inactiveAccessoryWarnsButInactiveMainDeviceBlocks() {
        SlmV2Snapshot base = snapshot(pkg(105, 1, "Hệ Hy-Brid 9 kWp 1 pha - Lưu trữ 16 kWh", "Hy-Brid", "1 pha",
                8750.0, 144_200_000.0));
        List<SlmV2Snapshot.Material> materials = new ArrayList<>(base.materials());
        materials.add(new SlmV2Snapshot.Material(134, "day_ac", "Dây điện AC 1 pha", 5, 4, "m", null, "{}", 60,
                false, 0));
        List<SlmV2Snapshot.PackageItem> items = new ArrayList<>(base.packageItems());
        items.add(new SlmV2Snapshot.PackageItem(9999, 105, 134, 20, 60, true, false, 1));
        CatalogRows.PackageRow accessory = normalizer.normalize(new SlmV2Snapshot(base.brands(), base.branches(),
                base.materialGroups(), materials, base.packageFamilies(), base.packages(), items, base.domains()))
                .packages().get(0);
        assertThat(accessory.warnings()).contains("INACTIVE_ACCESSORY_IN_PACKAGE");
        assertThat(accessory.recommendable()).isTrue();

        items.set(items.size() - 1, new SlmV2Snapshot.PackageItem(9999, 105, 134, 20, 60, true, true, 1));
        CatalogRows.PackageRow main = normalizer.normalize(new SlmV2Snapshot(base.brands(), base.branches(),
                base.materialGroups(), materials, base.packageFamilies(), base.packages(), items, base.domains()))
                .packages().get(0);
        assertThat(main.warnings()).contains("INACTIVE_MATERIAL_IN_PACKAGE");
        assertThat(main.recommendable()).isFalse();
    }

    @Test
    void sameInputGivesSameHashes() {
        SlmV2Snapshot s = snapshot(pkg(98, 1, "Hệ Hy-Brid 5 kWp", "Hy-Brid", "1 pha", 5000.0, 90_800_000.0));
        assertThat(normalizer.normalize(s).version()).isEqualTo(normalizer.normalize(s).version());
        assertThat(normalizer.normalize(s).packages().get(0).hash())
                .isNotEqualTo(normalizer.normalize(snapshot(pkg(98, 1, "Hệ Hy-Brid 5 kWp", "Hy-Brid", "1 pha", 5000.0,
                        91_000_000.0))).packages().get(0).hash());
    }

    // ------------------------------------------------------------------ dữ liệu mẫu giống slm_v2

    static SlmV2Snapshot.Package pkg(int id, int branchId, String name, String type, String phase, Double watts,
                                     Double price) {
        return new SlmV2Snapshot.Package(id, branchId, 6, name, 347, type, phase, watts, 500.0, 700.0, null, price,
                0.0, false, 1);
    }

    /** Mỗi gói: 8 tấm JA 625 Wp, inverter 5 kW, pin 5,12 kWh, nhân công */
    static SlmV2Snapshot snapshot(SlmV2Snapshot.Package... packages) {
        List<SlmV2Snapshot.PackageItem> items = new ArrayList<>();
        int itemId = 1;
        for (SlmV2Snapshot.Package p : packages) {
            items.add(new SlmV2Snapshot.PackageItem(itemId++, p.id(), 233, 8, 144, true, true, 1));
            items.add(new SlmV2Snapshot.PackageItem(itemId++, p.id(), 62, 1, 60, true, true, 1));
            items.add(new SlmV2Snapshot.PackageItem(itemId++, p.id(), 57, 1, 60, true, true, 1));
            items.add(new SlmV2Snapshot.PackageItem(itemId++, p.id(), 58, 1, 24, true, false, 1));
            items.add(new SlmV2Snapshot.PackageItem(itemId++, p.id(), 115, 1, 0, false, true, 1));
        }
        return new SlmV2Snapshot(
                List.of(new SlmV2Snapshot.Brand(1, "JA Solar", null, "Trung Quốc", null, 1),
                        new SlmV2Snapshot.Brand(2, "Solis", null, "Trung Quốc", null, 1),
                        new SlmV2Snapshot.Brand(4, "SLM", null, "Việt Nam", null, 1),
                        new SlmV2Snapshot.Brand(5, "Dyness", null, "Trung Quốc", null, 1)),
                List.of(new SlmV2Snapshot.Branch(1, "HN", "Hà Nội", null, null, 1),
                        new SlmV2Snapshot.Branch(2, "HCM", "Hồ Chí Minh", null, null, 1)),
                List.of(new SlmV2Snapshot.MaterialGroup(1, "TAM_PIN", "Tấm pin", 1),
                        new SlmV2Snapshot.MaterialGroup(2, "BIEN_TAN", "Biến tần", 1),
                        new SlmV2Snapshot.MaterialGroup(3, "PIN_LUU_TRU", "Pin lưu trữ", 1),
                        new SlmV2Snapshot.MaterialGroup(4, "TRON_GOI_LAP_DAT", "Trọn gói lắp đặt", 1),
                        new SlmV2Snapshot.MaterialGroup(5, "HE_DAY_DIEN", "Hệ dây điện", 1)),
                List.of(material(233, 1, 1, "Tấm pin mặt trời JA 625 Wp",
                                "{\"cong_suat\": {\"ten\": \"Công suất\", \"donVi\": \"Wp\", \"giaTri\": 625}}", 144),
                        material(62, 2, 2, "Inverter Solis Hy-Brid 1 pha 5 kW",
                                "{\"cong_suat\": {\"ten\": \"Công suất\", \"donVi\": \"kW\", \"giaTri\": 5}}", 60),
                        material(57, 3, 5, "Pin lithium Dyness 5.12 kWh",
                                "{\"dung_luong\": {\"ten\": \"Dung lượng\", \"donVi\": \"kWh\", \"giaTri\": 5.12}}", 60),
                        material(58, 2, 2, "Bộ ghi dữ liệu biến tần",
                                "{\"cong_suat\": {\"ten\": \"Công suất\", \"donVi\": \"kW\", \"giaTri\": 0}}", 24),
                        material(115, 4, 4, "Nhân công lắp đặt, vận chuyển", "{}", 0)),
                List.of(new SlmV2Snapshot.PackageFamily(6, "JA Solar - Solis - Dyness", 1, 2, 5, 1)),
                List.of(packages),
                items,
                List.of(new SlmV2Snapshot.Domain(4, "slmsolar.vn", 1, 1),
                        new SlmV2Snapshot.Domain(2, "slmsolar.com.vn", 2, 1)));
    }

    static SlmV2Snapshot withPanels(SlmV2Snapshot s, int packageId, int panels) {
        List<SlmV2Snapshot.PackageItem> items = s.packageItems().stream()
                .map(i -> i.tronGoiId() == packageId && i.vatTuId() == 233
                        ? new SlmV2Snapshot.PackageItem(i.id(), i.tronGoiId(), i.vatTuId(), panels, i.thoiGianBaoHanh(),
                        i.duocBaoHanh(), i.duocXem(), i.trangThai())
                        : i)
                .toList();
        return new SlmV2Snapshot(s.brands(), s.branches(), s.materialGroups(), s.materials(), s.packageFamilies(),
                s.packages(), items, s.domains());
    }

    private static SlmV2Snapshot.Material material(int id, int group, int brand, String name, String specs,
                                                   int warranty) {
        return new SlmV2Snapshot.Material(id, "vt_" + id, name, group, brand, "Bộ", null, specs, warranty, true, 1);
    }
}
