package vn.slmglobal.slmai.adapters.slmv2;

import java.util.List;

/**
 * Dữ liệu thô đọc từ các view ai_v_* của slm_v2 trong một lần đồng bộ (một transaction REPEATABLE READ nên các view
 * nhất quán với nhau). Tên trường theo cột của view; chuẩn hóa ở CatalogNormalizer.
 */
public record SlmV2Snapshot(
        List<Brand> brands,
        List<Branch> branches,
        List<MaterialGroup> materialGroups,
        List<Material> materials,
        List<PackageFamily> packageFamilies,
        List<Package> packages,
        List<PackageItem> packageItems,
        List<Domain> domains
) {

    /** ai_v_thuong_hieu */
    public record Brand(int id, String ten, String tenQuocTe, String quocGia, String moTa, Integer trangThai) {
    }

    /** ai_v_co_so */
    public record Branch(int id, String ma, String ten, String dcVanPhong, String sdt, Integer trangThai) {
    }

    /** ai_v_nhom_vat_tu */
    public record MaterialGroup(int id, String ma, String ten, Integer trangThai) {
    }

    /** ai_v_vat_tu; {@code duLieuRieng} là chuỗi JSON thông số */
    public record Material(int id, String ma, String ten, Integer nhomVatTuId, Integer thuongHieuId, String donVi,
                           String moTaBaoGia, String duLieuRieng, Integer thoiGianBaoHanh, Boolean vatTuChinh,
                           Integer trangThai) {
    }

    /** ai_v_nhom_tron_goi */
    public record PackageFamily(int id, String ten, Integer thuongHieuTamPin, Integer thuongHieuInverter,
                                Integer thuongHieuPinLuuTru, Integer trangThai) {
    }

    /** ai_v_tron_goi */
    public record Package(int id, Integer coSoId, Integer nhomTronGoiId, String ten, Integer tepTinId,
                          String loaiHeThong, String loaiPha, Double congSuatHeThong, Double sanLuongToiThieu,
                          Double sanLuongToiDa, String moTa, Double tongGia, Double giaKhungSat, Boolean banChay,
                          Integer trangThai) {
    }

    /** ai_v_vat_tu_tron_goi */
    public record PackageItem(int id, int tronGoiId, int vatTuId, Integer soLuong, Integer thoiGianBaoHanh,
                              Boolean duocBaoHanh, Boolean duocXem, Integer trangThai) {
    }

    /** ai_v_mien: tên miền website theo cơ sở */
    public record Domain(int id, String tenMien, Integer coSoId, Integer trangThai) {
    }
}
