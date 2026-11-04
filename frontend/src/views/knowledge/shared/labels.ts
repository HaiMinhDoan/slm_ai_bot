import type { KnowledgeDocType, ReleaseStatus } from '@/api/knowledge/model/knowledgeModel';

/** Loại tài liệu (khớp KnowledgeDocType ở backend) */
export const DOC_TYPE_LABELS: Record<KnowledgeDocType, string> = {
  CORE: 'Cốt lõi',
  PLAYBOOK: 'Cách tư vấn',
  POLICY: 'Chính sách',
  FAQ: 'Hỏi đáp',
  EXAMPLE: 'Chat mẫu',
  PROMPT: 'Hướng dẫn hệ thống',
  RULE: 'Quy tắc',
  GUIDE: 'Hướng dẫn soạn',
  TEMPLATE: 'Mẫu tài liệu',
  CONTENT: 'Nội dung đăng bài',
};

export const DOC_TYPE_FOLDERS: Record<KnowledgeDocType, string> = {
  CORE: 'core',
  PLAYBOOK: 'playbooks',
  POLICY: 'policies',
  FAQ: 'faq',
  EXAMPLE: 'examples',
  PROMPT: 'prompts',
  RULE: 'rules',
  GUIDE: 'guide',
  TEMPLATE: 'guide/mau',
  CONTENT: 'content',
};

export const DOC_TYPE_COLORS: Partial<Record<KnowledgeDocType, string>> = {
  CORE: 'red',
  PLAYBOOK: 'blue',
  POLICY: 'purple',
  FAQ: 'cyan',
  EXAMPLE: 'green',
  PROMPT: 'magenta',
  RULE: 'orange',
};

/** Loại tạo mới được trên trang Tài liệu (rule và hướng dẫn hệ thống là bộ cố định) */
export const CREATABLE_TYPES: KnowledgeDocType[] = [
  'POLICY',
  'FAQ',
  'PLAYBOOK',
  'EXAMPLE',
  'CORE',
  'GUIDE',
  'TEMPLATE',
  'CONTENT',
];

export const RELEASE_STATUS: Record<ReleaseStatus, { label: string; color: string }> = {
  ACTIVE: { label: 'Đang chạy', color: 'green' },
  READY: { label: 'Chờ kích hoạt', color: 'blue' },
  SUPERSEDED: { label: 'Bản cũ', color: 'default' },
  FAILED: { label: 'Không đạt', color: 'red' },
  VALIDATING: { label: 'Đang kiểm tra', color: 'orange' },
};

export const CHECK_LEVEL: Record<string, { label: string; color: string }> = {
  ERROR: { label: 'Lỗi', color: 'red' },
  WARNING: { label: 'Cảnh báo', color: 'orange' },
  INFO: { label: 'Gợi ý', color: 'blue' },
  OK: { label: 'Đạt', color: 'green' },
};

export const CHANGE_ACTION: Record<string, { label: string; color: string }> = {
  ADDED: { label: 'Thêm mới', color: 'green' },
  UPDATED: { label: 'Sửa', color: 'blue' },
  REMOVED: { label: 'Bỏ (lưu trữ)', color: 'default' },
  CREATE: { label: 'Tạo mới', color: 'green' },
  UPDATE: { label: 'Sửa', color: 'blue' },
  RETIRE: { label: 'Ngừng dùng', color: 'default' },
};

export const INTENT_LABELS: Record<string, string> = {
  CHAO_HOI: 'Chào hỏi',
  TU_VAN_LAP_MOI: 'Tư vấn lắp mới',
  HOI_GIA: 'Hỏi giá',
  GIAI_THICH: 'Giải thích',
  CHINH_SACH: 'Chính sách',
  KY_THUAT_SU_CO: 'Kỹ thuật / sự cố',
  KHIEU_NAI: 'Khiếu nại',
  KHAC: 'Khác',
};

export const DAY_LABELS: Record<string, string> = {
  MONDAY: 'Thứ Hai',
  TUESDAY: 'Thứ Ba',
  WEDNESDAY: 'Thứ Tư',
  THURSDAY: 'Thứ Năm',
  FRIDAY: 'Thứ Sáu',
  SATURDAY: 'Thứ Bảy',
  SUNDAY: 'Chủ nhật',
};

export const MODEL_LABELS: Record<string, string> = {
  'claude-sonnet-5-5': 'Claude Sonnet 5.5',
  'claude-opus-5-5': 'Claude Opus 5.5 (viết tốt nhất, chậm và đắt hơn)',
  'claude-haiku-5-5': 'Claude Haiku 5.5 (nhanh, rẻ)',
};

export const EFFORT_LABELS: Record<string, string> = {
  low: 'Thấp: nhanh, rẻ',
  medium: 'Vừa',
  high: 'Cao',
  xhigh: 'Rất cao',
  max: 'Tối đa: chậm, đắt',
};

export const TIMELINE_LABELS: Record<string, string> = {
  TUAN_TOI: 'Tuần tới',
  THANG_TOI: 'Tháng tới',
  VAI_THANG: 'Vài tháng nữa',
  NAM_TOI: 'Năm tới',
  CHUA_RO: 'Chưa rõ',
};

export const options = (labels: Record<string, string>) =>
  Object.entries(labels).map(([value, label]) => ({ label, value }));
