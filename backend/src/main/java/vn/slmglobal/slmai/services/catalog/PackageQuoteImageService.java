package vn.slmglobal.slmai.services.catalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import vn.slmglobal.slmai.adapters.gotenberg.GotenbergClient;
import vn.slmglobal.slmai.common.storage.StorageService;
import vn.slmglobal.slmai.common.util.HashUtil;
import vn.slmglobal.slmai.services.catalog.CatalogRepository.CatalogPackage;
import vn.slmglobal.slmai.services.catalog.QuoteImageTemplate.Device;
import vn.slmglobal.slmai.services.catalog.QuoteImageTemplate.Row;
import vn.slmglobal.slmai.services.catalog.QuoteImageTemplate.Spec;
import vn.slmglobal.slmai.services.catalog.QuoteImageTemplate.Title;
import vn.slmglobal.slmai.services.catalog.QuoteImageTemplate.Warranty;
import vn.slmglobal.slmai.services.sizing.VnFormat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ảnh báo giá gói tự tạo cho gói chưa có ảnh báo giá do thiết kế làm (2026-11-18): dựng trang HTML theo mẫu
 * ({@link QuoteImageTemplate}) từ số liệu catalog đang dùng (giá trọn gói, thiết bị, giá niêm yết, logo hãng và ảnh
 * vật tư của slm_v2), chụp thành JPEG qua Gotenberg rồi lưu MinIO ở {@code quotes/}. Tên tệp là mã băm của trang và
 * mọi tệp kèm, nên số liệu đổi (giá, thiết bị, mẫu) thì tự dựng ảnh mới, không đổi thì dùng lại ảnh đã lưu.
 */
@Slf4j
@Service
public class PackageQuoteImageService {

    /** Tăng khi đổi cách dựng trang mà HTML không đổi (hiếm) */
    static final String TEMPLATE_VERSION = "1";
    static final String KEY_PREFIX = "quotes/";
    static final int WIDTH = 1080;
    private static final Duration REMOTE_CACHE = Duration.ofHours(6);
    private static final int REMOTE_CACHE_MAX = 200;
    /** Gotenberg lỗi thì tạm coi như không tạo được ảnh trong chừng này, để AI không hứa gửi ảnh báo giá */
    static final Duration PAUSE_AFTER_FAILURE = Duration.ofMinutes(5);

    private static final Pattern KWP = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*kWp", Pattern.CASE_INSENSITIVE);
    private static final Pattern KW = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*kW(?![a-z])", Pattern.CASE_INSENSITIVE);
    private static final Pattern KWH = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*kWh", Pattern.CASE_INSENSITIVE);
    private static final Pattern WP = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*Wp", Pattern.CASE_INSENSITIVE);
    private static final Locale VI = Locale.forLanguageTag("vi-VN");

    private final CatalogRepository catalog;
    private final NamedParameterJdbcTemplate jdbc;
    private final CatalogImageService images;
    private final GotenbergClient gotenberg;
    private final StorageService storage;
    private final ObjectMapper objectMapper;
    /** Ảnh vật tư / logo hãng tải từ MinIO của slm_v2, giữ một lúc để không tải lại mỗi lần dựng */
    private final Map<String, Remote> remote = new ConcurrentHashMap<>();
    private volatile Map<String, byte[]> staticFiles;
    private volatile Instant pausedUntil = Instant.MIN;

    private record Remote(CatalogImageService.Image image, Instant at) {
    }

    /** Một dòng vật tư đang dùng của gói, kèm giá niêm yết hiện hành ở miền của gói */
    record Line(String name, String groupCode, String brand, String brandLogoUrl, String unit, Integer quantity,
                Integer warrantyMonths, Double price, boolean visible, JsonNode specs, String imageUrl) {
    }

    /**
     * @param objectKey đường dẫn trong bucket media của bot
     * @param sha256    mã băm của ảnh (kênh dùng lại tệp đã tải lên theo mã này)
     */
    public record QuoteImage(int packageId, String objectKey, String sha256, byte[] bytes) {
    }

