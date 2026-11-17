package vn.slmglobal.slmai.services.media;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.common.security.AuthInfo;
import vn.slmglobal.slmai.common.security.RoleCode;
import vn.slmglobal.slmai.common.storage.StorageService;
import vn.slmglobal.slmai.services.media.MediaAdminService.BulkAction;
import vn.slmglobal.slmai.services.media.MediaAdminService.BulkRequest;
import vn.slmglobal.slmai.services.media.MediaAdminService.BulkResult;
import vn.slmglobal.slmai.services.media.MediaAdminService.LinkInput;
import vn.slmglobal.slmai.services.media.MediaAdminService.LinksResult;
import vn.slmglobal.slmai.services.media.MediaAdminService.MediaResponse;
import vn.slmglobal.slmai.services.media.MediaAdminService.UpdateRequest;
import vn.slmglobal.slmai.services.media.MediaAdminService.CreateInfo;
import vn.slmglobal.slmai.services.media.MediaSourceService.CreateLinkRequest;
import vn.slmglobal.slmai.services.media.MediaSourceService.CreateResult;
import vn.slmglobal.slmai.services.media.MediaSourceService.FileMeta;
import vn.slmglobal.slmai.services.media.MediaSourceService.LinkRequest;
import vn.slmglobal.slmai.services.media.MediaSourceService.Outcome;
import vn.slmglobal.slmai.services.media.MediaTopicService.TopicRequest;
import vn.slmglobal.slmai.services.media.entity.AvailabilityStatus;
import vn.slmglobal.slmai.services.media.entity.LinkRole;
import vn.slmglobal.slmai.services.media.entity.LinkTargetType;
import vn.slmglobal.slmai.services.media.entity.MediaStatus;
import vn.slmglobal.slmai.services.media.entity.RightsStatus;
import vn.slmglobal.slmai.services.media.repository.MediaTargetRepository;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Trang quản lý media đầy đủ (2026-11-12) trên PostgreSQL thật; MinIO, Drive và oEmbed được giả lập. */
@Testcontainers
@SpringBootTest(properties = {
        "app.jwt.signer-key=test-signer-key-0123456789-abcdefghijklmnop",
        "app.conversation.worker-enabled=false",
        "app.ai.worker-enabled=false"
})
class MediaLibraryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final AuthInfo ACTOR = new AuthInfo(null, "admin@test.local", "Quản trị",
            Set.of(RoleCode.CONTENT_ADMIN));
    private static final String YOUTUBE = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";

    @MockitoBean
    StorageService storage;
    @MockitoBean
    DriveImageFetcher drive;
    @MockitoBean
    LinkPreviewClient previews;
    @MockitoBean
    MediaDraftService drafts;

    @Autowired
    MediaSourceService sourceService;
    @Autowired
    MediaAdminService admin;
    @Autowired
    MediaTopicService topicService;
    @Autowired
    JdbcTemplate jdbc;

    @BeforeEach
    void setUp() throws Exception {
        jdbc.execute("TRUNCATE media, media_nguon, media_lien_ket, nhat_ky_thao_tac CASCADE");
        jdbc.execute("DELETE FROM dm_tron_goi WHERE id_nguon IN (97, 98)");
        jdbc.update("UPDATE chu_de SET dang_dung = true");
        when(storage.isAvailable()).thenReturn(true);
        when(storage.bucket()).thenReturn("slm-ai-media");
        when(storage.get(anyString())).thenAnswer(i -> new ByteArrayInputStream(DriveImageFetcherTest.jpeg(8, 8)));
        when(previews.preview(any())).thenReturn(Optional.empty());
    }

    @Test
    void uploadedFilesBecomeDraftMediaAndDuplicatesAreReported() throws Exception {
        CreateResult image = upload("anh_he-5kWp mai ton.jpg", DriveImageFetcherTest.jpeg(30, 20), null, null);
        assertThat(image.outcome()).isEqualTo(Outcome.CREATED);
        MediaResponse m = admin.get(image.mediaId());
        assertThat(m.type()).isEqualTo("IMAGE");
        assertThat(m.title()).isEqualTo("anh he 5kWp mai ton");
        assertThat(m.status()).isEqualTo("DRAFT");
        assertThat(m.rights()).isEqualTo("OWNED");
        assertThat(m.sources().get(0).width()).isEqualTo(30);
        verify(storage).put(argThat(k -> k.startsWith("media/images/")), any(), anyLong(), any());

        CreateResult again = upload("ban-sao.jpg", DriveImageFetcherTest.jpeg(30, 20), null, null);
        assertThat(again.outcome()).isEqualTo(Outcome.EXISTS);
        assertThat(again.mediaId()).isEqualTo(image.mediaId());

        assertThat(upload("ghi-chu.txt", "xin chào".getBytes(), null, null).note()).contains("Chỉ nhận ảnh");

        // Video: định dạng nhận theo nội dung, thông số và ảnh thu nhỏ do trình duyệt gửi
        CreateResult video = upload("lap-dat.mp4", MediaFilesTest.mp4("isom"), DriveImageFetcherTest.jpeg(16, 9),
                new FileMeta(1920, 1080, 42));
        MediaResponse v = admin.get(video.mediaId());
        assertThat(v.type()).isEqualTo("VIDEO");
        assertThat(v.sources().get(0).durationSeconds()).isEqualTo(42);
        assertThat(v.sources().get(0).hasThumbnail()).isTrue();
        assertThat(sourceService.thumbnailSource(v.id(), v.sources().get(0).id())).isPresent();
    }

    @Test
    void linksBecomeVideosWithPreviewAndDuplicatesAreReported() throws Exception {
        when(previews.preview(argThat(p -> p != null && "dQw4w9WgXcQ".equals(p.externalId())))).thenReturn(Optional.of(
                new LinkPreviewClient.Preview("Lắp điện mặt trời 5 kWp", "SLM Solar", "https://i.ytimg.com/vi/x.jpg")));

        CreateResult created = sourceService.createFromLink(ACTOR, new CreateLinkRequest(YOUTUBE + "&t=5", null));
        assertThat(created.outcome()).isEqualTo(Outcome.CREATED);
        MediaResponse m = admin.get(created.mediaId());
        assertThat(m.title()).isEqualTo("Lắp điện mặt trời 5 kWp");
        assertThat(m.rights()).isEqualTo("LINK_ONLY");
        assertThat(m.sources().get(0).authorName()).isEqualTo("SLM Solar");
        assertThat(m.sources().get(0).availabilityStatus()).isEqualTo("ACTIVE");

        assertThat(sourceService.createFromLink(ACTOR, new CreateLinkRequest("https://youtu.be/dQw4w9WgXcQ", null))
                .outcome()).isEqualTo(Outcome.EXISTS);
        CreateResult bad = sourceService.createFromLink(ACTOR, new CreateLinkRequest("https://example.com/v", null));
        assertThat(bad.outcome()).isEqualTo(Outcome.FAILED);
        assertThat(bad.note()).contains("Không nhận ra link");

        // Facebook không lấy được tiêu đề: dùng tiêu đề admin nhập
        CreateResult fb = sourceService.createFromLink(ACTOR, new CreateLinkRequest(
                "https://www.facebook.com/reel/1234567890123", titled("Reel bàn giao công trình")));
        assertThat(admin.get(fb.mediaId()).title()).isEqualTo("Reel bàn giao công trình");
    }

    @Test
    void everyContentFieldIsEditable() throws Exception {
        UUID id = upload("a.jpg", DriveImageFetcherTest.jpeg(5, 5), null, null).mediaId();

        MediaResponse m = admin.update(ACTOR, id, new UpdateRequest("Ảnh hệ 5 kWp", MediaStatus.APPROVED,
                RightsStatus.OWNED, "Hệ Hybrid 5 kWp trên mái tôn", "Khách hỏi gói 5 kWp", "Khách khiếu nại",
                "Mô tả", null, List.of("pin-luu-tru", "hybrid", "hybrid"), List.of("CONSULTING", "LEAD"),
                List.of("ZALO_OA", "MESSENGER")));
        assertThat(m.topics()).containsExactly("pin-luu-tru", "hybrid");
        assertThat(m.customerStages()).containsExactly("LEAD", "CONSULTING");
        assertThat(m.allowedChannels()).containsExactly("MESSENGER", "ZALO_OA");
        assertThat(m.indexCode()).isEqualTo("M01");

        assertThatThrownBy(() -> admin.update(ACTOR, id, withTopics(List.of("khong-co"))))
                .isInstanceOf(CommonException.class).hasMessageContaining("khong-co");
        assertThatThrownBy(() -> admin.update(ACTOR, id, new UpdateRequest("x", MediaStatus.DRAFT, RightsStatus.OWNED,
                null, null, null, null, null, null, List.of("VIP"), null)))
                .isInstanceOf(CommonException.class).hasMessageContaining("Giai đoạn khách");

        // Chủ đề đã tắt: media đang có thì giữ được, media khác không chọn mới được
        topicService.update(ACTOR, "hybrid", new TopicRequest(null, "Hệ Hybrid", null, null, false));
        assertThat(admin.update(ACTOR, id, withTopics(List.of("hybrid"))).topics()).containsExactly("hybrid");
        UUID other = upload("b.jpg", DriveImageFetcherTest.jpeg(6, 6), null, null).mediaId();
        assertThatThrownBy(() -> admin.update(ACTOR, other, withTopics(List.of("hybrid"))))
                .isInstanceOf(CommonException.class).hasMessageContaining("đã tắt");

        assertThat(admin.history(id)).extracting(MediaAdminService.HistoryEntry::action)
                .containsExactly("media.update", "media.update", "media.create");
    }

    @Test
    void sourcesCanBeAddedReplacedUpdatedAndRemoved() throws Exception {
        UUID video = sourceService.createFromLink(ACTOR, new CreateLinkRequest(YOUTUBE, null)).mediaId();

        MediaResponse two = sourceService.addLink(ACTOR, video, new LinkRequest(
                "https://www.tiktok.com/@slm/video/7301234567890123456", null));
        assertThat(two.sources()).extracting(MediaAdminService.SourceResponse::sourceType)
                .containsExactlyInAnyOrder("YOUTUBE", "TIKTOK");
        UUID tiktok = two.sources().stream().filter(s -> s.sourceType().equals("TIKTOK")).findFirst().orElseThrow()
                .id();

        // Sửa link = thay nguồn
        MediaResponse replaced = sourceService.addLink(ACTOR, video, new LinkRequest(
                "https://www.facebook.com/reel/1234567890123", tiktok));
        assertThat(replaced.sources()).extracting(MediaAdminService.SourceResponse::sourceType)
                .containsExactlyInAnyOrder("YOUTUBE", "FACEBOOK");

        UUID fb = replaced.sources().stream().filter(s -> s.sourceType().equals("FACEBOOK")).findFirst().orElseThrow()
                .id();
        MediaResponse updated = sourceService.updateSource(ACTOR, video, fb,
                new MediaSourceService.SourceUpdate(AvailabilityStatus.UNAVAILABLE, "Page SLM"));
        assertThat(updated.sources()).anySatisfy(s -> {
            assertThat(s.availabilityStatus()).isEqualTo("UNAVAILABLE");
            assertThat(s.authorName()).isEqualTo("Page SLM");
        });

        // Ảnh không thêm vào media video
        assertThatThrownBy(() -> sourceService.addFile(ACTOR, video, file("x.jpg", DriveImageFetcherTest.jpeg(3, 3)),
                null, null, null)).isInstanceOf(CommonException.class).hasMessageContaining("là video");

        MediaResponse one = sourceService.deleteSource(ACTOR, video, fb);
        assertThat(one.sources()).hasSize(1);
        assertThatThrownBy(() -> sourceService.deleteSource(ACTOR, video, one.sources().get(0).id()))
                .isInstanceOf(CommonException.class).hasMessageContaining("ít nhất một nguồn");
    }

    @Test
    void linksKeepOneMainImagePerPackageAndShowNames() throws Exception {
        insertPackages();
        List<MediaTargetRepository.Target> found = admin.searchTargets(LinkTargetType.PACKAGE, "hy-brid 5");
        assertThat(found).extracting(MediaTargetRepository.Target::key).containsExactly("98", "97");
        assertThat(found.get(0).twinKey()).isEqualTo("97");

        UUID a = upload("a.jpg", DriveImageFetcherTest.jpeg(5, 5), null, null).mediaId();
        UUID b = upload("b.jpg", DriveImageFetcherTest.jpeg(7, 7), null, null).mediaId();
        LinksResult first = admin.updateLinks(ACTOR, a, List.of(
                new LinkInput(LinkTargetType.PACKAGE, "98", LinkRole.MAIN_IMAGE, 0),
                new LinkInput(LinkTargetType.PACKAGE, "97", LinkRole.MAIN_IMAGE, 0)));
        assertThat(first.media().links()).extracting(MediaAdminService.LinkResponse::label)
                .containsExactlyInAnyOrder("#98 Hệ Hy-Brid 5 kWp 1 pha (HN)", "#97 Hệ Hy-Brid 5 kWp 1 pha (HCM)");

        LinksResult second = admin.updateLinks(ACTOR, b, List.of(
                new LinkInput(LinkTargetType.PACKAGE, "98", LinkRole.MAIN_IMAGE, 0)));
        assertThat(second.notes()).singleElement().satisfies(n -> assertThat(n).contains("Gói 98", "ảnh thêm"));
        assertThat(jdbc.queryForObject("SELECT vai_tro FROM media_lien_ket WHERE media_id = ? AND ma_doi_tuong = '98'",
                String.class, a)).isEqualTo("GALLERY");

        assertThatThrownBy(() -> admin.updateLinks(ACTOR, a, List.of(
                new LinkInput(LinkTargetType.PACKAGE, "999", LinkRole.GALLERY, 0))))
                .isInstanceOf(CommonException.class).hasMessageContaining("999");
        UUID video = sourceService.createFromLink(ACTOR, new CreateLinkRequest(YOUTUBE, null)).mediaId();
        assertThatThrownBy(() -> admin.updateLinks(ACTOR, video, List.of(
                new LinkInput(LinkTargetType.PACKAGE, "98", LinkRole.MAIN_IMAGE, 0))))
                .isInstanceOf(CommonException.class).hasMessageContaining("Chỉ ảnh");
        // Gỡ hết liên kết
        assertThat(admin.updateLinks(ACTOR, b, List.of()).media().links()).isEmpty();
    }

    @Test
    void bulkActionsReportEachItem() throws Exception {
        UUID ready = upload("a.jpg", DriveImageFetcherTest.jpeg(5, 5), null, null).mediaId();
        UUID empty = upload("b.jpg", DriveImageFetcherTest.jpeg(6, 6), null, null).mediaId();
        admin.update(ACTOR, ready, new UpdateRequest("Ảnh A", MediaStatus.DRAFT, RightsStatus.OWNED, "Tóm tắt",
                "Dùng khi", null, null, null));

        List<BulkResult> approved = admin.bulk(ACTOR, new BulkRequest(List.of(ready, empty), BulkAction.SET_STATUS,
                MediaStatus.APPROVED, null, null, null));
        assertThat(approved).extracting(BulkResult::ok).containsExactly(true, false);
        assertThat(approved.get(1).note()).contains("Tóm tắt");
        assertThat(admin.get(ready).indexCode()).isEqualTo("M01");

        admin.bulk(ACTOR, new BulkRequest(List.of(ready, empty), BulkAction.ADD_TOPICS, null, null,
                List.of("lap-dat"), null));
        assertThat(admin.get(empty).topics()).containsExactly("lap-dat");

        assertThatThrownBy(() -> admin.bulk(ACTOR, new BulkRequest(List.of(ready, empty), BulkAction.ADD_LINK, null,
                null, null, new LinkInput(LinkTargetType.PACKAGE, "98", LinkRole.MAIN_IMAGE, 0))))
                .isInstanceOf(CommonException.class).hasMessageContaining("ảnh chính");

        admin.bulk(ACTOR, new BulkRequest(List.of(ready, empty), BulkAction.DELETE, null, null, null, null));
        assertThat(admin.get(ready).deleted()).isTrue();
        admin.bulk(ACTOR, new BulkRequest(List.of(ready), BulkAction.RESTORE, null, null, null, null));
        assertThat(admin.get(ready).deleted()).isFalse();
    }

    @Test
    void addingMediaTakesEveryFieldAtOnce() throws Exception {
        insertPackages();
        CreateResult r = sourceService.createFromFile(ACTOR, file("x.jpg", DriveImageFetcherTest.jpeg(9, 9)), null,
                null, new CreateInfo("Ảnh hệ 5 kWp mái tôn", MediaStatus.APPROVED, RightsStatus.OWNED,
                        "Hệ Hybrid 5 kWp trên mái tôn", "Khách hỏi gói 5 kWp", "Khách khiếu nại", "Mô tả", "bỏ qua",
                        List.of("hybrid"), List.of("LEAD"), List.of("MESSENGER", "ZALO_OA"),
                        List.of(new LinkInput(LinkTargetType.PACKAGE, "98", LinkRole.MAIN_IMAGE, 0)), false));
        assertThat(r.outcome()).isEqualTo(Outcome.CREATED);

        MediaResponse m = admin.get(r.mediaId());
        assertThat(m.title()).isEqualTo("Ảnh hệ 5 kWp mái tôn");
        assertThat(m.status()).isEqualTo("APPROVED");
        assertThat(m.indexCode()).isEqualTo("M01");
        assertThat(m.summary()).isEqualTo("Hệ Hybrid 5 kWp trên mái tôn");
        assertThat(m.avoidWhen()).isEqualTo("Khách khiếu nại");
        assertThat(m.transcript()).isNull();
        assertThat(m.topics()).containsExactly("hybrid");
        assertThat(m.customerStages()).containsExactly("LEAD");
        assertThat(m.allowedChannels()).containsExactly("MESSENGER", "ZALO_OA");
        assertThat(m.links()).singleElement().satisfies(l -> assertThat(l.role()).isEqualTo("MAIN_IMAGE"));
        assertThat(admin.history(r.mediaId())).extracting(MediaAdminService.HistoryEntry::action)
                .contains("media.create", "media.update", "media.links");
    }

    @Test
    void wrongInfoIsRejectedBeforeAnythingIsStored() throws Exception {
        CreateResult unknownTopic = sourceService.createFromFile(ACTOR, file("x.jpg", DriveImageFetcherTest.jpeg(9, 9)),
                null, null, withTopicsInfo(List.of("khong-co")));
        assertThat(unknownTopic.outcome()).isEqualTo(Outcome.FAILED);
        assertThat(unknownTopic.note()).contains("khong-co");

        CreateResult approvedEmpty = sourceService.createFromFile(ACTOR, file("x.jpg",
                DriveImageFetcherTest.jpeg(9, 9)), null, null, new CreateInfo(null, MediaStatus.APPROVED, null, null,
                null, null, null, null, null, null, null, null, false));
        assertThat(approvedEmpty.note()).contains("Tóm tắt");

        CreateResult videoAsMainImage = sourceService.createFromLink(ACTOR, new CreateLinkRequest(YOUTUBE,
                new CreateInfo(null, null, null, null, null, null, null, null, null, null, null,
                        List.of(new LinkInput(LinkTargetType.PACKAGE, "98", LinkRole.MAIN_IMAGE, 0)), false)));
        assertThat(videoAsMainImage.note()).contains("Chỉ ảnh");

        verify(storage, never()).put(anyString(), any(), anyLong(), any());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM media", Integer.class)).isZero();
    }

    @Test
    void aiDraftAfterAddingFillsOnlyEmptyFieldsAndKeepsDraft() throws Exception {
        when(drafts.draft(any(), any())).thenReturn(new MediaDraftService.Draft("AI tóm tắt", "AI dùng khi",
                "AI tránh khi", "AI mô tả", List.of("lap-dat", "khong-co"), false));

        CreateResult r = sourceService.createFromLink(ACTOR, new CreateLinkRequest(YOUTUBE, new CreateInfo(
                "Video lắp đặt", MediaStatus.APPROVED, null, "Admin tóm tắt", null, null, null, null,
                List.of("hybrid"), null, null, null, true)));

        assertThat(r.note()).contains("AI đã viết nháp");
        MediaResponse m = admin.get(r.mediaId());
        // Có AI viết nháp thì giữ ở Nháp để admin xem lại; ô admin đã nhập giữ nguyên
        assertThat(m.status()).isEqualTo("DRAFT");
        assertThat(m.summary()).isEqualTo("Admin tóm tắt");
        assertThat(m.useWhen()).isEqualTo("AI dùng khi");
        assertThat(m.topics()).containsExactly("hybrid", "lap-dat");

        // AI lỗi: media vẫn được thêm, chỉ báo lại
        when(drafts.draft(any(), any()))
                .thenThrow(new CommonException(HttpStatus.BAD_GATEWAY, "Gọi AI lỗi: hết hạn mức"));
        CreateResult failed = sourceService.createFromLink(ACTOR, new CreateLinkRequest(
                "https://www.facebook.com/reel/1234567890123", new CreateInfo(null, null, null, null, null, null, null,
                null, null, null, null, null, true)));
        assertThat(failed.outcome()).isEqualTo(Outcome.CREATED);
        assertThat(failed.note()).contains("AI viết nháp lỗi");
    }

    @Test
    void topicsCanBeAddedAndEdited() throws Exception {
        topicService.create(ACTOR, new TopicRequest("ve-sinh", "Vệ sinh tấm pin", "Rửa, lau tấm pin", 85, null));
        assertThat(topicService.list()).extracting(MediaTopicService.TopicResponse::code).contains("ve-sinh");
        assertThatThrownBy(() -> topicService.create(ACTOR, new TopicRequest("ve-sinh", "Trùng", null, null, null)))
                .isInstanceOf(CommonException.class).hasMessageContaining("Đã có");
        MediaTopicService.TopicResponse off = topicService.update(ACTOR, "ve-sinh",
                new TopicRequest(null, "Vệ sinh, bảo dưỡng", null, null, false));
        assertThat(off.name()).isEqualTo("Vệ sinh, bảo dưỡng");
        assertThat(off.active()).isFalse();
        jdbc.update("DELETE FROM chu_de WHERE ma = 've-sinh'");
    }

    private CreateResult upload(String name, byte[] bytes, byte[] thumbnail, FileMeta meta) {
        return sourceService.createFromFile(ACTOR, file(name, bytes),
                thumbnail == null ? null : new MockMultipartFile("thumbnail", "thumb.jpg", "image/jpeg", thumbnail),
                meta, null);
    }

    private static MockMultipartFile file(String name, byte[] bytes) {
        return new MockMultipartFile("file", name, "application/octet-stream", bytes);
    }

    private void insertPackages() {
        jdbc.update("""
                INSERT INTO dm_tron_goi (id_nguon, ma_co_so, ten, loai_he_thong, so_pha, dang_dung, duoc_goi_y, ma_bam,
                                         dong_bo_luc)
                VALUES (98, 'HN', 'Hệ Hy-Brid 5 kWp 1 pha', 'HYBRID', 1, true, true, 'a', now()),
                       (97, 'HCM', 'Hệ Hy-Brid 5 kWp 1 pha', 'HYBRID', 1, true, true, 'b', now())""");
    }

    private static CreateInfo titled(String title) {
        return new CreateInfo(title, null, null, null, null, null, null, null, null, null, null, null, false);
    }

    private static CreateInfo withTopicsInfo(List<String> topics) {
        return new CreateInfo(null, null, null, null, null, null, null, null, topics, null, null, null, false);
    }

    private static UpdateRequest withTopics(List<String> topics) {
        return new UpdateRequest("Ảnh", MediaStatus.DRAFT, RightsStatus.OWNED, null, null, null, null, null, topics,
                null, null);
    }
}
