package vn.slmglobal.slmai.services.lead;

import java.util.List;
import java.util.stream.Stream;

/**
 * Tên và giá trị hợp lệ của thông tin tư vấn (bản nháp rules/consult-slots.yaml, mục 58.3, 58.4, 58.7).
 * Khi Knowledge có bảng kt_* thì đọc từ tài liệu rules; tạm để hằng số để prompt, schema và backend khớp nhau.
 */
public final class ConsultSlots {

    public static final String KHU_VUC = "khu_vuc";
    public static final String SO_PHA = "so_pha";
    public static final String HOA_DON_THANG = "hoa_don_thang";
    public static final String TY_LE_NGAY_DEM = "ty_le_ngay_dem";
    public static final String LOAI_MAI = "loai_mai";
    public static final String DIEN_TICH_MAI = "dien_tich_mai";
    public static final String TEN_KHACH = "ten_khach";
    public static final String DANH_XUNG = "danh_xung";

    /** Thông tin tối thiểu để đưa giải pháp (mục 58.4) */
    public static final List<String> REQUIRED = List.of(KHU_VUC, SO_PHA, HOA_DON_THANG, TY_LE_NGAY_DEM, LOAI_MAI);

    /** Các khoá của {@code collected} trong kết quả AI */
    public static final List<String> COLLECTED_KEYS =
            List.of(KHU_VUC, SO_PHA, HOA_DON_THANG, TY_LE_NGAY_DEM, LOAI_MAI, DIEN_TICH_MAI, TEN_KHACH, DANH_XUNG);

    /** Câu hỏi về ba tín hiệu phân loại và liên hệ */
    public static final String Q_NGAN_SACH = "ngan_sach";
    public static final String Q_THOI_DIEM = "thoi_diem";
    public static final String Q_SO_DIEN_THOAI = "so_dien_thoai";
    public static final String Q_KHAC = "khac";

    public static final List<String> DAY_NIGHT_VALUES = List.of("NGAY_NHIEU", "CAN_BANG", "DEM_NHIEU");
    public static final List<String> ROOF_VALUES = List.of("TON", "NGOI", "BANG");
    public static final List<String> SALUTATION_VALUES = List.of("ANH", "CHI");
    public static final List<String> RESEARCH_LEVELS = List.of("CHUA", "SO_BO", "KY");
    public static final List<String> TIMELINES = List.of("TUAN_TOI", "THANG_TOI", "VAI_THANG", "NAM_TOI", "CHUA_RO");

    private ConsultSlots() {
    }

    public static List<String> questionKeys() {
        return Stream.concat(COLLECTED_KEYS.stream(), Stream.of(Q_NGAN_SACH, Q_THOI_DIEM, Q_SO_DIEN_THOAI, Q_KHAC))
                .toList();
    }
}
