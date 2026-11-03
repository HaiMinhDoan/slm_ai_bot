package vn.slmglobal.slmai.services.policy;

import java.util.ArrayList;
import java.util.List;

/**
 * Luật kiểm tra tin AI trước khi gửi (rule {@code rules/kiem-tra-truoc-khi-gui}, mục 33), sửa trên admin và phát
 * hành cùng kiến thức. Vi phạm thì lượt chuyển sang DRAFT, không bỏ tin.
 *
 * @param maxLength         độ dài tối đa một tin (ký tự); kênh có giới hạn thấp hơn thì lấy giới hạn của kênh
 * @param oneQuestionOnly   mỗi tin chỉ một câu hỏi (mục 58.2)
 * @param numberTolerance   sai lệch tương đối cho phép khi đối chiếu con số với nguồn (0,01 = 1%)
 * @param extraAllowedLinks link / tên miền luôn được gửi, ngoài link trong kết quả công cụ và trang chủ theo miền
 */
public record ReplyPolicyRule(int maxLength, boolean oneQuestionOnly, double numberTolerance,
                              List<String> extraAllowedLinks) {

    public static final ReplyPolicyRule DEFAULT = new ReplyPolicyRule(2000, true, NumberChecker.DEFAULT_TOLERANCE,
            List.of());

    public ReplyPolicyRule {
        extraAllowedLinks = extraAllowedLinks == null ? List.of() : List.copyOf(extraAllowedLinks);
    }

    /** Lỗi kiểm tra khi phát hành, rỗng nếu hợp lệ */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        if (maxLength < 100 || maxLength > 2000) {
            errors.add("max_length: phải từ 100 đến 2000 ký tự (Messenger giới hạn 2000)");
        }
        if (numberTolerance < 0 || numberTolerance > 0.1) {
            errors.add("number_tolerance: phải từ 0 đến 0,1 (10%)");
        }
        for (String link : extraAllowedLinks) {
            if (link == null || link.isBlank() || ReplyPolicy.links(link).isEmpty()) {
                errors.add("extra_allowed_links: \"" + link + "\" không phải link hay tên miền hợp lệ");
            }
        }
        return errors;
    }
}
