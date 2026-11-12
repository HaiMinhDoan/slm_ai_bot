package vn.slmglobal.slmai.services.media;

import vn.slmglobal.slmai.services.media.entity.MediaType;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.Optional;

/**
 * Nhận diện định dạng thật của file media theo magic bytes (không tin đuôi file hay Content-Type trình duyệt gửi),
 * kích thước ảnh và sha256. Dùng chung cho ảnh tải từ Drive và file tải lên từ trang admin.
 */
public final class MediaFiles {

    /** Số byte đầu cần đọc để nhận diện */
    public static final int HEAD_BYTES = 16;

    public enum Format {
        JPEG("image/jpeg", "jpg", MediaType.IMAGE),
        PNG("image/png", "png", MediaType.IMAGE),
        WEBP("image/webp", "webp", MediaType.IMAGE),
        MP4("video/mp4", "mp4", MediaType.VIDEO),
        MOV("video/quicktime", "mov", MediaType.VIDEO),
        WEBM("video/webm", "webm", MediaType.VIDEO);

        public final String mimeType;
        public final String extension;
        public final MediaType mediaType;

        Format(String mimeType, String extension, MediaType mediaType) {
            this.mimeType = mimeType;
            this.extension = extension;
            this.mediaType = mediaType;
        }
    }

    private MediaFiles() {
    }

    public static Optional<Format> sniff(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return Optional.of(Format.JPEG);
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return Optional.of(Format.PNG);
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return Optional.of(Format.WEBP);
        }
        // ISO base media: hộp "ftyp" ở byte 4; nhãn "qt  " là QuickTime (.mov), còn lại coi là MP4
        if (b.length >= 12 && b[4] == 'f' && b[5] == 't' && b[6] == 'y' && b[7] == 'p') {
            return Optional.of(b[8] == 'q' && b[9] == 't' ? Format.MOV : Format.MP4);
        }
        if (b.length >= 4 && (b[0] & 0xFF) == 0x1A && (b[1] & 0xFF) == 0x45 && (b[2] & 0xFF) == 0xDF
                && (b[3] & 0xFF) == 0xA3) {
            return Optional.of(Format.WEBM);
        }
        return Optional.empty();
    }

    /** Kích thước ảnh đọc từ header, không giải mã cả ảnh; WebP (ImageIO không hỗ trợ sẵn) thì null. */
    public static int[] dimensions(byte[] bytes) {
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                return null;
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(iis, true, true);
                return new int[]{reader.getWidth(0), reader.getHeight(0)};
            } finally {
                reader.dispose();
            }
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    public static String sha256(byte[] bytes) {
        return HexFormat.of().formatHex(digest().digest(bytes));
    }

    /** sha256 của cả luồng (file lớn như video không đọc hết vào bộ nhớ) */
    public static String sha256(InputStream in) throws IOException {
        MessageDigest md = digest();
        try (DigestInputStream dis = new DigestInputStream(in, md)) {
            dis.transferTo(OutputStream.nullOutputStream());
        }
        return HexFormat.of().formatHex(md.digest());
    }

    private static MessageDigest digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
