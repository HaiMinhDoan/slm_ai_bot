<template>
  <div>
    <RuleHeader
      :detail="detail"
      description="Tỉnh khách nói (kể cả tên tỉnh cũ, tên viết tắt) được quy đổi sang tỉnh mới → miền giá → bảng giá của cơ sở và link website theo miền (mục 4.2.9)."
      :errors="errors.length ? errors : detail.ruleErrors"
      :saving="saving"
      @save="handleSave()"
      @discard="discard"
      @diff="diff"
      @yaml="handleYaml"
    />
    <BasicForm @register="registerForm" />
    <BasicTable @register="registerTable">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'mien'">
          <Tag :color="record.mien === 'BAC' ? 'blue' : 'orange'">
            {{ record.mien === 'BAC' ? 'Miền Bắc' : 'Miền Nam' }}
          </Tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <TableAction
            :actions="[{ icon: 'clarity:note-edit-line', tooltip: 'Sửa', onClick: handleEdit.bind(null, record) }]"
          />
        </template>
      </template>
    </BasicTable>
    <BasicModal @register="registerProvince" title="Sửa tỉnh" width="640px" @ok="handleProvinceOk">
      <BasicForm @register="registerProvinceForm" />
    </BasicModal>
    <DiffModal @register="registerDiff" @ok="handleSave(true)" />
    <VersionModal @register="registerVersion" />
  </div>
</template>

<script lang="ts" setup>
  import { onMounted, ref, watch } from 'vue';
  import { Tag } from 'ant-design-vue';
  import { BasicForm, useForm } from '@/components/Form';
  import { BasicModal, useModal } from '@/components/Modal';
  import { BasicTable, TableAction, useTable } from '@/components/Table';
  import { useMessage } from '@/hooks/web/useMessage';
  import type { KnowledgeDocDetail } from '@/api/knowledge/model/knowledgeModel';
  import DiffModal from '../shared/DiffModal.vue';
  import VersionModal from '../shared/VersionModal.vue';
  import RuleHeader from './RuleHeader.vue';
  import { PROVINCE_FORM, REGIONS_FORM } from './rules.data';
  import { useRuleSave } from './useRuleSave';

  defineOptions({ name: 'KnowledgeRegionsTab' });

  const props = defineProps<{ detail: KnowledgeDocDetail }>();
  const emit = defineEmits(['saved']);
  const { createMessage } = useMessage();

  /** Danh sách tỉnh đang sửa (lưu cùng form khi bấm Lưu nháp) */
  const provinces = ref<Recordable[]>([]);
  const editing = ref<number>(-1);

  const [registerDiff, { openModal: openDiffModal }] = useModal();
  const [registerVersion, { openModal: openVersion }] = useModal();
  const [registerProvince, { openModal: openProvince, closeModal: closeProvince }] = useModal();
  const { saving, errors, save, discard, diff } = useRuleSave(
    'tinh-mien',
    () => props.detail,
    (d) => emit('saved', d),
    (data) => openDiffModal(true, data),
  );

  const [registerForm, { setFieldsValue, validate }] = useForm({
    labelWidth: 160,
    baseColProps: { span: 12 },
    schemas: REGIONS_FORM,
    showActionButtonGroup: false,
  });

  const [registerProvinceForm, provinceForm] = useForm({
    labelWidth: 150,
    baseColProps: { span: 24 },
    schemas: PROVINCE_FORM,
    showActionButtonGroup: false,
  });

  const [registerTable, { setTableData }] = useTable({
    title: 'Tỉnh, thành phố',
    columns: [
      { title: 'Mã', dataIndex: 'ma', width: 140 },
      { title: 'Tên', dataIndex: 'ten', width: 160, align: 'left' },
      { title: 'Miền giá', dataIndex: 'mien', key: 'mien', width: 110 },
      {
        title: 'Tỉnh cũ đã sáp nhập',
        dataIndex: 'tinh_cu',
        align: 'left',
        customRender: ({ record }) => (record.tinh_cu ?? []).join(', '),
      },
      {
        title: 'Tên gọi khác',
        dataIndex: 'ten_khac',
        align: 'left',
        customRender: ({ record }) => (record.ten_khac ?? []).join(', '),
      },
    ],
    rowKey: 'ma',
    bordered: true,
    striped: false,
    canResize: false,
    actionColumn: { width: 70, title: 'Sửa', dataIndex: 'action', fixed: 'right' },
  });

  function fill() {
    const v = props.detail.value;
    if (!v) return;
    setFieldsValue({
      nguon: v.nguon,
      bac_ten: v.mien?.BAC?.ten,
      bac_co_so: v.mien?.BAC?.co_so,
      nam_ten: v.mien?.NAM?.ten,
      nam_co_so: v.mien?.NAM?.co_so,
    });
    provinces.value = (v.tinh ?? []).map((t: Recordable) => ({ ...t }));
    setTableData(provinces.value);
  }

  onMounted(fill);
  watch(() => props.detail, fill);

  function handleEdit(record: Recordable) {
    editing.value = provinces.value.findIndex((p) => p.ma === record.ma);
    openProvince(true);
    // Modal mở xong form mới có trong DOM
    setTimeout(() => provinceForm.setFieldsValue({ ...record }), 0);
  }

  async function handleProvinceOk() {
    const f = await provinceForm.validate();
    provinces.value[editing.value] = { ...provinces.value[editing.value], ...f };
    setTableData([...provinces.value]);
    closeProvince();
    createMessage.info('Đã sửa trong bảng; bấm Lưu nháp để lưu.');
  }

  async function handleSave(force = false) {
    const f = await validate();
    await save(
      {
        nguon: f.nguon ?? '',
        mien: {
          BAC: { ten: f.bac_ten, co_so: f.bac_co_so },
          NAM: { ten: f.nam_ten, co_so: f.nam_co_so },
        },
        tinh: provinces.value.map((p) => ({
          ma: p.ma,
          ten: p.ten,
          mien: p.mien,
          tinh_cu: p.tinh_cu ?? [],
          ten_khac: p.ten_khac ?? [],
        })),
      },
      force,
    );
  }

  function handleYaml() {
    const v = props.detail.draft ?? props.detail.latest;
    if (v) openVersion(true, { versionId: v.id, key: props.detail.doc.key, yaml: true });
  }
</script>
