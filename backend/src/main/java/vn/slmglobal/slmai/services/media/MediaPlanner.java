package vn.slmglobal.slmai.services.media;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.slmglobal.slmai.services.catalog.CatalogRepository;
import vn.slmglobal.slmai.services.catalog.CatalogRepository.CatalogPackage;
import vn.slmglobal.slmai.services.catalog.PackageQuoteImageService;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.conversation.entity.Message;
import vn.slmglobal.slmai.services.conversation.entity.MessageDirection;
import vn.slmglobal.slmai.services.media.entity.LinkRole;
import vn.slmglobal.slmai.services.media.entity.LinkTargetType;
import vn.slmglobal.slmai.services.media.entity.MediaType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Chọn media gửi kèm một câu trả lời (mục 7.1–7.3), trước khi chốt lượt; kết quả ghi vào tin_nhan.dinh_kem của tin AI
 * để gửi ngay (lượt tự gửi) hoặc gửi cùng nháp khi nhân viên bấm gửi:
 * <ol>
 *   <li>Câu trả lời có link gói (…/products/{goi_id}): ảnh báo giá của gói, là ảnh chính đã duyệt trong thư viện,
 *   chưa có thì ảnh tự tạo theo mẫu ({@link PackageQuoteImageService}).</li>
 *   <li>Media AI chọn theo mã trong danh mục (trường media của kết quả).</li>
 *   <li>Có link gói mà chưa có video: một video công trình của gói đó, không có thì của gói cùng loại hệ gần công suất
 *   nhất.</li>
 * </ol>
 * Không gửi lại media đã gửi trong hội thoại; mỗi lượt tối đa {@value #MAX_VIDEOS} video, {@value #MAX_IMAGES} ảnh AI
 * chọn và ảnh báo giá của tối đa {@value #MAX_PACKAGES} gói.
 */
@Service
@RequiredArgsConstructor
public class MediaPlanner {

    static final int MAX_IMAGES = 2;
    static final int MAX_VIDEOS = 1;
    static final int MAX_PACKAGES = 2;

    /** Khoá của một mục trong tin_nhan.dinh_kem do trang gửi */
    public static final String KIND = "kind";
    public static final String KIND_MEDIA = "MEDIA";
    public static final String KIND_QUOTE = "QUOTE";
    public static final String STATUS = "status";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SENT = "SENT";

    private static final Pattern PRODUCT_LINK = Pattern.compile("https?://[^\\s\"<>]+?/products/(\\d+)");

    private final MediaCatalog mediaCatalog;
    private final CatalogRepository catalog;
    private final PackageQuoteImageService quoteImages;
    private final ObjectMapper objectMapper;

    /**
     * @param attachments các mục sẽ gửi, theo thứ tự gửi
     * @param dropped     mã AI chọn nhưng bị bỏ, kèm lý do (ghi log)
     */
    public record Plan(ArrayNode attachments, List<String> dropped) {
    }

    public Plan plan(ChannelCode channel, String reply, List<String> codes, List<Message> history) {
        return plan(channel, reply, codes, history, null);
    }

    /**
     * @param preselected media chọn sẵn trước khi gọi AI (tin nhắc khách, {@link #followUpExtra}); gửi đầu tiên và
     *                    tính vào giới hạn ảnh / video của lượt; null là không có
     */
    public Plan plan(ChannelCode channel, String reply, List<String> codes, List<Message> history,
                     ObjectNode preselected) {
        List<MediaCatalog.Entry> entries = mediaCatalog.entries(channel);
        Sent sent = sent(history);
        ArrayNode out = objectMapper.createArrayNode();
        List<String> dropped = new ArrayList<>();
        Set<UUID> chosen = new HashSet<>();
        int images = 0;
        int videos = 0;
        if (preselected != null) {
            out.add(preselected);
            if (KIND_MEDIA.equals(preselected.path(KIND).asText())) {
                chosen.add(UUID.fromString(preselected.path("media_id").asText()));
            }
            if ("VIDEO".equals(preselected.path("media_type").asText())) {
                videos++;
            } else {
                images++;
            }
        }

        List<Integer> packageIds = productIds(reply);
        for (int id : packageIds) {
            Optional<MediaCatalog.Entry> main = mainImage(entries, id);
            if (main.isPresent()) {
                if (!sent.media().contains(main.get().id()) && chosen.add(main.get().id())) {
                    out.add(media(main.get(), "AUTO", id));
                }
            } else if (!sent.quotes().contains(id) && !hasQuote(out, id) && quoteImages.canRender(id)) {
                out.add(quote(id));
            }
        }

        Map<String, MediaCatalog.Entry> byCode = entries.stream().filter(e -> e.code() != null)
                .collect(Collectors.toMap(MediaCatalog.Entry::code, Function.identity(), (a, b) -> a));
        for (String raw : codes == null ? List.<String>of() : codes) {
            String code = raw == null ? "" : raw.strip().toUpperCase();
            MediaCatalog.Entry e = byCode.get(code);
            if (e == null || e.packageMainImage()) {
                dropped.add(code + ": không có trong danh mục media");
            } else if (sent.media().contains(e.id())) {
                dropped.add(code + ": đã gửi trong hội thoại");
            } else if (!chosen.add(e.id())) {
                continue;
            } else if (e.type() == MediaType.VIDEO && videos >= MAX_VIDEOS) {
                dropped.add(code + ": quá " + MAX_VIDEOS + " video một lượt");
            } else if (e.type() == MediaType.IMAGE && images >= MAX_IMAGES) {
                dropped.add(code + ": quá " + MAX_IMAGES + " ảnh một lượt");
            } else {
                out.add(media(e, "AI", null));
                if (e.type() == MediaType.VIDEO) {
                    videos++;
                } else {
                    images++;
                }
            }
        }

        if (videos == 0 && !packageIds.isEmpty()) {
            similarVideo(entries, packageIds.get(0), sent.media(), chosen)
                    .ifPresent(v -> out.add(media(v, "AUTO", packageIds.get(0))));
        }
        return new Plan(out, dropped);
    }

    /**
     * Media kèm tin nhắc khách (mục 56.9), chọn trước khi gọi AI để AI nói đúng: một media chưa gửi của gói đã đề
     * xuất, ưu tiên video công trình của gói hoặc gói tương tự, rồi tới ảnh báo giá. Rỗng khi chưa đề xuất gói hoặc
     * đã gửi hết.
     */
    public Optional<ObjectNode> followUpExtra(ChannelCode channel, List<Message> history, List<Integer> packageIds) {
        if (packageIds == null || packageIds.isEmpty()) {
            return Optional.empty();
        }
        List<MediaCatalog.Entry> entries = mediaCatalog.entries(channel);
        Sent sent = sent(history);
        List<Integer> ids = packageIds.stream().distinct().limit(MAX_PACKAGES).toList();
        for (int id : ids) {
            Optional<MediaCatalog.Entry> video = similarVideo(entries, id, sent.media(), Set.of());
            if (video.isPresent()) {
                return Optional.of(media(video.get(), "AUTO", id));
            }
        }
        for (int id : ids) {
            Optional<MediaCatalog.Entry> main = mainImage(entries, id);
            if (main.isPresent()) {
                if (!sent.media().contains(main.get().id())) {
                    return Optional.of(media(main.get(), "AUTO", id));
                }
            } else if (!sent.quotes().contains(id) && quoteImages.canRender(id)) {
                return Optional.of(quote(id));
            }
        }
        return Optional.empty();
    }

    /** Mô tả một mục gửi kèm cho AI: "video M27 Video: Hệ Hy-Brid …", "Ảnh báo giá Hệ On-Grid …" */
    public static String describe(JsonNode item) {
        if (KIND_QUOTE.equals(item.path(KIND).asText())) {
            return item.path("title").asText("ảnh báo giá");
        }
        String code = item.path("code").asText("");
        return ("VIDEO".equals(item.path("media_type").asText()) ? "video " : "ảnh ")
                + (code.isEmpty() ? "" : code + " ") + item.path("title").asText("");
    }

    /** Gửi link gói này thì hệ thống gửi kèm gì (đưa vào kết quả công cụ để AI giới thiệu đúng) */
    public List<String> extrasForPackage(ChannelCode channel, int packageId) {
        List<MediaCatalog.Entry> entries = mediaCatalog.entries(channel);
        List<String> out = new ArrayList<>();
        if (mainImage(entries, packageId).isPresent() || quoteImages.canRender(packageId)) {
            out.add("ảnh báo giá của gói");
        }
        similarVideo(entries, packageId, Set.of(), Set.of()).ifPresent(v ->
                out.add(v.linkedTo(LinkTargetType.PACKAGE, String.valueOf(packageId))
                        ? "video công trình thực tế của gói này" : "video công trình thực tế của gói tương tự"));
        return out;
    }

    /** goi_id trong các link gói của câu trả lời, theo thứ tự, không trùng */
    static List<Integer> productIds(String reply) {
        Set<Integer> ids = new LinkedHashSet<>();
        Matcher m = PRODUCT_LINK.matcher(reply == null ? "" : reply);
        while (m.find() && ids.size() < MAX_PACKAGES) {
            try {
                ids.add(Integer.parseInt(m.group(1)));
            } catch (NumberFormatException ignored) {
                // số quá lớn: không phải goi_id
            }
        }
        return List.copyOf(ids);
    }

    private static Optional<MediaCatalog.Entry> mainImage(List<MediaCatalog.Entry> entries, int packageId) {
        String key = String.valueOf(packageId);
        return entries.stream().filter(e -> e.type() == MediaType.IMAGE && e.links().stream().anyMatch(l ->
                l.targetType() == LinkTargetType.PACKAGE && l.targetKey().equals(key)
                        && l.role() == LinkRole.MAIN_IMAGE)).findFirst();
    }

    /** Video của chính gói; không có thì video gắn với gói cùng loại hệ, ưu tiên cùng số pha, gần công suất nhất */
    private Optional<MediaCatalog.Entry> similarVideo(List<MediaCatalog.Entry> entries, int packageId,
                                                      Set<UUID> sent, Set<UUID> chosen) {
        List<MediaCatalog.Entry> videos = entries.stream()
                .filter(e -> e.type() == MediaType.VIDEO && !sent.contains(e.id()) && !chosen.contains(e.id())
                        && !e.packageIds().isEmpty())
                .toList();
        if (videos.isEmpty()) {
            return Optional.empty();
        }
        String key = String.valueOf(packageId);
        Optional<MediaCatalog.Entry> own = videos.stream().filter(v -> v.linkedTo(LinkTargetType.PACKAGE, key))
                .findFirst();
        if (own.isPresent()) {
            return own;
        }
        Map<Integer, CatalogPackage> packages = catalog.allPackages().stream()
                .collect(Collectors.toMap(CatalogPackage::id, Function.identity(), (a, b) -> a));
        CatalogPackage target = packages.get(packageId);
        if (target == null || target.kwp() == null) {
            return Optional.empty();
        }
        return videos.stream()
                .map(v -> new Scored(v, v.packageIds().stream().map(packages::get).filter(Objects::nonNull)
                        .filter(p -> Objects.equals(p.systemType(), target.systemType()) && p.kwp() != null)
                        .mapToDouble(p -> (Objects.equals(p.phases(), target.phases()) ? 0 : 1000)
                                + Math.abs(p.kwp() - target.kwp()))
                        .min().orElse(Double.MAX_VALUE)))
                .filter(s -> s.score() < Double.MAX_VALUE)
                .min(Comparator.comparingDouble(Scored::score))
                .map(Scored::entry);
    }

    private record Scored(MediaCatalog.Entry entry, double score) {
    }

    /** Ảnh báo giá tự tạo của gói (PackageQuoteImageService), dựng lúc gửi */
    private ObjectNode quote(int packageId) {
        ObjectNode q = objectMapper.createObjectNode();
        q.put(KIND, KIND_QUOTE);
        q.put("package_id", packageId);
        q.put("title", "Ảnh báo giá " + catalog.findPackage(packageId).map(CatalogPackage::name)
                .orElse("gói " + packageId));
        q.put("by", "AUTO");
        q.put(STATUS, STATUS_PENDING);
        return q;
    }

    private static boolean hasQuote(ArrayNode out, int packageId) {
        for (JsonNode a : out) {
            if (KIND_QUOTE.equals(a.path(KIND).asText()) && a.path("package_id").asInt(-1) == packageId) {
                return true;
            }
        }
        return false;
    }

    private ObjectNode media(MediaCatalog.Entry e, String by, Integer packageId) {
        ObjectNode o = objectMapper.createObjectNode();
        o.put(KIND, KIND_MEDIA);
        o.put("media_id", e.id().toString());
        o.put("source_id", e.sourceId().toString());
        o.put("code", e.code());
        o.put("media_type", e.type().name());
        o.put("title", e.title());
        o.put("by", by);
        if (packageId != null) {
            o.put("package_id", packageId);
        }
        o.put(STATUS, STATUS_PENDING);
        return o;
    }

    /** Media đã tới khách trong hội thoại (theo dinh_kem của tin trang đã gửi) */
    private record Sent(Set<UUID> media, Set<Integer> quotes) {
    }

    private static Sent sent(List<Message> history) {
        Set<UUID> media = new HashSet<>();
        Set<Integer> quotes = new HashSet<>();
        for (Message m : history == null ? List.<Message>of() : history) {
            if (m.getDirection() != MessageDirection.OUT || m.getAttachments() == null) {
                continue;
            }
            for (JsonNode a : m.getAttachments()) {
                if (!STATUS_SENT.equals(a.path(STATUS).asText())) {
                    continue;
                }
                if (KIND_MEDIA.equals(a.path(KIND).asText())) {
                    try {
                        media.add(UUID.fromString(a.path("media_id").asText()));
                    } catch (IllegalArgumentException ignored) {
                        // mục hỏng: bỏ qua
                    }
                } else if (KIND_QUOTE.equals(a.path(KIND).asText()) && a.path("package_id").canConvertToInt()) {
                    quotes.add(a.path("package_id").asInt());
                }
            }
        }
        return new Sent(media, quotes);
    }
}
