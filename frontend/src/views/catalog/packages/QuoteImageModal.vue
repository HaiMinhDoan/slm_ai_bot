<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="title"
    width="680px"
    :minHeight="480"
    :showOkBtn="false"
    cancelText="Đóng"
  >
    <Alert
      type="info"
      show-icon
      class="mb-3"
      message="Bot gửi ảnh này khi gói chưa có ảnh báo giá đã duyệt trong thư viện media"
      description="Dựng theo mẫu ảnh báo giá từ số liệu catalog hiện hành: giá trọn gói, thiết bị chính, giá niêm yết, logo hãng, ảnh vật tư. Số liệu gói đổi thì ảnh tự dựng lại; lần đầu mất vài giây."
    />
    <QuoteImage v-if="packageId" :key="packageId" :packageId="packageId" :width="600" />
  </BasicModal>
</template>

<script lang="ts" setup>
  import { ref } from 'vue';
  import { Alert } from 'ant-design-vue';
  import { BasicModal, useModalInner } from '@/components/Modal';
  import QuoteImage from '../QuoteImage.vue';

  defineOptions({ name: 'PackageQuoteImageModal' });

  defineEmits(['register']);

  const packageId = ref<number>();
  const title = ref('Ảnh báo giá');

  const [registerModal] = useModalInner((data: { id: number; name?: string }) => {
    packageId.value = data.id;
    title.value = `Ảnh báo giá gói ${data.id}${data.name ? ' · ' + data.name : ''}`;
  });
</script>
