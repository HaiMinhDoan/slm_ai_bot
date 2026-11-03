package vn.slmglobal.slmai.services.knowledge.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.slmglobal.slmai.services.knowledge.entity.KnowledgeRelease;

import java.util.List;
import java.util.Optional;

public interface KnowledgeReleaseRepository extends JpaRepository<KnowledgeRelease, Long> {

    Optional<KnowledgeRelease> findFirstByStatus(KnowledgeRelease.Status status);

    Optional<KnowledgeRelease> findByKbVersion(String kbVersion);

    List<KnowledgeRelease> findByStatus(KnowledgeRelease.Status status);

    Page<KnowledgeRelease> findAllByOrderByIdDesc(Pageable pageable);

    /** Id bản đang chạy: kiểm tra rẻ mỗi 30 giây để biết có cần nạp lại không */
    @Query("SELECT r.id FROM KnowledgeRelease r WHERE r.status = vn.slmglobal.slmai.services.knowledge.entity.KnowledgeRelease.Status.ACTIVE")
    Optional<Long> activeId();
}
