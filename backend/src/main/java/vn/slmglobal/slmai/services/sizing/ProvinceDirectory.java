package vn.slmglobal.slmai.services.sizing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import vn.slmglobal.slmai.common.util.VietnameseTextUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Danh mục 34 tỉnh, thành phố (data/tinh-thanh.json, mục 4.2.9, 60.3): quy đổi tỉnh khách nói, kể cả tên 63 tỉnh
 * cũ, tên viết tắt, không dấu, tên thành phố lớn, sang tỉnh mới → miền giá (BAC / NAM) → cơ sở giá (HN / HCM).
 *
 * <p>AI chỉ ghi tỉnh khách nói; backend quy đổi. Không nhận ra tỉnh thì trả rỗng, không đoán miền.
 */
@Component
public class ProvinceDirectory {

    static final String LOCATION = "data/tinh-thanh.json";

    /** Tiền tố hành chính bỏ đi trước khi so khớp: "Tỉnh Bình Dương", "TP. HCM", "thành phố Huế" */
    private static final Pattern ADMIN_PREFIX = Pattern.compile("^(tinh|thanh pho|tp|t p)\\s+");
    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");

    public enum Region {
        BAC,
        NAM
    }

    /**
     * @param formerProvinces tên các tỉnh cũ (trước 2025) đã sáp nhập vào tỉnh này
     */
    public record Province(String code, String name, Region region, String branchCode,
                           List<String> formerProvinces) {
    }

    /**
     * @param formerName tên tỉnh cũ khách nói (ví dụ "Bình Dương" → TP Hồ Chí Minh); null nếu khách nói tên mới
     */
    public record Resolution(Province province, String formerName) {
    }

    private final Map<String, Province> byCode;
    /** Thứ tự trong file: Bắc vào Nam */
    private final List<Province> ordered;
    private final Map<Region, String> regionNames = new HashMap<>();
    /** Tên đã chuẩn hóa (giữ khoảng trắng): khớp nguyên cụm và tìm trong địa chỉ */
    private final Map<String, Alias> aliases = new HashMap<>();
    /** Dạng viết liền của tên nhiều từ ("hochiminh", "binhduong"): chỉ khớp nguyên cụm */
    private final Map<String, Alias> compact = new HashMap<>();
    private final String source;

    private record Alias(Province province, String formerName) {
    }

    public ProvinceDirectory(ObjectMapper objectMapper) {
        JsonNode root;
        try (InputStream in = new ClassPathResource(LOCATION).getInputStream()) {
            root = objectMapper.readTree(in);
        } catch (IOException e) {
            throw new UncheckedIOException("Không đọc được " + LOCATION, e);
        }
        this.source = root.path("nguon").asText();
        Map<Region, String> branches = new HashMap<>();
        for (Region r : Region.values()) {
            JsonNode node = root.path("mien").path(r.name());
            branches.put(r, node.path("co_so").asText());
            regionNames.put(r, node.path("ten").asText(r.name()));
        }
        Map<String, Province> provinces = new LinkedHashMap<>();
        for (JsonNode t : root.path("tinh")) {
            Region region = Region.valueOf(t.path("mien").asText());
            List<String> former = new ArrayList<>();
            t.path("tinh_cu").forEach(n -> former.add(n.asText()));
            Province p = new Province(t.path("ma").asText(), t.path("ten").asText(), region, branches.get(region),
                    List.copyOf(former));
            if (provinces.put(p.code(), p) != null) {
                throw new IllegalStateException(LOCATION + ": trùng mã tỉnh " + p.code());
            }
            addAlias(p.name(), new Alias(p, null));
            addAlias(p.code().replace('_', ' '), new Alias(p, null));
            for (String f : former) {
                addAlias(f, new Alias(p, sameName(f, p.name()) ? null : f));
            }
            t.path("ten_khac").forEach(n -> addAlias(n.asText(), new Alias(p, null)));
        }
        this.byCode = Map.copyOf(provinces);
        this.ordered = List.copyOf(provinces.values());
    }

    /** Nguồn danh mục (Nghị quyết, cách chia miền) */
    public String source() {
        return source;
    }

    /** 34 tỉnh theo thứ tự từ Bắc vào Nam */
    public List<Province> all() {
        return ordered;
    }

    public Optional<Province> byCode(String code) {
        return Optional.ofNullable(code == null ? null : byCode.get(code));
    }

    /** "miền Bắc" / "miền Nam" */
    public String regionName(Region region) {
        return regionNames.get(region);
    }

    /**
     * Tìm tỉnh trong câu khách nói: khớp nguyên cụm trước, sau đó tìm tên tỉnh nằm trong một địa chỉ dài
     * ("xã A, huyện B, Hà Giang"). Địa chỉ Việt Nam viết từ nhỏ đến lớn nên tên đứng sau cùng được ưu tiên;
     * hai tỉnh khác nhau cùng vị trí thì coi là không rõ.
     */
    public Optional<Resolution> resolve(String text) {
        String key = normalize(text);
        if (key.isEmpty()) {
            return Optional.empty();
        }
        Alias exact = aliases.get(key);
        if (exact == null) {
            exact = compact.get(key.replace(" ", ""));
        }
        if (exact != null) {
            return Optional.of(new Resolution(exact.province(), exact.formerName()));
        }
        String padded = " " + key + " ";
        Alias best = null;
        int bestEnd = -1;
        int bestLength = -1;
        boolean ambiguous = false;
        for (Map.Entry<String, Alias> e : aliases.entrySet()) {
            String alias = e.getKey();
            int at = padded.lastIndexOf(" " + alias + " ");
            if (at < 0) {
                continue;
            }
            int end = at + alias.length();
            if (end > bestEnd || (end == bestEnd && alias.length() > bestLength)) {
                best = e.getValue();
                bestEnd = end;
                bestLength = alias.length();
                ambiguous = false;
            } else if (end == bestEnd && alias.length() == bestLength && best.province() != e.getValue().province()) {
                ambiguous = true;
            }
        }
        if (best == null || ambiguous) {
            return Optional.empty();
        }
        return Optional.of(new Resolution(best.province(), best.formerName()));
    }

    /** Bỏ dấu, chữ thường, bỏ dấu câu và tiền tố "tỉnh", "thành phố", "TP" */
    static String normalize(String text) {
        String s = NON_ALNUM.matcher(VietnameseTextUtil.unaccent(text)).replaceAll(" ").strip();
        String prev;
        do {
            prev = s;
            s = ADMIN_PREFIX.matcher(s).replaceFirst("").strip();
        } while (!s.equals(prev));
        return s;
    }

    private void addAlias(String raw, Alias alias) {
        String key = normalize(raw);
        if (key.isEmpty()) {
            return;
        }
        put(aliases, key, alias);
        if (key.indexOf(' ') >= 0) {
            put(compact, key.replace(" ", ""), alias);
        }
    }

    private static void put(Map<String, Alias> map, String key, Alias alias) {
        Alias existing = map.get(key);
        if (existing != null && existing.province() != alias.province()) {
            throw new IllegalStateException(LOCATION + ": tên \"" + key + "\" thuộc hai tỉnh "
                    + existing.province().code() + " và " + alias.province().code());
        }
        if (existing == null || (existing.formerName() != null && alias.formerName() == null)) {
            map.put(key, alias);
        }
    }

    private static boolean sameName(String a, String b) {
        return normalize(a).equals(normalize(b));
    }
}
