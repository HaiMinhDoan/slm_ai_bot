package vn.slmglobal.slmai.services.knowledge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import vn.slmglobal.slmai.common.util.HashUtil;
import vn.slmglobal.slmai.services.ai.AiProperties;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Bộ kiến thức cho AI trả lời khách. Bản đầu đọc Markdown trong classpath {@code knowledge/**}; khi có bảng kt_*
 * và Knowledge Editor (mục 5.9) thì đọc bản phát hành trong CSDL, giữ nguyên cách ghép và cách tính phiên bản.
 *
 * <p>Thứ tự ghép cố định theo đường dẫn để khối kiến thức không đổi giữa các lượt (prompt cache, mục 5.4).
 */
@Slf4j
@Component
public class KnowledgeBase {

    /** Chỗ thay tên công ty trong tài liệu và prompt */
    public static final String COMPANY_PLACEHOLDER = "{{ten_cong_ty}}";

    public record Document(String key, String content) {
    }

    private final List<Document> documents;
    private final String rendered;
    private final String version;

    public KnowledgeBase(AiProperties properties) {
        this.documents = load(properties.companyName());
        StringBuilder sb = new StringBuilder();
        for (Document d : documents) {
            sb.append("<tai_lieu ma=\"").append(d.key()).append("\">\n")
                    .append(d.content().strip())
                    .append("\n</tai_lieu>\n\n");
        }
        this.rendered = sb.toString().strip();
        this.version = HashUtil.sha256Hex(rendered);
        log.info("Nạp {} tài liệu kiến thức, phiên bản {}", documents.size(), version.substring(0, 12));
    }

    public List<Document> documents() {
        return documents;
    }

    /** Toàn bộ tài liệu, mỗi tài liệu trong một thẻ {@code <tai_lieu ma="...">} */
    public String rendered() {
        return rendered;
    }

    /** sha256 của khối kiến thức (luot_goi_ai.phien_ban_kt) */
    public String version() {
        return version;
    }

    /** Đọc một tài nguyên văn bản trong classpath, thay tên công ty. */
    public static String readClasspathText(String location, String companyName) {
        try {
            Resource resource = new PathMatchingResourcePatternResolver().getResource("classpath:" + location);
            return substitute(resource.getContentAsString(StandardCharsets.UTF_8), companyName);
        } catch (IOException e) {
            throw new UncheckedIOException("Không đọc được " + location, e);
        }
    }

    private static List<Document> load(String companyName) {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath*:knowledge/**/*.md");
            List<Document> out = new ArrayList<>();
            for (Resource r : resources) {
                String key = keyOf(r);
                if (key != null) {
                    out.add(new Document(key, substitute(r.getContentAsString(StandardCharsets.UTF_8), companyName)));
                }
            }
            out.sort(Comparator.comparing(Document::key));
            return List.copyOf(out);
        } catch (IOException e) {
            throw new UncheckedIOException("Không đọc được thư mục knowledge/", e);
        }
    }

    /** Mã tài liệu = đường dẫn dưới knowledge/, bỏ đuôi .md (ví dụ playbooks/khach-hoi-gia) */
    private static String keyOf(Resource r) throws IOException {
        String url = r.getURL().toString();
        int i = url.lastIndexOf("/knowledge/");
        if (i < 0 || !url.endsWith(".md")) {
            return null;
        }
        return url.substring(i + "/knowledge/".length(), url.length() - ".md".length());
    }

    private static String substitute(String text, String companyName) {
        String name = companyName == null || companyName.isBlank() ? "công ty" : companyName.strip();
        return text.replace(COMPANY_PLACEHOLDER, name);
    }
}
