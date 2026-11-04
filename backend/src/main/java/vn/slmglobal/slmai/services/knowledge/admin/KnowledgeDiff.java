package vn.slmglobal.slmai.services.knowledge.admin;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Patch;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/** Unified diff giữa hai nội dung (mục 5.9: lịch sử phiên bản, diff, chống ghi đè). Trang admin chỉ hiển thị. */
public final class KnowledgeDiff {

    private KnowledgeDiff() {
    }

    /**
     * Bỏ qua dòng trống và khoảng trắng cuối dòng; với Markdown bỏ qua cả thụt lề đầu dòng. Trình soạn Markdown
     * (vditor) tự chuẩn hoá định dạng (thêm dòng trống sau tiêu đề, đổi thụt lề danh sách), không đổi nghĩa khi bot
     * đọc. YAML giữ thụt lề vì thụt lề là cấu trúc. Rỗng khi hai nội dung giống nhau theo cách so này.
     */
    public static String unified(String fromName, String from, String toName, String to, boolean yaml) {
        List<String> original = lines(from);
        List<String> revised = lines(to);
        Patch<String> patch = DiffUtils.diff(original, revised,
                yaml ? (a, b) -> a.stripTrailing().equals(b.stripTrailing()) : (a, b) -> a.strip().equals(b.strip()));
        Patch<String> meaningful = new Patch<>();
        for (AbstractDelta<String> d : patch.getDeltas()) {
            if (!blank(d.getSource().getLines()) || !blank(d.getTarget().getLines())) {
                meaningful.addDelta(d);
            }
        }
        if (meaningful.getDeltas().isEmpty()) {
            return "";
        }
        // Cắt khoảng trắng cuối dòng: dòng ngữ cảnh trống (" ") bị trình xem diff tô đỏ như lỗi khoảng trắng
        return UnifiedDiffUtils.generateUnifiedDiff(fromName, toName, original, meaningful, 3).stream()
                .map(String::stripTrailing).collect(Collectors.joining("\n"));
    }

    private static boolean blank(List<String> lines) {
        return lines.stream().allMatch(String::isBlank);
    }

    private static List<String> lines(String text) {
        return text == null || text.isEmpty() ? List.of() : Arrays.asList(text.split("\n", -1));
    }
}
