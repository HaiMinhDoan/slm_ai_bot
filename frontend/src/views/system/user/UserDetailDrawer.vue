<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="Chi tiết người dùng" width="560px">
    <template v-if="user">
      <Descriptions :column="1" bordered size="small" class="mb-4">
        <DescriptionsItem label="Họ tên">{{ user.fullName }}</DescriptionsItem>
        <DescriptionsItem label="Email">{{ user.email }}</DescriptionsItem>
        <DescriptionsItem label="Trạng thái">
          <Tag :color="STATUS_COLOR[user.status]">{{ statusLabel(user.status) }}</Tag>
          <span v-if="user.lockReason" class="text-gray-500">{{ user.lockReason }}</span>
        </DescriptionsItem>
        <DescriptionsItem label="Đăng nhập gần nhất">
          {{ user.lastLoginAt ? new Date(user.lastLoginAt).toLocaleString('vi-VN') : '—' }}
        </DescriptionsItem>
        <DescriptionsItem label="Ngày tạo">
          {{ new Date(user.createdAt).toLocaleString('vi-VN') }}
        </DescriptionsItem>
      </Descriptions>

      <Divider>Vai trò</Divider>
      <CheckboxGroup v-model:value="selectedRoles" class="mb-3">
        <div v-for="r in roleOptions" :key="r.code" class="mb-1">
          <Checkbox :value="r.code">{{ r.name }}</Checkbox>
          <span v-if="r.description" class="text-gray-500 text-xs">{{ r.description }}</span>
        </div>
      </CheckboxGroup>
      <div>
        <Button type="primary" size="small" :loading="savingRoles" @click="saveRoles">Lưu vai trò</Button>
      </div>

      <Divider>Trạng thái tài khoản</Divider>
      <RadioGroup v-model:value="statusDraft" class="mb-3">
        <Radio v-for="o in STATUS_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</Radio>
      </RadioGroup>
      <Input
        v-if="statusDraft !== 'ACTIVE'"
        v-model:value="lockReasonDraft"
        placeholder="Lý do khóa / vô hiệu hóa"
        class="mb-3"
      />
      <div>
        <Button type="primary" size="small" :loading="savingStatus" @click="saveStatus">Lưu trạng thái</Button>
      </div>

      <Divider>Đặt lại mật khẩu</Divider>
      <Space>
        <InputPassword v-model:value="newPassword" placeholder="Mật khẩu mới (8–72 ký tự)" style="width: 260px" />
        <Button size="small" :loading="resettingPassword" @click="resetPassword">Đặt lại</Button>
      </Space>
      <p class="text-gray-500 text-xs mt-2">Mọi phiên đăng nhập của người này sẽ bị đăng xuất.</p>
    </template>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  // Dự án không đăng ký sẵn component Ant Design: import ngay trong file dùng nó,
  // thiếu import thì Vue coi <a-descriptions>... là thẻ HTML lạ mà không báo lỗi.
  import { Button, Checkbox, Descriptions, Divider, Input, Radio, Space, Tag } from 'ant-design-vue';
  import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
  import { useMessage } from '@/hooks/web/useMessage';
  import {
    userDetailApi,
    userResetPasswordApi,
    userRoleOptionsApi,
    userSetRolesApi,
    userSetStatusApi,
  } from '@/api/system/user';
  import type { RoleOption, UserAdminModel, UserStatus } from '@/api/system/model/systemModel';
  import { STATUS_COLOR, STATUS_OPTIONS, statusLabel } from './user.data';

  const DescriptionsItem = Descriptions.Item;
  const CheckboxGroup = Checkbox.Group;
  const RadioGroup = Radio.Group;
  const InputPassword = Input.Password;

  defineOptions({ name: 'UserDetailDrawer' });

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();

  const user = ref<UserAdminModel>();
  const roleOptions = ref<RoleOption[]>([]);
  const selectedRoles = ref<string[]>([]);
  const statusDraft = ref<UserStatus>('ACTIVE');
  const lockReasonDraft = ref('');
  const newPassword = ref('');
  const savingRoles = ref(false);
  const savingStatus = ref(false);
  const resettingPassword = ref(false);

  const [registerDrawer] = useDrawerInner(async (data) => {
    if (!roleOptions.value.length) {
      roleOptions.value = await userRoleOptionsApi();
    }
    user.value = await userDetailApi(data?.id as string);
    selectedRoles.value = [...user.value.roleCodes];
    statusDraft.value = user.value.status;
    lockReasonDraft.value = user.value.lockReason ?? '';
    newPassword.value = '';
  });

  async function saveRoles() {
    if (!user.value) return;
    savingRoles.value = true;
    try {
      user.value = await userSetRolesApi(user.value.id, selectedRoles.value);
      emit('success');
    } finally {
      savingRoles.value = false;
    }
  }

  async function saveStatus() {
    if (!user.value) return;
    savingStatus.value = true;
    try {
      user.value = await userSetStatusApi(
        user.value.id,
        statusDraft.value,
        statusDraft.value === 'ACTIVE' ? undefined : lockReasonDraft.value,
      );
      emit('success');
    } finally {
      savingStatus.value = false;
    }
  }

  async function resetPassword() {
    if (!user.value) return;
    if (newPassword.value.length < 8 || newPassword.value.length > 72) {
      createMessage.warning('Mật khẩu phải từ 8 đến 72 ký tự');
      return;
    }
    resettingPassword.value = true;
    try {
      await userResetPasswordApi(user.value.id, newPassword.value);
      newPassword.value = '';
    } finally {
      resettingPassword.value = false;
    }
  }
</script>
