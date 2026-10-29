package vn.slmglobal.slmai.services.media.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import vn.slmglobal.slmai.services.media.entity.MediaItem;

import java.util.UUID;

public interface MediaItemRepository extends JpaRepository<MediaItem, UUID>, JpaSpecificationExecutor<MediaItem> {

    /** Số lớn nhất trong các mã danh mục dạng M07 (0 nếu chưa có) */
    @Query(value = """
            SELECT COALESCE(MAX(CAST(substring(ma_danh_muc FROM 2) AS INT)), 0) FROM media
            WHERE ma_danh_muc ~ '^M[0-9]+$'""", nativeQuery = true)
    int maxIndexNumber();
}
