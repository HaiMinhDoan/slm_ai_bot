<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="detail?.ten ?? 'Chi tiết gói'"
    width="720px"
    :showOkBtn="false"
    cancelText="Đóng"
  >
    <template v-if="detail">
      <Alert
        v-for="n in detail.ghi_chu ?? []"
        :key="n"
        type="warning"
        show-icon
        class="mb-2"
        :message="n"
      />
      <Table
        v-if="detail.thanh_phan"
        :columns="itemColumns"
        :data-source="detail.thanh_phan"
        :pagination="false"
        rowKey="ten"
        size="small"
        bordered
        class="mb-3"
      />
      <pre class="json">{{ JSON.stringify(detail, null, 2) }}</pre>
    </template>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { Alert, Table } from 'ant-design-vue';
  import { BasicModal, useModalInner } from '@/components/Modal';

  import { itemColumns } from './advise.data';
  import { catalogPackageDetailApi } from '@/api/catalog';

  defineOptions({ name: 'PackageDetailModal' });

  defineEmits(['register']);

  /** Đúng dữ liệu AI nhận được từ công cụ chi_tiet_goi */
  const detail = ref<Recordable>();

  const [registerModal, { changeLoading }] = useModalInner(async (data) => {
    detail.value = undefined;
    changeLoading(true);
    try {
      detail.value = await catalogPackageDetailApi(data.id, data.province);
    } finally {
      changeLoading(false);
    }
  });
</script>

<style lang="less" scoped>
  .json {
    max-height: 360px;
    overflow: auto;
    font-size: 12px;
    white-space: pre-wrap;
  }
</style>
