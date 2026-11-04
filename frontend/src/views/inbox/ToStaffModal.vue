<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="Chuyển cho sale"
    okText="Chuyển cho sale"
    :okButtonProps="{ danger: true }"
    @ok="handleOk"
  >
    <Alert
      type="warning"
      show-icon
      class="mb-3"
      message="AI dừng trả lời hội thoại này, mọi nhân viên nhận thông báo."
      description="Khách nhắn tiếp sẽ chỉ báo nhân viên. Xử lý xong thì bấm Chuyển cho AI; quá thời gian cài đặt mà không có nhân viên nhắn thì hội thoại tự trả lại AI."
    />
    <InputTextArea
      v-model:value="note"
      :rows="3"
      :maxlength="500"
      show-count
      placeholder="Ghi chú cho nhân viên (không bắt buộc), ví dụ: khách muốn gặp người, hỏi lắp cho nhà xưởng"
    />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { Alert, Input } from 'ant-design-vue';
  import { BasicModal, useModalInner } from '@/components/Modal';
  import { useMessage } from '@/hooks/web/useMessage';
  import { inboxToStaffApi } from '@/api/inbox';

  defineOptions({ name: 'InboxToStaffModal' });

  /** Mở bằng openModal(true, { id }); chuyển xong phát `done` kèm hội thoại mới */
  const emit = defineEmits(['done', 'register']);
  const InputTextArea = Input.TextArea;
  const { createMessage } = useMessage();

  const id = ref<string>();
  const note = ref('');

  const [registerModal, { closeModal, setModalProps }] = useModalInner((data) => {
    id.value = data?.id;
    note.value = '';
  });

  async function handleOk() {
    if (!id.value) return;
    setModalProps({ confirmLoading: true });
    try {
      const d = await inboxToStaffApi(id.value, note.value.trim() || undefined);
      createMessage.success('Đã chuyển cho sale');
      closeModal();
      emit('done', d);
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
