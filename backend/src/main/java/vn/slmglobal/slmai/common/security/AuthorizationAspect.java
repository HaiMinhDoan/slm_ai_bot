package vn.slmglobal.slmai.common.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import vn.slmglobal.slmai.common.exception.CommonException;

import java.util.Arrays;

/** Kiểm tra {@link RequireAuth} trước khi vào controller. */
@Aspect
@Component
public class AuthorizationAspect {

    @Around("@annotation(requireAuth)")
    public Object checkAuthorization(ProceedingJoinPoint joinPoint, RequireAuth requireAuth) throws Throwable {
        AuthInfo authInfo = RequestContext.requireAuthInfo();

        String[] required = requireAuth.roles();
        boolean anyRole = required.length == 0 || Arrays.asList(required).contains(RoleCode.ALL);
        if (anyRole || authInfo.hasAnyRole(RoleCode.SYSTEM_ADMIN)) {
            return joinPoint.proceed();
        }

        boolean allowed = requireAuth.rolesLogic() == RequireAuth.LogicType.OR
                ? authInfo.hasAnyRole(required)
                : authInfo.hasAllRoles(required);
        if (!allowed) {
            throw CommonException.forbidden("Vai trò không đủ quyền thực hiện thao tác này");
        }
        return joinPoint.proceed();
    }
}
