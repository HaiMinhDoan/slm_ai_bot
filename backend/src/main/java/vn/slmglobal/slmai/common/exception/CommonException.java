package vn.slmglobal.slmai.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Lỗi nghiệp vụ có mã HTTP. {@code message} là câu tiếng Việt hiển thị được cho người dùng.
 * Mặc định 400; dùng các hàm tạo nhanh cho 401 / 403 / 404 / 409.
 */
@Getter
public class CommonException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final transient Object data;

    public CommonException(String message) {
        this(HttpStatus.BAD_REQUEST, message, null);
    }

    public CommonException(HttpStatus httpStatus, String message) {
        this(httpStatus, message, null);
    }

    public CommonException(HttpStatus httpStatus, String message, Object data) {
        super(message);
        this.httpStatus = httpStatus;
        this.data = data;
    }

    public static CommonException unauthorized(String message) {
        return new CommonException(HttpStatus.UNAUTHORIZED, message);
    }

    public static CommonException forbidden(String message) {
        return new CommonException(HttpStatus.FORBIDDEN, message);
    }

    public static CommonException notFound(String message) {
        return new CommonException(HttpStatus.NOT_FOUND, message);
    }

    public static CommonException conflict(String message) {
        return new CommonException(HttpStatus.CONFLICT, message);
    }
}
