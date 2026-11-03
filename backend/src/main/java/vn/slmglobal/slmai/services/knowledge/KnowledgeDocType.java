package vn.slmglobal.slmai.services.knowledge;

import java.util.Arrays;
import java.util.Optional;

/**
 * Loại tài liệu kiến thức (kt_tai_lieu.loai_tai_lieu, mục 5.1, 5.2). Mã tài liệu bắt đầu bằng thư mục của loại:
 * {@code playbooks/khach-hoi-gia}, {@code rules/bao-gia}.
 *
 * @param folder   thư mục đầu của mã tài liệu
 * @param yaml     nội dung YAML (rule) thay vì Markdown
 * @param botReads bot chăm sóc khách đọc trong prompt (khối kiến thức)
 * @param label    tên hiển thị trên admin
 */
public enum KnowledgeDocType {
    CORE("core", false, true, "Cốt lõi"),
    PLAYBOOK("playbooks", false, true, "Cách tư vấn"),
    POLICY("policies", false, true, "Chính sách"),
    FAQ("faq", false, true, "Hỏi đáp"),
    EXAMPLE("examples", false, true, "Chat mẫu"),
    /** Hướng dẫn hệ thống cho AI (prompts/customer-chat): nằm trước khối kiến thức */
    PROMPT("prompts", false, false, "Hướng dẫn hệ thống"),
    RULE("rules", true, false, "Quy tắc"),
    GUIDE("guide", false, false, "Hướng dẫn soạn"),
    /** Mẫu tài liệu mới (guide/mau/faq...) */
    TEMPLATE("guide/mau", false, false, "Mẫu tài liệu"),
    CONTENT("content", false, false, "Nội dung đăng bài");

    private final String folder;
    private final boolean yaml;
    private final boolean botReads;
    private final String label;

    KnowledgeDocType(String folder, boolean yaml, boolean botReads, String label) {
        this.folder = folder;
        this.yaml = yaml;
        this.botReads = botReads;
        this.label = label;
    }

    public String folder() {
        return folder;
    }

    public boolean yaml() {
        return yaml;
    }

    public boolean botReads() {
        return botReads;
    }

    public String label() {
        return label;
    }

    public DocFormat format() {
        return yaml ? DocFormat.YAML : DocFormat.MARKDOWN;
    }

    /** Loại theo mã tài liệu: thư mục dài nhất khớp trước (guide/mau trước guide) */
    public static Optional<KnowledgeDocType> ofKey(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(t -> key.startsWith(t.folder + "/"))
                .max((a, b) -> Integer.compare(a.folder.length(), b.folder.length()));
    }

    public enum DocFormat {
        MARKDOWN,
        YAML
    }
}
