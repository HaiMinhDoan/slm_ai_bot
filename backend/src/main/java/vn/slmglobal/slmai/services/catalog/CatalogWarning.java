package vn.slmglobal.slmai.services.catalog;

/**
 * Cảnh báo dữ liệu khi đồng bộ catalog (mục 4.2.4). Loại {@code blocking}: gói không được gợi ý (duoc_goi_y = false)
 * cho tới khi sửa ở slm_v2. Hiện trên trang admin và trong dm_lich_su_dong_bo.canh_bao.
 */
public enum CatalogWarning {
    /** cong_suat_he_thong < 1000 W và không tính được từ tấm pin */
    CAPACITY_SUSPICIOUS(true, "Công suất hệ không hợp lệ"),
    /** cong_suat_he_thong < 1000 W: dùng số tấm × công suất tấm thay thế (ví dụ gói 101 ghi 9) */
    CAPACITY_FROM_PANELS(false, "Công suất hệ ghi sai, đã tính lại theo số tấm pin"),
    /** cong_suat_he_thong lệch quá 3% so với số tấm × công suất tấm */
    CAPACITY_MISMATCH(false, "Công suất hệ lệch với số tấm pin"),
    UNKNOWN_SYSTEM_TYPE(true, "Không nhận ra loại hệ"),
    UNKNOWN_PHASE(true, "Không nhận ra số pha"),
    UNKNOWN_BRANCH(true, "Không nhận ra cơ sở giá"),
    MISSING_PRICE(true, "Chưa có giá gói"),
    /** Hệ Hybrid không có pin lưu trữ có dung lượng */
    MISSING_BATTERY(true, "Hệ Hybrid chưa có dung lượng pin"),
    /** Thành phần hiển thị cho khách (tấm pin, inverter, pin...) là vật tư đã ngừng */
    INACTIVE_MATERIAL_IN_PACKAGE(true, "Gói chứa thiết bị chính đã ngừng"),
    /** Phụ kiện không hiển thị cho khách là vật tư đã ngừng */
    INACTIVE_ACCESSORY_IN_PACKAGE(false, "Gói chứa phụ kiện đã ngừng"),
    /** Tên gói ghi "1 pha" nhưng loai_pha là 3 pha (hoặc ngược lại) */
    NAME_PHASE_MISMATCH(false, "Tên gói và số pha không khớp"),
    /** Thành phần gói trỏ tới vật tư không có trong view */
    UNKNOWN_MATERIAL(false, "Thành phần trỏ tới vật tư không tồn tại"),
    MISSING_IMAGE(false, "Gói chưa có ảnh"),
    /** Thông số chính (công suất, dung lượng) của thiết bị chính là giá trị giữ chỗ */
    SPEC_PLACEHOLDER(false, "Thông số thiết bị chính đang để trống / giữ chỗ");

    private final boolean blocking;
    private final String label;

    CatalogWarning(boolean blocking, String label) {
        this.blocking = blocking;
        this.label = label;
    }

    public boolean blocking() {
        return blocking;
    }

    public String label() {
        return label;
    }
}
