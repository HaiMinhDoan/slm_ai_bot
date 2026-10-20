package vn.slmglobal.slmai.services.auth.dto;

import vn.slmglobal.slmai.services.auth.entity.UserStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Người dùng đang đăng nhập. Các trường userId, username, realName, avatar, desc, homePath, roles
 * khớp GetUserInfoModel của Vben để store người dùng của Vben dùng ngay.
 */
public record UserProfileResponse(
        UUID userId,
        String username,
        String realName,
        String avatar,
        String desc,
        String homePath,
        List<RoleInfo> roles,
        String email,
        UserStatus status,
        OffsetDateTime lastLoginAt) {

    /** Vben đọc roles[].value để phân quyền menu. */
    public record RoleInfo(String roleName, String value) {
    }
}
