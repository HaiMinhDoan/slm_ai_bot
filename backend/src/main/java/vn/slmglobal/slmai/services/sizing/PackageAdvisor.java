package vn.slmglobal.slmai.services.sizing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.slmglobal.slmai.services.catalog.CatalogRepository;
import vn.slmglobal.slmai.services.catalog.CatalogRepository.CatalogPackage;
import vn.slmglobal.slmai.services.lead.ConsultSlots;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Logic của tool {@code tim_goi_phu_hop} và {@code chi_tiet_goi} (mục 4.2.6, 58.2, 60.3). Mọi con số AI được nói
 * về giá, công suất, sản lượng, tiết kiệm đều do lớp này tính, kèm chuỗi hiển thị để AI chép nguyên văn.
 *
 * <ul>
 *   <li>Chưa có hoá đơn: trả giá khởi điểm theo loại hệ / số pha của miền khách (báo khoảng giá sớm), không trả
 *       dải giá của cả catalog.</li>
 *   <li>Có hoá đơn: tính công suất và pin cần (SizingService), chọn gói rẻ nhất đáp ứng; chưa biết tỉnh hoặc tỷ lệ
 *       ngày / đêm thì trả khoảng (gói nhỏ nhất và lớn nhất có thể).</li>
 *   <li>Sản lượng theo PV out EVN của tỉnh; link sản phẩm theo miền (Bắc slmsolar.vn, Nam slmsolar.com.vn).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class PackageAdvisor {

    static final String ON_GRID = "ON_GRID";
    static final String HYBRID = "HYBRID";
    /** Chưa biết miền: giá tham khảo theo cơ sở HN (giá hai cơ sở gần như bằng nhau), chưa gửi link */
    static final String DEFAULT_BRANCH = "HN";

    private final CatalogRepository catalog;
    private final ProvinceDirectory provinces;
    private final PvoutService pvout;
    private final SizingService sizing;
    private final PricingProperties pricing;
    private final ObjectMapper objectMapper;

    /** Thông tin khách đã có; trường chưa biết là null */
    public record AdviceRequest(String province, Integer phases, Long monthlyBillVnd, String dayNight,
                                String roofType, Double roofAreaM2, String systemType, Boolean backupNeeded) {
    }

    /**
     * @param quotedPackageIds  gói đề xuất cụ thể theo hoá đơn của khách (rỗng ở chế độ giá khởi điểm)
     * @param cheapestQuotedVnd giá thấp nhất trong các gói đề xuất (xét OFF_SEGMENT, mục 58.5)
     */
    public record Advice(ObjectNode result, List<Integer> quotedPackageIds, Long cheapestQuotedVnd) {
    }

    /** Tỉnh, miền, cơ sở giá và PV out của khách cho một lần gọi tool */
    private record Place(ProvinceDirectory.Resolution resolution, String branch, PvoutRepository.Table table,
                         double[] pvMonthly) {

        boolean known() {
            return resolution != null;
        }

        ProvinceDirectory.Province province() {
            return resolution == null ? null : resolution.province();
        }
    }

    private record Pick(CatalogPackage pkg, String note) {
    }

    public boolean available() {
        return catalog.hasRecommendablePackages();
    }

    public Advice advise(AdviceRequest r) {
        ObjectNode out = objectMapper.createObjectNode();
        List<String> notes = new ArrayList<>();
        Place place = place(r.province(), out, notes);
        List<CatalogPackage> all = catalog.recommendablePackages(place.branch());
        if (all.isEmpty()) {
            out.put("co_bang_gia", false);
            notes.add("Chưa có bảng giá: không đưa con số giá; mời khách để lại số điện thoại để nhân viên gửi báo giá.");
            out.set("ghi_chu", strings(notes));
            return new Advice(out, List.of(), null);
        }
        out.put("co_bang_gia", true);
        String preferred = normalizeType(r.systemType());
        if ("BANG".equals(r.roofType()) && pricing.flatRoofNote() != null) {
            notes.add(pricing.flatRoofNote());
        }

        List<Integer> quoted = new ArrayList<>();
        Long cheapest = null;
        boolean billKnown = r.monthlyBillVnd() != null && r.monthlyBillVnd() > 0;
        if (!billKnown || place.table() == null) {
            if (billKnown) {
                notes.add("Chưa có bảng PV out để tính công suất: tạm đưa giá khởi điểm.");
            }
            startingPrices(out, all, r.phases(), preferred, place);
            notes.add("Chưa biết tiền điện mỗi tháng: đây là giá khởi điểm của từng loại hệ. Hỏi tiền điện để chọn "
                    + "đúng gói.");
        } else {
            out.put("che_do", "DE_XUAT");
            SizingService.Sizing s = sizing.size(r.monthlyBillVnd(), r.dayNight(),
                    place.pvMonthly() == null ? null : PvoutRepository.Table.annual(place.pvMonthly()),
                    nationalRange(place.table()), r.roofAreaM2());
            out.set("uoc_tinh", estimate(s, place, r));
            if (s.requiredKwp().max() > pricing.largeSystemKwp()) {
                notes.add("Nhu cầu lớn (trên " + VnFormat.kwp(pricing.largeSystemKwp())
                        + "): nên để nhân viên khảo sát và báo giá riêng.");
            }
            ArrayNode options = out.putArray("goi_de_xuat");
            double minPrice = Double.MAX_VALUE;
            double maxPrice = 0;
            for (String type : types(preferred, r)) {
                List<Pick> picks = new ArrayList<>();
                if (s.requiredKwp().exact() && s.batteryKwh().exact()) {
                    picks.add(pick(all, r.phases(), type, s.requiredKwp().max(), s.batteryKwh().max(),
                            s.maxKwpByRoof()));
                } else {
                    picks.add(pick(all, r.phases(), type, s.requiredKwp().min(), s.batteryKwh().min(),
                            s.maxKwpByRoof()));
                    picks.add(pick(all, r.phases(), type, s.requiredKwp().max(), s.batteryKwh().max(),
                            s.maxKwpByRoof()));
                }
                for (Pick p : distinct(picks)) {
                    if (p.pkg() == null) {
                        continue;
                    }
                    ObjectNode o = summary(p.pkg(), place);
                    o.put("ly_do", reason(type, r, s));
                    if (p.note() != null) {
                        o.put("luu_y", p.note());
                    }
                    addProduction(o, p.pkg(), place, r, s);
                    options.add(o);
                    quoted.add(p.pkg().id());
                    double price = p.pkg().totalPrice();
                    minPrice = Math.min(minPrice, price);
                    maxPrice = Math.max(maxPrice, price);
                }
            }
            if (!quoted.isEmpty()) {
                cheapest = Math.round(minPrice);
                if (maxPrice > minPrice) {
                    ObjectNode range = out.putObject("khoang_gia");
                    range.put("tu", VnFormat.money(minPrice));
                    range.put("den", VnFormat.money(maxPrice));
                }
            } else {
                notes.add("Không có gói phù hợp số pha / loại hệ này: nhân viên sẽ tư vấn hệ riêng.");
            }
            if (r.phases() == null) {
                notes.add("Chưa biết điện 1 pha hay 3 pha: đang chọn gói 1 pha (đa số hộ gia đình), gói 3 pha khi "
                        + "nhu cầu vượt gói 1 pha lớn nhất.");
            }
        }
        out.set("con_thieu", missing(r, place));
        if (pricing.quoteConditions() != null) {
            out.put("dieu_kien_bao_gia", pricing.quoteConditions());
        }
        out.set("ghi_chu", strings(notes));
        return new Advice(out, List.copyOf(quoted), cheapest);
    }

    /** chi_tiet_goi: thành phần hiển thị cho khách, bảo hành, giá, link, sản lượng theo tỉnh (nếu biết) */
    public ObjectNode packageDetail(int packageId, String provinceText) {
        ObjectNode out = objectMapper.createObjectNode();
        List<String> notes = new ArrayList<>();
        Optional<CatalogPackage> found = catalog.findPackage(packageId);
        if (found.isEmpty()) {
            out.put("loi", "Không có gói mã " + packageId + "; dùng goi_id từ kết quả tim_goi_phu_hop.");
            return out;
        }
        Place place = place(provinceText, out, notes);
        CatalogPackage pkg = found.get();
        if (place.known() && !pkg.branchCode().equals(place.branch())) {
            Optional<CatalogPackage> twin = catalog.findTwin(pkg, place.branch());
            if (twin.isPresent()) {
                notes.add("Gói " + pkg.id() + " là bản giá " + regionOfBranch(pkg.branchCode())
                        + "; khách ở " + provinces.regionName(place.province().region()) + " nên dùng gói "
                        + twin.get().id() + ".");
                pkg = twin.get();
            }
        }
        if (!pkg.active() || !pkg.recommendable()) {
            out.put("goi_id", pkg.id());
            out.put("ten", pkg.name());
            out.put("dang_ban", false);
            notes.add("Gói này hiện không được báo giá (ngừng bán hoặc dữ liệu đang được rà lại): không đưa giá, "
                    + "đề xuất gói khác bằng tim_goi_phu_hop hoặc để nhân viên xác nhận.");
            out.set("ghi_chu", strings(notes));
            return out;
        }
        ObjectNode o = summary(pkg, place);
        o.put("dang_ban", true);
        ArrayNode items = o.putArray("thanh_phan");
        for (CatalogRepository.CatalogItem it : catalog.visibleItems(pkg.id())) {
            ObjectNode i = items.addObject();
            i.put("ten", it.name());
            if (it.brand() != null) {
                i.put("hang", it.brand());
            }
            if (it.quantity() != null) {
                i.put("so_luong", it.quantity() + (it.unit() == null ? "" : " " + it.unit().toLowerCase()));
            }
            if (it.warrantyMonths() != null && it.warrantyMonths() > 0) {
                i.put("bao_hanh", "bảo hành " + VnFormat.warranty(it.warrantyMonths()));
            }
        }
        if (pkg.steelFramePrice() > 0) {
            o.put("gia_khung_sat_mai_bang", VnFormat.money(pkg.steelFramePrice()));
        } else if (pricing.flatRoofNote() != null) {
            o.put("mai_bang", pricing.flatRoofNote());
        }
        addProduction(o, pkg, place, null, null);
        out.setAll(o);
        if (pricing.quoteConditions() != null) {
            out.put("dieu_kien_bao_gia", pricing.quoteConditions());
        }
        out.set("ghi_chu", strings(notes));
        return out;
    }

    // ------------------------------------------------------------------ chọn gói

    /**
     * Gói rẻ nhất có kWp ≥ dung sai × công suất cần (và vừa mái), Hybrid thì pin ≥ pin cần. Chưa biết số pha thì
     * chọn trong gói 1 pha trước, không có gói 1 pha đủ lớn mới sang 3 pha.
     */
    private Pick pick(List<CatalogPackage> all, Integer phases, String type, double needKwp, double needBattery,
                      Double roofCap) {
        if (phases != null) {
            return pickIn(pool(all, phases, type), type, needKwp, needBattery, roofCap);
        }
        Pick one = pickIn(pool(all, 1, type), type, needKwp, needBattery, roofCap);
        if (one.pkg() != null && one.note() == null) {
            return one;
        }
        Pick three = pickIn(pool(all, 3, type), type, needKwp, needBattery, roofCap);
        if (three.pkg() != null && three.note() == null) {
            return three;
        }
        return one.pkg() != null ? one : three;
    }

    private Pick pickIn(List<CatalogPackage> pool, String type, double needKwp, double needBattery,
                        Double roofCap) {
        if (pool.isEmpty()) {
            return new Pick(null, null);
        }
        List<CatalogPackage> fitsRoof = roofCap == null ? pool
                : pool.stream().filter(p -> p.kwp() <= roofCap + 1e-9).toList();
        if (fitsRoof.isEmpty()) {
            CatalogPackage smallest = pool.stream().min(Comparator.comparing(CatalogPackage::kwp)
                    .thenComparing(CatalogPackage::totalPrice)).orElseThrow();
            return new Pick(smallest, "Diện tích mái khách nói chỉ đủ khoảng " + VnFormat.kwp(roofCap)
                    + ", nhỏ hơn gói nhỏ nhất; nhân viên sẽ khảo sát mái.");
        }
        double min = needKwp * pricing.capacityTolerance();
        List<CatalogPackage> enough = fitsRoof.stream().filter(p -> p.kwp() >= min - 1e-9).toList();
        if (enough.isEmpty()) {
            CatalogPackage largest = fitsRoof.stream().max(Comparator.comparing(CatalogPackage::kwp)
                    .thenComparing(CatalogPackage::storageKwh)
                    .thenComparing(Comparator.comparing(CatalogPackage::totalPrice).reversed())).orElseThrow();
            boolean roofLimited = roofCap != null && pool.stream().anyMatch(p -> p.kwp() >= min - 1e-9);
            return new Pick(largest, roofLimited
                    ? "Mái chỉ đủ khoảng " + VnFormat.kwp(roofCap) + ": đây là gói lớn nhất vừa mái, chưa đủ nhu cầu "
                    + "khoảng " + VnFormat.kwp(needKwp) + "."
                    : "Nhu cầu khoảng " + VnFormat.kwp(needKwp) + " lớn hơn gói lớn nhất loại này; nhân viên sẽ tư "
                    + "vấn hệ riêng.");
        }
        Comparator<CatalogPackage> cheapest = Comparator.comparing(CatalogPackage::totalPrice)
                .thenComparing(CatalogPackage::kwp);
        if (!HYBRID.equals(type)) {
            return new Pick(enough.stream().min(cheapest).orElseThrow(), null);
        }
        Optional<CatalogPackage> withBattery = enough.stream()
                .filter(p -> p.storageKwh() >= needBattery - 1e-9).min(cheapest);
        if (withBattery.isPresent()) {
            return new Pick(withBattery.get(), null);
        }
        CatalogPackage biggestBattery = enough.stream().max(Comparator.comparing(CatalogPackage::storageKwh)
                .thenComparing(cheapest.reversed())).orElseThrow();
        return new Pick(biggestBattery, "Pin lớn nhất của các gói đủ công suất là "
                + VnFormat.kwh(biggestBattery.storageKwh()) + ", thấp hơn nhu cầu khoảng " + VnFormat.kwh(needBattery)
                + "; có thể bổ sung pin, nhân viên tư vấn thêm.");
    }

    private static List<CatalogPackage> pool(List<CatalogPackage> all, int phases, String type) {
        return all.stream().filter(p -> type.equals(p.systemType()) && p.phases() != null && p.phases() == phases
                && p.kwp() != null && p.totalPrice() != null).toList();
    }

    /**
     * Loại hệ đề xuất (mục 60.3 bước 5): khách chỉ định thì theo khách; dùng đêm từ 50% hoặc cần dự phòng mất điện
     * thì Hybrid; ban ngày nhiều thì On-Grid (rẻ hơn) kèm Hybrid pin nhỏ; chưa biết thì cả hai.
     */
    private List<String> types(String preferred, AdviceRequest r) {
        if (preferred != null) {
            return List.of(preferred);
        }
        Double share = r.dayNight() == null ? null : pricing.nightShare().get(r.dayNight());
        if (Boolean.TRUE.equals(r.backupNeeded()) || (share != null && share >= 0.5)) {
            return List.of(HYBRID);
        }
        return List.of(ON_GRID, HYBRID);
    }

    private String reason(String type, AdviceRequest r, SizingService.Sizing s) {
        if (ON_GRID.equals(type)) {
            return "Hệ hòa lưới, không có pin: rẻ hơn, hợp nhà dùng điện ban ngày nhiều; buổi tối và khi lưới mất "
                    + "điện thì không dùng được điện mặt trời.";
        }
        if (Boolean.TRUE.equals(r.backupNeeded())) {
            return "Hệ Hybrid có pin lưu trữ: dùng được buổi tối và có điện dự phòng khi lưới mất điện.";
        }
        return "Hệ Hybrid có pin lưu trữ " + (s.batteryKwh().exact() ? "(cần khoảng "
                + VnFormat.kwh(s.batteryKwh().max()) + ")" : "") + ": trữ điện ban ngày để dùng buổi tối.";
    }

    // ------------------------------------------------------------------ giá khởi điểm

    private void startingPrices(ObjectNode out, List<CatalogPackage> all, Integer phases, String preferred,
                                Place place) {
        out.put("che_do", "GIA_KHOI_DIEM");
        ArrayNode ref = out.putArray("gia_khoi_diem");
        List<String> types = preferred != null ? List.of(preferred) : List.of(ON_GRID, HYBRID);
        List<Integer> phaseList = phases != null ? List.of(phases) : List.of(1, 3);
        for (String type : types) {
            for (int ph : phaseList) {
                pool(all, ph, type).stream().min(Comparator.comparing(CatalogPackage::totalPrice)
                        .thenComparing(CatalogPackage::kwp)).ifPresent(p -> {
                    ObjectNode o = ref.addObject();
                    o.put("loai", typeLabel(type) + ", " + ph + " pha");
                    o.put("gia_tu", VnFormat.money(p.totalPrice()));
                    o.set("goi_re_nhat", summary(p, place));
                });
            }
        }
        ArrayNode best = out.putArray("goi_ban_chay");
        all.stream().filter(CatalogPackage::bestSeller)
                .filter(p -> phases == null || phases.equals(p.phases()))
                .filter(p -> preferred == null || preferred.equals(p.systemType()))
                .limit(3).forEach(p -> best.add(summary(p, place)));
    }

    // ------------------------------------------------------------------ dựng kết quả

    private Place place(String provinceText, ObjectNode out, List<String> notes) {
        PvoutRepository.Table table = pvout.current().orElse(null);
        ProvinceDirectory.Resolution res = provinceText == null || provinceText.isBlank() ? null
                : provinces.resolve(provinceText).orElse(null);
        ObjectNode area = out.putObject("khu_vuc");
        if (res == null) {
            if (provinceText != null && !provinceText.isBlank()) {
                area.put("khach_noi", provinceText);
                notes.add("Không nhận ra tỉnh \"" + provinceText + "\": hỏi lại khách tỉnh / thành phố.");
            }
            area.put("tinh", "chưa biết");
            area.put("bang_gia", "tạm theo bảng giá miền Bắc (giá hai miền gần như bằng nhau)");
            notes.add("Chưa biết tỉnh: chưa gửi link sản phẩm; sản lượng tính theo khoảng PV out cả nước.");
            return new Place(null, DEFAULT_BRANCH, table, null);
        }
        ProvinceDirectory.Province p = res.province();
        area.put("tinh", p.name());
        if (res.formerName() != null) {
            area.put("tinh_cu", res.formerName() + " (nay thuộc " + p.name() + ")");
        }
        area.put("mien", provinces.regionName(p.region()));
        area.put("bang_gia", "bảng giá " + provinces.regionName(p.region()));
        double[] months = table == null ? null : table.of(p.code()).orElse(null);
        return new Place(res, p.branchCode(), table, months);
    }

    private ObjectNode estimate(SizingService.Sizing s, Place place, AdviceRequest r) {
        ObjectNode e = objectMapper.createObjectNode();
        e.put("hoa_don_thang", VnFormat.money(r.monthlyBillVnd()));
        e.put("gia_dien_quy_doi", pricing.pricePerKwh() + " đ/kWh");
        e.put("dien_dung_ngay", "khoảng " + VnFormat.kwhRounded(s.dailyKwh()) + "/ngày");
        e.put("dien_dung_thang", "khoảng " + VnFormat.kwhRounded(s.monthlyKwh()) + "/tháng");
        e.put("ty_le_dung_dem", range(s.nightShare(), VnFormat::percent)
                + (s.nightShare().exact() ? "" : " (chưa biết dùng ngày / đêm)"));
        e.put("cong_suat_can", "khoảng " + range(s.requiredKwp(), VnFormat::kwp));
        e.put("pin_can_neu_dung_hybrid", "khoảng " + range(s.batteryKwh(), VnFormat::kwh));
        if (s.maxKwpByRoof() != null) {
            e.put("cong_suat_toi_da_theo_mai", "khoảng " + VnFormat.kwp(s.maxKwpByRoof()));
        }
        if (place.table() != null) {
            e.put("pvout", place.pvMonthly() != null
                    ? VnFormat.decimal(PvoutRepository.Table.annual(place.pvMonthly()), 0) + " kWh/kWp mỗi năm ("
                    + place.province().name() + ", bảng EVN năm " + place.table().year() + ")"
                    : "chưa biết tỉnh: tính theo khoảng PV out cả nước, bảng EVN năm " + place.table().year());
        }
        return e;
    }

    /** Sản lượng theo PV out tháng của tỉnh; tiết kiệm và hoàn vốn khi biết hoá đơn và tỷ lệ ngày / đêm */
    private void addProduction(ObjectNode o, CatalogPackage p, Place place, AdviceRequest r,
                               SizingService.Sizing s) {
        if (place.pvMonthly() == null) {
            return;
        }
        double[] prod = SizingService.monthlyProduction(p.kwp(), place.pvMonthly());
        int lo = 0;
        int hi = 0;
        double year = 0;
        for (int i = 0; i < prod.length; i++) {
            year += prod[i];
            lo = prod[i] < prod[lo] ? i : lo;
            hi = prod[i] > prod[hi] ? i : hi;
        }
        ObjectNode sl = o.putObject("san_luong_uoc_tinh");
        sl.put("trung_binh", "khoảng " + VnFormat.kwhRounded(year / 12) + "/tháng");
        sl.put("thang_thap_nhat", "khoảng " + VnFormat.kwhRounded(prod[lo]) + " (tháng " + (lo + 1) + ")");
        sl.put("thang_cao_nhat", "khoảng " + VnFormat.kwhRounded(prod[hi]) + " (tháng " + (hi + 1) + ")");
        sl.put("ca_nam", "khoảng " + VnFormat.kwhRounded(year));
        sl.put("cach_tinh", VnFormat.kwp(p.kwp()) + " × PV out EVN " + place.table().year() + " của "
                + place.province().name());
        if (r == null || s == null || !s.nightShare().exact()) {
            return;
        }
        double storage = HYBRID.equals(p.systemType()) ? p.storageKwh() : 0;
        double annual = sizing.annualSavings(p.kwp(), storage, place.pvMonthly(), s.dailyKwh(), s.nightShare().max());
        if (annual <= 0) {
            return;
        }
        ObjectNode tk = o.putObject("tiet_kiem_uoc_tinh");
        tk.put("moi_thang_trung_binh", "khoảng " + VnFormat.moneyApprox(annual / 12));
        tk.put("ca_nam", "khoảng " + VnFormat.moneyApprox(annual));
        tk.put("hoan_von", "khoảng " + VnFormat.decimal(p.totalPrice() / annual, 1) + " năm");
        tk.put("cach_tinh", "điện mặt trời tự dùng (ban ngày, và pin cho buổi tối nếu có) × "
                + pricing.pricePerKwh() + " đ/kWh; không tính bán điện dư lên lưới");
    }

    private ObjectNode summary(CatalogPackage p, Place place) {
        ObjectNode o = objectMapper.createObjectNode();
        o.put("goi_id", p.id());
        o.put("ten", p.name());
        o.put("loai_he_thong", typeLabel(p.systemType()));
        o.put("so_pha", p.phases() + " pha");
        o.put("cong_suat", VnFormat.kwp(p.kwp()));
        if (p.panelCount() != null) {
            o.put("tam_pin", p.panelCount() + " tấm" + (p.panelBrand() == null ? "" : " " + p.panelBrand())
                    + (p.panelWp() == null ? "" : " " + VnFormat.decimal(p.panelWp(), 0) + " Wp"));
        }
        if (p.inverterBrand() != null || p.inverterKw() != null) {
            o.put("inverter", (p.inverterBrand() == null ? "" : p.inverterBrand() + " ")
                    + (p.inverterKw() == null ? "" : VnFormat.decimal(p.inverterKw(), 2) + " kW"));
        }
        if (HYBRID.equals(p.systemType())) {
            o.put("luu_tru", VnFormat.kwh(p.storageKwh()));
            if (p.batteryBrand() != null) {
                o.put("hang_pin_luu_tru", p.batteryBrand());
            }
        }
        o.put("gia", VnFormat.money(p.totalPrice()));
        o.put("gia_day_du", VnFormat.moneyExact(p.totalPrice()));
        if (p.bestSeller()) {
            o.put("ban_chay", true);
        }
        link(p, place).ifPresent(l -> o.put("link", l));
        return o;
    }

    /** https://<tên miền theo miền khách>/products/<id gói>; chưa biết tỉnh thì không có link */
    private Optional<String> link(CatalogPackage p, Place place) {
        if (!place.known()) {
            return Optional.empty();
        }
        String preferred = pricing.productLinkDomains().get(place.province().region().name());
        List<String> synced = catalog.domains(p.branchCode());
        String domain = preferred != null && (synced.isEmpty() || synced.contains(preferred)) ? preferred
                : synced.isEmpty() ? null : synced.get(0);
        return domain == null ? Optional.empty() : Optional.of("https://" + domain + "/products/" + p.id());
    }

    private ArrayNode missing(AdviceRequest r, Place place) {
        ArrayNode a = objectMapper.createArrayNode();
        if (!place.known()) {
            a.add(ConsultSlots.KHU_VUC);
        }
        if (r.phases() == null) {
            a.add(ConsultSlots.SO_PHA);
        }
        if (r.monthlyBillVnd() == null || r.monthlyBillVnd() <= 0) {
            a.add(ConsultSlots.HOA_DON_THANG);
        }
        if (r.dayNight() == null) {
            a.add(ConsultSlots.TY_LE_NGAY_DEM);
        }
        if (r.roofType() == null) {
            a.add(ConsultSlots.LOAI_MAI);
        }
        return a;
    }

    private static SizingService.Range nationalRange(PvoutRepository.Table table) {
        double min = Double.MAX_VALUE;
        double max = 0;
        for (double[] m : table.monthly().values()) {
            double a = PvoutRepository.Table.annual(m);
            min = Math.min(min, a);
            max = Math.max(max, a);
        }
        return new SizingService.Range(min, max);
    }

    private static String range(SizingService.Range r, java.util.function.DoubleFunction<String> f) {
        if (r.exact()) {
            return f.apply(r.max());
        }
        return f.apply(r.min()) + " đến " + f.apply(r.max());
    }

    private static List<Pick> distinct(List<Pick> picks) {
        Map<Integer, Pick> m = new LinkedHashMap<>();
        for (Pick p : picks) {
            if (p.pkg() != null) {
                m.putIfAbsent(p.pkg().id(), p);
            }
        }
        return List.copyOf(m.values());
    }

    private String regionOfBranch(String branch) {
        for (ProvinceDirectory.Region r : ProvinceDirectory.Region.values()) {
            if (provinces.all().stream().anyMatch(p -> p.region() == r && p.branchCode().equals(branch))) {
                return provinces.regionName(r);
            }
        }
        return branch;
    }

    static String typeLabel(String type) {
        return HYBRID.equals(type) ? "Hybrid (có pin lưu trữ)" : "On-Grid (hòa lưới, không pin)";
    }

    static String normalizeType(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.toUpperCase().replaceAll("[^A-Z]", "");
        return switch (s) {
            case "ONGRID" -> ON_GRID;
            case "HYBRID" -> HYBRID;
            default -> null;
        };
    }

    private ArrayNode strings(List<String> values) {
        ArrayNode a = objectMapper.createArrayNode();
        values.stream().distinct().forEach(a::add);
        return a;
    }
}
