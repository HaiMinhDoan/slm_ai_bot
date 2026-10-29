<template>
  <PageWrapper
    dense
    contentFullHeight
    title="Thử báo giá"
    content="Nhập thông tin như khách nhắn. Kết quả dưới đây đúng là dữ liệu AI nhận được từ công cụ tim_goi_phu_hop; AI chỉ được nói các con số và link có trong kết quả."
  >
    <div class="p-4 bg-white">
      <BasicForm @register="registerForm" @submit="handleSubmit" @reset="result = undefined" />
    </div>

    <div v-if="result" class="p-4 mt-2 bg-white">
      <Alert
        v-if="result.co_bang_gia === false"
        type="warning"
        show-icon
        class="mb-2"
        message="Chưa có bảng giá: catalog chưa đồng bộ thành công lần nào"
      />
      <Alert
        v-for="n in result.ghi_chu ?? []"
        :key="n"
        type="warning"
        show-icon
        class="mb-2"
        :message="n"
      />

      <Description
        title="Khu vực và ước tính"
        :data="summary.data"
        :schema="summary.schema"
        :column="2"
        size="small"
        class="mb-3"
      />

      <template v-if="result.gia_khoi_diem?.length">
        <Divider orientation="left">Giá khởi điểm (chưa có tiền điện)</Divider>
        <div class="cards">
          <PackageCard
            v-for="g in result.gia_khoi_diem"
            :key="g.loai"
            :pkg="g.goi_re_nhat"
            :heading="g.loai + ': từ ' + g.gia_tu"
            @detail="handleDetail"
          />
        </div>
      </template>
      <template v-if="result.goi_ban_chay?.length">
        <Divider orientation="left">Gói bán chạy</Divider>
        <div class="cards">
          <PackageCard
            v-for="g in result.goi_ban_chay"
            :key="g.goi_id"
            :pkg="g"
            @detail="handleDetail"
          />
        </div>
      </template>
      <template v-if="result.goi_de_xuat?.length">
        <Divider orientation="left">Gói đề xuất</Divider>
        <div class="cards">
          <PackageCard
            v-for="g in result.goi_de_xuat"
            :key="g.goi_id"
            :pkg="g"
            @detail="handleDetail"
          />
        </div>
      </template>
      <Alert
        v-if="result.dieu_kien_bao_gia"
        class="mt-3"
        type="info"
        :message="result.dieu_kien_bao_gia"
      />

      <Collapse class="mt-3">
        <CollapsePanel key="json" header="Dữ liệu AI nhận được (JSON)">
          <pre class="json">{{ JSON.stringify(result, null, 2) }}</pre>
        </CollapsePanel>
      </Collapse>
    </div>

    <PackageDetailModal @register="registerDetailModal" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { Alert, Collapse, Divider } from 'ant-design-vue';
  import { BasicForm, useForm } from '@/components/Form';
  import { Description } from '@/components/Description';
  import { PageWrapper } from '@/components/Page';
  import { useModal } from '@/components/Modal';

  import PackageCard from './PackageCard.vue';
  import PackageDetailModal from './PackageDetailModal.vue';
  import { adviseFormSchema, summaryOf } from './advise.data';
  import { catalogAdviseApi } from '@/api/catalog';

  defineOptions({ name: 'CatalogAdvise' });

  const CollapsePanel = Collapse.Panel;

  const [registerDetailModal, { openModal: openDetailModal }] = useModal();
  const result = ref<Recordable>();
  const province = ref<string>();

  const summary = computed(() => summaryOf(result.value ?? {}));

  const [registerForm, { setProps }] = useForm({
    labelWidth: 130,
    schemas: adviseFormSchema,
    baseColProps: { span: 8 },
    actionColOptions: { span: 24 },
    submitButtonOptions: { text: 'Tính' },
    resetButtonOptions: { text: 'Xoá' },
  });

  async function handleSubmit(values: Recordable) {
    province.value = values.province?.trim() || undefined;
    setProps({ submitButtonOptions: { loading: true } });
    try {
      result.value = await catalogAdviseApi({
        ...values,
        province: province.value,
        backupNeeded: values.backupNeeded == null ? undefined : values.backupNeeded === 'true',
      });
    } finally {
      setProps({ submitButtonOptions: { loading: false } });
    }
  }

  function handleDetail(id: number) {
    openDetailModal(true, { id, province: province.value });
  }
</script>

<style lang="less" scoped>
  .cards {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 12px;
  }

  .json {
    max-height: 420px;
    overflow: auto;
    font-size: 12px;
    white-space: pre-wrap;
  }
</style>
