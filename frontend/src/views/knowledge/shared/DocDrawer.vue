<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    :title="drawerTitle"
    width="80%"
    showFooter
    okText="Lưu nháp"
    :okButtonProps="{ disabled: !detail || detail.doc.archived }"
    @ok="handleSave(false)"
  >
    <template #insertFooter>
      <Popconfirm
        v-if="detail?.draft"
        title="Bỏ bản nháp này? Nội dung đang chạy không đổi."
        placement="topRight"
        @confirm="handleDiscard"
      >
        <Button danger class="mr-2">Bỏ nháp</Button>
      </Popconfirm>
      <Button class="mr-2" :disabled="!detail" @click="handleCheck">Kiểm tra &amp; đếm token</Button>
      <Button class="mr-2" :disabled="!detail?.draft" @click="handleDiff()">
        So với bản đang chạy
      </Button>
    </template>

    <template v-if="detail">
      <Tabs v-model:activeKey="tab">
        <TabPane key="edit" tab="Soạn">
          <Alert
            v-if="detail.doc.archived"
            type="warning"
            show-icon
            class="mb-3"
            message="Tài liệu đã lưu trữ: không có trong bản phát hành sau. Bỏ lưu trữ ở danh sách để sửa tiếp."
          />
          <Alert
            v-else-if="detail.draft"
            type="info"
            show-icon
            class="mb-3"
            :message="`Đang sửa bản nháp v${detail.draft.versionNo} (lưu lần cuối bởi ${detail.draft.author}, ${formatToDateTime(detail.draft.updatedAt)}).`"
            description="Bản nháp không ảnh hưởng tới khách. Bot chỉ dùng nội dung mới sau khi phát hành và kích hoạt ở trang Phát hành."
          />
          <Alert
            v-if="detail.doc.type === 'PROMPT'"
            type="warning"
            show-icon
            class="mb-3"
            message="Hướng dẫn hệ thống cho AI"
            description="Đây là chỉ dẫn chính AI đọc trước mọi tài liệu (cách dùng công cụ báo giá, ý nghĩa từng trường kết quả). Giữ nguyên tên các trường (goal, intent, reply, collected...), chỉ sửa khi hiểu rõ."
          />
          <BasicForm @register="registerForm" />
          <div class="editor-wrap">
            <div v-if="isYaml" class="yaml-box">
              <CodeEditor v-model:value="content" :mode="MODE.YAML" :config="CODE_CONFIG" bordered />
            </div>
            <MarkDown v-else v-model:value="content" :height="560" />
          </div>
          <div class="mt-3">
            <div v-if="tokens" class="mb-2">
              Khối kiến thức bot đọc (gồm tài liệu này):
              <b>{{ tokens.tokens.toLocaleString('vi-VN') }}</b> /
              {{ tokens.budget.toLocaleString('vi-VN') }} token
              <Tag :color="tokens.percent > 90 ? 'red' : tokens.percent > 70 ? 'orange' : 'green'">
                {{ tokens.percent.toLocaleString('vi-VN') }}%
              </Tag>
              <span v-if="tokens.estimated" class="text-gray-500">(ước tính, chưa đếm bằng API)</span>
            </div>
            <div v-if="lint !== undefined">
              <div v-if="!lint.length" class="text-green-600">Kiểm tra nhanh: không có cảnh báo.</div>
              <div v-for="(i, idx) in lint" :key="idx" class="lint-row">
                <Tag :color="CHECK_LEVEL[i.level]?.color">{{ CHECK_LEVEL[i.level]?.label }}</Tag>
                <span v-if="i.line" class="text-gray-500">Dòng {{ i.line }}: </span>{{ i.message }}
              </div>
            </div>
          </div>
        </TabPane>
        <TabPane key="history" :tab="`Lịch sử (${detail.versions.length})`">
          <BasicTable @register="registerTable" :dataSource="detail.versions">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'versionNo'">
                v{{ record.versionNo }}
                <Tag v-if="record.active" color="green">Đang chạy</Tag>
                <Tag v-if="record.status === 'DRAFT'" color="orange">Nháp</Tag>
                <Tag v-if="record.aiAssisted" color="purple">AI soạn</Tag>
              </template>
              <template v-else-if="column.key === 'action'">
                <TableAction
                  :actions="[
                    {
                      icon: 'ant-design:eye-outlined',
                      tooltip: 'Xem nội dung',
                      onClick: handleView.bind(null, record),
                    },
                    {
                      icon: 'ant-design:diff-outlined',
                      tooltip: 'So với bản đang chạy',
                      onClick: handleDiff.bind(null, record),
                    },
                    {
                      icon: 'ant-design:rollback-outlined',
                      tooltip: 'Khôi phục bản này thành bản nháp',
                      ifShow: record.status !== 'DRAFT',
                      popConfirm: {
                        title: `Tạo bản nháp từ v${record.versionNo}? Bản nháp đang có (nếu có) bị thay.`,
                        placement: 'left',
                        confirm: handleRestore.bind(null, record),
                      },
                    },
                  ]"
                />
              </template>
            </template>
          </BasicTable>
        </TabPane>
      </Tabs>
    </template>

    <DiffModal @register="registerDiff" @ok="handleSave(true)" />
    <VersionModal @register="registerVersion" />
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { computed, nextTick, ref } from 'vue';
  import { Alert, Popconfirm, Tabs, Tag } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
  import { BasicForm, useForm } from '@/components/Form';
  import { BasicTable, TableAction, useTable } from '@/components/Table';
  import { useModal } from '@/components/Modal';
  import { MarkDown } from '@/components/Markdown';
  import { CodeEditor, MODE } from '@/components/CodeEditor';
  import { useMessage } from '@/hooks/web/useMessage';
  import { formatToDateTime } from '@/utils/dateUtil';

  import DiffModal from './DiffModal.vue';
  import { CODE_CONFIG } from './codeEditor';
  import VersionModal from './VersionModal.vue';
  import { CHECK_LEVEL } from './labels';
  import { apiError } from './errors';
  import {
    knowledgeCountTokensApi,
    knowledgeDiffApi,
    knowledgeDiscardDraftApi,
    knowledgeDocApi,
    knowledgeLintApi,
    knowledgeRestoreApi,
    knowledgeSaveDraftApi,
  } from '@/api/knowledge';
  import type {
    KnowledgeDocDetail,
    KnowledgeLintIssue,
    KnowledgeTokenCount,
  } from '@/api/knowledge/model/knowledgeModel';

  defineOptions({ name: 'KnowledgeDocDrawer' });

  /** Mở bằng openDrawer(true, { id }); lưu / bỏ nháp / khôi phục xong phát `success` để trang tải lại */
  const emit = defineEmits(['success', 'register']);
  const TabPane = Tabs.TabPane;
  const { createMessage } = useMessage();

  const detail = ref<KnowledgeDocDetail>();
  const content = ref('');
  const tab = ref('edit');
  const tokens = ref<KnowledgeTokenCount>();
  const lint = ref<KnowledgeLintIssue[]>();
  /** Phiên bản mới nhất lúc mở trang / sua_luc của nháp: gửi lại để phát hiện xung đột */
  const baseVersionId = ref<number>();
  const expectedDraftUpdatedAt = ref<string>();

  const isYaml = computed(() => detail.value?.doc.format === 'YAML');
  const drawerTitle = computed(() =>
    detail.value ? `${detail.value.doc.key} · ${detail.value.doc.typeLabel}` : 'Tài liệu',
  );

  const [registerForm, { setFieldsValue, validate, resetFields }] = useForm({
    labelWidth: 110,
    baseColProps: { span: 12 },
    showActionButtonGroup: false,
    schemas: [
      { field: 'title', label: 'Tiêu đề', component: 'Input', required: true, componentProps: { maxlength: 300 } },
      {
        field: 'changeNote',
        label: 'Ghi chú thay đổi',
        component: 'Input',
        componentProps: { maxlength: 1000, placeholder: 'Sửa gì, vì sao (hiện ở lịch sử và trang Phát hành)' },
      },
    ],
  });

  // Bảng nằm trong tab chưa mở nên nhận dữ liệu qua :dataSource, không qua setTableData
  const [registerTable] = useTable({
    columns: [
      { title: 'Phiên bản', dataIndex: 'versionNo', key: 'versionNo', width: 200, align: 'left' },
      { title: 'Tiêu đề', dataIndex: 'title', align: 'left' },
      { title: 'Ghi chú', dataIndex: 'changeNote', align: 'left' },
      { title: 'Người sửa', dataIndex: 'author', width: 180 },
      {
        title: 'Lúc',
        dataIndex: 'updatedAt',
        width: 150,
        customRender: ({ record }) => formatToDateTime(record.updatedAt),
      },
    ],
    rowKey: 'id',
    bordered: true,
    striped: false,
    canResize: false,
    actionColumn: { width: 120, title: 'Thao tác', dataIndex: 'action', fixed: 'right' },
  });

  const [registerDiff, { openModal: openDiff }] = useModal();
  const [registerVersion, { openModal: openVersion }] = useModal();

  const [registerDrawer, { changeLoading, setDrawerProps }] = useDrawerInner(async (data) => {
    detail.value = undefined;
    tokens.value = undefined;
    lint.value = undefined;
    tab.value = 'edit';
    await load(data.id);
  });

  async function load(id: number) {
    changeLoading(true);
    try {
      await apply(await knowledgeDocApi(id));
    } finally {
      changeLoading(false);
    }
  }

  async function apply(d: KnowledgeDocDetail) {
    detail.value = d;
    const shown = d.draft ?? d.latest;
    content.value = shown?.content ?? '';
    baseVersionId.value = d.latest?.id;
    expectedDraftUpdatedAt.value = d.draft?.updatedAt;
    // Form chỉ dựng sau khi có detail (v-if), đợi render xong mới ghi giá trị
    await nextTick();
    await resetFields();
    await setFieldsValue({ title: shown?.title ?? d.doc.title, changeNote: d.draft?.changeNote ?? '' });
  }

  async function handleSave(force: boolean) {
    if (!detail.value) return;
    const values = await validate();
    setDrawerProps({ confirmLoading: true });
    try {
      const saved = await knowledgeSaveDraftApi(detail.value.doc.id, {
        title: values.title,
        content: content.value,
        changeNote: values.changeNote,
        baseVersionId: baseVersionId.value,
        expectedDraftUpdatedAt: expectedDraftUpdatedAt.value,
        force,
      });
      await apply(saved);
      createMessage.success('Đã lưu nháp. Vào trang Phát hành để áp dụng cho bot.');
      emit('success');
      handleCheck();
    } catch (e) {
      const err = apiError(e);
      if (err.status === 409) {
        openDiff(true, {
          title: 'Có thay đổi mới hơn bản bạn đang sửa',
          message: err.message,
          diff: err.data?.diff,
          okText: 'Vẫn lưu đè',
        });
      } else {
        createMessage.error(err.message);
      }
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }

  async function handleCheck() {
    if (!detail.value) return;
    const { key, type } = detail.value.doc;
    const [l, t] = await Promise.all([
      knowledgeLintApi({ type, key, content: content.value }),
      detail.value.doc.botReads || type === 'PROMPT'
        ? knowledgeCountTokensApi({ key, content: content.value })
        : Promise.resolve(undefined),
    ]);
    lint.value = l;
    tokens.value = t;
  }

  async function handleDiscard() {
    if (!detail.value) return;
    await apply(await knowledgeDiscardDraftApi(detail.value.doc.id));
    createMessage.success('Đã bỏ bản nháp');
    emit('success');
  }

  async function handleDiff(v?: Recordable) {
    if (!detail.value) return;
    const d = await knowledgeDiffApi(detail.value.doc.id, undefined, v?.id);
    openDiff(true, {
      title: `${detail.value.doc.key}: ${d.from ? 'v' + d.from.versionNo : 'trống'} → ${d.to ? 'v' + d.to.versionNo : 'trống'}`,
      diff: d.diff,
    });
  }

  function handleView(v: Recordable) {
    openVersion(true, { versionId: v.id, key: detail.value?.doc.key, yaml: isYaml.value });
  }

  async function handleRestore(v: Recordable) {
    if (!detail.value) return;
    await apply(await knowledgeRestoreApi(detail.value.doc.id, v.id));
    tab.value = 'edit';
    createMessage.success(`Đã tạo bản nháp từ v${v.versionNo}`);
    emit('success');
  }
</script>

<style lang="less" scoped>
  .editor-wrap {
    margin-top: 4px;
  }

  .yaml-box {
    height: 560px;

    :deep(.CodeMirror) {
      height: 100%;
    }
  }

  .lint-row {
    margin-bottom: 4px;
    font-size: 13px;
  }
</style>