    /** Trang đã dựng: HTML và các tệp nó dùng */
    record Built(String html, Map<String, byte[]> files) {
    }

    public PackageQuoteImageService(CatalogRepository catalog, NamedParameterJdbcTemplate jdbc,
                                    CatalogImageService images, GotenbergClient gotenberg, StorageService storage,
                                    ObjectMapper objectMapper) {
        this.catalog = catalog;
        this.jdbc = jdbc;
        this.images = images;
        this.gotenberg = gotenberg;
        this.storage = storage;
        this.objectMapper = objectMapper;
    }

    /** Có Gotenberg và MinIO để tạo ảnh */
    public boolean enabled() {
        return gotenberg.enabled() && storage.isAvailable();
    }

    /** Gói tạo được ảnh: đang có trong catalog, có giá trọn gói, và Gotenberg không vừa lỗi */
    public boolean canRender(int packageId) {
        return enabled() && Instant.now().isAfter(pausedUntil) && catalog.findPackage(packageId)
                .filter(p -> p.totalPrice() != null && p.totalPrice() > 0).isPresent();
    }

    /**
     * Ảnh báo giá của gói: dùng lại ảnh đã lưu nếu số liệu không đổi, không thì dựng mới. Rỗng khi không tạo được
     * (chưa cấu hình, gói không có giá, Gotenberg / MinIO lỗi); lỗi chỉ ghi log để lượt trả lời khách vẫn đi tiếp.
     */
    public Optional<QuoteImage> image(int packageId) {
        if (!enabled()) {
            return Optional.empty();
        }
        CatalogPackage p = catalog.findPackage(packageId).orElse(null);
        if (p == null || p.totalPrice() == null || p.totalPrice() <= 0) {
            return Optional.empty();
        }
        try {
            Built built = build(p);
            String hash = hash(built);
            String key = KEY_PREFIX + hash.substring(0, 2) + "/" + hash + ".jpg";
            byte[] saved = read(key);
            if (saved != null) {
                return Optional.of(new QuoteImage(packageId, key, HashUtil.sha256Hex(saved), saved));
            }
            long started = System.nanoTime();
            byte[] jpg;
            try {
                jpg = gotenberg.screenshotJpeg(built.html(), built.files(), WIDTH);
            } catch (RuntimeException e) {
                pausedUntil = Instant.now().plus(PAUSE_AFTER_FAILURE);
                throw e;
            }
            storage.put(key, new ByteArrayInputStream(jpg), jpg.length, "image/jpeg");
            log.info("Đã tạo ảnh báo giá gói {} ({} KB, {} ms)", packageId, jpg.length / 1024,
                    (System.nanoTime() - started) / 1_000_000);
            return Optional.of(new QuoteImage(packageId, key, HashUtil.sha256Hex(jpg), jpg));
        } catch (Exception e) {
            log.warn("Không tạo được ảnh báo giá gói {}: {}", packageId, e.toString());
            return Optional.empty();
        }
    }

    private byte[] read(String key) {
        try (InputStream in = storage.get(key)) {
            return in.readAllBytes();
        } catch (Exception e) {
            // Chưa có (NoSuchKey) hoặc đọc lỗi: dựng lại
            return null;
        }
    }

    private String hash(Built b) {
        StringBuilder sb = new StringBuilder(TEMPLATE_VERSION).append('\n').append(b.html());
        b.files().forEach((name, bytes) -> sb.append('\n').append(name).append('=').append(HashUtil.sha256Hex(bytes)));
        return HashUtil.sha256Hex(sb.toString());
    }

    // ------------------------------------------------------------------ dựng trang

    Built build(CatalogPackage p) {
        Map<String, byte[]> files = new LinkedHashMap<>(staticFiles());
        Assets assets = new Assets(files);
        double[] production = production(p.id());
        QuoteImageTemplate.Page page = page(p, production, lines(p.id(), p.branchCode()), assets);
        return new Built(QuoteImageTemplate.html(page), files);
    }

