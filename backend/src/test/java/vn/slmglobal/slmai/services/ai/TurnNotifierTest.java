package vn.slmglobal.slmai.services.ai;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import vn.slmglobal.slmai.services.ai.entity.AiRun;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.conversation.entity.Conversation;
import vn.slmglobal.slmai.services.conversation.entity.Message;
import vn.slmglobal.slmai.services.conversation.entity.MessageDirection;
import vn.slmglobal.slmai.services.conversation.entity.MessageStatus;
import vn.slmglobal.slmai.services.conversation.entity.ReplyMode;
import vn.slmglobal.slmai.services.conversation.entity.SenderType;
import vn.slmglobal.slmai.services.customer.entity.ChannelIdentity;
import vn.slmglobal.slmai.services.notification.NotificationDraft;
import vn.slmglobal.slmai.services.notification.NotificationService;
import vn.slmglobal.slmai.services.notification.NotificationType;
import vn.slmglobal.slmai.services.policy.ReplyPolicy;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class TurnNotifierTest {

    private final NotificationService notifications = mock(NotificationService.class);
    private final TurnNotifier notifier = new TurnNotifier(notifications);
    private final Conversation conversation = Conversation.builder().id(UUID.randomUUID())
            .customerId(UUID.randomUUID()).build();
    private final AiRun run = AiRun.builder().id(UUID.randomUUID()).build();
    private final Message draft = Message.builder().id(UUID.randomUUID()).content("Dạ em chào anh/chị")
            .direction(MessageDirection.OUT).senderType(SenderType.AI).status(MessageStatus.DRAFT).build();

    @Test
    void handoffBecomesUrgentHandoffNotice() {
        notifier.turnOutcome(snapshot(false), conversation, run, null,
                ReplyPolicy.Decision.handoff(ReplyPolicy.HANDOFF_INTENT), null);

        NotificationDraft d = captured();
        assertThat(d.type()).isEqualTo(NotificationType.AI_HANDOFF);
        assertThat(d.dedupeKey()).isEqualTo("AI_HANDOFF:" + run.getId());
        assertThat(d.body()).contains("sự cố").doesNotContain("0912");
    }

    @Test
    void draftCarriesDraftTextAndReasonLabels() {
        notifier.turnOutcome(snapshot(false), conversation, run, null, draftDecision(), draft);

        NotificationDraft d = captured();
        assertThat(d.type()).isEqualTo(NotificationType.AI_DRAFT);
        assertThat(d.dedupeKey()).isEqualTo("AI_DRAFT:" + draft.getId());
        assertThat(d.data()).containsEntry("draftText", "Dạ em chào anh/chị")
                .containsEntry("lastCustomerMessage", "số em 0912345678");
        assertThat(d.body()).contains("loại câu hỏi không được gửi tự động").doesNotContain("0912");
    }

    @Test
    void firstTurnAfterHandbackSendsOneHandbackNoticeInsteadOfDraft() {
        notifier.turnOutcome(snapshot(true), conversation, run, null, draftDecision(), draft);

        assertThat(captured().type()).isEqualTo(NotificationType.HANDBACK_REPLY);
    }

    @Test
    void autoTurnNotifiesNobody() {
        notifier.turnOutcome(snapshot(false), conversation, run, null,
                new ReplyPolicy.Decision(ReplyPolicy.Action.AUTO, List.of(), List.of()), draft);

        verify(notifications, never()).notify(org.mockito.ArgumentMatchers.any());
    }

    private NotificationDraft captured() {
        ArgumentCaptor<NotificationDraft> captor = ArgumentCaptor.forClass(NotificationDraft.class);
        verify(notifications).notify(captor.capture());
        return captor.getValue();
    }

    private static ReplyPolicy.Decision draftDecision() {
        return new ReplyPolicy.Decision(ReplyPolicy.Action.DRAFT, List.of(ReplyPolicy.INTENT_NOT_AUTO), List.of());
    }

    private TurnSnapshot snapshot(boolean firstAfterHandback) {
        ChannelIdentity identity = ChannelIdentity.builder().id(UUID.randomUUID()).channel(ChannelCode.MESSENGER)
                .accountId("PAGE_1").externalUserId("PSID_1").build();
        Message inbound = Message.builder().id(UUID.randomUUID()).content("số em 0912345678")
                .direction(MessageDirection.IN).senderType(SenderType.CUSTOMER).status(MessageStatus.RECEIVED).build();
        return new TurnSnapshot(conversation.getId(), conversation.getCustomerId(), ChannelCode.MESSENGER, identity,
                OffsetDateTime.now(), 1, List.of(inbound), List.of(inbound), ReplyMode.AUTO, firstAfterHandback,
                OffsetDateTime.now(), null, null, "Lan", null);
    }
}
