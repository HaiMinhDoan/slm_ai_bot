package vn.slmglobal.slmai.services.catalog;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Dòng catalog đã chuẩn hóa, sẵn sàng ghi vào các bảng dm_* (mục 29, Catalog). {@code hash} là ma_bam. */
public final class CatalogRows {

    private CatalogRows() {
    }

    public record ProductLineRow(int id, String code, String name, String salesPhone, String techPhone,
                                 boolean active, String hash) {
    }

    /** @param logoUrl đường dẫn logo trên MinIO của slm_v2 */
    public record BrandRow(int id, String name, String nameIntl, String country, String description,
                           Integer logoFileId, String logoUrl, boolean active, String hash) {
    }

    public record BranchRow(int id, String code, String name, String address, String phone, boolean active,
                            String hash) {
    }

    /** @param attributes khuôn thông số của nhóm (thuoc_tinh_rieng) */
    public record MaterialGroupRow(int id, String code, String name, Integer productLineId, ObjectNode attributes,
                                   Boolean main, boolean active, String hash) {
    }

    public record MaterialRow(int id, String code, String name, String groupCode, Integer brandId, String unit,
                              Integer warrantyMonths, Boolean main, ObjectNode specs, String quoteDescription,
                              boolean active, List<String> warnings, String hash) {
    }

    /** @param salePrices mã cơ sở → giá bán ({"HN": 27000000}); không có giá nhập */
    public record PriceRow(int id, int materialId, LocalDateTime effectiveFrom, ObjectNode salePrices, boolean active,
                           String hash) {
    }

    public record MaterialImageRow(int id, int materialId, Integer fileId, String url, Boolean main, boolean active,
                                   String hash) {
    }

    public record PackageFamilyRow(int id, String name, Integer productLineId, Integer panelBrandId,
                                   Integer inverterBrandId, Integer batteryBrandId, boolean active, String hash) {
    }

    public record PackageRow(int id, String branchCode, Integer familyId, String familyName, Integer panelBrandId,
                             Integer inverterBrandId, Integer batteryBrandId, String name, String systemType,
                             Integer phases, Double kwp, Integer panelCount, Double panelWp, Double inverterKw,
                             double storageKwh, Double outputMin, Double outputMax, Double totalPrice,
                             double steelFramePrice, Boolean bestSeller, Integer imageFileId, String imageUrl,
                             String description, boolean active, boolean recommendable, List<String> warnings,
                             String hash) {
    }

    /** @param salePrice giá bán một đơn vị lúc dựng gói (vat_tu_tron_goi.gia) */
    public record ItemRow(int id, int packageId, int materialId, Integer quantity, Integer warrantyMonths,
                          Boolean hasWarranty, boolean customerVisible, Double salePrice, String description,
                          boolean active, String hash) {
    }

    public record DomainRow(int id, String domain, String branchCode, boolean active, String hash) {
    }

    public record DomainContactRow(int id, int domainId, String phone, String email, boolean active, String hash) {
    }

    /**
     * @param warnings mã cảnh báo → id_nguon các dòng bị cảnh báo (dm_lich_su_dong_bo.canh_bao)
     * @param version  phiên bản catalog: hash của mọi ma_bam (luot_goi_ai.phien_ban_catalog)
     */
    public record Normalized(List<ProductLineRow> productLines, List<BrandRow> brands, List<BranchRow> branches,
                             List<MaterialGroupRow> materialGroups, List<MaterialRow> materials,
                             List<PriceRow> prices, List<MaterialImageRow> materialImages,
                             List<PackageFamilyRow> packageFamilies, List<PackageRow> packages,
                             List<ItemRow> items, List<DomainRow> domains, List<DomainContactRow> domainContacts,
                             Map<String, List<Integer>> warnings, String version) {
    }
}
