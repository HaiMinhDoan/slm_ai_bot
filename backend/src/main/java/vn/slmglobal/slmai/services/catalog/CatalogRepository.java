package vn.slmglobal.slmai.services.catalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Bảng catalog dm_* (V7): ghi khi đồng bộ, đọc cho tool báo giá. Dùng SQL vì cần upsert theo id_nguon
 * ({@code ON CONFLICT ... DO UPDATE}) và các truy vấn gộp thương hiệu / thành phần.
 */
@Repository
@RequiredArgsConstructor
public class CatalogRepository {

    /** ma_bam của dòng bị ngừng vì biến mất ở nguồn: xuất hiện lại thì luôn được ghi lại */
    static final String GONE_HASH = "0".repeat(64);

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    /** Gói đọc cho tool (kèm tên thương hiệu) */
    public record CatalogPackage(int id, String branchCode, Integer familyId, String familyName, String name,
                                 String systemType, Integer phases, Double kwp, Integer panelCount, Double panelWp,
                                 Double inverterKw, double storageKwh, Double totalPrice, double steelFramePrice,
                                 boolean bestSeller, boolean active, boolean recommendable, List<String> warnings,
                                 String panelBrand, String inverterBrand, String batteryBrand) {
    }

    /** Thành phần hiển thị cho khách của một gói */
    public record CatalogItem(String name, String groupCode, String brand, Integer quantity, String unit,
                              Integer warrantyMonths) {
    }

    public record SyncRun(long id, String trigger, String status, OffsetDateTime startedAt, OffsetDateTime finishedAt,
                          Integer inserted, Integer updated, Integer deactivated, JsonNode warnings, String error,
                          String version, String triggeredBy) {
    }

    public record Counts(int inserted, int updated, int deactivated) {

        Counts plus(Counts o) {
            return new Counts(inserted + o.inserted, updated + o.updated, deactivated + o.deactivated);
        }
    }

    // ------------------------------------------------------------------ ghi khi đồng bộ

    /** id_nguon → ma_bam của một bảng dm_* */
    public Map<Integer, String> hashes(String table) {
        Map<Integer, String> out = new HashMap<>();
        jdbc.query("SELECT id_nguon, ma_bam FROM " + table, rs -> {
            out.put(rs.getInt(1), rs.getString(2));
        });
        return out;
    }

    public int countActivePackages() {
        Integer n = jdbc.queryForObject("SELECT count(*) FROM dm_tron_goi WHERE dang_dung", Map.of(), Integer.class);
        return n == null ? 0 : n;
    }

