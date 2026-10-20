package vn.slmglobal.slmai.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/** Cấu hình riêng của ứng dụng, khối {@code app:} trong application.yml. */
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Cors cors,
        BootstrapAdmin bootstrapAdmin,
        Minio minio
) {

    public record Jwt(String signerKey, Duration accessTokenTtl, Duration refreshTokenTtl) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record BootstrapAdmin(String email, String password, String fullName) {
    }

    public record Minio(String endpoint, String publicUrl, String accessKey, String secretKey, String bucket) {
    }
}
