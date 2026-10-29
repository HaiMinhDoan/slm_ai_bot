<template>
  <PageWrapper dense contentFullHeight fixedHeight>
    <Alert v-if="syncInfo" :type="syncInfo.type" show-icon class="mb-2" :message="syncInfo.message">
      <template v-if="syncInfo.error || warningSummary.length" #description>
        <div v-if="syncInfo.error" class="text-red-600">{{ syncInfo.error }}</div>
        <div v-for="w in warningSummary" :key="w.code" class="text-xs">
          <Tag :color="w.blocking ? 'red' : 'orange'">{{ w.label }}</Tag>
          gói / vật tư {{ w.ids.join(', ') }}
          <span v-if="w.blocking" class="text-red-600">
            · không được gợi ý cho khách tới khi sửa ở slm_v2
          </span>
        </div>
      </template>
    </Alert>

    <BasicTable @register="registerTable">
      <template #toolbar>
        <Button
          type="primary"
          :loading="syncing"
          :disabled="status && !status.configured"
          @click="handleSync"
        >
          Đồng bộ ngay
        </Button>
      </template>

      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'warnings'">
          <Tag
            v-for="w in record.warnings"
            :key="w"
            :color="labels[w]?.blocking ? 'red' : 'orange'"
            class="mb-1"
          >
            {{ labels[w]?.label ?? w }}
          </Tag>
        </template>
      </template>
    </BasicTable>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { Alert, Tag } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { BasicTable, useTable } from '@/components/Table';
  import { PageWrapper } from '@/components/Page';
  import { useMessage } from '@/hooks/web/useMessage';
  import { formatToDateTime } from '@/utils/dateUtil';

  import { columns, matchesFilter, searchFormSchema } from './packages.data';
  import {
    catalogPackagesApi,
    catalogStatusApi,
    catalogSyncApi,
    catalogWarningLabelsApi,
  } from '@/api/catalog';
  import type { CatalogStatusModel, WarningLabels } from '@/api/catalog/model/catalogModel';

  defineOptions({ name: 'CatalogPackages' });

  const { createMessage } = useMessage();
  const status = ref<CatalogStatusModel>();
  const labels = ref<WarningLabels>({});
  const syncing = ref(false);

  /**
   * Tải lại bảng (nút làm mới của bảng, đổi bộ lọc) thì tải lại cả trạng thái đồng bộ,
   * để dòng trạng thái phía trên luôn khớp với danh sách gói.
   */
  const loadPackages = async (params: Recordable) => {
    const [s, list] = await Promise.all([catalogStatusApi(), catalogPackagesApi()]);
    status.value = s;
    if (!Object.keys(labels.value).length) {
      labels.value = await catalogWarningLabelsApi();
    }
    const items = list.filter((p) => matchesFilter(p, params));
    return { items, total: items.length };
  };

  const [registerTable, { reload }] = useTable({
    title: 'Gói đồng bộ từ slm_v2',
    titleHelpMessage:
      'Đọc từ slm_v2 (chỉ đọc), tự đồng bộ 10 phút một lần. Sửa giá, thành phần, ảnh ở slm_v2.',
    api: loadPackages,
    rowKey: 'id',
    columns,
    formConfig: {
      labelWidth: 90,
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
    },
    useSearchForm: true,
    pagination: false,
    showTableSetting: true,
    bordered: true,
    striped: false,
  });

  const STATUS_LABELS: Record<string, string> = {
    SUCCESS: 'thành công',
    FAILED: 'lỗi',
    RUNNING: 'đang chạy',
  };

  const syncInfo = computed(() => {
    const s = status.value;
    if (!s) return undefined;
    if (!s.configured) {
      return {
        type: 'error' as const,
        message:
          'Chưa cấu hình kết nối slm_v2 (SLM_V2_DB_URL / SLM_V2_DB_USERNAME / SLM_V2_DB_PASSWORD trong .env của server): bot chưa có công cụ báo giá.',
      };
    }
    const last = s.runs[0];
    if (!last) {
      return {
        type: 'info' as const,
        message: 'Chưa đồng bộ lần nào. Hệ thống tự đồng bộ 10 phút một lần.',
      };
    }
    let message = `Lần đồng bộ gần nhất ${formatToDateTime(last.startedAt)}: ${STATUS_LABELS[last.status]}`;
    if (s.lastSuccess && s.lastSuccess.id !== last.id) {
      message += ` · bot đang dùng bản thành công lúc ${formatToDateTime(s.lastSuccess.startedAt)}`;
    }
    if (s.version) message += ` · phiên bản ${s.version.slice(0, 12)}`;
    return {
      type: last.status === 'FAILED' ? ('error' as const) : ('success' as const),
      message,
      error: last.status === 'FAILED' ? last.error : undefined,
    };
  });

  const warningSummary = computed(() =>
    Object.entries(status.value?.lastSuccess?.warnings ?? {}).map(([code, ids]) => ({
      code,
      ids,
      label: labels.value[code]?.label ?? code,
      blocking: !!labels.value[code]?.blocking,
    })),
  );

  async function handleSync() {
    syncing.value = true;
    try {
      const run = await catalogSyncApi();
      if (run.status === 'SUCCESS') {
        createMessage.success(
          `Đồng bộ xong: thêm ${run.inserted ?? 0}, sửa ${run.updated ?? 0}, ngừng ${run.deactivated ?? 0}`,
        );
      } else {
        createMessage.error('Đồng bộ lỗi: ' + (run.error ?? ''));
      }
      reload();
    } finally {
      syncing.value = false;
    }
  }
</script>
