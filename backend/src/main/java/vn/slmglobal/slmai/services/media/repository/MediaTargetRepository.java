package vn.slmglobal.slmai.services.media.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.slmglobal.slmai.services.media.entity.LinkTargetType;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Đối tượng media gắn vào (media_lien_ket): gói, vật tư, hãng đồng bộ từ slm_v2 và tài liệu kiến thức. Tìm để chọn
 * trên trang admin và lấy tên hiển thị cho các liên kết đã có. Mã đối tượng: {@code id_nguon} (gói, vật tư, hãng)
 * hoặc {@code ma_tai_lieu}.
 */
@Repository
@RequiredArgsConstructor
public class MediaTargetRepository {

    private final NamedParameterJdbcTemplate jdbc;

    /**
     * @param twinKey gói cùng tên ở cơ sở giá miền kia (HN ↔ HCM), để gắn cả hai một lần
     */
    public record Target(String key, String label, String twinKey, String twinLabel) {
    }

    private static final String PACKAGE_LABEL = "'#' || g.id_nguon || ' ' || COALESCE(dc.ten, g.ten) || ' (' || g.ma_co_so || ')'";

    public List<Target> search(LinkTargetType type, String keyword, int limit) {
        String kw = keyword == null || keyword.isBlank() ? null : "%" + keyword.strip() + "%";
        String raw = keyword == null ? "" : keyword.strip().replaceFirst("^#", "");
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("kw", kw).addValue("raw", raw)
                .addValue("limit", limit);
        return switch (type) {
            case PACKAGE -> jdbc.query("""
                    SELECT CAST(g.id_nguon AS TEXT) AS k, %s AS label,
                           CAST(t.id_nguon AS TEXT) AS twin_k,
                           '#' || t.id_nguon || ' ' || COALESCE(tdc.ten, t.ten) || ' (' || t.ma_co_so || ')' AS twin_label
                    FROM dm_tron_goi g
                    LEFT JOIN dm_tron_goi_dieu_chinh dc ON dc.id_nguon = g.id_nguon
                    LEFT JOIN LATERAL (
                        SELECT x.id_nguon, x.ten, x.ma_co_so FROM dm_tron_goi x
                        WHERE x.ma_co_so <> g.ma_co_so AND x.dang_dung
                          AND lower(trim(x.ten)) = lower(trim(g.ten))
                          AND x.nhom_tron_goi_id IS NOT DISTINCT FROM g.nhom_tron_goi_id
                        ORDER BY x.duoc_goi_y DESC, x.id_nguon LIMIT 1) t ON true
                    LEFT JOIN dm_tron_goi_dieu_chinh tdc ON tdc.id_nguon = t.id_nguon
                    WHERE g.dang_dung AND dc.xoa_luc IS NULL
                      AND (CAST(:kw AS TEXT) IS NULL OR COALESCE(dc.ten, g.ten) ILIKE :kw
                           OR CAST(g.id_nguon AS TEXT) = :raw)
                    ORDER BY g.ma_co_so DESC, COALESCE(dc.ten, g.ten), g.id_nguon
                    LIMIT :limit""".formatted(PACKAGE_LABEL), p, (rs, i) -> new Target(rs.getString("k"),
                    rs.getString("label"), rs.getString("twin_k"), rs.getString("twin_label")));
            case MATERIAL -> jdbc.query("""
                    SELECT CAST(id_nguon AS TEXT) AS k, ten || COALESCE(' (' || ma || ')', '') AS label
                    FROM dm_vat_tu
                    WHERE dang_dung AND (CAST(:kw AS TEXT) IS NULL OR ten ILIKE :kw OR ma ILIKE :kw
                                         OR CAST(id_nguon AS TEXT) = :raw)
                    ORDER BY ten LIMIT :limit""", p, (rs, i) -> new Target(rs.getString("k"),
                    rs.getString("label"), null, null));
            case BRAND -> jdbc.query("""
                    SELECT CAST(id_nguon AS TEXT) AS k, ten AS label FROM dm_thuong_hieu
                    WHERE dang_dung AND (CAST(:kw AS TEXT) IS NULL OR ten ILIKE :kw)
                    ORDER BY ten LIMIT :limit""", p, (rs, i) -> new Target(rs.getString("k"),
                    rs.getString("label"), null, null));
            case KNOWLEDGE_DOC -> jdbc.query("""
                    SELECT d.ma_tai_lieu AS k, d.ma_tai_lieu || COALESCE(' · ' || v.tieu_de, '') AS label
                    FROM kt_tai_lieu d
                    LEFT JOIN LATERAL (SELECT tieu_de FROM kt_phien_ban WHERE tai_lieu_id = d.id
                                       ORDER BY so_phien_ban DESC LIMIT 1) v ON true
                    WHERE NOT d.da_luu_tru AND d.loai_tai_lieu NOT IN ('RULE', 'PROMPT', 'GUIDE', 'TEMPLATE')
                      AND (CAST(:kw AS TEXT) IS NULL OR d.ma_tai_lieu ILIKE :kw OR v.tieu_de ILIKE :kw)
                    ORDER BY d.ma_tai_lieu LIMIT :limit""", p, (rs, i) -> new Target(rs.getString("k"),
                    rs.getString("label"), null, null));
        };
    }

    /** Tên hiển thị theo mã; mã không còn tồn tại thì không có trong kết quả */
    public Map<String, String> labels(LinkTargetType type, Collection<String> keys) {
        Map<String, String> out = new HashMap<>();
        if (keys.isEmpty()) {
            return out;
        }
        MapSqlParameterSource p = new MapSqlParameterSource("keys", keys);
        String sql = switch (type) {
            case PACKAGE -> """
                    SELECT CAST(g.id_nguon AS TEXT) AS k, %s AS label FROM dm_tron_goi g
                    LEFT JOIN dm_tron_goi_dieu_chinh dc ON dc.id_nguon = g.id_nguon
                    WHERE CAST(g.id_nguon AS TEXT) IN (:keys)""".formatted(PACKAGE_LABEL);
            case MATERIAL -> """
                    SELECT CAST(id_nguon AS TEXT) AS k, ten || COALESCE(' (' || ma || ')', '') AS label FROM dm_vat_tu
                    WHERE CAST(id_nguon AS TEXT) IN (:keys)""";
            case BRAND -> "SELECT CAST(id_nguon AS TEXT) AS k, ten AS label FROM dm_thuong_hieu "
                    + "WHERE CAST(id_nguon AS TEXT) IN (:keys)";
            case KNOWLEDGE_DOC -> """
                    SELECT d.ma_tai_lieu AS k, d.ma_tai_lieu || COALESCE(' · ' || v.tieu_de, '') AS label
                    FROM kt_tai_lieu d
                    LEFT JOIN LATERAL (SELECT tieu_de FROM kt_phien_ban WHERE tai_lieu_id = d.id
                                       ORDER BY so_phien_ban DESC LIMIT 1) v ON true
                    WHERE d.ma_tai_lieu IN (:keys)""";
        };
        jdbc.query(sql, p, rs -> {
            out.put(rs.getString("k"), rs.getString("label"));
        });
        return out;
    }
}
