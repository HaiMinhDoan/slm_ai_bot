<template>
  <div>
    <Alert
      :type="report.ok ? 'success' : 'error'"
      show-icon
      class="mb-3"
      :message="report.ok ? 'Kiểm tra đạt: tạo được bản phát hành' : 'Kiểm tra không đạt: sửa các lỗi dưới đây'"
    />
    <div v-if="report.tokenTotal" class="mb-3">
      Khối kiến thức bot đọc: <b>{{ report.tokenTotal.toLocaleString('vi-VN') }}</b> /
      {{ report.tokenBudget.toLocaleString('vi-VN') }} token
      <span v-if="report.tokenEstimated" class="text-gray-500">(ước tính)</span>
      <Progress :percent="percent" :status="percent > 100 ? 'exception' : 'normal'" size="small" />
    </div>
    <div v-if="report.changes.length" class="mb-3">
      <div class="font-medium mb-1">Thay đổi so với bản đang chạy</div>
      <div v-for="c in report.changes" :key="c.key" class="text-sm">
        <Tag :color="CHANGE_ACTION[c.action]?.color">{{ CHANGE_ACTION[c.action]?.label }}</Tag>
        <b>{{ c.key }}</b> · {{ c.title }}
        <span class="text-gray-500">
          {{ c.fromVersionNo ? 'v' + c.fromVersionNo : '' }}{{ c.fromVersionNo && c.toVersionNo ? ' → ' : '' }}
          {{ c.toVersionNo ? 'v' + c.toVersionNo : '' }}
        </span>
      </div>
    </div>
    <div v-if="report.checks.length">
      <div class="font-medium mb-1">Kết quả kiểm tra</div>
      <div v-for="(c, i) in report.checks" :key="i" class="text-sm mb-1">
        <Tag :color="CHECK_LEVEL[c.level]?.color">{{ CHECK_LEVEL[c.level]?.label }}</Tag>
        <span class="text-gray-500">Bước {{ c.step }}<template v-if="c.docKey"> · {{ c.docKey }}</template>: </span>
        {{ c.message }}
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import { Alert, Progress, Tag } from 'ant-design-vue';
  import type { ReleaseReport } from '@/api/knowledge/model/knowledgeModel';
  import { CHANGE_ACTION, CHECK_LEVEL } from '../shared/labels';

  defineOptions({ name: 'KnowledgeReportView' });

  const props = defineProps<{ report: ReleaseReport }>();
  const percent = computed(() =>
    props.report.tokenTotal ? Math.round((props.report.tokenTotal * 1000) / props.report.tokenBudget) / 10 : 0,
  );
</script>
