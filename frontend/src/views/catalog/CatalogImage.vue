<template>
  <div class="catalog-image">
    <Image v-if="url" :src="url" :width="size" :height="size" class="img" />
    <Spin v-else-if="loading" size="small" />
    <TypographyText v-else type="secondary">—</TypographyText>
  </div>
</template>

<script lang="ts" setup>
  import { onBeforeUnmount, ref, watch } from 'vue';
  import { Image, Spin, Typography } from 'ant-design-vue';
  import { catalogImageBlobApi } from '@/api/catalog';

  defineOptions({ name: 'CatalogImage' });

  /** Ảnh vật tư / gói của slm_v2 (MinIO http) tải qua backend dạng blob, trang https không nhúng thẳng được */
  const props = withDefaults(defineProps<{ fileId?: number; size?: number }>(), { size: 48 });

  const TypographyText = Typography.Text;
  const url = ref<string>();
  const loading = ref(false);

  async function load(id?: number) {
    if (url.value) URL.revokeObjectURL(url.value);
    url.value = undefined;
    if (!id) return;
    loading.value = true;
    try {
      const res = await catalogImageBlobApi(id);
      url.value = URL.createObjectURL(res.data);
    } catch {
      // MinIO của slm_v2 không trả ảnh: để trống
    } finally {
      loading.value = false;
    }
  }

  watch(() => props.fileId, load, { immediate: true });
  onBeforeUnmount(() => url.value && URL.revokeObjectURL(url.value));
</script>

<style lang="less" scoped>
  .catalog-image {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 32px;
  }

  .img {
    object-fit: contain;
  }
</style>
