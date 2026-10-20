package vn.slmglobal.slmai.services.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.slmglobal.slmai.common.config.AppProperties;
import vn.slmglobal.slmai.common.security.RoleCode;
import vn.slmglobal.slmai.services.auth.dto.CreateUserRequest;
import vn.slmglobal.slmai.services.auth.entity.UserStatus;
import vn.slmglobal.slmai.services.auth.repository.UserRepository;
import vn.slmglobal.slmai.services.auth.repository.UserRoleRepository;

import java.util.List;

/**
 * Tạo tài khoản SYSTEM_ADMIN đầu tiên từ ADMIN_EMAIL / ADMIN_PASSWORD khi CSDL chưa có quản trị viên nào đang hoạt động.
 * Đã có quản trị viên thì bỏ qua, nên để biến môi trường lại cũng không sao; nên xóa mật khẩu khỏi .env sau lần đầu.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final AppProperties appProperties;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserAdminService userAdminService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRoleRepository.existsUserWithRole(RoleCode.SYSTEM_ADMIN, UserStatus.ACTIVE)) {
            return;
        }
        AppProperties.BootstrapAdmin admin = appProperties.bootstrapAdmin();
        if (admin == null || isBlank(admin.email()) || isBlank(admin.password())) {
            log.warn("Chưa có tài khoản SYSTEM_ADMIN nào. Đặt ADMIN_EMAIL và ADMIN_PASSWORD rồi khởi động lại để tạo.");
            return;
        }
        if (userRepository.existsByEmailNormalized(AuthService.normalizeEmail(admin.email()))) {
            log.warn("Email {} đã có tài khoản nhưng không phải SYSTEM_ADMIN đang hoạt động; không tự cấp quyền.", admin.email());
            return;
        }
        userAdminService.create(null, new CreateUserRequest(
                admin.email(), admin.fullName(), admin.password(), List.of(RoleCode.SYSTEM_ADMIN)));
        log.info("Đã tạo tài khoản SYSTEM_ADMIN đầu tiên: {}", admin.email());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
