<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="`Thao tác với ${ids.length} media`"
    width="760px"
    okText="Thực hiện"
    :okButtonProps="{ loading: running }"
    @ok="handleRun"
  >
    <BasicForm @register="registerForm" @field-value-change="handleChange" />
    <div v-if="action === 'ADD_LINK'" class="link-row">
      <Select v-model:value="targetType" :options="options(TARGET_LABELS)" style="width: 110px" />
      <Select
        :value="targetKey"
        showSearch
        :filterOption="false"
        :options="targetOptions"
        :loading="searching"
        placeholder="Gõ để tìm gói, vật tư, hãng, tài liệu"
        class="target"
        @search="search"
        @focus="() => !targetOptions.length && search('')"
        @change="(v) => (targetKey = v as string)"
      />
      <Select v-model:value="role" :options="options(ROLE_LABELS)" style="width: 170px" />
    </div>
    <Table
      v-if="results.length"
      :columns="columns"
      :data-source="results"
      :pagination="false"
      :scroll="{ y: 280 }"
      rowKey="id"
      size="small"
      bordered
      class="mt-3"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'ok'">
          <Tag :color="record.ok ? 'green' : 'red'">{{ record.ok ? 'Xong' : 'Lỗi' }}</Tag>
        </template>
      </template>
    </Table>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref, watch } from 'vue';
  import { Select, Table, Tag } from 'ant-design-vue';
  import { useDebounceFn } from '@vueuse/core';
  import { BasicForm, useForm } from '@/components/Form';
  import { BasicModal, useModalInner } from '@/components/Modal';
  import { useMessage } from '@/hooks/web/useMessage';
  import { mediaBulkApi, mediaLinkTargetsApi, topicListApi } from '@/api/media';
  import type {
    BulkAction,
    LinkRole,
    LinkTargetType,
    MediaBulkResult,
  } from '@/api/media/model/mediaModel';
  import { RIGHTS_LABELS, ROLE_LABELS, STATUS_LABELS, TARGET_LABELS, options } from './media.data';

  defineOptions({ name: 'MediaBulkModal' });

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();

  const ACTION_LABELS: Record<BulkAction, string> = {
    SET_STATUS: 'Đổi trạng thái (duyệt, về nháp, lưu trữ)',
    SET_RIGHTS: 'Đổi quyền sử dụng',
    ADD_TOPICS: 'Thêm chủ đề',
    ADD_LINK: 'Gắn với gói / vật tư / hãng / tài liệu',
    DELETE: 'Xoá (xoá mềm)',
    RESTORE: 'Khôi phục',
  };
  const columns = [
    { title: 'Media', dataIndex: 'title', ellipsis: true },
    { title: 'Kết quả', key: 'ok', width: 80 },
    { title: 'Ghi chú', dataIndex: 'note' },
  ];

  const ids = ref<string[]>([]);
  const action = ref<BulkAction>();
  const running = ref(false);
  const results = ref<MediaBulkResult[]>([]);
  const targetType = ref<LinkTargetType>('PACKAGE');
  const targetKey = ref<string>();
  const targetOptions = ref<{ value: string; label: string }[]>([]);
  const searching = ref(false);
  const role = ref<LinkRole>('GALLERY');

  const [registerForm, { resetFields, validate, updateSchema }] = useForm({
    labelWidth: 120,
    showActionButtonGroup: false,
    schemas: [
      {
        field: 'action',
        label: 'Thao tác',
        component: 'Select',
        required: true,
        componentProps: { options: options(ACTION_LABELS) },
        colProps: { span: 24 },
      },
      {
        field: 'status',
        label: 'Trạng thái',
        component: 'Select',
        required: true,
        componentProps: { options: options(STATUS_LABELS) },
        helpMessage: 'Duyệt cần Tóm tắt, Dùng khi và quyền sử dụng đã rõ; mục chưa đủ sẽ báo lỗi riêng.',
        ifShow: ({ values }) => values.action === 'SET_STATUS',
        colProps: { span: 24 },
      },
      {
        field: 'rights',
        label: 'Quyền sử dụng',
        component: 'Select',
        required: true,
        componentProps: { options: options(RIGHTS_LABELS) },
        ifShow: ({ values }) => values.action === 'SET_RIGHTS',
        colProps: { span: 24 },
      },
      {
        field: 'topics',
        label: 'Chủ đề',
        component: 'Select',
        required: true,
        componentProps: { mode: 'multiple', options: [] },
        ifShow: ({ values }) => values.action === 'ADD_TOPICS',
        colProps: { span: 24 },
      },
    ],
  });

  const [registerModal] = useModalInner(async (data) => {
    ids.value = data.ids as string[];
    action.value = undefined;
    results.value = [];
    targetKey.value = undefined;
    targetOptions.value = [];
    await resetFields();
    const topics = await topicListApi();
    await updateSchema({
      field: 'topics',
      componentProps: {
        mode: 'multiple',
        options: topics.filter((t) => t.active).map((t) => ({ value: t.code, label: t.name })),
      },
    });
  });

  function handleChange(key: string, value: any) {
    if (key === 'action') action.value = value;
  }

  watch(targetType, () => {
    targetKey.value = undefined;
    targetOptions.value = [];
  });

  const search = useDebounceFn(async (keyword: string) => {
    searching.value = true;
    try {
      const found = await mediaLinkTargetsApi(targetType.value, keyword || undefined);
      targetOptions.value = found.map((t) => ({ value: t.key, label: t.label }));
    } finally {
      searching.value = false;
    }
  }, 300);

  async function handleRun() {
    const values = await validate();
    if (values.action === 'ADD_LINK' && !targetKey.value) {
      createMessage.error('Chọn đối tượng để gắn');
      return;
    }
    running.value = true;
    try {
      results.value = await mediaBulkApi({
        ids: ids.value,
        action: values.action,
        status: values.status,
        rights: values.rights,
        topics: values.topics,
        link:
          values.action === 'ADD_LINK'
            ? { targetType: targetType.value, targetKey: targetKey.value!, role: role.value }
            : undefined,
      });
      const failed = results.value.filter((r) => !r.ok).length;
      if (failed) {
        createMessage.warning(`${results.value.length - failed} mục xong, ${failed} mục lỗi: xem bảng bên dưới`);
      } else {
        createMessage.success(`Đã xong ${results.value.length} mục`);
      }
      emit('success');
    } finally {
      running.value = false;
    }
  }
</script>

<style lang="less" scoped>
  .link-row {
    display: flex;
    gap: 8px;
    margin: 0 0 12px 120px;

    .target {
      flex: 1;
      min-width: 0;
    }
  }
</style>
