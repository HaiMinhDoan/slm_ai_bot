package vn.slmglobal.slmai.services.catalog;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;

/** Dòng catalog đã chuẩn hóa, sẵn sàng ghi vào các bảng dm_* (mục 29, Catalog). {@code hash} là ma_bam. */
public final class CatalogRows {

    private CatalogRows() {
    }

    public record BrandRow(int id, String name, String nameIntl, String country, String description, boolean active,
                           String hash) {
    }

    public record BranchRow(int id, String code, String name, String address, String phone, boolean active,
                            String hash) {
    }

    public record MaterialRow(int id, String code, String name, String groupCode, Integer brandId, String unit,
                              Integer warrantyMonths, Boolean main, ObjectNode specs, String quoteDescription,
                              boolean active, List<String> warnings, String hash) {
    }

    public record PackageRow(int id, String branchCode, Integer familyId, String familyName, Integer panelBrandId,
                             Integer inverterBrandId, Integer batteryBrandId, String name, String systemType,
                             Integer phases, Double kwp, Integer panelCount, Double panelWp, Double inverterKw,
                             double storageKwh, Double outputMin, Double outputMax, Double totalPrice,
                             double steelFramePrice, Boolean bestSeller, Integer imageFileId, String description,
                             boolean active, boolean recommendable, List<String> warnings, String hash) {
    }

    public record ItemRow(int id, int packageId, int materialId, Integer quantity, Integer warrantyMonths,
                          Boolean hasWarranty, boolean customerVisible, boolean active, String hash) {
    }

    public record DomainRow(int id, String domain, String branchCode, boolean active, String hash) {
    }

    /**
     * @param warnings mã cảnh báo → id_nguon các dòng bị cảnh báo (dm_lich_su_dong_bo.canh_bao)
     * @param version  phiên bản catalog: hash của mọi ma_bam (luot_goi_ai.phien_ban_catalog)
     */
    public record Normalized(List<BrandRow> brands, List<BranchRow> branches, List<MaterialRow> materials,
                             List<PackageRow> packages, List<ItemRow> items, List<DomainRow> domains,
                             Map<String, List<Integer>> warnings, String version) {
    }
}
