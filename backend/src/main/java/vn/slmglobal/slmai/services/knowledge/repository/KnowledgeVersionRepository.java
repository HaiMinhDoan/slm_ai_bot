package vn.slmglobal.slmai.services.knowledge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.slmglobal.slmai.services.knowledge.entity.KnowledgeVersion;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface KnowledgeVersionRepository extends JpaRepository<KnowledgeVersion, Long> {

    Optional<KnowledgeVersion> findByDocIdAndStatus(Long docId, KnowledgeVersion.Status status);

    List<KnowledgeVersion> findByDocIdOrderByVersionNoDesc(Long docId);

    List<KnowledgeVersion> findByStatus(KnowledgeVersion.Status status);

    List<KnowledgeVersion> findByIdIn(Collection<Long> ids);

    @Query("SELECT COALESCE(MAX(v.versionNo), 0) FROM KnowledgeVersion v WHERE v.docId = :docId")
    int maxVersionNo(@Param("docId") Long docId);
}
