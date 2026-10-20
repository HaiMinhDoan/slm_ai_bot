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
        MinioClient.Builder builder = MinioClient.builder().endpoint(minio.endpoint());
        // MinioClient từ chối key rỗng ngay lúc dựng; chưa cấu hình key thì dựng client không đăng nhập
        // để ứng dụng vẫn khởi động, lỗi chỉ xảy ra ở thao tác media.
        if (!isBlank(minio.accessKey()) && !isBlank(minio.secretKey())) {
            builder.credentials(minio.accessKey(), minio.secretKey());
        }
        return builder.build();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
