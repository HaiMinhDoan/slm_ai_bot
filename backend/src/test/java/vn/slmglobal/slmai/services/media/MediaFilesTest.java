package vn.slmglobal.slmai.services.media;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/** Nhận diện định dạng thật của file tải lên và thu nhỏ ảnh trước khi gửi Claude. */
class MediaFilesTest {

    @Test
    void sniffsImagesAndVideosByContent() throws Exception {
        assertThat(MediaFiles.sniff(DriveImageFetcherTest.jpeg(4, 4))).contains(MediaFiles.Format.JPEG);
        assertThat(MediaFiles.sniff(mp4("isom"))).contains(MediaFiles.Format.MP4);
        assertThat(MediaFiles.sniff(mp4("qt  "))).contains(MediaFiles.Format.MOV);
        assertThat(MediaFiles.sniff(new byte[]{0x1A, 0x45, (byte) 0xDF, (byte) 0xA3, 0, 0})).contains(
                MediaFiles.Format.WEBM);
        assertThat(MediaFiles.sniff("<html>đăng nhập</html>".getBytes(StandardCharsets.UTF_8))).isEmpty();
        assertThat(MediaFiles.sha256(new ByteArrayInputStream(new byte[]{1, 2, 3})))
                .isEqualTo(MediaFiles.sha256(new byte[]{1, 2, 3}));
    }

    @Test
    void largeImageIsScaledDownForClaude() throws Exception {
        MediaDraftService.ImageInput small = MediaDraftService.prepare(DriveImageFetcherTest.jpeg(40, 30)).orElseThrow();
        assertThat(small.bytes().length).isLessThan(5_000);

        MediaDraftService.ImageInput big = MediaDraftService.prepare(DriveImageFetcherTest.jpeg(3000, 2000))
                .orElseThrow();
        BufferedImage scaled = ImageIO.read(new ByteArrayInputStream(big.bytes()));
        assertThat(scaled.getWidth()).isEqualTo(1568);
        assertThat(scaled.getHeight()).isEqualTo(1045);
        assertThat(MediaDraftService.prepare(mp4("isom"))).isEmpty();
    }

    static byte[] mp4(String brand) {
        byte[] b = new byte[32];
        b[3] = 0x18;
        System.arraycopy("ftyp".getBytes(StandardCharsets.US_ASCII), 0, b, 4, 4);
        System.arraycopy(brand.getBytes(StandardCharsets.US_ASCII), 0, b, 8, 4);
        return b;
    }
}
