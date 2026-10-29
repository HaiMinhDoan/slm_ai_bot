import type { BasicColumn, FormSchema } from '@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';
import type { MediaLinkModel, MediaRights, MediaStatus } from '@/api/media/model/mediaModel';
import { formatToDateTime } from '@/utils/dateUtil';

export const STATUS_LABELS: Record<MediaStatus, string> = {
  DRAFT: 'Nháp',
  APPROVED: 'Đã duyệt',
  ARCHIVED: 'Lưu trữ',
};

export const STATUS_COLORS: Record<MediaStatus, string> = {
  DRAFT: 'orange',
  APPROVED: 'green',
  ARCHIVED: 'default',
};

export const RIGHTS_LABELS: Record<MediaRights, string> = {
  OWNED: 'Của công ty',
  LICENSED: 'Được cấp phép',
  LINK_ONLY: 'Chỉ gửi link',
  NEEDS_REVIEW: 'Chưa rõ quyền',
};

export const SOURCE_LABELS: Record<string, string> = {
  UPLOAD: 'File trong hệ thống',
  FACEBOOK: 'Facebook',
  YOUTUBE: 'YouTube',
  TIKTOK: 'TikTok',
  SLM_V2: 'slm_v2',
};

const ROLE_LABELS: Record<string, string> = {
  MAIN_IMAGE: 'ảnh chính',
  GALLERY: 'ảnh thêm',
};

/** "Gói 98 (ảnh chính)" */
export function linkText(l: MediaLinkModel): string {
  const target = l.targetType === 'PACKAGE' ? 'Gói' : l.targetType;
  const role = ROLE_LABELS[l.role];
  return `${target} ${l.targetKey}` + (role ? ` (${role})` : '');
}

const options = (labels: Record<string, string>) =>
  Object.entries(labels).map(([value, label]) => ({ label, value }));

export const columns: BasicColumn[] = [
  { title: 'Xem', key: 'preview', dataIndex: 'preview', width: 90 },
  { title: 'Mã', dataIndex: 'indexCode', width: 60 },
  {
    title: 'Loại',
    dataIndex: 'type',
    width: 70,
    customRender: ({ record }) =>
      h(Tag, { color: record.type === 'IMAGE' ? 'blue' : 'purple' }, () =>
        record.type === 'IMAGE' ? 'Ảnh' : 'Video',
      ),
  },
  {
    // Không đặt độ rộng: cột này giãn theo chỗ còn lại để cả bảng vừa màn hình, cột Liên kết không bị
    // cột Thao tác (cố định bên phải) che mất
    title: 'Tiêu đề',
    dataIndex: 'title',
    align: 'left',
    customRender: ({ record }) =>
      h('div', {}, [
        h('div', {}, record.title),
        record.summary
          ? h('div', { style: 'color:#888;font-size:12px' }, record.summary)
          : h('div', { style: 'color:#d48806;font-size:12px' }, 'Chưa có tóm tắt cho AI'),
      ]),
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status',
    width: 110,
    customRender: ({ record }) =>
      h('div', {}, [
        record.deleted
          ? h(Tag, {}, () => 'Đã xoá')
          : h(Tag, { color: STATUS_COLORS[record.status] }, () => STATUS_LABELS[record.status]),
        h('div', { style: 'color:#888;font-size:12px' }, RIGHTS_LABELS[record.rights]),
      ]),
  },
  { title: 'Nguồn', key: 'source', dataIndex: 'sources', width: 160, ellipsis: false },
  {
    title: 'Liên kết',
    key: 'links',
    dataIndex: 'links',
    width: 170,
    align: 'left',
    ellipsis: false,
  },
  {
    title: 'Ngày nhập',
    dataIndex: 'createdAt',
    width: 140,
    customRender: ({ record }) => formatToDateTime(record.createdAt),
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'keyword',
    label: 'Tìm kiếm',
    component: 'Input',
    componentProps: { placeholder: 'Tiêu đề hoặc mã M07' },
    colProps: { span: 6 },
  },
  {
    field: 'type',
    label: 'Loại',
    component: 'Select',
    componentProps: {
      allowClear: true,
      placeholder: 'Tất cả',
      options: [
        { label: 'Ảnh', value: 'IMAGE' },
        { label: 'Video', value: 'VIDEO' },
      ],
    },
    colProps: { span: 6 },
  },
  {
    field: 'status',
    label: 'Trạng thái',
    component: 'Select',
    componentProps: {
      allowClear: true,
      placeholder: 'Tất cả',
      options: [...options(STATUS_LABELS), { label: 'Đã xoá', value: 'DELETED' }],
    },
    colProps: { span: 6 },
  },
  {
    field: 'packageId',
    label: 'Mã gói',
    component: 'InputNumber',
    componentProps: { min: 1, precision: 0, placeholder: 'tron_goi.id', style: { width: '100%' } },
    colProps: { span: 6 },
  },
];

