package vn.slmglobal.slmai.services.sizing;

import vn.slmglobal.slmai.common.util.VietnameseTextUtil;
import vn.slmglobal.slmai.services.catalog.CatalogMaterialRepository.QuotableMaterial;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Chọn vật tư khớp từ khoá khách nói cho tool {@code tra_cuu_vat_tu}. So không dấu, tách chữ và số ("5kwh" → "5",
 * "kwh"; "5,12" = "5.12"); vật tư khớp nhiều từ nhất thắng. Số nguyên khớp phần nguyên ("pin 5 kWh" khớp 5.12 kWh).
 */
final class MaterialMatcher {

    /** Nhóm thiết bị chính (ma_nhom ở slm_v2) và nhóm gộp mọi phụ kiện còn lại */
    static final List<String> GROUPS = List.of("TAM_PIN", "BIEN_TAN", "PIN_LUU_TRU", "PHU_KIEN");
    static final Set<String> MAIN_GROUPS = Set.of("TAM_PIN", "BIEN_TAN", "PIN_LUU_TRU");
    static final String ACCESSORIES = "PHU_KIEN";

    /** Từ hỏi chung, không giúp phân biệt thiết bị */
    private static final Set<String> STOP_WORDS = Set.of("gia", "bao", "nhieu", "loai", "cua", "cho", "va", "thiet",
            "bi", "nao", "khong", "co", "la", "cac", "nhung", "hang", "rieng", "le", "mot", "cai", "ben", "em", "anh",
            "chi", "minh", "the", "nhu", "vay", "a", "nha");

    private MaterialMatcher() {
    }

    /**
     * @param group TAM_PIN / BIEN_TAN / PIN_LUU_TRU, PHU_KIEN (nhóm còn lại) hoặc null (mọi nhóm)
     * @return từ khoá rỗng: cả nhóm (không có nhóm thì rỗng); không vật tư nào khớp: rỗng
     */
    static List<QuotableMaterial> match(List<QuotableMaterial> all, String keyword, String group) {
        List<QuotableMaterial> pool = all.stream().filter(m -> inGroup(m, group)).toList();
        List<String> query = tokens(keyword).stream().filter(t -> !STOP_WORDS.contains(t)).distinct().toList();
        if (query.isEmpty()) {
            return group == null ? List.of() : pool;
        }
        int best = 0;
        List<QuotableMaterial> out = new ArrayList<>();
        for (QuotableMaterial m : pool) {
            Set<String> words = words(m);
            int score = (int) query.stream().filter(q -> matches(q, words)).count();
            if (score > best) {
                best = score;
                out.clear();
            }
            if (score > 0 && score == best) {
                out.add(m);
            }
        }
        return out;
    }

    private static boolean inGroup(QuotableMaterial m, String group) {
        if (group == null) {
            return true;
        }
        boolean main = m.groupCode() != null && MAIN_GROUPS.contains(m.groupCode());
        return ACCESSORIES.equals(group) ? !main : group.equals(m.groupCode());
    }

    /** Tên, nhóm, hãng; "Hy-Brid" có cả "hy", "brid" và "hybrid" */
    private static Set<String> words(QuotableMaterial m) {
        String text = String.join(" ", m.name() == null ? "" : m.name(), m.groupName() == null ? "" : m.groupName(),
                m.brand() == null ? "" : m.brand(), m.groupCode() == null ? "" : m.groupCode().replace('_', ' '));
        Set<String> out = new LinkedHashSet<>(tokens(text));
        out.addAll(tokens(text.replace("-", "")));
        return out;
    }

    private static boolean matches(String q, Set<String> words) {
        if (words.contains(q)) {
            return true;
        }
        Double n = number(q);
        if (n != null) {
            boolean whole = n == Math.floor(n);
            return words.stream().map(MaterialMatcher::number)
                    .anyMatch(w -> w != null && (w.equals(n) || whole && Math.floor(w) == n));
        }
        return q.length() >= 3 && words.stream().anyMatch(w -> w.startsWith(q));
    }

    static List<String> tokens(String text) {
        String s = VietnameseTextUtil.unaccent(text).replaceAll("(\\d),(\\d)", "$1.$2")
                .replaceAll("(\\d)([a-z])", "$1 $2").replaceAll("([a-z])(\\d)", "$1 $2");
        List<String> out = new ArrayList<>();
        for (String t : s.split("[^a-z0-9.]+")) {
            t = t.replaceAll("^\\.+|\\.+$", "");
            if (!t.isEmpty()) {
                out.add(t);
            }
        }
        return out;
    }

    private static Double number(String t) {
        return t.matches("\\d+(\\.\\d+)?") ? Double.valueOf(t) : null;
    }
}
