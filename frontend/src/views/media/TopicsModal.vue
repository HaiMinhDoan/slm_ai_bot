<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="Bộ chủ đề"
    width="860px"
    :showOkBtn="false"
    cancelText="Đóng"
    :closeFunc="handleClose"
  >
    <Alert
      type="info"
      show-icon
      class="mb-3"
      message="Chủ đề gắn cho media để AI và người soạn tìm đúng media; có hiệu lực ngay"
      description="Mã viết thường không dấu, nối bằng gạch (pin-luu-tru) và không đổi được sau khi tạo. Không xoá chủ đề vì media cũ đang gắn: tắt thì không chọn mới được."
    />
    <BasicForm @register="registerForm" />
    <div class="form-actions">
      <Button v-if="editing" @click="cancelEdit">Huỷ sửa</Button>
      <Button type="primary" :loading="saving" @click="handleSave">
        {{ editing ? `Lưu chủ đề ${editing}` : 'Thêm chủ đề' }}
      </Button>
    </div>
    <Table
      :columns="columns"
      :data-source="topics"
      :loading="loading"
      :pagination="false"
      :scroll="{ y: 320 }"
      rowKey="code"
      size="small"
      bordered
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'active'">
          <Switch :checked="record.active" size="small" @change="(v) => toggle(record as TopicModel, !!v)" />
        </template>
        <template v-else-if="column.key === 'action'">
          <Button size="small" type="link" @click="startEdit(record as TopicModel)">Sửa</Button>
        </template>
      </template>
    </Table>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { Alert, Switch, Table } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { BasicForm, useForm } from '@/components/Form';
  import { BasicModal, useModalInner } from '@/components/Modal';
  import { useMessage } from '@/hooks/web/useMessage';
  import { topicCreateApi, topicListApi, topicUpdateApi } from '@/api/media';
  import type { TopicModel } from '@/api/media/model/mediaModel';

  defineOptions({ name: 'TopicsModal' });

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();

  const topics = ref<TopicModel[]>([]);
  const loading = ref(false);
  const saving = ref(false);
  const editing = ref<string>();
  let changed = false;

  const columns = [
    { title: 'Mã', dataIndex: 'code', width: 130 },
    { title: 'Tên', dataIndex: 'name', width: 170 },
    { title: 'Khi nào gắn', dataIndex: 'description' },
    { title: 'Thứ tự', dataIndex: 'sortOrder', width: 70 },
    { title: 'Dùng', key: 'active', width: 60 },
    { title: '', key: 'action', width: 60 },
  ];

  const [registerForm, { resetFields, setFieldsValue, validate, updateSchema }] = useForm({
    labelWidth: 90,
    showActionButtonGroup: false,
    schemas: [
      {
        field: 'code',
        label: 'Mã',
        component: 'Input',
        required: true,
        componentProps: { maxlength: 50, placeholder: 've-sinh' },
        rules: [
          {
            pattern: /^[a-z0-9]+(-[a-z0-9]+)*$/,
            message: 'Viết thường không dấu, nối bằng gạch',
          },
        ],
        colProps: { span: 8 },
      },
      {
        field: 'name',
        label: 'Tên',
        component: 'Input',
        required: true,
        componentProps: { maxlength: 100, placeholder: 'Vệ sinh tấm pin' },
        colProps: { span: 10 },
      },
      {
        field: 'sortOrder',
        label: 'Thứ tự',
        component: 'InputNumber',
        defaultValue: 0,
        componentProps: { min: 0, style: { width: '100%' } },
        colProps: { span: 6 },
      },
      {
        field: 'description',
        label: 'Khi nào gắn',
        component: 'Input',
        componentProps: { maxlength: 1000 },
        colProps: { span: 24 },
      },
    ],
  });

  const [registerModal] = useModalInner(async () => {
    changed = false;
    await cancelEdit();
    await reload();
  });

  async function reload() {
    loading.value = true;
    try {
      topics.value = await topicListApi();
    } finally {
      loading.value = false;
    }
  }

  async function startEdit(t: TopicModel) {
    editing.value = t.code;
    await updateSchema({ field: 'code', componentProps: { disabled: true } });
    await setFieldsValue({ ...t });
  }

  async function cancelEdit() {
    editing.value = undefined;
    await updateSchema({ field: 'code', componentProps: { disabled: false, maxlength: 50 } });
    await resetFields();
  }

  async function handleSave() {
    const values = await validate();
    saving.value = true;
    try {
      if (editing.value) {
        await topicUpdateApi(editing.value, values);
        createMessage.success('Đã lưu chủ đề');
      } else {
        await topicCreateApi(values);
        createMessage.success('Đã thêm chủ đề');
      }
      changed = true;
      await cancelEdit();
      await reload();
    } finally {
      saving.value = false;
    }
  }

  async function toggle(t: TopicModel, active: boolean) {
    await topicUpdateApi(t.code, { name: t.name, description: t.description, active });
    changed = true;
    await reload();
  }

  async function handleClose() {
    if (changed) emit('success');
    return true;
  }
</script>

<style lang="less" scoped>
  .form-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-bottom: 12px;
  }
</style>
