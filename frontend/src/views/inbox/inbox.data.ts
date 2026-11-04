import type { BasicColumn, FormSchema } from '@/components/Table';
import type {
  ChannelCode,
  InboxView,
  LeadLevel,
  Ownership,
  SenderType,
} from '@/api/inbox/model/inboxModel';

export const CHANNEL_LABELS: Record<ChannelCode, string> = {
  MESSENGER: 'Messenger',
  FACEBOOK_COMMENT: 'Bình luận Facebook',
  ZALO_OA: 'Zalo OA',
  TIKTOK_DM: 'TikTok',
  TIKTOK_COMMENT: 'Bình luận TikTok',
  FACEBOOK_GROUP: 'Nhóm Facebook',
  ZALO_PERSONAL: 'Zalo cá nhân',
};

/** Tab luôn hiện; kênh khác chỉ hiện khi đã có hội thoại */
export const MAIN_CHANNELS: ChannelCode[] = [
  'MESSENGER',
  'FACEBOOK_COMMENT',
  'ZALO_OA',
  'TIKTOK_DM',
];

export const OWNERSHIP: Record<Ownership, { label: string; color: string }> = {
  AI_ACTIVE: { label: 'AI đang trả lời', color: 'blue' },
  HUMAN_OWNED: { label: 'Nhân viên phụ trách', color: 'orange' },
};

export const LEAD_LEVEL: Record<LeadLevel, { label: string; color: string }> = {
  HOT: { label: 'HOT', color: 'red' },
  WARM: { label: 'WARM', color: 'orange' },
  COLD: { label: 'COLD', color: 'blue' },
  OFF_SEGMENT: { label: 'Ngoài phân khúc', color: 'default' },
};

/** Trạng thái lead gửi Sales API (khach_tiem_nang.trang_thai) */
export const LEAD_STATUS: Record<string, string> = {
  NEW: 'chưa gửi Sales API',
  SENT: 'đã gửi Sales API',
  FAILED: 'gửi Sales API lỗi',
  DUPLICATE: 'trùng lead đã có',
};

export const SENDER_LABELS: Record<SenderType, string> = {
  CUSTOMER: 'Khách',
  AI: 'AI',
  STAFF: 'Nhân viên',
  SYSTEM: 'Hệ thống',
};

export const VIEW_OPTIONS: { label: string; value: InboxView }[] = [
  { label: 'Cần nhân viên (phụ trách hoặc có nháp)', value: 'NEEDS_STAFF' },
  { label: 'Nhân viên đang phụ trách', value: 'STAFF' },
  { label: 'AI đang trả lời', value: 'AI' },
  { label: 'Có nháp AI / tin lỗi chờ xử lý', value: 'DRAFT' },
];

/** Thông tin AI thu khi tư vấn (ConsultSlots) */
export const SLOT_LABELS: Record<string, string> = {
  khu_vuc: 'Tỉnh / thành phố',
  so_pha: 'Điện 1 pha / 3 pha',
  hoa_don_thang: 'Tiền điện mỗi tháng',
  ty_le_ngay_dem: 'Dùng điện ngày / đêm',
  loai_mai: 'Loại mái',
  dien_tich_mai: 'Diện tích mái (m²)',
  ten_khach: 'Tên khách',
  danh_xung: 'Cách gọi',
};

export const SLOT_VALUE_LABELS: Record<string, string> = {
  NGAY_NHIEU: 'Ban ngày nhiều',
  CAN_BANG: 'Cân bằng',
  DEM_NHIEU: 'Ban đêm nhiều',
  TON: 'Mái tôn',
  NGOI: 'Mái ngói',
  BANG: 'Mái bằng',
  ANH: 'Anh',
  CHI: 'Chị',
};

/** Giá trị tín hiệu phân loại khách (mục 58.3): timeline, research_level.value */
export const QUALIFICATION_VALUE_LABELS: Record<string, string> = {
  TUAN_TOI: 'Tuần tới',
  THANG_TOI: 'Tháng tới',
  VAI_THANG: 'Vài tháng nữa',
  NAM_TOI: 'Năm tới',
  CHUA_RO: 'Chưa rõ',
  CHUA: 'Chưa tìm hiểu',
  SO_BO: 'Tìm hiểu sơ bộ',
  KY: 'Tìm hiểu kỹ',
};

// Tin cuối hiện 2 dòng: tắt ellipsis một dòng mặc định của BasicTable
export const columns: BasicColumn[] = [
  { title: 'Khách', dataIndex: 'customerName', width: 180, align: 'left', ellipsis: false },
  { title: 'Kênh', dataIndex: 'channel', width: 120 },
  { title: 'Tin cuối', dataIndex: 'lastText', align: 'left', ellipsis: false, minWidth: 280 },
  { title: 'Phụ trách', dataIndex: 'ownership', width: 160 },
  { title: 'Tiềm năng', dataIndex: 'leadLevel', width: 110 },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'view',
    label: 'Trạng thái',
    component: 'Select',
    colProps: { span: 8 },
    componentProps: { options: VIEW_OPTIONS, allowClear: true, placeholder: 'Tất cả' },
  },
  {
    field: 'keyword',
    label: 'Tìm kiếm',
    component: 'Input',
    colProps: { span: 8 },
    componentProps: { placeholder: 'Tên khách hoặc số điện thoại' },
  },
];
