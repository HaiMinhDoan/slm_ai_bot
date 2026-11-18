package vn.slmglobal.slmai.adapters.gotenberg;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * Chụp một trang HTML thành ảnh JPEG qua Gotenberg: POST /forms/chromium/screenshot/html, gửi index.html cùng các tệp
 * nó dùng (css, font, ảnh) theo tên tệp; ảnh lấy hết chiều cao trang. Trang chỉ dùng tệp gửi kèm, không tải gì từ mạng.
 */
@Component
public class GotenbergClient {

    private final GotenbergProperties properties;
    private final RestClient restClient;

    public GotenbergClient(RestClient.Builder builder, GotenbergProperties properties) {
        this.properties = properties;
        Duration timeout = properties.timeoutOrDefault();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build());
        factory.setReadTimeout(timeout);
        this.restClient = builder.baseUrl(properties.enabled() ? properties.url().strip() : "http://localhost")
                .requestFactory(factory)
                .build();
    }

    public boolean enabled() {
        return properties.enabled();
    }

    /**
     * @param html   nội dung index.html
     * @param assets tệp đi kèm, khoá là tên tệp trang HTML tham chiếu
     * @param width  chiều rộng trang (px)
     */
    public byte[] screenshotJpeg(String html, Map<String, byte[]> assets, int width) {
        if (!enabled()) {
            throw new IllegalStateException("Chưa cấu hình GOTENBERG_URL");
        }
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("files", file(html.getBytes(StandardCharsets.UTF_8), "index.html", MediaType.TEXT_HTML));
        assets.forEach((name, bytes) -> body.add("files", file(bytes, name, MediaType.APPLICATION_OCTET_STREAM)));
        body.add("width", String.valueOf(width));
        // Chiều cao khung nhìn ban đầu; ảnh vẫn lấy hết trang vì không cắt (clip = false)
        body.add("height", "1000");
        body.add("format", "jpeg");
        body.add("quality", "90");
        byte[] out = restClient.post()
                .uri("/forms/chromium/screenshot/html")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new IllegalStateException("Gotenberg trả HTTP " + res.getStatusCode().value() + ": "
                            + new String(res.getBody().readNBytes(500), StandardCharsets.UTF_8));
                })
                .body(byte[].class);
        if (out == null || out.length == 0) {
            throw new IllegalStateException("Gotenberg không trả ảnh");
        }
        return out;
    }

    /** Một phần tệp của form multipart (không dùng MultipartBodyBuilder: lớp đó cần thư viện reactive-streams) */
    static HttpEntity<ByteArrayResource> file(byte[] bytes, String name, MediaType type) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(type);
        headers.setContentDispositionFormData("files", name);
        return new HttpEntity<>(new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return name;
            }
        }, headers);
    }
}
