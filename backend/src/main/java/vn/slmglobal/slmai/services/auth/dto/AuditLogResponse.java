package vn.slmglobal.slmai.services.auth.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID actorId,
        String actorName,
        String action,
        String targetType,
        String targetKey,
        JsonNode beforeData,
        JsonNode afterData,
        String ipAddress,
        OffsetDateTime createdAt) {
}
