package vn.slmglobal.slmai.services.knowledge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.slmglobal.slmai.services.knowledge.entity.KnowledgeDoc;

import java.util.List;
import java.util.Optional;

public interface KnowledgeDocRepository extends JpaRepository<KnowledgeDoc, Long> {

    Optional<KnowledgeDoc> findByKey(String key);

    boolean existsByKey(String key);

    List<KnowledgeDoc> findAllByOrderByKeyAsc();
}
