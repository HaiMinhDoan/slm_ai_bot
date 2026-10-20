package vn.slmglobal.slmai.services.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Quản trị viên tạo tài khoản cho nhân viên. Không có đăng ký công khai. */
public record CreateUserRequest(
        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        @Size(max = 255) String email,
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 150) String fullName,
        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, max = 72, message = "Mật khẩu phải từ 8 đến 72 ký tự") String password,
        @NotEmpty(message = "Phải chọn ít nhất một vai trò") List<String> roleCodes) {
}
