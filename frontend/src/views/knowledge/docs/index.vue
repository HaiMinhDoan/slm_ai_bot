<template>
  <PageWrapper dense contentFullHeight fixedHeight>
    <BasicTable @register="registerTable">
      <template #toolbar>
        <Button @click="openAssistant(true, {})">AI hỗ trợ soạn</Button>
        <Button @click="go('/knowledge/releases')">Phát hành</Button>
        <Button type="primary" @click="openCreate(true, {})">Tạo tài liệu</Button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <TableAction
            :actions="[
              {
                icon: 'clarity:note-edit-line',
                tooltip: 'Soạn, xem lịch sử',
                onClick: handleEdit.bind(null, record),
              },
              {
                icon: 'ant-design:robot-outlined',
                tooltip: 'Nhờ AI sửa tài liệu này',
                ifShow: !record.archived && record.type !== 'RULE',
                onClick: handleAssist.bind(null, record),
              },
              {
                icon: record.archived ? 'ant-design:undo-outlined' : 'ant-design:inbox-outlined',
                tooltip: record.archived ? 'Bỏ lưu trữ' : 'Lưu trữ (bỏ khỏi bản phát hành sau)',
                ifShow: record.type !== 'RULE' && record.type !== 'PROMPT',
                popConfirm: {
                  title: record.archived
                    ? `Bỏ lưu trữ ${record.key}?`
                    : `Lưu trữ ${record.key}? Bản phát hành sau sẽ không còn tài liệu này.`,
                  placement: 'left',
                  confirm: handleArchive.bind(null, record),
                },
              },
            ]"
          />
        </template>
      </template>
    </BasicTable>

    <DocDrawer @register="registerDrawer" @success="reload" />
    <CreateDocModal @register="registerCreate" @success="handleCreated" />
    <AssistantDrawer @register="registerAssistant" @changed="reload" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { Button } from '@/components/Button';
  import { BasicTable, TableAction, useTable } from '@/components/Table';
  import { PageWrapper } from '@/components/Page';
  import { useDrawer } from '@/components/Drawer';
  import { useModal } from '@/components/Modal';
  import { useMessage } from '@/hooks/web/useMessage';
  import { useGo } from '@/hooks/web/usePage';
  import { knowledgeArchiveApi, knowledgeDocsApi } from '@/api/knowledge';

  import DocDrawer from '../shared/DocDrawer.vue';
  import CreateDocModal from './CreateDocModal.vue';
  import AssistantDrawer from './AssistantDrawer.vue';
  import { columns, searchFormSchema } from './docs.data';

  defineOptions({ name: 'KnowledgeDocs' });

  const { createMessage } = useMessage();
  const go = useGo();
  const [registerDrawer, { openDrawer }] = useDrawer();
  const [registerAssistant, { openDrawer: openAssistant }] = useDrawer();
  const [registerCreate, { openModal: openCreate }] = useModal();

  // vben đánh trang từ 1, Spring Data từ 0
  const loadDocs = async (params: Recordable) => {
    const { page = 1, size = 10, ...filters } = params;
    return knowledgeDocsApi({ ...filters, page: page - 1, size });
  };

  const [registerTable, { reload }] = useTable({
    title: 'Tài liệu kiến thức',
    titleHelpMessage: [
      'Bot đọc các tài liệu Cốt lõi, Cách tư vấn, Chính sách, Hỏi đáp, Chat mẫu trong mọi lượt trả lời.',
      'Sửa xong là bản nháp; bot chỉ dùng nội dung mới sau khi phát hành và kích hoạt ở trang Phát hành.',
    ],
    api: loadDocs,
    rowKey: 'id',
    columns,
    formConfig: { labelWidth: 80, schemas: searchFormSchema, autoSubmitOnEnter: true },
    // Backend nhận `size`, vben mặc định gửi `pageSize`
    fetchSetting: { sizeField: 'size' },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    striped: false,
    actionColumn: { width: 130, title: 'Thao tác', dataIndex: 'action', fixed: 'right' },
  });

  function handleEdit(record: Recordable) {
    openDrawer(true, { id: record.id });
  }

  function handleAssist(record: Recordable) {
    openAssistant(true, { docId: record.id, docKey: record.key });
  }

  function handleCreated(id: number) {
    reload();
    openDrawer(true, { id });
  }

  async function handleArchive(record: Recordable) {
    await knowledgeArchiveApi(record.id, !record.archived);
    createMessage.success(record.archived ? 'Đã bỏ lưu trữ' : 'Đã lưu trữ. Áp dụng cho bot ở trang Phát hành.');
    reload();
  }
</script>
