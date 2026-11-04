<template>
  <div>
    <div class="flex flex-wrap items-center gap-2 mb-3">
      <Tag v-if="detail.active" color="green">Bot đang dùng v{{ detail.active.versionNo }}</Tag>
      <Tag v-if="detail.draft" color="orange">Có nháp v{{ detail.draft.versionNo }} chưa phát hành</Tag>
      <Button type="primary" class="ml-auto" @click="openDrawer(true, { id: detail.doc.id })">Soạn</Button>
    </div>
    <Alert
      type="warning"
      show-icon
      class="mb-3"
      message="Chỉ dẫn chính AI đọc trước mọi tài liệu kiến thức"
      description="Gồm cách đọc tài liệu, khi nào gọi công cụ báo giá và ý nghĩa từng trường kết quả (goal, intent, reply, collected...). Đổi tên trường hay bỏ quy tắc ở đây có thể làm AI trả sai định dạng: sửa trên bản nháp, phát hành rồi thử ngay."
    />
    <MarkdownViewer :value="(detail.draft ?? detail.latest)?.content ?? ''" />
    <DocDrawer @register="registerDrawer" @success="$emit('saved')" />
  </div>
</template>

<script lang="ts" setup>
  import { Alert, Tag } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { MarkdownViewer } from '@/components/Markdown';
  import { useDrawer } from '@/components/Drawer';
  import type { KnowledgeDocDetail } from '@/api/knowledge/model/knowledgeModel';
  import DocDrawer from '../shared/DocDrawer.vue';

  defineOptions({ name: 'KnowledgePromptTab' });

  defineProps<{ detail: KnowledgeDocDetail }>();
  defineEmits(['saved']);
  const [registerDrawer, { openDrawer }] = useDrawer();
</script>
