<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="Nạp bảng PV out EVN"
    width="960px"
    okText="Lưu bảng"
    :okButtonProps="{ disabled: !canSave }"
    @ok="handleSave"
  >
    <template #insertFooter>
      <Button :loading="checking" @click="handleCheck">Kiểm tra</Button>
    </template>

    <Alert
      type="info"
      show-icon
      class="mb-4"
      message="Dán bảng từ công văn EVN"
      description="Mỗi dòng một tỉnh: tên tỉnh (34 tỉnh mới) rồi 12 số từ tháng 1 đến tháng 12, cách nhau bằng dấu cách, tab hoặc |. Bấm Kiểm tra để xem lỗi và mức chênh so với năm trước; đúng rồi mới Lưu bảng."
    />

    <BasicForm @register="registerForm" @field-value-change="checked = false" />

    <Alert
      v-if="parseErrors.length"
      type="error"
      show-icon
      class="mb-3"
      :message="`Có ${parseErrors.length} dòng không đọc được`"
    >
      <template #description>
        <div v-for="e in parseErrors" :key="e">{{ e }}</div>
      </template>
    </Alert>

    <template v-if="result">
      <Alert
        v-if="result.errors.length"
        type="error"
        show-icon
        class="mb-3"
        :message="`Có ${result.errors.length} lỗi, chưa lưu`"
      >
        <template #description>
          <div v-for="e in result.errors" :key="e">{{ e }}</div>
        </template>
      </Alert>
      <Alert
        v-else
        type="success"
        show-icon
        class="mb-3"
        :message="`Bảng hợp lệ${result.comparedYear ? ', so với năm ' + result.comparedYear : ''}. Bấm Lưu bảng để dùng.`"
      />
      <Table
        :columns="previewColumns"
        :data-source="result.rows"
        :pagination="false"
        :scroll="{ y: 320 }"
        rowKey="code"
        size="small"
        bordered
      />
    </template>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  // Dự án không đăng ký sẵn component Ant Design: import ngay trong file dùng nó
  import { Alert, Button, Table } from 'ant-design-vue';
  import { BasicModal, useModalInner } from '@/components/Modal';
  import { BasicForm, useForm } from '@/components/Form';
  import { useMessage } from '@/hooks/web/useMessage';

  import { importFormSchema, parsePvoutText, previewColumns } from './pvout.data';
  import { pvoutImportApi } from '@/api/catalog';
  import type {
    PvoutImportResultModel,
    PvoutTableInfoModel,
  } from '@/api/catalog/model/catalogModel';

  defineOptions({ name: 'PvoutImportModal' });

  const emit = defineEmits(['success', 'register']);
  const { createMessage, createConfirm } = useMessage();

  const years = ref<PvoutTableInfoModel[]>([]);
  const parseErrors = ref<string[]>([]);
  const result = ref<PvoutImportResultModel>();
  const checking = ref(false);
  /** Đã Kiểm tra đúng nội dung đang có trong form; sửa bất kỳ ô nào thì phải kiểm tra lại */
  const checked = ref(false);

  const canSave = computed(() => checked.value && !!result.value && !result.value.errors.length);

  const [registerForm, { resetFields, validate }] = useForm({
    labelWidth: 100,
    schemas: importFormSchema,
    showActionButtonGroup: false,
  });

  const [registerModal, { changeOkLoading, closeModal }] = useModalInner(async (data) => {
    await resetFields();
    years.value = data?.years ?? [];
    parseErrors.value = [];
    result.value = undefined;
    checked.value = false;
  });

  async function buildRequest(dryRun: boolean) {
    const values = await validate();
    const parsed = parsePvoutText(values.raw ?? '');
    parseErrors.value = parsed.errors;
    if (parsed.errors.length || !parsed.rows.length) return undefined;
    return {
      year: values.year as number,
      source: (values.source as string).trim(),
      note: (values.note as string | undefined)?.trim() || undefined,
      rows: parsed.rows,
      dryRun,
    };
  }

  /** dryRun=true: backend kiểm tra đủ 34 tỉnh × 12 tháng và so với năm trước nhưng KHÔNG ghi gì */
  async function handleCheck() {
    const request = await buildRequest(true);
    if (!request) {
      result.value = undefined;
      return;
    }
    checking.value = true;
    try {
      result.value = await pvoutImportApi(request);
      checked.value = true;
    } finally {
      checking.value = false;
    }
  }

  async function handleSave() {
    const request = await buildRequest(false);
    if (!request) return;
    if (years.value.some((y) => y.year === request.year)) {
      createConfirm({
        iconType: 'warning',
        title: 'Thay bảng đã có',
        content: `Đã có bảng năm ${request.year}. Thay bằng bảng mới?`,
        onOk: () => save(request),
      });
      return;
    }
    await save(request);
  }

  async function save(request: NonNullable<Awaited<ReturnType<typeof buildRequest>>>) {
    changeOkLoading(true);
    try {
      const res = await pvoutImportApi(request);
      if (!res.saved) {
        result.value = res;
        return;
      }
      createMessage.success(`Đã lưu bảng PV out năm ${res.year}`);
      closeModal();
      emit('success', res.year);
    } finally {
      changeOkLoading(false);
    }
  }
</script>
