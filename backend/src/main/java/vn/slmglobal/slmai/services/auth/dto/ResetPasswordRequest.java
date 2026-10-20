package vn.slmglobal.slmai.services.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Quản trị viên đặt lại mật khẩu cho nhân viên (thay cho quên mật khẩu qua email). */
public record ResetPasswordRequest(
        @NotBlank(message = "Mật khẩu mới không được để trống")
        @Size(min = 8, max = 72, message = "Mật khẩu phải từ 8 đến 72 ký tự") String newPassword) {
}
