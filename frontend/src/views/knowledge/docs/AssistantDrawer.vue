<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    title="AI hỗ trợ soạn kiến thức"
    width="75%"
    showFooter
    :footerHeight="104"
    @close="stopPolling"
  >
    <div class="flex items-center gap-2 mb-3">
      <span>Phiên:</span>
      <Select
        class="flex-1"
        :value="session?.id"
        :options="sessionOptions"
        placeholder="Chưa có phiên"
        @change="(id) => openSession(id as string)"
      />
      <Button @click="newSession()">Phiên mới</Button>
    </div>
    <Alert
      type="info"
      show-icon
      class="mb-3"
      message='Nhập yêu cầu bằng lời thường, ví dụ: "Mái bằng thì báo khách phát sinh phần khung, nhân viên báo giá sau khảo sát".'
      description="AI đọc hướng dẫn viết và toàn bộ kiến thức đang chạy rồi đề xuất sửa / tạo tài liệu. Bạn xem khác biệt, duyệt thành bản nháp; bot chỉ đổi khi phát hành ở trang Phát hành. Đổi con số trong quy tắc (giá điện, ngưỡng...) thì sửa ở trang Quy tắc."
    />
    <Alert
      v-if="targetDoc"
      type="success"
      class="mb-3"
      :message="`Tài liệu đích: ${targetDoc.key}`"
    />

    <div v-if="session" class="chat">
      <div v-if="!session.messages.length" class="text-gray-500 py-6 text-center">
        Chưa có tin nhắn. Mô tả thay đổi bạn muốn ở ô bên dưới.
      </div>
      <div
        v-for="m in session.messages"
        :key="m.id"
        :class="['msg', m.role === 'USER' ? 'msg-user' : 'msg-ai']"
      >
        <div class="msg-head"
          >{{ m.role === 'USER' ? 'Bạn' : 'AI' }} · {{ formatToDateTime(m.createdAt) }}</div
        >
        <Alert v-if="m.error" type="error" show-icon :message="m.error" />
        <div v-else class="msg-text">{{ m.text }}</div>
        <div v-for="p in proposalsOf(m)" :key="p.id" class="proposal">
          <div class="flex items-center flex-wrap gap-1">
            <Tag :color="CHANGE_ACTION[p.action]?.color">{{ CHANGE_ACTION[p.action]?.label }}</Tag>
            <b>{{ p.docKey }}</b>
            <span class="text-gray-500">· {{ p.title }}</span>
            <Tag :color="PROPOSAL_STATUS[p.status].color" class="ml-auto">
              {{ PROPOSAL_STATUS[p.status].label }}
            </Tag>
          </div>
          <div class="mt-1"><b>Thay đổi:</b> {{ p.summary }}</div>
          <div v-if="p.reason"><b>Lý do:</b> {{ p.reason }}</div>
          <Alert
            v-if="p.conflicts.length"
            type="warning"
            class="mt-2"
            message="AI thấy có thể mâu thuẫn với tài liệu khác"
          >
            <template #description>
              <div v-for="(c, i) in p.conflicts" :key="i">• {{ c }}</div>
            </template>
          </Alert>
          <div v-for="(i, idx) in p.lint ?? []" :key="idx" class="text-xs mt-1">
            <Tag :color="CHECK_LEVEL[i.level]?.color">{{ CHECK_LEVEL[i.level]?.label }}</Tag>
            <span v-if="i.line" class="text-gray-500">Dòng {{ i.line }}: </span>{{ i.message }}
          </div>
          <div v-if="p.status === 'PROPOSED' && p.diff !== undefined" class="mt-2">
            <a @click="toggle(p.id)">{{ opened.has(p.id) ? 'Ẩn khác biệt' : 'Xem khác biệt' }}</a>
            <div v-if="opened.has(p.id)" class="diff-box mt-1">
              <CodeEditor
                :value="p.diff || '(không có khác biệt)'"
                :mode="MODE.DIFF"
                :config="CODE_CONFIG"
                readonly
                bordered
              />
            </div>
          </div>
          <div v-if="p.status === 'PROPOSED'" class="mt-2 flex gap-2">
            <Button type="primary" size="small" :disabled="!p.canAccept" @click="accept(p)">
              Duyệt thành bản nháp
            </Button>
            <Button size="small" @click="reject(p)">Từ chối</Button>
            <span v-if="!p.canAccept" class="text-red-500 text-xs self-center">
              Đề xuất có lỗi, nhắn AI sửa lại.
            </span>
          </div>
        </div>
      </div>
      <div v-if="session.pending" class="msg msg-ai">
        <Spin size="small" />
        <span class="ml-2 text-gray-500">AI đang soạn đề xuất (có thể mất 30–90 giây)...</span>
      </div>
    </div>

    <template #footer>
      <div class="flex gap-2 p-2">
        <InputTextArea
          v-model:value="text"
          :rows="3"
          :maxlength="10000"
          placeholder='Mô tả thay đổi, hoặc nhắn tiếp: "ngắn hơn", "thêm ví dụ"...'
          :disabled="!session || session.pending"
          @press-enter="(e) => e.ctrlKey && send()"
        />
        <Button
          type="primary"
          :loading="sending"
          :disabled="!session || session.pending || !text.trim()"
          @click="send"
        >
          Gửi
        </Button>
      </div>
    </template>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { computed, onBeforeUnmount, reactive, ref } from 'vue';
  import { Alert, Input, Select, Spin, Tag } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
  import { CodeEditor, MODE } from '@/components/CodeEditor';
  import { useMessage } from '@/hooks/web/useMessage';
  import { formatToDateTime } from '@/utils/dateUtil';
  import { CHANGE_ACTION, CHECK_LEVEL } from '../shared/labels';
  import { apiError } from '../shared/errors';
  import { CODE_CONFIG } from '../shared/codeEditor';
  import {
    assistantAcceptApi,
    assistantRejectApi,
    assistantSendApi,
    assistantSessionApi,
    assistantSessionsApi,
    assistantStartApi,
  } from '@/api/knowledge';
  import type {
    AssistantMessage,
    AssistantProposal,
    AssistantSession,
    AssistantSessionRow,
  } from '@/api/knowledge/model/knowledgeModel';

  defineOptions({ name: 'KnowledgeAssistantDrawer' });

  /** Mở bằng openDrawer(true, { docId?, docKey? }); duyệt đề xuất xong phát `changed` để trang tải lại */
  const emit = defineEmits(['changed', 'register']);
  const InputTextArea = Input.TextArea;
  const { createMessage, createConfirm } = useMessage();

  const PROPOSAL_STATUS = {
    PROPOSED: { label: 'Chờ duyệt', color: 'blue' },
    ACCEPTED: { label: 'Đã duyệt thành nháp', color: 'green' },
    REJECTED: { label: 'Đã từ chối', color: 'default' },
    SUPERSEDED: { label: 'Đã thay bằng đề xuất sau', color: 'default' },
  };

  const sessions = ref<AssistantSessionRow[]>([]);
  const session = ref<AssistantSession>();
  const targetDoc = ref<{ id: number; key: string }>();
  const text = ref('');
  const sending = ref(false);
  const opened = reactive(new Set<number>());
  let timer: ReturnType<typeof setTimeout> | undefined;

  const sessionOptions = computed(() =>
    sessions.value.map((s) => ({
      label: `${s.title} · ${formatToDateTime(s.createdAt)}`,
      value: s.id,
    })),
  );

  const [registerDrawer] = useDrawerInner(async (data) => {
    targetDoc.value = data?.docId ? { id: data.docId, key: data.docKey } : undefined;
    text.value = '';
    sessions.value = await assistantSessionsApi();
    // Mở lại phiên vừa dùng gần nhất (của tài liệu đích nếu có) thay vì mỗi lần mở lại tạo một phiên trống;
    // phiên đã có tin nhắn xếp trước phiên trống
    const recent = sessions.value
      .filter((s) => !targetDoc.value || s.title === targetDoc.value.key)
      .sort(
        (a, b) =>
          (b.lastMessageAt ?? '').localeCompare(a.lastMessageAt ?? '') ||
          b.createdAt.localeCompare(a.createdAt),
      )[0];
    if (recent) {
      await openSession(recent.id);
    } else {
      await newSession(targetDoc.value?.key);
    }
  });

  function proposalsOf(m: AssistantMessage): AssistantProposal[] {
    return (session.value?.proposals ?? []).filter((p) => m.proposalIds.includes(p.id));
  }

  async function newSession(title?: string) {
    session.value = await assistantStartApi({ title, docId: targetDoc.value?.id });
    sessions.value = await assistantSessionsApi();
  }

  async function openSession(id: string) {
    stopPolling();
    session.value = await assistantSessionApi(id);
    if (session.value.pending) poll();
  }

  async function send() {
    if (!session.value || !text.value.trim()) return;
    sending.value = true;
    try {
      session.value = await assistantSendApi(session.value.id, {
        text: text.value.trim(),
        docId: targetDoc.value?.id,
      });
      text.value = '';
      poll();
    } finally {
      sending.value = false;
    }
  }

  function poll() {
    stopPolling();
    timer = setTimeout(async () => {
      if (!session.value) return;
      session.value = await assistantSessionApi(session.value.id);
      if (session.value.pending) {
        poll();
      }
    }, 2500);
  }

  function stopPolling() {
    if (timer) clearTimeout(timer);
    timer = undefined;
  }

  function toggle(id: number) {
    if (opened.has(id)) opened.delete(id);
    else opened.add(id);
  }

  async function accept(p: AssistantProposal, force = false) {
    try {
      await assistantAcceptApi(p.id, force);
      createMessage.success(
        p.action === 'RETIRE'
          ? `Đã lưu trữ ${p.docKey}. Áp dụng cho bot ở trang Phát hành.`
          : `Đã tạo bản nháp cho ${p.docKey}. Áp dụng cho bot ở trang Phát hành.`,
      );
      session.value = await assistantSessionApi(session.value!.id);
      emit('changed');
    } catch (e) {
      const err = apiError(e);
      if (err.status === 409) {
        createConfirm({
          iconType: 'warning',
          title: 'Ghi đè bản nháp đang có?',
          content: err.message,
          okText: 'Ghi đè',
          onOk: () => accept(p, true),
        });
      } else {
        createMessage.error(err.message);
      }
    }
  }

  async function reject(p: AssistantProposal) {
    await assistantRejectApi(p.id);
    session.value = await assistantSessionApi(session.value!.id);
  }

  onBeforeUnmount(stopPolling);
</script>

<style lang="less" scoped>
  .chat {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  // Màu bán trong suốt để đọc được ở cả giao diện sáng và tối
  .msg {
    max-width: 92%;
    padding: 10px 12px;
    border: 1px solid rgb(128 128 128 / 25%);
    border-radius: 8px;
  }

  .msg-user {
    align-self: flex-end;
    background: rgb(22 119 255 / 10%);
  }

  .msg-ai {
    align-self: flex-start;
    background: rgb(128 128 128 / 8%);
  }

  .msg-head {
    margin-bottom: 4px;
    color: #888;
    font-size: 12px;
  }

  .msg-text {
    white-space: pre-wrap;
  }

  .proposal {
    margin-top: 10px;
    padding: 10px;
    border: 1px solid rgb(128 128 128 / 35%);
    border-radius: 6px;
  }

  .diff-box {
    height: 360px;

    :deep(.CodeMirror) {
      height: 100%;
    }
  }
</style>
