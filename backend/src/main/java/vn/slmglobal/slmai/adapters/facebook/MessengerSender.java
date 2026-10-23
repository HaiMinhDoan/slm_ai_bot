package vn.slmglobal.slmai.adapters.facebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.slmglobal.slmai.services.conversation.ChannelSender;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.customer.entity.ChannelIdentity;

/** Gửi tin AI / tin hệ thống ra Messenger. Tin AI luôn là RESPONSE trong cửa sổ 24 giờ, không dùng tag. */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessengerSender implements ChannelSender {

    private final MessengerClient client;

    @Override
    public ChannelCode channel() {
        return ChannelCode.MESSENGER;
    }

    @Override
    public int maxTextLength() {
        return MessengerClient.MAX_TEXT_LENGTH;
    }

    @Override
    public String sendText(ChannelIdentity recipient, String text) {
        return client.sendText(recipient.getExternalUserId(), text, false).messageId();
    }

    @Override
    public void typing(ChannelIdentity recipient) {
        try {
            client.senderAction(recipient.getExternalUserId(), "mark_seen");
            client.senderAction(recipient.getExternalUserId(), "typing_on");
        } catch (RuntimeException e) {
            log.debug("Không gửi được typing_on: {}", e.getMessage());
        }
    }
}
