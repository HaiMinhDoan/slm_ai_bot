<template>
  <BasicDrawer
    v-bind="$attrs"
    @register="registerDrawer"
    :title="media ? `Sửa media${media.indexCode ? ' ' + media.indexCode : ''}` : 'Sửa media'"
    width="960px"
    showFooter
    okText="Lưu"
    @ok="handleSubmit"
  >
    <div v-if="media" class="media-head">
      <MediaPreview :media="media" :size="120" />
      <div class="media-meta">
        <div class="title">{{ media.title }}</div>
        <div>
          <Tag :color="media.type === 'IMAGE' ? 'blue' : 'purple'">
            {{ media.type === 'IMAGE' ? 'Ảnh' : 'Video' }}
          </Tag>
          <Tag :color="CATEGORY_COLORS[media.category] ?? 'default'">
            {{ categoryLabel(media.category) }}
          </Tag>
          <Tag :color="STATUS_COLORS[media.status]">{{ STATUS_LABELS[media.status] }}</Tag>
          <span v-if="media.indexCode" class="muted">Mã danh mục {{ media.indexCode }}</span>
        </div>
        <div class="muted">
          Thêm bởi {{ media.createdBy ?? 'hệ thống' }} lúc {{ formatToDateTime(media.createdAt) }}
        </div>
        <div v-if="media.approvedBy" class="muted">
          Duyệt bởi {{ media.approvedBy }} lúc {{ formatToDateTime(media.approvedAt) }}
        </div>
        <div v-if="media.updatedBy" class="muted">Sửa gần nhất bởi {{ media.updatedBy }}</div>
      </div>
    </div>

    <Tabs v-model:activeKey="tab">
      <TabPane key="info" tab="Thông tin" forceRender>
        <BasicForm @register="registerInfo" @field-value-change="handleInfoChange" />
      </TabPane>

      <TabPane key="ai" tab="Cho AI" forceRender>
        <Alert
          type="info"
          show-icon
          class="mb-3"
          message="AI chỉ hiểu media qua Tóm tắt, Dùng khi và Tránh khi"
          description="Viết rõ media nói gì và khi nào nên gửi. Chỉ mục Đã duyệt mới được AI chọn gửi cho khách (khi có tính năng gửi media); lần duyệt đầu cấp mã M01, M02... cho danh mục media."
        />
        <div class="draft-bar">
          <Popconfirm
            title="AI sẽ viết lại Tóm tắt, Dùng khi, Tránh khi, Mô tả và gợi ý chủ đề (giữ các ý đúng đã có). Chưa lưu cho tới khi bấm Lưu."
            okText="Viết nháp"
            @confirm="handleAiDraft"
          >
            <Button :loading="drafting" preIcon="ant-design:robot-outlined">AI viết nháp</Button>
          </Popconfirm>
          <span class="muted">
            Claude xem ảnh (video: ảnh thu nhỏ và tiêu đề), gói đang gắn rồi viết nháp; mỗi lần bấm tốn
            một lượt gọi Claude.
          </span>
        </div>
        <BasicForm @register="registerAi" />
      </TabPane>

      <TabPane key="sources" :tab="`Nguồn (${media?.sources.length ?? 0})`">
        <Table
          v-if="media"
          :columns="sourceColumns"
          :data-source="media.sources"
          :pagination="false"
          rowKey="id"
          size="small"
          bordered
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'preview'">
              <MediaThumb
                v-if="record.stored && record.mimeType?.startsWith('image/')"
                :mediaId="media.id"
                :sourceId="record.id"
              />
              <MediaThumb
                v-else-if="record.hasThumbnail"
                :mediaId="media.id"
                :sourceId="record.id"
                thumbnail
              />
              <Image
                v-else-if="record.thumbnailUrl"
                :src="record.thumbnailUrl"
                :width="64"
                :height="64"
              />
            </template>
            <template v-else-if="column.key === 'info'">
              <div>
                <b>{{ SOURCE_LABELS[record.sourceType] ?? record.sourceType }}</b>
                <a
                  v-if="record.sourceUrl"
                  :href="record.sourceUrl"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  · link gốc
                </a>
              </div>
              <div class="muted">
                <span v-if="record.mimeType">{{ record.mimeType }}</span>
                <span v-if="record.sizeBytes"> · {{ sizeText(record.sizeBytes) }}</span>
                <span v-if="record.width"> · {{ record.width }}×{{ record.height }}</span>
                <span v-if="record.durationSeconds">
                  · {{ durationText(record.durationSeconds) }}
                </span>
              </div>
              <div v-if="record.sha256" class="muted">sha256 {{ record.sha256.slice(0, 12) }}…</div>
              <div v-if="record.externalId && !record.stored" class="muted">
                id {{ record.externalId }}
              </div>
            </template>
            <template v-else-if="column.key === 'author'">
              <Input
                v-if="!record.stored"
                :value="record.authorName"
                placeholder="Kênh / tác giả"
                size="small"
                @change="(e) => (record.authorName = e.target.value)"
                @blur="handleSourceUpdate(record as MediaSourceModel)"
              />
            </template>
            <template v-else-if="column.key === 'availability'">
              <Select
                v-if="!record.stored"
                :value="record.availabilityStatus"
                :options="options(AVAILABILITY_LABELS)"
                size="small"
                style="width: 140px"
                @change="(v) => handleSourceUpdate(record as MediaSourceModel, v as AvailabilityStatus)"
              />
              <Tag v-else :color="AVAILABILITY_COLORS[record.availabilityStatus]">
                {{ AVAILABILITY_LABELS[record.availabilityStatus] }}
              </Tag>
            </template>
            <template v-else-if="column.key === 'action'">
              <Upload
                v-if="record.stored"
                :accept="accept"
                :showUploadList="false"
                :beforeUpload="(f) => pickFile(f, record.id)"
              >
                <Button size="small" type="link" :loading="busySource === record.id">Thay file</Button>
              </Upload>
              <Button
                v-else
                size="small"
                type="link"
                @click="startReplaceLink(record.id, record.sourceUrl)"
              >
                Sửa link
              </Button>
              <Popconfirm
                title="Bỏ nguồn này? File trong hệ thống vẫn giữ, nhật ký ghi lại đường dẫn."
                @confirm="handleDeleteSource(record.id)"
              >
                <Button size="small" type="link" danger :disabled="media.sources.length <= 1">
                  Bỏ
                </Button>
              </Popconfirm>
            </template>
          </template>
        </Table>
        <div class="source-add">
          <Upload :accept="accept" :showUploadList="false" :beforeUpload="(f) => pickFile(f)">
            <Button :loading="busySource === 'new-file'" preIcon="ant-design:upload-outlined">
              Thêm file {{ media?.type === 'VIDEO' ? 'video' : 'ảnh' }}
            </Button>
          </Upload>
          <Input
            v-model:value="linkInput"
            :placeholder="
              replaceSourceId
                ? 'Dán link mới thay cho link cũ'
                : media?.type === 'VIDEO'
                  ? 'Dán link YouTube / Facebook / TikTok'
                  : 'Dán link ảnh Google Drive'
            "
            allowClear
            class="link-input"
            @press-enter="handleAddLink"
          />
          <Button type="primary" :loading="busySource === 'new-link'" @click="handleAddLink">
            {{ replaceSourceId ? 'Thay link' : 'Thêm link' }}
          </Button>
          <Button v-if="replaceSourceId" @click="cancelReplaceLink">Huỷ</Button>
        </div>
        <div class="muted">
          Ảnh JPEG / PNG / WebP tối đa 15 MB; video MP4 / MOV / WebM tối đa 100 MB (Messenger chỉ gửi kèm
          video khoảng 25 MB, video lớn nên đăng lên Page rồi dán link). Thêm, thay, bỏ nguồn lưu ngay.
        </div>
      </TabPane>

      <TabPane key="links" :tab="`Liên kết (${linkRows.length})`">
        <Alert
          type="info"
          show-icon
          class="mb-3"
          message="Gắn media với gói, vật tư, hãng hoặc tài liệu kiến thức"
          description="Mỗi gói chỉ có một ảnh chính: chọn ảnh này làm ảnh chính thì ảnh chính cũ của gói chuyển thành ảnh thêm. Chọn gói ở miền này thì gói cùng tên ở miền kia được thêm theo. Lưu cùng nút Lưu."
        />
        <LinkEditor v-model:rows="linkRows" :mediaType="media?.type" />
      </TabPane>

      <TabPane key="history" tab="Lịch sử">
        <Table
          :columns="historyColumns"
          :data-source="history"
          :loading="historyLoading"
          :pagination="{ pageSize: 20 }"
          rowKey="at"
          size="small"
          bordered
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'at'">{{ formatToDateTime(record.at) }}</template>
            <template v-else-if="column.key === 'action'">
              {{ ACTION_LABELS[record.action] ?? record.action }}
            </template>
            <template v-else-if="column.key === 'detail'">
              {{ historyDetail(record as MediaHistoryModel) }}
            </template>
          </template>
        </Table>
      </TabPane>
    </Tabs>
  </BasicDrawer>
