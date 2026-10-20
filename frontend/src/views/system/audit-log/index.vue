<template>
  <PageWrapper dense contentFullHeight fixedHeight>
    <BasicTable @register="registerTable">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action-col'">
          <Button size="small" @click="showDetail(record)">Xem trước/sau</Button>
        </template>
      </template>
    </BasicTable>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { Button } from '@/components/Button';
  import { BasicTable, useTable } from '@/components/Table';
  import { PageWrapper } from '@/components/Page';
  import { Modal } from 'ant-design-vue';
  import { h } from 'vue';

  import { columns, searchFormSchema } from './audit-log.data';
  import { auditLogFilterApi } from '@/api/system/auditLog';

  defineOptions({ name: 'AuditLogViewer' });

  const loadLogs = async (params: Recordable) => {
    const { page = 1, size = 20, action, targetType } = params;
    return auditLogFilterApi({ action, targetType, page: page - 1, size });
  };

  const [registerTable] = useTable({
    title: 'Nhật ký thao tác',
    api: loadLogs,
    rowKey: 'id',
    columns: [
      ...columns,
      { title: 'Chi tiết', dataIndex: 'action-col', width: 120, key: 'action-col' },
    ],
    formConfig: { labelWidth: 100, schemas: searchFormSchema },
    useSearchForm: true,
    fetchSetting: { sizeField: 'size' },
    showTableSetting: true,
    bordered: true,
  });

  function showDetail(record: Recordable<any>) {
    Modal.info({
      title: `Chi tiết: ${record.action}`,
      width: 640,
      content: h('div', {}, [
        h('p', { style: 'font-weight:700;margin-bottom:4px' }, 'Trước:'),
        h('pre', { style: 'white-space:pre-wrap;background:#f5f5f5;padding:8px;border-radius:4px' },
          JSON.stringify(record.beforeData ?? null, null, 2)),
        h('p', { style: 'font-weight:700;margin:12px 0 4px' }, 'Sau:'),
        h('pre', { style: 'white-space:pre-wrap;background:#f5f5f5;padding:8px;border-radius:4px' },
          JSON.stringify(record.afterData ?? null, null, 2)),
      ]),
    });
  }
</script>
