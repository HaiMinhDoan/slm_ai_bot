package vn.slmglobal.slmai.adapters.facebook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vn.slmglobal.slmai.services.conversation.ChannelSender;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.customer.entity.ChannelIdentity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Gửi tin AI / tin hệ thống ra Messenger. Tin AI luôn là RESPONSE trong cửa sổ 24 giờ, không dùng tag. */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessengerSender implements ChannelSender {

    /** Số tệp nhớ attachment_id (tệp gửi lại cho khách khác không phải tải lên lại) */
    static final int REUSABLE_CACHE = 500;

    private final MessengerClient client;
    /** key của tệp (sha256 nội dung) → attachment_id của Meta; mất khi khởi động lại thì tải lên lại một lần */
    private final Map<String, String> reusable = Collections.synchronizedMap(
            new LinkedHashMap<>(64, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                    return size() > REUSABLE_CACHE;
                }
            });

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
    public boolean supportsFiles() {
        return true;
    }

    @Override
    public String sendFile(ChannelIdentity recipient, OutgoingFile file) {
        String psid = recipient.getExternalUserId();
        String attachmentId = file.key() == null ? null : reusable.get(file.key());
        if (attachmentId != null) {
            try {
                return client.sendAttachmentById(psid, file.kind(), attachmentId).messageId();
            } catch (MessengerApiException e) {
                // attachment_id hết hạn hoặc bị xoá: tải lên lại
                log.info("Không gửi lại được tệp đã tải ({}), tải lên lại: {}", file.fileName(), e.getMessage());
                reusable.remove(file.key());
            }
        }
        MessengerClient.AttachmentResult r = client.sendAttachment(psid, file.kind(), file.content().get(),
                file.mimeType(), file.fileName());
        if (file.key() != null && r.attachmentId() != null) {
            reusable.put(file.key(), r.attachmentId());
        }
        return r.messageId();
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
