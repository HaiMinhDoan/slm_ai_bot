package vn.slmglobal.slmai.common.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import vn.slmglobal.slmai.common.security.RequestContext;

import java.util.Date;

/** Dựng response thành công theo cùng một khuôn {@link ResponseData}. */
public final class ApiResponses {

    private ApiResponses() {
    }

    public static <T> ResponseEntity<ResponseData<T>> ok(T data, String messageCode) {
        return ResponseEntity.ok(ResponseData.<T>builder()
                .status(HttpStatus.OK.value())
                .messageCode(messageCode)
                .data(data)
                .lang(RequestContext.getLang())
                .path(RequestContext.getPath())
                .timestamp(new Date())
                .build());
    }
}
