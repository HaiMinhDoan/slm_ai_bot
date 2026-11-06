package vn.slmglobal.slmai.services.conversation;

import org.junit.jupiter.api.Test;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.customer.entity.ChannelIdentity;
import vn.slmglobal.slmai.services.customer.repository.ChannelIdentityRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Lấy lại tên kênh cho khách chưa có tên: mỗi khách thử một lần trong một tiến trình */
class ChannelNameServiceTest {

    private final ChannelIdentityRepository repository = mock(ChannelIdentityRepository.class);
    private final List<String> asked = new ArrayList<>();

    private final ChannelSender messenger = new ChannelSender() {
        @Override
        public ChannelCode channel() {
            return ChannelCode.MESSENGER;
        }

        @Override
        public int maxTextLength() {
            return 2000;
        }

        @Override
        public String sendText(ChannelIdentity recipient, String text) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<String> fetchDisplayName(ChannelIdentity recipient) {
            asked.add(recipient.getExternalUserId());
            return "psid-lan".equals(recipient.getExternalUserId()) ? Optional.of("Lan Nguyễn") : Optional.empty();
        }
    };

    private final ChannelNameService service = new ChannelNameService(repository, List.of(messenger));

    @Test
    void backfillTriesEachUnnamedCustomerOnce() {
        ChannelIdentity lan = identity("psid-lan", null);
        ChannelIdentity hidden = identity("psid-an", null);
        when(repository.findIdsWithoutDisplayName()).thenReturn(List.of(lan.getId(), hidden.getId()));
        when(repository.findAllById(List.of(lan.getId(), hidden.getId()))).thenReturn(List.of(lan, hidden));

        assertThat(service.backfill()).isEqualTo(1);
        verify(repository).setDisplayNameIfMissing(lan.getId(), "Lan Nguyễn");

        // Lần quét sau không hỏi lại khách đã thử, kể cả khách không lấy được tên
        assertThat(service.backfill()).isZero();
        assertThat(asked).containsExactly("psid-lan", "psid-an");
    }

    @Test
    void knownNameIsNotFetchedAgain() {
        assertThat(service.fetchOnce(identity("psid-lan", "Chị Lan"))).contains("Chị Lan");
        assertThat(asked).isEmpty();
        verify(repository, never()).setDisplayNameIfMissing(any(), anyString());
    }

    private static ChannelIdentity identity(String psid, String name) {
        ChannelIdentity i = new ChannelIdentity();
        i.setId(UUID.randomUUID());
        i.setChannel(ChannelCode.MESSENGER);
        i.setExternalUserId(psid);
        i.setDisplayName(name);
        return i;
    }
}
