<template>
  <div>
    <Alert
      type="info"
      show-icon
      class="mb-3"
      message="Nhập thông tin như khách nhắn. Kết quả dưới đây đúng là dữ liệu AI nhận được từ công cụ tim_goi_phu_hop; AI chỉ được nói các con số và link có trong kết quả."
    />
    <Form layout="vertical" class="form">
      <FormItem label="Tỉnh / thành (tên mới hoặc cũ)">
        <Input v-model:value="req.province" placeholder="Ví dụ: Bình Dương, Hà Nội" allowClear />
      </FormItem>
      <FormItem label="Tiền điện mỗi tháng (đồng)">
        <InputNumber
          v-model:value="req.monthlyBillVnd"
          :min="0"
          :step="100000"
          class="w-full"
          :formatter="(v: any) => (v ? Number(v).toLocaleString('vi-VN') : '')"
          :parser="(v: string) => Number(v.replace(/\D/g, ''))"
        />
      </FormItem>
      <FormItem label="Số pha">
        <Select v-model:value="req.phases" :options="phaseOptions" allowClear />
      </FormItem>
      <FormItem label="Dùng điện">
        <Select v-model:value="req.dayNight" :options="dayNightOptions" allowClear />
      </FormItem>
      <FormItem label="Loại mái">
        <Select v-model:value="req.roofType" :options="roofOptions" allowClear />
      </FormItem>
      <FormItem label="Diện tích mái (m²)">
        <InputNumber v-model:value="req.roofAreaM2" :min="0" class="w-full" />
      </FormItem>
      <FormItem label="Khách muốn loại hệ">
        <Select v-model:value="req.systemType" :options="typeOptions" allowClear />
      </FormItem>
      <FormItem label="Cần dự phòng khi mất điện">
        <Select v-model:value="backup" :options="backupOptions" allowClear />
      </FormItem>
    </Form>
    <Space class="mb-3">
      <Button type="primary" :loading="loading" @click="run">Tính</Button>
      <Button @click="reset">Xoá</Button>
    </Space>

    <template v-if="result">
      <Alert v-if="result.co_bang_gia === false" type="warning" show-icon class="mb-2" message="Chưa có bảng giá" />
      <Alert
        v-for="n in result.ghi_chu ?? []"
        :key="n"
        type="warning"
        show-icon
        class="mb-2"
        :message="n"
      />
      <Descriptions :column="{ xs: 1, md: 2 }" bordered size="small" class="mb-3" title="Khu vực và ước tính">
        <DescriptionsItem v-for="(v, k) in flat(result.khu_vuc)" :key="'k' + k" :label="KEY_LABELS[k] ?? k">
          {{ v }}
        </DescriptionsItem>
        <DescriptionsItem v-for="(v, k) in flat(result.uoc_tinh)" :key="'u' + k" :label="KEY_LABELS[k] ?? k">
          {{ v }}
        </DescriptionsItem>
        <DescriptionsItem v-if="result.khoang_gia" label="Khoảng giá">
          {{ result.khoang_gia.tu }} đến {{ result.khoang_gia.den }}
        </DescriptionsItem>
        <DescriptionsItem v-if="result.con_thieu?.length" label="Còn thiếu">
          <Tag v-for="c in result.con_thieu" :key="c">{{ KEY_LABELS[c] ?? c }}</Tag>
        </DescriptionsItem>
      </Descriptions>

      <template v-if="result.gia_khoi_diem?.length">
        <Divider orientation="left">Giá khởi điểm (chưa có tiền điện)</Divider>
        <div class="cards">
          <PackageCard
            v-for="g in result.gia_khoi_diem"
            :key="g.loai"
            :pkg="g.goi_re_nhat"
            :heading="g.loai + ': từ ' + g.gia_tu"
            @detail="openDetail"
          />
        </div>
      </template>
      <template v-if="result.goi_ban_chay?.length">
        <Divider orientation="left">Gói bán chạy</Divider>
        <div class="cards">
          <PackageCard v-for="g in result.goi_ban_chay" :key="g.goi_id" :pkg="g" @detail="openDetail" />
        </div>
      </template>
      <template v-if="result.goi_de_xuat?.length">
        <Divider orientation="left">Gói đề xuất</Divider>
        <div class="cards">
          <PackageCard v-for="g in result.goi_de_xuat" :key="g.goi_id" :pkg="g" @detail="openDetail" />
        </div>
      </template>
      <Alert v-if="result.dieu_kien_bao_gia" class="mt-3" type="info" :message="result.dieu_kien_bao_gia" />

      <Collapse class="mt-3">
        <CollapsePanel key="json" header="Dữ liệu AI nhận được (JSON)">
          <pre class="json">{{ JSON.stringify(result, null, 2) }}</pre>
        </CollapsePanel>
      </Collapse>
    </template>

    <Modal v-model:open="detailOpen" :title="detail?.ten ?? 'Chi tiết gói'" :footer="null" width="640px">
      <Spin :spinning="detailLoading">
        <template v-if="detail">
          <Alert v-for="n in detail.ghi_chu ?? []" :key="n" type="warning" class="mb-2" :message="n" />
          <Table
            v-if="detail.thanh_phan"
            :dataSource="detail.thanh_phan"
            :columns="itemColumns"
            :pagination="false"
            size="small"
            rowKey="ten"
          />
          <pre class="json mt-2">{{ JSON.stringify(detail, null, 2) }}</pre>
        </template>
      </Spin>
    </Modal>
  </div>
