<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    :title="title"
    width="88%"
    showFooter
    :footerHeight="56"
    @close="stopPolling"
  >
    <template #footer>
      <div class="footer">
        <div class="footer-status">
          <Tag v-if="detail" :color="OWNERSHIP[detail.conversation.ownership].color">
            {{ OWNERSHIP[detail.conversation.ownership].label }}
          </Tag>
          <span v-if="detail?.conversation.aiReplying" class="text-gray-500">
            <Spin size="small" /> AI đang soạn trả lời...
          </span>
        </div>
        <Button :loading="loading" @click="load()">Tải lại</Button>
        <Popconfirm
          v-if="detail?.conversation.ownership === 'HUMAN_OWNED'"
          title="Chuyển cho AI? AI trả lời ngay các tin khách chưa được trả lời; nháp chưa gửi sẽ bị bỏ."
          placement="topRight"
          @confirm="toAi"
        >
          <Button type="primary" :loading="acting">Chuyển cho AI</Button>
        </Popconfirm>
        <Button
          v-if="detail?.conversation.ownership === 'AI_ACTIVE'"
          type="primary"
          danger
          :loading="acting"
          @click="openToStaff(true, { id: detail?.conversation.id })"
        >
          Chuyển cho sale
        </Button>
      </div>
    </template>

    <div v-if="detail" class="layout">
      <div class="chat-col">
        <Alert
          v-if="!detail.window.channelSupported"
          type="warning"
          show-icon
          class="mb-2"
          message="Kênh này chưa gửi được tin từ trang admin; trả lời khách trên ứng dụng của kênh."
        />
        <Alert
          v-else-if="!detail.window.open"
          type="warning"
          show-icon
          class="mb-2"
          :message="`Đã quá 24 giờ kể từ tin cuối của khách${detail.window.endsAt ? ' (hết hạn ' + formatToDateTime(detail.window.endsAt) + ')' : ''}: kênh không cho gửi.`"
          description="Liên hệ khách qua số điện thoại, hoặc chờ khách nhắn lại."
        />
        <Alert
          v-else-if="detail.conversation.ownership === 'HUMAN_OWNED'"
          type="info"
          show-icon
          class="mb-2"
          :message="`Nhân viên đang phụ trách${detail.conversation.takenOverAt ? ' từ ' + formatToDateTime(detail.conversation.takenOverAt) : ''}: AI không trả lời khách.`"
          description="Trả lời khách trong hộp thư Meta Business Suite (gõ tin ngay trên trang này sẽ có ở bản sau), hoặc gửi nháp AI bên dưới. Xong thì bấm Chuyển cho AI."
        />

        <div ref="scrollEl" class="messages">
          <div v-if="!detail.messages.length" class="text-gray-500 py-6 text-center">Chưa có tin nhắn.</div>
          <div
            v-for="m in detail.messages"
            :key="m.id"
            :class="[
              'msg',
              m.direction === 'IN' ? 'msg-in' : 'msg-out',
              m.senderType === 'SYSTEM' ? 'msg-system' : '',
              isPending(m) ? 'msg-pending' : '',
              m.status === 'DISCARDED' ? 'msg-discarded' : '',
            ]"
          >
            <div class="msg-head">
              <b>{{ senderLabel(m) }}</b>
              · {{ formatToDateTime(m.sentAt || m.createdAt) }}
              <Tag v-if="m.status === 'DRAFT'" color="orange" class="ml-1">Nháp chưa gửi</Tag>
              <Tag v-else-if="m.status === 'FAILED'" color="red" class="ml-1">Gửi lỗi</Tag>
              <Tag v-else-if="m.status === 'DISCARDED'" class="ml-1">Đã bỏ</Tag>
            </div>

            <template v-if="editing[m.id] !== undefined">
              <InputTextArea
                v-model:value="editing[m.id]"
                :rows="4"
                :maxlength="detail.window.maxLength || 2000"
                show-count
              />
              <div class="mt-2 flex gap-2">
                <Popconfirm title="Gửi bản đã sửa cho khách?" @confirm="send(m, editing[m.id])">
                  <Button
                    type="primary"
                    size="small"
                    :loading="sending === m.id"
                    :disabled="!canSend || !editing[m.id]?.trim()"
                  >
                    Gửi bản sửa
                  </Button>
                </Popconfirm>
                <Button size="small" @click="cancelEdit(m.id)">Huỷ sửa</Button>
              </div>
            </template>
            <div v-else class="msg-text">{{ m.content }}</div>

            <div v-for="(a, i) in m.attachments ?? []" :key="i" class="mt-1">
              <Image
                v-if="a.type === 'image' && a.payload?.url"
                :src="a.payload.url"
                :width="120"
                class="rounded"
              />
              <a v-else-if="a.payload?.url" :href="a.payload.url" target="_blank" rel="noopener noreferrer">
                Tệp đính kèm ({{ a.type || 'tệp' }})
              </a>
              <span v-else class="text-gray-500">[{{ a.type || 'đính kèm' }}]</span>
            </div>

            <template v-if="isPending(m) && editing[m.id] === undefined">
              <div v-if="m.reasons.length" class="text-xs mt-1 text-gray-500">
                Chưa tự gửi vì: {{ m.reasons.join(', ') }}
              </div>
              <div class="mt-2 flex gap-2">
                <Popconfirm title="Gửi tin này cho khách?" @confirm="send(m)">
                  <Button type="primary" size="small" :loading="sending === m.id" :disabled="!canSend">
                    {{ m.status === 'FAILED' ? 'Gửi lại' : 'Gửi' }}
                  </Button>
                </Popconfirm>
                <Button size="small" :disabled="!canSend" @click="editing[m.id] = m.content ?? ''">
                  Sửa rồi gửi
                </Button>
                <Popconfirm title="Bỏ tin này? Khách sẽ không nhận được." @confirm="discard(m)">
                  <Button size="small" danger>Bỏ</Button>
                </Popconfirm>
              </div>
            </template>
          </div>
        </div>
      </div>

      <div class="info-col">
        <Descriptions title="Khách" size="small" bordered :column="1">
          <DescriptionsItem label="Tên">{{ detail.customer.name || '—' }}</DescriptionsItem>
          <DescriptionsItem v-if="detail.customer.displayName" label="Tên trên kênh">
            {{ detail.customer.displayName }}
          </DescriptionsItem>
          <DescriptionsItem label="Số điện thoại">
            <TypographyText v-if="detail.customer.phone" :copyable="{ text: detail.customer.phone }">
              {{ detail.customer.phone }}
            </TypographyText>
            <span v-else>Chưa có</span>
          </DescriptionsItem>
          <DescriptionsItem v-if="detail.customer.salutation" label="Cách gọi">
            {{ SLOT_VALUE_LABELS[detail.customer.salutation] ?? detail.customer.salutation }}
          </DescriptionsItem>
          <DescriptionsItem label="Kênh">{{ CHANNEL_LABELS[detail.conversation.channel] }}</DescriptionsItem>
          <DescriptionsItem v-if="detail.conversation.intent" label="Loại câu hỏi cuối">
            {{ INTENT_LABELS[detail.conversation.intent] ?? detail.conversation.intent }}
          </DescriptionsItem>
        </Descriptions>

        <Descriptions title="Tiềm năng" size="small" bordered :column="1" class="mt-4">
          <DescriptionsItem label="Mức">
            <Tag v-if="level" :color="LEAD_LEVEL[level].color">{{ LEAD_LEVEL[level].label }}</Tag>
            <span v-else>Chưa phân loại</span>
          </DescriptionsItem>
          <DescriptionsItem v-if="detail.profile?.leadLevelReason" label="Lý do">
            {{ detail.profile.leadLevelReason }}
          </DescriptionsItem>
          <DescriptionsItem v-if="qualification.timeline" label="Thời điểm lắp">
            {{ QUALIFICATION_VALUE_LABELS[qualification.timeline] ?? qualification.timeline }}
          </DescriptionsItem>
          <DescriptionsItem v-if="budget" label="Ngân sách">{{ budget }}</DescriptionsItem>
          <DescriptionsItem v-if="qualification.research_level?.value" label="Mức tìm hiểu">
            {{
              QUALIFICATION_VALUE_LABELS[qualification.research_level.value] ??
              qualification.research_level.value
            }}
          </DescriptionsItem>
          <DescriptionsItem v-if="detail.lead" label="Lead">
            Đã có lead ({{ LEAD_STATUS[detail.lead.status] ?? detail.lead.status }}),
            {{ formatToDateTime(detail.lead.createdAt) }}
          </DescriptionsItem>
        </Descriptions>

        <Descriptions title="Thông tin tư vấn AI đã thu" size="small" bordered :column="1" class="mt-4">
          <DescriptionsItem v-for="(label, key) in SLOT_LABELS" :key="key" :label="label">
            <span v-if="slotValue(key)">{{ slotValue(key) }}</span>
            <span v-else class="text-gray-400">
              {{ detail.profile?.missing?.includes(key) ? 'Còn thiếu' : '—' }}
            </span>
          </DescriptionsItem>
          <DescriptionsItem v-if="detail.profile?.packages.length" label="Gói đã báo">
            <div v-for="p in detail.profile.packages" :key="p.id">{{ p.name }}</div>
          </DescriptionsItem>
          <DescriptionsItem v-if="detail.profile?.cheapestQuotedPrice" label="Giá thấp nhất đã báo">
            {{ money(detail.profile.cheapestQuotedPrice) }}
          </DescriptionsItem>
        </Descriptions>
      </div>
    </div>

    <ToStaffModal @register="registerToStaff" @done="apply" />
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { computed, nextTick, reactive, ref } from 'vue';
  import { Alert, Descriptions, Image, Input, Popconfirm, Spin, Tag, Typography } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
  import { useModal } from '@/components/Modal';
  import { useMessage } from '@/hooks/web/useMessage';
  import { formatToDateTime } from '@/utils/dateUtil';
  import { apiError } from '@/views/knowledge/shared/errors';
  import { INTENT_LABELS } from '@/views/knowledge/shared/labels';
  import {
    inboxConversationApi,
    inboxDiscardDraftApi,
    inboxSendDraftApi,
    inboxToAiApi,
  } from '@/api/inbox';
  import type { ChatMessage, ConversationDetail } from '@/api/inbox/model/inboxModel';
  import ToStaffModal from './ToStaffModal.vue';
  import {
    CHANNEL_LABELS,
    LEAD_LEVEL,
    LEAD_STATUS,
    OWNERSHIP,
    QUALIFICATION_VALUE_LABELS,
    SENDER_LABELS,
    SLOT_LABELS,
    SLOT_VALUE_LABELS,
  } from './inbox.data';

  defineOptions({ name: 'InboxConversationDrawer' });

  /** Mở bằng openDrawer(true, { id }); đổi người phụ trách / gửi / bỏ nháp xong phát `changed` để bảng tải lại */
  const emit = defineEmits(['changed', 'register']);
  const InputTextArea = Input.TextArea;
  const DescriptionsItem = Descriptions.Item;
  const TypographyText = Typography.Text;
  const { createMessage } = useMessage();

  /** Hỏi lại hội thoại đang mở: thấy tin mới của khách và câu trả lời của AI */
  const POLL_MS = 5000;

  const detail = ref<ConversationDetail>();
  const conversationId = ref<string>();
  const loading = ref(false);
  const acting = ref(false);
  const sending = ref<string>();
  /** Nội dung đang sửa theo id tin nháp */
  const editing = reactive<Record<string, string>>({});
  const scrollEl = ref<HTMLElement>();
  let timer: ReturnType<typeof setInterval> | undefined;

  const [registerToStaff, { openModal: openToStaff }] = useModal();

  const title = computed(() => {
    const c = detail.value?.customer;
    const name = c?.name || c?.displayName || 'Khách';
    return detail.value ? `${name} · ${CHANNEL_LABELS[detail.value.conversation.channel]}` : 'Hội thoại';
  });
  const canSend = computed(() => !!detail.value?.window.channelSupported && !!detail.value?.window.open);
  const level = computed(() => detail.value?.profile?.leadLevel ?? detail.value?.lead?.leadLevel);
  const qualification = computed<Recordable>(() => detail.value?.profile?.qualification ?? {});
  const budget = computed(() => {
    const b = qualification.value.budget_vnd;
    if (!b || (b.min == null && b.max == null)) return undefined;
    if (b.min != null && b.max != null) return `${money(b.min)} – ${money(b.max)}`;
    return b.max != null ? `Tới ${money(b.max)}` : `Từ ${money(b.min)}`;
  });

  const [registerDrawer, { changeLoading }] = useDrawerInner(async (data) => {
    conversationId.value = data.id;
    detail.value = undefined;
    Object.keys(editing).forEach((k) => delete editing[k]);
    changeLoading(true);
    try {
      await load(true);
    } finally {
      changeLoading(false);
    }
    stopPolling();
    timer = setInterval(() => {
      if (!document.hidden && !acting.value && !sending.value) load();
    }, POLL_MS);
  });

  async function load(scrollToEnd = false) {
    if (!conversationId.value) return;
    loading.value = true;
    try {
      const atBottom = isAtBottom();
      const before = detail.value?.messages.length ?? 0;
      apply(await inboxConversationApi(conversationId.value));
      if (scrollToEnd || (atBottom && (detail.value?.messages.length ?? 0) > before)) {
        await nextTick();
        scrollEl.value?.scrollTo({ top: scrollEl.value.scrollHeight });
      }
    } finally {
      loading.value = false;
    }
  }

  function apply(d: ConversationDetail) {
    const changed = detail.value && changedFrom(detail.value, d);
    detail.value = d;
    // Tin đang sửa đã được gửi / bỏ (bởi người khác) thì bỏ chế độ sửa
    Object.keys(editing).forEach((id) => {
      if (!d.messages.some((m) => m.id === id && isPending(m))) delete editing[id];
    });
    if (changed) emit('changed');
  }

  function changedFrom(a: ConversationDetail, b: ConversationDetail) {
    return (
      a.conversation.ownership !== b.conversation.ownership ||
      a.messages.length !== b.messages.length ||
      a.messages.some((m, i) => b.messages[i]?.status !== m.status)
    );
  }

  function cancelEdit(id: string) {
    delete editing[id];
  }

  function isAtBottom() {
    const el = scrollEl.value;
    return !el || el.scrollHeight - el.scrollTop - el.clientHeight < 40;
  }

  function stopPolling() {
    if (timer) clearInterval(timer);
    timer = undefined;
  }

  function isPending(m: ChatMessage) {
    return m.direction === 'OUT' && (m.status === 'DRAFT' || m.status === 'FAILED');
  }

  function senderLabel(m: ChatMessage) {
    if (m.senderType === 'AI' && m.staffId) return `AI (duyệt bởi ${m.staffId})`;
    if (m.senderType === 'STAFF') return m.staffId ? `Nhân viên ${m.staffId}` : 'Nhân viên (hộp thư Meta)';
    return SENDER_LABELS[m.senderType];
  }

  function slotValue(key: string) {
    const v = detail.value?.profile?.slots?.[key];
    if (v === undefined || v === null || v === '') return undefined;
    if (key === 'hoa_don_thang' && typeof v === 'number') return money(v);
    return SLOT_VALUE_LABELS[String(v)] ?? String(v);
  }

  function money(v: number) {
    return `${Number(v).toLocaleString('vi-VN')} đ`;
  }

  async function send(m: ChatMessage, text?: string) {
    sending.value = m.id;
    try {
      apply(await inboxSendDraftApi(m.id, text?.trim()));
      delete editing[m.id];
      createMessage.success('Đã gửi cho khách');
      emit('changed');
    } catch (e) {
      const err = apiError(e);
      createMessage.error(err.message);
      if (err.status === 409) await load();
    } finally {
      sending.value = undefined;
    }
  }

  async function discard(m: ChatMessage) {
    apply(await inboxDiscardDraftApi(m.id));
    createMessage.success('Đã bỏ tin');
    emit('changed');
  }

  async function toAi() {
    if (!detail.value) return;
    acting.value = true;
    try {
      apply(await inboxToAiApi(detail.value.conversation.id));
      createMessage.success('Đã chuyển cho AI');
      emit('changed');
    } finally {
      acting.value = false;
    }
  }
