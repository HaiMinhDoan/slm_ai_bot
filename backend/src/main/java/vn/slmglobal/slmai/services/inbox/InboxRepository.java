package vn.slmglobal.slmai.services.inbox;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Đọc danh sách hội thoại cho trang chat bằng SQL: mỗi dòng cần tin cuối, số nháp, khách có đang chờ trả lời không,
 * nên dùng LATERAL thay vì nạp từng hội thoại.
 *
 * <p>"Khách đang chờ": tin cuối không tính nháp / tin bị bỏ / tin gửi lỗi là tin của khách.
 */
@Repository
@RequiredArgsConstructor
public class InboxRepository {

    /** Bộ lọc trạng thái trên trang chat */
    public enum View {
        /** Cần nhân viên: nhân viên đang phụ trách, hoặc có nháp / tin gửi lỗi chờ xử lý */
        NEEDS_STAFF,
        /** Nhân viên đang phụ trách (AI dừng) */
        STAFF,
        /** AI đang trả lời */
        AI,
        /** Có nháp AI hoặc tin gửi lỗi */
        DRAFT
    }

    public record Filter(String channel, View view, String keyword) {
    }

    public record Row(UUID id, String channel, String ownership, String status, String intent, UUID customerId,
                      String customerName, String phone, String leadLevel, String lastSender, String lastText,
                      String lastStatus, OffsetDateTime lastAt, int draftCount, boolean customerWaiting,
                      OffsetDateTime lastCustomerMessageAt, OffsetDateTime takenOverAt) {
    }

    public record ChannelStats(String channel, long total, long needsStaff, long drafts, long staff) {
    }

    private static final String FROM = """
            FROM hoi_thoai h
            JOIN khach_hang k ON k.id = h.khach_hang_id
            JOIN dinh_danh_kenh d ON d.id = h.dinh_danh_kenh_id
            LEFT JOIN ho_so_tu_van hs ON hs.hoi_thoai_id = h.id
            LEFT JOIN LATERAL (
                SELECT t.nguoi_gui, t.noi_dung, t.trang_thai, t.tao_luc
                FROM tin_nhan t
                WHERE t.hoi_thoai_id = h.id AND t.trang_thai <> 'DISCARDED'
                ORDER BY t.tao_luc DESC LIMIT 1) cuoi ON true
            LEFT JOIN LATERAL (
                SELECT t.chieu
                FROM tin_nhan t
                WHERE t.hoi_thoai_id = h.id AND t.trang_thai NOT IN ('DRAFT', 'DISCARDED', 'FAILED')
                ORDER BY t.tao_luc DESC LIMIT 1) that ON true
            LEFT JOIN LATERAL (
                SELECT count(*) AS so
                FROM tin_nhan t
                WHERE t.hoi_thoai_id = h.id AND t.chieu = 'OUT' AND t.trang_thai IN ('DRAFT', 'FAILED')) nhap ON true
            """;

    private final NamedParameterJdbcTemplate jdbc;

    public List<Row> list(Filter f, int page, int size) {
        MapSqlParameterSource p = params(f).addValue("limit", size).addValue("offset", (long) page * size);
        return jdbc.query("""
                SELECT h.id, h.kenh, h.nguoi_phu_trach, h.trang_thai, h.y_dinh, h.khach_hang_id,
                       COALESCE(k.ten, d.ten_hien_thi) AS ten_khach, k.sdt, hs.muc_tiem_nang,
                       cuoi.nguoi_gui, cuoi.noi_dung, cuoi.trang_thai AS trang_thai_tin, cuoi.tao_luc AS tin_cuoi_luc,
                       nhap.so AS so_nhap, that.chieu = 'IN' AS khach_dang_cho,
                       h.tin_khach_cuoi_luc, h.tiep_quan_luc
                """ + FROM + where(f) + """
                ORDER BY COALESCE(cuoi.tao_luc, h.tao_luc) DESC
                LIMIT :limit OFFSET :offset
                """, p, (rs, i) -> new Row(
                rs.getObject("id", UUID.class),
                rs.getString("kenh"),
                rs.getString("nguoi_phu_trach"),
                rs.getString("trang_thai"),
                rs.getString("y_dinh"),
                rs.getObject("khach_hang_id", UUID.class),
                rs.getString("ten_khach"),
                rs.getString("sdt"),
                rs.getString("muc_tiem_nang"),
                rs.getString("nguoi_gui"),
                rs.getString("noi_dung"),
                rs.getString("trang_thai_tin"),
                rs.getObject("tin_cuoi_luc", OffsetDateTime.class),
                rs.getInt("so_nhap"),
                rs.getBoolean("khach_dang_cho"),
                rs.getObject("tin_khach_cuoi_luc", OffsetDateTime.class),
                rs.getObject("tiep_quan_luc", OffsetDateTime.class)));
    }

