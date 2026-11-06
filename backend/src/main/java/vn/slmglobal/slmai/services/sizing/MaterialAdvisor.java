package vn.slmglobal.slmai.services.sizing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.slmglobal.slmai.services.catalog.CatalogMaterialRepository;
import vn.slmglobal.slmai.services.catalog.CatalogMaterialRepository.QuotableMaterial;
import vn.slmglobal.slmai.services.catalog.CatalogRepository;
import vn.slmglobal.slmai.services.catalog.CatalogRepository.CatalogPackage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Logic của tool {@code tra_cuu_vat_tu}: thiết bị khách hỏi, giá niêm yết theo miền, thông số, bảo hành, link trang
 * thiết bị và các gói đang bán có thiết bị đó. Giá như website của slm_v2 (slmsolar.vn/devices/{id}): miền Nam chưa
 * có giá riêng thì lấy giá miền Bắc. Chỉ thiết bị chính (tấm pin, biến tần, pin lưu trữ) có giá lẻ; phụ kiện (khung,
 * dây, tủ điện, tiếp địa, nhân công) đã gồm trong giá trọn gói nên không báo giá lẻ.
 */
@Service
@RequiredArgsConstructor
public class MaterialAdvisor {

    /** Giá trị nhóm của tool */
    public static final List<String> GROUPS = MaterialMatcher.GROUPS;
    static final int MAX_RESULTS = 8;
    static final int MAX_PACKAGES = 3;
    /** Giá thiết bị chính dưới mức này là số giữ chỗ ở slm_v2 (đã gặp 1.000 đ): không báo */
    static final double MIN_DEVICE_PRICE = 100_000;
    static final String DEVICE_CONDITIONS = "Giá niêm yết của thiết bị mua lẻ, chưa gồm lắp đặt và vật tư phụ; "
            + "giá cuối nhân viên xác nhận.";
    private static final Set<String> PLACEHOLDERS = Set.of("", "0", "abc", "null", "-");

    private final CatalogMaterialRepository materials;
    private final CatalogRepository catalog;
    private final ProvinceDirectory provinces;
    private final PricingSource pricingSource;
    private final ObjectMapper objectMapper;

    /**
     * @param keyword      thiết bị khách hỏi (rỗng: liệt kê theo nhóm)
     * @param group        TAM_PIN / BIEN_TAN / PIN_LUU_TRU / PHU_KIEN hoặc null
     * @param provinceText tỉnh khách nói; chưa biết thì giá miền Bắc, không link
     */
    public ObjectNode lookup(String keyword, String group, String provinceText) {
        ObjectNode out = objectMapper.createObjectNode();
        List<String> notes = new ArrayList<>();
        ProvinceDirectory.Resolution place = provinceText == null || provinceText.isBlank() ? null
                : provinces.resolve(provinceText).orElse(null);
        String branch = place == null ? PackageAdvisor.DEFAULT_BRANCH : place.province().branchCode();
        String domain = null;
        if (place == null) {
            if (provinceText != null && !provinceText.isBlank()) {
                notes.add("Không nhận ra tỉnh \"" + provinceText + "\": hỏi lại khách tỉnh / thành phố.");
            }
            out.put("bang_gia", "tạm theo giá miền Bắc (giá hai miền gần như bằng nhau)");
            notes.add("Chưa biết tỉnh: chưa gửi link thiết bị.");
        } else {
            out.put("bang_gia", "giá " + provinces.regionName(place.province().region()));
            domain = PackageAdvisor.linkDomain(
                    pricingSource.pricing().productLinkDomains().get(place.province().region().name()),
                    catalog.domains(branch)).orElse(null);
        }

        String g = group != null && GROUPS.contains(group) ? group : null;
        List<QuotableMaterial> found = MaterialMatcher.match(materials.quotableMaterials(branch), keyword, g);
        List<QuotableMaterial> shown = found.subList(0, Math.min(MAX_RESULTS, found.size()));
        List<CatalogPackage> selling = catalog.recommendablePackages(branch);
        Map<Integer, List<Integer>> using = materials.packagesUsing(
                shown.stream().filter(QuotableMaterial::mainDevice).map(QuotableMaterial::id).toList(), branch);
        ArrayNode list = out.putArray("vat_tu");
        for (QuotableMaterial m : shown) {
            list.add(material(m, domain, selling, using.getOrDefault(m.id(), List.of())));
        }
        if (found.size() > shown.size()) {
            notes.add("Còn " + (found.size() - shown.size()) + " thiết bị khác khớp: hỏi khách cụ thể hơn (hãng, "
                    + "công suất / dung lượng, số pha) rồi tra lại.");
        }
        if (found.isEmpty()) {
            notes.add("Không có thiết bị đang bán khớp \"" + (keyword == null ? "" : keyword) + "\": không bịa tên, "
                    + "hãng hay giá; nói bên em chưa có thiết bị đó hoặc hỏi lại khách.");
        }
        out.put("dieu_kien_gia_le", DEVICE_CONDITIONS);
        notes.add("Không cộng giá lẻ các thiết bị để ra giá một hệ hay cấu hình tự chọn (thêm / bớt tấm, đổi pin, đổi "
                + "inverter): giá hệ chỉ lấy từ tim_goi_phu_hop / chi_tiet_goi; cấu hình khác gói có sẵn thì xin số "
                + "điện thoại để nhân viên báo giá.");
        ArrayNode n = out.putArray("ghi_chu");
        notes.forEach(n::add);
        return out;
    }

