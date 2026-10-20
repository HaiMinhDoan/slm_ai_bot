package vn.slmglobal.slmai.services.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.common.security.AuthInfo;
import vn.slmglobal.slmai.common.security.JwtService;
import vn.slmglobal.slmai.common.security.TokenBlacklist;
import vn.slmglobal.slmai.common.util.HashUtil;
import vn.slmglobal.slmai.services.auth.dto.AuthTokenResponse;
import vn.slmglobal.slmai.services.auth.dto.ChangePasswordRequest;
import vn.slmglobal.slmai.services.auth.dto.LoginRequest;
import vn.slmglobal.slmai.services.auth.dto.UserProfileResponse;
import vn.slmglobal.slmai.services.auth.entity.RefreshToken;
import vn.slmglobal.slmai.services.auth.entity.User;
import vn.slmglobal.slmai.services.auth.entity.UserStatus;
import vn.slmglobal.slmai.services.auth.repository.RefreshTokenRepository;
import vn.slmglobal.slmai.services.auth.repository.UserRepository;
import vn.slmglobal.slmai.services.auth.repository.UserRoleRepository;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Đăng nhập, làm mới token (xoay vòng), đăng xuất, đổi mật khẩu cho người dùng nội bộ. */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String HOME_PATH = "/dashboard";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final TokenBlacklist tokenBlacklist;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Transactional
    public AuthTokenResponse login(LoginRequest request, String userAgent, String ipAddress) {
        User user = userRepository.findByEmailNormalized(normalizeEmail(request.email()))
                .orElseThrow(AuthService::invalidCredentials);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }
        assertActive(user);

        user.setLastLoginAt(OffsetDateTime.now());
        return issueTokens(user, userAgent, ipAddress);
    }

    /** Xoay refresh token: token vừa dùng bị thu hồi ngay, nên một token bị lộ chỉ dùng được một lần. */
    @Transactional
    public AuthTokenResponse refresh(String rawRefreshToken, String userAgent, String ipAddress) {
        jwtService.verifyRefreshToken(rawRefreshToken)
                .orElseThrow(() -> CommonException.unauthorized("Refresh token không hợp lệ hoặc đã hết hạn"));

        RefreshToken stored = refreshTokenRepository
                .findByTokenHashAndRevokedAtIsNull(HashUtil.sha256Hex(rawRefreshToken))
                .orElseThrow(() -> CommonException.unauthorized("Refresh token đã bị thu hồi, vui lòng đăng nhập lại"));
        if (stored.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw CommonException.unauthorized("Refresh token đã hết hạn, vui lòng đăng nhập lại");
        }

        User user = stored.getUser();
        assertActive(user);

        stored.setRevokedAt(OffsetDateTime.now());
        return issueTokens(user, userAgent, ipAddress);
    }

    /** Đưa access token vào danh sách thu hồi và thu hồi refresh token (nếu gửi kèm). */
    @Transactional
    public void logout(String accessToken, String rawRefreshToken) {
        jwtService.verifyAccessToken(accessToken)
                .ifPresent(t -> tokenBlacklist.revoke(t.jti(), t.expiresAt()));

        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(HashUtil.sha256Hex(rawRefreshToken))
                    .ifPresent(rt -> rt.setRevokedAt(OffsetDateTime.now()));
        }
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CommonException.notFound("Không tìm thấy người dùng"));
        return toProfile(user, userRoleRepository.findRoleCodesByUserId(userId));
    }

    /** Đổi mật khẩu xong thì mọi phiên đăng nhập cũ trên các thiết bị khác phải đăng nhập lại. */
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CommonException.notFound("Không tìm thấy người dùng"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new CommonException("Mật khẩu hiện tại không đúng");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        refreshTokenRepository.revokeAllByUserId(userId, OffsetDateTime.now());
        auditLogService.record(userId, "user.password.change", "nguoi_dung", userId, null, null);
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private AuthTokenResponse issueTokens(User user, String userAgent, String ipAddress) {
        List<String> roleCodes = userRoleRepository.findRoleCodesByUserId(user.getId());
        AuthInfo authInfo = new AuthInfo(user.getId(), user.getEmail(), user.getFullName(), new HashSet<>(roleCodes));

        String accessToken = jwtService.generateAccessToken(authInfo);
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        // Lưu băm, không lưu token gốc: lộ DB cũng không dùng được để đăng nhập
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(HashUtil.sha256Hex(refreshToken))
                .expiresAt(OffsetDateTime.now().plus(jwtService.getRefreshTokenTtl()))
                .userAgent(truncate(userAgent, 255))
                .ipAddress(truncate(ipAddress, 64))
                .build());

        return new AuthTokenResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenTtl().toSeconds(),
                toProfile(user, roleCodes));
    }

    private static UserProfileResponse toProfile(User user, List<String> roleCodes) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                null,
                null,
                HOME_PATH,
                roleCodes.stream().map(code -> new UserProfileResponse.RoleInfo(code, code)).toList(),
                user.getEmail(),
                user.getStatus(),
                user.getLastLoginAt());
    }

    private static void assertActive(User user) {
        if (user.getStatus() == UserStatus.LOCKED) {
            String reason = user.getLockReason() == null ? "" : ": " + user.getLockReason();
            throw CommonException.forbidden("Tài khoản đang bị khóa" + reason);
        }
        if (user.getStatus() == UserStatus.DISABLED) {
            throw CommonException.forbidden("Tài khoản đã bị vô hiệu hóa");
        }
    }

    /** Cùng một thông báo cho "email không tồn tại" và "sai mật khẩu", để không dò được email nào có tài khoản. */
    private static CommonException invalidCredentials() {
        return CommonException.unauthorized("Email hoặc mật khẩu không đúng");
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
