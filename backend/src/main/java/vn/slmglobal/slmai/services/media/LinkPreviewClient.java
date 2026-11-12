package vn.slmglobal.slmai.services.media;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.slmglobal.slmai.services.media.entity.SourceType;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/**
 * Tiêu đề, kênh và ảnh xem trước của video YouTube / TikTok qua oEmbed công khai (mục 6.3 B). Chỉ gọi hai địa chỉ
 * cố định, không theo chuyển hướng. Facebook cần app token nên không lấy; lỗi thì bỏ qua, admin tự nhập tiêu đề.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LinkPreviewClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(6);

    private final ObjectMapper objectMapper;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(TIMEOUT)
            .followRedirects(HttpClient.Redirect.NEVER).build();

    public record Preview(String title, String authorName, String thumbnailUrl) {
    }

    public Optional<Preview> preview(MediaLinkParser.Parsed link) {
        String endpoint = link.type() == SourceType.YOUTUBE ? "https://www.youtube.com/oembed?format=json&url="
                : link.type() == SourceType.TIKTOK ? "https://www.tiktok.com/oembed?url=" : null;
        if (endpoint == null) {
            return Optional.empty();
        }
        try {
            HttpResponse<String> res = http.send(HttpRequest.newBuilder(
                            URI.create(endpoint + URLEncoder.encode(link.url(), StandardCharsets.UTF_8)))
                    .timeout(TIMEOUT).header("Accept", "application/json").GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                return Optional.empty();
            }
            JsonNode n = objectMapper.readTree(res.body());
            return Optional.of(new Preview(text(n, "title"), text(n, "author_name"), httpsUrl(n.path("thumbnail_url"))));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception e) {
            log.info("Không lấy được thông tin link {}: {}", link.url(), e.getMessage());
            return Optional.empty();
        }
    }

    private static String text(JsonNode n, String field) {
        String v = n.path(field).asText("").strip();
        return v.isEmpty() ? null : v.length() > 300 ? v.substring(0, 300) : v;
    }

    /** Ảnh xem trước hiện thẳng trên trang admin: chỉ nhận https */
    private static String httpsUrl(JsonNode n) {
        String v = n.asText("").strip();
        return v.startsWith("https://") && v.length() <= 2000 ? v : null;
    }
}
