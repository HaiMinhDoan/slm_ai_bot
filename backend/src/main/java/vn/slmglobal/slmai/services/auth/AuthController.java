package vn.slmglobal.slmai.services.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.slmglobal.slmai.common.security.JwtService;
import vn.slmglobal.slmai.common.security.RequestContext;
import vn.slmglobal.slmai.common.security.RequireAuth;
import vn.slmglobal.slmai.common.security.RoleCode;
import vn.slmglobal.slmai.common.web.ApiResponses;
import vn.slmglobal.slmai.common.web.ResponseData;
import vn.slmglobal.slmai.services.auth.dto.AuthTokenResponse;
import vn.slmglobal.slmai.services.auth.dto.ChangePasswordRequest;
import vn.slmglobal.slmai.services.auth.dto.LoginRequest;
import vn.slmglobal.slmai.services.auth.dto.RefreshTokenRequest;
import vn.slmglobal.slmai.services.auth.dto.UserProfileResponse;

@Tag(name = "Xác thực", description = "Đăng nhập, làm mới token, đăng xuất, đổi mật khẩu (người dùng nội bộ)")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Đăng nhập")
    @PostMapping("/login")
    public ResponseEntity<ResponseData<AuthTokenResponse>> login(@Valid @RequestBody LoginRequest request,
                                                                 HttpServletRequest http) {
        return ApiResponses.ok(authService.login(request, http.getHeader("User-Agent"), http.getRemoteAddr()),
                "AUTH_LOGIN_SUCCESS");
    }

    @Operation(summary = "Làm mới access token", description = "Refresh token được xoay: token cũ bị thu hồi ngay khi cấp token mới.")
    @PostMapping("/refresh")
    public ResponseEntity<ResponseData<AuthTokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request,
                                                                   HttpServletRequest http) {
        return ApiResponses.ok(
                authService.refresh(request.refreshToken(), http.getHeader("User-Agent"), http.getRemoteAddr()),
                "AUTH_REFRESH_SUCCESS");
    }

    @Operation(summary = "Thông tin người dùng đang đăng nhập")
    @RequireAuth(roles = RoleCode.ALL)
    @GetMapping("/me")
    public ResponseEntity<ResponseData<UserProfileResponse>> me() {
        return ApiResponses.ok(authService.getProfile(RequestContext.currentUserId()), "AUTH_PROFILE_SUCCESS");
    }

    @Operation(summary = "Đăng xuất", description = "Thu hồi access token đang dùng và refresh token gửi kèm.")
    @RequireAuth(roles = RoleCode.ALL)
    @PostMapping("/logout")
    public ResponseEntity<ResponseData<Void>> logout(@RequestBody(required = false) RefreshTokenRequest request,
                                                     HttpServletRequest http) {
        authService.logout(JwtService.extractBearer(http.getHeader("Authorization")),
                request == null ? null : request.refreshToken());
        return ApiResponses.ok(null, "AUTH_LOGOUT_SUCCESS");
    }

    @Operation(summary = "Đổi mật khẩu", description = "Đổi thành công thì mọi phiên đăng nhập khác phải đăng nhập lại.")
    @RequireAuth(roles = RoleCode.ALL)
    @PostMapping("/change-password")
    public ResponseEntity<ResponseData<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(RequestContext.currentUserId(), request);
        return ApiResponses.ok(null, "AUTH_CHANGE_PASSWORD_SUCCESS");
    }
}
