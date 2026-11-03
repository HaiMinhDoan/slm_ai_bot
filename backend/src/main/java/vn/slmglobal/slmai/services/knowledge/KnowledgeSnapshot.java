package vn.slmglobal.slmai.services.knowledge;

import vn.slmglobal.slmai.services.knowledge.rules.RuleSet;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Kiến thức và rule của bản phát hành đang chạy (mục 5.3), bất biến. Một lượt AI lấy đúng một snapshot rồi dùng cho
 * cả prompt, kiểm tra con số và ghi {@code luot_goi_ai.phien_ban_kt}, để phiên bản ghi lại khớp nội dung đã gửi.
 *
 * @param releaseId id bản phát hành; null khi chạy bằng kiến thức gốc trong repo (chưa có CSDL / chưa nhập)
 * @param version   kt_phat_hanh.phien_ban_kt: hash các phiên bản trong bản phát hành
 * @param docs      mọi tài liệu của bản phát hành, xếp theo mã
 * @param rules     các rule đã đọc
 */
public record KnowledgeSnapshot(Long releaseId, String version, List<Doc> docs, RuleSet rules) {

    /** Mã tài liệu hướng dẫn hệ thống cho AI trả lời khách */
    public static final String PROMPT_KEY = "prompts/customer-chat";

    public record Doc(String key, KnowledgeDocType type, String title, String content) {
    }

    public KnowledgeSnapshot {
        docs = docs.stream().sorted(Comparator.comparing(Doc::key)).toList();
    }

    public Optional<Doc> doc(String key) {
        return docs.stream().filter(d -> d.key().equals(key)).findFirst();
    }

    /** Tài liệu bot chăm sóc khách đọc trong prompt (core, playbooks, policies, faq, examples), xếp theo mã */
    public List<Doc> botDocs() {
        return docs.stream().filter(d -> d.type().botReads()).toList();
    }

    /** Khối kiến thức trong prompt: mỗi tài liệu một thẻ {@code <tai_lieu ma="...">} */
    public String renderKnowledge(String companyName, String personaName) {
        StringBuilder sb = new StringBuilder();
        for (Doc d : botDocs()) {
            sb.append("<tai_lieu ma=\"").append(d.key()).append("\">\n")
                    .append(substitute(d.content(), companyName, personaName).strip())
                    .append("\n</tai_lieu>\n\n");
        }
        return sb.toString().strip();
    }

    /** Hướng dẫn hệ thống cho AI (prompts/customer-chat); rỗng nếu bản phát hành thiếu tài liệu này */
    public String renderPrompt(String companyName, String personaName) {
        return doc(PROMPT_KEY).map(d -> substitute(d.content(), companyName, personaName).strip()).orElse("");
    }

    static String substitute(String text, String companyName, String personaName) {
        return text.replace(KnowledgeBase.COMPANY_PLACEHOLDER, orDefault(companyName, "công ty"))
                .replace(KnowledgeBase.PERSONA_PLACEHOLDER, orDefault(personaName, "em"));
    }

    private static String orDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.strip();
    }
}
