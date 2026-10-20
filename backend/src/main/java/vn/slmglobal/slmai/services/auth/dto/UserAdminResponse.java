package vn.slmglobal.slmai.services.auth.dto;

import vn.slmglobal.slmai.services.auth.entity.UserStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record UserAdminResponse(
        UUID id,
        String email,
        String fullName,
        UserStatus status,
        String lockReason,
        List<String> roleCodes,
        OffsetDateTime lastLoginAt,
        OffsetDateTime createdAt) {
}
