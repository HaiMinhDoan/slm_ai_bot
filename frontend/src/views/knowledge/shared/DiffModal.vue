<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="state.title"
    width="900px"
    :showOkBtn="!!state.okText"
    :okText="state.okText"
    @ok="handleOk"
  >
    <Alert v-if="state.message" type="warning" show-icon class="mb-3" :message="state.message" />
    <div v-if="!state.diff" class="py-8 text-center text-gray-500">Hai bản giống nhau, không có khác biệt.</div>
    <div v-else class="diff-box">
      <CodeEditor :value="state.diff" :mode="MODE.DIFF" :config="CODE_CONFIG" readonly bordered />
    </div>
    <div v-if="state.diff" class="mt-2 text-xs text-gray-500">
      Dòng bắt đầu bằng <b>-</b> là bản cũ, <b>+</b> là bản mới. Không tính khác biệt chỉ về dòng trống, khoảng
      trắng (trình soạn tự chỉnh định dạng).
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { reactive } from 'vue';
  import { Alert } from 'ant-design-vue';
  import { BasicModal, useModalInner } from '@/components/Modal';
  import { CodeEditor, MODE } from '@/components/CodeEditor';
  import { CODE_CONFIG } from './codeEditor';

  defineOptions({ name: 'KnowledgeDiffModal' });

  /**
   * Mở bằng openModal(true, { title, diff, message?, okText? }); có okText thì hiện nút xác nhận (ví dụ "Ghi đè")
   * và phát sự kiện `ok`.
   */
  const emit = defineEmits(['ok', 'register']);
  const state = reactive({ title: 'So sánh', diff: '', message: '', okText: '' });

  const [registerModal, { closeModal }] = useModalInner((data) => {
    state.title = data?.title ?? 'So sánh';
    state.diff = data?.diff ?? '';
    state.message = data?.message ?? '';
    state.okText = data?.okText ?? '';
  });

  function handleOk() {
    closeModal();
    emit('ok');
  }
</script>

<style lang="less" scoped>
  .diff-box {
    height: 60vh;

    :deep(.CodeMirror) {
      height: 100%;
    }
  }
</style>
