package vn.slmglobal.slmai.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import vn.slmglobal.slmai.common.web.ResponseData;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Chuyển mọi lỗi thành {@link ResponseData} với mã HTTP đúng.
 * Lỗi do client gửi sai trả 4xx (để người gọi biết sửa ở đâu); chỉ lỗi thật của máy chủ mới trả 500,
 * và không đưa chi tiết lỗi nội bộ ra ngoài.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<ResponseData<Object>> handleCommon(CommonException ex, HttpServletRequest request) {
        return build(ex.getHttpStatus(), ex.getHttpStatus().name(), ex.getMessage(), ex.getData(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseData<Object>> handleInvalidBody(MethodArgumentNotValidException ex,
                                                                  HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        String message = fieldErrors.values().stream()
                .filter(m -> m != null && !m.isBlank())
                .findFirst()
                .orElse("Dữ liệu gửi lên chưa hợp lệ");
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message, fieldErrors, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseData<Object>> handleConstraint(ConstraintViolationException ex,
                                                                 HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .findFirst()
                .orElse("Dữ liệu gửi lên chưa hợp lệ");
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message, null, request);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestPartException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ResponseData<Object>> handleBadRequest(Exception ex, HttpServletRequest request) {
        String message;
        if (ex instanceof MissingServletRequestPartException e) {
            message = "Thiếu phần dữ liệu bắt buộc: " + e.getRequestPartName();
        } else if (ex instanceof MissingServletRequestParameterException e) {
            message = "Thiếu tham số bắt buộc: " + e.getParameterName();
        } else if (ex instanceof MethodArgumentTypeMismatchException e) {
            message = "Tham số '" + e.getName() + "' sai định dạng";
        } else {
            message = "Nội dung gửi lên không đọc được (JSON sai định dạng)";
        }
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message, null, request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ResponseData<Object>> handleMediaType(HttpMediaTypeNotSupportedException ex,
                                                                HttpServletRequest request) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",
                "Kiểu dữ liệu gửi lên không được hỗ trợ", null, request);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ResponseData<Object>> handleTooLarge(MaxUploadSizeExceededException ex,
                                                               HttpServletRequest request) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE, "PAYLOAD_TOO_LARGE",
                "Tệp quá lớn so với giới hạn của máy chủ", null, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponseData<Object>> handleMethod(HttpRequestMethodNotSupportedException ex,
                                                             HttpServletRequest request) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
                "Phương thức HTTP không được hỗ trợ", null, request);
    }

    /** Đường dẫn không tồn tại. Thiếu handler này thì Spring 6.1+ rơi xuống handleGeneric và trả 500. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ResponseData<Object>> handleNoResource(NoResourceFoundException ex,
                                                                 HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", "Không tìm thấy đường dẫn", null, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseData<Object>> handleIntegrity(DataIntegrityViolationException ex,
                                                                HttpServletRequest request) {
        log.warn("Vi phạm ràng buộc dữ liệu tại {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION",
                "Dữ liệu bị trùng hoặc vi phạm ràng buộc", null, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseData<Object>> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Lỗi không xử lý tại {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "Máy chủ gặp lỗi, vui lòng thử lại sau", null, request);
    }

    private ResponseEntity<ResponseData<Object>> build(HttpStatus status, String messageCode, String message,
                                                       Object data, HttpServletRequest request) {
        return ResponseEntity.status(status).body(ResponseData.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .messageCode(messageCode)
                .message(message)
                .data(data)
                .path(request.getRequestURI())
                .timestamp(new Date())
                .build());
    }
}
