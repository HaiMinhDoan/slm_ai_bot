package vn.slmglobal.slmai.services.catalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Đọc vật tư, giá bán, ảnh và thành phần gói đã đồng bộ từ slm_v2 cho trang admin (chỉ đọc). Giá hiện hành như
 * slm_v2: lần ghi giá (dm_gia_vat_tu đang dùng) gần nhất; miền nào lần đó không có giá riêng thì lấy giá miền Bắc (HN)
 * của chính lần đó.
 */
@Repository
@RequiredArgsConstructor
public class CatalogMaterialRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    /**
     * @param status ACTIVE (mặc định) / INACTIVE / ALL
     * @param usage  USED: có trong gói đang bán; UNUSED: không thuộc gói đang bán nào; null: tất cả
     */
    public record Filter(String keyword, String groupCode, String status, String usage) {
    }

    /**
     * @param salePrices  giá bán hiện hành theo mã cơ sở ({"HN": 27000000, "HCM": 27000000})
     * @param northPriced các mã cơ sở mà lần ghi giá mới nhất không có giá riêng nên đang lấy giá miền Bắc
     * @param priceFrom   ngày áp dụng của lần ghi giá mới nhất
     */
    public record MaterialRow(int id, String code, String name, String groupCode, String groupName, String brand,
                              String unit, Integer warrantyMonths, Boolean main, boolean active, List<String> warnings,
                              JsonNode salePrices, List<String> northPriced, LocalDateTime priceFrom, int packageCount,
                              Integer imageFileId) {
    }

    public record Group(String code, String name, Boolean main) {
    }

    public record Image(int id, Integer fileId, Boolean main, boolean active) {
    }

    public record Price(int id, LocalDateTime effectiveFrom, JsonNode salePrices, boolean active) {
    }

    /** Một gói dùng vật tư: số lượng, giá bán một đơn vị lúc dựng gói */
    public record PackageUse(int itemId, int packageId, String packageName, String branchCode, boolean packageActive,
                             Double totalPrice, Integer quantity, Double salePrice, boolean customerVisible,
                             boolean itemActive) {
    }

    public record MaterialDetail(MaterialRow material, JsonNode specs, String quoteDescription,
                                 OffsetDateTime syncedAt, List<Image> images, List<Price> prices,
                                 List<PackageUse> packages) {
    }

    /**
     * Một dòng vật tư trong gói. {@code salePrice}: giá lúc dựng gói (slm_v2 vat_tu_tron_goi.gia);
     * {@code currentPrice}: giá bán hiện hành của vật tư ở miền của gói; {@code northPriced}: giá đó là giá miền Bắc
     * vì lần ghi giá mới nhất không có giá riêng của miền của gói.
     */
    public record PackageItem(int id, int materialId, String code, String name, String groupCode, String brand,
                              String unit, Integer quantity, Double salePrice, Double currentPrice,
                              boolean northPriced, boolean customerVisible, boolean active, String description, Integer warrantyMonths) {
    }

    /**
     * @param lineTotal    tổng giá lúc dựng gói của các dòng đang dùng (số lượng × giá); khác giá gói là bình thường
     * @param currentTotal tổng theo giá hiện hành; null nếu có dòng chưa có giá (kể cả giá miền Bắc)
     */
    public record PackageItems(int packageId, String packageName, String branchCode, Double totalPrice,
                               List<PackageItem> items, double lineTotal, Double currentTotal) {
    }

    /**
     * Vật tư đang dùng cho tool {@code tra_cuu_vat_tu}. {@code mainDevice}: thuộc nhóm thiết bị chính (tấm pin, biến
     * tần, pin lưu trữ) và không bị slm_v2 đánh dấu là phụ; website có trang và giá niêm yết cho các thiết bị này.
     * {@code price}: giá bán hiện hành ở cơ sở được hỏi.
     */
    public record QuotableMaterial(int id, String name, String groupCode, String groupName, boolean mainDevice,
                                   String brand, String unit, Integer warrantyMonths, JsonNode specs, Double price) {
    }

    /**
     * Giá hiện hành: {@code gia_moi} là lần ghi giá đang dùng gần nhất; {@code gia.gia_ban} là giá của từng cơ sở, cơ sở
     * nào lần đó không có giá riêng thì lấy giá HN (miền Bắc); {@code gia.theo_mien_bac} là các cơ sở đó.
     */
    private static final String CURRENT_PRICE = """
            LEFT JOIN LATERAL (
                SELECT g.gia_ban, g.hieu_luc_tu FROM dm_gia_vat_tu g
                WHERE g.vat_tu_id = v.id_nguon AND g.dang_dung
                ORDER BY g.hieu_luc_tu DESC NULLS LAST, g.id_nguon DESC LIMIT 1) gia_moi ON true
            LEFT JOIN LATERAL (
                SELECT jsonb_object_agg(c.ma, COALESCE(gia_moi.gia_ban -> c.ma, gia_moi.gia_ban -> 'HN')) AS gia_ban,
                       jsonb_agg(c.ma ORDER BY c.id_nguon) FILTER (WHERE gia_moi.gia_ban -> c.ma IS NULL) AS theo_mien_bac
                FROM dm_co_so c
                WHERE COALESCE(gia_moi.gia_ban -> c.ma, gia_moi.gia_ban -> 'HN') IS NOT NULL) gia ON true
            """;

    private static final String MATERIAL_FROM = """
            FROM dm_vat_tu v
            LEFT JOIN LATERAL (SELECT ten FROM dm_nhom_vat_tu WHERE ma = v.ma_nhom ORDER BY id_nguon LIMIT 1) nv ON true
            LEFT JOIN dm_thuong_hieu th ON th.id_nguon = v.thuong_hieu_id
            """ + CURRENT_PRICE + """
            LEFT JOIN LATERAL (
                SELECT count(DISTINCT i.tron_goi_id) AS so FROM dm_vat_tu_tron_goi i
                JOIN dm_tron_goi t ON t.id_nguon = i.tron_goi_id
                WHERE i.vat_tu_id = v.id_nguon AND i.dang_dung AND t.dang_dung) goi ON true
            LEFT JOIN LATERAL (
                SELECT a.tep_tin_id FROM dm_anh_vat_tu a WHERE a.vat_tu_id = v.id_nguon AND a.dang_dung
                ORDER BY a.anh_chinh DESC NULLS LAST, a.id_nguon LIMIT 1) anh ON true
            """;

    private static final String MATERIAL_COLUMNS = """
            SELECT v.id_nguon, v.ma, v.ten, v.ma_nhom, nv.ten AS ten_nhom, th.ten AS thuong_hieu, v.don_vi,
                   v.bao_hanh_thang, v.vat_tu_chinh, v.dang_dung, v.canh_bao, gia.gia_ban::text AS gia_ban,
                   gia.theo_mien_bac::text AS theo_mien_bac, gia_moi.hieu_luc_tu, COALESCE(goi.so, 0) AS so_goi,
                   anh.tep_tin_id AS anh_id
            """;

    /** Thiết bị chính lên trước (tấm pin, biến tần, pin lưu trữ), rồi theo tên */
    private static final String ORDER = """
            ORDER BY CASE v.ma_nhom WHEN 'TAM_PIN' THEN 1 WHEN 'BIEN_TAN' THEN 2 WHEN 'PIN_LUU_TRU' THEN 3 ELSE 4 END,
                     v.ma_nhom, v.ten, v.id_nguon
            """;

    public List<MaterialRow> list(Filter f, int page, int size) {
        MapSqlParameterSource p = params(f).addValue("limit", size).addValue("offset", (long) page * size);
        return jdbc.query(MATERIAL_COLUMNS + MATERIAL_FROM + where(f) + ORDER + "LIMIT :limit OFFSET :offset", p,
                (rs, i) -> material(rs));
    }

    public long count(Filter f) {
        Long n = jdbc.queryForObject("SELECT count(*) " + MATERIAL_FROM + where(f), params(f), Long.class);
        return n == null ? 0 : n;
    }

    public List<Group> groups() {
        return jdbc.query("""
                SELECT ma, ten, vat_tu_chinh FROM dm_nhom_vat_tu WHERE dang_dung AND ma IS NOT NULL
                ORDER BY vat_tu_chinh DESC NULLS LAST, id_nguon""", new MapSqlParameterSource(),
                (rs, i) -> new Group(rs.getString("ma"), rs.getString("ten"), bool(rs, "vat_tu_chinh")));
    }

    public Optional<MaterialDetail> detail(int id) {
        MapSqlParameterSource p = new MapSqlParameterSource("id", id);
        List<MaterialDetail> found = jdbc.query(MATERIAL_COLUMNS + """
                , v.thong_so::text AS thong_so, v.mo_ta_bao_gia, v.dong_bo_luc
                """ + MATERIAL_FROM + "WHERE v.id_nguon = :id", p, (rs, i) -> new MaterialDetail(material(rs),
                readJson(rs.getString("thong_so")), rs.getString("mo_ta_bao_gia"),
                rs.getObject("dong_bo_luc", OffsetDateTime.class), null, null, null));
        if (found.isEmpty()) {
            return Optional.empty();
        }
        MaterialDetail d = found.get(0);
        List<Image> images = jdbc.query("""
                SELECT id_nguon, tep_tin_id, anh_chinh, dang_dung FROM dm_anh_vat_tu WHERE vat_tu_id = :id
                ORDER BY dang_dung DESC, anh_chinh DESC NULLS LAST, id_nguon""", p,
                (rs, i) -> new Image(rs.getInt("id_nguon"), integer(rs, "tep_tin_id"), bool(rs, "anh_chinh"),
                        rs.getBoolean("dang_dung")));
        List<Price> prices = jdbc.query("""
                SELECT id_nguon, hieu_luc_tu, gia_ban::text AS gia_ban, dang_dung FROM dm_gia_vat_tu
                WHERE vat_tu_id = :id ORDER BY hieu_luc_tu DESC NULLS LAST, id_nguon DESC""", p,
                (rs, i) -> new Price(rs.getInt("id_nguon"), rs.getObject("hieu_luc_tu", LocalDateTime.class),
                        readJson(rs.getString("gia_ban")), rs.getBoolean("dang_dung")));
        List<PackageUse> packages = jdbc.query("""
                SELECT i.id_nguon AS dong_id, t.id_nguon, t.ten, t.ma_co_so, t.dang_dung AS goi_dang_dung, t.tong_gia,
                       i.so_luong,
                       i.gia_ban, i.hien_thi_khach, i.dang_dung
                FROM dm_vat_tu_tron_goi i JOIN dm_tron_goi t ON t.id_nguon = i.tron_goi_id
                WHERE i.vat_tu_id = :id
                ORDER BY t.dang_dung DESC, i.dang_dung DESC, t.ma_co_so, t.ten, t.id_nguon""", p,
                (rs, i) -> new PackageUse(rs.getInt("dong_id"), rs.getInt("id_nguon"), rs.getString("ten"), rs.getString("ma_co_so"),
                        rs.getBoolean("goi_dang_dung"), decimal(rs, "tong_gia"), integer(rs, "so_luong"),
                        decimal(rs, "gia_ban"), rs.getBoolean("hien_thi_khach"), rs.getBoolean("dang_dung")));
        return Optional.of(new MaterialDetail(d.material(), d.specs(), d.quoteDescription(), d.syncedAt(), images,
                prices, packages));
    }

    public Optional<PackageItems> packageItems(int packageId) {
        MapSqlParameterSource p = new MapSqlParameterSource("id", packageId);
        List<Object[]> head = jdbc.query("SELECT ten, ma_co_so, tong_gia FROM dm_tron_goi WHERE id_nguon = :id", p,
                (rs, i) -> new Object[]{rs.getString("ten"), rs.getString("ma_co_so"), decimal(rs, "tong_gia")});
        if (head.isEmpty()) {
            return Optional.empty();
        }
        String branch = (String) head.get(0)[1];
        List<PackageItem> items = jdbc.query("""
                SELECT i.id_nguon, v.id_nguon AS vat_tu_id, v.ma, v.ten, v.ma_nhom, th.ten AS thuong_hieu, v.don_vi,
                       i.so_luong, i.gia_ban, (gia.gia_ban->>:coSo)::numeric AS gia_hien_hanh,
                       gia_moi.gia_ban -> :coSo IS NULL AND gia.gia_ban -> :coSo IS NOT NULL AS theo_mien_bac,
                       i.hien_thi_khach,
                       i.dang_dung, i.mo_ta,
                       COALESCE(NULLIF(i.bao_hanh_thang, 0), NULLIF(v.bao_hanh_thang, 0)) AS bao_hanh
                FROM dm_vat_tu_tron_goi i
                JOIN dm_vat_tu v ON v.id_nguon = i.vat_tu_id
                LEFT JOIN dm_thuong_hieu th ON th.id_nguon = v.thuong_hieu_id
                """ + CURRENT_PRICE + """
                WHERE i.tron_goi_id = :id
                ORDER BY i.dang_dung DESC, CASE v.ma_nhom WHEN 'TAM_PIN' THEN 1 WHEN 'BIEN_TAN' THEN 2
                         WHEN 'PIN_LUU_TRU' THEN 3 ELSE 4 END, i.hien_thi_khach DESC, i.id_nguon""",
                p.addValue("coSo", branch == null ? "" : branch),
                (rs, i) -> new PackageItem(rs.getInt("id_nguon"), rs.getInt("vat_tu_id"), rs.getString("ma"),
                        rs.getString("ten"), rs.getString("ma_nhom"), rs.getString("thuong_hieu"),
                        rs.getString("don_vi"), integer(rs, "so_luong"), decimal(rs, "gia_ban"),
                        decimal(rs, "gia_hien_hanh"), rs.getBoolean("theo_mien_bac"), rs.getBoolean("hien_thi_khach"),
                        rs.getBoolean("dang_dung"), rs.getString("mo_ta"), integer(rs, "bao_hanh")));
        double lineTotal = 0;
        double currentTotal = 0;
        boolean currentComplete = true;
        for (PackageItem it : items) {
            if (!it.active()) {
                continue;
            }
            int q = it.quantity() == null ? 0 : it.quantity();
            lineTotal += q * (it.salePrice() == null ? 0 : it.salePrice());
            if (it.currentPrice() == null) {
                currentComplete = false;
            } else {
                currentTotal += q * it.currentPrice();
            }
        }
        return Optional.of(new PackageItems(packageId, (String) head.get(0)[0], branch, (Double) head.get(0)[2],
                items, lineTotal, currentComplete ? currentTotal : null));
    }

    /** Vật tư đang dùng kèm giá hiện hành ở một cơ sở, thiết bị chính lên trước */
    public List<QuotableMaterial> quotableMaterials(String branchCode) {
        return jdbc.query("""
                SELECT v.id_nguon, v.ten, v.ma_nhom, nv.ten AS ten_nhom, th.ten AS thuong_hieu, v.don_vi,
                       v.bao_hanh_thang, COALESCE(nv.vat_tu_chinh, false) AND COALESCE(v.vat_tu_chinh, true) AS chinh,
                       v.thong_so::text AS thong_so, (gia.gia_ban ->> :coSo)::numeric AS gia
                FROM dm_vat_tu v
                LEFT JOIN LATERAL (SELECT ten, vat_tu_chinh FROM dm_nhom_vat_tu WHERE ma = v.ma_nhom
                                   ORDER BY id_nguon LIMIT 1) nv ON true
                LEFT JOIN dm_thuong_hieu th ON th.id_nguon = v.thuong_hieu_id
                """ + CURRENT_PRICE + "WHERE v.dang_dung\n" + ORDER,
                new MapSqlParameterSource("coSo", branchCode),
                (rs, i) -> new QuotableMaterial(rs.getInt("id_nguon"), rs.getString("ten"), rs.getString("ma_nhom"),
                        rs.getString("ten_nhom"), rs.getBoolean("chinh"), rs.getString("thuong_hieu"),
                        rs.getString("don_vi"), integer(rs, "bao_hanh_thang"), readJson(rs.getString("thong_so")),
                        decimal(rs, "gia")));
    }

    /** Vật tư → các gói của một cơ sở có dòng đang dùng là vật tư đó (chưa lọc gói được báo giá) */
    public Map<Integer, List<Integer>> packagesUsing(Collection<Integer> materialIds, String branchCode) {
        Map<Integer, List<Integer>> out = new HashMap<>();
        if (materialIds.isEmpty()) {
            return out;
        }
        jdbc.query("""
                SELECT DISTINCT i.vat_tu_id, i.tron_goi_id FROM dm_vat_tu_tron_goi i
                JOIN dm_tron_goi t ON t.id_nguon = i.tron_goi_id
                WHERE i.dang_dung AND t.ma_co_so = :coSo AND i.vat_tu_id IN (:ids)""",
                new MapSqlParameterSource("coSo", branchCode).addValue("ids", materialIds),
                rs -> {
                    out.computeIfAbsent(rs.getInt("vat_tu_id"), k -> new ArrayList<>()).add(rs.getInt("tron_goi_id"));
                });
        return out;
    }

    /** Đường dẫn ảnh (MinIO của slm_v2) của một tep_tin_id đã đồng bộ: ảnh vật tư, ảnh gói, logo hãng */
    public Optional<String> imageUrl(int fileId) {
        return jdbc.query("""
                SELECT duong_dan AS url FROM dm_anh_vat_tu WHERE tep_tin_id = :id AND duong_dan IS NOT NULL
                UNION ALL SELECT anh_url FROM dm_tron_goi WHERE tep_tin_id = :id AND anh_url IS NOT NULL
                UNION ALL SELECT anh_url FROM dm_thuong_hieu WHERE tep_tin_id = :id AND anh_url IS NOT NULL
                LIMIT 1""", new MapSqlParameterSource("id", fileId), (rs, i) -> rs.getString("url"))
                .stream().findFirst();
    }

    private MaterialRow material(ResultSet rs) throws SQLException {
        Array w = rs.getArray("canh_bao");
        return new MaterialRow(rs.getInt("id_nguon"), rs.getString("ma"), rs.getString("ten"),
                rs.getString("ma_nhom"), rs.getString("ten_nhom"), rs.getString("thuong_hieu"),
                rs.getString("don_vi"), integer(rs, "bao_hanh_thang"), bool(rs, "vat_tu_chinh"),
                rs.getBoolean("dang_dung"), w == null ? List.of() : List.of((String[]) w.getArray()),
                readJson(rs.getString("gia_ban")), codes(rs.getString("theo_mien_bac")),
                rs.getObject("hieu_luc_tu", LocalDateTime.class), rs.getInt("so_goi"), integer(rs, "anh_id"));
    }

    private static String where(Filter f) {
        StringBuilder w = new StringBuilder("WHERE true");
        String status = f.status() == null ? "ACTIVE" : f.status();
        if ("ACTIVE".equals(status)) {
            w.append(" AND v.dang_dung");
        } else if ("INACTIVE".equals(status)) {
            w.append(" AND NOT v.dang_dung");
        }
        if (f.groupCode() != null && !f.groupCode().isBlank()) {
            w.append(" AND v.ma_nhom = :nhom");
        }
        if ("USED".equals(f.usage())) {
            w.append(" AND COALESCE(goi.so, 0) > 0");
        } else if ("UNUSED".equals(f.usage())) {
            w.append(" AND COALESCE(goi.so, 0) = 0");
        }
        if (keyword(f) != null) {
            w.append(" AND (lower(v.ten) LIKE :kw OR lower(v.ma) LIKE :kw)");
        }
        return w.append('\n').toString();
    }

    private static MapSqlParameterSource params(Filter f) {
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("nhom", f.groupCode());
        String kw = keyword(f);
        if (kw != null) {
            p.addValue("kw", "%" + kw.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%");
        }
        return p;
    }

    private static String keyword(Filter f) {
        return f.keyword() == null || f.keyword().isBlank() ? null : f.keyword().strip().toLowerCase(Locale.ROOT);
    }

    private List<String> codes(String jsonArray) {
        List<String> out = new ArrayList<>();
        readJson(jsonArray).forEach(c -> out.add(c.asText()));
        return out;
    }

    private JsonNode readJson(String json) {
        if (json == null) {
            return objectMapper.createObjectNode();
        }
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            return objectMapper.createObjectNode();
        }
    }

    private static Integer integer(ResultSet rs, String column) throws SQLException {
        int v = rs.getInt(column);
        return rs.wasNull() ? null : v;
    }

    private static Double decimal(ResultSet rs, String column) throws SQLException {
        double v = rs.getDouble(column);
        return rs.wasNull() ? null : v;
    }

    private static Boolean bool(ResultSet rs, String column) throws SQLException {
        boolean v = rs.getBoolean(column);
        return rs.wasNull() ? null : v;
    }
}
