package vn.slmglobal.slmai.services.knowledge.assistant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChangeProposalRepository extends JpaRepository<ChangeProposal, Long> {

    List<ChangeProposal> findBySessionIdOrderByIdAsc(UUID sessionId);

    List<ChangeProposal> findBySessionIdAndStatus(UUID sessionId, ChangeProposal.Status status);
}
