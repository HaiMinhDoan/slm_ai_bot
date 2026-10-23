package vn.slmglobal.slmai.services.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.slmglobal.slmai.services.ai.entity.AiRun;

import java.util.UUID;

public interface AiRunRepository extends JpaRepository<AiRun, UUID> {
}
