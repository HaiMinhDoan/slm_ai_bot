<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    :title="edit ? `Sửa gói ${edit.current.id}: ${edit.current.name}` : 'Sửa gói'"
    width="720px"
    showFooter
    @ok="handleSubmit"
  >
    <Alert
      type="info"
      show-icon
      class="mb-3"
      message="Gói đồng bộ từ slm_v2"
      description="Sửa ở đây chỉ áp dụng cho bot: slm_v2 không đổi, đồng bộ 10 phút một lần cũng không đè mất. Ô nào để trống hoặc nhập trùng số liệu slm_v2 (ghi ở biểu tượng ? cạnh tên ô) thì bot dùng lại số liệu slm_v2."
    />
    <Alert
      v-if="edit?.current.deleted"
      type="warning"
      show-icon
      class="mb-3"
      message="Gói đã xoá mềm: bot không gợi ý. Khôi phục ở danh sách (lọc trạng thái Đã xoá)."
    />
    <Alert
      v-if="editedText"
      type="warning"
      show-icon
      class="mb-3"
      :message="'Bot đang dùng số liệu đã sửa: ' + editedText"
      :description="
        edit?.override?.updatedBy ? 'Sửa gần nhất bởi ' + edit.override.updatedBy : undefined
      "
    />
    <BasicForm @register="registerForm" />
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { Alert } from 'ant-design-vue';
  import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
  import { BasicForm, useForm } from '@/components/Form';
  import { useMessage } from '@/hooks/web/useMessage';

  import { EDIT_FIELD_LABELS, SOURCE_FIELDS, packageFormSchema, sourceText } from './packages.data';
  import { catalogPackageEditApi, catalogPackageUpdateApi } from '@/api/catalog';
  import type { PackageEditModel, PackageUpdateModel } from '@/api/catalog/model/catalogModel';

  defineOptions({ name: 'PackageDrawer' });

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();

  const edit = ref<PackageEditModel>();
  const editedText = computed(() =>
    (edit.value?.current.edited ?? []).map((f) => EDIT_FIELD_LABELS[f] ?? f).join(', '),
  );

  const [registerForm, { resetFields, setFieldsValue, updateSchema, validate }] = useForm({
    labelWidth: 140,
    schemas: packageFormSchema,
    showActionButtonGroup: false,
  });

  const [registerDrawer, { setDrawerProps, closeDrawer, changeLoading }] = useDrawerInner(
    async (data) => {
      edit.value = undefined;
      await resetFields();
      setDrawerProps({ confirmLoading: false });
      changeLoading(true);
      try {
        const e = await catalogPackageEditApi(data.id as number);
        edit.value = e;
        const p = e.current;
        await setFieldsValue({
          enabled: !p.disabled,
          name: p.name,
          systemType: p.systemType,
          phases: p.phases,
          kwp: p.kwp,
          storageKwh: p.storageKwh,
          panelCount: p.panelCount,
          panelWp: p.panelWp,
          totalPrice: p.totalPrice,
          bestSeller: p.bestSeller,
          note: p.note,
        });
        await updateSchema(
          SOURCE_FIELDS.map((field) => ({
            field,
            helpMessage: 'slm_v2: ' + sourceText(field, e.source),
          })),
        );
      } finally {
        changeLoading(false);
      }
    },
  );

  async function handleSubmit() {
    if (!edit.value) return;
    try {
      const values = (await validate()) as PackageUpdateModel;
      setDrawerProps({ confirmLoading: true });
      await catalogPackageUpdateApi(edit.value.current.id, values);
      createMessage.success(`Đã lưu gói ${edit.value.current.id}`);
      closeDrawer();
      emit('success');
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }
</script>
