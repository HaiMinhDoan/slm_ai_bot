package vn.slmglobal.slmai.common.storage;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.slmglobal.slmai.common.config.AppProperties;

@Configuration
public class MinioConfig {

    /**
     * Chỉ dựng client, không gọi mạng lúc khởi động: MinIO chưa sẵn sàng thì ứng dụng vẫn chạy,
     * lỗi chỉ xảy ra ở thao tác media. Bucket được kiểm tra bởi {@link StorageService#ensureBucket()}.
     */
    @Bean
    public MinioClient minioClient(AppProperties appProperties) {
        AppProperties.Minio minio = appProperties.minio();
        return MinioClient.builder()
                .endpoint(minio.endpoint())
                .credentials(nullToEmpty(minio.accessKey()), nullToEmpty(minio.secretKey()))
                .build();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
