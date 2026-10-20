package vn.slmglobal.slmai.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bắt buộc đăng nhập (và có vai trò) mới gọi được API. Kiểm tra trong {@link AuthorizationAspect}.
 * {@code roles} rỗng hoặc chứa {@link RoleCode#ALL}: chỉ cần đăng nhập. SYSTEM_ADMIN luôn qua.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAuth {

    String[] roles() default {};

    LogicType rolesLogic() default LogicType.OR;

    enum LogicType {
        AND, OR
    }
}
