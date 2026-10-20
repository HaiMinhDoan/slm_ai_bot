package vn.slmglobal.slmai.services.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(@NotBlank(message = "Thiếu refresh token") String refreshToken) {
}
