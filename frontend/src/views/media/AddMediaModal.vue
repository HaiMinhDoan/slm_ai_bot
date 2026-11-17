<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="Thêm media"
    width="960px"
    :minHeight="420"
    :okText="okText"
    :okButtonProps="{ disabled: !count || running, loading: running }"
    cancelText="Đóng"
    :closeFunc="handleClose"
    @ok="handleRun"
  >
    <Tabs v-model:activeKey="tab">
      <TabPane key="files" tab="Tải file lên" :disabled="running">
        <!-- Khối bọc: wrapper của Upload là phần tử inline, không bọc thì khung tab tính sai chiều cao -->
        <div>
          <UploadDragger
            :accept="`${ACCEPT_IMAGES},${ACCEPT_VIDEOS}`"
            multiple
            :showUploadList="false"
            :beforeUpload="pickFile"
            :disabled="running"
          >
            <p class="drop-title">Kéo thả hoặc bấm để chọn ảnh, video</p>
            <p class="muted">
              Ảnh JPEG / PNG / WebP tối đa 15 MB; video MP4 / MOV / WebM tối đa 100 MB. Mỗi file thành
              một media.
            </p>
          </UploadDragger>
        </div>
        <div v-if="files.length" class="picked">
          <Tag v-for="f in files" :key="f.uid" closable @close="removeFile(f)">
            {{ f.name }} · {{ sizeText(f.size) }}
          </Tag>
        </div>
      </TabPane>

      <TabPane key="links" tab="Dán link" :disabled="running">
        <Textarea
          v-model:value="rawLinks"
          :rows="4"
          :disabled="running"
          placeholder="Mỗi dòng một link: video YouTube, Facebook (reel, video, share), TikTok, hoặc ảnh Google Drive"
        />
        <div class="muted mt-1">
          Video YouTube / TikTok tự lấy tiêu đề; video Facebook đặt tạm "Video Facebook" nếu không nhập
          tiêu đề. Ảnh Drive phải chia sẻ công khai.
        </div>
      </TabPane>
    </Tabs>

    <Divider orientation="left" class="section">Thông tin media</Divider>
    <div class="muted mb-2">
      {{
        count > 1
          ? `Áp chung cho ${count} media; sửa riêng từng media sau ở nút Sửa.`
          : 'Điền đủ thì không cần mở form sửa sau khi thêm.'
      }}
    </div>
    <BasicForm @register="registerForm" />

    <Divider orientation="left" class="section">Liên kết</Divider>
    <LinkEditor v-model:rows="linkRows" :mediaType="singleType" />

    <Progress v-if="running" :percent="progress" size="small" class="mt-3" />
    <Table
      v-if="results.length"
      :columns="columns"
      :data-source="results"
      :pagination="false"
      :scroll="{ y: 240 }"
      rowKey="key"
      size="small"
      bordered
      class="mt-3"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'outcome'">
          <Tag :color="OUTCOME_COLORS[record.outcome]">{{ OUTCOME_LABELS[record.outcome] }}</Tag>
        </template>
        <template v-else-if="column.key === 'open'">
          <Button v-if="record.mediaId" size="small" type="link" @click="emit('open', record.mediaId)">
            Mở
          </Button>
        </template>
      </template>
    </Table>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { computed, nextTick, ref, watch } from 'vue';
  // Dự án không đăng ký sẵn component Ant Design: import ngay trong file dùng nó
  import { Divider, Input, Progress, Table, Tabs, Tag, Upload } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { BasicForm, useForm } from '@/components/Form';
  import { BasicModal, useModalInner } from '@/components/Modal';
  import { useMessage } from '@/hooks/web/useMessage';
  import { mediaAddByLinkApi, mediaUploadApi, topicListApi } from '@/api/media';
  import type {
    CreateOutcome,
    MediaCreateInfo,
    MediaCreateResult,
  } from '@/api/media/model/mediaModel';
  import LinkEditor from './LinkEditor.vue';
  import { toLinkInputs, type LinkRow } from './linkRows';
  import { addFormSchema } from './media.data';
  import { ACCEPT_IMAGES, ACCEPT_VIDEOS, isVideoFile, readVideoMeta, sizeText } from './videoMeta';

  defineOptions({ name: 'AddMediaModal' });

  const TabPane = Tabs.TabPane;
  const Textarea = Input.TextArea;
  const UploadDragger = Upload.Dragger;

  /** success: có media mới (tải lại danh sách); open: mở form sửa một media vừa thêm */
  const emit = defineEmits(['success', 'open', 'register']);
  const { createMessage } = useMessage();

  const OUTCOME_LABELS: Record<CreateOutcome, string> = {
    CREATED: 'Đã thêm',
    EXISTS: 'Đã có',
    FAILED: 'Lỗi',
  };
  const OUTCOME_COLORS: Record<CreateOutcome, string> = {
    CREATED: 'green',
    EXISTS: 'blue',
    FAILED: 'red',
  };
  const columns = [
    { title: 'File / link', dataIndex: 'name', ellipsis: true },
    { title: 'Kết quả', key: 'outcome', width: 90 },
    { title: 'Ghi chú', dataIndex: 'note' },
    { title: '', key: 'open', width: 60 },
  ];

  const tab = ref('files');
  const files = ref<(File & { uid: string })[]>([]);
  const rawLinks = ref('');
  const linkRows = ref<LinkRow[]>([]);
  const running = ref(false);
  const progress = ref(0);
  const results = ref<(MediaCreateResult & { key: number })[]>([]);
  let created = false;

  const links = computed(() =>
    Array.from(
      new Set(
        rawLinks.value
          .split(/\s+/)
          .map((l) => l.trim())
          .filter((l) => l.startsWith('http')),
      ),
    ),
  );
  const count = computed(() => (tab.value === 'files' ? files.value.length : links.value.length));
  /** Link ảnh Google Drive (tải về thành ảnh); link khác là video */
  const isDriveLink = (l: string) => /^https?:\/\/(drive|docs)\.google\.com\//i.test(l);
  const hasVideo = computed(() =>
    tab.value === 'files' ? files.value.some(isVideoFile) : links.value.some((l) => !isDriveLink(l)),
  );
  /** Loại media khi biết chắc (để ẩn vai trò chỉ dành cho ảnh); trộn ảnh và video thì để backend kiểm tra */
  const singleType = computed(() => {
    const kinds =
      tab.value === 'files'
        ? files.value.map((f) => (isVideoFile(f) ? 'VIDEO' : 'IMAGE'))
        : links.value.map((l) => (isDriveLink(l) ? 'IMAGE' : 'VIDEO'));
    if (!kinds.length) return tab.value === 'links' ? 'VIDEO' : undefined;
    return kinds.every((k) => k === kinds[0]) ? (kinds[0] as 'IMAGE' | 'VIDEO') : undefined;
  });
  const okText = computed(() =>
    count.value ? `Thêm ${count.value} ${tab.value === 'files' ? 'file' : 'link'}` : 'Thêm',
  );

  const [registerForm, { resetFields, validate, updateSchema, setFieldsValue }] = useForm({
    labelWidth: 120,
    showActionButtonGroup: false,
    schemas: addFormSchema(
      () => count.value === 1,
      () => hasVideo.value,
    ),
  });

  const [registerModal, { redoModalHeight }] = useModalInner(async () => {
    tab.value = 'files';
    files.value = [];
    rawLinks.value = '';
    linkRows.value = [];
    results.value = [];
    progress.value = 0;
    created = false;
    await resetFields();
    const topics = await topicListApi();
    await updateSchema({
      field: 'topics',
      componentProps: {
        mode: 'multiple',
        optionFilterProp: 'label',
        placeholder: 'Chọn trong bộ chủ đề',
        options: topics.filter((t) => t.active).map((t) => ({ value: t.code, label: t.name })),
      },
    });
  });

  // Khung hộp thoại tính chiều cao một lần lúc mở: đổi tab, chọn file, có kết quả thì tính lại
  watch(
    () => [tab.value, files.value.length, results.value.length, linkRows.value.length],
    () => nextTick(() => redoModalHeight?.()),
  );

  // Thêm nhiều mục thì không nhập được tiêu đề chung: xoá tiêu đề đã gõ
  watch(count, (n) => {
    if (n > 1) setFieldsValue({ title: undefined });
  });

  function pickFile(file: File & { uid: string }) {
    if (!files.value.some((f) => f.uid === file.uid)) files.value.push(file);
    return false;
  }

  function removeFile(file: File & { uid: string }) {
    files.value = files.value.filter((f) => f.uid !== file.uid);
  }

  /** Lỗi gọi API (mất mạng, file quá giới hạn của proxy) thành một dòng Lỗi, không dừng các mục sau */
  function failed(name: string, e: any): MediaCreateResult {
    const status = e?.response?.status;
    return {
      name,
      outcome: 'FAILED',
      note:
        status === 413
          ? 'File vượt giới hạn dung lượng của máy chủ'
          : e?.response?.data?.message ?? e?.message ?? 'Lỗi không rõ',
    };
  }

  /** Nội dung form thành thông tin gửi kèm từng mục; trường trống không gửi để backend dùng mặc định */
  async function buildInfo(): Promise<MediaCreateInfo | undefined> {
    const v = await validate();
    if (linkRows.value.some((r) => !r.targetKey)) {
      createMessage.error('Chọn đối tượng cho mọi liên kết, hoặc gỡ dòng còn trống');
      return undefined;
    }
    const images = tab.value === 'files' ? files.value.filter((f) => !isVideoFile(f)).length : 0;
    if (images > 1 && linkRows.value.some((r) => r.role === 'MAIN_IMAGE')) {
      createMessage.error('Mỗi gói chỉ có một ảnh chính: thêm nhiều ảnh thì chọn vai trò Ảnh thêm');
      return undefined;
    }
    return {
      title: count.value === 1 ? v.title || undefined : undefined,
      status: v.aiDraft ? 'DRAFT' : v.status,
      rights: v.rights || undefined,
      summary: v.summary,
      useWhen: v.useWhen,
      avoidWhen: v.avoidWhen,
      description: v.description,
      transcript: v.transcript,
      topics: v.topics ?? [],
      customerStages: v.customerStages ?? [],
      allowedChannels: v.allowedChannels ?? [],
      links: toLinkInputs(linkRows.value),
      aiDraft: !!v.aiDraft,
    };
  }

  async function runAll<T>(
    items: T[],
    name: (t: T) => string,
    call: (t: T) => Promise<MediaCreateResult>,
  ) {
    running.value = true;
    progress.value = 0;
    results.value = [];
    try {
      for (let i = 0; i < items.length; i++) {
        let r: MediaCreateResult;
        try {
          r = await call(items[i]);
        } catch (e) {
          r = failed(name(items[i]), e);
        }
        results.value.push({ ...r, key: i });
        if (r.outcome === 'CREATED') created = true;
        progress.value = Math.round(((i + 1) / items.length) * 100);
      }
    } finally {
      running.value = false;
    }
  }

  async function handleRun() {
    const info = await buildInfo();
    if (!info) return;
    // Mục lỗi giữ lại để sửa thông tin rồi bấm thêm lại; mục đã thêm hoặc đã có thì bỏ khỏi danh sách
    if (tab.value === 'files') {
      const items = [...files.value];
      await runAll(
        items,
        (f) => f.name,
        async (f) => mediaUploadApi(f, isVideoFile(f) ? await readVideoMeta(f) : undefined, info),
      );
      files.value = items.filter((_, i) => results.value[i]?.outcome === 'FAILED');
    } else {
      const items = [...links.value];
      await runAll(
        items,
        (l) => l,
        (l) => mediaAddByLinkApi(l, info),
      );
      rawLinks.value = items.filter((_, i) => results.value[i]?.outcome === 'FAILED').join('\n');
    }
    // Thêm đúng một mục: mở luôn form sửa để xem lại (ví dụ nháp AI vừa viết)
    const added = results.value.filter((r) => r.outcome === 'CREATED');
    if (results.value.length === 1 && added.length === 1) {
      emit('open', added[0].mediaId);
    }
  }

  async function handleClose() {
    if (running.value) return false;
    if (created) emit('success');
    return true;
  }
</script>

<style lang="less" scoped>
  .drop-title {
    font-size: 15px;
    margin-bottom: 4px;
  }

  .muted {
    color: #888;
    font-size: 12px;
  }

  .picked {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    margin-top: 8px;
  }

  .section {
    margin: 16px 0 8px;
  }
</style>
