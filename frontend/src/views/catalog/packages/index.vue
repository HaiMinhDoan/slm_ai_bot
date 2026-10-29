<template>
  <PageWrapper dense contentFullHeight fixedHeight>
    <Alert v-if="syncInfo" :type="syncInfo.type" show-icon class="mb-2" :message="syncInfo.message">
      <template v-if="syncInfo.error || warningSummary.length" #description>
        <div v-if="syncInfo.error" class="text-red-600">{{ syncInfo.error }}</div>
        <div v-for="w in warningSummary" :key="w.code" class="text-xs">
          <Tag :color="w.blocking ? 'red' : 'orange'">{{ w.label }}</Tag>
          gói / vật tư {{ w.ids.join(', ') }}
          <span v-if="w.blocking" class="text-red-600">
            · không được gợi ý cho khách tới khi sửa ở slm_v2 hoặc sửa gói ngay trên trang này
          </span>
        </div>
      </template>
    </Alert>

    <BasicTable @register="registerTable" :rowSelection="canEdit ? rowSelection : undefined">
      <template #toolbar>
        <template v-if="canEdit">
          <Button :disabled="!selectedRowKeys.length" @click="handleBulkActive(true)">
            Bật ({{ selectedRowKeys.length }})
          </Button>
          <Button :disabled="!selectedRowKeys.length" @click="handleBulkActive(false)">Tắt</Button>
        </template>
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
        <template v-if="column.key === 'action'">
          <TableAction
            v-if="record.deleted"
            :actions="[
              {
                icon: 'ant-design:undo-outlined',
                tooltip: 'Khôi phục gói',
                auth: EDIT_ROLES,
                onClick: handleRestore.bind(null, record),
              },
            ]"
          />
          <TableAction
            v-else
            :actions="[
              {
                icon: 'clarity:note-edit-line',
                tooltip: 'Sửa gói',
                auth: EDIT_ROLES,
                onClick: handleEdit.bind(null, record),
              },
              {
                icon: 'ant-design:delete-outlined',
                color: 'error',
                tooltip: 'Xoá gói',
                auth: EDIT_ROLES,
                popConfirm: {
                  title: `Xoá gói ${record.id}? Bot sẽ không gợi ý gói này; khôi phục được ở bộ lọc Đã xoá.`,
                  placement: 'left',
                  confirm: handleDelete.bind(null, record),
                },
              },
            ]"
          />
        </template>
        <template v-else-if="column.key === 'warnings'">
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

    <PackageDrawer @register="registerDrawer" @success="handleSuccess" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { computed, ref, unref } from 'vue';
  import { Alert, Tag } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { BasicTable, useTable, TableAction } from '@/components/Table';
  import { PageWrapper } from '@/components/Page';
  import { useDrawer } from '@/components/Drawer';
  import { useMessage } from '@/hooks/web/useMessage';
  import { usePermission } from '@/hooks/web/usePermission';
  import { RoleEnum } from '@/enums/roleEnum';
  import { formatToDateTime } from '@/utils/dateUtil';

  import PackageDrawer from './PackageDrawer.vue';
  import { columns, searchFormSchema } from './packages.data';
  import {
    catalogPackageDeleteApi,
    catalogPackageRestoreApi,
    catalogPackageSetActiveApi,
    catalogPackagesApi,
    catalogStatusApi,
    catalogSyncApi,
    catalogWarningLabelsApi,
  } from '@/api/catalog';
  import type { CatalogStatusModel, WarningLabels } from '@/api/catalog/model/catalogModel';

  defineOptions({ name: 'CatalogPackages' });

  /** Nhân viên trực chat chỉ xem; sửa, tắt, xoá là việc của quản trị (khớp quyền ở backend) */
  const EDIT_ROLES = [RoleEnum.SYSTEM_ADMIN, RoleEnum.CONTENT_ADMIN];

  const { createMessage } = useMessage();
  const { hasPermission } = usePermission();
  const canEdit = hasPermission(EDIT_ROLES);
  const [registerDrawer, { openDrawer }] = useDrawer();
  const status = ref<CatalogStatusModel>();
  const labels = ref<WarningLabels>({});
  const syncing = ref(false);

  const selectedRowKeys = ref<number[]>([]);
  const rowSelection = computed(
    () =>
      ({
        type: 'checkbox',
        selectedRowKeys: unref(selectedRowKeys),
        onChange: (keys: (string | number)[]) => {
          selectedRowKeys.value = keys as number[];
        },
      }) as any,
  );

  /**
   * vben đánh trang từ 1, Spring Data từ 0 nên phải trừ đi 1.
   * Tải lại bảng (nút làm mới, đổi bộ lọc, chuyển trang) thì tải lại cả trạng thái đồng bộ,
   * để dòng trạng thái phía trên luôn khớp với danh sách gói.
   */
  const loadPackages = async (params: Recordable) => {
    const { page = 1, size = 10, ...filters } = params;
    const [s, result] = await Promise.all([
      catalogStatusApi(),
      catalogPackagesApi({ ...filters, page: page - 1, size }),
    ]);
    status.value = s;
    if (!Object.keys(labels.value).length) {
      labels.value = await catalogWarningLabelsApi();
    }
    return result;
  };

  const [registerTable, { reload, clearSelectedRowKeys }] = useTable({
    title: 'Gói đồng bộ từ slm_v2',
    titleHelpMessage: [
      'Đọc từ slm_v2 (chỉ đọc), tự đồng bộ 10 phút một lần.',
      'Sửa, tắt, xoá ở đây chỉ áp dụng cho bot; slm_v2 không đổi và đồng bộ không đè mất.',
    ],
    api: loadPackages,
    rowKey: 'id',
    columns,
    formConfig: {
      labelWidth: 90,
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
    },
    // Backend nhận `size`, vben mặc định gửi `pageSize`
    fetchSetting: { sizeField: 'size' },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    striped: false,
    // Chỉ chọn bằng ô tick: bấm nút sửa trên một dòng không được kéo dòng đó vào lệnh Bật / Tắt
    clickToRowSelect: false,
    actionColumn: canEdit
      ? { width: 100, title: 'Thao tác', dataIndex: 'action', fixed: 'right' }
      : undefined,
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

  // Slot bodyCell trả Recordable chứ không phải CatalogPackageModel, nên lấy id ở biên
  function handleEdit(record: Recordable) {
    openDrawer(true, { id: record.id });
  }

  async function handleDelete(record: Recordable) {
    await catalogPackageDeleteApi(record.id);
    createMessage.success(`Đã xoá gói ${record.id}`);
    handleSuccess();
  }

  async function handleRestore(record: Recordable) {
    await catalogPackageRestoreApi(record.id);
    createMessage.success(`Đã khôi phục gói ${record.id}`);
    handleSuccess();
  }

  async function handleBulkActive(active: boolean) {
    const ids = unref(selectedRowKeys);
    if (!ids.length) return;
    const result = await catalogPackageSetActiveApi(ids, active);
    createMessage.success(`${active ? 'Đã bật' : 'Đã tắt'} ${result.affected} gói`);
    handleSuccess();
  }

  function handleSuccess() {
    selectedRowKeys.value = [];
    clearSelectedRowKeys();
    reload();
  }

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
