package vn.slmglobal.slmai.services.auth.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** Thay toàn bộ vai trò hiện có của người dùng bằng danh sách này. */
public record UserRolesRequest(@NotEmpty(message = "Phải giữ ít nhất một vai trò") List<String> roleCodes) {
}
