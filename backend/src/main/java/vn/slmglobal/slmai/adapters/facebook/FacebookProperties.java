package vn.slmglobal.slmai.adapters.facebook;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cấu hình Meta App và Page, khối {@code app.facebook:} trong application.yml (mục 54.3).
 * Mọi giá trị đọc từ biến môi trường FB_*; token và secret không được ghi log.
 *
 * @param appId           App ID của Meta App: dùng để nhận ra echo do chính ứng dụng gửi
 * @param appSecret       App Secret: xác thực chữ ký X-Hub-Signature-256 của webhook
 * @param verifyToken     chuỗi tự đặt, nhập giống hệt ở phần cấu hình Webhooks của Meta App
 * @param pageId          Page ID; sự kiện của Page khác bị bỏ qua
 * @param pageAccessToken Page Access Token dài hạn (nên dùng token của System User)
 * @param graphApiVersion phiên bản Graph API, ví dụ v26.0
 * @param graphApiBaseUrl địa chỉ Graph API, đổi được khi test
 */
@ConfigurationProperties(prefix = "app.facebook")
public record FacebookProperties(
        String appId,
        String appSecret,
        String verifyToken,
        String pageId,
        String pageAccessToken,
        String graphApiVersion,
        String graphApiBaseUrl
) {

    public boolean hasAppSecret() {
        return appSecret != null && !appSecret.isBlank();
    }

    public boolean hasAppId() {
        return appId != null && !appId.isBlank();
    }

    public boolean hasPageAccessToken() {
        return pageAccessToken != null && !pageAccessToken.isBlank();
    }

    /** Sự kiện có thuộc Page đang cấu hình không. Chưa cấu hình pageId thì nhận mọi Page (môi trường dev). */
    public boolean isOwnPage(String id) {
        return pageId == null || pageId.isBlank() || pageId.equals(id);
    }
}
