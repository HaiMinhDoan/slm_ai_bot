package vn.slmglobal.slmai.services.knowledge;

import org.junit.jupiter.api.Test;
import vn.slmglobal.slmai.services.knowledge.admin.KnowledgeDiff;

import static org.assertj.core.api.Assertions.assertThat;

/** Diff bỏ qua phần vditor tự chuẩn hoá (dòng trống, thụt lề Markdown), giữ thụt lề YAML. */
class KnowledgeDiffTest {

    private static final String ORIGINAL = "# Tiêu đề\n## Các bước\n1. Bước một\n  tiếp ý\n## Không được\n- Không hứa";

    @Test
    void markdownFormattingOnlyIsNoDifference() {
        String formatted = "# Tiêu đề\n\n## Các bước\n\n1. Bước một\n    tiếp ý\n\n## Không được\n\n- Không hứa\n";
        assertThat(KnowledgeDiff.unified("a", ORIGINAL, "b", formatted, false)).isEmpty();
    }

    @Test
    void realChangeShowsWithoutBlankLineNoise() {
        String edited = "# Tiêu đề\n\n## Các bước\n\n1. Bước một\n    tiếp ý\n\n## Không được\n\n- Không hứa\n"
                + "- Không giảm giá\n";
        String diff = KnowledgeDiff.unified("a", ORIGINAL, "b", edited, false);
        assertThat(diff).contains("+- Không giảm giá");
        // Dòng trống vditor chèn sau tiêu đề không hiện thành thay đổi
        assertThat(diff.lines().filter(l -> l.startsWith("+") && !l.startsWith("+++") && !l.substring(1).isBlank()))
                .containsExactly("+- Không giảm giá");
        assertThat(diff).doesNotContain("@@ -1,");
    }

    @Test
    void yamlKeepsIndentation() {
        String before = "regions:\n  BAC:\n    base: HN\n";
        String moved = "regions:\n  BAC:\n  base: HN\n";
        assertThat(KnowledgeDiff.unified("a", before, "b", moved, true)).contains("-    base: HN", "+  base: HN");
        assertThat(KnowledgeDiff.unified("a", before, "b", before + "\n", true)).isEmpty();
    }
}
