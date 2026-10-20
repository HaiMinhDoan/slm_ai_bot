package vn.slmglobal.slmai.services.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.common.web.PageResponse;
import vn.slmglobal.slmai.services.auth.dto.CreateUserRequest;
import vn.slmglobal.slmai.services.auth.dto.RoleOption;
import vn.slmglobal.slmai.services.auth.dto.UserAdminResponse;
import vn.slmglobal.slmai.services.auth.dto.UserStatusRequest;
import vn.slmglobal.slmai.services.auth.entity.Role;
import vn.slmglobal.slmai.services.auth.entity.User;
import vn.slmglobal.slmai.services.auth.entity.UserRole;
import vn.slmglobal.slmai.services.auth.entity.UserStatus;
import vn.slmglobal.slmai.services.auth.repository.RefreshTokenRepository;
import vn.slmglobal.slmai.services.auth.repository.RoleRepository;
import vn.slmglobal.slmai.services.auth.repository.UserRepository;
import vn.slmglobal.slmai.services.auth.repository.UserRoleRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Quản trị tài khoản nội bộ: tạo, khóa / mở, đổi vai trò, đặt lại mật khẩu.
 * Mọi thay đổi đều ghi nhật ký thao tác. Không ai tự đổi vai trò / trạng thái của chính mình.
 */
@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<RoleOption> roles() {
        return roleRepository.findAllByOrderByCodeAsc().stream()
                .map(r -> new RoleOption(r.getCode(), r.getName(), r.getDescription()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<UserAdminResponse> filter(String keyword, UserStatus status, Pageable pageable) {
        String pattern = keyword == null || keyword.isBlank()
                ? null : "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
        return PageResponse.of(userRepository.adminFilter(pattern, status, pageable), this::toResponse);
    }

    @Transactional(readOnly = true)
    public UserAdminResponse detail(UUID id) {
        return toResponse(findUser(id));
    }

    @Transactional
    public UserAdminResponse create(UUID actorId, CreateUserRequest request) {
        if (userRepository.existsByEmailNormalized(AuthService.normalizeEmail(request.email()))) {
            throw CommonException.conflict("Email này đã có tài khoản");
        }
        User user = userRepository.save(User.builder()
                .email(request.email().trim())
                .fullName(request.fullName().trim())
                .passwordHash(passwordEncoder.encode(request.password()))
                .build());
        grantRoles(user, resolveRoles(request.roleCodes()), actorId);

        auditLogService.record(actorId, "user.create", "nguoi_dung", user.getId(), null,
                Map.of("email", user.getEmail(), "roleCodes", request.roleCodes()));
        return toResponse(user);
    }

    @Transactional
    public UserAdminResponse setStatus(UUID actorId, UUID id, UserStatusRequest request) {
        assertNotSelf(actorId, id, "Không thể tự đổi trạng thái tài khoản của chính mình");
        User user = findUser(id);

        UserStatus before = user.getStatus();
        user.setStatus(request.status());
        user.setLockReason(request.status() == UserStatus.ACTIVE ? null : request.lockReason());
        if (request.status() != UserStatus.ACTIVE) {
            // Khóa thì phải mất phiên ngay; access token còn hạn hết hiệu lực khi hết TTL
            refreshTokenRepository.revokeAllByUserId(id, OffsetDateTime.now());
        }

        auditLogService.record(actorId, "user.status.update", "nguoi_dung", id,
                Map.of("status", before),
                Map.of("status", request.status(), "lockReason", String.valueOf(request.lockReason())));
        return toResponse(user);
    }

    @Transactional
    public UserAdminResponse setRoles(UUID actorId, UUID id, List<String> roleCodes) {
        assertNotSelf(actorId, id, "Không thể tự đổi vai trò của chính mình, nhờ một quản trị viên khác thao tác");
        User user = findUser(id);
        List<Role> roles = resolveRoles(roleCodes);
        List<String> before = userRoleRepository.findRoleCodesByUserId(id);

        userRoleRepository.deleteAllByUserId(id);
        userRoleRepository.flush();
        grantRoles(user, roles, actorId);

        auditLogService.record(actorId, "user.roles.update", "nguoi_dung", id,
                Map.of("roleCodes", before), Map.of("roleCodes", roleCodes));
        return toResponse(user);
    }

    @Transactional
    public void resetPassword(UUID actorId, UUID id, String newPassword) {
        User user = findUser(id);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        refreshTokenRepository.revokeAllByUserId(id, OffsetDateTime.now());
        auditLogService.record(actorId, "user.password.reset", "nguoi_dung", id, null, null);
    }

    private void grantRoles(User user, List<Role> roles, UUID actorId) {
        User grantedBy = actorId == null ? null : userRepository.getReferenceById(actorId);
        for (Role role : roles) {
            userRoleRepository.save(UserRole.builder()
                    .id(new UserRole.Id(user.getId(), role.getId()))
                    .user(user)
                    .role(role)
                    .grantedBy(grantedBy)
                    .build());
        }
    }

    private List<Role> resolveRoles(List<String> roleCodes) {
        return roleCodes.stream()
                .distinct()
                .map(code -> roleRepository.findByCode(code)
                        .orElseThrow(() -> new CommonException("Không có vai trò: " + code)))
                .toList();
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> CommonException.notFound("Không tìm thấy người dùng"));
    }

    private static void assertNotSelf(UUID actorId, UUID targetId, String message) {
        if (targetId.equals(actorId)) {
            throw CommonException.forbidden(message);
        }
    }

    private UserAdminResponse toResponse(User u) {
        return new UserAdminResponse(
                u.getId(),
                u.getEmail(),
                u.getFullName(),
                u.getStatus(),
                u.getLockReason(),
                userRoleRepository.findRoleCodesByUserId(u.getId()),
                u.getLastLoginAt(),
                u.getCreatedAt());
    }
}
