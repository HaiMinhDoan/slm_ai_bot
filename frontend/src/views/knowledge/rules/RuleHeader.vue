<template>
  <div class="mb-3">
    <div class="flex flex-wrap items-center gap-2">
      <Tag v-if="detail.active" color="green">Bot đang dùng v{{ detail.active.versionNo }}</Tag>
      <Tag v-else>Chưa phát hành</Tag>
      <Tag v-if="detail.draft" color="orange">
        Có nháp v{{ detail.draft.versionNo }} chưa phát hành ({{ detail.draft.author }},
        {{ formatToDateTime(detail.draft.updatedAt) }})
      </Tag>
      <div class="ml-auto flex gap-2">
        <Button @click="$emit('yaml')">Xem YAML</Button>
        <Button :disabled="!detail.draft" @click="$emit('diff')">So với bản đang chạy</Button>
        <Popconfirm
          v-if="detail.draft"
          title="Bỏ bản nháp? Rule đang chạy không đổi."
          placement="bottomRight"
          @confirm="$emit('discard')"
        >
          <Button danger>Bỏ nháp</Button>
        </Popconfirm>
        <Button type="primary" :loading="saving" @click="$emit('save')">Lưu nháp</Button>
      </div>
    </div>
    <div class="mt-2 text-gray-500">{{ description }}</div>
    <Alert v-if="errors.length" type="error" show-icon class="mt-2" message="Rule chưa hợp lệ">
      <template #description>
        <div v-for="(e, i) in errors" :key="i">• {{ e }}</div>
      </template>
    </Alert>
  </div>
</template>

<script lang="ts" setup>
  import { Alert, Popconfirm, Tag } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { formatToDateTime } from '@/utils/dateUtil';
  import type { KnowledgeDocDetail } from '@/api/knowledge/model/knowledgeModel';

  defineOptions({ name: 'KnowledgeRuleHeader' });

  defineProps<{ detail: KnowledgeDocDetail; description: string; errors: string[]; saving?: boolean }>();
  defineEmits(['save', 'discard', 'diff', 'yaml']);
</script>
