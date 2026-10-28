<template>
  <div>
    <Alert
      type="info"
      show-icon
      class="mb-3"
      message="Hệ số PV out lý thuyết theo tỉnh (kWh/kWp mỗi tháng) do EVN công bố mỗi năm, thường vào tháng 1. Bot tính sản lượng và tiết kiệm theo bảng của năm hiện tại; năm chưa có bảng thì dùng bảng gần nhất. Từ 1/12 hệ thống nhắc quản trị nạp bảng năm sau."
    />
    <div class="toolbar mb-2">
      <Select
        v-model:value="year"
        :options="yearOptions"
        placeholder="Chọn năm"
        class="w-36"
        @change="loadTable"
      />
      <span v-if="info" class="muted">
        {{ info.source }} · nạp {{ formatToDateTime(info.importedAt) }}{{ info.importedBy ? ' bởi ' + info.importedBy : '' }}
      </span>
      <Button type="primary" class="ml-auto" @click="importOpen = true">Nạp bảng năm mới</Button>
    </div>
    <Table
      :dataSource="table?.rows ?? []"
      :columns="columns"
      :loading="loading"
      :pagination="false"
      size="small"
      rowKey="code"
      :scroll="{ x: 1100 }"
    />
    <PvoutImportModal v-model:open="importOpen" :years="years" @saved="onSaved" />
  </div>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue';
  import { Alert, Select, Table } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { pvoutListApi, pvoutTableApi } from '@/api/catalog';
  import type {
    PvoutProvinceRowModel,
    PvoutTableInfoModel,
    PvoutTableModel,
  } from '@/api/catalog/model/catalogModel';
  import { formatToDateTime } from '@/utils/dateUtil';
  import PvoutImportModal from './PvoutImportModal.vue';
  import { decimal } from './labels';

  const years = ref<PvoutTableInfoModel[]>([]);
  const year = ref<number>();
  const table = ref<PvoutTableModel>();
  const loading = ref(false);
  const importOpen = ref(false);

  const yearOptions = computed(() => years.value.map((y) => ({ value: y.year, label: 'Năm ' + y.year })));
  const info = computed(() => years.value.find((y) => y.year === year.value));

  const columns = [
    { title: 'Tỉnh', dataIndex: 'name', width: 140, fixed: 'left' as const },
    { title: 'Miền', dataIndex: 'region', width: 90 },
    ...Array.from({ length: 12 }, (_, i) => ({
      title: 'T' + (i + 1),
      key: 'm' + i,
      width: 56,
      customRender: ({ record }: { record: PvoutProvinceRowModel }) => decimal(record.monthly[i], 1),
    })),
    {
      title: 'Cả năm',
      dataIndex: 'annual',
      width: 80,
      customRender: ({ record }: { record: PvoutProvinceRowModel }) => decimal(record.annual, 0),
    },
  ];

  async function loadYears() {
    years.value = await pvoutListApi();
    if (!year.value && years.value.length) year.value = years.value[0].year;
  }

  async function loadTable() {
    if (!year.value) return;
    loading.value = true;
    try {
      table.value = await pvoutTableApi(year.value);
    } finally {
      loading.value = false;
    }
  }

  async function onSaved(saved: number) {
    year.value = saved;
    await loadYears();
    await loadTable();
  }

  onMounted(async () => {
    await loadYears();
    await loadTable();
  });
</script>

<style lang="less" scoped>
  .toolbar {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .muted {
    color: #888;
    font-size: 12px;
  }
</style>