    static QuoteImageTemplate.Page page(CatalogPackage p, double[] production, List<Line> lines, AssetSink assets) {
        boolean hybrid = "HYBRID".equals(p.systemType());
        String name = p.name() == null ? "" : p.name();
        String lower = name.toLowerCase(VI);
        boolean premium = lower.contains("cao cấp");
        String brandWord = premium && p.inverterBrand() != null ? " " + p.inverterBrand().toUpperCase(VI) : "";
        String kwp = first(KWP, name).orElse(p.kwp() == null ? "" : VnFormat.decimal(p.kwp(), 2)) + " kWp";

        List<Title> title = new ArrayList<>();
        if (hybrid) {
            String storage = first(KWH, name).orElse(VnFormat.decimal(p.storageKwh(), 2)) + " kWh";
            String voltage = lower.contains("áp cao") ? " - ÁP CAO" : lower.contains("áp thấp") ? " - ÁP THẤP" : "";
            title.add(new Title("HỆ HY-BRID" + brandWord, kwp));
            title.add(new Title("LƯU TRỮ", storage + voltage));
        } else {
            title.add(new Title(premium && !brandWord.isEmpty() ? "HỆ CAO CẤP" + brandWord : "HỆ ĐIỆN MẶT TRỜI", ""));
            title.add(new Title("BÁM TẢI", kwp));
        }
        String productionText = production == null ? null
                : "Sản lượng trung bình " + thousands(production[0]) + "-" + thousands(production[1]) + " kWh/tháng";

        List<Device> devices = new ArrayList<>();
        List<Row> rows = new ArrayList<>();
        boolean rails = false;
        boolean cabinet = false;
        Set<String> otherRows = new HashSet<>();
        for (Line l : lines) {
            String group = l.groupCode() == null ? "" : l.groupCode();
            rails |= group.equals("HE_KHUNG_NHOM");
            cabinet |= group.equals("TU_DIEN");
            int q = l.quantity() == null ? 0 : l.quantity();
            if (!l.visible() || q <= 0) {
                continue;
            }
            String logo = "SLM".equalsIgnoreCase(l.brand()) ? QuoteImageTemplate.SOLARMAX_LOGO
                    : assets.add(l.brandLogoUrl(), "brand");
            String brand = "SLM".equalsIgnoreCase(l.brand()) ? "SolarMax" : l.brand();
            String qty = quantity(q, l.unit());
            switch (group) {
                case "TAM_PIN" -> {
                    String wp = spec(l.specs(), "cong_suat")
                            .orElse(first(WP, l.name()).map(v -> v + " Wp")
                                    .orElse(p.panelWp() == null ? null : VnFormat.decimal(p.panelWp(), 1) + " Wp"));
                    List<Spec> specs = new ArrayList<>();
                    if (wp != null) {
                        specs.add(new Spec("Công suất", wp));
                    }
                    spec(l.specs(), "hieu_suat").ifPresent(v -> specs.add(new Spec("Hiệu suất", v)));
                    specs.add(new Spec("Số lượng", qty));
                    devices.add(device(l, assets, "Tấm pin" + suffix(brand), null, specs, logo, brand));
                    rows.add(new Row("Tấm quang năng" + (wp == null ? "" : " " + wp), logo, brand, qty));
                }
                case "BIEN_TAN" -> {
                    String kw = first(KW, l.name()).map(v -> v + " kW").or(() -> spec(l.specs(), "cong_suat"))
                            .orElse(null);
                    String kind = (hybrid ? "Hy-Brid" : "On-Grid")
                            + (p.phases() == null ? "" : " | " + p.phases() + " pha");
                    List<Spec> specs = new ArrayList<>();
                    specs.add(new Spec("Hệ Inverter", kind));
                    if (kw != null) {
                        specs.add(new Spec("Công suất", kw));
                    }
                    devices.add(device(l, assets, "Biến tần" + suffix(brand), kw, specs, logo, brand));
                    rows.add(new Row("Biến tần " + (hybrid ? "Hy-Brid" : "On-Grid") + (kw == null ? "" : " " + kw)
                            + (p.phases() == null ? "" : " - " + p.phases() + " pha"), logo, brand, qty));
                }
                case "PIN_LUU_TRU" -> {
                    String kwh = spec(l.specs(), "dung_luong").or(() -> first(KWH, l.name()).map(v -> v + " kWh"))
                            .orElse(null);
                    List<Spec> specs = new ArrayList<>();
                    if (kwh != null) {
                        specs.add(new Spec("Dung lượng", kwh));
                    }
                    spec(l.specs(), "cach_lap_dat").ifPresent(v -> specs.add(new Spec("Lắp đặt", v)));
                    if (q > 1) {
                        specs.add(new Spec("Số lượng", qty));
                    }
                    devices.add(device(l, assets, "Pin" + suffix(brand), kwh, specs, logo, brand));
                    rows.add(new Row("Pin lưu trữ" + (kwh == null ? "" : " " + kwh), logo, brand, qty));
                }
                default -> {
                    // Phần khách thấy khác (nhân công lắp đặt...): một dòng, không lặp tên
                    if (otherRows.add(l.name())) {
                        rows.add(new Row(l.name(), logo, brand,
                                "TRON_GOI_LAP_DAT".equals(group) ? "Trọn gói" : qty));
                    }
                }
            }
        }
        // Khung và tủ điện slm_v2 để ẩn với khách; ảnh mẫu vẫn liệt kê như hai bộ của SolarMax
        int at = Math.min(rows.size(), devices.size());
        if (cabinet) {
            rows.add(at, new Row("Bộ tủ điện Năng lượng mặt trời", QuoteImageTemplate.SOLARMAX_LOGO, "SolarMax",
                    "01 bộ"));
        }
        if (rails) {
            rows.add(at, new Row("Hệ ray nhôm đỡ tấm quang năng", QuoteImageTemplate.SOLARMAX_LOGO, "SolarMax",
                    "01 bộ"));
        }
        return new QuoteImageTemplate.Page(hybrid, title, productionText, p.phases(), devices,
                thousands(p.totalPrice()), rows);
    }