</template>

<script lang="ts" setup>
  import { reactive, ref } from 'vue';
  import {
    Alert,
    Collapse,
    Descriptions,
    Divider,
    Form,
    Input,
    InputNumber,
    Modal,
    Select,
    Space,
    Spin,
    Table,
    Tag,
  } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { catalogAdviseApi, catalogPackageDetailApi } from '@/api/catalog';
  import type { AdviceRequestModel } from '@/api/catalog/model/catalogModel';
  import PackageCard from './PackageCard.vue';
  import { DAY_NIGHT_LABELS, ROOF_LABELS, SYSTEM_TYPE_LABELS, options } from './labels';

  const FormItem = Form.Item;
  const DescriptionsItem = Descriptions.Item;
  const CollapsePanel = Collapse.Panel;

  const KEY_LABELS: Record<string, string> = {
    tinh: 'Tỉnh',
    tinh_cu: 'Tên cũ',
    khach_noi: 'Khách nói',
    mien: 'Miền',
    bang_gia: 'Bảng giá',
    hoa_don_thang: 'Hoá đơn',
    gia_dien_quy_doi: 'Giá điện quy đổi',
    dien_dung_ngay: 'Điện dùng mỗi ngày',
    dien_dung_thang: 'Điện dùng mỗi tháng',
    ty_le_dung_dem: 'Tỷ lệ dùng đêm',
    cong_suat_can: 'Công suất cần',
    pin_can_neu_dung_hybrid: 'Pin cần (Hybrid)',
    cong_suat_toi_da_theo_mai: 'Tối đa theo mái',
    pvout: 'PV out',
    khu_vuc: 'tỉnh',
    so_pha: 'số pha',
    ty_le_ngay_dem: 'ngày / đêm',
    loai_mai: 'loại mái',
  };

  const phaseOptions = [
    { value: 1, label: '1 pha' },
    { value: 3, label: '3 pha' },
  ];
  const dayNightOptions = options(DAY_NIGHT_LABELS);
  const roofOptions = options(ROOF_LABELS);
  const typeOptions = options(SYSTEM_TYPE_LABELS);
  const backupOptions = [
    { value: 'yes', label: 'Có' },
    { value: 'no', label: 'Không' },
  ];
  const itemColumns = [
    { title: 'Thành phần', dataIndex: 'ten' },
    { title: 'Hãng', dataIndex: 'hang', width: 120 },
    { title: 'Số lượng', dataIndex: 'so_luong', width: 100 },
    { title: 'Bảo hành', dataIndex: 'bao_hanh', width: 140 },
  ];

  const req = reactive<AdviceRequestModel>({});
  const backup = ref<string>();
  const loading = ref(false);
  const result = ref<Recordable>();
  const detailOpen = ref(false);
  const detailLoading = ref(false);
  const detail = ref<Recordable>();

  function flat(o?: Recordable): Record<string, string> {
    return o ?? {};
  }

  async function run() {
    loading.value = true;
    try {
      result.value = await catalogAdviseApi({
        ...req,
        province: req.province?.trim() || undefined,
        backupNeeded: backup.value === undefined ? undefined : backup.value === 'yes',
      });
    } finally {
      loading.value = false;
    }
  }

  function reset() {
    (Object.keys(req) as (keyof AdviceRequestModel)[]).forEach((k) => delete req[k]);
    backup.value = undefined;
    result.value = undefined;
  }

  async function openDetail(id: number) {
    detailOpen.value = true;
    detailLoading.value = true;
    detail.value = undefined;
    try {
      detail.value = await catalogPackageDetailApi(id, req.province?.trim() || undefined);
    } finally {
      detailLoading.value = false;
    }
  }
</script>

<style lang="less" scoped>
  .form {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    column-gap: 12px;
  }

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