</script>

<style lang="less" scoped>
  // Cao cố định để khung chat và khung thông tin cuộn riêng (trừ tiêu đề, chân drawer)
  .layout {
    display: flex;
    gap: 16px;
    height: calc(100vh - 150px);
  }

  .chat-col {
    display: flex;
    flex: 1;
    flex-direction: column;
    min-width: 0;
  }

  .messages {
    display: flex;
    flex: 1;
    flex-direction: column;
    gap: 10px;
    padding-right: 4px;
    overflow-y: auto;
  }

  .info-col {
    flex: 0 0 360px;
    overflow-y: auto;
  }

  // Màu bán trong suốt để đọc được ở cả giao diện sáng và tối
  .msg {
    max-width: 75%;
    padding: 8px 12px;
    border: 1px solid rgb(128 128 128 / 25%);
    border-radius: 10px;
  }

  .msg-in {
    align-self: flex-start;
    background: rgb(128 128 128 / 8%);
  }

  .msg-out {
    align-self: flex-end;
    background: rgb(22 119 255 / 10%);
  }

  .msg-system {
    background: rgb(128 128 128 / 4%);
    font-size: 13px;
  }

  .msg-pending {
    border: 1px dashed rgb(250 140 22 / 80%);
    background: rgb(250 140 22 / 8%);
  }

  .msg-discarded {
    opacity: 0.5;
  }

  .msg-head {
    margin-bottom: 4px;
    color: #888;
    font-size: 12px;
  }

  .msg-text {
    white-space: pre-wrap;
    word-break: break-word;
  }

  .footer {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    height: 100%;
    padding: 0 16px;
    gap: 8px;
  }

  .footer-status {
    display: flex;
    flex: 1;
    align-items: center;
    gap: 8px;
  }
</style>
