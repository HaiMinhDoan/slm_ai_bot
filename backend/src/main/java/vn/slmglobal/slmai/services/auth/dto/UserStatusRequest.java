package vn.slmglobal.slmai.services.auth.dto;

import jakarta.validation.constraints.NotNull;
import vn.slmglobal.slmai.services.auth.entity.UserStatus;

/** {@code lockReason} nên có khi khóa, để lại lịch sử vì sao khóa. */
public record UserStatusRequest(
        @NotNull(message = "Thiếu trạng thái tài khoản") UserStatus status,
        String lockReason) {
}
