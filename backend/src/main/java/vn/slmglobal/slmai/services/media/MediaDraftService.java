package vn.slmglobal.slmai.services.media;

import com.anthropic.core.RequestOptions;
import com.anthropic.core.http.StreamResponse;
import com.anthropic.helpers.MessageAccumulator;
import com.anthropic.models.messages.Base64ImageSource;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.ImageBlockParam;
import com.anthropic.models.messages.JsonOutputFormat;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.RawMessageStreamEvent;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.TextBlock;
import com.anthropic.models.messages.ThinkingConfigAdaptive;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import vn.slmglobal.slmai.common.exception.CommonException;
import vn.slmglobal.slmai.common.security.AuthInfo;
import vn.slmglobal.slmai.common.storage.StorageService;
import vn.slmglobal.slmai.common.util.HashUtil;
import vn.slmglobal.slmai.services.ai.ClaudeChatClient;
import vn.slmglobal.slmai.services.ai.entity.AiPurpose;
import vn.slmglobal.slmai.services.ai.entity.AiRun;
import vn.slmglobal.slmai.services.ai.repository.AiRunRepository;
import vn.slmglobal.slmai.services.auth.AuditLogService;
import vn.slmglobal.slmai.services.media.MediaAdminService.LinkResponse;
import vn.slmglobal.slmai.services.media.MediaAdminService.MediaResponse;
import vn.slmglobal.slmai.services.media.entity.LinkTargetType;
import vn.slmglobal.slmai.services.media.entity.MediaSource;
import vn.slmglobal.slmai.services.media.entity.MediaType;
import vn.slmglobal.slmai.services.media.entity.Topic;
import vn.slmglobal.slmai.services.media.repository.MediaSourceRepository;
import vn.slmglobal.slmai.services.media.repository.TopicRepository;
import vn.slmglobal.slmai.services.settings.AiSettingsService;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * "AI viết nháp" trên trang media (mục 6.2, 6.3): Claude nhìn ảnh (hoặc ảnh thu nhỏ của video) cùng tiêu đề, gói
 * đang gắn, nội dung đã có, rồi viết nháp tóm tắt, "dùng khi", "tránh khi", mô tả và chủ đề. Kết quả chỉ điền vào
 * form, admin sửa rồi tự lưu / duyệt; không ghi gì vào media. Mỗi lần gọi ghi luot_goi_ai (CONTENT_ASSISTANT).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaDraftService {

    private static final long MAX_TOKENS = 4_000;
    private static final Duration TIMEOUT = Duration.ofMinutes(2);
    /** Ảnh gửi Claude: cạnh dài tối đa 1568 px (lớn hơn không thêm chi tiết), dưới giới hạn 5 MB base64 */
    private static final int MAX_EDGE = 1568;
    private static final int MAX_IMAGE_BYTES = 3_500_000;
    private static final String NONE = HashUtil.sha256Hex("none");

    private static final String INSTRUCTIONS = """
            Bạn giúp quản trị viên của một công ty điện mặt trời viết thông tin cho một media (ảnh hoặc video) trong \
            thư viện. Bot tư vấn khách trên Messenger chỉ hiểu media qua phần chữ này để quyết định có gửi media cho \
            khách hay không, nên viết đúng những gì media cho thấy và khi nào gửi thì có ích.

            Quy tắc:
            - Tiếng Việt có dấu, câu ngắn, rõ.
            - Chỉ dùng điều thấy trong ảnh hoặc có trong thông tin đi kèm (tiêu đề, gói đang gắn, nội dung đã có). \
            Không bịa con số, giá, công suất, hãng, địa điểm. Media đã gắn với gói thì được nêu tên gói đó.
            - Video không xem được nội dung: chỉ dựa vào tiêu đề, ảnh thu nhỏ (nếu có) và thông tin đi kèm; viết \
            thận trọng.
            - Nội dung đã có do admin viết: giữ các ý đúng, viết lại cho rõ, không bỏ thông tin admin đã thêm.

            Các trường:
            - summary: 1–2 câu, media cho thấy gì.
            - use_when: các tình huống trong hội thoại nên gửi media này (khách hỏi gì, đang ở bước nào của tư vấn); \
            1–3 ý, ngăn bằng dấu chấm phẩy.
            - avoid_when: khi nào không nên gửi (ví dụ khách đang khiếu nại, khách hỏi gói khác); không có thì chuỗi \
            rỗng.
            - description: mô tả chi tiết. Ảnh: thiết bị, bố cục, chữ trên ảnh. Video: nội dung theo tiêu đề và \
            thông tin đi kèm.
            - topics: 1–4 mã chủ đề phù hợp nhất, chỉ chọn trong danh sách được cho.""";

    private final ClaudeChatClient claude;
    private final AiSettingsService settings;
    private final AiRunRepository aiRuns;
    private final StorageService storage;
    private final MediaAdminService admin;
    private final MediaSourceRepository sources;
    private final TopicRepository topics;
    private final AuditLogService audit;
    private final TransactionTemplate tx;
    private final ObjectMapper objectMapper;

    public record Draft(String summary, String useWhen, String avoidWhen, String description, List<String> topics,
                        boolean sawImage) {
    }

    record ImageInput(byte[] bytes, Base64ImageSource.MediaType mediaType) {
    }

    public Draft draft(AuthInfo actor, UUID mediaId) {
        if (!settings.ai().hasApiKey()) {
            throw new CommonException(HttpStatus.SERVICE_UNAVAILABLE, "Chưa cấu hình ANTHROPIC_API_KEY");
        }
        MediaResponse m = admin.get(mediaId);
        List<Topic> active = topics.findAllByOrderBySortOrderAscCodeAsc().stream().filter(Topic::isActive).toList();
        Optional<ImageInput> image = image(mediaId);
        String model = settings.settings().assistantModel();

        List<ContentBlockParam> content = new ArrayList<>();
        image.ifPresent(img -> content.add(ContentBlockParam.ofImage(ImageBlockParam.builder()
                .source(Base64ImageSource.builder()
                        .data(Base64.getEncoder().encodeToString(img.bytes()))
                        .mediaType(img.mediaType()).build())
                .build())));
        content.add(ContentBlockParam.ofText(context(m, active, image.isPresent())));

        AiRun run = AiRun.builder()
                .purpose(AiPurpose.CONTENT_ASSISTANT)
                .model(model)
                .kbVersion(NONE)
                .catalogVersion(NONE)
                .mediaVersion(NONE)
                .promptVersion("media-draft:" + HashUtil.sha256Hex(INSTRUCTIONS).substring(0, 16))
                .build();
        long started = System.nanoTime();
        JsonNode output = null;
        String error = null;
        try {
            MessageCreateParams params = MessageCreateParams.builder()
                    .model(model)
                    .maxTokens(MAX_TOKENS)
                    .thinking(ThinkingConfigAdaptive.builder().build())
                    .outputConfig(OutputConfig.builder()
                            .effort(OutputConfig.Effort.of("low"))
                            .format(JsonOutputFormat.builder()
                                    .schema(ClaudeChatClient.schemaOf(schema(active))).build())
                            .build())
                    .system(INSTRUCTIONS)
                    .addUserMessageOfBlockParams(content)
                    .build();
            MessageAccumulator acc = MessageAccumulator.create();
            try (StreamResponse<RawMessageStreamEvent> stream = claude.sdk().messages().createStreaming(params,
                    RequestOptions.builder().timeout(TIMEOUT).build())) {
                stream.stream().forEach(acc::accumulate);
            }
            Message msg = acc.message();
            run.setModel(msg.model().asString());
            run.setInputTokens((int) msg.usage().inputTokens());
            run.setOutputTokens((int) msg.usage().outputTokens());
            run.setStopReason(msg.stopReason().map(StopReason::asString).orElse(null));
            if (msg.stopReason().map(StopReason.REFUSAL::equals).orElse(false)) {
                error = "AI từ chối viết cho media này";
            } else if (msg.stopReason().map(StopReason.MAX_TOKENS::equals).orElse(false)) {
                error = "AI viết quá dài, bị cắt giữa chừng; thử lại";
            } else {
                output = objectMapper.readTree(msg.content().stream().flatMap(b -> b.text().stream())
                        .map(TextBlock::text).collect(Collectors.joining()));
            }
        } catch (Exception e) {
            error = "Gọi AI lỗi: " + e.getMessage();
            log.warn("AI viết nháp media {} lỗi: {}", mediaId, e.toString());
        }
        run.setLatencyMs((int) ((System.nanoTime() - started) / 1_000_000));
        run.setOutput(output);
        run.setError(error == null ? null : error.length() > 2000 ? error.substring(0, 2000) : error);
        JsonNode result = output;
        tx.executeWithoutResult(st -> {
            AiRun saved = aiRuns.save(run);
            audit.record(actor.id(), "media.ai_draft", MediaAdminService.TARGET, mediaId, null,
                    Map.of("aiRunId", saved.getId(), "ok", result != null));
        });
        if (output == null) {
            throw new CommonException(HttpStatus.BAD_GATEWAY, error == null ? "AI không trả kết quả" : error);
        }
        List<String> codes = new ArrayList<>();
        output.path("topics").forEach(t -> codes.add(t.asText()));
        return new Draft(output.path("summary").asText(""), output.path("use_when").asText(""),
                output.path("avoid_when").asText(""), output.path("description").asText(""),
                codes.stream().distinct().toList(), image.isPresent());
    }

    /** Thông tin đi kèm ảnh: loại, tiêu đề, nguồn, gói / tài liệu đang gắn, nội dung đã có, danh sách chủ đề */
    private static String context(MediaResponse m, List<Topic> active, boolean hasImage) {
        StringBuilder sb = new StringBuilder();
        boolean video = MediaType.VIDEO.name().equals(m.type());
        sb.append("Loại: ").append(video ? "video" : "ảnh").append('\n');
        sb.append("Tiêu đề: ").append(m.title()).append('\n');
        for (MediaAdminService.SourceResponse s : m.sources()) {
            sb.append("Nguồn: ").append(s.sourceType());
            if (s.authorName() != null) {
                sb.append(", kênh / tác giả ").append(s.authorName());
            }
            if (s.durationSeconds() != null) {
                sb.append(", dài ").append(s.durationSeconds()).append(" giây");
            }
            sb.append('\n');
        }
        for (LinkResponse l : m.links()) {
            sb.append("Đang gắn với: ").append(MediaAdminService.targetName(LinkTargetType.valueOf(l.targetType())))
                    .append(' ').append(l.label() == null ? l.targetKey() : l.label())
                    .append(" (").append(l.role()).append(")\n");
        }
        Map<String, String> existing = new LinkedHashMap<>();
        existing.put("Tóm tắt", m.summary());
        existing.put("Dùng khi", m.useWhen());
        existing.put("Tránh khi", m.avoidWhen());
        existing.put("Mô tả", m.description());
        existing.put("Lời thoại", m.transcript());
        existing.forEach((k, v) -> {
            if (v != null && !v.isBlank()) {
                sb.append(k).append(" (admin đã viết): ").append(v).append('\n');
            }
        });
        if (!hasImage) {
            sb.append(video ? "Không xem được nội dung video và chưa có ảnh thu nhỏ.\n"
                    : "Không đọc được file ảnh, chỉ dựa vào thông tin trên.\n");
        } else if (video) {
            sb.append("Ảnh kèm theo là một khung hình của video.\n");
        }
        sb.append("\nDanh sách chủ đề (mã: tên, khi nào gắn):\n");
        for (Topic t : active) {
            sb.append("- ").append(t.getCode()).append(": ").append(t.getName());
            if (t.getDescription() != null) {
                sb.append(" (").append(t.getDescription()).append(')');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private static Map<String, Object> schema(List<Topic> active) {
        Map<String, Object> topicItem = active.isEmpty() ? Map.of("type", "string")
                : ordered("type", "string", "enum", active.stream().map(Topic::getCode).toList());
        Map<String, Object> properties = ordered(
                "summary", Map.of("type", "string"),
                "use_when", Map.of("type", "string"),
                "avoid_when", Map.of("type", "string"),
                "description", Map.of("type", "string"),
                "topics", ordered("type", "array", "items", topicItem));
        return ordered("type", "object", "properties", properties, "required", List.copyOf(properties.keySet()),
                "additionalProperties", false);
    }

    /** Ảnh để Claude xem: file ảnh đã lưu, hoặc ảnh thu nhỏ của video tải lên */
    private Optional<ImageInput> image(UUID mediaId) {
        for (MediaSource s : sources.findByMediaIdIn(List.of(mediaId))) {
            String key = s.getMimeType() != null && s.getMimeType().startsWith("image/") ? s.getObjectKey()
                    : s.getThumbnailKey();
            if (key == null) {
                continue;
            }
            try (InputStream in = storage.get(key)) {
                Optional<ImageInput> img = prepare(in.readNBytes((int) DriveImageFetcher.MAX_BYTES + 1));
                if (img.isPresent()) {
                    return img;
                }
            } catch (Exception e) {
                log.info("Không đọc được ảnh {} của media {}: {}", key, mediaId, e.getMessage());
            }
        }
        return Optional.empty();
    }

    /** Thu nhỏ ảnh lớn (JPEG / PNG) về cạnh dài 1568 px; WebP lớn quá thì bỏ (ImageIO không đọc được WebP) */
    static Optional<ImageInput> prepare(byte[] bytes) {
        Optional<MediaFiles.Format> format = MediaFiles.sniff(bytes).filter(f -> f.mediaType == MediaType.IMAGE);
        if (format.isEmpty() || bytes.length > DriveImageFetcher.MAX_BYTES) {
            return Optional.empty();
        }
        int[] size = MediaFiles.dimensions(bytes);
        boolean small = bytes.length <= MAX_IMAGE_BYTES
                && (size == null || Math.max(size[0], size[1]) <= MAX_EDGE);
        if (small) {
            return Optional.of(new ImageInput(bytes, switch (format.get()) {
                case PNG -> Base64ImageSource.MediaType.IMAGE_PNG;
                case WEBP -> Base64ImageSource.MediaType.IMAGE_WEBP;
                default -> Base64ImageSource.MediaType.IMAGE_JPEG;
            }));
        }
        try {
            BufferedImage src = ImageIO.read(new ByteArrayInputStream(bytes));
            if (src == null) {
                return Optional.empty();
            }
            double scale = Math.min(1.0, (double) MAX_EDGE / Math.max(src.getWidth(), src.getHeight()));
            int w = Math.max(1, (int) Math.round(src.getWidth() * scale));
            int h = Math.max(1, (int) Math.round(src.getHeight() * scale));
            BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = dst.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(src, 0, 0, w, h, Color.WHITE, null);
            g.dispose();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(dst, "jpg", out);
            return out.size() > MAX_IMAGE_BYTES ? Optional.empty()
                    : Optional.of(new ImageInput(out.toByteArray(), Base64ImageSource.MediaType.IMAGE_JPEG));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static Map<String, Object> ordered(Object... keyValues) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            m.put((String) keyValues[i], keyValues[i + 1]);
        }
        return m;
    }
}
