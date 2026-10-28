<template>
  <div>
    <Card size="small" class="mb-3">
      <div class="status">
        <div>
          <template v-if="status && !status.configured">
            <Tag color="red">Chưa cấu hình</Tag>
            Thiếu SLM_V2_DB_URL / SLM_V2_DB_USERNAME / SLM_V2_DB_PASSWORD trong .env của server: bot chưa có
            công cụ báo giá.
          </template>
          <template v-else-if="last">
            <Tag :color="last.status === 'SUCCESS' ? 'green' : last.status === 'FAILED' ? 'red' : 'blue'">
              {{ STATUS_LABELS[last.status] }}
            </Tag>
            Lần đồng bộ gần nhất {{ formatToDateTime(last.startedAt) }}
            <span v-if="status?.lastSuccess && status.lastSuccess.id !== last.id">
              · thành công gần nhất {{ formatToDateTime(status.lastSuccess.startedAt) }}
            </span>
            <span v-if="status?.version" class="muted"> · phiên bản {{ status.version.slice(0, 12) }}</span>
            <div v-if="last.status === 'FAILED'" class="error">{{ last.error }}</div>
          </template>
          <template v-else>Chưa đồng bộ lần nào. Hệ thống tự đồng bộ 10 phút một lần.</template>
        </div>
        <Space>
          <Button @click="load" :loading="loading">Tải lại</Button>
          <Button type="primary" :loading="syncing" :disabled="status && !status.configured" @click="syncNow">
            Đồng bộ ngay
          </Button>
        </Space>
      </div>
      <div v-if="warningSummary.length" class="warnings">
        <div v-for="w in warningSummary" :key="w.code">
          <Tag :color="w.blocking ? 'red' : 'orange'">{{ w.label }}</Tag>
          gói / vật tư {{ w.ids.join(', ') }}
          <span v-if="w.blocking" class="error"> · không được gợi ý cho khách tới khi sửa ở slm_v2</span>
        </div>
      </div>
    </Card>

    <div class="filters mb-2">
      <Select v-model:value="branch" :options="branchOptions" allowClear placeholder="Cơ sở giá" class="w-44" />
      <Select v-model:value="type" :options="typeOptions" allowClear placeholder="Loại hệ" class="w-44" />
      <Select v-model:value="phases" :options="phaseOptions" allowClear placeholder="Số pha" class="w-28" />
      <Checkbox v-model:checked="onlyIssues">Chỉ gói có cảnh báo / ngừng bán</Checkbox>
      <span class="muted">{{ filtered.length }} / {{ packages.length }} gói</span>
    </div>

    <Table
      :dataSource="filtered"
      :columns="columns"
      :loading="loading"
      :pagination="{ pageSize: 50, showSizeChanger: false }"
      size="small"
      rowKey="id"
      :scroll="{ x: 1200 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'name'">
          <div>{{ record.name }}</div>
          <div class="muted">{{ record.familyName }}</div>
        </template>
        <template v-else-if="column.key === 'type'">
          {{ record.systemType ? SYSTEM_TYPE_LABELS[record.systemType as SystemType] : '?' }}
        </template>
        <template v-else-if="column.key === 'panels'">
          <span v-if="record.panelCount">{{ record.panelCount }} × {{ decimal(record.panelWp, 0) }} Wp</span>
        </template>
        <template v-else-if="column.key === 'status'">
          <Tag v-if="!record.active">Ngừng bán</Tag>
          <Tag v-else-if="record.recommendable" color="green">Được gợi ý</Tag>
          <Tag v-else color="red">Bị chặn</Tag>
          <Tag v-if="record.bestSeller" color="gold">Bán chạy</Tag>
        </template>
        <template v-else-if="column.key === 'warnings'">
          <Tag
            v-for="w in record.warnings"
            :key="w"
            :color="labels[w]?.blocking ? 'red' : 'orange'"
            class="mb-1"
          >
            {{ labels[w]?.label ?? w }}
          </Tag>
        </template>
      </template>
    </Table>
  </div>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue';
  import { Card, Checkbox, Select, Space, Table, Tag } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { useMessage } from '@/hooks/web/useMessage';
  import {
    catalogPackagesApi,
    catalogStatusApi,
    catalogSyncApi,
    catalogWarningLabelsApi,
  } from '@/api/catalog';
  import type {
    CatalogPackageModel,
    CatalogStatusModel,
    SystemType,
    WarningLabels,
  } from '@/api/catalog/model/catalogModel';
  import { formatToDateTime } from '@/utils/dateUtil';
  import { BRANCH_LABELS, SYSTEM_TYPE_LABELS, decimal, millions, options } from './labels';

  const STATUS_LABELS: Record<string, string> = {
    SUCCESS: 'Thành công',
    FAILED: 'Lỗi',
    RUNNING: 'Đang chạy',
  };

  const { createMessage } = useMessage();
  const status = ref<CatalogStatusModel>();
  const packages = ref<CatalogPackageModel[]>([]);
  const labels = ref<WarningLabels>({});
  const loading = ref(false);
  const syncing = ref(false);

  const branch = ref<string>();
  const type = ref<SystemType>();
  const phases = ref<number>();
  const onlyIssues = ref(false);

  const branchOptions = Object.entries(BRANCH_LABELS).map(([value, label]) => ({ value, label }));
  const typeOptions = options(SYSTEM_TYPE_LABELS);
  const phaseOptions = [
    { value: 1, label: '1 pha' },
    { value: 3, label: '3 pha' },
  ];

  const last = computed(() => status.value?.runs[0]);

  const warningSummary = computed(() => {
    const w = status.value?.lastSuccess?.warnings ?? {};
    return Object.entries(w).map(([code, ids]) => ({
      code,
      ids,
      label: labels.value[code]?.label ?? code,
      blocking: !!labels.value[code]?.blocking,
    }));
  });

  const filtered = computed(() =>
    packages.value.filter(
      (p) =>
        (!branch.value || p.branchCode === branch.value) &&
        (!type.value || p.systemType === type.value) &&
        (!phases.value || p.phases === phases.value) &&
        (!onlyIssues.value || p.warnings.length > 0 || !p.active || !p.recommendable),
    ),
  );

  const columns = [
    { title: 'Mã', dataIndex: 'id', width: 60, fixed: 'left' as const },
    { title: 'Cơ sở', dataIndex: 'branchCode', width: 64 },
    { title: 'Tên gói', key: 'name', width: 320 },
    { title: 'Loại', key: 'type', width: 130 },
    {
      title: 'Pha',
      dataIndex: 'phases',
      width: 50,
    },
    {
      title: 'kWp',
      dataIndex: 'kwp',
      width: 70,
      customRender: ({ record }: { record: CatalogPackageModel }) => decimal(record.kwp),
    },
    { title: 'Tấm pin', key: 'panels', width: 110 },
    {
      title: 'Pin (kWh)',
      dataIndex: 'storageKwh',
      width: 80,
      customRender: ({ record }: { record: CatalogPackageModel }) =>
        record.storageKwh ? decimal(record.storageKwh) : '',
    },
    {
      title: 'Giá',
      dataIndex: 'totalPrice',
      width: 90,
      customRender: ({ record }: { record: CatalogPackageModel }) => millions(record.totalPrice),
    },
    { title: 'Trạng thái', key: 'status', width: 170 },
    { title: 'Cảnh báo dữ liệu', key: 'warnings' },
  ];

  async function load() {
    loading.value = true;
    try {
      const [s, p, l] = await Promise.all([
        catalogStatusApi(),
        catalogPackagesApi(),
        catalogWarningLabelsApi(),
      ]);
      status.value = s;
      packages.value = p;
      labels.value = l;
    } finally {
      loading.value = false;
    }
  }

  async function syncNow() {
    syncing.value = true;
    try {
      const run = await catalogSyncApi();
      if (run.status === 'SUCCESS') {
        createMessage.success(
          `Đồng bộ xong: thêm ${run.inserted ?? 0}, sửa ${run.updated ?? 0}, ngừng ${run.deactivated ?? 0}`,
        );
      } else {
        createMessage.error('Đồng bộ lỗi: ' + (run.error ?? ''));
      }
      await load();
    } finally {
      syncing.value = false;
    }
  }

  onMounted(load);
</script>

<style lang="less" scoped>
  .status {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
  }

  .warnings {
    margin-top: 8px;
    font-size: 12px;
  }

  .filters {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
  }

  .muted {
    color: #888;
    font-size: 12px;
  }

  .error {
    color: #cf1322;
  }
</style>
