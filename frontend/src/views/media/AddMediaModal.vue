<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    title="Thêm media"
    width="900px"
    :minHeight="340"
    :showOkBtn="false"
    cancelText="Đóng"
    :closeFunc="handleClose"
  >
    <Tabs v-model:activeKey="tab">
      <TabPane key="files" tab="Tải file lên">
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
              một media nháp.
            </p>
          </UploadDragger>
        </div>
        <div v-if="files.length" class="picked">
          <Tag v-for="f in files" :key="f.uid" closable @close="removeFile(f)">
            {{ f.name }} · {{ sizeText(f.size) }}
          </Tag>
        </div>
        <div class="actions">
          <span>Quyền sử dụng</span>
          <Select v-model:value="fileRights" :options="rightsOptions" style="width: 200px" />
          <Button type="primary" :disabled="!files.length || running" :loading="running" @click="runFiles">
            Thêm {{ files.length || '' }} file
          </Button>
        </div>
      </TabPane>

      <TabPane key="links" tab="Dán link">
        <Textarea
          v-model:value="rawLinks"
          :rows="6"
          :disabled="running"
          placeholder="Mỗi dòng một link: video YouTube, Facebook (reel, video, share), TikTok, hoặc ảnh Google Drive"
        />
        <div class="actions">
          <span>Quyền sử dụng</span>
          <Select v-model:value="linkRights" :options="rightsOptions" style="width: 200px" />
          <Button type="primary" :disabled="!links.length || running" :loading="running" @click="runLinks">
            Thêm {{ links.length || '' }} link
          </Button>
        </div>
        <div class="muted">
          Video YouTube / TikTok tự lấy tiêu đề; video Facebook đặt tạm tiêu đề "Video Facebook", sửa sau.
          Ảnh Drive phải chia sẻ công khai.
        </div>
      </TabPane>
    </Tabs>

    <Progress v-if="running" :percent="progress" size="small" class="mt-2" />
    <Table
      v-if="results.length"
      :columns="columns"
      :data-source="results"
      :pagination="false"
      :scroll="{ y: 300 }"
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
  import { Input, Progress, Select, Table, Tabs, Tag, Upload } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { BasicModal, useModalInner } from '@/components/Modal';
  import { mediaAddByLinkApi, mediaUploadApi } from '@/api/media';
  import type {
    CreateOutcome,
    MediaCreateResult,
    MediaRights,
  } from '@/api/media/model/mediaModel';
  import { RIGHTS_LABELS, options } from './media.data';
  import { ACCEPT_IMAGES, ACCEPT_VIDEOS, isVideoFile, readVideoMeta, sizeText } from './videoMeta';

  defineOptions({ name: 'AddMediaModal' });

  const TabPane = Tabs.TabPane;
  const Textarea = Input.TextArea;
  const UploadDragger = Upload.Dragger;

  /** success: có media mới (tải lại danh sách); open: mở form sửa một media vừa thêm */
  const emit = defineEmits(['success', 'open', 'register']);

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
  const rightsOptions = options(RIGHTS_LABELS);
  const columns = [
    { title: 'File / link', dataIndex: 'name', ellipsis: true },
    { title: 'Kết quả', key: 'outcome', width: 90 },
    { title: 'Ghi chú', dataIndex: 'note' },
    { title: '', key: 'open', width: 60 },
  ];

  const tab = ref('files');
  const files = ref<(File & { uid: string })[]>([]);
  const fileRights = ref<MediaRights>('OWNED');
  const rawLinks = ref('');
  const linkRights = ref<MediaRights>('LINK_ONLY');
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

  const [registerModal, { redoModalHeight }] = useModalInner(() => {
    tab.value = 'files';
    files.value = [];
    rawLinks.value = '';
    results.value = [];
    progress.value = 0;
    created = false;
  });

  // Khung hộp thoại tính chiều cao một lần lúc mở: đổi tab, chọn file, có kết quả thì tính lại
  watch(
    () => [tab.value, files.value.length, results.value.length],
    () => nextTick(() => redoModalHeight?.()),
  );

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

  async function runAll<T>(items: T[], name: (t: T) => string, call: (t: T) => Promise<MediaCreateResult>) {
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

  async function runFiles() {
    await runAll(
      files.value,
      (f) => f.name,
      async (f) => mediaUploadApi(f, isVideoFile(f) ? await readVideoMeta(f) : undefined, fileRights.value),
    );
    files.value = [];
  }

  async function runLinks() {
    await runAll(
      links.value,
      (l) => l,
      (l) => mediaAddByLinkApi(l, undefined, linkRights.value),
    );
    rawLinks.value = '';
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

  .actions {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 12px 0 4px;
  }
</style>
