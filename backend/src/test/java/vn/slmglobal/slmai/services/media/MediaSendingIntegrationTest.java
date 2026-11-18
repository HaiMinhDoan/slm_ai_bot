package vn.slmglobal.slmai.services.media;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import vn.slmglobal.slmai.common.storage.StorageService;
import vn.slmglobal.slmai.services.catalog.PackageQuoteImageService;
import vn.slmglobal.slmai.services.conversation.ChannelSender;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.conversation.entity.Message;
import vn.slmglobal.slmai.services.conversation.entity.MessageDirection;
import vn.slmglobal.slmai.services.conversation.entity.MessageStatus;
import vn.slmglobal.slmai.services.conversation.entity.SenderType;
import vn.slmglobal.slmai.services.customer.entity.ChannelIdentity;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Gửi media cho khách (2026-11-18) trên PostgreSQL thật: V15 duyệt media gói đã nhập, chọn ảnh báo giá / video theo
 * link gói và mã AI chọn, gửi qua kênh (ảnh là tệp, video Facebook là link riêng), danh mục media trong prompt.
 * MinIO và ảnh báo giá tự tạo được giả lập.
 */
@Testcontainers
@SpringBootTest(properties = {
        "app.jwt.signer-key=test-signer-key-0123456789-abcdefghijklmnop",
        "app.conversation.worker-enabled=false",
        "app.ai.worker-enabled=false"
})
class MediaSendingIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final byte[] IMAGE = "jpeg-bytes".getBytes(StandardCharsets.UTF_8);

    @MockitoBean
    StorageService storage;
    @MockitoBean
    PackageQuoteImageService quoteImages;

    @Autowired
    MediaPlanner planner;
    @Autowired
    MediaDelivery delivery;
    @Autowired
    MediaCatalog catalog;
    @Autowired
    JdbcTemplate jdbc;
    @Autowired
    TransactionTemplate tx;

    private UUID image19;
    private UUID videoHybrid;
    private UUID videoOnGrid;
    private UUID unlinked;

    @BeforeEach
    void setUp() throws Exception {
        jdbc.execute("TRUNCATE media, media_nguon, media_lien_ket, nhat_ky_thao_tac CASCADE");
        jdbc.execute("DELETE FROM dm_tron_goi WHERE id_nguon IN (132, 156, 157, 158, 184)");
        jdbc.update("""
                INSERT INTO dm_tron_goi (id_nguon, ma_co_so, ten, loai_he_thong, so_pha, cong_suat_kwp, luu_tru_kwh,
                                         tong_gia, dang_dung, duoc_goi_y, ma_bam, dong_bo_luc)
                VALUES (156, 'HN', 'Hệ On-Grid 19 kWp 3 pha', 'ON_GRID', 3, 18.75, 0, 154300000, true, true, 'a', now()),
                       (157, 'HCM', 'Hệ On-Grid 19 kWp 3 pha', 'ON_GRID', 3, 18.75, 0, 154300000, true, true, 'b', now()),
                       (158, 'HN', 'Hệ On-Grid 5 kWp 1 pha', 'ON_GRID', 1, 5, 0, 53600000, true, true, 'c', now()),
                       (184, 'HN', 'Hệ On-Grid 29.4 kWp 3 pha', 'ON_GRID', 3, 29.375, 0, 252700000, true, true, 'd',
                        now()),
                       (132, 'HN', 'Hệ Hy-Brid 16 kWp 3 pha áp cao - Lưu trữ 15.36 kWh', 'HYBRID', 3, 15.625, 15.36,
                        256100000, true, true, 'e', now())""");
        when(storage.isAvailable()).thenReturn(true);
        when(storage.bucket()).thenReturn("slm-ai-media");
        when(storage.get(anyString())).thenAnswer(i -> new ByteArrayInputStream(IMAGE));
        when(quoteImages.canRender(anyInt())).thenReturn(true);
        when(quoteImages.image(anyInt())).thenAnswer(i -> Optional.of(new PackageQuoteImageService.QuoteImage(
                i.getArgument(0), "quotes/x.jpg", "sha-quote-" + i.getArgument(0), IMAGE)));

        // Như dữ liệu đã nhập ngày 2026-10-29: Nháp, chưa có tóm tắt
        image19 = media("IMAGE", "Hệ On-Grid 19 kWp 3 pha", "OWNED");
        upload(image19, "media/images/e8/e87d.jpg");
        link(image19, "156", "MAIN_IMAGE");
        link(image19, "157", "MAIN_IMAGE");
        videoHybrid = media("VIDEO", "Video: Hệ Hy-Brid 16 kWp 3 pha áp cao - Lưu trữ 15.36 kWh", "LINK_ONLY");
        facebook(videoHybrid, "https://www.facebook.com/share/v/19niuBUNZc/", "19niuBUNZc");
        link(videoHybrid, "132", "RELATED");
        videoOnGrid = media("VIDEO", "Video: Hệ On-Grid 5 kWp 1 pha", "LINK_ONLY");
        facebook(videoOnGrid, "https://www.facebook.com/share/r/1CFxH5VjQy/", "1CFxH5VjQy");
        link(videoOnGrid, "158", "RELATED");
        unlinked = media("IMAGE", "Ảnh chưa gắn gói", "OWNED");
        upload(unlinked, "media/images/aa/aa.jpg");
        runV15();
    }

    @Test
    void v15ApprovesImportedPackageMediaWithCodesAndAudit() {
        List<Map<String, Object>> rows = jdbc.queryForList("""
                SELECT id, trang_thai, ma_danh_muc, tom_tat, dung_khi, tranh_khi, array_to_string(chu_de, ',') AS chu_de
                FROM media ORDER BY ma_danh_muc NULLS LAST""");
        assertThat(rows).extracting(r -> r.get("ma_danh_muc")).containsExactly("M01", "M02", "M03", null);
        Map<String, Object> img = rows.get(0);
        assertThat(img.get("id")).isEqualTo(image19);
        assertThat(img.get("trang_thai")).isEqualTo("APPROVED");
        assertThat((String) img.get("tom_tat")).startsWith("Ảnh báo giá Hệ On-Grid 19 kWp 3 pha");
        assertThat(img.get("chu_de")).isEqualTo("bang-gia,on-grid");
        Map<String, Object> video = rows.get(1);
        assertThat(video.get("id")).isEqualTo(videoHybrid);
        assertThat((String) video.get("tom_tat")).isEqualTo(
                "Video công trình thực tế lắp Hệ Hy-Brid 16 kWp 3 pha áp cao - Lưu trữ 15.36 kWh.");
        assertThat(video.get("tranh_khi")).isNotNull();
        assertThat(video.get("chu_de")).isEqualTo("cong-trinh,hybrid");
        // Ảnh không gắn gói vẫn là Nháp
        assertThat(rows.get(3).get("trang_thai")).isEqualTo("DRAFT");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM nhat_ky_thao_tac WHERE hanh_dong = 'media.update'",
                Integer.class)).isEqualTo(3);

        // Chạy lại (CSDL đã duyệt) không đổi gì
        runV15();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM nhat_ky_thao_tac", Integer.class)).isEqualTo(3);
    }

    @Test
    void packageLinkSendsItsQuoteImageThenASimilarVideo() {
        String reply = "Dạ nhà mình hợp hệ On-Grid 19 kWp, giá trọn gói 154.300.000 đ ạ.\n\n"
                + "https://slmsolar.vn/products/156";
        ArrayNode plan = planner.plan(ChannelCode.MESSENGER, reply, List.of(), List.of()).attachments();

        assertThat(plan).hasSize(2);
        assertThat(plan.get(0).path("kind").asText()).isEqualTo("MEDIA");
        assertThat(plan.get(0).path("media_id").asText()).isEqualTo(image19.toString());
        assertThat(plan.get(0).path("by").asText()).isEqualTo("AUTO");
        // Không có video của chính gói: video gói cùng loại hệ (On-Grid) dù khác số pha
        assertThat(plan.get(1).path("media_id").asText()).isEqualTo(videoOnGrid.toString());

        // Gói chưa có ảnh trong thư viện: ảnh báo giá tự tạo
        ArrayNode noImage = planner.plan(ChannelCode.MESSENGER, "Xem tại https://slmsolar.vn/products/184 ạ",
                List.of(), List.of()).attachments();
        assertThat(noImage.get(0).path("kind").asText()).isEqualTo("QUOTE");
        assertThat(noImage.get(0).path("package_id").asInt()).isEqualTo(184);
        assertThat(noImage.get(0).path("title").asText()).isEqualTo("Ảnh báo giá Hệ On-Grid 29.4 kWp 3 pha");

        assertThat(planner.extrasForPackage(ChannelCode.MESSENGER, 132))
                .containsExactly("ảnh báo giá của gói", "video công trình thực tế của gói này");
        assertThat(planner.plan(ChannelCode.MESSENGER, "Dạ vâng anh ạ.", List.of(), List.of()).attachments())
                .isEmpty();
    }

    @Test
    void aiChosenCodesAreCheckedAndAlreadySentMediaIsNotRepeated() {
        MediaPlanner.Plan plan = planner.plan(ChannelCode.MESSENGER, "Dạ em gửi anh video công trình ạ.",
                List.of("m02", "M99", "M01"), List.of());
        assertThat(plan.attachments()).hasSize(1);
        assertThat(plan.attachments().get(0).path("code").asText()).isEqualTo("M02");
        assertThat(plan.attachments().get(0).path("by").asText()).isEqualTo("AI");
        // Ảnh báo giá của gói không nằm trong danh mục cho AI
        assertThat(plan.dropped()).containsExactly("M99: không có trong danh mục media",
                "M01: không có trong danh mục media");

        Message earlier = Message.builder().direction(MessageDirection.OUT).senderType(SenderType.AI)
                .status(MessageStatus.SENT).content("…").attachments(sent(image19, videoOnGrid)).build();
        ArrayNode again = planner.plan(ChannelCode.MESSENGER, "Gói đây ạ: https://slmsolar.vn/products/156",
                List.of(), List.of(earlier)).attachments();
        // Ảnh và video đã gửi: không gửi lại; còn video Hybrid cũng không hợp loại hệ nên không có gì
        assertThat(again).isEmpty();
    }

    @Test
    void deliverySendsImagesAsFilesAndVideoLinksAsTheirOwnMessage() {
        String reply = "Gói đây ạ https://slmsolar.vn/products/184 và https://slmsolar.vn/products/156";
        ArrayNode plan = planner.plan(ChannelCode.MESSENGER, reply, List.of(), List.of()).attachments();
        assertThat(plan).extracting(n -> n.path("kind").asText()).containsExactly("QUOTE", "MEDIA", "MEDIA");

        RecordingSender sender = new RecordingSender();
        int[] pauses = {0};
        MediaDelivery.Result r = delivery.send(sender, new ChannelIdentity(), plan, () -> pauses[0]++);

        assertThat(r.sent()).isEqualTo(3);
        assertThat(pauses[0]).isEqualTo(3);
        assertThat(sender.calls).containsExactly(
                "file image bao-gia-184.jpg key=sha-quote-184",
                "file image e87d.jpg key=media/images/e8/e87d.jpg",
                "text https://www.facebook.com/share/r/1CFxH5VjQy/");
        assertThat(r.attachments()).allSatisfy(a -> {
            assertThat(a.path("status").asText()).isEqualTo("SENT");
            assertThat(a.path("external_id").asText()).startsWith("mid.");
        });

        // Gửi lại (nhân viên bấm "Gửi lại"): mục đã SENT bỏ qua; media bỏ duyệt thì SKIPPED
        jdbc.update("UPDATE media SET trang_thai = 'DRAFT' WHERE id = ?", videoOnGrid);
        ArrayNode retry = ((ArrayNode) r.attachments()).deepCopy();
        ((com.fasterxml.jackson.databind.node.ObjectNode) retry.get(2)).put("status", "PENDING");
        MediaDelivery.Result again = delivery.send(sender, new ChannelIdentity(), retry, null);
        assertThat(again.sent()).isZero();
        assertThat(again.attachments().get(2).path("status").asText()).isEqualTo("SKIPPED");
        assertThat(sender.calls).hasSize(3);

        // Kênh không gửi được tệp: ảnh bị bỏ qua, link video vẫn gửi
        RecordingSender textOnly = new RecordingSender();
        textOnly.files = false;
        jdbc.update("UPDATE media SET trang_thai = 'APPROVED' WHERE id = ?", videoOnGrid);
        MediaDelivery.Result plain = delivery.send(textOnly, new ChannelIdentity(), plan, null);
        assertThat(plain.sent()).isEqualTo(1);
        assertThat(plain.attachments().get(0).path("status").asText()).isEqualTo("SKIPPED");
    }

    @Test
    void promptCatalogListsApprovedMediaExceptPackageQuoteImages() {
        MediaCatalog.Block block = catalog.block(ChannelCode.MESSENGER);
        assertThat(block.text()).contains("# Gửi media", "## Danh mục media")
                .contains("[M02] VIDEO · Video: Hệ Hy-Brid 16 kWp 3 pha áp cao - Lưu trữ 15.36 kWh")
                .contains("gắn với: gói Hệ Hy-Brid 16 kWp 3 pha áp cao - Lưu trữ 15.36 kWh (goi_id 132)")
                .contains("[M03] VIDEO")
                .doesNotContain("[M01]");
        assertThat(block.version()).hasSize(64);

        // Kênh không nằm trong kenh_cho_phep: không có media nào
        assertThat(catalog.entries(ChannelCode.ZALO_OA)).isEmpty();
        String before = block.version();
        jdbc.update("UPDATE media SET dung_khi = 'Khách hỏi lắp xong trông thế nào' WHERE id = ?", videoHybrid);
        assertThat(catalog.block(ChannelCode.MESSENGER).version()).isNotEqualTo(before);
    }

    // ------------------------------------------------------------------ hỗ trợ

    private void runV15() {
        String sql;
        try {
            sql = new String(new ClassPathResource("db/migration/V15__duyet_media_goi.sql").getInputStream()
                    .readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        tx.executeWithoutResult(s -> jdbc.execute(sql));
    }

    private UUID media(String type, String title, String rights) {
        return jdbc.queryForObject("""
                INSERT INTO media (loai_media, tieu_de, quyen_su_dung) VALUES (?, ?, ?) RETURNING id""",
                UUID.class, type, title, rights);
    }

    private void upload(UUID media, String key) {
        jdbc.update("""
                INSERT INTO media_nguon (media_id, loai_nguon, bucket_luu_tru, duong_dan_luu_tru, loai_mime,
                                         trang_thai_xu_ly, trang_thai_truy_cap)
                VALUES (?, 'UPLOAD', 'slm-ai-media', ?, 'image/jpeg', 'READY', 'ACTIVE')""", media, key);
    }

    private void facebook(UUID media, String url, String id) {
        jdbc.update("""
                INSERT INTO media_nguon (media_id, loai_nguon, url_nguon, id_ngoai, trang_thai_xu_ly)
                VALUES (?, 'FACEBOOK', ?, ?, 'READY')""", media, url, id);
    }

    private void link(UUID media, String packageId, String role) {
        jdbc.update("INSERT INTO media_lien_ket (media_id, loai_doi_tuong, ma_doi_tuong, vai_tro) "
                + "VALUES (?, 'PACKAGE', ?, ?)", media, packageId, role);
    }

    private static JsonNode sent(UUID... ids) {
        ArrayNode a = JSON.createArrayNode();
        for (UUID id : ids) {
            a.addObject().put("kind", "MEDIA").put("media_id", id.toString()).put("status", "SENT");
        }
        return a;
    }

    /** Kênh giả ghi lại từng lần gửi */
    static final class RecordingSender implements ChannelSender {
        final List<String> calls = new ArrayList<>();
        boolean files = true;

        @Override
        public ChannelCode channel() {
            return ChannelCode.MESSENGER;
        }

        @Override
        public int maxTextLength() {
            return 2000;
        }

        @Override
        public String sendText(ChannelIdentity recipient, String text) {
            calls.add("text " + text);
            return "mid." + calls.size();
        }

        @Override
        public boolean supportsFiles() {
            return files;
        }

        @Override
        public String sendFile(ChannelIdentity recipient, OutgoingFile file) {
            assertThat(file.content().get()).isEqualTo(IMAGE);
            calls.add("file " + file.kind() + " " + file.fileName() + " key=" + file.key());
            return "mid." + calls.size();
        }
    }
}