    private static Device device(Line l, AssetSink assets, String title, String light, List<Spec> specs, String logo,
                                 String brand) {
        String weight = number(l.specs(), "khoi_luong").map(v -> VnFormat.decimal(v, 1)).orElse(null);
        return new Device(assets.add(l.imageUrl(), "photo"), warranty(l.warrantyMonths()), title, light, specs,
                l.price() == null || l.price() <= 0 ? null : thousands(l.price()), logo, brand, weight,
                size(l.specs()));
    }

    private static String suffix(String brand) {
        return brand == null || brand.isBlank() ? "" : " " + brand;
    }

    private static Warranty warranty(Integer months) {
        if (months == null || months <= 0) {
            return null;
        }
        return months % 12 == 0 ? new Warranty(months / 12, "NĂM") : new Warranty(months, "THÁNG");
    }

    /** 30 tấm, 01 bộ */
    private static String quantity(int q, String unit) {
        String u = unit == null || unit.isBlank() ? "bộ" : unit.strip().toLowerCase(VI);
        return (q < 10 ? "0" + q : String.valueOf(q)) + " " + u;
    }

    /** "2382 x 1134 x 30" mm → "2382x1134\nx30mm" như ô kích thước của ảnh mẫu */
    static String size(JsonNode specs) {
        JsonNode n = specs == null ? null : specs.path("kich_thuoc");
        String v = n == null ? null : text(n.path("value"));
        if (v == null) {
            return null;
        }
        String s = v.replaceAll("\\s*[x×*X]\\s*", "x").replaceAll("\\s+", "");
        if (!s.matches("[0-9.,]+(x[0-9.,]+)+")) {
            return null;
        }
        String unit = text(n.path("unit"));
        unit = unit == null ? "mm" : unit;
        int cut = s.lastIndexOf('x');
        return s.substring(0, cut) + "\n" + s.substring(cut) + unit;
    }

