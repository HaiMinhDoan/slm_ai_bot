package vn.slmglobal.slmai.services.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Truy cập thong_bao / thong_bao_da_doc bằng SQL: cần {@code INSERT ... ON CONFLICT ... RETURNING} (lỗi trùng khoá
 * trong PostgreSQL làm hỏng cả transaction đang chạy, kể cả lượt AI) và trạng thái đã đọc theo từng người.
 *
 * <p>Người dùng thấy thông báo gửi cho mọi người ({@code nguoi_nhan_id IS NULL}) và thông báo gửi riêng cho mình,
 * trong khoảng {@code since} gần đây. Quyền xem theo vai trò kiểm ở controller.
 */
@Repository
@RequiredArgsConstructor
public class NotificationRepository {

    private static final String VISIBLE = "(t.nguoi_nhan_id IS NULL OR t.nguoi_nhan_id = :uid) AND t.tao_luc >= :since";

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    /** Một dòng thông báo kèm trạng thái đã đọc của người đang xem */
    public record Row(long id, NotificationType type, NotificationSeverity severity, String title, String body,
                      JsonNode data, UUID recipientId, UUID conversationId, UUID customerId,
                      OffsetDateTime createdAt, OffsetDateTime readAt) {
    }

    /** Ghi nếu chưa có khoá chống trùng; trả dòng vừa ghi, rỗng nếu đã có. */
    public Optional<Row> insertIfAbsent(NotificationDraft d) {
        String data;
        try {
            data = objectMapper.writeValueAsString(d.data() == null ? Map.of() : d.data());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Dữ liệu thông báo không chuyển được sang JSON", e);
        }
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("loai", d.type().name())
                .addValue("mucDo", (d.severity() == null ? NotificationSeverity.INFO : d.severity()).name())
                .addValue("tieuDe", truncate(d.title(), 300))
                .addValue("noiDung", d.body())
                .addValue("duLieu", data)
                .addValue("nguoiNhan", d.recipientId())
                .addValue("hoiThoai", d.conversationId())
                .addValue("khachHang", d.customerId())
                .addValue("khoa", truncate(d.dedupeKey(), 200));
        List<Row> rows = jdbc.query("""
                INSERT INTO thong_bao (loai, muc_do, tieu_de, noi_dung, du_lieu, nguoi_nhan_id, hoi_thoai_id,
                                       khach_hang_id, khoa_chong_trung)
                VALUES (:loai, :mucDo, :tieuDe, :noiDung, CAST(:duLieu AS jsonb), :nguoiNhan, :hoiThoai,
                        :khachHang, :khoa)
                ON CONFLICT (khoa_chong_trung) DO NOTHING
                RETURNING id, loai, muc_do, tieu_de, noi_dung, du_lieu, nguoi_nhan_id, hoi_thoai_id, khach_hang_id,
                          tao_luc, NULL::timestamptz AS doc_luc""", p, rowMapper());
        return rows.stream().findFirst();
    }

    public List<Row> list(UUID userId, OffsetDateTime since, boolean unreadOnly, NotificationType type,
                          int limit, int offset) {
        MapSqlParameterSource p = visibility(userId, since).addValue("limit", limit).addValue("offset", offset);
        String sql = "SELECT t.*, d.doc_luc FROM thong_bao t "
                + "LEFT JOIN thong_bao_da_doc d ON d.thong_bao_id = t.id AND d.nguoi_dung_id = :uid "
                + "WHERE " + VISIBLE + filters(unreadOnly, type, p)
                + " ORDER BY t.id DESC LIMIT :limit OFFSET :offset";
        return jdbc.query(sql, p, rowMapper());
    }

    public long count(UUID userId, OffsetDateTime since, boolean unreadOnly, NotificationType type) {
        MapSqlParameterSource p = visibility(userId, since);
        String sql = "SELECT count(*) FROM thong_bao t "
                + "LEFT JOIN thong_bao_da_doc d ON d.thong_bao_id = t.id AND d.nguoi_dung_id = :uid "
                + "WHERE " + VISIBLE + filters(unreadOnly, type, p);
        Long n = jdbc.queryForObject(sql, p, Long.class);
        return n == null ? 0 : n;
    }

    public Optional<Row> findVisible(UUID userId, OffsetDateTime since, long id) {
        MapSqlParameterSource p = visibility(userId, since).addValue("id", id);
        return jdbc.query("SELECT t.*, d.doc_luc FROM thong_bao t "
                + "LEFT JOIN thong_bao_da_doc d ON d.thong_bao_id = t.id AND d.nguoi_dung_id = :uid "
                + "WHERE t.id = :id AND " + VISIBLE, p, rowMapper()).stream().findFirst();
    }

    /** Đánh dấu đã đọc một thông báo người dùng được xem; trả false nếu không xem được. */
    public boolean markRead(UUID userId, OffsetDateTime since, long id) {
        MapSqlParameterSource p = visibility(userId, since).addValue("id", id);
        int n = jdbc.update("INSERT INTO thong_bao_da_doc (nguoi_dung_id, thong_bao_id) "
                + "SELECT :uid, t.id FROM thong_bao t WHERE t.id = :id AND " + VISIBLE
                + " ON CONFLICT DO NOTHING", p);
        return n > 0 || findVisible(userId, since, id).isPresent();
    }

    /** Đọc hết tới {@code maxId} (thông báo tới sau lúc bấm vẫn chưa đọc); trả số dòng vừa đánh dấu. */
    public int markAllRead(UUID userId, OffsetDateTime since, long maxId) {
        MapSqlParameterSource p = visibility(userId, since).addValue("maxId", maxId);
        return jdbc.update("INSERT INTO thong_bao_da_doc (nguoi_dung_id, thong_bao_id) "
                + "SELECT :uid, t.id FROM thong_bao t WHERE t.id <= :maxId AND " + VISIBLE
                + " ON CONFLICT DO NOTHING", p);
    }

    private static MapSqlParameterSource visibility(UUID userId, OffsetDateTime since) {
        return new MapSqlParameterSource().addValue("uid", userId).addValue("since", since);
    }

    private static String filters(boolean unreadOnly, NotificationType type, MapSqlParameterSource p) {
        StringBuilder sb = new StringBuilder();
        if (unreadOnly) {
            sb.append(" AND d.thong_bao_id IS NULL");
        }
        if (type != null) {
            sb.append(" AND t.loai = :loai");
            p.addValue("loai", type.name());
        }
        return sb.toString();
    }

    private RowMapper<Row> rowMapper() {
        return (rs, i) -> new Row(
                rs.getLong("id"),
                NotificationType.valueOf(rs.getString("loai")),
                NotificationSeverity.valueOf(rs.getString("muc_do")),
                rs.getString("tieu_de"),
                rs.getString("noi_dung"),
                readJson(rs.getString("du_lieu")),
                rs.getObject("nguoi_nhan_id", UUID.class),
                rs.getObject("hoi_thoai_id", UUID.class),
                rs.getObject("khach_hang_id", UUID.class),
                rs.getObject("tao_luc", OffsetDateTime.class),
                rs.getObject("doc_luc", OffsetDateTime.class));
    }

    private JsonNode readJson(String json) {
        try {
            return json == null ? objectMapper.createObjectNode() : objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            return objectMapper.createObjectNode();
        }
    }

    private static String truncate(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }
}
