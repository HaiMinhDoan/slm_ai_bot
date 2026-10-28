<template>
  <Modal
    :open="open"
    title="Nạp bảng PV out EVN"
    width="900px"
    :footer="null"
    :maskClosable="false"
    @cancel="emit('update:open', false)"
  >
    <Alert
      type="info"
      show-icon
      class="mb-3"
      message="Dán bảng từ công văn EVN: mỗi dòng một tỉnh, tên tỉnh (34 tỉnh mới) rồi 12 số từ tháng 1 đến tháng 12, cách nhau bằng dấu cách, tab hoặc |. Bấm Kiểm tra để xem lỗi và mức chênh so với năm trước, đúng rồi mới Lưu."
    />
    <div class="row mb-2">
      <InputNumber v-model:value="year" :min="2020" :max="2100" addonBefore="Năm áp dụng" class="w-48" />
      <Input v-model:value="source" placeholder="Văn bản công bố, ví dụ: EVN, công văn số .../EVN-KDMBĐ ngày ..." />
    </div>
    <Input v-model:value="note" placeholder="Ghi chú (không bắt buộc)" class="mb-2" />
    <Textarea
      v-model:value="raw"
      :rows="10"
      placeholder="Tuyên Quang|49 37 52 68 108 109 120 112 107 93 80 63"
    />
    <div class="actions">
      <span :class="{ error: parsed.errors.length }">
        {{ parsed.errors.length ? parsed.errors[0] : parsed.rows.length + ' dòng' }}
      </span>
      <Space>
        <Button :loading="running" :disabled="!canRun" @click="run(true)">Kiểm tra</Button>
        <Button type="primary" :loading="running" :disabled="!canSave" @click="run(false)">Lưu bảng</Button>
      </Space>
    </div>

    <template v-if="result">
      <Alert
        v-if="result.errors.length"
        type="error"
        class="mb-2"
        :message="'Có ' + result.errors.length + ' lỗi, chưa lưu'"
      >
        <template #description>
          <div v-for="e in result.errors" :key="e">{{ e }}</div>
        </template>
      </Alert>
      <Alert v-else-if="result.saved" type="success" class="mb-2" :message="'Đã lưu bảng năm ' + result.year" />
      <Alert
        v-else
        type="success"
        class="mb-2"
        :message="'Bảng hợp lệ' + (result.comparedYear ? ', so với năm ' + result.comparedYear : '') + '. Bấm Lưu bảng để dùng.'"
      />
      <Table
        :dataSource="result.rows"
        :columns="previewColumns"
        :pagination="false"
        size="small"
        rowKey="code"
        :scroll="{ y: 320 }"
      />
    </template>
  </Modal>
</template>

<script lang="ts" setup>
  import { computed, ref, watch } from 'vue';
  import { Alert, Input, InputNumber, Modal, Space, Table } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { pvoutImportApi } from '@/api/catalog';
  import type { PvoutImportResultModel, PvoutTableInfoModel } from '@/api/catalog/model/catalogModel';
  import { decimal } from './labels';

  const Textarea = Input.TextArea;

  const props = defineProps<{ open: boolean; years: PvoutTableInfoModel[] }>();
  const emit = defineEmits<{ (e: 'update:open', v: boolean): void; (e: 'saved', year: number): void }>();

  const year = ref<number>(new Date().getFullYear() + 1);
  const source = ref('');
  const note = ref('');
  const raw = ref('');
  const running = ref(false);
  const result = ref<PvoutImportResultModel>();
  const checkedKey = ref('');

  watch(
    () => props.open,
    (o) => {
      if (o) result.value = undefined;
    },
  );

  /** "Tên tỉnh|12 số" hoặc "Tên tỉnh  12 số": tên là phần chữ đầu dòng, sau đó là các số */
  const parsed = computed(() => {
    const rows: { province: string; monthly: number[] }[] = [];
    const errors: string[] = [];
    raw.value
      .split(/\r?\n/)
      .map((l) => l.trim())
      .filter((l) => l)
      .forEach((line, i) => {
        const m = line.match(/^([^\d|\t;]+?)[\s|\t;]+([\d\s.,|\t;]+)$/);
        if (!m) {
          errors.push(`Dòng ${i + 1}: không đọc được "${line.slice(0, 40)}"`);
          return;
        }
        const monthly = m[2]
          .split(/[\s|\t;]+/)
          .filter((v) => v)
          .map((v) => Number(v.replace(',', '.')));
        if (monthly.length !== 12 || monthly.some((v) => Number.isNaN(v))) {
          errors.push(`Dòng ${i + 1} (${m[1].trim()}): cần đúng 12 số`);
          return;
        }
        rows.push({ province: m[1].trim(), monthly });
      });
    return { rows, errors };
  });

  const key = computed(() => JSON.stringify([year.value, source.value, raw.value]));
  const canRun = computed(
    () => !running.value && !!year.value && !!source.value.trim() && parsed.value.rows.length > 0,
  );
  /** Chỉ lưu sau khi đã kiểm tra đúng nội dung này, không lỗi */
  const canSave = computed(
    () => canRun.value && !!result.value && !result.value.errors.length && checkedKey.value === key.value,
  );
  const replacing = computed(() => props.years.some((y) => y.year === year.value));

  const previewColumns = [
    { title: 'Tỉnh', dataIndex: 'name', width: 140 },
    {
      title: 'Cả năm',
      dataIndex: 'annual',
      width: 90,
      customRender: ({ record }: { record: Recordable }) => decimal(record.annual, 0),
    },
    {
      title: 'Năm trước',
      dataIndex: 'previousAnnual',
      width: 90,
      customRender: ({ record }: { record: Recordable }) => decimal(record.previousAnnual, 0),
    },
    {
      title: 'Chênh',
      dataIndex: 'changePercent',
      width: 80,
      customRender: ({ record }: { record: Recordable }) =>
        record.changePercent == null ? '' : (record.changePercent > 0 ? '+' : '') + decimal(record.changePercent, 1) + '%',
    },
    {
      title: '12 tháng',
      key: 'monthly',
      customRender: ({ record }: { record: Recordable }) =>
        (record.monthly as number[]).map((v) => decimal(v, 1)).join(' · '),
    },
  ];

  async function run(dryRun: boolean) {
    if (!dryRun && replacing.value && !window.confirm(`Đã có bảng năm ${year.value}. Thay bằng bảng mới?`)) {
      return;
    }
    running.value = true;
    try {
      result.value = await pvoutImportApi({
        year: year.value,
        source: source.value.trim(),
        note: note.value.trim() || undefined,
        rows: parsed.value.rows,
        dryRun,
      });
      checkedKey.value = key.value;
      if (result.value.saved) emit('saved', result.value.year);
    } finally {
      running.value = false;
    }
  }
</script>

<style lang="less" scoped>
  .row {
    display: flex;
    gap: 8px;
  }

  .actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 12px 0;
  }

  .error {
    color: #cf1322;
  }
</style>
