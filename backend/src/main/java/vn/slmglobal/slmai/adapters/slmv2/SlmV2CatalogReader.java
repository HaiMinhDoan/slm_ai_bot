package vn.slmglobal.slmai.adapters.slmv2;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * Đọc phần sản phẩm của slm_v2 (mục 4.2.3) trong một transaction REPEATABLE READ, READ ONLY: view ai_v_* khi có,
 * bảng gốc cho phần view chưa có (lịch sử giá, giá vật tư trong gói, hotline theo tên miền), luôn chọn cột cụ thể để
 * giá nhập / biên lợi nhuận không ra khỏi slm_v2. Đọc bảng gốc cần tài khoản có quyền trên bảng (postgres, 2026-10-29). Mỗi lần đồng bộ
 * mở một kết nối rồi đóng ngay: 10 phút một lần, không cần giữ pool tới DB của hệ thống khác.
 *
 * <p>Không khai báo DataSource thành bean: để Spring Boot vẫn tự cấu hình DataSource chính (JPA, Flyway) của bot.
 */
@Component
@RequiredArgsConstructor
public class SlmV2CatalogReader {

    private final SlmV2Properties properties;

    public boolean isConfigured() {
        return properties.isConfigured();
    }

    public SlmV2Snapshot read() throws SQLException {
        if (!properties.isConfigured()) {
            throw new IllegalStateException("Chưa cấu hình kết nối slm_v2 (SLM_V2_DB_URL, SLM_V2_DB_USERNAME)");
        }
        Properties p = new Properties();
        p.setProperty("user", properties.username());
        if (properties.password() != null) {
            p.setProperty("password", properties.password());
        }
        p.setProperty("readOnly", "true");
        p.setProperty("connectTimeout", String.valueOf(Math.max(1, properties.connectTimeout().toSeconds())));
        p.setProperty("socketTimeout", String.valueOf(Math.max(1, properties.socketTimeout().toSeconds())));
        p.setProperty("ApplicationName", "slm-ai-catalog-sync");
        try (Connection c = DriverManager.getConnection(properties.url(), p)) {
            c.setAutoCommit(false);
            c.setReadOnly(true);
            c.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            try {
                return read(new JdbcTemplate(new SingleConnectionDataSource(c, true)));
            } finally {
                c.rollback();
            }
        }
    }

