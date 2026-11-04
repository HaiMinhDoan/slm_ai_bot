<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" :title="title" width="1100px">
    <template v-if="data">
      <Descriptions size="small" bordered :column="3" class="mb-3">
        <DescriptionsItem label="Giá gói (báo khách)">
          <b>{{ vnd(data.totalPrice) || 'Chưa có' }}</b>
        </DescriptionsItem>
        <DescriptionsItem label="Tổng vật tư lúc dựng gói">{{ vnd(data.lineTotal) }}</DescriptionsItem>
        <DescriptionsItem label="Tổng theo giá hiện hành">
          <template v-if="data.currentTotal != null">{{ vnd(data.currentTotal) }}</template>
          <TypographyText v-else type="warning">Có vật tư chưa có giá của miền này</TypographyText>
        </DescriptionsItem>
      </Descriptions>
      <Alert
        type="info"
        show-icon
        class="mb-3"
        message="Giá gói là giá trọn gói đặt riêng ở slm_v2, không bằng tổng giá vật tư. Bot luôn báo khách theo giá gói."
      />
      <BasicTable
        @register="registerTable"
        :dataSource="data.items"
        @row-click="(record) => openMaterial(true, { id: record.materialId })"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column?.dataIndex === 'name'">
            <div>{{ record.name }}</div>
            <TypographyText type="secondary" class="text-xs">
              {{ [record.brand, record.code].filter((x) => x).join(' · ') }}
            </TypographyText>
            <div v-if="record.description">
              <TypographyText type="secondary" class="text-xs">{{ record.description }}</TypographyText>
            </div>
          </template>
          <template v-else-if="column?.dataIndex === 'currentPrice'">
            <span v-if="record.currentPrice != null">{{ vnd(record.currentPrice) }}</span>
            <TypographyText v-else type="warning">Chưa có giá</TypographyText>
            <Tag
              v-if="record.currentPrice != null && record.salePrice != null && record.currentPrice !== record.salePrice"
              color="orange"
              class="ml-1"
            >
              đã đổi
            </Tag>
          </template>
          <template v-else-if="column?.dataIndex === 'customerVisible'">
            <Tag v-if="record.customerVisible" color="blue">Hiện cho khách</Tag>
            <Tag v-else>Phụ kiện ẩn</Tag>
          </template>
          <template v-else-if="column?.dataIndex === 'active'">
            <Tag :color="record.active ? 'green' : 'default'">{{ record.active ? 'Đang dùng' : 'Ngừng' }}</Tag>
          </template>
        </template>
      </BasicTable>
    </template>
    <MaterialDrawer @register="registerMaterial" />
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { Alert, Descriptions, Tag, Typography } from 'ant-design-vue';
  import { BasicDrawer, useDrawer, useDrawerInner } from '@/components/Drawer';
  import { BasicTable, useTable } from '@/components/Table';
  import { catalogPackageItemsApi } from '@/api/catalog';
  import type { PackageItemsModel } from '@/api/catalog/model/catalogModel';
  import MaterialDrawer from '../materials/MaterialDrawer.vue';
  import { BRANCH_LABELS, vnd } from '../labels';

  defineOptions({ name: 'CatalogPackageItemsDrawer' });

  /** Mở bằng openDrawer(true, { id }); bấm một dòng mở chi tiết vật tư */
  defineEmits(['register']);
  const DescriptionsItem = Descriptions.Item;
  const TypographyText = Typography.Text;

  const data = ref<PackageItemsModel>();
  const title = computed(() =>
    data.value
      ? `Gói ${data.value.packageId}: ${data.value.packageName} · ${BRANCH_LABELS[data.value.branchCode] ?? data.value.branchCode}`
      : 'Vật tư trong gói',
  );
  const [registerMaterial, { openDrawer: openMaterial }] = useDrawer();

  // Bảng nằm trong v-if nên nhận dữ liệu qua :dataSource
  const [registerTable] = useTable({
    title: 'Vật tư trong gói (bấm một dòng để xem chi tiết vật tư)',
    columns: [
      { title: 'Vật tư', dataIndex: 'name', align: 'left', ellipsis: false },
      { title: 'SL', dataIndex: 'quantity', width: 60 },
      { title: 'Đơn vị', dataIndex: 'unit', width: 80 },
      {
        title: 'Giá lúc dựng gói',
        dataIndex: 'salePrice',
        width: 140,
        align: 'right',
        customRender: ({ record }) => vnd(record.salePrice),
      },
      { title: 'Giá hiện hành', dataIndex: 'currentPrice', width: 170, align: 'right' },
      {
        title: 'Thành tiền',
        dataIndex: 'lineTotal',
        width: 140,
        align: 'right',
        customRender: ({ record }) =>
          record.salePrice == null ? '' : vnd((record.quantity ?? 0) * record.salePrice),
      },
      {
        title: 'Bảo hành',
        dataIndex: 'warrantyMonths',
        width: 90,
        customRender: ({ record }) => (record.warrantyMonths ? `${record.warrantyMonths} th` : ''),
      },
      { title: 'Hiển thị', dataIndex: 'customerVisible', width: 130 },
      { title: 'Trạng thái', dataIndex: 'active', width: 100 },
    ],
    rowKey: 'id',
    bordered: true,
    striped: false,
    canResize: false,
    pagination: { pageSize: 50 },
  });

  const [registerDrawer, { changeLoading }] = useDrawerInner(async (d) => {
    data.value = undefined;
    changeLoading(true);
    try {
      data.value = await catalogPackageItemsApi(d.id);
    } finally {
      changeLoading(false);
    }
  });
</script>
