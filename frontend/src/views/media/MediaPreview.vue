<template>
  <MediaThumb
    v-if="stored"
    :key="stored.id"
    :mediaId="media.id"
    :sourceId="stored.id"
    :size="size"
  />
  <MediaThumb
    v-else-if="withThumb"
    :key="'t' + withThumb.id"
    :mediaId="media.id"
    :sourceId="withThumb.id"
    :size="size"
    thumbnail
  />
  <Image
    v-else-if="external?.thumbnailUrl"
    :src="external.thumbnailUrl"
    :width="size"
    :height="size"
    class="img"
  />
  <a v-else-if="external" :href="external.sourceUrl" target="_blank" rel="noopener noreferrer">Mở link</a>
  <span v-else class="empty">—</span>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { Image } from 'ant-design-vue';
  import type { MediaModel } from '@/api/media/model/mediaModel';
  import MediaThumb from './MediaThumb.vue';

  /** Ảnh xem trước của một media: file ảnh, ảnh thu nhỏ video tải lên, hoặc ảnh xem trước của link ngoài */
  const props = withDefaults(defineProps<{ media: MediaModel; size?: number }>(), { size: 64 });

  const stored = computed(() =>
    props.media.sources.find((s) => s.stored && s.mimeType?.startsWith('image/')),
  );
  const withThumb = computed(() => props.media.sources.find((s) => s.hasThumbnail));
  const external = computed(() => props.media.sources.find((s) => !s.stored && s.sourceUrl));
</script>

<style lang="less" scoped>
  .img {
    border-radius: 4px;
    object-fit: cover;
  }

  .empty {
    color: #999;
  }
</style>
