<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    :title="item ? item.title : 'Chi tiết thông báo'"
    width="560px"
  >
    <template v-if="item">
      <Descriptions :column="1" bordered size="small" class="mb-4">
        <DescriptionsItem label="Loại">
          <Tag :color="SEVERITY_COLORS[item.severity]">{{ TYPE_LABELS[item.type] }}</Tag>
        </DescriptionsItem>
        <DescriptionsItem label="Thời điểm">{{
          formatToDateTime(item.createdAt)
        }}</DescriptionsItem>
        <DescriptionsItem v-if="isCatalog && item.body" label="Nội dung">{{
          item.body
        }}</DescriptionsItem>
        <DescriptionsItem v-if="isCatalog && data.error" label="Lỗi">{{
          data.error
        }}</DescriptionsItem>
        <DescriptionsItem v-if="data.customerName" label="Khách">{{
          data.customerName
        }}</DescriptionsItem>
        <DescriptionsItem v-if="data.phone" label="Số điện thoại">
          <TypographyText :copyable="{ text: data.phone }">{{ data.phone }}</TypographyText>
        </DescriptionsItem>
        <DescriptionsItem v-if="data.province" label="Khu vực">{{
          data.province
        }}</DescriptionsItem>
        <DescriptionsItem v-if="data.needSummary" label="Nhu cầu">{{
          data.needSummary
        }}</DescriptionsItem>
        <DescriptionsItem v-if="data.leadLevelReason" label="Vì sao HOT">
          {{ data.leadLevelReason }}
        </DescriptionsItem>
        <DescriptionsItem v-if="reasonLabels.length" label="Lý do">
          <div v-for="r in reasonLabels" :key="r">• {{ r }}</div>
        </DescriptionsItem>
        <DescriptionsItem v-if="data.by" label="Người chuyển">{{ data.by }}</DescriptionsItem>
        <DescriptionsItem v-if="data.note" label="Ghi chú">{{ data.note }}</DescriptionsItem>
        <DescriptionsItem v-if="data.aiHandoffReason" label="AI ghi chú">
          {{ data.aiHandoffReason }}
        </DescriptionsItem>
        <DescriptionsItem v-if="data.unverifiedNumbers?.length" label="Con số chưa có nguồn">
          {{ data.unverifiedNumbers.join(', ') }}
        </DescriptionsItem>
      </Descriptions>

      <template v-if="data.lastCustomerMessage">
        <Divider orientation="left">Tin cuối của khách</Divider>
        <Card size="small">
          <TypographyParagraph class="mb-0 whitespace-pre-wrap">{{
            data.lastCustomerMessage
          }}</TypographyParagraph>
        </Card>
      </template>

      <template v-if="data.draftText || data.text">
        <Divider orientation="left">{{
          data.draftText ? 'Nháp AI (chưa gửi khách)' : 'Tin chưa gửi được'
        }}</Divider>
        <Card size="small">
          <TypographyParagraph
            class="mb-0 whitespace-pre-wrap"
            :copyable="{ text: data.draftText || data.text }"
          >
            {{ data.draftText || data.text }}
          </TypographyParagraph>
        </Card>
        <Alert
          type="info"
          show-icon
          class="mt-3"
          message="Mở hội thoại để gửi, sửa rồi gửi, hoặc bỏ nháp ngay trên trang Chat với khách."
        />
      </template>

      <div class="mt-4 flex gap-2">
        <Button v-if="item.conversationId" type="primary" @click="openConversation">
          Mở hội thoại
        </Button>
        <Button
          v-if="inboxUrl"
          :href="inboxUrl"
          target="_blank"
          rel="noopener noreferrer"
        >
          Mở hộp thư Meta
        </Button>
        <Button v-if="isCatalog" type="primary" @click="openCatalog"
          >Mở trang Catalog &amp; báo giá</Button
        >
      </div>
    </template>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { useRouter } from 'vue-router';
  import { Alert, Button, Card, Descriptions, Divider, Tag, Typography } from 'ant-design-vue';
  import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
  import { notificationDetailApi } from '@/api/notification';
  import type { NotificationModel } from '@/api/notification/model/notificationModel';
  import { useNotificationStore } from '@/store/modules/notification';
  import { formatToDateTime } from '@/utils/dateUtil';
  import { openConversationLater } from '@/views/inbox/pending';
  import { CATALOG_TYPES, SEVERITY_COLORS, TYPE_LABELS, metaInboxUrl } from './labels';

  const DescriptionsItem = Descriptions.Item;
  const TypographyParagraph = Typography.Paragraph;
  const TypographyText = Typography.Text;

  defineOptions({ name: 'NotificationDetailDrawer' });

  const emit = defineEmits(['read', 'register']);

  const notificationStore = useNotificationStore();
  const item = ref<NotificationModel>();

  const data = computed<Recordable>(() => item.value?.data ?? {});
  const reasonLabels = computed<string[]>(() => data.value.reasonLabels ?? []);
  const inboxUrl = computed(() => metaInboxUrl(data.value.pageId));
  const isCatalog = computed(() => !!item.value && CATALOG_TYPES.includes(item.value.type));

  const router = useRouter();
  function openConversation() {
    closeDrawer();
    openConversationLater(item.value?.conversationId);
    router.push('/inbox/index');
  }

  function openCatalog() {
    closeDrawer();
    router.push(data.value.kind === 'PVOUT' ? '/catalog/pvout' : '/catalog/packages');
  }

  const [registerDrawer, { changeLoading, closeDrawer }] = useDrawerInner(async (d) => {
    const id = d?.id as number | undefined;
    if (!id) return;
    item.value = undefined;
    changeLoading(true);
    try {
      item.value = await notificationDetailApi(id);
      if (!item.value.read) {
        await notificationStore.markRead(id).catch(() => undefined);
        emit('read', id);
      }
    } finally {
      changeLoading(false);
    }
  });
</script>

