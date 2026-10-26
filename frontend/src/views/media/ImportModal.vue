<template>
  <Modal
    :open="open"
    title="Nhập ảnh / video combo"
    width="920px"
    :footer="null"
    :maskClosable="false"
    @cancel="close"
  >
    <Alert
      type="info"
      show-icon
      class="mb-3"
      message="Dán danh sách combo dạng JSON (tạo từ bảng combo). Mỗi mục: tronGoiId, tronGoiIdHcm, title, driveUrl, facebookUrl, webUrl. Nên chạy thử trước: hệ thống kiểm tra link và tải thử ảnh nhưng không lưu gì."
    />
    <Textarea
      v-model:value="raw"
      :rows="8"
      placeholder='[{"tronGoiId": 98, "tronGoiIdHcm": 97, "title": "Hệ Hy-Brid 5 kWp 1 pha - Lưu trữ 5.12 kWh", "driveUrl": "https://drive.google.com/file/d/.../view", "facebookUrl": "https://www.facebook.com/share/r/.../", "webUrl": "https://slmsolar.vn/products/98"}]'
    />
    <div class="actions">
      <span v-if="parsed.error" class="error">{{ parsed.error }}</span>
      <span v-else-if="parsed.items.length">{{ parsed.items.length }} combo</span>
      <Space>
        <Button :loading="running && dryRun" :disabled="!canRun" @click="run(true)"
          >Chạy thử</Button
        >
        <Button
          type="primary"
          :loading="running && !dryRun"
          :disabled="!canRun"
          @click="run(false)"
        >
          Nhập thật
        </Button>
      </Space>
    </div>
    <Progress v-if="running" :percent="progress" size="small" class="mb-2" />

    <Table
      v-if="results.length"
      :dataSource="results"
      :columns="columns"
      :pagination="false"
      size="small"
      rowKey="tronGoiId"
      :scroll="{ y: 360 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'image' || column.key === 'video'">
          <Tag :color="OUTCOME_COLORS[record[column.key].outcome]">
            {{ OUTCOME_LABELS[record[column.key].outcome] }}
          </Tag>
          <div class="note">{{ record[column.key].note }}</div>
        </template>
        <template v-else-if="column.key === 'warnings'">
          <div v-for="w in record.warnings" :key="w" class="warn">{{ w }}</div>
        </template>
      </template>
    </Table>
    <div v-if="results.length" class="summary">{{ summary }}</div>
  </Modal>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { Alert, Button, Input, Modal, Progress, Space, Table, Tag } from 'ant-design-vue';
  import { MEDIA_IMPORT_BATCH, mediaImportPackagesApi } from '@/api/media';
  import type {
    ImportOutcome,
    PackageImportItem,
    PackageImportResult,
  } from '@/api/media/model/mediaModel';

  const Textarea = Input.TextArea;

  defineProps<{ open: boolean }>();
  const emit = defineEmits<{ (e: 'update:open', v: boolean): void; (e: 'imported'): void }>();

  const OUTCOME_LABELS: Record<ImportOutcome, string> = {
    CREATED: 'Đã nhập',
    EXISTS: 'Đã có',
    WOULD_CREATE: 'Sẽ nhập',
    SKIPPED: 'Bỏ qua',
    FAILED: 'Lỗi',
  };
  const OUTCOME_COLORS: Record<ImportOutcome, string> = {
    CREATED: 'green',
    EXISTS: 'blue',
    WOULD_CREATE: 'cyan',
    SKIPPED: 'default',
    FAILED: 'red',
  };
  const columns = [
    { title: 'Gói HN', dataIndex: 'tronGoiId', width: 70 },
    { title: 'Gói HCM', dataIndex: 'tronGoiIdHcm', width: 75 },
    { title: 'Ảnh (Drive)', key: 'image', width: 230 },
    { title: 'Video (Facebook)', key: 'video', width: 200 },
    { title: 'Cảnh báo', key: 'warnings' },
  ];

  const raw = ref('');
  const running = ref(false);
  const dryRun = ref(true);
  const progress = ref(0);
  const results = ref<PackageImportResult[]>([]);

  const parsed = computed<{ items: PackageImportItem[]; error?: string }>(() => {
    if (!raw.value.trim()) return { items: [] };
    try {
      const value = JSON.parse(raw.value);
      const items = Array.isArray(value) ? value : value?.items;
      if (!Array.isArray(items))
        return { items: [], error: 'Cần một mảng JSON hoặc {"items": [...]}' };
      const bad = items.findIndex((x) => !x || typeof x.tronGoiId !== 'number' || !x.title);
      if (bad >= 0)
        return { items: [], error: `Mục thứ ${bad + 1} thiếu tronGoiId (số) hoặc title` };
      return { items };
    } catch (e: any) {
      return { items: [], error: 'JSON không hợp lệ: ' + e.message };
    }
  });
  const canRun = computed(() => !running.value && parsed.value.items.length > 0);

  const summary = computed(() => {
    const count = (part: 'image' | 'video', o: ImportOutcome) =>
      results.value.filter((r) => r[part].outcome === o).length;
    return (
      `Ảnh: ${count('image', 'CREATED')} nhập, ${count('image', 'EXISTS')} đã có, ` +
      `${count('image', 'WOULD_CREATE')} sẽ nhập, ${count('image', 'FAILED')} lỗi. ` +
      `Video: ${count('video', 'CREATED')} nhập, ${count('video', 'EXISTS')} đã có, ` +
      `${count('video', 'WOULD_CREATE')} sẽ nhập, ${count('video', 'FAILED')} lỗi.`
    );
  });

  /** Chia lô 10 mục / lần gọi (giới hạn của backend) */
  async function run(isDryRun: boolean) {
    const items = parsed.value.items;
    running.value = true;
    dryRun.value = isDryRun;
    progress.value = 0;
    results.value = [];
    try {
      for (let i = 0; i < items.length; i += MEDIA_IMPORT_BATCH) {
        const batch = items.slice(i, i + MEDIA_IMPORT_BATCH);
        const res = await mediaImportPackagesApi(batch, isDryRun);
        results.value = [...results.value, ...res];
        progress.value = Math.round(((i + batch.length) / items.length) * 100);
      }
      if (!isDryRun) emit('imported');
    } finally {
      running.value = false;
    }
  }

  function close() {
    if (running.value) return;
    emit('update:open', false);
  }
</script>

<style lang="less" scoped>
  .actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin: 12px 0;
  }

  .error {
    color: #cf1322;
  }

  .note {
    color: #666;
    font-size: 12px;
  }

  .warn {
    color: #d46b08;
    font-size: 12px;
  }

  .summary {
    margin-top: 8px;
    font-weight: 500;
  }
</style>
