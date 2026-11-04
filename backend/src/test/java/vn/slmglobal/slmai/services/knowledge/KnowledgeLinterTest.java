package vn.slmglobal.slmai.services.knowledge;

import org.junit.jupiter.api.Test;
import vn.slmglobal.slmai.services.knowledge.admin.KnowledgeLinter;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Kiểm tra tức thì khi soạn (mục 5.10 A). */
class KnowledgeLinterTest {

    private static final Map<String, String> TERMS = KnowledgeLinter.terms("""
            | Dùng | Tránh | Ghi chú |
            |---|---|---|
            | pin lưu trữ | ắc quy, bình | |
            | kWh | số điện (trong tài liệu) | |
            """);

    @Test
    void termsTableIsParsed() {
        assertThat(TERMS).containsEntry("bình", "pin lưu trữ").containsEntry("số điện", "kWh")
                .containsEntry("ắc quy", "pin lưu trữ");
    }

    @Test
    void numbersAreFlaggedWithOrWithoutDiacritics() {
        assertThat(codes(KnowledgeDocType.PLAYBOOK, "Hệ 5 kWp giá 80 triệu.")).contains("HARD_NUMBER");
        assertThat(codes(KnowledgeDocType.PLAYBOOK, "Gói này khoảng 5 trieu thôi.")).contains("HARD_NUMBER");
        // Tài liệu bot không đọc (hướng dẫn soạn) được ghi số
        assertThat(codes(KnowledgeDocType.GUIDE, "Hệ 5 kWp giá 80 triệu.")).doesNotContain("HARD_NUMBER");
    }

    @Test
    void termsIgnoreCompoundsWithAnotherMeaning() {
        assertThat(codes(KnowledgeDocType.FAQ, "Mời khách để lại số điện thoại.")).doesNotContain("TERM");
        assertThat(codes(KnowledgeDocType.FAQ, "Mức dùng trung bình của nhà ở.")).doesNotContain("TERM");
        assertThat(codes(KnowledgeDocType.FAQ, "Bình 10 kWh dùng được cả đêm.")).contains("TERM");
        assertThat(codes(KnowledgeDocType.FAQ, "Nhà dùng 300 số điện mỗi tháng.")).contains("TERM");
    }

    @Test
    void unaccentedLinesAndUnknownPlaceholders() {
        assertThat(codes(KnowledgeDocType.FAQ, "Neu khach hoi gia thi nhan vien goi lai cho khach nhe"))
                .contains("NO_DIACRITICS");
        assertThat(codes(KnowledgeDocType.FAQ, "Chào anh chị, em là {{ten_tu_van_vien}} của {{ten_cong_ty}}."))
                .doesNotContain("UNKNOWN_PLACEHOLDER");
        assertThat(codes(KnowledgeDocType.FAQ, "Em là {{ten_nhan_vien}}.")).contains("UNKNOWN_PLACEHOLDER");
    }

    @Test
    void templateGapsAreFlagged() {
        assertThat(codes(KnowledgeDocType.POLICY, "- <điều khoản 1>")).contains("TEMPLATE_GAP");
        assertThat(codes(KnowledgeDocType.POLICY, "- <...>")).contains("TEMPLATE_GAP");
        assertThat(codes(KnowledgeDocType.POLICY, "Xuống dòng<br>tiếp")).doesNotContain("TEMPLATE_GAP");
        // Mẫu (guide/mau) bot không đọc: chỗ trống là chủ ý
        assertThat(codes(KnowledgeDocType.TEMPLATE, "- <điều khoản 1>")).doesNotContain("TEMPLATE_GAP");
    }

    @Test
    void playbookMissingSectionsAndYamlSkipped() {
        assertThat(codes(KnowledgeDocType.PLAYBOOK, "# Tiêu đề\n\n## Mục tiêu\n\nBáo giá sớm.\n"))
                .contains("MISSING_SECTION");
        assertThat(codes(KnowledgeDocType.RULE, "price_per_kwh: 3500")).isEmpty();
    }

    private static List<String> codes(KnowledgeDocType type, String content) {
        return KnowledgeLinter.lint(type, content, TERMS).stream().map(KnowledgeLinter.Issue::code).toList();
    }
}
