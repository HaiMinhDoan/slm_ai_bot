<template>
  <PageWrapper dense contentFullHeight fixedHeight>
    <BasicTable @register="registerTable" @row-click="(record) => open(record.id)">
      <template #headerTop>
        <Tabs v-model:activeKey="channel" @change="reload()">
          <TabPane v-for="t in tabs" :key="t.key">
            <template #tab>
              {{ t.label }}
              <Badge
                v-if="t.needsStaff"
                :count="t.needsStaff"
                :title="`${t.needsStaff} hội thoại cần nhân viên`"
                class="ml-1"
              />
            </template>
          </TabPane>
        </Tabs>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'customerName'">
          <div class="flex items-center gap-1">
            <Badge v-if="record.customerWaiting" status="processing" title="Khách đang chờ trả lời" />
            <b>{{ record.customerName || 'Khách chưa rõ tên' }}</b>
          </div>
          <div v-if="record.phone" class="text-gray-500 text-xs">{{ record.phone }}</div>
        </template>
        <template v-else-if="column.dataIndex === 'channel'">
          <Tag>{{ CHANNEL_LABELS[record.channel] ?? record.channel }}</Tag>
        </template>
        <template v-else-if="column.dataIndex === 'lastText'">
          <div class="last-text">
            <span class="text-gray-500">{{ lastPrefix(record) }}</span>{{ record.lastText || '[đính kèm]' }}
          </div>
          <div class="text-gray-500 text-xs">
            {{ record.lastAt ? formatToDateTime(record.lastAt) : '' }}
            <Tag v-if="record.draftCount" color="orange" class="ml-1">
              {{ record.draftCount }} tin chờ xử lý
            </Tag>
          </div>
        </template>
        <template v-else-if="column.dataIndex === 'ownership'">
          <Tag :color="OWNERSHIP[record.ownership]?.color">{{ OWNERSHIP[record.ownership]?.label }}</Tag>
        </template>
        <template v-else-if="column.dataIndex === 'leadLevel'">
          <Tag v-if="record.leadLevel" :color="LEAD_LEVEL[record.leadLevel]?.color">
            {{ LEAD_LEVEL[record.leadLevel]?.label }}
          </Tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <TableAction
            :actions="[
              { icon: 'ant-design:message-outlined', tooltip: 'Mở hội thoại', onClick: () => open(record.id) },
            ]"
          />
        </template>
      </template>
    </BasicTable>

    <ConversationDrawer @register="registerDrawer" @changed="reload()" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { computed, onActivated, onBeforeUnmount, onMounted, ref, watch } from 'vue';
  import { Badge, Tabs, Tag } from 'ant-design-vue';
  import { BasicTable, TableAction, useTable } from '@/components/Table';
  import { PageWrapper } from '@/components/Page';
  import { useDrawer } from '@/components/Drawer';
  import { formatToDateTime } from '@/utils/dateUtil';
  import { useNotificationStore } from '@/store/modules/notification';
  import { inboxConversationsApi, inboxStatsApi } from '@/api/inbox';
  import type { ChannelCode, ChannelStats } from '@/api/inbox/model/inboxModel';
  import ConversationDrawer from './ConversationDrawer.vue';
  import { pendingConversationId } from './pending';
  import {
    CHANNEL_LABELS,
    LEAD_LEVEL,
    MAIN_CHANNELS,
    OWNERSHIP,
    SENDER_LABELS,
    columns,
    searchFormSchema,
  } from './inbox.data';

  defineOptions({ name: 'InboxList' });

  const TabPane = Tabs.TabPane;
  /** Danh sách tự tải lại: khách nhắn ở hội thoại AI đang trả lời không có thông báo */
  const REFRESH_MS = 30000;

  const notificationStore = useNotificationStore();
  const [registerDrawer, { openDrawer, getOpen }] = useDrawer();

  /** '' = tất cả kênh */
  const channel = ref<ChannelCode | ''>('');
  const stats = ref<ChannelStats[]>([]);
  let timer: ReturnType<typeof setInterval> | undefined;

  const tabs = computed(() => {
    const byChannel = new Map(stats.value.map((s) => [s.channel, s]));
    const shown = [
      ...MAIN_CHANNELS,
      ...stats.value.map((s) => s.channel).filter((c) => !MAIN_CHANNELS.includes(c)),
    ];
    return [
      {
        key: '',
        label: 'Tất cả',
        needsStaff: stats.value.reduce((n, s) => n + s.needsStaff, 0),
      },
      ...shown.map((c) => ({
        key: c,
        label: CHANNEL_LABELS[c],
        needsStaff: byChannel.get(c)?.needsStaff ?? 0,
      })),
    ];
  });

  // vben đánh trang từ 1, backend từ 0; số đếm các tab tải cùng lúc với bảng
  const loadConversations = async (params: Recordable) => {
    const { page = 1, size = 10, ...filters } = params;
    const [rows, s] = await Promise.all([
      inboxConversationsApi({ ...filters, channel: channel.value || undefined, page: page - 1, size }),
      inboxStatsApi(),
    ]);
    stats.value = s;
    return rows;
  };

  const [registerTable, { reload }] = useTable({
    title: 'Hội thoại đang mở',
    titleHelpMessage: [
      'Mới nhắn lên trước. Chấm xanh: khách đang chờ trả lời.',
      'Bấm vào một dòng để xem tin, gửi / sửa / bỏ nháp AI, chuyển cho sale hoặc chuyển lại cho AI.',
    ],
    api: loadConversations,
    rowKey: 'id',
    columns: columns.map((c) => (c.dataIndex === 'channel' ? { ...c, ifShow: () => !channel.value } : c)),
    formConfig: { labelWidth: 80, schemas: searchFormSchema, autoSubmitOnEnter: true },
    // Backend nhận `size`, vben mặc định gửi `pageSize`
    fetchSetting: { sizeField: 'size' },
    useSearchForm: true,
    showTableSetting: true,
    bordered: true,
    striped: false,
    actionColumn: { width: 60, title: 'Mở', dataIndex: 'action', fixed: 'right' },
  });

  function lastPrefix(r: Recordable) {
    if (!r.lastSender) return '';
    const who = r.lastSender === 'CUSTOMER' ? 'Khách' : SENDER_LABELS[r.lastSender];
    return r.lastStatus === 'DRAFT' ? `Nháp ${who}: ` : r.lastStatus === 'FAILED' ? `${who} (lỗi): ` : `${who}: `;
  }

  function open(id: string) {
    openDrawer(true, { id });
  }

  /** Mở hội thoại được yêu cầu từ trang khác (trang Thông báo) */
  function openPending() {
    const id = pendingConversationId.value;
    if (id) {
      pendingConversationId.value = undefined;
      open(id);
    }
  }

  // Thông báo mới qua SSE (nháp AI, chuyển nhân viên, khách nhắn...) thì tải lại danh sách
  watch(
    () => notificationStore.latest[0]?.id,
    (id, old) => {
      if (id && id !== old) reload();
    },
  );
  watch(pendingConversationId, openPending);

  onMounted(() => {
    openPending();
    timer = setInterval(() => {
      if (!document.hidden && !getOpen?.value) reload();
    }, REFRESH_MS);
  });
  onActivated(openPending);
  onBeforeUnmount(() => timer && clearInterval(timer));
</script>

<style lang="less" scoped>
  .last-text {
    display: -webkit-box;
    overflow: hidden;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    word-break: break-word;
  }
</style>
