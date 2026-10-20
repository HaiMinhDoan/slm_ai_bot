package vn.slmglobal.slmai.common.security;

import vn.slmglobal.slmai.common.exception.CommonException;

import java.util.UUID;

/**
 * Thông tin của request hiện tại (người dùng, đường dẫn, ngôn ngữ), gắn theo luồng xử lý.
 * {@link AuthInterceptor} gán ở đầu request và xóa khi request kết thúc.
 */
public final class RequestContext {

    private static final ThreadLocal<String> PATH = new ThreadLocal<>();
    private static final ThreadLocal<String> LANG = new ThreadLocal<>();
    private static final ThreadLocal<AuthInfo> AUTH_INFO = new ThreadLocal<>();

    private RequestContext() {
    }

    public static void set(String path, String lang, AuthInfo authInfo) {
        PATH.set(path);
        LANG.set(lang);
        AUTH_INFO.set(authInfo);
    }

    public static String getPath() {
        return PATH.get();
    }

    public static String getLang() {
        return LANG.get();
    }

    public static AuthInfo getAuthInfo() {
        return AUTH_INFO.get();
    }

    /** Người dùng đang đăng nhập; ném 401 nếu chưa đăng nhập. */
    public static AuthInfo requireAuthInfo() {
        AuthInfo authInfo = AUTH_INFO.get();
        if (authInfo == null) {
            throw CommonException.unauthorized("Chưa đăng nhập");
        }
        return authInfo;
    }

    public static UUID currentUserId() {
        return requireAuthInfo().id();
    }

    public static void clear() {
        PATH.remove();
        LANG.remove();
        AUTH_INFO.remove();
    }
}
