package vn.slmglobal.slmai.common.web;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.Date;

/**
 * Vỏ bọc chung của mọi response. Frontend (Vben) đọc {@code data} khi thành công
 * và {@code message} để hiện lỗi cho người dùng.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ResponseData<T> {
    int status;
    String lang;
    String messageCode;
    T data;
    String error;
    String message;
    Date timestamp;
    String path;
}