    /** Thông số có giá trị thật: bỏ giá trị trống, 0 và chữ giữ chỗ ("abc"); kèm đơn vị */
    static Optional<String> spec(JsonNode specs, String key) {
        JsonNode n = specs == null ? null : specs.path(key);
        if (n == null || n.isMissingNode()) {
            return Optional.empty();
        }
        String unit = text(n.path("unit"));
        JsonNode v = n.path("value");
        String value;
        if (v.isNumber()) {
            if (v.asDouble() <= 0) {
                return Optional.empty();
            }
            value = VnFormat.decimal(v.asDouble(), 2);
        } else {
            value = text(v);
            if (value == null || value.equalsIgnoreCase("abc") || value.equals("0")) {
                return Optional.empty();
            }
        }
        if (unit != null && !value.toLowerCase(VI).endsWith(unit.toLowerCase(VI))) {
            value = value + (unit.equals("%") ? "" : " ") + unit;
        }
        return Optional.of(value);
    }

    private static Optional<Double> number(JsonNode specs, String key) {
        JsonNode v = specs == null ? null : specs.path(key).path("value");
        if (v == null) {
            return Optional.empty();
        }
        if (v.isNumber()) {
            return v.asDouble() > 0 ? Optional.of(v.asDouble()) : Optional.empty();
        }
        String t = text(v);
        try {
            double d = t == null ? 0 : Double.parseDouble(t.replace(',', '.'));
            return d > 0 ? Optional.of(d) : Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private static String text(JsonNode n) {
        return n != null && n.isTextual() && !n.asText().isBlank() ? n.asText().strip() : null;
    }

    private static Optional<String> first(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text == null ? "" : text);
        return m.find() ? Optional.of(m.group(1).replace('.', ',')) : Optional.empty();
    }

    /** 154300000 → "154.300.000" */
    static String thousands(double v) {
        return new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(VI)).format(Math.round(v));
    }

    // ------------------------------------------------------------------ số liệu

    private double[] production(int packageId) {
        List<double[]> r = jdbc.query("""
                SELECT san_luong_thang_min, san_luong_thang_max FROM dm_tron_goi
                WHERE id_nguon = :id AND san_luong_thang_min > 0 AND san_luong_thang_max >= san_luong_thang_min""",
                new MapSqlParameterSource("id", packageId),
                (rs, i) -> new double[]{rs.getDouble(1), rs.getDouble(2)});
        return r.isEmpty() ? null : r.get(0);
    }

    List<Line> lines(int packageId, String branchCode) {
        return jdbc.query("""
                SELECT v.ten, v.ma_nhom, th.ten AS thuong_hieu, th.anh_url AS logo, v.don_vi, i.so_luong,
                       COALESCE(NULLIF(i.bao_hanh_thang, 0), NULLIF(v.bao_hanh_thang, 0)) AS bao_hanh,
                       (gia.gia_ban->>:coSo)::numeric AS gia, i.hien_thi_khach, v.thong_so::text AS thong_so,
                       (SELECT a.duong_dan FROM dm_anh_vat_tu a
                        WHERE a.vat_tu_id = v.id_nguon AND a.dang_dung AND a.duong_dan IS NOT NULL
                        ORDER BY a.anh_chinh DESC NULLS LAST, a.id_nguon LIMIT 1) AS anh
                FROM dm_vat_tu_tron_goi i
                JOIN dm_vat_tu v ON v.id_nguon = i.vat_tu_id
                LEFT JOIN dm_thuong_hieu th ON th.id_nguon = v.thuong_hieu_id
                """ + CatalogMaterialRepository.CURRENT_PRICE + """
                WHERE i.tron_goi_id = :id AND i.dang_dung
                ORDER BY CASE v.ma_nhom WHEN 'TAM_PIN' THEN 1 WHEN 'BIEN_TAN' THEN 2 WHEN 'PIN_LUU_TRU' THEN 3
                         ELSE 4 END, i.id_nguon""",
                new MapSqlParameterSource("id", packageId).addValue("coSo", branchCode == null ? "" : branchCode),
                (rs, i) -> new Line(rs.getString("ten"), rs.getString("ma_nhom"), rs.getString("thuong_hieu"),
                        rs.getString("logo"), rs.getString("don_vi"), (Integer) rs.getObject("so_luong"),
                        (Integer) rs.getObject("bao_hanh"),
                        rs.getBigDecimal("gia") == null ? null : rs.getBigDecimal("gia").doubleValue(),
                        rs.getBoolean("hien_thi_khach"), json(rs.getString("thong_so")), rs.getString("anh")));
    }