</template>

<script lang="ts" setup>
  import { computed, ref, watch } from 'vue';
  import { Alert, Image, Input, Popconfirm, Select, Table, Tabs, Tag, Upload } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
  import { BasicForm, useForm } from '@/components/Form';
  import { useMessage } from '@/hooks/web/useMessage';
  import { formatToDateTime } from '@/utils/dateUtil';

  import LinkEditor from './LinkEditor.vue';
  import MediaPreview from './MediaPreview.vue';
  import MediaThumb from './MediaThumb.vue';
  import {
    ACTION_LABELS,
    AVAILABILITY_COLORS,
    AVAILABILITY_LABELS,
    CATEGORY_COLORS,
    FIELD_LABELS,
    SOURCE_LABELS,
    STATUS_COLORS,
    STATUS_LABELS,
    aiFormSchema,
    categoryLabel,
    infoFormSchema,
    options,
  } from './media.data';
  import {
    ACCEPT_IMAGES,
    ACCEPT_VIDEOS,
    durationText,
    isVideoFile,
    readVideoMeta,
    sizeText,
  } from './videoMeta';
  import {
    mediaAddSourceFileApi,
    mediaAddSourceLinkApi,
    mediaAiDraftApi,
    mediaDeleteSourceApi,
    mediaGetApi,
    mediaHistoryApi,
    mediaUpdateApi,
    mediaUpdateLinksApi,
    mediaUpdateSourceApi,
    topicListApi,
  } from '@/api/media';
  import { linksKey, toLinkInputs, toLinkRows, type LinkRow } from './linkRows';
  import type {
    AvailabilityStatus,
    MediaHistoryModel,
    MediaModel,
    MediaSourceModel,
    TopicModel,
  } from '@/api/media/model/mediaModel';

  defineOptions({ name: 'MediaDrawer' });

  const TabPane = Tabs.TabPane;

  const emit = defineEmits(['success', 'register']);
  const { createMessage } = useMessage();

  const media = ref<MediaModel>();
  const tab = ref('info');
  const status = ref<string>();
  const drafting = ref(false);
  const busySource = ref<string>();
  const linkInput = ref('');
  const replaceSourceId = ref<string>();
  const linkRows = ref<LinkRow[]>([]);
  const linksBefore = ref('');
  const history = ref<MediaHistoryModel[]>([]);
  const historyLoading = ref(false);
  const topics = ref<TopicModel[]>([]);

  const accept = computed(() => (media.value?.type === 'VIDEO' ? ACCEPT_VIDEOS : ACCEPT_IMAGES));

  const [
    registerInfo,
    {
      resetFields: resetInfo,
      setFieldsValue: setInfo,
      validate: validateInfo,
      updateSchema,
      getFieldsValue: getInfo,
    },
  ] = useForm({ labelWidth: 120, schemas: infoFormSchema, showActionButtonGroup: false });

  const [registerAi, { resetFields: resetAi, setFieldsValue: setAi, validate: validateAi }] = useForm(
    {
      labelWidth: 120,
      schemas: aiFormSchema(() => status.value === 'APPROVED'),
      showActionButtonGroup: false,
    },
  );

  const sourceColumns = [
    { title: 'Xem', key: 'preview', width: 80 },
    { title: 'Nguồn', key: 'info' },
    { title: 'Kênh / tác giả', key: 'author', width: 160 },
    { title: 'Truy cập', key: 'availability', width: 160 },
    { title: '', key: 'action', width: 150 },
  ];

  const historyColumns = [
    { title: 'Lúc', key: 'at', width: 150 },
    { title: 'Người', dataIndex: 'actorName', width: 160 },
    { title: 'Thao tác', key: 'action', width: 130 },
    { title: 'Chi tiết', key: 'detail' },
  ];

  const [registerDrawer, { setDrawerProps, closeDrawer, changeLoading }] = useDrawerInner(
    async (data) => {
      media.value = undefined;
      tab.value = 'info';
      linkInput.value = '';
      replaceSourceId.value = undefined;
      history.value = [];
      await Promise.all([resetInfo(), resetAi()]);
      setDrawerProps({ confirmLoading: false });
      changeLoading(true);
      try {
        const [m, t] = await Promise.all([mediaGetApi(data.id as string), topicListApi()]);
        topics.value = t;
        // Chủ đề đã tắt vẫn hiện nếu media đang gắn, để không mất khi lưu
        await updateSchema({
          field: 'topics',
          componentProps: {
            mode: 'multiple',
        optionFilterProp: 'label',
            placeholder: 'Chọn trong bộ chủ đề',
            options: t
              .filter((x) => x.active || m.topics.includes(x.code))
              .map((x) => ({ value: x.code, label: x.active ? x.name : `${x.name} (đã tắt)` })),
          },
        });
        await load(m);
      } finally {
        changeLoading(false);
      }
    },
  );

  /** Điền form và danh sách liên kết từ media vừa tải */
  async function load(m: MediaModel) {
    media.value = m;
    status.value = m.status;
    await setInfo({
      title: m.title,
      category: m.category,
      status: m.status,
      rights: m.rights,
      topics: m.topics,
      customerStages: m.customerStages,
      allowedChannels: m.allowedChannels,
    });
    await setAi({
      type: m.type,
      summary: m.summary,
      useWhen: m.useWhen,
      avoidWhen: m.avoidWhen,
      description: m.description,
      transcript: m.transcript,
    });
    linkRows.value = toLinkRows(m.links);
    linksBefore.value = linksKey(linkRows.value);
  }

  watch(tab, async (t) => {
    if (t === 'history' && media.value) {
      historyLoading.value = true;
      try {
        history.value = await mediaHistoryApi(media.value.id);
      } finally {
        historyLoading.value = false;
      }
    }
  });

  function handleInfoChange(key: string, value: any) {
    if (key === 'status') status.value = value;
  }

  // ------------------------------------------------------------------ lưu

  async function validateOn<T>(name: string, run: () => Promise<T>): Promise<T> {
    try {
      return await run();
    } catch (e) {
      tab.value = name;
      throw e;
    }
  }

  async function handleSubmit() {
    if (!media.value) return;
    const info = await validateOn('info', validateInfo);
    const { type: _type, ...ai } = await validateOn('ai', validateAi);
    if (info.status === 'APPROVED' && (!ai.summary?.trim() || !ai.useWhen?.trim())) {
      tab.value = 'ai';
      createMessage.error('Duyệt cần có Tóm tắt và Dùng khi');
      return;
    }
    if (linkRows.value.some((r) => !r.targetKey)) {
      tab.value = 'links';
      createMessage.error('Chọn đối tượng cho mọi liên kết, hoặc gỡ dòng còn trống');
      return;
    }
    try {
      setDrawerProps({ confirmLoading: true });
      await mediaUpdateApi(media.value.id, {
        ...info,
        ...ai,
        topics: info.topics ?? [],
        customerStages: info.customerStages ?? [],
        allowedChannels: info.allowedChannels ?? [],
      } as any);
      if (linksKey(linkRows.value) !== linksBefore.value) {
        const res = await mediaUpdateLinksApi(media.value.id, toLinkInputs(linkRows.value));
        res.notes.forEach((n) => createMessage.info(n));
      }
      createMessage.success('Đã lưu media');
      closeDrawer();
      emit('success');
    } finally {
      setDrawerProps({ confirmLoading: false });
    }
  }

  // ------------------------------------------------------------------ AI viết nháp

  async function handleAiDraft() {
    if (!media.value) return;
    drafting.value = true;
    try {
      const d = await mediaAiDraftApi(media.value.id);
      await setAi({
        summary: d.summary,
        useWhen: d.useWhen,
        avoidWhen: d.avoidWhen,
        description: d.description,
      });
      const current: string[] = getInfo().topics ?? [];
      await setInfo({ topics: Array.from(new Set([...current, ...d.topics])) });
      createMessage.success(
        (d.sawImage ? 'AI đã xem ảnh và viết nháp' : 'AI đã viết nháp (không có ảnh để xem)') +
          ', xem lại rồi bấm Lưu',
      );
    } finally {
      drafting.value = false;
    }
  }

  // ------------------------------------------------------------------ nguồn (lưu ngay)

  async function afterSourceChange(m: MediaModel) {
    media.value = { ...media.value!, sources: m.sources, updatedBy: m.updatedBy };
    emit('success');
  }

  /** Tự gửi file qua API của trang (kèm thông số video), không để Upload của ant-design gửi */
  function pickFile(file: File, replaceId?: string) {
    handleAddFile(file, replaceId);
    return false;
  }

  async function handleAddFile(file: File, replaceId?: string) {
    if (!media.value) return;
    busySource.value = replaceId ?? 'new-file';
    try {
      const meta = isVideoFile(file) ? await readVideoMeta(file) : undefined;
      const m = await mediaAddSourceFileApi(media.value.id, file, meta, replaceId);
      await afterSourceChange(m);
      createMessage.success(replaceId ? 'Đã thay file' : 'Đã thêm file');
    } finally {
      busySource.value = undefined;
    }
  }

  function startReplaceLink(sourceId: string, url?: string) {
    replaceSourceId.value = sourceId;
    linkInput.value = url ?? '';
  }

  function cancelReplaceLink() {
    replaceSourceId.value = undefined;
    linkInput.value = '';
  }

  async function handleAddLink() {
    if (!media.value || !linkInput.value.trim()) return;
    busySource.value = 'new-link';
    try {
      const m = await mediaAddSourceLinkApi(
        media.value.id,
        linkInput.value.trim(),
        replaceSourceId.value,
      );
      await afterSourceChange(m);
      createMessage.success(replaceSourceId.value ? 'Đã thay link' : 'Đã thêm link');
      cancelReplaceLink();
    } finally {
      busySource.value = undefined;
    }
  }

  async function handleSourceUpdate(source: MediaSourceModel, availability?: AvailabilityStatus) {
    if (!media.value) return;
    const m = await mediaUpdateSourceApi(media.value.id, source.id, {
      availabilityStatus: availability ?? source.availabilityStatus,
      authorName: source.authorName,
    });
    await afterSourceChange(m);
  }

  async function handleDeleteSource(sourceId: string) {
    if (!media.value) return;
    const m = await mediaDeleteSourceApi(media.value.id, sourceId);
    await afterSourceChange(m);
    createMessage.success('Đã bỏ nguồn');
  }

  // ------------------------------------------------------------------ lịch sử

  function historyDetail(h: MediaHistoryModel): string {
    if (h.action === 'media.update' && h.before && h.after) {
      const changed = Object.keys(h.after).filter(
        (k) => JSON.stringify(h.before?.[k] ?? null) !== JSON.stringify(h.after?.[k] ?? null),
      );
      return changed.length
        ? 'Đổi ' + changed.map((k) => FIELD_LABELS[k] ?? k).join(', ')
        : 'Không đổi gì';
    }
    const data = h.after ?? h.before;
    if (!data) return '';
    if (Array.isArray(data)) return `${data.length} liên kết`;
    return [data.name, data.origin, data.title].filter(Boolean).join(' · ');
  }
</script>

<style lang="less" scoped>
  .media-head {
    display: flex;
    gap: 16px;
    margin-bottom: 8px;
  }

  .media-meta {
    flex: 1;
    min-width: 0;
    font-size: 13px;

    .title {
      font-size: 15px;
      font-weight: 500;
      margin-bottom: 4px;
    }
  }

  .muted {
    color: #888;
    font-size: 12px;
  }

  .draft-bar {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;
  }

  .source-add {
    display: flex;
    gap: 8px;
    margin: 12px 0 4px;

    .link-input {
      flex: 1;
    }
  }
</style>
