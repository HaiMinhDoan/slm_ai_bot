package vn.slmglobal.slmai.common.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import vn.slmglobal.slmai.common.security.RequireAuth;

/** Đánh dấu trên Swagger những API cần đăng nhập và vai trò yêu cầu, đọc từ {@link RequireAuth}. */
@Component
public class RequireAuthOperationCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        RequireAuth requireAuth = handlerMethod.getMethodAnnotation(RequireAuth.class);
        if (requireAuth == null) {
            return operation;
        }
        operation.addSecurityItem(new SecurityRequirement().addList(OpenApiConfig.BEARER_SCHEME));

        String[] roles = requireAuth.roles();
        if (roles.length > 0) {
            StringBuilder description = new StringBuilder();
            if (operation.getDescription() != null && !operation.getDescription().isBlank()) {
                description.append(operation.getDescription()).append("\n\n");
            }
            description.append("**Vai trò:** `").append(String.join("`, `", roles)).append('`');
            if (roles.length > 1) {
                description.append(requireAuth.rolesLogic() == RequireAuth.LogicType.AND
                        ? " (cần đủ tất cả)" : " (một trong các vai trò)");
            }
            operation.setDescription(description.toString());
        }
        return operation;
    }
}
