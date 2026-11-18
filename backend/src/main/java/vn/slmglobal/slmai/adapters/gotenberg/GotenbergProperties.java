package vn.slmglobal.slmai.adapters.gotenberg;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Dịch vụ Gotenberg (Chromium chạy sẵn trong container riêng) để chụp trang HTML thành ảnh, khối {@code app.gotenberg:}
 * trong application.yml. Dùng cho ảnh báo giá gói tự tạo.
 *
 * @param url     ví dụ http://slm-ai-gotenberg:3000; để trống = không tạo ảnh báo giá
 * @param timeout thời gian chờ một lần chụp
 */
@ConfigurationProperties(prefix = "app.gotenberg")
public record GotenbergProperties(String url, Duration timeout) {

    public boolean enabled() {
        return url != null && !url.isBlank();
    }

    public Duration timeoutOrDefault() {
        return timeout == null ? Duration.ofSeconds(60) : timeout;
    }
}
