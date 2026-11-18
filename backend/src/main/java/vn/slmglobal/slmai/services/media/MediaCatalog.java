package vn.slmglobal.slmai.services.media;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import vn.slmglobal.slmai.common.util.HashUtil;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.media.entity.LinkRole;
import vn.slmglobal.slmai.services.media.entity.LinkTargetType;
import vn.slmglobal.slmai.services.media.entity.MediaType;
import vn.slmglobal.slmai.services.media.entity.SourceType;
import vn.slmglobal.slmai.services.media.repository.MediaLinkRepository;
import vn.slmglobal.slmai.services.media.repository.MediaTargetRepository;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Media bot được gửi qua một kênh (mục 7.1): đã duyệt, chưa xoá, kênh nằm trong kenh_cho_phep và có nguồn gửi được.
 * Dựng khối "Gửi media" cho prompt (phần cache): cách gửi và danh mục media AI tự chọn được. Ảnh chính của gói không
 * nằm trong danh mục vì được gửi tự động khi câu trả lời có link gói (mục 7.2).
 */
@Service
public class MediaCatalog {

    /** Messenger nhận tệp đính kèm tới khoảng 25MB; video tải lên lớn hơn thì không gửi được (mục 6.4) */
    static final long MAX_VIDEO_BYTES = 25L * 1024 * 1024;

    private final NamedParameterJdbcTemplate jdbc;
    private final MediaLinkRepository links;
    private final MediaTargetRepository targets;

    public MediaCatalog(NamedParameterJdbcTemplate jdbc, MediaLinkRepository links, MediaTargetRepository targets) {
        this.jdbc = jdbc;
        this.links = links;
        this.targets = targets;
    }

    /**
     * Một media gửi được. {@code sourceId} là nguồn dùng để gửi: ảnh là tệp tải lên; video ưu tiên link (Facebook,
     * YouTube, TikTok: khách xem ngay trên thẻ xem trước), không có link thì tệp tải lên nhỏ hơn 25MB.
     */
    public record Entry(UUID id, String code, MediaType type, String title, String summary, String useWhen,
                        String avoidWhen, List<String> topics, Integer durationSeconds, UUID sourceId,
                        SourceType sourceType, List<MediaLinkRepository.Link> links) {

        /** Ảnh chính (ảnh báo giá) của một gói: gửi tự động theo link gói, không đưa vào danh mục cho AI */
        public boolean packageMainImage() {
            return type == MediaType.IMAGE && links.stream().anyMatch(l -> l.targetType() == LinkTargetType.PACKAGE
                    && l.role() == LinkRole.MAIN_IMAGE);
        }

        public boolean linkedTo(LinkTargetType type, String key) {
            return links.stream().anyMatch(l -> l.targetType() == type && l.targetKey().equals(key));
        }

        public List<Integer> packageIds() {
            List<Integer> out = new ArrayList<>();
            for (MediaLinkRepository.Link l : links) {
                if (l.targetType() == LinkTargetType.PACKAGE && l.targetKey().matches("\\d+")) {
                    out.add(Integer.parseInt(l.targetKey()));
                }
            }
            return out;
        }
    }

    /** Khối prompt và phiên bản (luot_goi_ai.phien_ban_media) */
    public record Block(String text, String version) {
    }

    public List<Entry> entries(ChannelCode channel) {
        List<Entry> rows = jdbc.query("""
                SELECT m.id, m.ma_danh_muc, m.loai_media, m.tieu_de, m.tom_tat, m.dung_khi, m.tranh_khi, m.chu_de,
                       n.id AS nguon_id, n.loai_nguon, n.thoi_luong_giay
                FROM media m
                JOIN LATERAL (
                    SELECT n.id, n.loai_nguon, n.thoi_luong_giay, n.tao_luc FROM media_nguon n
                    WHERE n.media_id = m.id
                      AND n.trang_thai_truy_cap NOT IN ('PRIVATE', 'UNAVAILABLE', 'DELETED')
                      AND ((n.loai_nguon = 'UPLOAD' AND n.duong_dan_luu_tru IS NOT NULL
                            AND n.trang_thai_xu_ly = 'READY'
                            AND (m.loai_media = 'IMAGE' OR COALESCE(n.dung_luong_byte, 0) <= :maxVideo))
                        OR (m.loai_media = 'VIDEO' AND n.loai_nguon IN ('FACEBOOK', 'YOUTUBE', 'TIKTOK')
                            AND n.url_nguon IS NOT NULL))
                    ORDER BY CASE WHEN n.loai_nguon = 'UPLOAD' THEN 1 ELSE 0 END, n.tao_luc
                    LIMIT 1) n ON true
                WHERE m.trang_thai = 'APPROVED' AND m.xoa_luc IS NULL AND :kenh = ANY (m.kenh_cho_phep)
                ORDER BY m.ma_danh_muc NULLS LAST, m.tao_luc""",
                new MapSqlParameterSource("kenh", channel.name()).addValue("maxVideo", MAX_VIDEO_BYTES),
                (rs, i) -> new Entry(rs.getObject("id", UUID.class), rs.getString("ma_danh_muc"),
                        MediaType.valueOf(rs.getString("loai_media")), rs.getString("tieu_de"),
                        rs.getString("tom_tat"), rs.getString("dung_khi"), rs.getString("tranh_khi"),
                        strings(rs.getArray("chu_de")), (Integer) rs.getObject("thoi_luong_giay"),
                        rs.getObject("nguon_id", UUID.class), SourceType.valueOf(rs.getString("loai_nguon")),
                        List.of()));
        if (rows.isEmpty()) {
            return rows;
        }
        Map<UUID, List<MediaLinkRepository.Link>> byMedia = links.findByMediaIds(
                rows.stream().map(Entry::id).toList()).stream()
                .collect(Collectors.groupingBy(MediaLinkRepository.Link::mediaId));
        return rows.stream().map(e -> new Entry(e.id(), e.code(), e.type(), e.title(), e.summary(), e.useWhen(),
                e.avoidWhen(), e.topics(), e.durationSeconds(), e.sourceId(), e.sourceType(),
                byMedia.getOrDefault(e.id(), List.of()))).toList();
    }

