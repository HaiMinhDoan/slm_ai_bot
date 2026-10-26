<template>
  <div class="media-thumb">
    <Image v-if="url" :src="url" :width="size" :height="size" class="img" />
    <Spin v-else-if="loading" size="small" />
    <span v-else class="empty">—</span>
  </div>
</template>

<script lang="ts" setup>
  import { onBeforeUnmount, onMounted, ref } from 'vue';
  import { Image, Spin } from 'ant-design-vue';
  import { mediaContentBlobApi } from '@/api/media';

  /** Ảnh xem trước tải qua backend dạng blob (MinIO nội bộ không mở ra ngoài) */
  const props = withDefaults(defineProps<{ mediaId: string; sourceId: string; size?: number }>(), {
    size: 64,
  });

  const url = ref<string>();
  const loading = ref(true);

  onMounted(async () => {
    try {
      const res = await mediaContentBlobApi(props.mediaId, props.sourceId);
      url.value = URL.createObjectURL(res.data);
    } catch {
      // Không xem trước được (MinIO lỗi): để trống
    } finally {
      loading.value = false;
    }
  });

  onBeforeUnmount(() => {
    if (url.value) URL.revokeObjectURL(url.value);
  });
</script>

<style lang="less" scoped>
  .media-thumb {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 40px;
  }

  .img {
    border-radius: 4px;
    object-fit: cover;
  }

  .empty {
    color: #999;
  }
</style>