    public long count(Filter f) {
        Long n = jdbc.queryForObject("SELECT count(*) " + FROM + where(f), params(f), Long.class);
        return n == null ? 0 : n;
    }

    /** Số hội thoại theo kênh (tab của trang chat): tổng, cần nhân viên, có nháp, nhân viên đang phụ trách */
    public List<ChannelStats> stats() {
        return jdbc.query("""
                SELECT h.kenh, count(*) AS tong,
                       count(*) FILTER (WHERE h.nguoi_phu_trach = 'HUMAN_OWNED' OR nhap.so > 0) AS can_nhan_vien,
                       count(*) FILTER (WHERE nhap.so > 0) AS co_nhap,
                       count(*) FILTER (WHERE h.nguoi_phu_trach = 'HUMAN_OWNED') AS nhan_vien
                FROM hoi_thoai h
                LEFT JOIN LATERAL (
                    SELECT count(*) AS so
                    FROM tin_nhan t
                    WHERE t.hoi_thoai_id = h.id AND t.chieu = 'OUT' AND t.trang_thai IN ('DRAFT', 'FAILED')) nhap ON true
                WHERE h.trang_thai = 'OPEN'
                GROUP BY h.kenh
                """, new MapSqlParameterSource(), (rs, i) -> new ChannelStats(rs.getString("kenh"), rs.getLong("tong"),
                rs.getLong("can_nhan_vien"), rs.getLong("co_nhap"), rs.getLong("nhan_vien")));
    }

    /** Tên gói theo id slm_v2 (gói AI đã báo cho khách) */
    public Map<Integer, String> packageNames(Collection<Integer> ids) {
        Map<Integer, String> out = new LinkedHashMap<>();
        if (ids.isEmpty()) {
            return out;
        }
        jdbc.query("SELECT id_nguon, ten, ma_co_so FROM dm_tron_goi WHERE id_nguon IN (:ids)",
                new MapSqlParameterSource("ids", ids),
                rs -> {
                    out.put(rs.getInt("id_nguon"), rs.getString("ten") + " (" + rs.getString("ma_co_so") + ")");
                });
        return out;
    }

    /** Lead mới nhất của hội thoại (khach_tiem_nang); null nếu khách chưa để lại số */
    public Map<String, Object> latestLead(UUID conversationId) {
        List<Map<String, Object>> rows = jdbc.query("""
                SELECT sdt, tinh, mien, tom_tat_nhu_cau, muc_tiem_nang, ly_do_muc_tiem_nang, trang_thai, tao_luc
                FROM khach_tiem_nang WHERE hoi_thoai_id = :id ORDER BY tao_luc DESC LIMIT 1
                """, new MapSqlParameterSource("id", conversationId), (rs, i) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("phone", rs.getString("sdt"));
            m.put("province", rs.getString("tinh"));
            m.put("region", rs.getString("mien"));
            m.put("needSummary", rs.getString("tom_tat_nhu_cau"));
            m.put("leadLevel", rs.getString("muc_tiem_nang"));
            m.put("leadLevelReason", rs.getString("ly_do_muc_tiem_nang"));
            m.put("status", rs.getString("trang_thai"));
            m.put("createdAt", rs.getObject("tao_luc", OffsetDateTime.class));
            return m;
        });
        return rows.isEmpty() ? null : rows.get(0);
    }

    private static String where(Filter f) {
        StringBuilder w = new StringBuilder("WHERE h.trang_thai = 'OPEN'");
        if (f.channel() != null) {
            w.append(" AND h.kenh = :kenh");
        }
        if (f.view() != null) {
            w.append(switch (f.view()) {
                case NEEDS_STAFF -> " AND (h.nguoi_phu_trach = 'HUMAN_OWNED' OR nhap.so > 0)";
                case STAFF -> " AND h.nguoi_phu_trach = 'HUMAN_OWNED'";
                case AI -> " AND h.nguoi_phu_trach = 'AI_ACTIVE'";
                case DRAFT -> " AND nhap.so > 0";
            });
        }
        if (keyword(f) != null) {
            w.append(" AND (lower(COALESCE(k.ten, '')) LIKE :kw OR lower(COALESCE(d.ten_hien_thi, '')) LIKE :kw"
                    + " OR COALESCE(k.sdt, '') LIKE :kw)");
        }
        return w.append('\n').toString();
    }

    private static MapSqlParameterSource params(Filter f) {
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("kenh", f.channel());
        String kw = keyword(f);
        if (kw != null) {
            p.addValue("kw", "%" + kw.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%");
        }
        return p;
    }

    private static String keyword(Filter f) {
        return f.keyword() == null || f.keyword().isBlank() ? null : f.keyword().strip().toLowerCase(Locale.ROOT);
    }
}
