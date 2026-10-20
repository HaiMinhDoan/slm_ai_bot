<template>
  <BasicModal v-bind="$attrs" @register="registerModal" title="Thêm người dùng" @ok="handleSubmit">
    <BasicForm @register="registerForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { BasicModal, useModalInner } from '@/components/Modal';
  import { BasicForm, useForm, type FormSchema } from '@/components/Form';
  import { userCreateApi, userRoleOptionsApi } from '@/api/system/user';

  defineOptions({ name: 'UserCreateModal' });

  const emit = defineEmits(['success', 'register']);

  const schemas: FormSchema[] = [
    { field: 'fullName', label: 'Họ tên', component: 'Input', required: true },
    {
      field: 'email',
      label: 'Email',
      component: 'Input',
      required: true,
      rules: [{ type: 'email', message: 'Email không hợp lệ' }],
    },
    {
      field: 'password',
      label: 'Mật khẩu tạm',
      component: 'InputPassword',
      required: true,
      helpMessage: 'Gửi cho nhân viên qua kênh riêng; nhân viên đổi lại sau lần đăng nhập đầu',
      rules: [{ min: 8, max: 72, message: 'Mật khẩu phải từ 8 đến 72 ký tự' }],
    },
    {
      field: 'roleCodes',
      label: 'Vai trò',
      component: 'ApiSelect',
      required: true,
      componentProps: {
        mode: 'multiple',
        api: userRoleOptionsApi,
        labelField: 'name',
        valueField: 'code',
      },
    },
  ];

  const [registerForm, { validate, resetFields }] = useForm({
    labelWidth: 110,
    schemas,
    showActionButtonGroup: false,
  });

  const [registerModal, { setModalProps, closeModal }] = useModalInner(async () => {
    await resetFields();
  });

  async function handleSubmit() {
    const values = await validate();
    setModalProps({ confirmLoading: true });
    try {
      await userCreateApi(values as any);
      closeModal();
      emit('success');
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
