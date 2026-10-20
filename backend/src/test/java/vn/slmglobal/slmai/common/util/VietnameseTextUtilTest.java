package vn.slmglobal.slmai.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VietnameseTextUtilTest {

    @Test
    void unaccent() {
        assertThat(VietnameseTextUtil.unaccent("Hà Nội")).isEqualTo("ha noi");
        assertThat(VietnameseTextUtil.unaccent("  Đắk Lắk ")).isEqualTo("dak lak");
        assertThat(VietnameseTextUtil.unaccent("TP. Hồ Chí Minh")).isEqualTo("tp. ho chi minh");
        assertThat(VietnameseTextUtil.unaccent(null)).isEmpty();
    }

    @Test
    void toSlug() {
        assertThat(VietnameseTextUtil.toSlug("Chính sách bảo hành")).isEqualTo("chinh-sach-bao-hanh");
        assertThat(VietnameseTextUtil.toSlug("On-grid  hay   Hybrid?")).isEqualTo("on-grid-hay-hybrid");
    }
}
