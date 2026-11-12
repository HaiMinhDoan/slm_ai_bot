<template>
  <PageWrapper dense contentFullHeight fixedHeight>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <template #toolbar>
        <Button :disabled="!selectedRowKeys.length" @click="openBulk(true, { ids: selectedRowKeys })">
          Thao tác ({{ selectedRowKeys.length }})
        </Button>
        <Button @click="openTopics(true, {})">Chủ đề</Button>
        <Button @click="openImportModal(true, {})">Nhập combo từ Drive</Button>
        <Button type="primary" preIcon="ant-design:plus-outlined" @click="openAdd(true, {})">
          Thêm media
        </Button>
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
          <MediaPreview :media="record as MediaModel" />
        </template>
        <template v-else-if="column.key === 'topics'">
          <Tag v-for="t in record.topics" :key="t">{{ topicNames[t] ?? t }}</Tag>
        </template>
        <template v-else-if="column.key === 'source'">
          <div v-for="s in record.sources" :key="s.id" class="small">
            {{ SOURCE_LABELS[s.sourceType] ?? s.sourceType }}
            <span v-if="s.width">· {{ s.width }}×{{ s.height }}</span>
            <span v-if="s.sizeBytes">· {{ sizeText(s.sizeBytes) }}</span>
            <span v-if="s.durationSeconds">· {{ durationText(s.durationSeconds) }}</span>
          </div>
        </template>
        <template v-else-if="column.key === 'links'">
          <Tag v-for="l in record.links" :key="l.targetType + l.targetKey + l.role" class="link-tag">
            {{ linkText(l) }}
          </Tag>
        </template>
      </template>
    </BasicTable>

    <AddMediaModal @register="registerAdd" @success="reload" @open="openEditor" />
    <ImportModal @register="registerImportModal" @success="reload" />
    <TopicsModal @register="registerTopics" @success="loadTopics" />
    <BulkModal @register="registerBulk" @success="afterBulk" />
    <MediaDrawer @register="registerDrawer" @success="reload" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref, unref } from 'vue';
  import { Tag } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { BasicTable, useTable, TableAction } from '@/components/Table';
  import { PageWrapper } from '@/components/Page';
  import { useModal } from '@/components/Modal';
  import { useDrawer } from '@/components/Drawer';
  import { useMessage } from '@/hooks/web/useMessage';
  import { mediaDeleteApi, mediaListApi, mediaRestoreApi, topicListApi } from '@/api/media';
  import type { MediaModel } from '@/api/media/model/mediaModel';
  import AddMediaModal from './AddMediaModal.vue';
  import BulkModal from './BulkModal.vue';
  import ImportModal from './ImportModal.vue';
  import MediaDrawer from './MediaDrawer.vue';
  import MediaPreview from './MediaPreview.vue';
  import TopicsModal from './TopicsModal.vue';
  import { SOURCE_LABELS, columns, linkText, searchFormSchema } from './media.data';
  import { durationText, sizeText } from './videoMeta';

  defineOptions({ name: 'MediaLibrary' });

  const { createMessage } = useMessage();
  const [registerAdd, { openModal: openAdd, closeModal: closeAdd }] = useModal();
  const [registerImportModal, { openModal: openImportModal }] = useModal();
  const [registerTopics, { openModal: openTopics }] = useModal();
  const [registerBulk, { openModal: openBulk }] = useModal();
  const [registerDrawer, { openDrawer }] = useDrawer();

  const topicNames = ref<Record<string, string>>({});
  const selectedRowKeys = ref<string[]>([]);
  const rowSelection = computed(
    () =>
      ({
        type: 'checkbox',
        selectedRowKeys: unref(selectedRowKeys),
        onChange: (keys: (string | number)[]) => {
          selectedRowKeys.value = keys as string[];
        },
      }) as any,
  );

  // vben đánh trang từ 1, Spring Data từ 0
  const loadMedia = async (params: Recordable) => {
    const { page = 1, size = 10, ...filters } = params;
    return mediaListApi({ ...filters, page: page - 1, size });
  };

  // Cả trang chỉ dành cho quản trị hệ thống / nội dung (route và backend), nên không cần ẩn nút theo vai trò
  const [registerTable, { reload, clearSelectedRowKeys }] = useTable({
    title: 'Thư viện media',
    titleHelpMessage: [
      'Ảnh, video dùng để gửi kèm khi tư vấn. Media mới thêm ở trạng thái Nháp.',
      'Xoá là xoá mềm: file trong MinIO và liên kết với gói vẫn giữ, khôi phục ở bộ lọc Đã xoá.',
      'Chọn nhiều dòng rồi bấm Thao tác để duyệt, đổi quyền, thêm chủ đề, gắn gói hoặc xoá hàng loạt.',
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

  async function loadTopics() {
    const topics = await topicListApi();
    topicNames.value = Object.fromEntries(topics.map((t) => [t.code, t.name]));
  }

  onMounted(loadTopics);

  function handleEdit(record: Recordable) {
    openDrawer(true, { id: record.id });
  }

  /** Mở form sửa một media vừa thêm (từ hộp Thêm media) */
  function openEditor(id: string) {
    closeAdd();
    reload();
    openDrawer(true, { id });
  }

  function afterBulk() {
    selectedRowKeys.value = [];
    clearSelectedRowKeys();
    reload();
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

  .link-tag {
    white-space: normal;
    margin-bottom: 2px;
  }
</style>
