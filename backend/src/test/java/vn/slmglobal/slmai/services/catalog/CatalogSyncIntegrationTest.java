package vn.slmglobal.slmai.services.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import vn.slmglobal.slmai.common.security.RoleCode;
import vn.slmglobal.slmai.common.web.PageResponse;
import vn.slmglobal.slmai.services.auth.UserAdminService;
import vn.slmglobal.slmai.services.auth.dto.CreateUserRequest;
import vn.slmglobal.slmai.services.sizing.PackageAdvisor;
import vn.slmglobal.slmai.services.sizing.PvoutReminderJob;
import vn.slmglobal.slmai.services.sizing.PvoutService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Đồng bộ catalog trên PostgreSQL thật. "slm_v2" giả là schema slmv2 trong cùng container, có các bảng mang tên
 * view ai_v_* (cùng cột); bot đọc bằng kết nối riêng như với slm_v2 thật. Kèm bảng PV out và nhắc nạp năm mới.
 */
@Testcontainers
@SpringBootTest(properties = {
        "app.jwt.signer-key=test-signer-key-0123456789-abcdefghijklmnop",
        "app.conversation.worker-enabled=false",
        "app.ai.worker-enabled=false",
        "app.catalog.sync-enabled=false"
})
class CatalogSyncIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void slmV2(DynamicPropertyRegistry registry) {
        registry.add("app.slm-v2.url", () -> postgres.getJdbcUrl()
                + (postgres.getJdbcUrl().contains("?") ? "&" : "?") + "currentSchema=slmv2");
        registry.add("app.slm-v2.username", postgres::getUsername);
        registry.add("app.slm-v2.password", postgres::getPassword);
    }

    @Autowired
    CatalogSyncService syncService;
    @Autowired
    CatalogRepository repository;
    @Autowired
    PackageAdvisor advisor;
    @Autowired
    PvoutService pvoutService;
    @Autowired
    PvoutReminderJob reminderJob;
    @Autowired
    UserAdminService users;
    @Autowired
    JdbcTemplate jdbc;

    private UUID adminId;

    @BeforeEach
    void setUp() {
        jdbc.execute("TRUNCATE dm_vat_tu_tron_goi, dm_tron_goi, dm_vat_tu, dm_thuong_hieu, dm_co_so, dm_ten_mien, "
                + "dm_lich_su_dong_bo, thong_bao, thong_bao_da_doc RESTART IDENTITY CASCADE");
        jdbc.update("DELETE FROM nl_bang_pvout WHERE nam <> 2026");
        jdbc.update("DELETE FROM nguoi_dung");
        createSource();
        adminId = users.create(null, new CreateUserRequest("admin-" + UUID.randomUUID() + "@test.local",
                "Quản trị", "Test@123456789", List.of(RoleCode.SYSTEM_ADMIN))).id();
    }

    @Test
    void syncsSnapshotAndSkipsUnchangedRows() {
        CatalogRepository.SyncRun run = syncService.sync(CatalogSyncService.TRIGGER_MANUAL, "admin@test.local");

        assertThat(run.status()).isEqualTo("SUCCESS");
        assertThat(run.inserted()).isGreaterThan(0);
        assertThat(run.triggeredBy()).isEqualTo("admin@test.local");
        Map<String, Object> hn = jdbc.queryForMap("SELECT * FROM dm_tron_goi WHERE id_nguon = 98");
        assertThat(hn.get("ma_co_so")).isEqualTo("HN");
        assertThat(hn.get("loai_he_thong")).isEqualTo("HYBRID");
        assertThat(((Number) hn.get("cong_suat_kwp")).doubleValue()).isEqualTo(5.0);
        assertThat(((Number) hn.get("luu_tru_kwh")).doubleValue()).isEqualTo(5.12);
        assertThat(hn.get("so_tam_pin")).isEqualTo(8);
        assertThat(hn.get("duoc_goi_y")).isEqualTo(true);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM dm_vat_tu_tron_goi", Integer.class)).isEqualTo(8);
        assertThat(repository.domains("HCM")).containsExactly("slmsolar.com.vn");
        // Giá nhập không bao giờ có ở bot (view slm_v2 đã bỏ, bảng dm_* cũng không có cột)
        assertThat(jdbc.queryForObject("SELECT count(*) FROM information_schema.columns WHERE table_schema = 'public' "
                + "AND table_name LIKE 'dm_%' AND column_name ILIKE '%nhap%'", Integer.class)).isZero();

        CatalogRepository.SyncRun again = syncService.sync(CatalogSyncService.TRIGGER_SCHEDULED, null);
        assertThat(again.inserted()).isZero();
        assertThat(again.updated()).isZero();
        assertThat(again.deactivated()).isZero();
        assertThat(again.version()).isEqualTo(run.version());
        assertThat(syncService.currentVersion()).isEqualTo(run.version());
    }

    @Test
    void updatesChangedRowsAndDeactivatesMissingOnes() {
        syncService.sync(CatalogSyncService.TRIGGER_MANUAL, null);
        jdbc.update("UPDATE slmv2.ai_v_tron_goi SET tong_gia = 91000000 WHERE id = 98");
        jdbc.update("DELETE FROM slmv2.ai_v_vat_tu_tron_goi WHERE tron_goi_id = 97");
        jdbc.update("DELETE FROM slmv2.ai_v_tron_goi WHERE id = 97");

        CatalogRepository.SyncRun run = syncService.sync(CatalogSyncService.TRIGGER_MANUAL, null);

        assertThat(run.status()).isEqualTo("SUCCESS");
        assertThat(run.updated()).isEqualTo(1);
        assertThat(run.deactivated()).isEqualTo(5);
        assertThat(jdbc.queryForObject("SELECT tong_gia FROM dm_tron_goi WHERE id_nguon = 98", Long.class))
                .isEqualTo(91_000_000L);
        assertThat(jdbc.queryForMap("SELECT dang_dung, duoc_goi_y FROM dm_tron_goi WHERE id_nguon = 97"))
                .containsEntry("dang_dung", false).containsEntry("duoc_goi_y", false);

        // Xuất hiện lại ở nguồn thì được bật lại
        insertPackage(97, 2, 90_800_000);
        assertThat(syncService.sync(CatalogSyncService.TRIGGER_MANUAL, null).status()).isEqualTo("SUCCESS");
        assertThat(jdbc.queryForObject("SELECT dang_dung FROM dm_tron_goi WHERE id_nguon = 97", Boolean.class))
                .isTrue();
    }

    @Test
    void failureKeepsOldCatalogAndNotifiesAdminsOncePerDay() {
        syncService.sync(CatalogSyncService.TRIGGER_MANUAL, null);
        String version = syncService.currentVersion();
        jdbc.execute("DROP TABLE slmv2.ai_v_mien");

        CatalogRepository.SyncRun failed = syncService.sync(CatalogSyncService.TRIGGER_SCHEDULED, null);
        syncService.sync(CatalogSyncService.TRIGGER_SCHEDULED, null);

        assertThat(failed.status()).isEqualTo("FAILED");
        assertThat(failed.error()).contains("ai_v_mien");
        assertThat(syncService.currentVersion()).isEqualTo(version);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM dm_tron_goi WHERE dang_dung", Integer.class))
                .isEqualTo(2);
        assertThat(jdbc.queryForList("SELECT nguoi_nhan_id FROM thong_bao WHERE loai = 'CATALOG_SYNC_FAILED'",
                UUID.class)).containsExactly(adminId);
    }

    @Test
    void refusesToWipeCatalogWhenSourceLooksBroken() {
        syncService.sync(CatalogSyncService.TRIGGER_MANUAL, null);
        jdbc.update("UPDATE slmv2.ai_v_tron_goi SET trang_thai = 0");

        CatalogRepository.SyncRun run = syncService.sync(CatalogSyncService.TRIGGER_MANUAL, null);

        assertThat(run.status()).isEqualTo("FAILED");
        assertThat(run.error()).contains("không trả gói nào");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM dm_tron_goi WHERE dang_dung", Integer.class))
                .isEqualTo(2);
    }

    @Test
    void packageListFiltersAndPagesLikeOtherAdminLists() {
        syncService.sync(CatalogSyncService.TRIGGER_MANUAL, null);

        PageResponse<CatalogRepository.CatalogPackage> first = packages(null, null, null, 0, 1);
        assertThat(first.total()).isEqualTo(2);
        assertThat(first.totalPages()).isEqualTo(2);
        assertThat(first.items()).hasSize(1);
        PageResponse<CatalogRepository.CatalogPackage> second = packages(null, null, null, 1, 1);
        assertThat(second.items()).extracting(CatalogRepository.CatalogPackage::id)
                .doesNotContainAnyElementsOf(first.items().stream().map(CatalogRepository.CatalogPackage::id).toList());
        assertThat(packages(null, null, null, 5, 10).items()).isEmpty();

        assertThat(packages(null, "HCM", null, 0, 10).items()).extracting(CatalogRepository.CatalogPackage::id)
                .containsExactly(97);
        assertThat(packages("98", null, null, 0, 10).items()).extracting(CatalogRepository.CatalogPackage::id)
                .containsExactly(98);
        assertThat(packages(null, null, "RECOMMENDABLE", 0, 10).total()).isEqualTo(2);
        assertThat(packages(null, null, "INACTIVE", 0, 10).total()).isZero();
    }

    private PageResponse<CatalogRepository.CatalogPackage> packages(String keyword, String branchCode, String status,
                                                                    int page, int size) {
        return PageResponse.of(repository.findPackages(
                new CatalogRepository.PackageFilter(keyword, branchCode, null, null, status),
                PageRequest.of(page, size)), p -> p);
    }

    @Test
    void advisorQuotesSyncedPackagesWithRegionalLinks() {
        syncService.sync(CatalogSyncService.TRIGGER_MANUAL, null);

        JsonNode north = advisor.advise(new PackageAdvisor.AdviceRequest("Nam Định", 1, null, null, null, null,
                "HYBRID", null)).result();
        assertThat(north.path("gia_khoi_diem").get(0).path("goi_re_nhat").path("link").asText())
                .isEqualTo("https://slmsolar.vn/products/98");
        JsonNode south = advisor.advise(new PackageAdvisor.AdviceRequest("Đồng Nai", 1, 1_000_000L, "CAN_BANG",
                "NGOI", null, null, null)).result();
        assertThat(south.path("goi_de_xuat").get(0).path("link").asText())
                .isEqualTo("https://slmsolar.com.vn/products/97");
        assertThat(south.path("goi_de_xuat").get(0).path("san_luong_uoc_tinh").path("cach_tinh").asText())
                .contains("EVN 2026", "Đồng Nai");

        JsonNode detail = advisor.packageDetail(98, "Sài Gòn");
        assertThat(detail.path("goi_id").asInt()).isEqualTo(97);
        assertThat(detail.path("thanh_phan").toString()).contains("Tấm pin mặt trời JA 625 Wp", "bảo hành 12 năm");
        assertThat(detail.path("thanh_phan").toString()).doesNotContain("Bộ ghi dữ liệu");
    }

    @Test
    void pvoutImportChecksTableAndComparesWithPreviousYear() {
        PvoutService.ImportRequest bad = new PvoutService.ImportRequest(2027, "EVN thử", null,
                List.of(new PvoutService.ImportRow("Hà Nội", List.of(1.0, 2.0)),
                        new PvoutService.ImportRow("Bình Dương", twelve(120))), false);
        PvoutService.ImportResult rejected = pvoutService.importTable(bad, adminId, "admin@test.local");
        assertThat(rejected.saved()).isFalse();
        assertThat(String.join("\n", rejected.errors())).contains("cần đủ 12 tháng", "không nhận ra tỉnh",
                "Thiếu Hưng Yên");

        List<PvoutService.ImportRow> rows = new ArrayList<>();
        pvoutService.load(2026).orElseThrow().monthly().forEach((code, months) -> {
            List<Double> m = new ArrayList<>();
            for (double v : months) {
                m.add(v * 1.1);
            }
            rows.add(new PvoutService.ImportRow(code, m));
        });
        PvoutService.ImportResult preview = pvoutService.importTable(
                new PvoutService.ImportRequest(2027, "EVN thử", null, rows, true), adminId, "admin@test.local");
        assertThat(preview.errors()).isEmpty();
        assertThat(preview.saved()).isFalse();
        assertThat(preview.comparedYear()).isEqualTo(2026);
        assertThat(preview.rows()).hasSize(34).allSatisfy(r -> assertThat(r.changePercent()).isEqualTo(10.0));

        assertThat(pvoutService.importTable(new PvoutService.ImportRequest(2027, "EVN thử", null, rows, false),
                adminId, "admin@test.local").saved()).isTrue();
        assertThat(pvoutService.list()).extracting(i -> i.year()).containsExactly(2027, 2026);
    }

    @Test
    void remindsAdminsToLoadNextYearsTable() {
        reminderJob.check(LocalDate.of(2026, 12, 3));
        reminderJob.check(LocalDate.of(2026, 12, 4));
        reminderJob.check(LocalDate.of(2027, 2, 2));

        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT khoa_chong_trung, muc_do FROM thong_bao WHERE loai = 'DATA_UPDATE_DUE' ORDER BY id");
        assertThat(rows).extracting(r -> r.get("khoa_chong_trung"))
                .containsExactly("PVOUT_DUE:2027:" + adminId, "PVOUT_OVERDUE:2027:" + adminId);
        assertThat(rows.get(1).get("muc_do")).isEqualTo("WARNING");
    }

    // ------------------------------------------------------------------ "slm_v2" giả

    private void createSource() {
        jdbc.execute("DROP SCHEMA IF EXISTS slmv2 CASCADE");
        jdbc.execute("CREATE SCHEMA slmv2");
        jdbc.execute("""
                CREATE TABLE slmv2.ai_v_thuong_hieu (id int, ten varchar, ten_quoc_te varchar, quoc_gia varchar,
                    mo_ta text, tep_tin_id int, trang_thai int);
                CREATE TABLE slmv2.ai_v_co_so (id int, ma varchar, ten varchar, dc_van_phong varchar, sdt varchar,
                    trang_thai int);
                CREATE TABLE slmv2.ai_v_nhom_vat_tu (id int, ma varchar, ten varchar, nghanh_hang_id int,
                    thuoc_tinh_rieng jsonb, vat_tu_chinh boolean, trang_thai int);
                CREATE TABLE slmv2.ai_v_vat_tu (id int, ma varchar, ten varchar, nhom_vat_tu_id int,
                    thuong_hieu_id int, don_vi varchar, mo_ta_bao_gia text, du_lieu_rieng jsonb,
                    thoi_gian_bao_hanh int, vat_tu_chinh boolean, trang_thai int);
                CREATE TABLE slmv2.ai_v_nhom_tron_goi (id int, ten varchar, nghanh_hang_id int, thuong_hieu_tam_pin int,
                    thuong_hieu_inverter int, thuong_hieu_pin_luu_tru int, trang_thai int);
                CREATE TABLE slmv2.ai_v_tron_goi (id int, co_so_id int, nhom_tron_goi_id int, ten varchar,
                    tep_tin_id int, loai_he_thong varchar, loai_pha varchar, cong_suat_he_thong double precision,
                    san_luong_toi_thieu double precision, san_luong_toi_da double precision, mo_ta text,
                    tong_gia double precision, gia_khung_sat double precision, ban_chay boolean, trang_thai int);
                CREATE TABLE slmv2.ai_v_vat_tu_tron_goi (id int, tron_goi_id int, vat_tu_id int, so_luong int,
                    thoi_gian_bao_hanh int, duoc_bao_hanh boolean, duoc_xem boolean, trang_thai int);
                CREATE TABLE slmv2.ai_v_mien (id int, ten_mien varchar, co_so_id int, trang_thai int);
                INSERT INTO slmv2.ai_v_thuong_hieu VALUES (1, 'JA Solar', NULL, 'Trung Quốc', NULL, NULL, 1),
                    (2, 'Solis', NULL, 'Trung Quốc', NULL, NULL, 1), (5, 'Dyness', NULL, 'Trung Quốc', NULL, NULL, 1);
                INSERT INTO slmv2.ai_v_co_so VALUES (1, 'HN', 'Hà Nội', NULL, NULL, 1), (2, 'HCM', 'Hồ Chí Minh', NULL, NULL, 1);
                INSERT INTO slmv2.ai_v_nhom_vat_tu VALUES (1, 'TAM_PIN', 'Tấm pin', 1, '{}', true, 1),
                    (2, 'BIEN_TAN', 'Biến tần', 1, '{}', true, 1), (3, 'PIN_LUU_TRU', 'Pin lưu trữ', 1, '{}', true, 1);
                INSERT INTO slmv2.ai_v_vat_tu VALUES
                    (233, 'ja625', 'Tấm pin mặt trời JA 625 Wp', 1, 1, 'Tấm', NULL,
                     '{"cong_suat": {"ten": "Công suất", "donVi": "Wp", "giaTri": 625}}', 144, true, 1),
                    (62, 'solis5', 'Inverter Solis Hy-Brid 1 pha 5 kW', 2, 2, 'Chiếc', NULL,
                     '{"cong_suat": {"ten": "Công suất", "donVi": "kW", "giaTri": 5}}', 60, true, 1),
                    (57, 'dyness512', 'Pin lithium Dyness 5.12 kWh', 3, 5, 'Bộ', NULL,
                     '{"dung_luong": {"ten": "Dung lượng", "donVi": "kWh", "giaTri": "5,12 "}}', 60, true, 1),
                    (58, 'logger', 'Bộ ghi dữ liệu biến tần', 2, 2, 'Bộ', NULL,
                     '{"cong_suat": {"ten": "Công suất", "donVi": "kW", "giaTri": 0}}', 24, false, 1);
                INSERT INTO slmv2.ai_v_nhom_tron_goi VALUES (6, 'JA Solar - Solis - Dyness', 1, 1, 2, 5, 1);
                INSERT INTO slmv2.ai_v_mien VALUES (5, 'slmsolar.com', 1, 1), (4, 'slmsolar.vn', 1, 1),
                    (2, 'slmsolar.com.vn', 2, 1);
                """);
        insertPackage(98, 1, 90_800_000);
        insertPackage(97, 2, 90_800_000);
    }

    private void insertPackage(int id, int branchId, long price) {
        jdbc.update("INSERT INTO slmv2.ai_v_tron_goi VALUES (?, ?, 6, 'Hệ Hy-Brid 5 kWp 1 pha - Lưu trữ 5.12 kWh', "
                + "347, 'Hy-Brid', '1 pha', 5000, 500, 700, NULL, ?, 0, true, 1)", id, branchId, price);
        int base = id * 10;
        jdbc.update("INSERT INTO slmv2.ai_v_vat_tu_tron_goi VALUES (?, ?, 233, 8, 144, true, true, 1), "
                        + "(?, ?, 62, 1, 60, true, true, 1), (?, ?, 57, 1, 60, true, true, 1), (?, ?, 58, 1, 24, true, false, 1)",
                base, id, base + 1, id, base + 2, id, base + 3, id);
    }

    private static List<Double> twelve(double v) {
        List<Double> out = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            out.add(v);
        }
        return out;
    }
}
