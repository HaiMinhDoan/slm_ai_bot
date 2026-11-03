package vn.slmglobal.slmai.services.sizing;

/**
 * Nguồn thông số báo giá hiện hành: rule {@code rules/bao-gia} của bản phát hành kiến thức đang chạy (mục 5.3).
 * Mỗi lần tính lấy lại để bản mới vừa kích hoạt có hiệu lực ngay.
 */
@FunctionalInterface
public interface PricingSource {

    PricingProperties pricing();
}
