import type { BasicColumn } from '@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';
import { formatToDateTime } from '@/utils/dateUtil';
import type { ReleaseStatus } from '@/api/knowledge/model/knowledgeModel';
import { RELEASE_STATUS } from '../shared/labels';

const who = (by?: string, at?: string) =>
  by
    ? h('div', {}, [
        h('div', {}, by),
        h('div', { style: 'color:#888;font-size:12px' }, formatToDateTime(at)),
      ])
    : '';

export const columns: BasicColumn[] = [
  {
    title: 'Bản',
    dataIndex: 'id',
    width: 120,
    customRender: ({ record }) =>
      h('div', {}, [
        h('div', {}, `#${record.id}`),
        h('div', { style: 'color:#888;font-size:12px' }, record.kbVersion),
      ]),
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status',
    width: 130,
    customRender: ({ record }) => {
      const s = RELEASE_STATUS[record.status as ReleaseStatus];
      return h(Tag, { color: s?.color }, () => s?.label ?? record.status);
    },
  },
  { title: 'Ghi chú', dataIndex: 'notes', align: 'left' },
  { title: 'Tài liệu', dataIndex: 'docCount', width: 90 },
  {
    title: 'Token kiến thức',
    dataIndex: 'tokenTotal',
    width: 130,
    customRender: ({ record }) =>
      record.tokenTotal ? record.tokenTotal.toLocaleString('vi-VN') : '',
  },
  {
    title: 'Tạo',
    dataIndex: 'createdBy',
    width: 200,
    customRender: ({ record }) => who(record.createdBy, record.createdAt),
  },
  {
    title: 'Kích hoạt gần nhất',
    dataIndex: 'activatedBy',
    width: 200,
    customRender: ({ record }) => who(record.activatedBy, record.activatedAt),
  },
];

export const draftColumns: BasicColumn[] = [
  {
    title: 'Tài liệu',
    dataIndex: 'title',
    align: 'left',
    customRender: ({ record }) =>
      h('div', {}, [
        h('div', {}, record.title),
        h('div', { style: 'color:#888;font-size:12px' }, `${record.key} · ${record.typeLabel}`),
      ]),
  },
  {
    title: 'Thay đổi',
    dataIndex: 'versionNo',
    width: 140,
    customRender: ({ record }) =>
      record.activeVersionNo
        ? `v${record.activeVersionNo} → v${record.versionNo}`
        : `Mới: v${record.versionNo}`,
  },
  { title: 'Ghi chú', dataIndex: 'changeNote', align: 'left' },
  {
    title: 'Người sửa',
    dataIndex: 'author',
    width: 190,
    customRender: ({ record }) =>
      h('div', {}, [
        h('div', {}, record.author),
        record.aiAssisted ? h(Tag, { color: 'purple' }, () => 'AI soạn') : null,
      ]),
  },
];
