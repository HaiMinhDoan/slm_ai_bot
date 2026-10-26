package vn.slmglobal.slmai.common.security;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Đọc access token (nếu có) và gán người dùng vào {@link RequestContext}.
 * Không có header: request đi tiếp như khách; API cần đăng nhập sẽ bị {@code @RequireAuth} chặn.
 * Có header nhưng sai, hết hạn hoặc đã đăng xuất: trả 401 ngay để frontend làm mới token hoặc đăng nhập lại.
 *
 * <p>Request bất đồng bộ (SSE): Spring gọi {@link #afterConcurrentHandlingStarted} thay cho {@code afterCompletion}
 * ở lượt đầu, nên phải xoá {@link RequestContext} ở đó để luồng Tomcat không mang người dùng cũ sang request khác.
 * Lượt dispatch ASYNC sau đó không kiểm tra lại token (response đã gửi header, không ghi 401 được nữa); kết nối
 * dài hạn tự kiểm tra hạn và thu hồi token theo {@link #VERIFIED_TOKEN_ATTRIBUTE}.
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements AsyncHandlerInterceptor {

    /** Request attribute chứa {@link JwtService.VerifiedToken} của request đã đăng nhập */
    public static final String VERIFIED_TOKEN_ATTRIBUTE = "slm.verifiedToken";

    private final JwtService jwtService;
    private final TokenBlacklist tokenBlacklist;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws IOException {
        if (request.getDispatcherType() == DispatcherType.ASYNC) {
            return true;
        }
        String lang = Optional.ofNullable(request.getHeader("lang")).orElse("vi");
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null) {
            RequestContext.set(request.getRequestURI(), lang, null);
            return true;
        }

        String token = JwtService.extractBearer(authHeader);
        if (token == null) {
            return unauthorized(response, "Authorization phải có dạng: Bearer <token>");
        }

        Optional<JwtService.VerifiedToken> verified = jwtService.verifyAccessToken(token);
        if (verified.isEmpty()) {
            return unauthorized(response, "Token không hợp lệ hoặc đã hết hạn");
        }
        if (tokenBlacklist.isRevoked(verified.get().jti())) {
            return unauthorized(response, "Token đã bị thu hồi");
        }

        RequestContext.set(request.getRequestURI(), lang, verified.get().authInfo());
        request.setAttribute(VERIFIED_TOKEN_ATTRIBUTE, verified.get());
        return true;
    }

    @Override
    public void afterConcurrentHandlingStarted(@NonNull HttpServletRequest request,
                                               @NonNull HttpServletResponse response, @NonNull Object handler) {
        RequestContext.clear();
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                @NonNull Object handler, Exception ex) {
        RequestContext.clear();
    }

    private boolean unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // Đặt Content-Type trước khi ghi, nếu không charset không được áp dụng
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"status\":401,\"messageCode\":\"UNAUTHORIZED\",\"message\":\""
                + message.replace("\"", "\\\"") + "\"}");
        return false;
    }
}
