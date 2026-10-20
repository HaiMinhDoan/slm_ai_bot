package vn.slmglobal.slmai.adapters.facebook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Xác thực header X-Hub-Signature-256 của webhook Meta: "sha256=" + HMAC-SHA256(body thô, App Secret) dạng hex.
 * Phải tính trên đúng byte nhận được, không phải JSON đã parse lại.
 */
@Component
@RequiredArgsConstructor
public class FacebookSignatureVerifier {

    private static final String PREFIX = "sha256=";

    private final FacebookProperties properties;

    public boolean isValid(byte[] body, String signatureHeader) {
        if (!properties.hasAppSecret() || body == null || signatureHeader == null
                || !signatureHeader.startsWith(PREFIX)) {
            return false;
        }
        byte[] given;
        try {
            given = HexFormat.of().parseHex(signatureHeader.substring(PREFIX.length()));
        } catch (IllegalArgumentException e) {
            return false;
        }
        // So sánh thời gian hằng, không lộ thông tin qua độ trễ
        return MessageDigest.isEqual(sign(body), given);
    }

    byte[] sign(byte[] body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.appSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(body);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("JVM không hỗ trợ HmacSHA256", e);
        }
    }
}
