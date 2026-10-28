package vn.slmglobal.slmai.adapters.slmv2;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Kết nối chỉ đọc tới DB slm_v2 bằng tài khoản ai_sync (mục 4.2.5), khối {@code app.slm-v2:} trong application.yml.
 * Không cấu hình thì không đồng bộ catalog; bot vẫn chạy, chỉ không có công cụ báo giá.
 *
 * @param url            jdbc:postgresql://host:5432/slm_v2
 * @param connectTimeout chờ mở kết nối
 * @param socketTimeout  chờ một câu truy vấn
 */
@ConfigurationProperties(prefix = "app.slm-v2")
public record SlmV2Properties(String url, String username, String password, Duration connectTimeout,
                              Duration socketTimeout) {

    public SlmV2Properties {
        if (connectTimeout == null) {
            connectTimeout = Duration.ofSeconds(10);
        }
        if (socketTimeout == null) {
            socketTimeout = Duration.ofSeconds(120);
        }
    }

    public boolean isConfigured() {
        return url != null && !url.isBlank() && username != null && !username.isBlank();
    }
}
