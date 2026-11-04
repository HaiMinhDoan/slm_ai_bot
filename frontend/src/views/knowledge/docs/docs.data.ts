import type { BasicColumn, FormSchema } from '@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';
import { formatToDateTime } from '@/utils/dateUtil';
import type { KnowledgeDocType } from '@/api/knowledge/model/knowledgeModel';
import {
  CREATABLE_TYPES,
  DOC_TYPE_COLORS,
  DOC_TYPE_FOLDERS,
  DOC_TYPE_LABELS,
} from '../shared/labels';

export const columns: BasicColumn[] = [
  {
    title: 'Loại',
    dataIndex: 'type',
    width: 140,
    customRender: ({ record }) =>
      h(Tag, { color: DOC_TYPE_COLORS[record.type as KnowledgeDocType] }, () => record.typeLabel),
  },
  {
    title: 'Tài liệu',
    dataIndex: 'title',
    align: 'left',
    customRender: ({ record }) =>
      h('div', {}, [
        h('div', {}, record.title),
        h('div', { style: 'color:#888;font-size:12px' }, record.key),
      ]),
  },
  {
    title: 'Bot đang dùng',
    dataIndex: 'activeVersionNo',
    width: 130,
    customRender: ({ record }) =>
      record.activeVersionNo
        ? h(Tag, { color: 'green' }, () => `v${record.activeVersionNo}`)
        : h(Tag, {}, () => 'Chưa phát hành'),
  },
  {
    title: 'Bản mới nhất',
    dataIndex: 'latestVersionNo',
    width: 110,
    customRender: ({ record }) => (record.latestVersionNo ? `v${record.latestVersionNo}` : ''),
  },
  {
    title: 'Bản nháp',
    dataIndex: 'draftVersionId',
    width: 210,
    align: 'left',
    customRender: ({ record }) =>
      record.draftVersionId
        ? h('div', {}, [
            h(Tag, { color: 'orange' }, () => 'Có nháp chưa phát hành'),
            h(
              'div',
              { style: 'color:#888;font-size:12px' },
              `${record.draftAuthor} · ${formatToDateTime(record.draftUpdatedAt)}`,
            ),
          ])
        : '',
  },
  {
    title: 'Bot đọc',
    dataIndex: 'botReads',
    width: 110,
    helpMessage:
      'Rule do code đọc (không đưa vào prompt); hướng dẫn soạn và mẫu chỉ dành cho người soạn và AI hỗ trợ soạn.',
    customRender: ({ record }) =>
      record.type === 'PROMPT' ? 'Có (chỉ dẫn)' : record.botReads ? 'Có' : 'Không',
  },
];

export const typeOptions = (types: KnowledgeDocType[]) =>
  types.map((t) => ({ label: `${DOC_TYPE_LABELS[t]} (${DOC_TYPE_FOLDERS[t]}/)`, value: t }));

export const searchFormSchema: FormSchema[] = [
  {
    field: 'keyword',
    label: 'Tìm kiếm',
    component: 'Input',
    componentProps: { placeholder: 'Tiêu đề hoặc mã' },
    colProps: { span: 6 },
  },
  {
    field: 'type',
    label: 'Loại',
    component: 'Select',
    componentProps: {
      allowClear: true,
      placeholder: 'Tất cả',
      options: typeOptions(Object.keys(DOC_TYPE_LABELS) as KnowledgeDocType[]),
    },
    colProps: { span: 6 },
  },
  {
    field: 'status',
    label: 'Trạng thái',
    component: 'Select',
    componentProps: {
      allowClear: true,
      placeholder: 'Đang dùng',
      options: [
        { label: 'Có bản nháp', value: 'DRAFT' },
        { label: 'Chưa phát hành', value: 'NOT_RELEASED' },
        { label: 'Đã lưu trữ', value: 'ARCHIVED' },
      ],
    },
    colProps: { span: 6 },
  },
];

/** "Chính sách bảo hành" → "chinh-sach-bao-hanh" */
export function slug(text: string): string {
  return text
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/[đĐ]/g, 'd')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '');
}

export const createFormSchema: FormSchema[] = [
  {
    field: 'type',
    label: 'Loại',
    component: 'Select',
    required: true,
    defaultValue: 'POLICY',
    componentProps: { options: typeOptions(CREATABLE_TYPES) },
    helpMessage: [
      'Chính sách, Hỏi đáp, Cách tư vấn, Chat mẫu, Cốt lõi: bot đọc trong mọi lượt trả lời.',
      'Hướng dẫn soạn, Mẫu tài liệu: chỉ dùng khi soạn, bot không đọc.',
    ],
  },
  {
    field: 'title',
    label: 'Tiêu đề',
    component: 'Input',
    required: true,
    componentProps: { maxlength: 300, placeholder: 'Ví dụ: Chính sách bảo hành' },
  },
  {
    field: 'name',
    label: 'Tên trong mã',
    component: 'Input',
    required: true,
    componentProps: { placeholder: 'bao-hanh' },
    helpMessage: 'Viết thường, không dấu, nối bằng gạch ngang; tự gợi ý từ tiêu đề.',
    rules: [
      {
        pattern: /^[a-z0-9]+(-[a-z0-9]+)*$/,
        message: 'Chỉ chữ thường không dấu, số và gạch ngang',
      },
    ],
  },
];
