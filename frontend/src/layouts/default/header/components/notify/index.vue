<template>
  <div :class="prefixCls">
    <Popover
      v-model:open="open"
      title=""
      trigger="click"
      :overlayClassName="`${prefixCls}__overlay`"
    >
      <Badge :count="notificationStore.unreadCount" :overflowCount="99" :numberStyle="numberStyle">
        <BellOutlined />
      </Badge>
      <template #content>
        <div :class="`${prefixCls}__head`">
          <span>Thông báo</span>
          <span :class="`${prefixCls}__status`" :title="statusTitle">
            <Badge :status="notificationStore.connected ? 'success' : 'default'" />
          </span>
        </div>
        <NoticeList v-if="items.length" :list="items" @title-click="onNoticeClick" />
        <Empty v-else :image="Empty.PRESENTED_IMAGE_SIMPLE" description="Chưa có thông báo" />
        <div :class="`${prefixCls}__actions`">
          <Button type="link" size="small" @click="markAll" :disabled="!notificationStore.unreadCount">
            Đọc hết
          </Button>
          <Button type="link" size="small" @click="viewAll">Xem tất cả</Button>
        </div>
      </template>
    </Popover>
  </div>
</template>
<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { Popover, Badge, Button, Empty } from 'ant-design-vue';
  import { BellOutlined } from '@ant-design/icons-vue';
  import type { ListItem } from './data';
  import NoticeList from './NoticeList.vue';
  import { useDesign } from '@/hooks/web/useDesign';
  import { useRouter } from 'vue-router';
  import { useNotificationStore } from '@/store/modules/notification';
  import { formatToDateTime } from '@/utils/dateUtil';
  import { SEVERITY_COLORS, TYPE_LABELS } from '@/views/notification/labels';

  const { prefixCls } = useDesign('header-notify');
  const notificationStore = useNotificationStore();
  const router = useRouter();
  const open = ref(false);
  const numberStyle = {};

  const statusTitle = computed(() =>
    notificationStore.connected ? 'Đang nhận thông báo' : 'Mất kết nối, đang thử lại',
  );

  const items = computed<ListItem[]>(() =>
    notificationStore.latest.map((n) => ({
      id: String(n.id),
      avatar: '',
      title: n.title,
      titleDelete: n.read,
      read: n.read,
      datetime: formatToDateTime(n.createdAt),
      type: n.type,
      description: n.body ?? '',
      extra: TYPE_LABELS[n.type],
      color: SEVERITY_COLORS[n.severity],
    })),
  );

  async function onNoticeClick(record: ListItem) {
    open.value = false;
    const id = Number(record.id);
    await notificationStore.markRead(id).catch(() => undefined);
    router.push({ path: '/notifications/index', query: { id: record.id } });
  }

  async function markAll() {
    await notificationStore.markAllRead();
  }

  function viewAll() {
    open.value = false;
    router.push('/notifications/index');
  }
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-header-notify';

  .@{prefix-cls} {
    padding-bottom: 1px;

    &__overlay {
      width: 360px;
      max-width: calc(100vw - 32px);
    }

    &__head {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 8px;
      font-weight: 600;
    }

    &__actions {
      display: flex;
      justify-content: space-between;
      margin-top: 8px;
    }

    .ant-badge {
      display: flex;
      align-items: center;
      font-size: 18px;

      .ant-badge-multiple-words {
        padding: 0 4px;
      }

      svg {
        width: 0.9em;
      }
    }
  }
</style>
