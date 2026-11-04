package vn.slmglobal.slmai.services.catalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.slmglobal.slmai.adapters.slmv2.SlmV2Snapshot;
import vn.slmglobal.slmai.common.util.HashUtil;
import vn.slmglobal.slmai.common.util.VietnameseTextUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Chuẩn hóa dữ liệu thô của slm_v2 và gắn cảnh báo (mục 4.2.4). Hàm thuần, không đọc ghi CSDL.
 *
 * <ul>
 *   <li>Loại hệ: On-Grid / Ongrid → ON_GRID, Hy-Brid / Hybrid → HYBRID; số pha: "1 pha" → 1, "3 pha..." → 3.</li>
 *   <li>Công suất gói = cong_suat_he_thong (W) / 1000; ghi sai (dưới 1000 W) thì tính lại bằng số tấm × công suất tấm.</li>
 *   <li>Số tấm, công suất inverter, dung lượng lưu trữ cộng từ thành phần đang dùng theo nhóm vật tư.</li>
 *   <li>Thông số du_lieu_rieng: {@code {ten, donVi, giaTri}} → {@code {ten, value, unit}}; "abc", "", 0 → null.</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class CatalogNormalizer {

    static final String GROUP_PANEL = "TAM_PIN";
    static final String GROUP_INVERTER = "BIEN_TAN";
    static final String GROUP_BATTERY = "PIN_LUU_TRU";
    static final double MIN_CAPACITY_W = 1000;
    static final double CAPACITY_MISMATCH_RATIO = 0.03;

    private static final Pattern PHASE_IN_NAME = Pattern.compile("(\\d)\\s*pha", Pattern.CASE_INSENSITIVE);
    private static final Set<String> PLACEHOLDERS = Set.of("", "abc", "0", "null", "-");

    private final ObjectMapper objectMapper;

    public CatalogRows.Normalized normalize(SlmV2Snapshot s) {
        Map<String, List<Integer>> warnings = new TreeMap<>();
        Map<Integer, String> branchCodes = s.branches().stream()
                .collect(Collectors.toMap(SlmV2Snapshot.Branch::id, SlmV2Snapshot.Branch::ma));
        Map<Integer, String> groupCodes = s.materialGroups().stream()
                .collect(Collectors.toMap(SlmV2Snapshot.MaterialGroup::id, SlmV2Snapshot.MaterialGroup::ma));
        Map<Integer, SlmV2Snapshot.PackageFamily> families = byId(s.packageFamilies(), SlmV2Snapshot.PackageFamily::id);
        Map<Integer, SlmV2Snapshot.Material> materials = byId(s.materials(), SlmV2Snapshot.Material::id);
        Map<Integer, SlmV2Snapshot.Package> packages = byId(s.packages(), SlmV2Snapshot.Package::id);
        Set<Integer> brandIds = s.brands().stream().map(SlmV2Snapshot.Brand::id).collect(Collectors.toSet());
        // Đường dẫn file ảnh (MinIO của slm_v2) theo tep_tin_id: ảnh gói, ảnh vật tư, logo hãng
        Map<Integer, String> files = new HashMap<>();
        s.imageFiles().forEach(f -> files.put(f.id(), f.duongDan()));

        List<CatalogRows.ProductLineRow> productLines = s.productLines().stream().map(l ->
                new CatalogRows.ProductLineRow(l.id(), l.ma(), l.ten(), l.sdtSale(), l.sdtTech(), active(l.trangThai()),
                        hash(l.ma(), l.ten(), l.sdtSale(), l.sdtTech(), active(l.trangThai())))).toList();
        List<CatalogRows.BrandRow> brands = s.brands().stream().map(b -> {
            String logo = b.tepTinId() == null ? null : files.get(b.tepTinId());
            return new CatalogRows.BrandRow(b.id(), b.ten(), b.tenQuocTe(), b.quocGia(), b.moTa(), b.tepTinId(), logo,
                    active(b.trangThai()),
                    hash(b.ten(), b.tenQuocTe(), b.quocGia(), b.moTa(), b.tepTinId(), logo, active(b.trangThai())));
        }).toList();
        List<CatalogRows.BranchRow> branches = s.branches().stream().map(b -> new CatalogRows.BranchRow(b.id(),
                b.ma(), b.ten(), b.dcVanPhong(), b.sdt(), active(b.trangThai()),
                hash(b.ma(), b.ten(), b.dcVanPhong(), b.sdt(), active(b.trangThai())))).toList();

        List<CatalogRows.MaterialGroupRow> materialGroups = s.materialGroups().stream().map(g -> {
            ObjectNode attributes = specs(g.thuocTinhRieng());
            return new CatalogRows.MaterialGroupRow(g.id(), g.ma(), g.ten(), g.nganhHangId(), attributes,
                    g.vatTuChinh(), active(g.trangThai()),
                    hash(g.ma(), g.ten(), g.nganhHangId(), attributes, g.vatTuChinh(), active(g.trangThai())));
        }).toList();

        // Lịch sử giá bán: chỉ giá của vật tư có trong catalog. Giá hiện hành của một miền = lần ghi gần nhất có giá
        // của miền đó (slm_v2 hay ghi lần mới chỉ có giá HN, giá HCM vẫn là lần trước)
        List<CatalogRows.PriceRow> prices = new ArrayList<>();
        Map<Integer, Set<String>> pricedBranches = new HashMap<>();
        for (SlmV2Snapshot.Price pr : s.prices()) {
            if (!materials.containsKey(pr.vatTuId())) {
                continue;
            }
            ObjectNode sale = salePrices(pr.giaBan());
            CatalogRows.PriceRow row = new CatalogRows.PriceRow(pr.id(), pr.vatTuId(), pr.taoLuc(), sale,
                    active(pr.trangThai()), hash(pr.vatTuId(), pr.taoLuc(), sale, active(pr.trangThai())));
            prices.add(row);
            if (row.active()) {
                sale.fieldNames().forEachRemaining(b ->
                        pricedBranches.computeIfAbsent(pr.vatTuId(), k -> new HashSet<>()).add(b));
            }
        }

        List<CatalogRows.MaterialImageRow> materialImages = new ArrayList<>();
        for (SlmV2Snapshot.MaterialImage im : s.materialImages()) {
            if (!materials.containsKey(im.vatTuId())) {
                continue;
            }
            String url = im.tepTinId() == null ? null : files.get(im.tepTinId());
            materialImages.add(new CatalogRows.MaterialImageRow(im.id(), im.vatTuId(), im.tepTinId(), url,
                    im.anhChinh(), active(im.trangThai()),
                    hash(im.vatTuId(), im.tepTinId(), url, im.anhChinh(), active(im.trangThai()))));
        }

        // Thành phần đang dùng theo gói; thành phần trỏ tới vật tư / gói không có thì bỏ
        Map<Integer, List<SlmV2Snapshot.PackageItem>> itemsByPackage = new HashMap<>();
        List<CatalogRows.ItemRow> items = new ArrayList<>();
        Set<Integer> visibleMaterials = new HashSet<>();
        // Vật tư nằm trong gói đang bán mà chưa có giá bán của miền của gói
        Set<Integer> missingPrice = new HashSet<>();
        for (SlmV2Snapshot.PackageItem it : s.packageItems()) {
            if (!packages.containsKey(it.tronGoiId())) {
                continue;
            }
            if (!materials.containsKey(it.vatTuId())) {
                warn(warnings, CatalogWarning.UNKNOWN_MATERIAL, it.tronGoiId());
                continue;
            }
            boolean visible = Boolean.TRUE.equals(it.duocXem());
            boolean itemActive = active(it.trangThai());
            String description = it.moTa() == null || it.moTa().isBlank() ? null : it.moTa().strip();
            items.add(new CatalogRows.ItemRow(it.id(), it.tronGoiId(), it.vatTuId(), it.soLuong(),
                    it.thoiGianBaoHanh(), it.duocBaoHanh(), visible, it.gia(), description, itemActive,
                    hash(it.tronGoiId(), it.vatTuId(), it.soLuong(), it.thoiGianBaoHanh(), it.duocBaoHanh(), visible,
                            it.gia(), description, itemActive)));
            SlmV2Snapshot.Package pkg = packages.get(it.tronGoiId());
            String pkgBranch = pkg.coSoId() == null ? null : branchCodes.get(pkg.coSoId());
            if (itemActive && active(pkg.trangThai()) && pkgBranch != null
                    && !pricedBranches.getOrDefault(it.vatTuId(), Set.of()).contains(pkgBranch)) {
                missingPrice.add(it.vatTuId());
            }
            if (itemActive) {
                itemsByPackage.computeIfAbsent(it.tronGoiId(), k -> new ArrayList<>()).add(it);
                if (visible && active(packages.get(it.tronGoiId()).trangThai())) {
                    visibleMaterials.add(it.vatTuId());
                }
            }
        }

        Map<Integer, ObjectNode> specsById = new HashMap<>();
        List<CatalogRows.MaterialRow> materialRows = new ArrayList<>();
        for (SlmV2Snapshot.Material m : s.materials()) {
            ObjectNode specs = specs(m.duLieuRieng());
            specsById.put(m.id(), specs);
            String group = m.nhomVatTuId() == null ? null : groupCodes.get(m.nhomVatTuId());
            List<String> w = new ArrayList<>();
            String keySpec = GROUP_BATTERY.equals(group) ? "dung_luong"
                    : GROUP_PANEL.equals(group) || GROUP_INVERTER.equals(group) ? "cong_suat" : null;
            if (keySpec != null && active(m.trangThai()) && visibleMaterials.contains(m.id())
                    && number(specs, keySpec) == null) {
                w.add(CatalogWarning.SPEC_PLACEHOLDER.name());
                warn(warnings, CatalogWarning.SPEC_PLACEHOLDER, m.id());
            }
            if (active(m.trangThai()) && missingPrice.contains(m.id())) {
                w.add(CatalogWarning.MATERIAL_PRICE_MISSING.name());
                warn(warnings, CatalogWarning.MATERIAL_PRICE_MISSING, m.id());
            }
            Integer brand = m.thuongHieuId() != null && brandIds.contains(m.thuongHieuId()) ? m.thuongHieuId() : null;
            materialRows.add(new CatalogRows.MaterialRow(m.id(), m.ma(), m.ten() == null ? null : m.ten().strip(), group, brand, m.donVi(),
                    m.thoiGianBaoHanh(), m.vatTuChinh(), specs, m.moTaBaoGia(), active(m.trangThai()), List.copyOf(w),
                    hash(m.ma(), m.ten(), group, brand, m.donVi(), m.thoiGianBaoHanh(), m.vatTuChinh(), specs,
                            m.moTaBaoGia(), active(m.trangThai()), w)));
        }

        List<CatalogRows.PackageRow> packageRows = new ArrayList<>();
        for (SlmV2Snapshot.Package p : s.packages()) {
            packageRows.add(packageRow(p, branchCodes, families, brandIds, materials, groupCodes, specsById, files,
                    itemsByPackage.getOrDefault(p.id(), List.of()), warnings));
        }

        List<CatalogRows.PackageFamilyRow> packageFamilies = s.packageFamilies().stream().map(f -> {
            Integer panel = known(f.thuongHieuTamPin(), brandIds);
            Integer inverter = known(f.thuongHieuInverter(), brandIds);
            Integer battery = known(f.thuongHieuPinLuuTru(), brandIds);
            return new CatalogRows.PackageFamilyRow(f.id(), f.ten(), f.nganhHangId(), panel, inverter, battery,
                    active(f.trangThai()), hash(f.ten(), f.nganhHangId(), panel, inverter, battery,
                    active(f.trangThai())));
        }).toList();

        List<CatalogRows.DomainRow> domains = s.domains().stream().map(d -> {
            String domain = d.tenMien() == null ? "" : d.tenMien().strip().toLowerCase();
            String branch = d.coSoId() == null ? null : branchCodes.get(d.coSoId());
            return new CatalogRows.DomainRow(d.id(), domain, branch, active(d.trangThai()),
                    hash(domain, branch, active(d.trangThai())));
        }).toList();

        Set<Integer> domainIds = s.domains().stream().map(SlmV2Snapshot.Domain::id).collect(Collectors.toSet());
        List<CatalogRows.DomainContactRow> domainContacts = s.domainContacts().stream()
                .filter(c -> c.mienId() != null && domainIds.contains(c.mienId()))
                .map(c -> {
                    String phone = blankToNull(c.sdt());
                    String email = blankToNull(c.email());
                    return new CatalogRows.DomainContactRow(c.id(), c.mienId(), phone, email, active(c.trangThai()),
                            hash(c.mienId(), phone, email, active(c.trangThai())));
                }).toList();

        List<String> parts = new ArrayList<>();
        productLines.forEach(r -> parts.add("n" + r.id() + r.hash()));
        brands.forEach(r -> parts.add("b" + r.id() + r.hash()));
        materialGroups.forEach(r -> parts.add("nv" + r.id() + r.hash()));
        prices.forEach(r -> parts.add("p" + r.id() + r.hash()));
        materialImages.forEach(r -> parts.add("a" + r.id() + r.hash()));
        packageFamilies.forEach(r -> parts.add("ng" + r.id() + r.hash()));
        domainContacts.forEach(r -> parts.add("l" + r.id() + r.hash()));
        branches.forEach(r -> parts.add("c" + r.id() + r.hash()));
        materialRows.forEach(r -> parts.add("v" + r.id() + r.hash()));
        packageRows.forEach(r -> parts.add("g" + r.id() + r.hash()));
        items.forEach(r -> parts.add("i" + r.id() + r.hash()));
        domains.forEach(r -> parts.add("m" + r.id() + r.hash()));
        parts.sort(Comparator.naturalOrder());
        warnings.replaceAll((k, v) -> v.stream().distinct().sorted().toList());
        return new CatalogRows.Normalized(productLines, brands, branches, materialGroups, materialRows, prices,
                materialImages, packageFamilies, packageRows, items, domains, domainContacts, warnings,
                HashUtil.sha256Hex(String.join("|", parts)));
    }

    private CatalogRows.PackageRow packageRow(SlmV2Snapshot.Package raw, Map<Integer, String> branchCodes,
                                              Map<Integer, SlmV2Snapshot.PackageFamily> families,
                                              Set<Integer> brandIds,
                                              Map<Integer, SlmV2Snapshot.Material> materials,
                                              Map<Integer, String> groupCodes, Map<Integer, ObjectNode> specs,
                                              Map<Integer, String> files, List<SlmV2Snapshot.PackageItem> items,
                                              Map<String, List<Integer>> warnings) {
        SlmV2Snapshot.Package p = trimmed(raw);
        boolean active = active(p.trangThai());
        EnumSet<CatalogWarning> w = EnumSet.noneOf(CatalogWarning.class);

        String branch = p.coSoId() == null ? null : branchCodes.get(p.coSoId());
        if (branch == null) {
            w.add(CatalogWarning.UNKNOWN_BRANCH);
            branch = "?";
        }
        String systemType = systemType(p.loaiHeThong());
        if (systemType == null) {
            w.add(CatalogWarning.UNKNOWN_SYSTEM_TYPE);
        }
        Integer phases = phases(p.loaiPha());
        if (phases == null) {
            w.add(CatalogWarning.UNKNOWN_PHASE);
        } else if (namePhaseMismatch(p.ten(), phases)) {
            w.add(CatalogWarning.NAME_PHASE_MISMATCH);
        }

        int panelCount = 0;
        double panelW = 0;
        Double panelWp = null;
        int panelWpQty = -1;
        double inverterKw = 0;
        double storageKwh = 0;
        for (SlmV2Snapshot.PackageItem it : items) {
            SlmV2Snapshot.Material mat = materials.get(it.vatTuId());
            int qty = it.soLuong() == null ? 0 : it.soLuong();
            String group = mat.nhomVatTuId() == null ? null : groupCodes.get(mat.nhomVatTuId());
            if (!active(mat.trangThai())) {
                w.add(Boolean.TRUE.equals(it.duocXem()) ? CatalogWarning.INACTIVE_MATERIAL_IN_PACKAGE
                        : CatalogWarning.INACTIVE_ACCESSORY_IN_PACKAGE);
            }
            ObjectNode sp = specs.get(mat.id());
            if (GROUP_PANEL.equals(group)) {
                Double wp = number(sp, "cong_suat");
                panelCount += qty;
                if (wp != null && wp > 0) {
                    panelW += qty * wp;
                    if (qty > panelWpQty) {
                        panelWp = wp;
                        panelWpQty = qty;
                    }
                }
            } else if (GROUP_INVERTER.equals(group)) {
                Double kw = number(sp, "cong_suat");
                if (kw != null && kw > 0) {
                    inverterKw += qty * kw;
                }
            } else if (GROUP_BATTERY.equals(group)) {
                Double kwh = number(sp, "dung_luong");
                if (kwh != null && kwh > 0) {
                    storageKwh += qty * kwh;
                }
            }
        }

        Double kwp;
        double panelKwp = panelW / 1000.0;
        Double declared = p.congSuatHeThong();
        if (declared == null || declared < MIN_CAPACITY_W) {
            if (panelKwp > 0) {
                kwp = panelKwp;
                w.add(CatalogWarning.CAPACITY_FROM_PANELS);
            } else {
                kwp = null;
                w.add(CatalogWarning.CAPACITY_SUSPICIOUS);
            }
        } else {
            kwp = declared / 1000.0;
            if (panelKwp > 0 && Math.abs(kwp - panelKwp) / panelKwp > CAPACITY_MISMATCH_RATIO) {
                w.add(CatalogWarning.CAPACITY_MISMATCH);
            }
        }
        if (kwp != null) {
            kwp = Math.round(kwp * 1000) / 1000.0;
        }
        if (p.tongGia() == null || p.tongGia() <= 0) {
            w.add(CatalogWarning.MISSING_PRICE);
        }
        if ("HYBRID".equals(systemType) && storageKwh <= 0) {
            w.add(CatalogWarning.MISSING_BATTERY);
        }
        if (p.tepTinId() == null) {
            w.add(CatalogWarning.MISSING_IMAGE);
        }

        SlmV2Snapshot.PackageFamily f = p.nhomTronGoiId() == null ? null : families.get(p.nhomTronGoiId());
        Integer panelBrand = f == null ? null : known(f.thuongHieuTamPin(), brandIds);
        Integer inverterBrand = f == null ? null : known(f.thuongHieuInverter(), brandIds);
        Integer batteryBrand = f == null ? null : known(f.thuongHieuPinLuuTru(), brandIds);
        boolean recommendable = active && w.stream().noneMatch(CatalogWarning::blocking);
        if (active) {
            w.forEach(code -> warn(warnings, code, p.id()));
        }
        List<String> codes = w.stream().map(Enum::name).toList();
        double storage = Math.round(storageKwh * 100) / 100.0;
        Double inverter = inverterKw > 0 ? Math.round(inverterKw * 100) / 100.0 : null;
        double steel = p.giaKhungSat() == null ? 0 : p.giaKhungSat();
        String imageUrl = p.tepTinId() == null ? null : files.get(p.tepTinId());
        return new CatalogRows.PackageRow(p.id(), branch, p.nhomTronGoiId(), f == null ? null : f.ten(), panelBrand,
                inverterBrand, batteryBrand, p.ten(), systemType, phases, kwp, panelCount == 0 ? null : panelCount,
                panelWp, inverter, storage, p.sanLuongToiThieu(), p.sanLuongToiDa(), p.tongGia(), steel, p.banChay(),
                p.tepTinId(), imageUrl, p.moTa(), active, recommendable, codes,
                hash(branch, p.nhomTronGoiId(), f == null ? null : f.ten(), panelBrand, inverterBrand, batteryBrand,
                        p.ten(), systemType, phases, kwp, panelCount, panelWp, inverter, storage, p.sanLuongToiThieu(),
                        p.sanLuongToiDa(), p.tongGia(), steel, p.banChay(), p.tepTinId(), imageUrl, p.moTa(), active,
                        recommendable, codes));
    }

    /** Tên gói ghi số pha khác số pha của gói, ví dụ tên "... 3 pha" mà loai_pha là 1 pha */
    static boolean namePhaseMismatch(String name, Integer phases) {
        if (name == null || phases == null) {
            return false;
        }
        Matcher m = PHASE_IN_NAME.matcher(name);
        return m.find() && Integer.parseInt(m.group(1)) != phases;
    }

    /** slm_v2 có tên gói thừa khoảng trắng ở cuối */
    private static SlmV2Snapshot.Package trimmed(SlmV2Snapshot.Package p) {
        return new SlmV2Snapshot.Package(p.id(), p.coSoId(), p.nhomTronGoiId(), p.ten() == null ? null : p.ten().strip(),
                p.tepTinId(), p.loaiHeThong(), p.loaiPha(), p.congSuatHeThong(), p.sanLuongToiThieu(),
                p.sanLuongToiDa(), p.moTa(), p.tongGia(), p.giaKhungSat(), p.banChay(), p.trangThai());
    }

    /** "On-Grid", "Ongrid" → ON_GRID; "Hy-Brid", "Hybrid" → HYBRID */
    static String systemType(String raw) {
        String s = VietnameseTextUtil.unaccent(raw).replaceAll("[^a-z]", "");
        return switch (s) {
            case "ongrid", "hoaluoi" -> "ON_GRID";
            case "hybrid" -> "HYBRID";
            default -> null;
        };
    }

    /** "1 pha" → 1; "3 pha", "3 pha áp cao" → 3 */
    static Integer phases(String raw) {
        String s = VietnameseTextUtil.unaccent(raw);
        if (s.startsWith("1 pha") || s.equals("1")) {
            return 1;
        }
        if (s.startsWith("3 pha") || s.equals("3")) {
            return 3;
        }
        return null;
    }

    /**
     * ds_gia đã bỏ giaNhap ([{"maCoSo": "HN", "giaBan": 27000000}]) → {"HN": 27000000}. Một lần ghi có thể lặp cùng
     * cơ sở (đã gặp ở slm_v2, hai giá giống nhau): lấy giá sau cùng; bỏ giá trống.
     */
    ObjectNode salePrices(String json) {
        ObjectNode out = objectMapper.createObjectNode();
        if (json == null || json.isBlank()) {
            return out;
        }
        try {
            for (JsonNode e : objectMapper.readTree(json)) {
                String branch = e.path("maCoSo").asText("").strip();
                JsonNode v = e.get("giaBan");
                boolean number = v != null && (v.isNumber() || v.asText().strip().matches("\\d+(\\.\\d+)?"));
                if (!branch.isEmpty() && number) {
                    out.put(branch, Math.round(v.asDouble()));
                }
            }
        } catch (JsonProcessingException e) {
            return out;
        }
        return out;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.strip();
    }

    /** du_lieu_rieng → {khoá: {ten, value, unit}}; slm_v2 có bản ghi viết hoa khoá (Ten, DonVi, GiaTri) */
    ObjectNode specs(String json) {
        ObjectNode out = objectMapper.createObjectNode();
        if (json == null || json.isBlank()) {
            return out;
        }
        JsonNode root;
        try {
            root = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            return out;
        }
        if (!root.isObject()) {
            return out;
        }
        new TreeMap<>(toMap(root)).forEach((key, node) -> {
            ObjectNode spec = out.putObject(key);
            spec.put("ten", text(node, "ten", "Ten"));
            spec.put("unit", text(node, "donVi", "DonVi"));
            JsonNode v = node.has("giaTri") ? node.get("giaTri") : node.get("GiaTri");
            putValue(spec, v);
        });
        return out;
    }

    /** Giá trị số của thông số (null nếu trống / giữ chỗ / không phải số) */
    static Double number(ObjectNode specs, String key) {
        if (specs == null) {
            return null;
        }
        JsonNode v = specs.path(key).path("value");
        return v.isNumber() ? v.asDouble() : null;
    }

    private static void putValue(ObjectNode spec, JsonNode v) {
        if (v == null || v.isNull()) {
            spec.putNull("value");
        } else if (v.isNumber()) {
            if (v.asDouble() == 0) {
                spec.putNull("value");
            } else {
                spec.put("value", v.decimalValue().stripTrailingZeros());
            }
        } else {
            String s = v.asText().strip();
            if (PLACEHOLDERS.contains(s.toLowerCase())) {
                spec.putNull("value");
                return;
            }
            String numeric = s.replace(',', '.');
            if (numeric.matches("\\d+(\\.\\d+)?")) {
                double d = Double.parseDouble(numeric);
                if (d == 0) {
                    spec.putNull("value");
                } else {
                    spec.put("value", new java.math.BigDecimal(numeric).stripTrailingZeros());
                }
            } else {
                spec.put("value", s);
            }
        }
    }

    private static Map<String, JsonNode> toMap(JsonNode root) {
        Map<String, JsonNode> m = new HashMap<>();
        root.fields().forEachRemaining(e -> m.put(e.getKey(), e.getValue()));
        return m;
    }

    private static String text(JsonNode node, String key, String altKey) {
        JsonNode v = node.has(key) ? node.get(key) : node.get(altKey);
        return v == null || v.isNull() ? null : v.asText().strip();
    }

    private static Integer known(Integer id, Set<Integer> ids) {
        return id != null && ids.contains(id) ? id : null;
    }

    private static boolean active(Integer trangThai) {
        return trangThai != null && trangThai == 1;
    }

    private static void warn(Map<String, List<Integer>> warnings, CatalogWarning w, int id) {
        warnings.computeIfAbsent(w.name(), k -> new ArrayList<>()).add(id);
    }

    private static <T> Map<Integer, T> byId(List<T> list, Function<T, Integer> id) {
        Map<Integer, T> m = new HashMap<>();
        list.forEach(x -> m.put(id.apply(x), x));
        return m;
    }

    private static String hash(Object... parts) {
        StringBuilder sb = new StringBuilder();
        for (Object o : parts) {
            sb.append(Objects.toString(o, "∅")).append('\u001f');
        }
        return HashUtil.sha256Hex(sb.toString());
    }
}
