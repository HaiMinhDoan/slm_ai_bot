import type { BasicColumn, FormSchema } from '@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';
import type {
  AvailabilityStatus,
  LinkRole,
  LinkTargetType,
  MediaLinkModel,
  MediaRights,
  MediaStatus,
} from '@/api/media/model/mediaModel';
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

export const ROLE_LABELS: Record<LinkRole, string> = {
  MAIN_IMAGE: 'Ảnh chính',
  GALLERY: 'Ảnh thêm',
  EXPLAINER: 'Giải thích',
  CASE_STUDY: 'Công trình thực tế',
  GUIDE: 'Hướng dẫn',
  RELATED: 'Liên quan',
};

/** Vai trò chỉ dành cho ảnh */
export const IMAGE_ONLY_ROLES: LinkRole[] = ['MAIN_IMAGE', 'GALLERY'];

export const TARGET_LABELS: Record<LinkTargetType, string> = {
  PACKAGE: 'Gói',
  MATERIAL: 'Vật tư',
  BRAND: 'Hãng',
  KNOWLEDGE_DOC: 'Tài liệu',
};

/** Giai đoạn khách (mục 4.1) media hợp để gửi */
export const STAGE_LABELS: Record<string, string> = {
  LEAD: 'Khách mới',
  CONSULTING: 'Đang tư vấn',
  WON: 'Đã chốt',
  INSTALLATION: 'Đang lắp',
  INSTALLED: 'Đã lắp',
  AFTER_SALES: 'Sau bán hàng',
  REFERRAL: 'Giới thiệu khách',
};

export const CHANNEL_LABELS: Record<string, string> = {
  MESSENGER: 'Messenger',
  ZALO_OA: 'Zalo OA',
  TIKTOK_DM: 'TikTok',
};

export const AVAILABILITY_LABELS: Record<AvailabilityStatus, string> = {
  ACTIVE: 'Xem được',
  PRIVATE: 'Riêng tư',
  UNAVAILABLE: 'Không xem được',
  DELETED: 'Đã bị xoá',
  UNKNOWN: 'Chưa kiểm tra',
};

export const AVAILABILITY_COLORS: Record<AvailabilityStatus, string> = {
  ACTIVE: 'green',
  PRIVATE: 'orange',
  UNAVAILABLE: 'red',
  DELETED: 'red',
  UNKNOWN: 'default',
};

/** Nhật ký thao tác trên media */
export const ACTION_LABELS: Record<string, string> = {
  'media.import': 'Nhập combo',
  'media.create': 'Thêm media',
  'media.update': 'Sửa nội dung',
  'media.links': 'Sửa liên kết',
  'media.delete': 'Xoá',
  'media.restore': 'Khôi phục',
  'media.source.add': 'Thêm nguồn',
  'media.source.replace': 'Thay nguồn',
  'media.source.update': 'Sửa nguồn',
  'media.source.delete': 'Bỏ nguồn',
  'media.ai_draft': 'AI viết nháp',
};

/** Tên trường trong nhật ký sửa nội dung */
export const FIELD_LABELS: Record<string, string> = {
  title: 'tiêu đề',
  status: 'trạng thái',
  rights: 'quyền sử dụng',
  indexCode: 'mã danh mục',
  summary: 'tóm tắt',
  useWhen: 'dùng khi',
  avoidWhen: 'tránh khi',
  description: 'mô tả',
  transcript: 'lời thoại',
  topics: 'chủ đề',
  customerStages: 'giai đoạn khách',
  allowedChannels: 'kênh',
};

/** Ô chọn nhiều (chủ đề, giai đoạn, kênh) tìm theo tên hiển thị: Select mặc định lọc theo mã, gõ tiếng Việt không ra */
/** "Gói #98 Hệ Hy-Brid 5 kWp 1 pha (HN) · ảnh chính" */
export function linkText(l: MediaLinkModel): string {
  const target =
    l.label ?? `${TARGET_LABELS[l.targetType] ?? l.targetType} ${l.targetKey} (không còn)`;
  return `${target} · ${(ROLE_LABELS[l.role] ?? l.role).toLowerCase()}`;
}

export const options = (labels: Record<string, string>) =>
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
  { title: 'Chủ đề', key: 'topics', dataIndex: 'topics', width: 140, ellipsis: false },
  { title: 'Nguồn', key: 'source', dataIndex: 'sources', width: 160, ellipsis: false },
  {
    title: 'Liên kết',
    key: 'links',
    dataIndex: 'links',
    width: 220,
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

/** Tab Thông tin của form sửa; danh sách chủ đề nạp từ backend (updateSchema) */
export const infoFormSchema: FormSchema[] = [
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
      'Đã duyệt: AI được chọn gửi cho khách (khi có tính năng gửi media), cần Tóm tắt và Dùng khi ở tab Cho AI.',
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
    helpMessage: 'Chỉ gửi link: video Facebook / YouTube / TikTok, không tải về rồi đăng lại.',
    colProps: { span: 12 },
  },
  {
    field: 'topics',
    label: 'Chủ đề',
    component: 'Select',
    componentProps: {
      mode: 'multiple',
      optionFilterProp: 'label',
      options: [],
      placeholder: 'Chọn trong bộ chủ đề',
    },
    helpMessage: 'Sửa bộ chủ đề ở nút Chủ đề trên trang Thư viện media.',
    colProps: { span: 24 },
  },
  {
    field: 'customerStages',
    label: 'Giai đoạn khách',
    component: 'Select',
    componentProps: {
      mode: 'multiple',
      optionFilterProp: 'label',
      options: options(STAGE_LABELS),
      placeholder: 'Mọi giai đoạn',
    },
    helpMessage: 'Để trống là hợp với mọi giai đoạn.',
    colProps: { span: 24 },
  },
  {
    field: 'allowedChannels',
    label: 'Kênh được gửi',
    component: 'Select',
    componentProps: {
      mode: 'multiple',
      optionFilterProp: 'label',
      options: options(CHANNEL_LABELS),
    },
    colProps: { span: 24 },
  },
];

