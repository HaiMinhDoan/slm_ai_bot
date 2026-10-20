package vn.slmglobal.slmai.services.auth.entity;

/** Phải khớp CHECK của cột nguoi_dung.trang_thai. */
public enum UserStatus {
    ACTIVE,
    /** Tạm khóa, có lý do; mở lại được */
    LOCKED,
    /** Nghỉ việc / không dùng nữa */
    DISABLED
}
