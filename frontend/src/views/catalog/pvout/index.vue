<template>
  <PageWrapper dense contentFullHeight fixedHeight>
    <BasicTable @register="registerTable">
      <template #toolbar>
        <Tooltip v-if="info" :title="info.source">
          <span class="text-xs text-gray-500">
            Bảng năm {{ info.year }} · nạp {{ formatToDateTime(info.importedAt) }}
            {{ info.importedBy ? 'bởi ' + info.importedBy : '' }}
          </span>
        </Tooltip>
        <Button type="primary" @click="openImportModal(true, { years })">Nạp bảng năm mới</Button>
      </template>
    </BasicTable>

    <PvoutImportModal @register="registerImportModal" @success="handleSaved" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { Tooltip } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { BasicTable, useTable } from '@/components/Table';
  import { PageWrapper } from '@/components/Page';
  import { useModal } from '@/components/Modal';
  import { formatToDateTime } from '@/utils/dateUtil';

  import PvoutImportModal from './PvoutImportModal.vue';
  import { columns, searchFormSchema } from './pvout.data';
  import { pvoutListApi, pvoutTableApi } from '@/api/catalog';
  import type { PvoutTableInfoModel } from '@/api/catalog/model/catalogModel';

  defineOptions({ name: 'CatalogPvout' });

  const [registerImportModal, { openModal: openImportModal }] = useModal();
  const years = ref<PvoutTableInfoModel[]>([]);
  const info = ref<PvoutTableInfoModel>();

  /**
   * Chưa chọn năm thì xem bảng mới nhất, cũng là bảng bot đang dùng nếu chưa có bảng năm nay.
   * Bảng một năm chỉ có 34 tỉnh, backend trả cả bảng nên chia trang ngay ở đây.
   */
  const loadPvout = async (params: Recordable) => {
    const { page = 1, size = 10 } = params;
    years.value = await pvoutListApi();
    const year: number | undefined = params.year ?? years.value[0]?.year;
    info.value = years.value.find((y) => y.year === year);
    if (!year) return { items: [], total: 0 };
    const { rows } = await pvoutTableApi(year);
    return { items: rows.slice((page - 1) * size, page * size), total: rows.length };
  };

  const [registerTable, { reload, getForm }] = useTable({
    title: 'Hệ số PV out theo tỉnh (kWh/kWp mỗi tháng)',
    titleHelpMessage: [
      'EVN công bố mỗi năm, thường vào tháng 1.',
      'Bot tính sản lượng và tiết kiệm theo bảng của năm hiện tại; năm chưa có bảng thì dùng bảng gần nhất.',
      'Từ 1/12 hệ thống nhắc quản trị nạp bảng năm sau.',
    ],
    api: loadPvout,
    rowKey: 'code',
    columns,
    formConfig: { labelWidth: 60, schemas: searchFormSchema },
    fetchSetting: { sizeField: 'size' },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    striped: false,
  });

  async function handleSaved(year: number) {
    await getForm().setFieldsValue({ year });
    reload();
  }
</script>
