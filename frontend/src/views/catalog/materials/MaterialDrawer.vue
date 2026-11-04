<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" :title="title" width="960px">
    <template v-if="detail">
      <div v-if="detail.images.length" class="flex flex-wrap gap-2 mb-4">
        <CatalogImage v-for="img in detail.images" :key="img.id" :fileId="img.fileId" :size="96" />
      </div>

      <Descriptions size="small" bordered :column="2">
        <DescriptionsItem label="Mã">{{ m.code }}</DescriptionsItem>
        <DescriptionsItem label="Trạng thái">
          <Tag :color="m.active ? 'green' : 'default'">{{ m.active ? 'Đang dùng' : 'Ngừng ở slm_v2' }}</Tag>
        </DescriptionsItem>
        <DescriptionsItem label="Nhóm">{{ m.groupName || m.groupCode }}</DescriptionsItem>
        <DescriptionsItem label="Hãng">{{ m.brand }}</DescriptionsItem>
        <DescriptionsItem label="Đơn vị">{{ m.unit }}</DescriptionsItem>
        <DescriptionsItem label="Bảo hành">
          {{ m.warrantyMonths ? `${m.warrantyMonths} tháng` : '' }}
        </DescriptionsItem>
        <DescriptionsItem v-for="b in PRICE_BRANCHES" :key="b" :label="`Giá bán ${BRANCH_LABELS[b] ?? b}`">
          <b>{{ vnd(m.salePrices?.[b]) || 'Chưa có giá' }}</b>
          <TypographyText v-if="m.priceDates?.[b]" type="secondary" class="text-xs ml-1">
            từ {{ formatToDate(m.priceDates[b]) }}
          </TypographyText>
        </DescriptionsItem>
        <DescriptionsItem label="Đồng bộ lúc">
          {{ detail.syncedAt ? formatToDateTime(detail.syncedAt) : '' }}
        </DescriptionsItem>
        <DescriptionsItem v-if="detail.quoteDescription" label="Mô tả báo giá" :span="2">
          <TypographyParagraph class="mb-0 whitespace-pre-wrap">{{ detail.quoteDescription }}</TypographyParagraph>
        </DescriptionsItem>
      </Descriptions>

      <Descriptions v-if="specs.length" title="Thông số" size="small" bordered :column="2" class="mt-4">
        <DescriptionsItem v-for="s in specs" :key="s.key" :label="s.label">{{ s.value }}</DescriptionsItem>
      </Descriptions>

      <BasicTable @register="registerPrices" :dataSource="detail.prices" class="mt-4" />
      <BasicTable @register="registerPackages" :dataSource="detail.packages" class="mt-4">
        <template #bodyCell="{ column, record }">
          <template v-if="column?.dataIndex === 'packageName'">
            <div>{{ record.packageName }}</div>
            <TypographyText type="secondary" class="text-xs">Gói {{ record.packageId }}</TypographyText>
          </template>
          <template v-else-if="column?.dataIndex === 'customerVisible'">
            <Tag v-if="record.customerVisible" color="blue">Hiện cho khách</Tag>
            <Tag v-else>Phụ kiện ẩn</Tag>
          </template>
          <template v-else-if="column?.dataIndex === 'itemActive'">
            <Tag v-if="!record.packageActive">Gói ngừng</Tag>
            <Tag v-else-if="!record.itemActive">Dòng ngừng</Tag>
            <Tag v-else color="green">Đang dùng</Tag>
          </template>
        </template>
      </BasicTable>
    </template>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { Descriptions, Tag, Typography } from 'ant-design-vue';
  import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
  import { BasicTable, useTable } from '@/components/Table';
  import { formatToDate, formatToDateTime } from '@/utils/dateUtil';
  import { catalogMaterialApi } from '@/api/catalog';
  import type { MaterialDetailModel, MaterialModel } from '@/api/catalog/model/catalogModel';
  import CatalogImage from '../CatalogImage.vue';
  import { BRANCH_LABELS, PRICE_BRANCHES, vnd } from '../labels';

  defineOptions({ name: 'CatalogMaterialDrawer' });

  /** Mở bằng openDrawer(true, { id }) */
  defineEmits(['register']);
  const DescriptionsItem = Descriptions.Item;
  const TypographyText = Typography.Text;
  const TypographyParagraph = Typography.Paragraph;

  const detail = ref<MaterialDetailModel>();
  const m = computed(() => detail.value?.material ?? ({} as MaterialModel));
  const title = computed(() => (detail.value ? detail.value.material.name : 'Vật tư'));
  const specs = computed(() =>
    Object.entries(detail.value?.specs ?? {})
      .filter(([, s]) => s && s.value !== undefined && s.value !== null && s.value !== '')
      .map(([key, s]) => ({
        key,
        label: s.ten || key,
        value: `${typeof s.value === 'number' ? s.value.toLocaleString('vi-VN') : s.value}${s.unit ? ' ' + s.unit : ''}`,
      })),
  );

  // Bảng nằm trong v-if nên nhận dữ liệu qua :dataSource
  const [registerPrices] = useTable({
    title: 'Lịch sử giá bán (mới nhất trước)',
    columns: [
      {
        title: 'Hiệu lực từ',
        dataIndex: 'effectiveFrom',
        width: 170,
        customRender: ({ record }) => (record.effectiveFrom ? formatToDateTime(record.effectiveFrom) : ''),
      },
      ...PRICE_BRANCHES.map((b) => ({
        title: `Giá bán ${BRANCH_LABELS[b] ?? b}`,
        dataIndex: `price_${b}`,
        align: 'right' as const,
        customRender: ({ record }) => vnd(record.salePrices?.[b]),
      })),
      {
        title: 'Trạng thái',
        dataIndex: 'active',
        width: 120,
        customRender: ({ record }) => (record.active ? 'Đang dùng' : 'Đã xoá ở slm_v2'),
      },
    ],
    rowKey: 'id',
    bordered: true,
    striped: false,
    canResize: false,
  });

  const [registerPackages] = useTable({
    title: 'Các gói dùng vật tư này',
    columns: [
      { title: 'Gói', dataIndex: 'packageName', align: 'left', ellipsis: false },
      {
        title: 'Miền',
        dataIndex: 'branchCode',
        width: 130,
        customRender: ({ record }) => BRANCH_LABELS[record.branchCode] ?? record.branchCode,
      },
      { title: 'Số lượng', dataIndex: 'quantity', width: 90 },
      {
        title: 'Giá lúc dựng gói',
        dataIndex: 'salePrice',
        width: 140,
        align: 'right',
        customRender: ({ record }) => vnd(record.salePrice),
      },
      {
        title: 'Giá gói',
        dataIndex: 'totalPrice',
        width: 140,
        align: 'right',
        customRender: ({ record }) => vnd(record.totalPrice),
      },
      { title: 'Hiển thị', dataIndex: 'customerVisible', width: 130 },
      { title: 'Trạng thái', dataIndex: 'itemActive', width: 110 },
    ],
    rowKey: 'itemId',
    bordered: true,
    striped: false,
    canResize: false,
  });

  const [registerDrawer, { changeLoading }] = useDrawerInner(async (data) => {
    detail.value = undefined;
    changeLoading(true);
    try {
      detail.value = await catalogMaterialApi(data.id);
    } finally {
      changeLoading(false);
    }
  });
</script>
