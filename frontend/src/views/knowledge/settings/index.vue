<template>
  <PageWrapper title="Cài đặt AI" contentBackground contentClass="p-4">
    <template #headerContent>
      Có hiệu lực ngay khi lưu, không cần phát hành. Mỗi ô ghi giá trị mặc định (trong cấu hình server); ô
      <b>đang đổi</b> là ô khác mặc định.
    </template>

    <Alert
      v-if="view"
      :type="view.values.killSwitch.on ? 'error' : 'success'"
      show-icon
      class="mb-4"
      :message="
        view.values.killSwitch.on
          ? 'Công tắc dừng đang BẬT: mọi tin AI chỉ lưu nháp chờ nhân viên duyệt, không gửi tự động'
          : 'Công tắc dừng đang tắt: AI gửi tự động theo cài đặt bên dưới'
      "
    >
      <template #description>
        <div v-if="view.values.killSwitch.updatedBy">
          <template v-if="view.values.killSwitch.on">Lý do: {{ view.values.killSwitch.reason }} · </template>
          {{ view.values.killSwitch.on ? 'Bật' : 'Tắt' }} bởi {{ view.values.killSwitch.updatedBy }},
          {{ formatToDateTime(view.values.killSwitch.updatedAt) }}
        </div>
      </template>
      <template #action>
        <Popconfirm
          v-if="view.values.killSwitch.on"
          title="Tắt công tắc dừng? AI gửi tự động lại ngay."
          placement="bottomRight"
          @confirm="switchOff"
        >
          <Button type="primary">Tắt công tắc dừng</Button>
        </Popconfirm>
        <Button v-else danger @click="openKill(true)">Bật công tắc dừng</Button>
      </template>
    </Alert>

    <BasicForm @register="registerForm" />
    <div class="flex gap-2 justify-end">
      <Button @click="handleDefaults">Điền giá trị mặc định</Button>
      <Button type="primary" :loading="saving" @click="handleSave">Lưu cài đặt</Button>
    </div>

    <BasicModal @register="registerKill" title="Bật công tắc dừng" okText="Bật" @ok="switchOn">
      <Alert
        type="warning"
        show-icon
        class="mb-3"
        message="Mọi lượt AI sẽ chỉ lưu nháp, nhân viên phải duyệt từng tin. Quản trị hệ thống nhận thông báo."
      />
      <InputTextArea v-model:value="reason" :rows="3" :maxlength="500" placeholder="Lý do (bắt buộc)" />
    </BasicModal>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { onMounted, ref } from 'vue';
  import { Alert, Input, Popconfirm } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { PageWrapper } from '@/components/Page';
  import { BasicForm, useForm } from '@/components/Form';
  import { BasicModal, useModal } from '@/components/Modal';
  import { useMessage } from '@/hooks/web/useMessage';
  import { formatToDateTime } from '@/utils/dateUtil';
  import { aiKillSwitchApi, aiSettingsApi, aiSettingsSaveApi } from '@/api/aiSettings';
  import type { AiSettings, AiSettingsView } from '@/api/aiSettings';
  import { SNAKE, defaultText, settingsSchema } from './settings.data';

  defineOptions({ name: 'KnowledgeAiSettings' });

  const InputTextArea = Input.TextArea;
  const { createMessage } = useMessage();
  const view = ref<AiSettingsView>();
  const saving = ref(false);
  const reason = ref('');

  const [registerKill, { openModal: openKill, closeModal: closeKill, setModalProps: setKillProps }] = useModal();
  const [registerForm, { setFieldsValue, validate, updateSchema }] = useForm({
    labelWidth: 280,
    baseColProps: { span: 12 },
    schemas: settingsSchema,
    showActionButtonGroup: false,
  });

  async function load() {
    const v = await aiSettingsApi();
    view.value = v;
    await setFieldsValue(v.values);
    // Ghi giá trị mặc định vào chú thích từng ô, đánh dấu ô đang khác mặc định
    await updateSchema(
      Object.keys(SNAKE).map((field) => ({
        field,
        helpMessage: `Mặc định: ${defaultText(field, (v.defaults as Recordable)[field])}`,
        label:
          (settingsSchema.find((s) => s.field === field)?.label as string) +
          (v.overridden.includes(SNAKE[field]) ? ' (đang đổi)' : ''),
      })),
    );
  }

  async function handleSave() {
    if (!view.value) return;
    const f = await validate();
    saving.value = true;
    try {
      await aiSettingsSaveApi({ ...(view.value.values as AiSettings), ...f } as AiSettings);
      createMessage.success('Đã lưu, có hiệu lực ngay');
      await load();
    } finally {
      saving.value = false;
    }
  }

  async function handleDefaults() {
    if (view.value) {
      await setFieldsValue(view.value.defaults);
      createMessage.info('Đã điền giá trị mặc định; bấm Lưu cài đặt để áp dụng.');
    }
  }

  async function switchOn() {
    if (!reason.value.trim()) {
      createMessage.warning('Nhập lý do bật công tắc dừng');
      return;
    }
    setKillProps({ confirmLoading: true });
    try {
      await aiKillSwitchApi(true, reason.value.trim());
      closeKill();
      reason.value = '';
      createMessage.success('Đã bật công tắc dừng');
      await load();
    } finally {
      setKillProps({ confirmLoading: false });
    }
  }

  async function switchOff() {
    await aiKillSwitchApi(false);
    createMessage.success('Đã tắt công tắc dừng');
    await load();
  }

  onMounted(load);
</script>
