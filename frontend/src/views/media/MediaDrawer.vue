<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    :title="media ? `Sửa media${media.indexCode ? ' ' + media.indexCode : ''}` : 'Sửa media'"
    width="720px"
    showFooter
    @ok="handleSubmit"
  >
    <div v-if="media" class="media-head">
      <MediaThumb
        v-if="storedSource"
        :key="storedSource.id"
        :mediaId="media.id"
        :sourceId="storedSource.id"
        :size="120"
      />
      <div class="media-meta">
        <div v-for="s in media.sources" :key="s.id">
          {{ SOURCE_LABELS[s.sourceType] ?? s.sourceType }}
          <span v-if="s.width">· {{ s.width }}×{{ s.height }}</span>
          <span v-if="s.sizeBytes">· {{ Math.round(s.sizeBytes / 1024) }} KB</span>
          <a v-if="s.sourceUrl" :href="s.sourceUrl" target="_blank" rel="noopener noreferrer">
            · link gốc
          </a>
        </div>
        <div class="tags">
          <Tag v-for="l in media.links" :key="l.targetType + l.targetKey + l.role">
            {{ linkText(l) }}
          </Tag>
        </div>
        <div v-if="media.approvedBy" class="muted">
          Duyệt bởi {{ media.approvedBy }} lúc {{ formatToDateTime(media.approvedAt) }}
        </div>
        <div v-if="media.updatedBy" class="muted">Sửa gần nhất bởi {{ media.updatedBy }}</div>
      </div>
    </div>
    <Alert
      type="info"
      show-icon
      class="mb-3"
      message="AI chỉ hiểu media qua Tóm tắt, Dùng khi và Tránh khi"
      description="Viết rõ media nói gì và khi nào nên gửi. Chỉ mục Đã duyệt mới được AI chọn gửi cho khách (khi có tính năng gửi media); lần duyệt đầu cấp mã M01, M02... cho danh mục media."
    />
    <BasicForm @register="registerForm" />
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { Alert, Tag } from 'ant-design-vue';
  import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
  import { BasicForm, useForm } from '@/components/Form';
  import { useMessage } from '@/hooks/web/useMessage';
  import { formatToDateTime } from '@/utils/dateUtil';

  import MediaThumb from './MediaThumb.vue';
  import { SOURCE_LABELS, linkText, mediaFormSchema } from './media.data';
  import { mediaGetApi, mediaUpdateApi } from '@/api/media';
  import type { MediaModel, MediaUpdateModel } from '@/api/media/model/mediaModel';

  defineOptions({ name: 'MediaDrawer' });

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();

  const media = ref<MediaModel>();
  const storedSource = computed(() => media.value?.sources.find((s) => s.stored));

  const [registerForm, { resetFields, setFieldsValue, validate }] = useForm({
    labelWidth: 130,
    schemas: mediaFormSchema,
    showActionButtonGroup: false,
  });

  const [registerDrawer, { setDrawerProps, closeDrawer, changeLoading }] = useDrawerInner(
    async (data) => {
      media.value = undefined;
      await resetFields();
      setDrawerProps({ confirmLoading: false });
      changeLoading(true);
      try {
        const m = await mediaGetApi(data.id as string);
        media.value = m;
        await setFieldsValue({
          type: m.type,
          title: m.title,
          status: m.status,
          rights: m.rights,
          summary: m.summary,
          useWhen: m.useWhen,
          avoidWhen: m.avoidWhen,
          description: m.description,
          transcript: m.transcript,
        });
      } finally {
        changeLoading(false);
      }
    },
  );

  async function handleSubmit() {
    if (!media.value) return;
    try {
      const { type: _type, ...values } = await validate();
      setDrawerProps({ confirmLoading: true });
      await mediaUpdateApi(media.value.id, values as MediaUpdateModel);
      createMessage.success('Đã lưu media');
      closeDrawer();
      emit('success');
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }
</script>

<style lang="less" scoped>
  .media-head {
    display: flex;
    gap: 16px;
    margin-bottom: 12px;
  }

  .media-meta {
    flex: 1;
    min-width: 0;
    font-size: 13px;
  }

  .tags {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    margin: 6px 0;
  }

  .muted {
    color: #888;
    font-size: 12px;
  }
</style>
