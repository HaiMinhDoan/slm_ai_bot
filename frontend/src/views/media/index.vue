<template>
  <PageWrapper dense contentFullHeight fixedHeight>
    <BasicTable @register="registerTable">
      <template #toolbar>
        <Button type="primary" @click="openImportModal(true, {})">Nhập combo từ Drive</Button>
      </template>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'preview'">
          <MediaThumb
            v-if="storedSource(record)"
            :mediaId="record.id"
            :sourceId="storedSource(record)!.id"
          />
          <a
            v-else-if="externalSource(record)"
            :href="externalSource(record)!.sourceUrl"
            target="_blank"
            rel="noopener noreferrer"
          >
            Mở link
          </a>
        </template>
        <template v-else-if="column.key === 'type'">
          <Tag :color="record.type === 'IMAGE' ? 'blue' : 'purple'">
            {{ record.type === 'IMAGE' ? 'Ảnh' : 'Video' }}
          </Tag>
        </template>
        <template v-else-if="column.key === 'status'">
          <Tag :color="STATUS_COLORS[record.status]">{{ STATUS_LABELS[record.status] }}</Tag>
        </template>
        <template v-else-if="column.key === 'source'">
          <div v-for="s in record.sources" :key="s.id" class="small">
            {{ SOURCE_LABELS[s.sourceType] ?? s.sourceType }}
            <span v-if="s.width">· {{ s.width }}×{{ s.height }}</span>
            <span v-if="s.sizeBytes">· {{ Math.round(s.sizeBytes / 1024) }} KB</span>
          </div>
        </template>
        <template v-else-if="column.key === 'links'">
          <Tag
            v-for="l in record.links"
            :key="l.targetType + l.targetKey + l.role"
            class="link-tag"
          >
            {{ l.targetType === 'PACKAGE' ? 'Gói' : l.targetType }} {{ l.targetKey }}
            <span v-if="l.role === 'MAIN_IMAGE'">(ảnh chính)</span>
          </Tag>
        </template>
      </template>
    </BasicTable>

    <ImportModal @register="registerImportModal" @success="reload" />
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { Tag } from 'ant-design-vue';
  import { Button } from '@/components/Button';
  import { BasicTable, useTable, type BasicColumn, type FormSchema } from '@/components/Table';
  import { PageWrapper } from '@/components/Page';
  import { useModal } from '@/components/Modal';
  import { mediaListApi } from '@/api/media';
  import type { MediaModel, MediaSourceModel } from '@/api/media/model/mediaModel';
  import { formatToDateTime } from '@/utils/dateUtil';
  import ImportModal from './ImportModal.vue';
  import MediaThumb from './MediaThumb.vue';

  defineOptions({ name: 'MediaLibrary' });

  const STATUS_LABELS: Record<string, string> = {
    DRAFT: 'Nháp',
    APPROVED: 'Đã duyệt',
    ARCHIVED: 'Lưu trữ',
  };
  const STATUS_COLORS: Record<string, string> = {
    DRAFT: 'orange',
    APPROVED: 'green',
    ARCHIVED: 'default',
  };
  const SOURCE_LABELS: Record<string, string> = {
    UPLOAD: 'File trong hệ thống',
    FACEBOOK: 'Facebook',
    YOUTUBE: 'YouTube',
    TIKTOK: 'TikTok',
    SLM_V2: 'slm_v2',
  };

  const [registerImportModal, { openModal: openImportModal }] = useModal();

  const columns: BasicColumn[] = [
    { title: 'Xem', key: 'preview', dataIndex: 'preview', width: 90 },
    { title: 'Loại', key: 'type', dataIndex: 'type', width: 80 },
    { title: 'Tiêu đề', dataIndex: 'title', align: 'left', width: 320 },
    { title: 'Trạng thái', key: 'status', dataIndex: 'status', width: 100 },
    { title: 'Nguồn', key: 'source', dataIndex: 'sources', width: 220 },
    { title: 'Liên kết', key: 'links', dataIndex: 'links', align: 'left' },
    {
      title: 'Ngày nhập',
      dataIndex: 'createdAt',
      width: 160,
      customRender: ({ record }) => formatToDateTime(record.createdAt),
    },
  ];

  const searchFormSchema: FormSchema[] = [
    {
      field: 'type',
      label: 'Loại',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: [
          { label: 'Ảnh', value: 'IMAGE' },
          { label: 'Video', value: 'VIDEO' },
        ],
      },
      colProps: { span: 6 },
    },
  ];

  const loadMedia = async (params: Recordable) => {
    const { page = 1, size = 20, type } = params;
    return mediaListApi({ type, page: page - 1, size });
  };

  const [registerTable, { reload }] = useTable({
    title: 'Thư viện media',
    api: loadMedia,
    rowKey: 'id',
    columns,
    formConfig: { labelWidth: 60, schemas: searchFormSchema },
    useSearchForm: true,
    fetchSetting: { sizeField: 'size' },
    showTableSetting: true,
    bordered: true,
  });

  function storedSource(record: Recordable): MediaSourceModel | undefined {
    return (record as MediaModel).sources.find((s) => s.stored);
  }

  function externalSource(record: Recordable): MediaSourceModel | undefined {
    return (record as MediaModel).sources.find((s) => !s.stored && s.sourceUrl);
  }
</script>

<style lang="less" scoped>
  .small {
    font-size: 12px;
  }

  .link-tag {
    margin-bottom: 4px;
  }
</style>
