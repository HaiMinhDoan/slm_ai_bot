package vn.slmglobal.slmai.services.sizing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import vn.slmglobal.slmai.services.sizing.ProvinceDirectory.Region;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ProvinceDirectoryTest {

    private final ProvinceDirectory directory = new ProvinceDirectory(new ObjectMapper());

    @Test
    void has34ProvincesCovering63FormerProvinces() {
        List<ProvinceDirectory.Province> all = directory.all();
        assertThat(all).hasSize(34);
        Set<String> former = new HashSet<>();
        all.forEach(p -> former.addAll(p.formerProvinces()));
        assertThat(former).hasSize(63);
        assertThat(all.stream().filter(p -> p.region() == Region.BAC)).hasSize(20);
        assertThat(all.stream().filter(p -> p.region() == Region.NAM)).hasSize(14);
        assertThat(all.get(0).code()).isEqualTo("TUYEN_QUANG");
    }

    @Test
    void regionSplitsAtDaNang() {
        assertThat(region("Huế")).isEqualTo(Region.BAC);
        assertThat(region("Đà Nẵng")).isEqualTo(Region.NAM);
        assertThat(region("Quảng Nam")).isEqualTo(Region.NAM);
        assertThat(region("Quảng Bình")).isEqualTo(Region.BAC);
        assertThat(directory.byCode("DA_NANG").orElseThrow().branchCode()).isEqualTo("HCM");
        assertThat(directory.byCode("HA_NOI").orElseThrow().branchCode()).isEqualTo("HN");
    }

    @Test
    void resolvesFormerProvincesAndCommonNames() {
        assertResolves("Bình Dương", "HO_CHI_MINH", "Bình Dương");
        assertResolves("Bà Rịa - Vũng Tàu", "HO_CHI_MINH", "Bà Rịa - Vũng Tàu");
        assertResolves("Nam Định", "NINH_BINH", "Nam Định");
        assertResolves("Hà Nam", "NINH_BINH", "Hà Nam");
        assertResolves("Thừa Thiên Huế", "HUE", "Thừa Thiên Huế");
        assertResolves("Hà Nội", "HA_NOI", null);
        assertResolves("TP. HCM", "HO_CHI_MINH", null);
        assertResolves("tphcm", "HO_CHI_MINH", null);
        assertResolves("Sài Gòn", "HO_CHI_MINH", null);
        assertResolves("thanh pho ho chi minh", "HO_CHI_MINH", null);
        assertResolves("hn", "HA_NOI", null);
        assertResolves("Tỉnh Đắk Lắk", "DAK_LAK", null);
        assertResolves("Daklak", "DAK_LAK", null);
        assertResolves("Nha Trang", "KHANH_HOA", null);
        assertResolves("Vinh", "NGHE_AN", null);
        assertResolves("Vĩnh Phúc", "PHU_THO", "Vĩnh Phúc");
        assertResolves("binhduong", "HO_CHI_MINH", "Bình Dương");
    }

    @Test
    void findsProvinceAtTheEndOfAnAddress() {
        // Tên xã trùng tên tỉnh cũ: tên tỉnh đứng sau cùng thắng
        assertResolves("xã Vĩnh Phúc, huyện Bắc Quang, Hà Giang", "TUYEN_QUANG", "Hà Giang");
        assertResolves("Quận 7, TP HCM", "HO_CHI_MINH", null);
        assertResolves("nhà em ở Thủ Đức", "HO_CHI_MINH", null);
    }

    @Test
    void unknownOrEmptyIsNotGuessed() {
        assertThat(directory.resolve("Phnom Penh")).isEmpty();
        assertThat(directory.resolve("  ")).isEmpty();
        assertThat(directory.resolve(null)).isEmpty();
        assertThat(directory.resolve("miền Tây")).isEmpty();
    }

    private Region region(String name) {
        return directory.resolve(name).orElseThrow().province().region();
    }

    private void assertResolves(String text, String code, String formerName) {
        ProvinceDirectory.Resolution r = directory.resolve(text).orElseThrow(() -> new AssertionError(text));
        assertThat(r.province().code()).as(text).isEqualTo(code);
        assertThat(r.formerName()).as(text).isEqualTo(formerName);
    }
}
