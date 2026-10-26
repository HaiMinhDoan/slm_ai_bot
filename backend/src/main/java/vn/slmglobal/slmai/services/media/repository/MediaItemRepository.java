package vn.slmglobal.slmai.services.media.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.slmglobal.slmai.services.media.entity.MediaItem;
import vn.slmglobal.slmai.services.media.entity.MediaType;

import java.util.UUID;

public interface MediaItemRepository extends JpaRepository<MediaItem, UUID> {

    Page<MediaItem> findByTypeOrderByCreatedAtDesc(MediaType type, Pageable pageable);

    Page<MediaItem> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