    public Block block(ChannelCode channel) {
        return block(entries(channel));
    }

    Block block(List<Entry> entries) {
        List<Entry> listed = entries.stream().filter(e -> !e.packageMainImage() && e.code() != null).toList();
        Map<LinkTargetType, Map<String, String>> labels = labels(listed);
        StringBuilder sb = new StringBuilder("""
                # Gửi media

                Hệ thống gửi được ảnh và video cho khách ngay sau tin chữ của bạn:
                - Câu trả lời có link gói (…/products/…): hệ thống tự gửi link đó thành một tin riêng để khách thấy \
                thẻ xem trước, rồi gửi ảnh báo giá của gói và một video công trình tương tự nếu có (gui_kem trong \
                kết quả công cụ cho biết gói nào có gì). Không cần ghi gì vào trường media cho phần này; có thể nói \
                ngắn "em gửi anh ảnh báo giá chi tiết bên dưới ạ" nếu gui_kem có ảnh báo giá.
                - Muốn gửi thêm media trong danh mục bên dưới thì ghi mã vào trường media (ví dụ ["M07"]); không có \
                mục thật sự hợp với câu hỏi hoặc mối lo hiện tại của khách thì để []. Chọn theo "dùng khi" và \
                "tránh khi"; mỗi lượt tối đa 1 video và 2 ảnh; không gửi lại media đã gửi trong hội thoại (lịch sử \
                ghi [Đã gửi kèm: …]). Câu trả lời phải tự đủ ý khi khách không mở media; giới thiệu ngắn media gửi \
                kèm, ví dụ "em gửi anh video công trình thực tế bên dưới ạ".
                - Không tự viết link ảnh hay link video vào câu trả lời; hệ thống gửi theo mã.

                ## Danh mục media
                """);
        if (listed.isEmpty()) {
            sb.append("\n(Chưa có media nào trong danh mục: để media là [].)\n");
        }
        for (Entry e : listed) {
            sb.append("\n[").append(e.code()).append("] ").append(e.type() == MediaType.VIDEO ? "VIDEO" : "ẢNH");
            if (e.durationSeconds() != null && e.durationSeconds() > 0) {
                sb.append(' ').append(e.durationSeconds() / 60).append(':')
                        .append(String.format("%02d", e.durationSeconds() % 60));
            }
            sb.append(" · ").append(oneLine(e.title())).append('\n');
            line(sb, "tóm tắt", e.summary());
            line(sb, "dùng khi", e.useWhen());
            line(sb, "tránh khi", e.avoidWhen());
            if (!e.topics().isEmpty()) {
                line(sb, "chủ đề", String.join(", ", e.topics()));
            }
            String related = related(e, labels);
            if (!related.isEmpty()) {
                line(sb, "gắn với", related);
            }
        }
        String text = sb.toString();
        return new Block(text, HashUtil.sha256Hex(text));
    }

    private Map<LinkTargetType, Map<String, String>> labels(List<Entry> entries) {
        Map<LinkTargetType, Set<String>> keys = new EnumMap<>(LinkTargetType.class);
        entries.forEach(e -> e.links().forEach(l ->
                keys.computeIfAbsent(l.targetType(), t -> new LinkedHashSet<>()).add(l.targetKey())));
        Map<LinkTargetType, Map<String, String>> out = new EnumMap<>(LinkTargetType.class);
        keys.forEach((type, k) -> out.put(type, targets.labels(type, k)));
        return out;
    }

    /** "gói Hệ On-Grid 19 kWp 3 pha (goi_id 156, 157); tài liệu playbooks/…": gói hai miền cùng tên ghi một lần */
    private static String related(Entry e, Map<LinkTargetType, Map<String, String>> labels) {
        Map<String, List<String>> packages = new java.util.LinkedHashMap<>();
        Set<String> others = new LinkedHashSet<>();
        for (MediaLinkRepository.Link l : e.links()) {
            String label = labels.getOrDefault(l.targetType(), Map.of()).getOrDefault(l.targetKey(), l.targetKey());
            switch (l.targetType()) {
                case PACKAGE -> packages.computeIfAbsent(
                        label.replaceFirst("^#\\d+ ", "").replaceFirst(" \\([A-Z]+\\)$", ""),
                        k -> new ArrayList<>()).add(l.targetKey());
                case MATERIAL -> others.add("vật tư " + label);
                case BRAND -> others.add("hãng " + label);
                case KNOWLEDGE_DOC -> others.add("tài liệu " + label);
            }
        }
        List<String> parts = new ArrayList<>();
        packages.forEach((name, ids) -> parts.add("gói " + name + " (goi_id "
                + ids.stream().distinct().collect(Collectors.joining(", ")) + ")"));
        parts.addAll(others);
        return String.join("; ", parts);
    }

    private static void line(StringBuilder sb, String label, String value) {
        if (value != null && !value.isBlank()) {
            sb.append("  ").append(label).append(": ").append(oneLine(value)).append('\n');
        }
    }

    private static String oneLine(String s) {
        return s.strip().replaceAll("\\s*\\n\\s*", " ");
    }

    private static List<String> strings(Array a) throws SQLException {
        if (a == null) {
            return List.of();
        }
        return List.of((String[]) a.getArray());
    }
}
