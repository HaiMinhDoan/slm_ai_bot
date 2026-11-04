package vn.slmglobal.slmai.adapters.slmv2;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dữ liệu thô đọc từ slm_v2 trong một lần đồng bộ (một transaction REPEATABLE READ nên các bảng / view nhất quán với
 * nhau). Tên trường theo cột nguồn; chuẩn hóa ở CatalogNormalizer. Không có giá nhập, biên lợi nhuận.
 */
public record SlmV2Snapshot(
        List<ProductLine> productLines,
        List<Brand> brands,
        List<Branch> branches,
        List<MaterialGroup> materialGroups,
        List<Material> materials,
        List<Price> prices,
        List<MaterialImage> materialImages,
        List<ImageFile> imageFiles,
        List<PackageFamily> packageFamilies,
        List<Package> packages,
        List<PackageItem> packageItems,
        List<Domain> domains,
        List<DomainContact> domainContacts
) {

    /** ai_v_nganh_hang */
    public record ProductLine(int id, String ma, String ten, String sdtSale, String sdtTech, Integer trangThai) {
    }

    /** ai_v_thuong_hieu; {@code tepTinId} là logo */
    public record Brand(int id, String ten, String tenQuocTe, String quocGia, String moTa, Integer tepTinId,
                        Integer trangThai) {
    }

    /** ai_v_co_so */
    public record Branch(int id, String ma, String ten, String dcVanPhong, String sdt, Integer trangThai) {
    }

    /** ai_v_nhom_vat_tu; {@code thuocTinhRieng} là chuỗi JSON khuôn thông số của nhóm */
    public record MaterialGroup(int id, String ma, String ten, Integer nganhHangId, String thuocTinhRieng,
                                Boolean vatTuChinh, Integer trangThai) {
    }

    /** ai_v_vat_tu; {@code duLieuRieng} là chuỗi JSON thông số */
    public record Material(int id, String ma, String ten, Integer nhomVatTuId, Integer thuongHieuId, String donVi,
                           String moTaBaoGia, String duLieuRieng, Integer thoiGianBaoHanh, Boolean vatTuChinh,
                           Integer trangThai) {
    }

    /**
     * Một lần ghi giá của vật tư (thong_tin_gia, lịch sử). {@code giaBan} là chuỗi JSON
     * {@code [{"maCoSo": "HN", "giaBan": 27000000}]}: câu đọc đã bỏ giaNhap.
     */
    public record Price(int id, int vatTuId, LocalDateTime taoLuc, String giaBan, Integer trangThai) {
    }

    /** ai_v_anh_vat_tu */
    public record MaterialImage(int id, int vatTuId, Integer tepTinId, Boolean anhChinh, Integer trangThai) {
    }

    /** ai_v_tep_tin_anh: file ảnh mà gói / vật tư / hãng dùng */
    public record ImageFile(int id, String duongDan) {
    }

    /** ai_v_nhom_tron_goi */
    public record PackageFamily(int id, String ten, Integer nganhHangId, Integer thuongHieuTamPin,
                                Integer thuongHieuInverter, Integer thuongHieuPinLuuTru, Integer trangThai) {
    }

    /** ai_v_tron_goi */
    public record Package(int id, Integer coSoId, Integer nhomTronGoiId, String ten, Integer tepTinId,
                          String loaiHeThong, String loaiPha, Double congSuatHeThong, Double sanLuongToiThieu,
                          Double sanLuongToiDa, String moTa, Double tongGia, Double giaKhungSat, Boolean banChay,
                          Integer trangThai) {
    }

    /** vat_tu_tron_goi (không có gm); {@code gia} là giá bán một đơn vị lúc dựng gói */
    public record PackageItem(int id, int tronGoiId, int vatTuId, Integer soLuong, Integer thoiGianBaoHanh,
                              Boolean duocBaoHanh, Boolean duocXem, Double gia, String moTa, Integer trangThai) {
    }

    /** ai_v_mien: tên miền website theo cơ sở */
    public record Domain(int id, String tenMien, Integer coSoId, Integer trangThai) {
    }

    /** thong_tin_ten_mien: hotline / email của một tên miền */
    public record DomainContact(int id, Integer mienId, String sdt, String email, Integer trangThai) {
    }
}
