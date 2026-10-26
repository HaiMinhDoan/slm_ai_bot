<template>
  <PageWrapper dense contentFullHeight fixedHeight>
    <Card size="small" class="mb-2" title="Cài đặt thông báo trên máy này">
      <div class="settings">
        <div class="setting">
          <Switch :checked="notificationStore.settings.sound" @change="(c) => onSoundChange(!!c)" />
          <span>Âm thanh khi có thông báo</span>
          <Button size="small" @click="testSound">Nghe thử</Button>
        </div>
        <div class="setting">
          <Switch
            :checked="notificationStore.settings.desktop"
            :disabled="!desktopSupported"
            @change="(c) => onDesktopChange(!!c)"
          />
          <span>Hiện thông báo của máy tính khi đang ở tab khác</span>
        </div>
        <div class="setting">
          <Badge :status="notificationStore.connected ? 'success' : 'error'" />
          <span>{{ notificationStore.connected ? 'Đang nhận thông báo' : 'Mất kết nối, đang thử lại' }}</span>
        </div>
      </div>
      <Alert
        v-if="!desktopSupported"
        class="mt-2"
        type="warning"
        show-icon
        message="Thông báo của máy tính chỉ chạy khi trang admin mở bằng HTTPS (ví dụ https://fs-wms.io.vn)."
      />
      <Alert
        v-else-if="permissionDenied"
        class="mt-2"
        type="warning"
        show-icon
        message="Trình duyệt đang chặn thông báo của trang này. Bấm biểu tượng ổ khoá cạnh địa chỉ trang để cho phép."
      />
    </Card>

    <BasicTable @register="registerTable" @row-click="openDetail">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'type'">
          <Tag :color="SEVERITY_COLORS[record.severity]">{{ TYPE_LABELS[record.type] }}</Tag>
        </template>
        <template v-else-if="column.key === 'title'">
          <span :class="{ unread: !record.read }">{{ record.title }}</span>
        </template>
        <template v-else-if="column.key === 'action-col'">
          <Button size="small" @click.stop="openDetail(record)">Xem</Button>
        </template>
      </template>
      <template #toolbar>
        <Button @click="markAll" :disabled="!notificationStore.unreadCount">Đọc hết</Button>
      </template>
    </BasicTable>

    <NotificationDetailDrawer v-model:open="detailOpen" :notificationId="detailId" @read="onRead" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { computed, onMounted, ref, watch } from 'vue';
  import { useRoute } from 'vue-router';
  import { Alert, Badge, Card, Switch, Tag } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { BasicTable, useTable, type BasicColumn, type FormSchema } from '@/components/Table';
  import { PageWrapper } from '@/components/Page';
  import { useMessage } from '@/hooks/web/useMessage';
  import { notificationListApi } from '@/api/notification';
  import type { NotificationModel } from '@/api/notification/model/notificationModel';
  import {
    desktopNotificationSupported,
    useNotificationStore,
  } from '@/store/modules/notification';
  import { playNotificationSound } from '@/utils/notifySound';
  import { formatToDateTime } from '@/utils/dateUtil';
  import NotificationDetailDrawer from './NotificationDetailDrawer.vue';
  import { SEVERITY_COLORS, TYPE_LABELS, TYPE_OPTIONS } from './labels';

  defineOptions({ name: 'NotificationList' });

  const notificationStore = useNotificationStore();
  const { createMessage } = useMessage();
  const route = useRoute();
  const detailOpen = ref(false);
  const detailId = ref<number>();
  const desktopSupported = desktopNotificationSupported();
  const permissionDenied = computed(
    () => desktopSupported && typeof Notification !== 'undefined' && Notification.permission === 'denied',
  );

  const columns: BasicColumn[] = [
    {
      title: 'Thời gian',
      dataIndex: 'createdAt',
      width: 160,
      customRender: ({ record }) => formatToDateTime(record.createdAt),
    },
    { title: 'Loại', dataIndex: 'type', key: 'type', width: 190 },
    { title: 'Tiêu đề', dataIndex: 'title', key: 'title', width: 280, align: 'left' },
    { title: 'Nội dung', dataIndex: 'body', align: 'left' },
    {
      title: 'Trạng thái',
      dataIndex: 'read',
      width: 100,
      customRender: ({ record }) => (record.read ? 'Đã đọc' : 'Chưa đọc'),
    },
    { title: '', dataIndex: 'action-col', key: 'action-col', width: 80 },
  ];

  const searchFormSchema: FormSchema[] = [
    {
      field: 'show',
      label: 'Hiển thị',
      component: 'Select',
      defaultValue: 'all',
      componentProps: {
        options: [
          { label: 'Tất cả', value: 'all' },
          { label: 'Chưa đọc', value: 'unread' },
        ],
      },
      colProps: { span: 6 },
    },
    {
      field: 'type',
      label: 'Loại',
      component: 'Select',
      componentProps: { options: TYPE_OPTIONS, allowClear: true },
      colProps: { span: 8 },
    },
  ];

  const loadNotifications = async (params: Recordable) => {
    const { page = 1, size = 20, show, type } = params;
    return notificationListApi({ unreadOnly: show === 'unread', type, page: page - 1, size });
  };

  const [registerTable, { reload }] = useTable({
    title: 'Thông báo',
    api: loadNotifications,
    rowKey: 'id',
    columns,
    formConfig: { labelWidth: 80, schemas: searchFormSchema },
    useSearchForm: true,
    fetchSetting: { sizeField: 'size' },
    showTableSetting: true,
    bordered: true,
  });

  function openDetail(record: Recordable) {
    detailId.value = (record as NotificationModel).id;
    detailOpen.value = true;
  }

  function onRead() {
    reload();
  }

  async function markAll() {
    await notificationStore.markAllRead();
    reload();
  }

  function onSoundChange(checked: boolean) {
    notificationStore.setSound(checked);
  }

  async function onDesktopChange(checked: boolean) {
    const ok = await notificationStore.setDesktop(checked);
    if (!ok) {
      createMessage.warning('Trình duyệt chưa cho phép hiện thông báo của trang này');
    }
  }

  function testSound() {
    // Bấm nút cũng là thao tác mở khoá âm thanh của trình duyệt
    setTimeout(() => playNotificationSound(`test-${Date.now()}`, 'WARNING'), 50);
  }

  // Mở từ chuông / thông báo hệ điều hành: /notifications/index?id=123
  function openFromQuery() {
    const id = Number(route.query.id);
    if (id > 0) {
      detailId.value = id;
      detailOpen.value = true;
    }
  }
  onMounted(openFromQuery);
  watch(() => route.query.id, openFromQuery);

  // Có thông báo mới khi đang mở trang thì tải lại bảng
  watch(
    () => notificationStore.latest[0]?.id,
    (id, old) => {
      if (id && id !== old) reload();
    },
  );
</script>

<style lang="less" scoped>
  .settings {
    display: flex;
    flex-wrap: wrap;
    gap: 16px 32px;
  }

  .setting {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .unread {
    font-weight: 600;
  }
</style>
