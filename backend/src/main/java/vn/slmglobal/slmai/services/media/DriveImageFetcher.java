package vn.slmglobal.slmai.services.media;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tải ảnh công khai từ Google Drive để lưu vào thư viện media.
 *
 * <p>An toàn với link do người dùng nhập:
 * <ul>
 *   <li>Chỉ tách file id từ link rồi tự dựng URL tải {@code /uc?export=download&id=...}; không tải đúng URL được đưa.</li>
 *   <li>Tự đi theo chuyển hướng, tối đa 5 lần, chỉ HTTPS tới drive.google.com / drive.usercontent.google.com.</li>
 *   <li>Đọc tối đa 15 MB; kiểm tra magic bytes JPEG / PNG / WebP (Drive trả trang HTML đăng nhập với mã 200 khi file
 *       chưa chia sẻ công khai).</li>
 * </ul>
 */
@Component
public class DriveImageFetcher {

    public static final long MAX_BYTES = 15L * 1024 * 1024;
    static final int MAX_REDIRECTS = 5;

    private static final Pattern FILE_ID = Pattern.compile("[A-Za-z0-9_-]{20,100}");
    private static final Pattern FROM_PATH = Pattern.compile("/d/([A-Za-z0-9_-]{20,100})");
    private static final Pattern FROM_QUERY = Pattern.compile("[?&]id=([A-Za-z0-9_-]{20,100})");
    private static final Set<String> LINK_HOSTS = Set.of("drive.google.com", "docs.google.com");

    /** Nơi tải và các host được phép đi tới; test dùng server cục bộ qua http. */
    public record Policy(String downloadBase, Set<String> allowedHosts, boolean requireHttps) {

        static Policy googleDrive() {
            return new Policy("https://drive.google.com",
                    Set.of("drive.google.com", "drive.usercontent.google.com"), true);
        }
    }

    public record Image(String fileId, byte[] bytes, String mimeType, String extension, String sha256,
                        Integer width, Integer height) {
    }

    /** Lỗi tải ảnh, thông điệp tiếng Việt để hiện cho người nhập */
    public static class DriveFetchException extends RuntimeException {
        public DriveFetchException(String message) {
            super(message);
        }
    }

    private final Policy policy;
    private final HttpClient http;

    public DriveImageFetcher() {
        this(Policy.googleDrive());
    }

    DriveImageFetcher(Policy policy) {
        this.policy = policy;
        this.http = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /** File id trong link Drive ({@code /file/d/<id>/view}, {@code open?id=<id>}...); rỗng nếu không phải link Drive. */
    public static Optional<String> extractFileId(String link) {
        if (link == null || link.isBlank()) {
            return Optional.empty();
        }
        URI uri;
        try {
            uri = URI.create(link.strip());
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        if (uri.getHost() == null || !LINK_HOSTS.contains(uri.getHost().toLowerCase(Locale.ROOT))) {
            return Optional.empty();
        }
        String raw = uri.getRawPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
        Matcher m = FROM_PATH.matcher(raw);
        if (m.find()) {
            return Optional.of(m.group(1));
        }
        m = FROM_QUERY.matcher(raw);
        return m.find() ? Optional.of(m.group(1)) : Optional.empty();
    }

    public Image fetch(String fileId) {
        if (fileId == null || !FILE_ID.matcher(fileId).matches()) {
            throw new DriveFetchException("Mã file Drive không hợp lệ");
        }
        URI uri = URI.create(policy.downloadBase() + "/uc?export=download&id=" + fileId);
        for (int hop = 0; hop <= MAX_REDIRECTS; hop++) {
            checkAllowed(uri);
            HttpResponse<InputStream> res = send(uri);
            int code = res.statusCode();
            if (code == 301 || code == 302 || code == 303 || code == 307 || code == 308) {
                String location = res.headers().firstValue("location")
                        .orElseThrow(() -> new DriveFetchException("Drive chuyển hướng nhưng không có địa chỉ"));
                closeQuietly(res.body());
                uri = uri.resolve(location);
                continue;
            }
            try (InputStream in = res.body()) {
                if (code != 200) {
                    throw new DriveFetchException("Drive trả HTTP " + code
                            + (code == 403 || code == 404 ? " (file chưa chia sẻ công khai hoặc đã bị xoá)" : ""));
                }
                String contentType = res.headers().firstValue("content-type").orElse("").toLowerCase(Locale.ROOT);
                if (contentType.startsWith("text/html")) {
                    throw new DriveFetchException(
                            "Drive trả trang web thay vì ảnh: file chưa chia sẻ \"Bất kỳ ai có đường liên kết\"");
                }
                byte[] bytes = readLimited(in);
                Format format = sniff(bytes);
                int[] size = dimensions(bytes);
                return new Image(fileId, bytes, format.mimeType, format.extension, sha256(bytes),
                        size == null ? null : size[0], size == null ? null : size[1]);
            } catch (IOException e) {
                throw new DriveFetchException("Lỗi đọc dữ liệu từ Drive: " + e.getMessage());
            }
        }
        throw new DriveFetchException("Drive chuyển hướng quá " + MAX_REDIRECTS + " lần");
    }

    private HttpResponse<InputStream> send(URI uri) {
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(30))
                .header("User-Agent", "slm-ai-media-import")
                .GET()
                .build();
        try {
            return http.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (IOException e) {
            throw new DriveFetchException("Không kết nối được Drive: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DriveFetchException("Bị huỷ khi đang tải từ Drive");
        }
    }

    private void checkAllowed(URI uri) {
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        boolean schemeOk = policy.requireHttps() ? scheme.equals("https") : scheme.equals("https") || scheme.equals("http");
        boolean portOk = !policy.requireHttps() || uri.getPort() == -1 || uri.getPort() == 443;
        if (!schemeOk || !portOk || !policy.allowedHosts().contains(host)) {
            throw new DriveFetchException("Drive chuyển hướng tới địa chỉ không được phép: " + host);
        }
    }

    private static byte[] readLimited(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[64 * 1024];
        long total = 0;
        int n;
        while ((n = in.read(buf)) != -1) {
            total += n;
            if (total > MAX_BYTES) {
                throw new DriveFetchException("Ảnh lớn hơn " + (MAX_BYTES / 1024 / 1024) + " MB");
            }
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }

    private enum Format {
        JPEG("image/jpeg", "jpg"), PNG("image/png", "png"), WEBP("image/webp", "webp");

        final String mimeType;
        final String extension;

        Format(String mimeType, String extension) {
            this.mimeType = mimeType;
            this.extension = extension;
        }
    }

    private static Format sniff(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return Format.JPEG;
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return Format.PNG;
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return Format.WEBP;
        }
        throw new DriveFetchException("File trên Drive không phải ảnh JPEG / PNG / WebP");
    }

    /** Kích thước đọc từ header, không giải mã cả ảnh; WebP (ImageIO không hỗ trợ sẵn) thì null. */
    private static int[] dimensions(byte[] bytes) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                return null;
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(iis, true, true);
                return new int[]{reader.getWidth(0), reader.getHeight(0)};
            } finally {
                reader.dispose();
            }
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void closeQuietly(InputStream in) {
        try {
            in.close();
        } catch (IOException ignored) {
            // bỏ qua
        }
    }
}
