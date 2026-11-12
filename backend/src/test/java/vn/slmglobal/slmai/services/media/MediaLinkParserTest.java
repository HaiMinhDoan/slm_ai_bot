package vn.slmglobal.slmai.services.media;

import org.junit.jupiter.api.Test;
import vn.slmglobal.slmai.services.media.MediaLinkParser.Parsed;
import vn.slmglobal.slmai.services.media.entity.SourceType;

import static org.assertj.core.api.Assertions.assertThat;

/** Nhận diện link dán vào trang media: cùng một video dán theo nhiều dạng link ra cùng id (chống trùng). */
class MediaLinkParserTest {

    @Test
    void youtubeLinksShareOneId() {
        for (String url : new String[]{"https://www.youtube.com/watch?v=dQw4w9WgXcQ&t=10s",
                "https://youtu.be/dQw4w9WgXcQ", "https://youtube.com/shorts/dQw4w9WgXcQ",
                "https://m.youtube.com/watch?feature=share&v=dQw4w9WgXcQ"}) {
            Parsed p = MediaLinkParser.parse(url).orElseThrow();
            assertThat(p.type()).isEqualTo(SourceType.YOUTUBE);
            assertThat(p.externalId()).isEqualTo("dQw4w9WgXcQ");
            assertThat(p.url()).isEqualTo("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
        }
        assertThat(MediaLinkParser.parse("https://www.youtube.com/watch?v=short")).isEmpty();
    }

    @Test
    void facebookVideoForms() {
        // Cùng dạng id với nhập combo: dán lại link combo thì báo trùng
        assertThat(MediaLinkParser.parse("https://www.facebook.com/share/r/1FKCqC2LqE/").orElseThrow().externalId())
                .isEqualTo("share-r:1FKCqC2LqE");
        assertThat(MediaLinkParser.parse("https://www.facebook.com/reel/1234567890123").orElseThrow().externalId())
                .isEqualTo("reel:1234567890123");
        Parsed video = MediaLinkParser.parse("https://www.facebook.com/slmsolar/videos/987654321012345/").orElseThrow();
        assertThat(video.externalId()).isEqualTo("video:987654321012345");
        assertThat(video.url()).isEqualTo("https://www.facebook.com/watch/?v=987654321012345");
        assertThat(MediaLinkParser.parse("https://m.facebook.com/watch/?v=987654321012345").orElseThrow().externalId())
                .isEqualTo("video:987654321012345");
        assertThat(MediaLinkParser.parse("https://fb.watch/abCD12_x/").orElseThrow().externalId())
                .isEqualTo("fbwatch:abCD12_x");
        // Bài viết không phải video
        assertThat(MediaLinkParser.parse("https://www.facebook.com/share/p/1AbCdEf/")).isEmpty();
    }

    @Test
    void tiktokAndDrive() {
        Parsed tt = MediaLinkParser.parse("https://www.tiktok.com/@slm.solar/video/7301234567890123456?lang=vi")
                .orElseThrow();
        assertThat(tt.type()).isEqualTo(SourceType.TIKTOK);
        assertThat(tt.externalId()).isEqualTo("7301234567890123456");
        assertThat(tt.url()).isEqualTo("https://www.tiktok.com/@slm.solar/video/7301234567890123456");
        assertThat(MediaLinkParser.parse("https://vt.tiktok.com/ZSabc123/").orElseThrow().externalId())
                .isEqualTo("short:ZSabc123");

        Parsed drive = MediaLinkParser.parse(
                "https://drive.google.com/file/d/1MJDFYHkEuHjNLrqY6e6wSq4Db1bbJqkb/view?usp=drive_link").orElseThrow();
        assertThat(drive.drive()).isTrue();
        assertThat(drive.externalId()).isEqualTo("gdrive:1MJDFYHkEuHjNLrqY6e6wSq4Db1bbJqkb");
    }

    @Test
    void unknownLinksAreRejected() {
        assertThat(MediaLinkParser.parse("https://example.com/video/1")).isEmpty();
        assertThat(MediaLinkParser.parse("ftp://www.youtube.com/watch?v=dQw4w9WgXcQ")).isEmpty();
        assertThat(MediaLinkParser.parse("không phải link")).isEmpty();
        assertThat(MediaLinkParser.parse(null)).isEmpty();
    }
}
