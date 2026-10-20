<template>
  <PageWrapper dense contentFullHeight fixedHeight>
    <BasicTable @register="registerTable">
      <template #toolbar>
        <Button type="primary" @click="openCreateModal(true)">Thêm người dùng</Button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <TableAction :actions="[{ label: 'Chi tiết', onClick: handleDetail.bind(null, record) }]" />
        </template>
      </template>
    </BasicTable>

    <UserDetailDrawer @register="registerDrawer" @success="reload" />
    <UserCreateModal @register="registerCreateModal" @success="reload" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { Button } from 'ant-design-vue';
  import { BasicTable, useTable, TableAction } from '@/components/Table';
  import { PageWrapper } from '@/components/Page';
  import { useDrawer } from '@/components/Drawer';
  import { useModal } from '@/components/Modal';

  import UserDetailDrawer from './UserDetailDrawer.vue';
  import UserCreateModal from './UserCreateModal.vue';
  import { columns, searchFormSchema } from './user.data';
  import { userFilterApi } from '@/api/system/user';

  defineOptions({ name: 'UserManagement' });

  const [registerDrawer, { openDrawer }] = useDrawer();
  const [registerCreateModal, { openModal: openCreateModal }] = useModal();

  const loadUsers = async (params: Recordable) => {
    const { page = 1, size = 20, keyword, status } = params;
    return userFilterApi({ keyword, status, page: page - 1, size });
  };

  const [registerTable, { reload }] = useTable({
    title: 'Người dùng nội bộ',
    api: loadUsers,
    rowKey: 'id',
    columns,
    formConfig: { labelWidth: 100, schemas: searchFormSchema },
    useSearchForm: true,
    fetchSetting: { sizeField: 'size' },
    showTableSetting: true,
    bordered: true,
    actionColumn: {
      width: 100,
      title: 'Thao tác',
      dataIndex: 'action',
      fixed: 'right',
    },
  });

  function handleDetail(record: Recordable<any>) {
    openDrawer(true, { id: record.id });
  }
</script>
