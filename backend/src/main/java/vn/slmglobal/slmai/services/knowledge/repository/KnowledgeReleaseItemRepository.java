package vn.slmglobal.slmai.services.knowledge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.slmglobal.slmai.services.knowledge.entity.KnowledgeReleaseItem;

import java.util.List;

public interface KnowledgeReleaseItemRepository extends JpaRepository<KnowledgeReleaseItem, KnowledgeReleaseItem.Key> {

    List<KnowledgeReleaseItem> findByReleaseId(Long releaseId);
}
