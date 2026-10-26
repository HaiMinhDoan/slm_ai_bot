package vn.slmglobal.slmai.services.media.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.slmglobal.slmai.services.media.entity.MediaSource;
import vn.slmglobal.slmai.services.media.entity.SourceType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MediaSourceRepository extends JpaRepository<MediaSource, UUID> {

    Optional<MediaSource> findFirstBySourceTypeAndExternalId(SourceType sourceType, String externalId);

    Optional<MediaSource> findFirstBySha256(String sha256);

    List<MediaSource> findByMediaIdIn(Collection<UUID> mediaIds);

    Optional<MediaSource> findByIdAndMediaId(UUID id, UUID mediaId);
}
