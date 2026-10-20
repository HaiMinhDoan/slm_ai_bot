package vn.slmglobal.slmai.services.auth.dto;

/**
 * Kết quả đăng nhập / làm mới token.
 * Tên {@code token} (không phải accessToken) để khớp LoginResultModel của Vben.
 */
public record AuthTokenResponse(
        String token,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserProfileResponse user) {
}
