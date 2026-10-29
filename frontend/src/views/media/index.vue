<template>
  <PageWrapper dense contentFullHeight fixedHeight>
    <BasicTable @register="registerTable">
      <template #toolbar>
        <Button type="primary" @click="openImportModal(true, {})">Nhập combo từ Drive</Button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <TableAction
            v-if="record.deleted"
            :actions="[
              {
                icon: 'ant-design:undo-outlined',
                tooltip: 'Khôi phục media',
                onClick: handleRestore.bind(null, record),
              },
            ]"
          />
          <TableAction
            v-else
            :actions="[
              {
                icon: 'clarity:note-edit-line',
                tooltip: 'Sửa, duyệt media',
                onClick: handleEdit.bind(null, record),
              },
              {
                icon: 'ant-design:delete-outlined',
                color: 'error',
                tooltip: 'Xoá media',
                popConfirm: {
                  title: 'Xoá media này? File và liên kết vẫn giữ, khôi phục được ở bộ lọc Đã xoá.',
                  placement: 'left',
                  confirm: handleDelete.bind(null, record),
                },
              },
            ]"
          />
        </template>
        <template v-else-if="column.key === 'preview'">
          <MediaThumb
            v-if="storedSource(record)"
            :mediaId="record.id"
            :sourceId="storedSource(record)!.id"
          />
          <a
            v-else-if="externalSource(record)"
            :href="externalSource(record)!.sourceUrl"
            target="_blank"
            rel="noopener noreferrer"
          >
            Mở link
          </a>
        </template>
        <template v-else-if="column.key === 'source'">
          <div v-for="s in record.sources" :key="s.id" class="small">
            {{ SOURCE_LABELS[s.sourceType] ?? s.sourceType }}
            <span v-if="s.width">· {{ s.width }}×{{ s.height }}</span>
            <span v-if="s.sizeBytes">· {{ Math.round(s.sizeBytes / 1024) }} KB</span>
          </div>
        </template>
        <template v-else-if="column.key === 'links'">
          <Tag v-for="l in record.links" :key="l.targetType + l.targetKey + l.role">
            {{ linkText(l) }}
          </Tag>
        </template>
      </template>
    </BasicTable>

    <ImportModal @register="registerImportModal" @success="reload" />
    <MediaDrawer @register="registerDrawer" @success="reload" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { Tag } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { BasicTable, useTable, TableAction } from '@/components/Table';
  import { PageWrapper } from '@/components/Page';
  import { useModal } from '@/components/Modal';
  import { useDrawer } from '@/components/Drawer';
  import { useMessage } from '@/hooks/web/useMessage';
  import { mediaDeleteApi, mediaListApi, mediaRestoreApi } from '@/api/media';
  import type { MediaModel, MediaSourceModel } from '@/api/media/model/mediaModel';
  import ImportModal from './ImportModal.vue';
  import MediaDrawer from './MediaDrawer.vue';
  import MediaThumb from './MediaThumb.vue';
  import { SOURCE_LABELS, columns, linkText, searchFormSchema } from './media.data';

  defineOptions({ name: 'MediaLibrary' });

  const { createMessage } = useMessage();
  const [registerImportModal, { openModal: openImportModal }] = useModal();
  const [registerDrawer, { openDrawer }] = useDrawer();

  // vben đánh trang từ 1, Spring Data từ 0
  const loadMedia = async (params: Recordable) => {
    const { page = 1, size = 10, ...filters } = params;
    return mediaListApi({ ...filters, page: page - 1, size });
  };

  // Cả trang chỉ dành cho quản trị hệ thống / nội dung (route và backend), nên không cần ẩn nút theo vai trò
  const [registerTable, { reload }] = useTable({
    title: 'Thư viện media',
    titleHelpMessage: [
      'Ảnh, video dùng để gửi kèm khi tư vấn. Media mới nhập ở trạng thái Nháp.',
      'Xoá là xoá mềm: file trong MinIO và liên kết với gói vẫn giữ, khôi phục ở bộ lọc Đã xoá.',
    ],
    api: loadMedia,
    rowKey: 'id',
    columns,
    formConfig: { labelWidth: 80, schemas: searchFormSchema, autoSubmitOnEnter: true },
    // Backend nhận `size`, vben mặc định gửi `pageSize`
    fetchSetting: { sizeField: 'size' },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    striped: false,
    actionColumn: { width: 100, title: 'Thao tác', dataIndex: 'action', fixed: 'right' },
  });

  function storedSource(record: Recordable): MediaSourceModel | undefined {
    return (record as MediaModel).sources.find((s) => s.stored);
  }

  function externalSource(record: Recordable): MediaSourceModel | undefined {
    return (record as MediaModel).sources.find((s) => !s.stored && s.sourceUrl);
  }

  function handleEdit(record: Recordable) {
    openDrawer(true, { id: record.id });
  }

  async function handleDelete(record: Recordable) {
    await mediaDeleteApi(record.id);
    createMessage.success('Đã xoá media');
    reload();
  }

  async function handleRestore(record: Recordable) {
    await mediaRestoreApi(record.id);
    createMessage.success('Đã khôi phục media');
    reload();
  }
</script>

<style lang="less" scoped>
  .small {
    font-size: 12px;
  }
</style>
