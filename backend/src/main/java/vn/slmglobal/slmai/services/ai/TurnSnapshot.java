package vn.slmglobal.slmai.services.ai;

import vn.slmglobal.slmai.common.util.VietnamesePhone;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.conversation.entity.Message;
import vn.slmglobal.slmai.services.conversation.entity.ReplyMode;
import vn.slmglobal.slmai.services.customer.entity.ChannelIdentity;
import vn.slmglobal.slmai.services.lead.entity.ConsultProfile;
import vn.slmglobal.slmai.services.leadnurturing.entity.FollowUp;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Ảnh chụp hội thoại lúc nhận lượt (AiTurnStore.claim). Các entity bên trong đã tách khỏi transaction, chỉ đọc.
 *
 * @param leaseUntil      hạn giữ lượt đã ghi vào han_tra_loi_luc; lúc chốt so lại để biết có lượt mới chen vào không
 * @param inboundCount    số tin của khách lúc nhận lượt; lúc chốt mà tăng nghĩa là khách vừa nhắn thêm
 * @param history         các tin gần nhất, cũ trước
 * @param pendingInbound  tin của khách chưa được AI xử lý (RECEIVED), sẽ chuyển PROCESSED khi lượt được chốt
 * @param firstTurnAfterHandback lượt đầu sau khi hội thoại tự trả lại AI (tu_tra_lai_luc)
 * @param handedBackByStaff      lượt đầu sau khi nhân viên bấm "Chuyển cho AI" (nv_giao_lai_luc)
 * @param profile         hồ sơ tư vấn trước lượt này; null nếu chưa có
 * @param followUp        lịch nhắc khách đang chờ (mục 56.9); null là lượt trả lời thường
 */
public record TurnSnapshot(
        UUID conversationId,
        UUID customerId,
        ChannelCode channel,
        ChannelIdentity identity,
        OffsetDateTime leaseUntil,
        long inboundCount,
        List<Message> history,
        List<Message> pendingInbound,
        ReplyMode replyMode,
        boolean firstTurnAfterHandback,
        boolean handedBackByStaff,
        OffsetDateTime lastCustomerMessageAt,
        ConsultProfile profile,
        String customerPhone,
        String customerName,
        String salutation,
        FollowUp followUp
) {

    public TurnSnapshot(UUID conversationId, UUID customerId, ChannelCode channel, ChannelIdentity identity,
                        OffsetDateTime leaseUntil, long inboundCount, List<Message> history,
                        List<Message> pendingInbound, ReplyMode replyMode, boolean firstTurnAfterHandback,
                        boolean handedBackByStaff, OffsetDateTime lastCustomerMessageAt, ConsultProfile profile,
                        String customerPhone, String customerName, String salutation) {
        this(conversationId, customerId, channel, identity, leaseUntil, inboundCount, history, pendingInbound,
                replyMode, firstTurnAfterHandback, handedBackByStaff, lastCustomerMessageAt, profile, customerPhone,
                customerName, salutation, null);
    }

    /** Lượt nhắc khách: có lịch nhắc và không có tin khách nào đang chờ trả lời */
    public boolean isFollowUp() {
        return followUp != null && pendingInbound.isEmpty();
    }

    /** Số điện thoại khách vừa gửi trong các tin của lượt này */
    public Optional<String> phoneInPendingMessages() {
        return pendingInbound.stream()
                .map(m -> VietnamesePhone.findFirst(m.getContent()))
                .flatMap(Optional::stream)
                .findFirst();
    }

    public boolean hasPhone() {
        return customerPhone != null || phoneInPendingMessages().isPresent();
    }

    public List<UUID> pendingInboundIds() {
        return pendingInbound.stream().map(Message::getId).toList();
    }

    /** Bản sao có tên khách (vừa lấy từ kênh) */
    public TurnSnapshot withCustomerName(String name) {
        return new TurnSnapshot(conversationId, customerId, channel, identity, leaseUntil, inboundCount, history,
                pendingInbound, replyMode, firstTurnAfterHandback, handedBackByStaff, lastCustomerMessageAt, profile,
                customerPhone, name, salutation, followUp);
    }
}
