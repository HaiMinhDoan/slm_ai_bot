package vn.slmglobal.slmai.services.knowledge.assistant;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssistantSessionRepository extends JpaRepository<AssistantSession, UUID> {

    List<AssistantSession> findByUserIdAndTypeOrderByCreatedAtDesc(String userId, String type, Pageable pageable);
}
