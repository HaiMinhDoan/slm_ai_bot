package vn.slmglobal.slmai.services.media;

import vn.slmglobal.slmai.services.media.entity.SourceType;

import java.net.URI;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Nhận diện link media admin dán vào (mục 6.3 B): chuẩn hoá thành {@code id_ngoai} để chống dán trùng
 * ({@code (loai_nguon, id_ngoai)} unique) và một link gọn để lưu. Không gọi mạng: link rút gọn (fb.watch,
 * vt.tiktok.com) giữ nguyên mã rút gọn làm id, không mở ra link thật.
 */
public final class MediaLinkParser {

    /**
     * @param type        YOUTUBE / FACEBOOK / TIKTOK; null với ảnh Google Drive (tải về, lưu như file tải lên)
     * @param externalId  id chuẩn hoá, ví dụ {@code reel:123}, {@code share-r:1FKCqC2LqE}
     * @param url         link gọn để lưu và mở
     * @param driveFileId mã file Drive (chỉ với link Drive)
     */
    public record Parsed(SourceType type, String externalId, String url, String driveFileId) {

        public boolean drive() {
            return driveFileId != null;
        }
    }

    private static final Set<String> YOUTUBE = Set.of("youtube.com", "www.youtube.com", "m.youtube.com");
    private static final Set<String> FACEBOOK = Set.of("facebook.com", "www.facebook.com", "m.facebook.com",
            "web.facebook.com");
    private static final Set<String> TIKTOK = Set.of("tiktok.com", "www.tiktok.com", "m.tiktok.com");
    private static final Set<String> TIKTOK_SHORT = Set.of("vt.tiktok.com", "vm.tiktok.com");

    private static final Pattern YT_ID = Pattern.compile("[A-Za-z0-9_-]{11}");
    private static final Pattern YT_PATH = Pattern.compile("^/(?:shorts|embed|live)/([A-Za-z0-9_-]{11})/?$");
    private static final Pattern YT_QUERY = Pattern.compile("(?:^|&)v=([A-Za-z0-9_-]{11})(?:&|$)");
    private static final Pattern FB_SHARE = Pattern.compile("^/share/([rv])/([A-Za-z0-9_-]{4,64})/?$");
    private static final Pattern FB_REEL = Pattern.compile("^/reel/(\\d{5,25})/?$");
    private static final Pattern FB_VIDEOS = Pattern.compile("^/(?:[^/]+/)?videos/(?:[^/]+/)?(\\d{5,25})/?$");
    private static final Pattern FB_WATCH_QUERY = Pattern.compile("(?:^|&)v=(\\d{5,25})(?:&|$)");
    private static final Pattern SHORT_CODE = Pattern.compile("^/([A-Za-z0-9_-]{4,40})/?$");
    private static final Pattern TT_VIDEO = Pattern.compile("^/@([A-Za-z0-9._-]{1,40})/video/(\\d{8,25})/?$");

    private MediaLinkParser() {
    }

    public static Optional<Parsed> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        URI uri;
        try {
            uri = URI.create(raw.strip());
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        if (!scheme.equals("https") && !scheme.equals("http")) {
            return Optional.empty();
        }
        String path = uri.getRawPath() == null ? "" : uri.getRawPath();
        String query = uri.getRawQuery() == null ? "" : uri.getRawQuery();

        Optional<String> driveId = DriveImageFetcher.extractFileId(raw);
        if (driveId.isPresent()) {
            return Optional.of(new Parsed(null, "gdrive:" + driveId.get(),
                    "https://drive.google.com/file/d/" + driveId.get() + "/view", driveId.get()));
        }
        if (YOUTUBE.contains(host)) {
            Matcher m = YT_PATH.matcher(path);
            String id = m.matches() ? m.group(1) : null;
            if (id == null && path.equals("/watch")) {
                Matcher q = YT_QUERY.matcher(query);
                id = q.find() ? q.group(1) : null;
            }
            return Optional.ofNullable(id).map(MediaLinkParser::youtube);
        }
        if (host.equals("youtu.be")) {
            String id = path.length() > 1 ? path.substring(1).replaceAll("/$", "") : "";
            return YT_ID.matcher(id).matches() ? Optional.of(youtube(id)) : Optional.empty();
        }
        if (FACEBOOK.contains(host)) {
            return facebook(path, query);
        }
        if (host.equals("fb.watch")) {
            Matcher m = SHORT_CODE.matcher(path);
            return m.matches() ? Optional.of(new Parsed(SourceType.FACEBOOK, "fbwatch:" + m.group(1),
                    "https://fb.watch/" + m.group(1) + "/", null)) : Optional.empty();
        }
        if (TIKTOK.contains(host)) {
            Matcher m = TT_VIDEO.matcher(path);
            return m.matches() ? Optional.of(new Parsed(SourceType.TIKTOK, m.group(2),
                    "https://www.tiktok.com/@" + m.group(1) + "/video/" + m.group(2), null)) : Optional.empty();
        }
        if (TIKTOK_SHORT.contains(host)) {
            Matcher m = SHORT_CODE.matcher(path);
            return m.matches() ? Optional.of(new Parsed(SourceType.TIKTOK, "short:" + m.group(1),
                    "https://" + host + "/" + m.group(1) + "/", null)) : Optional.empty();
        }
        return Optional.empty();
    }

    private static Parsed youtube(String id) {
        return new Parsed(SourceType.YOUTUBE, id, "https://www.youtube.com/watch?v=" + id, null);
    }

    private static Optional<Parsed> facebook(String path, String query) {
        Matcher m = FB_SHARE.matcher(path);
        if (m.matches()) {
            // Cùng dạng id với nhập combo (PackageMediaImportService): dán lại link combo cũ thì báo trùng
            return Optional.of(new Parsed(SourceType.FACEBOOK, "share-" + m.group(1) + ":" + m.group(2),
                    "https://www.facebook.com/share/" + m.group(1) + "/" + m.group(2) + "/", null));
        }
        m = FB_REEL.matcher(path);
        if (m.matches()) {
            return Optional.of(new Parsed(SourceType.FACEBOOK, "reel:" + m.group(1),
                    "https://www.facebook.com/reel/" + m.group(1), null));
        }
        m = FB_VIDEOS.matcher(path);
        String id = m.matches() ? m.group(1) : null;
        if (id == null && (path.equals("/watch") || path.equals("/watch/"))) {
            Matcher q = FB_WATCH_QUERY.matcher(query);
            id = q.find() ? q.group(1) : null;
        }
        return Optional.ofNullable(id).map(v -> new Parsed(SourceType.FACEBOOK, "video:" + v,
                "https://www.facebook.com/watch/?v=" + v, null));
    }
}
