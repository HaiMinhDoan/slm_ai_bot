package vn.slmglobal.slmai.services.conversation;

import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.customer.entity.ChannelIdentity;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Gửi tin ra một kênh (mục 33: Policy → Channel Adapter → Send). Mỗi adapter kênh cài đặt một bean.
 * Kiểm tra cửa sổ nhắn tin và Policy đã làm ở tầng trên; ở đây chỉ gọi API của nền tảng.
 */
public interface ChannelSender {

    ChannelCode channel();

    /** Giới hạn độ dài một tin văn bản của kênh */
    int maxTextLength();

    /** Gửi tin văn bản; trả mã tin của nền tảng. Lỗi thì ném exception. */
    String sendText(ChannelIdentity recipient, String text);

    /**
     * Tệp gửi kèm (mục 6.4).
     *
     * @param key     mã ổn định theo nội dung (sha256): kênh dùng lại tệp đã tải lên thay vì tải lại
     * @param kind    {@code image} hoặc {@code video}
     * @param content đọc nội dung khi thật sự phải tải lên
     */
    record OutgoingFile(String key, String kind, String mimeType, String fileName, Supplier<byte[]> content) {
    }

    /** Kênh gửi được ảnh / video đính kèm */
    default boolean supportsFiles() {
        return false;
    }

    /** Gửi một tệp đính kèm; trả mã tin của nền tảng. Lỗi thì ném exception. */
    default String sendFile(ChannelIdentity recipient, OutgoingFile file) {
        throw new UnsupportedOperationException("Kênh " + channel() + " chưa gửi được tệp đính kèm");
    }

    /** Báo đã xem tin của khách. Không bắt buộc; lỗi bị bỏ qua. */
    default void markSeen(ChannelIdentity recipient) {
    }

    /** Bật "đang soạn tin" (Messenger tự tắt sau khoảng 20 giây). Không bắt buộc; lỗi bị bỏ qua. */
    default void typing(ChannelIdentity recipient) {
    }

    /** Tắt "đang soạn tin" khi lượt kết thúc mà không gửi gì. Không bắt buộc; lỗi bị bỏ qua. */
    default void stopTyping(ChannelIdentity recipient) {
    }

    /** Tên hiển thị của khách trên kênh, để AI chọn cách xưng hô. Không lấy được thì rỗng. */
    default Optional<String> fetchDisplayName(ChannelIdentity recipient) {
        return Optional.empty();
    }
}
