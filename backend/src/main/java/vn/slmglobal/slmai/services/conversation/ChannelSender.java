package vn.slmglobal.slmai.services.conversation;

import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.customer.entity.ChannelIdentity;

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

    /** Báo "đang soạn tin". Không bắt buộc; lỗi bị bỏ qua. */
    default void typing(ChannelIdentity recipient) {
    }
}
