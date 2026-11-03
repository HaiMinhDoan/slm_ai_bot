package vn.slmglobal.slmai.services.knowledge.admin;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;

import java.util.Arrays;
import java.util.List;

/** Unified diff giữa hai nội dung (mục 5.9: lịch sử phiên bản, diff, chống ghi đè). Trang admin chỉ hiển thị. */
public final class KnowledgeDiff {

    private KnowledgeDiff() {
    }

    /** Rỗng khi hai nội dung giống nhau */
    public static String unified(String fromName, String from, String toName, String to) {
        List<String> original = lines(from);
        List<String> revised = lines(to);
        Patch<String> patch = DiffUtils.diff(original, revised);
        if (patch.getDeltas().isEmpty()) {
            return "";
        }
        return String.join("\n", UnifiedDiffUtils.generateUnifiedDiff(fromName, toName, original, patch, 3));
    }

    private static List<String> lines(String text) {
        return text == null || text.isEmpty() ? List.of() : Arrays.asList(text.split("\n", -1));
    }
}
