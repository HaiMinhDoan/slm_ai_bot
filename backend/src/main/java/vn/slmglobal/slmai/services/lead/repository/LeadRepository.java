package vn.slmglobal.slmai.services.lead.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.slmglobal.slmai.services.lead.entity.Lead;

import java.util.Optional;
import java.util.UUID;

public interface LeadRepository extends JpaRepository<Lead, UUID> {

    Optional<Lead> findByIdempotencyKey(String idempotencyKey);
}
