package vn.slmglobal.slmai.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class HashUtil {

    private HashUtil() {
    }

    /**
     * SHA-256 dạng hex. Dùng cho token cần tra cứu theo giá trị băm (refresh token) và ma_bam của catalog.
     * Không dùng cho mật khẩu người dùng (mật khẩu dùng BCrypt).
     */
    public static String sha256Hex(String raw) {
        return sha256Hex(raw.getBytes(StandardCharsets.UTF_8));
    }

    /** SHA-256 dạng hex của nội dung tệp (ảnh báo giá tự tạo) */
    public static String sha256Hex(byte[] raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("JVM không hỗ trợ SHA-256", e);
        }
    }
}
