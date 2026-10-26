package vn.slmglobal.slmai.services.media;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Tải ảnh Drive an toàn: dùng server HTTP cục bộ thay cho Google Drive. */
class DriveImageFetcherTest {

    private static final String FILE_ID = "1MJDFYHkEuHjNLrqY6e6wSq4Db1bbJqkb";

    private HttpServer server;
    private DriveImageFetcher fetcher;
    private final AtomicReference<Handler> handler = new AtomicReference<>();

    interface Handler {
        void handle(HttpExchange ex) throws IOException;
    }

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", ex -> {
            try {
                handler.get().handle(ex);
            } finally {
                ex.close();
            }
        });
        server.start();
        fetcher = new DriveImageFetcher(new DriveImageFetcher.Policy(
                "http://localhost:" + server.getAddress().getPort(), Set.of("localhost"), false));
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    // ---------- tách file id ----------

    @Test
    void extractsFileIdFromCommonDriveLinks() {
        assertThat(DriveImageFetcher.extractFileId(
                "https://drive.google.com/file/d/" + FILE_ID + "/view?usp=drive_link")).contains(FILE_ID);
        assertThat(DriveImageFetcher.extractFileId("https://drive.google.com/open?id=" + FILE_ID)).contains(FILE_ID);
        assertThat(DriveImageFetcher.extractFileId("https://drive.google.com/uc?export=download&id=" + FILE_ID))
                .contains(FILE_ID);
    }

    @Test
    void rejectsNonDriveOrMalformedLinks() {
        assertThat(DriveImageFetcher.extractFileId("https://evil.example/file/d/" + FILE_ID + "/view")).isEmpty();
        assertThat(DriveImageFetcher.extractFileId("https://drive.google.com/file/d/short/view")).isEmpty();
        assertThat(DriveImageFetcher.extractFileId("not a url")).isEmpty();
        assertThat(DriveImageFetcher.extractFileId(null)).isEmpty();
    }

    // ---------- tải ----------

    @Test
    void followsRedirectWithinAllowedHostsAndReadsJpeg() throws IOException {
        byte[] jpeg = jpeg(40, 30);
        handler.set(ex -> {
            if (ex.getRequestURI().getPath().equals("/uc")) {
                assertThat(ex.getRequestURI().getQuery()).isEqualTo("export=download&id=" + FILE_ID);
                redirect(ex, "/download?id=" + FILE_ID);
            } else {
                respond(ex, 200, "image/jpeg", jpeg);
            }
        });

        DriveImageFetcher.Image img = fetcher.fetch(FILE_ID);

        assertThat(img.mimeType()).isEqualTo("image/jpeg");
        assertThat(img.extension()).isEqualTo("jpg");
        assertThat(img.width()).isEqualTo(40);
        assertThat(img.height()).isEqualTo(30);
        assertThat(img.sha256()).hasSize(64);
    }

    @Test
    void refusesRedirectToOtherHost() {
        handler.set(ex -> redirect(ex, "http://127.0.0.1:" + server.getAddress().getPort() + "/x"));

        assertThatThrownBy(() -> fetcher.fetch(FILE_ID)).hasMessageContaining("không được phép");
    }

    @Test
    void stopsAfterTooManyRedirects() {
        handler.set(ex -> redirect(ex, "/again"));

        assertThatThrownBy(() -> fetcher.fetch(FILE_ID)).hasMessageContaining("chuyển hướng quá");
    }

    @Test
    void detectsDriveLoginPageServedWith200() {
        handler.set(ex -> respond(ex, 200, "text/html; charset=utf-8",
                "<html>Sign in</html>".getBytes(StandardCharsets.UTF_8)));

        assertThatThrownBy(() -> fetcher.fetch(FILE_ID)).hasMessageContaining("chưa chia sẻ");
    }

    @Test
    void rejectsNonImageBytes() {
        handler.set(ex -> respond(ex, 200, "application/octet-stream", "hello".getBytes(StandardCharsets.UTF_8)));

        assertThatThrownBy(() -> fetcher.fetch(FILE_ID)).hasMessageContaining("không phải ảnh");
    }

    @Test
    void rejectsOversizedFile() {
        byte[] big = new byte[(int) DriveImageFetcher.MAX_BYTES + 1];
        big[0] = (byte) 0xFF;
        big[1] = (byte) 0xD8;
        big[2] = (byte) 0xFF;
        handler.set(ex -> respond(ex, 200, "image/jpeg", big));

        assertThatThrownBy(() -> fetcher.fetch(FILE_ID)).hasMessageContaining("lớn hơn");
    }

    @Test
    void reportsPrivateFile() {
        handler.set(ex -> respond(ex, 404, "text/plain", "x".getBytes(StandardCharsets.UTF_8)));

        assertThatThrownBy(() -> fetcher.fetch(FILE_ID)).hasMessageContaining("404");
    }

    @Test
    void rejectsInvalidFileId() {
        assertThatThrownBy(() -> fetcher.fetch("../../etc/passwd")).hasMessageContaining("không hợp lệ");
    }

    // ---------- tiện ích ----------

    static byte[] jpeg(int w, int h) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB), "jpg", out);
        return out.toByteArray();
    }

    private static void redirect(HttpExchange ex, String location) throws IOException {
        ex.getResponseHeaders().add("Location", location);
        ex.sendResponseHeaders(302, -1);
    }

    private static void respond(HttpExchange ex, int status, String type, byte[] body) throws IOException {
        ex.getResponseHeaders().add("Content-Type", type);
        ex.sendResponseHeaders(status, body.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(body);
        }
    }
}
