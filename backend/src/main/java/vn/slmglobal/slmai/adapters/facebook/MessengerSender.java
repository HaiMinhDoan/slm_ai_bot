package vn.slmglobal.slmai.adapters.facebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.slmglobal.slmai.services.conversation.ChannelSender;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.customer.entity.ChannelIdentity;

import java.util.Optional;

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
    public Optional<String> fetchDisplayName(ChannelIdentity recipient) {
        try {
            return Optional.ofNullable(client.fetchUserName(recipient.getExternalUserId()));
        } catch (RuntimeException e) {
            log.info("Không lấy được tên Facebook của khách: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void markSeen(ChannelIdentity recipient) {
        senderAction(recipient, "mark_seen");
    }

    @Override
    public void typing(ChannelIdentity recipient) {
        senderAction(recipient, "typing_on");
    }

    @Override
    public void stopTyping(ChannelIdentity recipient) {
        senderAction(recipient, "typing_off");
    }

    private void senderAction(ChannelIdentity recipient, String action) {
        try {
            client.senderAction(recipient.getExternalUserId(), action);
        } catch (RuntimeException e) {
            log.debug("Không gửi được {}: {}", action, e.getMessage());
        }
    }
}
