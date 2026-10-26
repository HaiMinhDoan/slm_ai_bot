<template>
  <Drawer
    :open="open"
    :title="item ? item.title : 'Chi tiết thông báo'"
    width="560"
    @close="emit('update:open', false)"
  >
    <Spin :spinning="loading">
      <template v-if="item">
        <Descriptions :column="1" bordered size="small" class="mb-4">
          <DescriptionsItem label="Loại">
            <Tag :color="SEVERITY_COLORS[item.severity]">{{ TYPE_LABELS[item.type] }}</Tag>
          </DescriptionsItem>
          <DescriptionsItem label="Thời điểm">{{ formatToDateTime(item.createdAt) }}</DescriptionsItem>
          <DescriptionsItem v-if="data.customerName" label="Khách">{{ data.customerName }}</DescriptionsItem>
          <DescriptionsItem v-if="data.phone" label="Số điện thoại">
            <TypographyText :copyable="{ text: data.phone }">{{ data.phone }}</TypographyText>
          </DescriptionsItem>
          <DescriptionsItem v-if="data.province" label="Khu vực">{{ data.province }}</DescriptionsItem>
          <DescriptionsItem v-if="data.needSummary" label="Nhu cầu">{{ data.needSummary }}</DescriptionsItem>
          <DescriptionsItem v-if="data.leadLevelReason" label="Vì sao HOT">
            {{ data.leadLevelReason }}
          </DescriptionsItem>
          <DescriptionsItem v-if="reasonLabels.length" label="Lý do">
            <div v-for="r in reasonLabels" :key="r">• {{ r }}</div>
          </DescriptionsItem>
          <DescriptionsItem v-if="data.aiHandoffReason" label="AI ghi chú">
            {{ data.aiHandoffReason }}
          </DescriptionsItem>
          <DescriptionsItem v-if="data.unverifiedNumbers?.length" label="Con số chưa có nguồn">
            {{ data.unverifiedNumbers.join(', ') }}
          </DescriptionsItem>
        </Descriptions>

        <template v-if="data.lastCustomerMessage">
          <Divider orientation="left">Tin cuối của khách</Divider>
          <TypographyParagraph class="bubble">{{ data.lastCustomerMessage }}</TypographyParagraph>
        </template>

        <template v-if="data.draftText || data.text">
          <Divider orientation="left">{{ data.draftText ? 'Nháp AI (chưa gửi khách)' : 'Tin chưa gửi được' }}</Divider>
          <TypographyParagraph class="bubble draft" :copyable="{ text: data.draftText || data.text }">
            {{ data.draftText || data.text }}
          </TypographyParagraph>
          <Alert
            type="info"
            show-icon
            message="Sao chép nháp, sửa nếu cần rồi gửi khách trong hộp thư Meta Business Suite. Inbox admin để duyệt và gửi ngay trên trang này sẽ có ở bản sau."
          />
        </template>

        <div class="mt-4">
          <Button v-if="inboxUrl" type="primary" :href="inboxUrl" target="_blank" rel="noopener noreferrer">
            Mở hộp thư Meta
          </Button>
        </div>
      </template>
    </Spin>
  </Drawer>
</template>

<script lang="ts" setup>
  import { computed, ref, watch } from 'vue';
  import {
    Alert,
    Button,
    Descriptions,
    Divider,
    Drawer,
    Spin,
    Tag,
    Typography,
  } from 'ant-design-vue';
  import { notificationDetailApi } from '@/api/notification';
  import type { NotificationModel } from '@/api/notification/model/notificationModel';
  import { useNotificationStore } from '@/store/modules/notification';
  import { formatToDateTime } from '@/utils/dateUtil';
  import { SEVERITY_COLORS, TYPE_LABELS, metaInboxUrl } from './labels';

  const DescriptionsItem = Descriptions.Item;
  const TypographyParagraph = Typography.Paragraph;
  const TypographyText = Typography.Text;

  const props = defineProps<{ open: boolean; notificationId?: number }>();
  const emit = defineEmits<{ (e: 'update:open', v: boolean): void; (e: 'read', id: number): void }>();

  const notificationStore = useNotificationStore();
  const item = ref<NotificationModel>();
  const loading = ref(false);

  const data = computed<Recordable>(() => item.value?.data ?? {});
  const reasonLabels = computed<string[]>(() => data.value.reasonLabels ?? []);
  const inboxUrl = computed(() => metaInboxUrl(data.value.pageId));

  watch(
    () => [props.open, props.notificationId] as const,
    async ([open, id]) => {
      if (!open || !id) return;
      loading.value = true;
      try {
        item.value = await notificationDetailApi(id);
        if (!item.value.read) {
          await notificationStore.markRead(id).catch(() => undefined);
          emit('read', id);
        }
      } finally {
        loading.value = false;
      }
    },
    { immediate: true },
  );
</script>

<style lang="less" scoped>
  .bubble {
    padding: 8px 12px;
    border-radius: 8px;
    background: #f5f5f5;
    white-space: pre-wrap;
  }

  .draft {
    background: #fffbe6;
  }
</style>
