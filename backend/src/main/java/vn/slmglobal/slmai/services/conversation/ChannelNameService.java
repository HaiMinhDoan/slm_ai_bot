package vn.slmglobal.slmai.services.conversation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.customer.entity.ChannelIdentity;
import vn.slmglobal.slmai.services.customer.repository.ChannelIdentityRepository;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tên kênh của khách (tên Facebook), lưu ở dinh_danh_kenh.ten_hien_thi để AI chọn anh / chị và để nhân viên thấy trên
 * trang Chat với khách. Mỗi định danh chỉ thử một lần trong một tiến trình: ở lượt AI đầu tiên, hoặc ở lần quét của
 * {@link ChannelNameBackfillJob} (khách do nhân viên giữ không có lượt AI; khách trước khi sửa cách lấy tên
 * 2026-11-06). Không lấy được thì để trống, khởi động lại thì thử lại.
 */
@Slf4j
@Service
public class ChannelNameService {

    /** Số định danh tối đa một lần quét */
    static final int BATCH = 50;

    private final ChannelIdentityRepository identities;
    private final Map<ChannelCode, ChannelSender> senders = new EnumMap<>(ChannelCode.class);
    private final Set<UUID> attempted = ConcurrentHashMap.newKeySet();

    public ChannelNameService(ChannelIdentityRepository identities, List<ChannelSender> channelSenders) {
        this.identities = identities;
        channelSenders.forEach(s -> senders.put(s.channel(), s));
    }

    /** Tên đã lưu; chưa có thì hỏi kênh nếu định danh này chưa được thử trong tiến trình */
    public Optional<String> fetchOnce(ChannelIdentity identity) {
        if (identity.getDisplayName() != null) {
            return Optional.of(identity.getDisplayName());
        }
        ChannelSender sender = senders.get(identity.getChannel());
        if (sender == null || !attempted.add(identity.getId())) {
            return Optional.empty();
        }
        Optional<String> name = sender.fetchDisplayName(identity);
        name.ifPresent(n -> identities.setDisplayNameIfMissing(identity.getId(), n));
        return name;
    }

    /** Thử lấy tên cho các định danh chưa có tên và chưa thử; trả số tên lấy được */
    public int backfill() {
        List<UUID> ids = identities.findIdsWithoutDisplayName().stream()
                .filter(id -> !attempted.contains(id))
                .limit(BATCH)
                .toList();
        int found = 0;
        for (ChannelIdentity identity : identities.findAllById(ids)) {
            if (fetchOnce(identity).isPresent()) {
                found++;
            }
        }
        if (!ids.isEmpty()) {
            log.info("Lấy lại tên kênh: {} / {} khách chưa có tên", found, ids.size());
        }
        return found;
    }
}
