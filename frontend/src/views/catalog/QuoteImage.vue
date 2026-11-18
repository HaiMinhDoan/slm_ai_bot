<template>
  <div class="quote-image">
    <Image v-if="url" :src="url" :width="width" class="img" />
    <Spin v-else-if="loading" size="small" />
    <TypographyText v-else type="secondary" class="text-xs">{{ error }}</TypographyText>
  </div>
</template>

<script lang="ts" setup>
  import { onBeforeUnmount, ref, watch } from 'vue';
  import { Image, Spin, Typography } from 'ant-design-vue';
  import { catalogQuoteImageBlobApi } from '@/api/catalog';

  defineOptions({ name: 'CatalogQuoteImage' });

  /** Ảnh báo giá tự tạo của gói (bot gửi khi gói chưa có ảnh báo giá trong thư viện media), tải dạng blob */
  const props = withDefaults(defineProps<{ packageId?: number; width?: number }>(), { width: 96 });

  const TypographyText = Typography.Text;
  const url = ref<string>();
  const loading = ref(false);
  const error = ref('—');

  /** Lỗi trả về dạng blob JSON ({ message }) vì request xin blob */
  async function message(e: any): Promise<string> {
    try {
      const data = e?.response?.data;
      const text = data instanceof Blob ? await data.text() : '';
      return JSON.parse(text)?.message || 'Không tạo được ảnh báo giá';
    } catch {
      return 'Không tạo được ảnh báo giá';
    }
  }

  async function load(id?: number) {
    if (url.value) URL.revokeObjectURL(url.value);
    url.value = undefined;
    error.value = '—';
    if (!id) return;
    loading.value = true;
    try {
      const res = await catalogQuoteImageBlobApi(id);
      url.value = URL.createObjectURL(res.data);
    } catch (e) {
      error.value = await message(e);
    } finally {
      loading.value = false;
    }
  }

  watch(() => props.packageId, load, { immediate: true });
  onBeforeUnmount(() => url.value && URL.revokeObjectURL(url.value));
</script>

<style lang="less" scoped>
  .quote-image {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 32px;
  }

  .img {
    border-radius: 4px;
  }
</style>
