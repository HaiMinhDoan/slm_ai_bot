package vn.slmglobal.slmai.services.media;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.common.storage.StorageService;
import vn.slmglobal.slmai.services.media.PackageMediaImportService.Item;
import vn.slmglobal.slmai.services.media.PackageMediaImportService.ItemResult;
import vn.slmglobal.slmai.services.media.PackageMediaImportService.Outcome;
import vn.slmglobal.slmai.services.media.PackageMediaImportService.Request;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Nhập ảnh combo trên PostgreSQL thật; Drive và MinIO được giả lập. */
@Testcontainers
@SpringBootTest(properties = {
        "app.jwt.signer-key=test-signer-key-0123456789-abcdefghijklmnop",
        "app.conversation.worker-enabled=false",
        "app.ai.worker-enabled=false"
})
class PackageMediaImportIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final String DRIVE_ID = "1MJDFYHkEuHjNLrqY6e6wSq4Db1bbJqkb";
    private static final String DRIVE_URL = "https://drive.google.com/file/d/" + DRIVE_ID + "/view?usp=drive_link";

    @MockitoBean
    DriveImageFetcher fetcher;
    @MockitoBean
    StorageService storage;

    @Autowired
    PackageMediaImportService importService;
    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void setUp() throws Exception {
        jdbc.execute("TRUNCATE media, media_nguon, media_lien_ket, nhat_ky_thao_tac CASCADE");
        when(storage.isAvailable()).thenReturn(true);
        when(storage.bucket()).thenReturn("slm-ai-media");
        byte[] jpeg = DriveImageFetcherTest.jpeg(20, 10);
        when(fetcher.fetch(DRIVE_ID)).thenReturn(new DriveImageFetcher.Image(DRIVE_ID, jpeg, "image/jpeg", "jpg",
                "a".repeat(64), 20, 10));
    }

    @Test
    void importsImageAndVideoLinkedToBothRegions() throws Exception {
        List<ItemResult> results = run(false, item(98, 97, DRIVE_URL, "https://www.facebook.com/share/r/1FKCqC2LqE/"));

        ItemResult r = results.get(0);
        assertThat(r.image().outcome()).isEqualTo(Outcome.CREATED);
        assertThat(r.video().outcome()).isEqualTo(Outcome.CREATED);
        assertThat(r.warnings()).isEmpty();
        verify(storage).put(eq("media/images/aa/" + "a".repeat(64) + ".jpg"), any(), anyLong(), eq("image/jpeg"));

        Map<String, Object> source = jdbc.queryForMap("SELECT * FROM media_nguon WHERE loai_nguon = 'UPLOAD'");
        assertThat(source.get("id_ngoai")).isEqualTo("gdrive:" + DRIVE_ID);
        assertThat(source.get("url_nguon")).isEqualTo(DRIVE_URL);
        assertThat(source.get("trang_thai_xu_ly")).isEqualTo("READY");
        assertThat(jdbc.queryForList("SELECT ma_doi_tuong FROM media_lien_ket WHERE vai_tro = 'MAIN_IMAGE' "
                + "ORDER BY ma_doi_tuong", String.class)).containsExactly("97", "98");
        assertThat(jdbc.queryForObject("SELECT quyen_su_dung FROM media WHERE loai_media = 'VIDEO'", String.class))
                .isEqualTo("LINK_ONLY");
        assertThat(jdbc.queryForObject("SELECT id_ngoai FROM media_nguon WHERE loai_nguon = 'FACEBOOK'", String.class))
                .isEqualTo("share-r:1FKCqC2LqE");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM nhat_ky_thao_tac WHERE hanh_dong = 'media.import'",
                Integer.class)).isEqualTo(2);
    }

    @Test
    void rerunReportsExistsWithoutDownloadingAgain() throws Exception {
        run(false, item(98, 97, DRIVE_URL, null));
        List<ItemResult> again = run(false, item(98, 97, DRIVE_URL, null));

        assertThat(again.get(0).image().outcome()).isEqualTo(Outcome.EXISTS);
        verify(fetcher, times(1)).fetch(DRIVE_ID);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM media", Integer.class)).isEqualTo(1);
    }

    @Test
    void dryRunWritesNothing() throws Exception {
        List<ItemResult> results = run(true, item(98, null, DRIVE_URL, "https://www.facebook.com/share/v/1DsTmxsPeh/"));

        assertThat(results.get(0).image().outcome()).isEqualTo(Outcome.WOULD_CREATE);
        assertThat(results.get(0).video().outcome()).isEqualTo(Outcome.WOULD_CREATE);
        verify(storage, never()).put(anyString(), any(), anyLong(), anyString());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM media", Integer.class)).isZero();
    }

    @Test
    void keepsExistingMainImageAndWarns() throws Exception {
        run(false, item(98, null, DRIVE_URL, null));
        String otherId = "1iwRAAy8SucdLYni6jiccq8ec6wJ2lX3d";
        when(fetcher.fetch(otherId)).thenReturn(new DriveImageFetcher.Image(otherId,
                DriveImageFetcherTest.jpeg(5, 5), "image/jpeg", "jpg", "b".repeat(64), 5, 5));

        List<ItemResult> results = run(false,
                item(98, null, "https://drive.google.com/file/d/" + otherId + "/view", null));

        assertThat(results.get(0).image().outcome()).isEqualTo(Outcome.CREATED);
        assertThat(results.get(0).warnings()).anyMatch(w -> w.contains("đã có ảnh chính khác"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM media_lien_ket WHERE ma_doi_tuong = '98' "
                + "AND vai_tro = 'MAIN_IMAGE'", Integer.class)).isEqualTo(1);
    }

    @Test
    void invalidLinksFailPerItemWithoutStoppingOthers() throws Exception {
        List<ItemResult> results = importService.importPackages(new Request(List.of(
                new Item(1, null, "Combo lỗi", "https://example.com/file/d/" + DRIVE_ID + "/view",
                        "https://www.facebook.com/watch?v=1", "https://slmsolar.vn/products/2"),
                item(98, null, DRIVE_URL, null)), false, null), null, "admin@test.local");

        assertThat(results.get(0).image().outcome()).isEqualTo(Outcome.FAILED);
        assertThat(results.get(0).video().outcome()).isEqualTo(Outcome.FAILED);
        assertThat(results.get(0).warnings()).anyMatch(w -> w.contains("khác gói 1"));
        assertThat(results.get(1).image().outcome()).isEqualTo(Outcome.CREATED);
    }

    @Test
    void refusesRealImportWhenStorageUnavailable() {
        when(storage.isAvailable()).thenReturn(false);

        assertThatThrownBy(() -> run(false, item(98, null, DRIVE_URL, null)))
                .isInstanceOf(CommonException.class)
                .hasMessageContaining("MinIO");
    }

    private List<ItemResult> run(boolean dryRun, Item item) {
        return importService.importPackages(new Request(List.of(item), dryRun, null), null, "admin@test.local");
    }

    private static Item item(int hn, Integer hcm, String drive, String facebook) {
        return new Item(hn, hcm, "Hệ Hy-Brid 5 kWp 1 pha - Lưu trữ 5.12 kWh", drive, facebook,
                "https://slmsolar.vn/products/" + hn);
    }
}
