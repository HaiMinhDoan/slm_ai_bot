package vn.slmglobal.slmai.common.security;

import java.util.Set;
import java.util.UUID;

/** Người dùng đang gọi API, lấy từ access token. */
public record AuthInfo(UUID id, String email, String fullName, Set<String> roles) {

    public boolean hasAnyRole(String... codes) {
        for (String code : codes) {
            if (roles.contains(code)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAllRoles(String... codes) {
        for (String code : codes) {
            if (!roles.contains(code)) {
                return false;
            }
        }
        return true;
    }
}