    private static SlmV2Snapshot read(JdbcTemplate jdbc) {
        return new SlmV2Snapshot(
                jdbc.query("SELECT id, ma, ten, sdt_sale, sdt_tech, trang_thai FROM ai_v_nganh_hang ORDER BY id",
                        (rs, i) -> new SlmV2Snapshot.ProductLine(rs.getInt("id"), rs.getString("ma"),
                                rs.getString("ten"), rs.getString("sdt_sale"), rs.getString("sdt_tech"),
                                integer(rs, "trang_thai"))),
                jdbc.query("""
                        SELECT id, ten, ten_quoc_te, quoc_gia, mo_ta, tep_tin_id, trang_thai
                        FROM ai_v_thuong_hieu ORDER BY id""",
                        (rs, i) -> new SlmV2Snapshot.Brand(rs.getInt("id"), rs.getString("ten"),
                                rs.getString("ten_quoc_te"), rs.getString("quoc_gia"), rs.getString("mo_ta"),
                                integer(rs, "tep_tin_id"), integer(rs, "trang_thai"))),
                jdbc.query("SELECT id, ma, ten, dc_van_phong, sdt, trang_thai FROM ai_v_co_so ORDER BY id",
                        (rs, i) -> new SlmV2Snapshot.Branch(rs.getInt("id"), rs.getString("ma"), rs.getString("ten"),
                                rs.getString("dc_van_phong"), rs.getString("sdt"), integer(rs, "trang_thai"))),
                jdbc.query("""
                        SELECT id, ma, ten, nghanh_hang_id, thuoc_tinh_rieng::text AS thuoc_tinh_rieng, vat_tu_chinh,
                               trang_thai
                        FROM ai_v_nhom_vat_tu ORDER BY id""",
                        (rs, i) -> new SlmV2Snapshot.MaterialGroup(rs.getInt("id"), rs.getString("ma"),
                                rs.getString("ten"), integer(rs, "nghanh_hang_id"), rs.getString("thuoc_tinh_rieng"),
                                bool(rs, "vat_tu_chinh"), integer(rs, "trang_thai"))),
                jdbc.query("""
                        SELECT id, ma, ten, nhom_vat_tu_id, thuong_hieu_id, don_vi, mo_ta_bao_gia,
                               du_lieu_rieng::text AS du_lieu_rieng, thoi_gian_bao_hanh, vat_tu_chinh, trang_thai
                        FROM ai_v_vat_tu ORDER BY id""",
                        (rs, i) -> new SlmV2Snapshot.Material(rs.getInt("id"), rs.getString("ma"), rs.getString("ten"),
                                integer(rs, "nhom_vat_tu_id"), integer(rs, "thuong_hieu_id"), rs.getString("don_vi"),
                                rs.getString("mo_ta_bao_gia"), rs.getString("du_lieu_rieng"),
                                integer(rs, "thoi_gian_bao_hanh"), bool(rs, "vat_tu_chinh"),
                                integer(rs, "trang_thai"))),
                // Bảng gốc (chưa có view lịch sử giá): chỉ lấy maCoSo, giaBan; giaNhap không ra khỏi slm_v2
                jdbc.query("""
                        SELECT id, vat_tu_id, tao_luc, trang_thai,
                               (SELECT jsonb_agg(jsonb_build_object('maCoSo', e->>'maCoSo', 'giaBan', e->'giaBan'))
                                FROM jsonb_array_elements(ds_gia) e)::text AS gia_ban
                        FROM thong_tin_gia ORDER BY id""",
                        (rs, i) -> new SlmV2Snapshot.Price(rs.getInt("id"), rs.getInt("vat_tu_id"),
                                rs.getObject("tao_luc", LocalDateTime.class), rs.getString("gia_ban"),
                                integer(rs, "trang_thai"))),
                jdbc.query("SELECT id, vat_tu_id, tep_tin_id, anh_chinh, trang_thai FROM ai_v_anh_vat_tu ORDER BY id",
                        (rs, i) -> new SlmV2Snapshot.MaterialImage(rs.getInt("id"), rs.getInt("vat_tu_id"),
                                integer(rs, "tep_tin_id"), bool(rs, "anh_chinh"), integer(rs, "trang_thai"))),
                jdbc.query("SELECT id, duong_dan FROM ai_v_tep_tin_anh ORDER BY id",
                        (rs, i) -> new SlmV2Snapshot.ImageFile(rs.getInt("id"), rs.getString("duong_dan"))),
                jdbc.query("""
                        SELECT id, ten, nghanh_hang_id, thuong_hieu_tam_pin, thuong_hieu_inverter,
                               thuong_hieu_pin_luu_tru, trang_thai
                        FROM ai_v_nhom_tron_goi ORDER BY id""",
                        (rs, i) -> new SlmV2Snapshot.PackageFamily(rs.getInt("id"), rs.getString("ten"),
                                integer(rs, "nghanh_hang_id"), integer(rs, "thuong_hieu_tam_pin"),
                                integer(rs, "thuong_hieu_inverter"), integer(rs, "thuong_hieu_pin_luu_tru"),
                                integer(rs, "trang_thai"))),
                jdbc.query("""
                        SELECT id, co_so_id, nhom_tron_goi_id, ten, tep_tin_id, loai_he_thong, loai_pha,
                               cong_suat_he_thong, san_luong_toi_thieu, san_luong_toi_da, mo_ta, tong_gia,
                               gia_khung_sat, ban_chay, trang_thai
                        FROM ai_v_tron_goi ORDER BY id""",
                        (rs, i) -> new SlmV2Snapshot.Package(rs.getInt("id"), integer(rs, "co_so_id"),
                                integer(rs, "nhom_tron_goi_id"), rs.getString("ten"), integer(rs, "tep_tin_id"),
                                rs.getString("loai_he_thong"), rs.getString("loai_pha"),
                                decimal(rs, "cong_suat_he_thong"), decimal(rs, "san_luong_toi_thieu"),
                                decimal(rs, "san_luong_toi_da"), rs.getString("mo_ta"), decimal(rs, "tong_gia"),
                                decimal(rs, "gia_khung_sat"), bool(rs, "ban_chay"), integer(rs, "trang_thai"))),
                // Bảng gốc: view ai_v_vat_tu_tron_goi bỏ cột giá; gia là giá bán, không lấy gm
                jdbc.query("""
                        SELECT id, tron_goi_id, vat_tu_id, so_luong, thoi_gian_bao_hanh, duoc_bao_hanh, duoc_xem,
                               gia, mo_ta, trang_thai
                        FROM vat_tu_tron_goi ORDER BY id""",
                        (rs, i) -> new SlmV2Snapshot.PackageItem(rs.getInt("id"), rs.getInt("tron_goi_id"),
                                rs.getInt("vat_tu_id"), integer(rs, "so_luong"), integer(rs, "thoi_gian_bao_hanh"),
                                bool(rs, "duoc_bao_hanh"), bool(rs, "duoc_xem"), decimal(rs, "gia"),
                                rs.getString("mo_ta"), integer(rs, "trang_thai"))),
                jdbc.query("SELECT id, ten_mien, co_so_id, trang_thai FROM ai_v_mien ORDER BY id",
                        (rs, i) -> new SlmV2Snapshot.Domain(rs.getInt("id"), rs.getString("ten_mien"),
                                integer(rs, "co_so_id"), integer(rs, "trang_thai"))),
                jdbc.query("SELECT id, mien_id, sdt, email, trang_thai FROM thong_tin_ten_mien ORDER BY id",
                        (rs, i) -> new SlmV2Snapshot.DomainContact(rs.getInt("id"), integer(rs, "mien_id"),
                                rs.getString("sdt"), rs.getString("email"), integer(rs, "trang_thai"))));
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
