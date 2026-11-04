<template>
  <PageWrapper dense contentFullHeight fixedHeight>
    <BasicTable @register="registerTable">
      <template #toolbar>
        <Button type="primary" @click="openCreate(true, {})">Tạo bản phát hành</Button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <TableAction
            :actions="[
              {
                icon: 'ant-design:eye-outlined',
                tooltip: 'Xem tài liệu và báo cáo kiểm tra',
                onClick: () => openDetail(true, { id: record.id }),
              },
              {
                icon: 'ant-design:play-circle-outlined',
                tooltip: 'Kích hoạt',
                ifShow: record.status === 'READY',
                popConfirm: {
                  title: `Kích hoạt bản #${record.id}? Bot dùng kiến thức mới ngay.`,
                  placement: 'left',
                  confirm: handleActivate.bind(null, record),
                },
              },
              {
                icon: 'ant-design:rollback-outlined',
                tooltip: 'Quay về bản này',
                ifShow: record.status === 'SUPERSEDED',
                popConfirm: {
                  title: `Quay về bản #${record.id}? Bot dùng lại kiến thức của bản này ngay.`,
                  placement: 'left',
                  confirm: handleActivate.bind(null, record),
                },
              },
            ]"
          />
        </template>
      </template>
    </BasicTable>
    <CreateReleaseModal @register="registerCreate" @success="reload" />
    <ReleaseDrawer @register="registerDetail" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { Button } from '@/components/Button';
  import { BasicTable, TableAction, useTable } from '@/components/Table';
  import { PageWrapper } from '@/components/Page';
  import { useModal } from '@/components/Modal';
  import { useDrawer } from '@/components/Drawer';
  import { useMessage } from '@/hooks/web/useMessage';
  import { releaseActivateApi, releasesApi } from '@/api/knowledge';
  import CreateReleaseModal from './CreateReleaseModal.vue';
  import ReleaseDrawer from './ReleaseDrawer.vue';
  import { columns } from './releases.data';

  defineOptions({ name: 'KnowledgeReleases' });

  const { createMessage } = useMessage();
  const [registerCreate, { openModal: openCreate }] = useModal();
  const [registerDetail, { openDrawer: openDetail }] = useDrawer();

  // vben đánh trang từ 1, Spring Data từ 0
  const loadReleases = async (params: Recordable) => {
    const { page = 1, size = 10 } = params;
    return releasesApi({ page: page - 1, size });
  };

  const [registerTable, { reload }] = useTable({
    title: 'Bản phát hành kiến thức',
    titleHelpMessage: [
      'Mỗi bản là ảnh chụp bất biến của kiến thức và quy tắc. Bot dùng bản Đang chạy.',
      'Quay về bản cũ là kích hoạt lại bản đó, có hiệu lực ngay.',
    ],
    api: loadReleases,
    rowKey: 'id',
    columns,
    fetchSetting: { sizeField: 'size' },
    showTableSetting: true,
    bordered: true,
    striped: false,
    actionColumn: { width: 110, title: 'Thao tác', dataIndex: 'action', fixed: 'right' },
  });

  async function handleActivate(record: Recordable) {
    await releaseActivateApi(record.id);
    createMessage.success(`Bot đang dùng bản #${record.id}`);
    reload();
  }
</script>
