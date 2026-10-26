package vn.slmglobal.slmai.common.storage;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import vn.slmglobal.slmai.common.config.AppProperties;

import java.io.InputStream;
import java.time.Duration;

/**
 * Thao tác với MinIO / S3 cho Media Library (mục 6).
 * Bucket để riêng tư; người dùng admin tải lên qua presigned URL, khách xem media qua tên miền HTTPS
 * (reverse proxy trước MinIO, mục 6.4), không bao giờ qua http://IP:9000.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final MinioClient minioClient;
    private final AppProperties appProperties;

    /** Tạo bucket nếu chưa có. Lỗi chỉ ghi log để ứng dụng vẫn khởi động được khi MinIO tạm thời không vào được. */
    @EventListener(ApplicationReadyEvent.class)
    public void ensureBucket() {
        String bucket = bucket();
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Đã tạo bucket MinIO: {}", bucket);
            }
        } catch (Exception e) {
            log.warn("Chưa kết nối được MinIO ({}) để kiểm tra bucket {}: {}",
                    appProperties.minio().endpoint(), bucket, e.getMessage());
        }
    }

    public String bucket() {
        return appProperties.minio().bucket();
    }

    /** MinIO vào được, bucket đã có và đã cấu hình khoá truy cập: kiểm tra trước khi tải file về rồi mới lưu. */
    public boolean isAvailable() {
        AppProperties.Minio minio = appProperties.minio();
        if (minio.accessKey() == null || minio.accessKey().isBlank()) {
            return false;
        }
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket()).build());
        } catch (Exception e) {
            log.warn("MinIO chưa sẵn sàng ({}): {}", minio.endpoint(), e.getMessage());
            return false;
        }
    }

    /** Đọc object (người gọi phải đóng stream). */
    public InputStream get(String objectKey) throws Exception {
        return minioClient.getObject(GetObjectArgs.builder().bucket(bucket()).object(objectKey).build());
    }

    public void put(String objectKey, InputStream stream, long size, String contentType) throws Exception {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket())
                .object(objectKey)
                .stream(stream, size, -1)
                .contentType(contentType)
                .build());
    }

    public StatObjectResponse stat(String objectKey) throws Exception {
        return minioClient.statObject(StatObjectArgs.builder().bucket(bucket()).object(objectKey).build());
    }

    public void delete(String objectKey) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket()).object(objectKey).build());
    }

    /** URL tải lên có thời hạn: trình duyệt upload thẳng lên MinIO, không đi qua backend (mục 6.3 A). */
    public String presignedUploadUrl(String objectKey, Duration expiry) throws Exception {
        return presigned(Method.PUT, objectKey, expiry);
    }

    public String presignedDownloadUrl(String objectKey, Duration expiry) throws Exception {
        return presigned(Method.GET, objectKey, expiry);
    }

    private String presigned(Method method, String objectKey, Duration expiry) throws Exception {
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(method)
                .bucket(bucket())
                .object(objectKey)
                .expiry((int) expiry.toSeconds())
                .build());
    }
}
