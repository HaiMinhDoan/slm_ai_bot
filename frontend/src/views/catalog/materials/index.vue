<template>
  <PageWrapper dense contentFullHeight fixedHeight>
    <BasicTable @register="registerTable" @row-click="(record) => openDrawer(true, { id: record.id })">
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'imageFileId'">
          <CatalogImage :fileId="record.imageFileId" :size="40" />
        </template>
        <template v-else-if="column.dataIndex === 'name'">
          <div>
            <b>{{ record.name }}</b>
            <Tag v-if="record.main" color="blue" class="ml-1">Thiết bị chính</Tag>
          </div>
          <TypographyText type="secondary" class="text-xs">{{ record.code }}</TypographyText>
          <div v-if="record.warnings?.length">
            <Tag v-for="w in record.warnings" :key="w" color="orange">{{ labels[w]?.label ?? w }}</Tag>
          </div>
        </template>
        <template v-else-if="String(column.dataIndex).startsWith(PRICE_PREFIX)">
          <div>{{ vnd(record.salePrices?.[String(column.dataIndex).slice(PRICE_PREFIX.length)]) }}</div>
          <TypographyText
            v-if="record.northPriced?.includes(String(column.dataIndex).slice(PRICE_PREFIX.length))"
            type="secondary"
            class="text-xs"
          >
            theo giá miền Bắc
          </TypographyText>
        </template>
        <template v-else-if="column.dataIndex === 'warrantyMonths'">
          {{ record.warrantyMonths ? `${record.warrantyMonths} tháng` : '' }}
        </template>
        <template v-else-if="column.dataIndex === 'active'">
          <Tag :color="record.active ? 'green' : 'default'">{{ record.active ? 'Đang dùng' : 'Ngừng' }}</Tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <TableAction
            :actions="[
              {
                icon: 'ant-design:eye-outlined',
                tooltip: 'Chi tiết, lịch sử giá, gói dùng',
                onClick: () => openDrawer(true, { id: record.id }),
              },
            ]"
          />
        </template>
      </template>
    </BasicTable>

    <MaterialDrawer @register="registerDrawer" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { Tag, Typography } from 'ant-design-vue';
  import { BasicTable, TableAction, useTable } from '@/components/Table';
  import { PageWrapper } from '@/components/Page';
  import { useDrawer } from '@/components/Drawer';
  import { catalogMaterialsApi, catalogWarningLabelsApi } from '@/api/catalog';
  import type { WarningLabels } from '@/api/catalog/model/catalogModel';
  import CatalogImage from '../CatalogImage.vue';
  import { vnd } from '../labels';
  import MaterialDrawer from './MaterialDrawer.vue';
  import { columns, PRICE_PREFIX, searchFormSchema } from './materials.data';

  defineOptions({ name: 'CatalogMaterials' });

  const TypographyText = Typography.Text;
  const [registerDrawer, { openDrawer }] = useDrawer();
  const labels = ref<WarningLabels>({});

  // vben đánh trang từ 1, backend từ 0
  const loadMaterials = async (params: Recordable) => {
    const { page = 1, size = 10, ...filters } = params;
    if (!Object.keys(labels.value).length) {
      labels.value = await catalogWarningLabelsApi();
    }
    return catalogMaterialsApi({ ...filters, page: page - 1, size });
  };

  const [registerTable] = useTable({
    title: 'Vật tư đồng bộ từ slm_v2',
    titleHelpMessage: [
      'Đọc từ slm_v2 (chỉ đọc), tự đồng bộ 10 phút một lần cùng với gói. Không đồng bộ giá nhập, biên lợi nhuận.',
      'Giá bán là lần ghi giá mới nhất ở slm_v2; lần đó không có giá miền Nam thì miền Nam lấy giá miền Bắc, như slm_v2.',
      'Giá gói báo cho khách vẫn là giá trọn gói, không cộng từ vật tư.',
    ],
    api: loadMaterials,
    rowKey: 'id',
    columns,
    formConfig: { labelWidth: 80, schemas: searchFormSchema, autoSubmitOnEnter: true },
    // Backend nhận `size`, vben mặc định gửi `pageSize`
    fetchSetting: { sizeField: 'size' },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    striped: false,
    actionColumn: { width: 70, title: 'Xem', dataIndex: 'action', fixed: 'right' },
  });
</script>
