<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" :title="title" width="900px">
    <template v-if="detail">
      <div class="mb-3">
        <Tag :color="RELEASE_STATUS[detail.release.status]?.color">
          {{ RELEASE_STATUS[detail.release.status]?.label }}
        </Tag>
        <span class="text-gray-500">
          Tạo bởi {{ detail.release.createdBy }}, {{ formatToDateTime(detail.release.createdAt) }}
          <template v-if="detail.release.activatedBy">
            · kích hoạt gần nhất bởi {{ detail.release.activatedBy }},
            {{ formatToDateTime(detail.release.activatedAt) }}
          </template>
        </span>
        <div v-if="detail.release.notes" class="mt-1">{{ detail.release.notes }}</div>
      </div>
      <BasicTable @register="registerTable" :dataSource="detail.items" />
      <ReportView v-if="detail.report" class="mt-4" :report="detail.report" />
    </template>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { Tag } from 'ant-design-vue';
  import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
  import { BasicTable, useTable } from '@/components/Table';
  import { formatToDateTime } from '@/utils/dateUtil';
  import { releaseApi } from '@/api/knowledge';
  import type { ReleaseDetail } from '@/api/knowledge/model/knowledgeModel';
  import { RELEASE_STATUS } from '../shared/labels';
  import ReportView from './ReportView.vue';

  defineOptions({ name: 'KnowledgeReleaseDrawer' });

  defineEmits(['register']);
  const detail = ref<ReleaseDetail>();
  const title = computed(() => (detail.value ? `Bản phát hành #${detail.value.release.id}` : 'Bản phát hành'));

  // Bảng nằm trong v-if nên nhận dữ liệu qua :dataSource
  const [registerTable] = useTable({
    title: 'Tài liệu trong bản phát hành',
    columns: [
      { title: 'Mã', dataIndex: 'key', align: 'left', width: 260 },
      { title: 'Loại', dataIndex: 'typeLabel', width: 150 },
      { title: 'Tiêu đề', dataIndex: 'title', align: 'left' },
      { title: 'Phiên bản', dataIndex: 'versionNo', width: 100, customRender: ({ record }) => `v${record.versionNo}` },
    ],
    rowKey: 'docId',
    bordered: true,
    striped: false,
    canResize: false,
  });

  const [registerDrawer, { changeLoading }] = useDrawerInner(async (data) => {
    detail.value = undefined;
    changeLoading(true);
    try {
      detail.value = await releaseApi(data.id);
    } finally {
      changeLoading(false);
    }
  });
</script>