    /** Khoá chống chạy đồng bộ song song (nhiều tiến trình); tự nhả khi transaction kết thúc */
    public boolean tryLock() {
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT pg_try_advisory_xact_lock(hashtext('dm_dong_bo'))",
                Map.of(), Boolean.class));
    }

    public Counts upsertBrands(List<CatalogRows.BrandRow> rows) {
        return upsert("dm_thuong_hieu", rows, CatalogRows.BrandRow::id, CatalogRows.BrandRow::hash, """
                INSERT INTO dm_thuong_hieu (id_nguon, ten, ten_quoc_te, quoc_gia, mo_ta, dang_dung, ma_bam, dong_bo_luc)
                VALUES (:id, :ten, :tenQuocTe, :quocGia, :moTa, :dangDung, :maBam, now())
                ON CONFLICT (id_nguon) DO UPDATE SET ten = EXCLUDED.ten, ten_quoc_te = EXCLUDED.ten_quoc_te,
                    quoc_gia = EXCLUDED.quoc_gia, mo_ta = EXCLUDED.mo_ta, dang_dung = EXCLUDED.dang_dung,
                    ma_bam = EXCLUDED.ma_bam, dong_bo_luc = now()""",
                r -> new MapSqlParameterSource().addValue("id", r.id()).addValue("ten", r.name())
                        .addValue("tenQuocTe", r.nameIntl()).addValue("quocGia", r.country())
                        .addValue("moTa", r.description()).addValue("dangDung", r.active()).addValue("maBam", r.hash()));
    }

    public Counts upsertBranches(List<CatalogRows.BranchRow> rows) {
        return upsert("dm_co_so", rows, CatalogRows.BranchRow::id, CatalogRows.BranchRow::hash, """
                INSERT INTO dm_co_so (id_nguon, ma, ten, dia_chi_van_phong, sdt, dang_dung, ma_bam, dong_bo_luc)
                VALUES (:id, :ma, :ten, :diaChi, :sdt, :dangDung, :maBam, now())
                ON CONFLICT (id_nguon) DO UPDATE SET ma = EXCLUDED.ma, ten = EXCLUDED.ten,
                    dia_chi_van_phong = EXCLUDED.dia_chi_van_phong, sdt = EXCLUDED.sdt,
                    dang_dung = EXCLUDED.dang_dung, ma_bam = EXCLUDED.ma_bam, dong_bo_luc = now()""",
                r -> new MapSqlParameterSource().addValue("id", r.id()).addValue("ma", r.code())
                        .addValue("ten", r.name()).addValue("diaChi", r.address()).addValue("sdt", r.phone())
                        .addValue("dangDung", r.active()).addValue("maBam", r.hash()));
    }

    public Counts upsertMaterials(List<CatalogRows.MaterialRow> rows) {
        return upsert("dm_vat_tu", rows, CatalogRows.MaterialRow::id, CatalogRows.MaterialRow::hash, """
                INSERT INTO dm_vat_tu (id_nguon, ma, ten, ma_nhom, thuong_hieu_id, don_vi, bao_hanh_thang,
                                       vat_tu_chinh, thong_so, mo_ta_bao_gia, dang_dung, canh_bao, ma_bam, dong_bo_luc)
                VALUES (:id, :ma, :ten, :maNhom, :thuongHieu, :donVi, :baoHanh, :chinh, CAST(:thongSo AS jsonb),
                        :moTa, :dangDung, CAST(:canhBao AS text[]), :maBam, now())
                ON CONFLICT (id_nguon) DO UPDATE SET ma = EXCLUDED.ma, ten = EXCLUDED.ten, ma_nhom = EXCLUDED.ma_nhom,
                    thuong_hieu_id = EXCLUDED.thuong_hieu_id, don_vi = EXCLUDED.don_vi,
                    bao_hanh_thang = EXCLUDED.bao_hanh_thang, vat_tu_chinh = EXCLUDED.vat_tu_chinh,
                    thong_so = EXCLUDED.thong_so, mo_ta_bao_gia = EXCLUDED.mo_ta_bao_gia,
                    dang_dung = EXCLUDED.dang_dung, canh_bao = EXCLUDED.canh_bao, ma_bam = EXCLUDED.ma_bam,
                    dong_bo_luc = now()""",
                r -> new MapSqlParameterSource().addValue("id", r.id()).addValue("ma", r.code())
                        .addValue("ten", r.name()).addValue("maNhom", r.groupCode()).addValue("thuongHieu", r.brandId())
                        .addValue("donVi", r.unit()).addValue("baoHanh", r.warrantyMonths()).addValue("chinh", r.main())
                        .addValue("thongSo", json(r.specs())).addValue("moTa", r.quoteDescription())
                        .addValue("dangDung", r.active()).addValue("canhBao", textArray(r.warnings()))
                        .addValue("maBam", r.hash()));
    }

    public Counts upsertPackages(List<CatalogRows.PackageRow> rows) {
        return upsert("dm_tron_goi", rows, CatalogRows.PackageRow::id, CatalogRows.PackageRow::hash, """
                INSERT INTO dm_tron_goi (id_nguon, ma_co_so, nhom_tron_goi_id, ten_nhom_goi, thuong_hieu_tam_pin_id,
                    thuong_hieu_inverter_id, thuong_hieu_pin_luu_tru_id, ten, loai_he_thong, so_pha, cong_suat_kwp,
                    so_tam_pin, cong_suat_tam_wp, cong_suat_inverter_kw, luu_tru_kwh, san_luong_thang_min,
                    san_luong_thang_max, tong_gia, gia_khung_sat, ban_chay, tep_tin_id, mo_ta, dang_dung, duoc_goi_y,
                    canh_bao, ma_bam, dong_bo_luc)
                VALUES (:id, :coSo, :nhom, :tenNhom, :thTamPin, :thInverter, :thPin, :ten, :loai, :pha, :kwp,
                    :soTam, :wp, :inverterKw, :luuTru, :slMin, :slMax, :gia, :khungSat, :banChay, :tepTin, :moTa,
                    :dangDung, :goiY, CAST(:canhBao AS text[]), :maBam, now())
                ON CONFLICT (id_nguon) DO UPDATE SET ma_co_so = EXCLUDED.ma_co_so,
                    nhom_tron_goi_id = EXCLUDED.nhom_tron_goi_id, ten_nhom_goi = EXCLUDED.ten_nhom_goi,
                    thuong_hieu_tam_pin_id = EXCLUDED.thuong_hieu_tam_pin_id,
                    thuong_hieu_inverter_id = EXCLUDED.thuong_hieu_inverter_id,
                    thuong_hieu_pin_luu_tru_id = EXCLUDED.thuong_hieu_pin_luu_tru_id, ten = EXCLUDED.ten,
                    loai_he_thong = EXCLUDED.loai_he_thong, so_pha = EXCLUDED.so_pha,
                    cong_suat_kwp = EXCLUDED.cong_suat_kwp, so_tam_pin = EXCLUDED.so_tam_pin,
                    cong_suat_tam_wp = EXCLUDED.cong_suat_tam_wp, cong_suat_inverter_kw = EXCLUDED.cong_suat_inverter_kw,
                    luu_tru_kwh = EXCLUDED.luu_tru_kwh, san_luong_thang_min = EXCLUDED.san_luong_thang_min,
                    san_luong_thang_max = EXCLUDED.san_luong_thang_max, tong_gia = EXCLUDED.tong_gia,
                    gia_khung_sat = EXCLUDED.gia_khung_sat, ban_chay = EXCLUDED.ban_chay,
                    tep_tin_id = EXCLUDED.tep_tin_id, mo_ta = EXCLUDED.mo_ta, dang_dung = EXCLUDED.dang_dung,
                    duoc_goi_y = EXCLUDED.duoc_goi_y, canh_bao = EXCLUDED.canh_bao, ma_bam = EXCLUDED.ma_bam,
                    dong_bo_luc = now()""",
                r -> new MapSqlParameterSource().addValue("id", r.id()).addValue("coSo", r.branchCode())
                        .addValue("nhom", r.familyId()).addValue("tenNhom", r.familyName())
                        .addValue("thTamPin", r.panelBrandId()).addValue("thInverter", r.inverterBrandId())
                        .addValue("thPin", r.batteryBrandId()).addValue("ten", r.name())
                        .addValue("loai", r.systemType()).addValue("pha", r.phases()).addValue("kwp", r.kwp())
                        .addValue("soTam", r.panelCount()).addValue("wp", r.panelWp())
                        .addValue("inverterKw", r.inverterKw()).addValue("luuTru", r.storageKwh())
                        .addValue("slMin", r.outputMin()).addValue("slMax", r.outputMax())
                        .addValue("gia", r.totalPrice()).addValue("khungSat", r.steelFramePrice())
                        .addValue("banChay", r.bestSeller()).addValue("tepTin", r.imageFileId())
                        .addValue("moTa", r.description()).addValue("dangDung", r.active())
                        .addValue("goiY", r.recommendable()).addValue("canhBao", textArray(r.warnings()))
                        .addValue("maBam", r.hash()));
    }

    public Counts upsertItems(List<CatalogRows.ItemRow> rows) {
        return upsert("dm_vat_tu_tron_goi", rows, CatalogRows.ItemRow::id, CatalogRows.ItemRow::hash, """
                INSERT INTO dm_vat_tu_tron_goi (id_nguon, tron_goi_id, vat_tu_id, so_luong, bao_hanh_thang,
                    duoc_bao_hanh, hien_thi_khach, dang_dung, ma_bam, dong_bo_luc)
                VALUES (:id, :goi, :vatTu, :soLuong, :baoHanh, :duocBaoHanh, :hienThi, :dangDung, :maBam, now())
                ON CONFLICT (id_nguon) DO UPDATE SET tron_goi_id = EXCLUDED.tron_goi_id,
                    vat_tu_id = EXCLUDED.vat_tu_id, so_luong = EXCLUDED.so_luong,
                    bao_hanh_thang = EXCLUDED.bao_hanh_thang, duoc_bao_hanh = EXCLUDED.duoc_bao_hanh,
                    hien_thi_khach = EXCLUDED.hien_thi_khach, dang_dung = EXCLUDED.dang_dung,
                    ma_bam = EXCLUDED.ma_bam, dong_bo_luc = now()""",
                r -> new MapSqlParameterSource().addValue("id", r.id()).addValue("goi", r.packageId())
                        .addValue("vatTu", r.materialId()).addValue("soLuong", r.quantity())
                        .addValue("baoHanh", r.warrantyMonths()).addValue("duocBaoHanh", r.hasWarranty())
                        .addValue("hienThi", r.customerVisible()).addValue("dangDung", r.active())
                        .addValue("maBam", r.hash()));
    }

    public Counts upsertDomains(List<CatalogRows.DomainRow> rows) {
        return upsert("dm_ten_mien", rows, CatalogRows.DomainRow::id, CatalogRows.DomainRow::hash, """
                INSERT INTO dm_ten_mien (id_nguon, ten_mien, ma_co_so, dang_dung, ma_bam, dong_bo_luc)
                VALUES (:id, :tenMien, :coSo, :dangDung, :maBam, now())
                ON CONFLICT (id_nguon) DO UPDATE SET ten_mien = EXCLUDED.ten_mien, ma_co_so = EXCLUDED.ma_co_so,
                    dang_dung = EXCLUDED.dang_dung, ma_bam = EXCLUDED.ma_bam, dong_bo_luc = now()""",
                r -> new MapSqlParameterSource().addValue("id", r.id()).addValue("tenMien", r.domain())
                        .addValue("coSo", r.branchCode()).addValue("dangDung", r.active()).addValue("maBam", r.hash()));
    }

    /**
     * Ghi các dòng mới hoặc có ma_bam khác; dòng có ở bot mà không còn ở nguồn thì ngừng (dang_dung = false,
     * không xoá, vì lịch sử hội thoại có thể đã nhắc tới).
     */
    private <T> Counts upsert(String table, List<T> rows, Function<T, Integer> id, Function<T, String> hash,
                              String sql, Function<T, MapSqlParameterSource> params) {
        Map<Integer, String> existing = hashes(table);
        List<MapSqlParameterSource> batch = new ArrayList<>();
        int inserted = 0;
        int updated = 0;
        List<Integer> ids = new ArrayList<>(rows.size());
        for (T r : rows) {
            ids.add(id.apply(r));
            String old = existing.get(id.apply(r));
            if (old == null) {
                inserted++;
            } else if (!old.equals(hash.apply(r))) {
                updated++;
            } else {
                continue;
            }
            batch.add(params.apply(r));
        }
        if (!batch.isEmpty()) {
            jdbc.batchUpdate(sql, batch.toArray(MapSqlParameterSource[]::new));
        }
        int gone = jdbc.update("UPDATE " + table + " SET dang_dung = false, ma_bam = :gone, dong_bo_luc = now() "
                        + "WHERE ma_bam <> :gone AND NOT (id_nguon = ANY (CAST(:ids AS int[])))",
                new MapSqlParameterSource().addValue("gone", GONE_HASH).addValue("ids", intArray(ids)));
        if (table.equals("dm_tron_goi") && gone > 0) {
            jdbc.update("UPDATE dm_tron_goi SET duoc_goi_y = false WHERE ma_bam = :gone",
                    Map.of("gone", GONE_HASH));
        }
        return new Counts(inserted, updated, gone);
    }

    // ------------------------------------------------------------------ lịch sử đồng bộ

    public long startRun(String trigger, String triggeredBy) {
        Long id = jdbc.queryForObject("""
                INSERT INTO dm_lich_su_dong_bo (kieu_kich_hoat, trang_thai, nguoi_kich_hoat)
                VALUES (:kieu, 'RUNNING', :nguoi) RETURNING id""",
                new MapSqlParameterSource().addValue("kieu", trigger).addValue("nguoi", triggeredBy), Long.class);
        return id == null ? 0 : id;
    }

    public void finishRun(long id, String status, Counts counts, Map<String, List<Integer>> warnings, String error,
                          String version) {
        jdbc.update("""
                UPDATE dm_lich_su_dong_bo SET trang_thai = :trangThai, ket_thuc_luc = now(), so_them_moi = :them,
                    so_cap_nhat = :capNhat, so_ngung = :ngung, canh_bao = CAST(:canhBao AS jsonb),
                    thong_bao_loi = :loi, phien_ban_catalog = :phienBan
                WHERE id = :id""", new MapSqlParameterSource().addValue("id", id).addValue("trangThai", status)
                .addValue("them", counts == null ? null : counts.inserted())
                .addValue("capNhat", counts == null ? null : counts.updated())
                .addValue("ngung", counts == null ? null : counts.deactivated())
                .addValue("canhBao", warnings == null ? null : json(warnings))
                .addValue("loi", error).addValue("phienBan", version));
    }

    /** Lần chạy dở dang do tiến trình bị tắt giữa chừng */
    public int failStaleRuns() {
        return jdbc.update("""
                UPDATE dm_lich_su_dong_bo SET trang_thai = 'FAILED', ket_thuc_luc = now(),
                    thong_bao_loi = 'Tiến trình dừng giữa chừng'
                WHERE trang_thai = 'RUNNING' AND bat_dau_luc < now() - interval '30 minutes'""", Map.of());
    }

    public List<SyncRun> runs(int limit) {
        return jdbc.query("SELECT * FROM dm_lich_su_dong_bo ORDER BY id DESC LIMIT :limit",
                Map.of("limit", limit), runMapper());
    }

    public Optional<SyncRun> lastSuccess() {
        return jdbc.query("SELECT * FROM dm_lich_su_dong_bo WHERE trang_thai = 'SUCCESS' ORDER BY id DESC LIMIT 1",
                Map.of(), runMapper()).stream().findFirst();
    }

    // ------------------------------------------------------------------ đọc cho tool báo giá

    private static final String PACKAGE_SELECT = """
            SELECT g.*, tp.ten AS th_tam_pin, ti.ten AS th_inverter, tl.ten AS th_pin
            FROM dm_tron_goi g
            LEFT JOIN dm_thuong_hieu tp ON tp.id_nguon = g.thuong_hieu_tam_pin_id
            LEFT JOIN dm_thuong_hieu ti ON ti.id_nguon = g.thuong_hieu_inverter_id
            LEFT JOIN dm_thuong_hieu tl ON tl.id_nguon = g.thuong_hieu_pin_luu_tru_id
            """;

    /** Gói đang bán, được gợi ý, của một cơ sở giá */
    public List<CatalogPackage> recommendablePackages(String branchCode) {
        return jdbc.query(PACKAGE_SELECT + " WHERE g.ma_co_so = :coSo AND g.dang_dung AND g.duoc_goi_y "
                + "ORDER BY g.tong_gia, g.id_nguon", Map.of("coSo", branchCode), packageMapper());
    }

    public boolean hasRecommendablePackages() {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM dm_tron_goi WHERE dang_dung AND duoc_goi_y)", Map.of(), Boolean.class));
    }

    public List<CatalogPackage> allPackages() {
        return jdbc.query(PACKAGE_SELECT + " ORDER BY g.ma_co_so, g.loai_he_thong, g.so_pha, g.cong_suat_kwp, "
                + "g.id_nguon", Map.of(), packageMapper());
    }

    public Optional<CatalogPackage> findPackage(int id) {
        return jdbc.query(PACKAGE_SELECT + " WHERE g.id_nguon = :id", Map.of("id", id), packageMapper())
                .stream().findFirst();
    }

    /** Gói cùng tên, cùng nhóm ở cơ sở giá kia (mỗi gói có một bản HN và một bản HCM) */
    public Optional<CatalogPackage> findTwin(CatalogPackage p, String branchCode) {
        return jdbc.query(PACKAGE_SELECT + """
                 WHERE g.ma_co_so = :coSo AND g.dang_dung AND lower(trim(g.ten)) = lower(trim(:ten))
                   AND g.nhom_tron_goi_id IS NOT DISTINCT FROM :nhom
                 ORDER BY g.duoc_goi_y DESC, g.id_nguon LIMIT 1""",
                new MapSqlParameterSource().addValue("coSo", branchCode).addValue("ten", p.name())
                        .addValue("nhom", p.familyId()), packageMapper()).stream().findFirst();
    }

    public List<CatalogItem> visibleItems(int packageId) {
        return jdbc.query("""
                SELECT v.ten, v.ma_nhom, th.ten AS thuong_hieu, i.so_luong, v.don_vi,
                       COALESCE(NULLIF(i.bao_hanh_thang, 0), NULLIF(v.bao_hanh_thang, 0)) AS bao_hanh
                FROM dm_vat_tu_tron_goi i
                JOIN dm_vat_tu v ON v.id_nguon = i.vat_tu_id
                LEFT JOIN dm_thuong_hieu th ON th.id_nguon = v.thuong_hieu_id
                WHERE i.tron_goi_id = :id AND i.dang_dung AND i.hien_thi_khach
                ORDER BY CASE v.ma_nhom WHEN 'TAM_PIN' THEN 1 WHEN 'BIEN_TAN' THEN 2 WHEN 'PIN_LUU_TRU' THEN 3
                         ELSE 4 END, i.id_nguon""", Map.of("id", packageId), (rs, i) -> new CatalogItem(
                rs.getString("ten"), rs.getString("ma_nhom"), rs.getString("thuong_hieu"),
                integer(rs, "so_luong"), rs.getString("don_vi"), integer(rs, "bao_hanh")));
    }

    /** Tên miền website đang dùng của một cơ sở giá */
    public List<String> domains(String branchCode) {
        return jdbc.queryForList("SELECT ten_mien FROM dm_ten_mien WHERE ma_co_so = :coSo AND dang_dung "
                + "ORDER BY id_nguon", Map.of("coSo", branchCode), String.class);
    }

    private RowMapper<CatalogPackage> packageMapper() {
        return (rs, i) -> new CatalogPackage(rs.getInt("id_nguon"), rs.getString("ma_co_so"),
                integer(rs, "nhom_tron_goi_id"), rs.getString("ten_nhom_goi"), rs.getString("ten"),
                rs.getString("loai_he_thong"), integer(rs, "so_pha"), decimal(rs, "cong_suat_kwp"),
                integer(rs, "so_tam_pin"), decimal(rs, "cong_suat_tam_wp"), decimal(rs, "cong_suat_inverter_kw"),
                rs.getDouble("luu_tru_kwh"), decimal(rs, "tong_gia"), rs.getDouble("gia_khung_sat"),
                rs.getBoolean("ban_chay"), rs.getBoolean("dang_dung"), rs.getBoolean("duoc_goi_y"),
                strings(rs.getArray("canh_bao")), rs.getString("th_tam_pin"), rs.getString("th_inverter"),
                rs.getString("th_pin"));
    }

    private RowMapper<SyncRun> runMapper() {
        return (rs, i) -> new SyncRun(rs.getLong("id"), rs.getString("kieu_kich_hoat"), rs.getString("trang_thai"),
                rs.getObject("bat_dau_luc", OffsetDateTime.class), rs.getObject("ket_thuc_luc", OffsetDateTime.class),
                integer(rs, "so_them_moi"), integer(rs, "so_cap_nhat"), integer(rs, "so_ngung"),
                readJson(rs.getString("canh_bao")), rs.getString("thong_bao_loi"), rs.getString("phien_ban_catalog"),
                rs.getString("nguoi_kich_hoat"));
    }

    private JsonNode readJson(String json) {
        try {
            return json == null ? null : objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /** Mảng text[] dạng chữ; mã cảnh báo chỉ gồm A-Z và _ nên không cần thoát ký tự */
    private static String textArray(List<String> values) {
        return "{" + String.join(",", values == null ? List.of() : values) + "}";
    }

    private static String intArray(List<Integer> values) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(values.get(i));
        }
        return sb.append('}').toString();
    }

    private static List<String> strings(Array array) throws SQLException {
        return array == null ? List.of() : Arrays.asList((String[]) array.getArray());
    }

    private static Integer integer(ResultSet rs, String column) throws SQLException {
        int v = rs.getInt(column);
        return rs.wasNull() ? null : v;
    }

    private static Double decimal(ResultSet rs, String column) throws SQLException {
        double v = rs.getDouble(column);
        return rs.wasNull() ? null : v;
    }
}
