package vn.slmglobal.slmai.services.media.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Nơi media nằm: file trong MinIO (UPLOAD) hoặc link ngoài (Facebook, YouTube, TikTok, slm_v2). Bảng media_nguon (V6).
 * Ảnh tải từ Google Drive: UPLOAD, {@code sourceUrl} = link Drive gốc, {@code externalId} = {@code gdrive:<file_id>}.
 */
@Entity
@Table(name = "media_nguon")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaSource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "media_id", nullable = false)
    private UUID mediaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_nguon", nullable = false)
    private SourceType sourceType;

    @Column(name = "url_nguon")
    private String sourceUrl;

    @Column(name = "id_ngoai")
    private String externalId;

    @Column(name = "ten_tac_gia")
    private String authorName;

    @Column(name = "bucket_luu_tru")
    private String storageBucket;

    @Column(name = "duong_dan_luu_tru")
    private String objectKey;

    @Column(name = "loai_mime")
    private String mimeType;

    @Column(name = "dung_luong_byte")
    private Long sizeBytes;

    @Column(name = "ma_bam_sha256")
    private String sha256;

    @Column(name = "chieu_rong")
    private Integer width;

    @Column(name = "chieu_cao")
    private Integer height;

    @Column(name = "thoi_luong_giay")
    private Integer durationSeconds;

    @Column(name = "url_anh_thu_nho")
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai_xu_ly", nullable = false)
    @Builder.Default
    private ProcessingStatus processingStatus = ProcessingStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai_truy_cap", nullable = false)
    @Builder.Default
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.UNKNOWN;

    @Column(name = "kiem_tra_cuoi_luc")
    private OffsetDateTime lastCheckedAt;

    @Column(name = "tao_luc", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    /** Do trigger dat_sua_luc (V1) cập nhật */
    @Column(name = "sua_luc", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}
