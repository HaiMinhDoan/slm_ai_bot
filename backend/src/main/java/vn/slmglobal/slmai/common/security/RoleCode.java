package vn.slmglobal.slmai.common.security;

/**
 * Mã vai trò của người dùng nội bộ (nhân viên, quản trị viên).
 * Phải khớp dữ liệu seed của bảng {@code vai_tro} trong V2__tai_khoan_quan_tri.sql.
 */
public final class RoleCode {

    /** Dùng trong {@link RequireAuth#roles()}: chỉ cần đăng nhập, vai trò nào cũng được. */
    public static final String ALL = "ALL";

    /** Quản trị hệ thống: người dùng, vai trò, cấu hình, bật tùy chọn rủi ro cao (mục 54.6). */
    public static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";

    /** Nhân viên xử lý hội thoại: inbox, duyệt nháp, tiếp quản (mục 19). */
    public static final String STAFF = "STAFF";

    /** Sửa bản nháp kiến thức (mục 5.9). */
    public static final String KNOWLEDGE_EDITOR = "KNOWLEDGE_EDITOR";

    /** Phát hành, rollback kiến thức (mục 5.9). */
    public static final String KNOWLEDGE_PUBLISHER = "KNOWLEDGE_PUBLISHER";

    /** Trợ lý nội dung, duyệt và lên lịch bài đăng (mục 26). */
    public static final String CONTENT_ADMIN = "CONTENT_ADMIN";

    private RoleCode() {
    }
}