    private JsonNode json(String s) {
        try {
            return s == null ? objectMapper.createObjectNode() : objectMapper.readTree(s);
        } catch (JsonProcessingException e) {
            return objectMapper.createObjectNode();
        }
    }

    // ------------------------------------------------------------------ tệp kèm

    Map<String, byte[]> staticFiles() {
        Map<String, byte[]> files = staticFiles;
        if (files == null) {
            files = new LinkedHashMap<>();
            for (String name : QuoteImageTemplate.STATIC_FILES) {
                try (InputStream in = new ClassPathResource("quote-image/" + name).getInputStream()) {
                    files.put(name, in.readAllBytes());
                } catch (IOException e) {
                    throw new UncheckedIOException("Thiếu tệp mẫu ảnh báo giá " + name, e);
                }
            }
            staticFiles = files;
        }
        return files;
    }

    /** Tải ảnh slm_v2 cho trang; lỗi thì bỏ ảnh đó (logo thay bằng tên hãng) */
    CatalogImageService.Image remote(String url) {
        Remote r = remote.get(url);
        if (r != null && r.at().plus(REMOTE_CACHE).isAfter(Instant.now())) {
            return r.image();
        }
        try {
            CatalogImageService.Image img = images.fetch(url);
            if (remote.size() >= REMOTE_CACHE_MAX) {
                remote.clear();
            }
            remote.put(url, new Remote(img, Instant.now()));
            return img;
        } catch (RuntimeException e) {
            log.info("Ảnh báo giá: không tải được {}: {}", url, e.getMessage());
            return null;
        }
    }

    /** Nơi nhận ảnh của trang: trả tên tệp trong trang, null khi không có ảnh */
    @FunctionalInterface
    interface AssetSink {
        String add(String url, String prefix);
    }

    /** Gom tệp ảnh của trang, đặt tên theo thứ tự thêm; cùng đường dẫn dùng chung một tệp */
    final class Assets implements AssetSink {
        private final Map<String, byte[]> files;
        private final Map<String, String> byUrl = new LinkedHashMap<>();

        Assets(Map<String, byte[]> files) {
            this.files = files;
        }

        /** Tên tệp trong trang, null khi không có đường dẫn hoặc tải lỗi */
        @Override
        public String add(String url, String prefix) {
            if (url == null || url.isBlank()) {
                return null;
            }
            if (byUrl.containsKey(url)) {
                return byUrl.get(url);
            }
            CatalogImageService.Image img = remote(url);
            String ext = img == null ? null : switch (img.contentType()) {
                case "image/png" -> "png";
                case "image/jpeg" -> "jpg";
                case "image/webp" -> "webp";
                default -> null;
            };
            String name = ext == null ? null : prefix + "-" + (byUrl.size() + 1) + "." + ext;
            if (name != null) {
                files.put(name, img.bytes());
            }
            byUrl.put(url, name);
            return name;
        }
    }
}