/**
 * Tab Cho AI. AI chỉ hiểu media qua Tóm tắt / Dùng khi / Tránh khi (mục 6.2), nên duyệt bắt buộc có hai ô đầu;
 * trạng thái nằm ở tab Thông tin nên truyền vào hàm kiểm tra.
 */
export function aiFormSchema(approved: () => boolean): FormSchema[] {
  const requiredWhenApproved = (message: string) => () =>
    approved() ? [{ required: true, message }] : [];
  return [
    {
      field: 'summary',
      label: 'Tóm tắt',
      component: 'InputTextArea',
      componentProps: {
        rows: 2,
        maxlength: 2000,
        placeholder: 'Media nói gì, ví dụ: ảnh hệ Hybrid 5 kWp, pin 5,12 kWh lắp trên mái tôn',
      },
      dynamicRules: requiredWhenApproved('Duyệt cần có tóm tắt'),
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
      dynamicRules: requiredWhenApproved('Duyệt cần có "dùng khi"'),
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
      componentProps: { rows: 4, maxlength: 10000 },
      colProps: { span: 24 },
    },
    {
      field: 'transcript',
      label: 'Lời thoại',
      component: 'InputTextArea',
      componentProps: { rows: 4, maxlength: 50000 },
      ifShow: ({ values }) => values.type === 'VIDEO',
      colProps: { span: 24 },
    },
    // Không hiện, chỉ để ô Lời thoại biết đây là video
    { field: 'type', label: '', component: 'Input', show: false },
  ];
}

/**
 * Form của hộp Thêm media: mọi trường của media, áp cho từng file / link trong lần thêm (2026-11-17).
 *
 * @param single đang thêm đúng một file / link (mới nhập được tiêu đề)
 * @param hasVideo có video trong lần thêm (hiện ô Lời thoại)
 */
export function addFormSchema(single: () => boolean, hasVideo: () => boolean): FormSchema[] {
  const info = Object.fromEntries(infoFormSchema.map((x) => [x.field, x]));
  const ai = Object.fromEntries(aiFormSchema(() => false).map((x) => [x.field, x]));
  const requiredToApprove =
    (message: string) =>
    ({ values }) =>
      values.status === 'APPROVED' && !values.aiDraft ? [{ required: true, message }] : [];
  return [
    {
      ...info.title,
      required: false,
      dynamicDisabled: () => !single(),
      componentProps: {
        maxlength: 300,
        placeholder: 'Để trống thì lấy theo tên file / tiêu đề video',
      },
      helpMessage:
        'Chỉ nhập được khi thêm đúng 1 file hoặc 1 link; thêm nhiều thì mỗi media lấy theo tên file.',
    },
    {
      ...info.status,
      defaultValue: 'DRAFT',
      dynamicDisabled: ({ values }) => !!values.aiDraft,
      helpMessage: [
        'Nháp: AI chưa dùng. Đã duyệt: cần Tóm tắt và Dùng khi.',
        'Bật AI viết nháp thì media để ở Nháp, xem lại rồi mới duyệt.',
      ],
    },
    {
      ...info.rights,
      required: false,
      componentProps: {
        ...info.rights.componentProps,
        allowClear: true,
        placeholder: 'Theo loại nguồn',
      },
      helpMessage:
        'Để trống: file tải lên và ảnh Drive là Của công ty, link video là Chỉ gửi link.',
    },
    info.topics,
    info.customerStages,
    { ...info.allowedChannels, defaultValue: ['MESSENGER'] },
    { field: 'dividerAi', label: 'Cho AI', component: 'Divider', colProps: { span: 24 } },
    {
      field: 'aiDraft',
      label: 'AI viết nháp',
      component: 'Switch',
      defaultValue: false,
      helpMessage:
        'Sau khi thêm, Claude xem ảnh (video: ảnh thu nhỏ và tiêu đề) rồi điền các ô dưới còn để trống. Mỗi file tốn một lượt gọi Claude.',
      colProps: { span: 24 },
    },
    { ...ai.summary, dynamicRules: requiredToApprove('Duyệt cần có tóm tắt') },
    { ...ai.useWhen, dynamicRules: requiredToApprove('Duyệt cần có "dùng khi"') },
    ai.avoidWhen,
    ai.description,
    { ...ai.transcript, ifShow: () => hasVideo() },
  ];
}