    private ObjectNode material(QuotableMaterial m, String domain, List<CatalogPackage> selling,
                                List<Integer> packageIds) {
        ObjectNode o = objectMapper.createObjectNode();
        o.put("vat_tu_id", m.id());
        o.put("ten", m.name());
        if (m.groupName() != null) {
            o.put("loai", m.groupName());
        }
        if (m.brand() != null) {
            o.put("hang", m.brand());
        }
        ObjectNode specs = specs(m.specs());
        if (!specs.isEmpty()) {
            o.set("thong_so", specs);
        }
        if (m.warrantyMonths() != null && m.warrantyMonths() > 0) {
            o.put("bao_hanh", "bảo hành " + VnFormat.warranty(m.warrantyMonths()));
        }
        if (!m.mainDevice()) {
            o.put("gia_niem_yet", "không báo giá lẻ: phụ kiện đã gồm trong giá trọn gói, mua riêng thì nhân viên "
                    + "báo giá");
            return o;
        }
        if (m.price() != null && m.price() >= MIN_DEVICE_PRICE) {
            o.put("gia_niem_yet", VnFormat.money(m.price()));
            o.put("gia_day_du", VnFormat.moneyExact(m.price()));
            if (domain != null) {
                o.put("link", "https://" + domain + "/devices/" + m.id());
            }
        } else {
            o.put("gia_niem_yet", "chưa có giá niêm yết: nhân viên báo giá");
        }
        List<CatalogPackage> in = selling.stream().filter(p -> packageIds.contains(p.id())).toList();
        if (!in.isEmpty()) {
            ArrayNode a = o.putArray("co_trong_goi");
            in.stream().limit(MAX_PACKAGES).forEach(p -> a.addObject()
                    .put("goi_id", p.id())
                    .put("ten", p.name())
                    .put("gia", VnFormat.money(p.totalPrice())));
            if (in.size() > MAX_PACKAGES) {
                o.put("so_goi_co_thiet_bi", in.size());
            }
        }
        return o;
    }

    /** {"dung_luong": {"ten": "Dung lượng", "unit": "kWh", "value": 5.12}} → {"Dung lượng": "5,12 kWh"} */
    private ObjectNode specs(JsonNode raw) {
        ObjectNode out = objectMapper.createObjectNode();
        if (raw == null || !raw.isObject()) {
            return out;
        }
        raw.fields().forEachRemaining(e -> {
            JsonNode v = e.getValue().path("value");
            String value = v.isNumber() ? VnFormat.decimal(v.asDouble(), 2) : v.isTextual() ? v.asText().strip() : "";
            if (PLACEHOLDERS.contains(value.toLowerCase())) {
                return;
            }
            String unit = e.getValue().path("unit").asText("").strip();
            String label = e.getValue().path("ten").asText(e.getKey()).strip();
            out.put(label.isEmpty() ? e.getKey() : label, unit.isEmpty() ? value : value + " " + unit);
        });
        return out;
    }
}
