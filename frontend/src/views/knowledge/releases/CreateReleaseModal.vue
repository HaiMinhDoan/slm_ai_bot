<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="Tạo bản phát hành"
    width="1000px"
    okText="Tạo bản phát hành"
    :okButtonProps="{ disabled: !report?.ok || stale }"
    @ok="handleCreate"
  >
    <Alert
      type="info"
      show-icon
      class="mb-3"
      message="Chọn bản nháp đưa vào bản phát hành, bấm Kiểm tra, rồi Tạo. Bản phát hành gồm bản đang chạy cộng các bản nháp đã chọn."
    />
    <BasicTable @register="registerTable" :rowSelection="rowSelection" />
    <InputTextArea
      v-model:value="notes"
      class="mt-3"
      :rows="2"
      :maxlength="1000"
      placeholder="Ghi chú cho bản phát hành (đổi gì, vì sao)"
    />
    <div class="mt-3 flex items-center gap-2">
      <Button :loading="checking" @click="handleValidate">Kiểm tra</Button>
      <span v-if="stale && report" class="text-orange-500">Đã đổi lựa chọn, bấm Kiểm tra lại.</span>
    </div>
    <ReportView v-if="report" class="mt-3" :report="report" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { computed, ref, unref } from 'vue';
  import { Alert, Input } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { BasicModal, useModalInner } from '@/components/Modal';
  import { BasicTable, useTable } from '@/components/Table';
  import { useMessage } from '@/hooks/web/useMessage';
  import { releaseActivateApi, releaseCreateApi, releaseDraftsApi, releaseValidateApi } from '@/api/knowledge';
  import type { KnowledgeDraftRow, ReleaseReport } from '@/api/knowledge/model/knowledgeModel';
  import ReportView from './ReportView.vue';
  import { draftColumns } from './releases.data';

  defineOptions({ name: 'KnowledgeCreateReleaseModal' });

  const emit = defineEmits(['success', 'register']);
  const InputTextArea = Input.TextArea;
  const { createMessage, createConfirm } = useMessage();

  const drafts = ref<KnowledgeDraftRow[]>([]);
  const selected = ref<number[]>([]);
  const notes = ref('');
  const report = ref<ReleaseReport>();
  /** Lựa chọn lúc bấm Kiểm tra: đổi lựa chọn sau đó thì phải kiểm tra lại */
  const checkedFor = ref('');
  const checking = ref(false);
  const stale = computed(() => checkedFor.value !== [...selected.value].sort().join(','));

  const rowSelection = computed(
    () =>
      ({
        type: 'checkbox',
        selectedRowKeys: unref(selected),
        onChange: (keys: (string | number)[]) => {
          selected.value = keys as number[];
        },
      }) as any,
  );

  const [registerTable, { setTableData }] = useTable({
    columns: draftColumns,
    rowKey: 'versionId',
    bordered: true,
    striped: false,
    canResize: false,
    clickToRowSelect: false,
  });

  const [registerModal, { closeModal, setModalProps, changeLoading }] = useModalInner(async () => {
    report.value = undefined;
    notes.value = '';
    checkedFor.value = '';
    changeLoading(true);
    try {
      drafts.value = (await releaseDraftsApi()).filter((d) => !d.archived);
      setTableData(drafts.value);
      selected.value = drafts.value.map((d) => d.versionId);
    } finally {
      changeLoading(false);
    }
  });

  async function handleValidate() {
    checking.value = true;
    try {
      report.value = await releaseValidateApi({ versionIds: selected.value, notes: notes.value });
      checkedFor.value = [...selected.value].sort().join(',');
    } finally {
      checking.value = false;
    }
  }

  async function handleCreate() {
    setModalProps({ confirmLoading: true });
    try {
      const row = await releaseCreateApi({ versionIds: selected.value, notes: notes.value });
      closeModal();
      emit('success');
      createConfirm({
        iconType: 'success',
        title: `Đã tạo bản phát hành #${row.id}`,
        content: 'Kích hoạt ngay để bot dùng kiến thức mới? Có thể quay về bản cũ bất cứ lúc nào.',
        okText: 'Kích hoạt ngay',
        cancelText: 'Để sau',
        onOk: async () => {
          await releaseActivateApi(row.id);
          createMessage.success(`Bot đang dùng bản #${row.id}`);
          emit('success');
        },
      });
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
