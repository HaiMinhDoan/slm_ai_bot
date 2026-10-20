package vn.slmglobal.slmai.services.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.slmglobal.slmai.common.security.RequestContext;
import vn.slmglobal.slmai.common.security.RequireAuth;
import vn.slmglobal.slmai.common.security.RoleCode;
import vn.slmglobal.slmai.common.web.ApiResponses;
import vn.slmglobal.slmai.common.web.PageResponse;
import vn.slmglobal.slmai.common.web.ResponseData;
import vn.slmglobal.slmai.services.auth.dto.CreateUserRequest;
import vn.slmglobal.slmai.services.auth.dto.ResetPasswordRequest;
import vn.slmglobal.slmai.services.auth.dto.RoleOption;
import vn.slmglobal.slmai.services.auth.dto.UserAdminResponse;
import vn.slmglobal.slmai.services.auth.dto.UserRolesRequest;
import vn.slmglobal.slmai.services.auth.dto.UserStatusRequest;
import vn.slmglobal.slmai.services.auth.entity.UserStatus;

import java.util.List;
import java.util.UUID;

@Tag(name = "Quản trị - Người dùng", description = "Tài khoản nhân viên, vai trò, trạng thái, mật khẩu")
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;

    @Operation(summary = "Danh sách vai trò để gán")
    @RequireAuth(roles = RoleCode.SYSTEM_ADMIN)
    @GetMapping("/roles")
    public ResponseEntity<ResponseData<List<RoleOption>>> roles() {
        return ApiResponses.ok(userAdminService.roles(), "ROLE_LIST_SUCCESS");
    }

    @Operation(summary = "Lọc danh sách người dùng")
    @RequireAuth(roles = RoleCode.SYSTEM_ADMIN)
    @GetMapping
    public ResponseEntity<ResponseData<PageResponse<UserAdminResponse>>> filter(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponses.ok(userAdminService.filter(keyword, status, PageRequest.of(page, Math.min(size, 100))),
                "USER_LIST_SUCCESS");
    }

    @Operation(summary = "Chi tiết người dùng")
    @RequireAuth(roles = RoleCode.SYSTEM_ADMIN)
    @GetMapping("/{id}")
    public ResponseEntity<ResponseData<UserAdminResponse>> detail(@PathVariable UUID id) {
        return ApiResponses.ok(userAdminService.detail(id), "USER_DETAIL_SUCCESS");
    }

    @Operation(summary = "Tạo tài khoản nhân viên")
    @RequireAuth(roles = RoleCode.SYSTEM_ADMIN)
    @PostMapping
    public ResponseEntity<ResponseData<UserAdminResponse>> create(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponses.ok(userAdminService.create(RequestContext.currentUserId(), request), "USER_CREATED");
    }

    @Operation(summary = "Đổi trạng thái tài khoản (mở / khóa / vô hiệu hóa)")
    @RequireAuth(roles = RoleCode.SYSTEM_ADMIN)
    @PutMapping("/{id}/status")
    public ResponseEntity<ResponseData<UserAdminResponse>> setStatus(@PathVariable UUID id,
                                                                     @Valid @RequestBody UserStatusRequest request) {
        return ApiResponses.ok(userAdminService.setStatus(RequestContext.currentUserId(), id, request),
                "USER_STATUS_UPDATED");
    }

    @Operation(summary = "Thay toàn bộ vai trò của người dùng")
    @RequireAuth(roles = RoleCode.SYSTEM_ADMIN)
    @PutMapping("/{id}/roles")
    public ResponseEntity<ResponseData<UserAdminResponse>> setRoles(@PathVariable UUID id,
                                                                    @Valid @RequestBody UserRolesRequest request) {
        return ApiResponses.ok(userAdminService.setRoles(RequestContext.currentUserId(), id, request.roleCodes()),
                "USER_ROLES_UPDATED");
    }

    @Operation(summary = "Đặt lại mật khẩu", description = "Thu hồi mọi phiên đăng nhập của người dùng đó.")
    @RequireAuth(roles = RoleCode.SYSTEM_ADMIN)
    @PutMapping("/{id}/password")
    public ResponseEntity<ResponseData<Void>> resetPassword(@PathVariable UUID id,
                                                            @Valid @RequestBody ResetPasswordRequest request) {
        userAdminService.resetPassword(RequestContext.currentUserId(), id, request.newPassword());
        return ApiResponses.ok(null, "USER_PASSWORD_RESET");
    }
}