/**
 * Form sửa media. AI chỉ hiểu media qua Tóm tắt / Dùng khi / Tránh khi (mục 6.2), nên duyệt bắt buộc có
 * hai ô đầu; backend kiểm tra lại.
 */
export const mediaFormSchema: FormSchema[] = [
  {
    field: 'title',
    label: 'Tiêu đề',
    component: 'Input',
    required: true,
    componentProps: { maxlength: 300 },
    colProps: { span: 24 },
  },
  {
    field: 'status',
    label: 'Trạng thái',
    component: 'Select',
    required: true,
    componentProps: { options: options(STATUS_LABELS) },
    helpMessage: [
      'Nháp: AI chưa dùng.',
      'Đã duyệt: AI được chọn gửi cho khách (khi có tính năng gửi media), cần Tóm tắt và Dùng khi.',
      'Lưu trữ: giữ lại để tra cứu, AI không dùng.',
    ],
    colProps: { span: 12 },
  },
  {
    field: 'rights',
    label: 'Quyền sử dụng',
    component: 'Select',
    required: true,
    componentProps: { options: options(RIGHTS_LABELS) },
    helpMessage: 'Chỉ gửi link: video Facebook / YouTube, không tải về rồi đăng lại.',
    colProps: { span: 12 },
  },
  {
    field: 'summary',
    label: 'Tóm tắt',
    component: 'InputTextArea',
    componentProps: {
      rows: 2,
      maxlength: 2000,
      placeholder: 'Media nói gì, ví dụ: ảnh hệ Hybrid 5 kWp, pin 5,12 kWh lắp trên mái tôn',
    },
    dynamicRules: ({ values }) =>
      values.status === 'APPROVED' ? [{ required: true, message: 'Duyệt cần có tóm tắt' }] : [],
    colProps: { span: 24 },
  },
  {
    field: 'useWhen',
    label: 'Dùng khi',
    component: 'InputTextArea',
    componentProps: {
      rows: 2,
      maxlength: 2000,
      placeholder: 'Khi nào nên gửi, ví dụ: khách hỏi gói 5 kWp hoặc muốn xem hệ thực tế',
    },
    dynamicRules: ({ values }) =>
      values.status === 'APPROVED' ? [{ required: true, message: 'Duyệt cần có "dùng khi"' }] : [],
    colProps: { span: 24 },
  },
  {
    field: 'avoidWhen',
    label: 'Tránh khi',
    component: 'InputTextArea',
    componentProps: { rows: 2, maxlength: 2000, placeholder: 'Không bắt buộc' },
    colProps: { span: 24 },
  },
  {
    field: 'description',
    label: 'Mô tả chi tiết',
    component: 'InputTextArea',
    componentProps: { rows: 3, maxlength: 10000 },
    colProps: { span: 24 },
  },
  {
    field: 'transcript',
    label: 'Lời thoại',
    component: 'InputTextArea',
    componentProps: { rows: 3, maxlength: 50000 },
    ifShow: ({ values }) => values.type === 'VIDEO',
    colProps: { span: 24 },
  },
  // Không hiện, chỉ để ô Lời thoại biết đây là video
  { field: 'type', label: '', component: 'Input', show: false },
];
