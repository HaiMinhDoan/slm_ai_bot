package vn.slmglobal.slmai.adapters.facebook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

/** Lỗi khi gọi Graph API. Thông báo chỉ gồm mã và mô tả lỗi của Meta, không chứa token. */
public class MessengerApiException extends RuntimeException {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public MessengerApiException(String message) {
        super(message);
    }

    /** Rút gọn body lỗi của Graph API: {"error": {"message", "type", "code", "error_subcode", "fbtrace_id"}} */
    static String describeGraphError(byte[] body) {
        try {
            JsonNode error = MAPPER.readTree(body).path("error");
            if (error.isObject()) {
                return "code=" + error.path("code").asText("?")
                        + (error.has("error_subcode") ? " subcode=" + error.path("error_subcode").asText() : "")
                        + " type=" + error.path("type").asText("?")
                        + " message=" + error.path("message").asText("")
                        + " fbtrace_id=" + error.path("fbtrace_id").asText("");
            }
        } catch (Exception ignored) {
            // body không phải JSON
        }
        String raw = new String(body, StandardCharsets.UTF_8);
        return raw.length() > 300 ? raw.substring(0, 300) : raw;
    }
}
