<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="Tạo tài liệu"
    okText="Tạo và soạn"
    width="640px"
    @ok="handleSubmit"
  >
    <BasicForm @register="registerForm" @field-value-change="handleFieldChange" />
    <div class="text-gray-500">
      Mã tài liệu: <b>{{ previewKey }}</b>. Tài liệu mới mở bằng mẫu của loại đó (guide/mau) để soạn tiếp.
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { BasicModal, useModalInner } from '@/components/Modal';
  import { BasicForm, useForm } from '@/components/Form';
  import { useMessage } from '@/hooks/web/useMessage';
  import { knowledgeCreateApi } from '@/api/knowledge';
  import type { KnowledgeDocType } from '@/api/knowledge/model/knowledgeModel';
  import { DOC_TYPE_FOLDERS } from '../shared/labels';
  import { createFormSchema, slug } from './docs.data';

  defineOptions({ name: 'KnowledgeCreateDocModal' });

  /** Tạo xong phát `success` kèm id tài liệu mới để mở trình soạn */
  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();
  const type = ref<KnowledgeDocType>('POLICY');
  const name = ref('');
  /** Người dùng đã tự sửa tên trong mã: không tự gợi ý theo tiêu đề nữa */
  const nameTouched = ref(false);
  const previewKey = computed(() => `${DOC_TYPE_FOLDERS[type.value]}/${name.value || '...'}`);

  const [registerForm, { validate, resetFields, setFieldsValue }] = useForm({
    labelWidth: 110,
    baseColProps: { span: 24 },
    schemas: createFormSchema,
    showActionButtonGroup: false,
  });

  const [registerModal, { closeModal, setModalProps }] = useModalInner(async () => {
    await resetFields();
    type.value = 'POLICY';
    name.value = '';
    nameTouched.value = false;
  });

  function handleFieldChange(field: string, value: any) {
    if (field === 'type') {
      type.value = value;
    } else if (field === 'title' && !nameTouched.value) {
      name.value = slug(value ?? '');
      setFieldsValue({ name: name.value });
    } else if (field === 'name') {
      if (value !== name.value) {
        nameTouched.value = true;
      }
      name.value = value ?? '';
    }
  }

  async function handleSubmit() {
    const values = await validate();
    setModalProps({ confirmLoading: true });
    try {
      const created = await knowledgeCreateApi({
        type: values.type,
        key: `${DOC_TYPE_FOLDERS[values.type as KnowledgeDocType]}/${values.name}`,
        title: values.title,
      });
      createMessage.success('Đã tạo bản nháp v1');
      closeModal();
      emit('success', created.doc.id);
    } finally {
      setModalProps({ confirmLoading: false });
    }
  }
</script>
