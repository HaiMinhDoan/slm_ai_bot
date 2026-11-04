package vn.slmglobal.slmai.services.catalog;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.slmglobal.slmai.common.exception.CommonException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Ảnh vật tư / gói / logo hãng của slm_v2 nằm trên MinIO chạy http (cổng 9000): trang admin chạy https không nhúng
 * thẳng được, nên đi qua backend như thư viện media. Chỉ tải đường dẫn đã đồng bộ vào dm_* (không nhận URL từ trình
 * duyệt), có giới hạn thời gian và dung lượng.
 */
@Service
@RequiredArgsConstructor
public class CatalogImageService {

    static final int MAX_BYTES = 10 * 1024 * 1024;
    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final CatalogMaterialRepository repository;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(TIMEOUT)
            .followRedirects(HttpClient.Redirect.NEVER).build();

    public record Image(byte[] bytes, String contentType) {
    }

    public Image load(int fileId) {
        String url = repository.imageUrl(fileId)
                .orElseThrow(() -> CommonException.notFound("Không có ảnh " + fileId + " trong catalog đã đồng bộ"));
        URI uri = toUri(url);
        try {
            HttpResponse<InputStream> res = http.send(HttpRequest.newBuilder(uri).timeout(TIMEOUT).GET().build(),
                    HttpResponse.BodyHandlers.ofInputStream());
            try (InputStream in = res.body()) {
                if (res.statusCode() != 200) {
                    throw new CommonException(HttpStatus.BAD_GATEWAY,
                            "MinIO của slm_v2 trả HTTP " + res.statusCode() + " cho ảnh " + fileId);
                }
                byte[] bytes = in.readNBytes(MAX_BYTES + 1);
                if (bytes.length > MAX_BYTES) {
                    throw new CommonException(HttpStatus.BAD_GATEWAY, "Ảnh lớn hơn 10 MB");
                }
                String type = res.headers().firstValue("Content-Type").orElse("application/octet-stream");
                if (!type.startsWith("image/")) {
                    // MinIO hay ghi application/octet-stream cho file không có đuôi: đoán theo nội dung
                    type = sniff(bytes);
                }
                return new Image(bytes, type);
            }
        } catch (IOException e) {
            throw new CommonException(HttpStatus.BAD_GATEWAY, "Không tải được ảnh từ slm_v2: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommonException(HttpStatus.BAD_GATEWAY, "Bị ngắt khi tải ảnh từ slm_v2");
        }
    }

    /**
     * slm_v2 lưu đường dẫn chưa mã hoá (có dấu cách, chữ tiếng Việt: ".../slm/vat_tu_anh_Tấm pin JA 550 Wp_1764..."):
     * tách bằng URL rồi dựng lại URI để mã hoá phần đường dẫn theo UTF-8.
     */
    static URI toUri(String url) {
        try {
            URL u = new URL(url.strip());
            if (!u.getProtocol().matches("https?")) {
                throw new MalformedURLException(u.getProtocol());
            }
            URI raw = new URI(u.getProtocol(), u.getUserInfo(), u.getHost(), u.getPort(), u.getPath(), u.getQuery(),
                    null);
            return URI.create(raw.toASCIIString());
        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
            throw new CommonException(HttpStatus.BAD_GATEWAY, "Đường dẫn ảnh ở slm_v2 không hợp lệ");
        }
    }

    static String sniff(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8) {
            return "image/jpeg";
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "image/png";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F' && b[8] == 'W' && b[9] == 'E'
                && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        if (b.length >= 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F') {
            return "image/gif";
        }
        // Không phải ảnh: trả kiểu nhị phân, trình duyệt không hiển thị như trang
        return "application/octet-stream";
    }
}
