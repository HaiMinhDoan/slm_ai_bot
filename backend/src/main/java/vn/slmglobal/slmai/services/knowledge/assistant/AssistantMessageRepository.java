package vn.slmglobal.slmai.services.knowledge.assistant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssistantMessageRepository extends JpaRepository<AssistantMessage, Long> {

    List<AssistantMessage> findBySessionIdOrderByIdAsc(UUID sessionId);
}
