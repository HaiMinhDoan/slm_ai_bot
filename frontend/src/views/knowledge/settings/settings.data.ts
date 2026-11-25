import type { FormSchema } from '@/components/Form';
import { DAY_LABELS, EFFORT_LABELS, INTENT_LABELS, MODEL_LABELS, options } from '../shared/labels';

const num = (props: Recordable = {}) => ({ style: { width: '100%' }, ...props });

/** Khoá snake_case của backend (cai_dat_ai) ứng với trường form */
export const SNAKE: Record<string, string> = {
  autoIntents: 'auto_intents',
  handoffIntents: 'handoff_intents',
  businessStart: 'business_start',
  businessEnd: 'business_end',
  businessDays: 'business_days',
  outOfHoursAck: 'out_of_hours_ack',
  handoffAck: 'handoff_ack',
  ackCooldownHours: 'ack_cooldown_hours',
  personaName: 'persona_name',
  companyName: 'company_name',
  typingIndicator: 'typing_indicator',
  typingCharsPerSecond: 'typing_chars_per_second',
  typingMaxSeconds: 'typing_max_seconds',
  debounceSeconds: 'debounce_seconds',
  autoHandbackHours: 'auto_handback_hours',
  model: 'model',
  effort: 'effort',
  assistantModel: 'assistant_model',
  maxAttempts: 'max_attempts',
  retryBackoffSeconds: 'retry_backoff_seconds',
  followUpEnabled: 'follow_up_enabled',
  followUpIntervalHours: 'follow_up_interval_hours',
  followUpMaxCount: 'follow_up_max_count',
  followUpAutoSend: 'follow_up_auto_send',
  followUpStart: 'follow_up_start',
  followUpEnd: 'follow_up_end',
  followUpNotifyStaff: 'follow_up_notify_staff',
};

export const settingsSchema: FormSchema[] = [
  { field: 'd1', label: 'Gửi tự động', component: 'Divider', colProps: { span: 24 } },
  {
    field: 'autoIntents',
    label: 'Loại câu hỏi được gửi tự động',
    component: 'Select',
    componentProps: { mode: 'multiple', options: options(INTENT_LABELS) },
    colProps: { span: 24 },
  },
  {
    field: 'handoffIntents',
    label: 'Loại câu hỏi luôn chuyển nhân viên',
    component: 'Select',
    componentProps: { mode: 'multiple', options: options(INTENT_LABELS) },
    colProps: { span: 24 },
  },
  {
    field: 'd2',
    label: 'Giờ làm việc của nhân viên',
    component: 'Divider',
    colProps: { span: 24 },
  },
  {
    field: 'businessStart',
    label: 'Bắt đầu',
    component: 'TimePicker',
    required: true,
    componentProps: { format: 'HH:mm', valueFormat: 'HH:mm', style: { width: '100%' } },
  },
  {
    field: 'businessEnd',
    label: 'Kết thúc',
    component: 'TimePicker',
    required: true,
    componentProps: { format: 'HH:mm', valueFormat: 'HH:mm', style: { width: '100%' } },
  },
  {
    field: 'businessDays',
    label: 'Ngày làm việc',
    component: 'CheckboxGroup',
    componentProps: { options: options(DAY_LABELS) },
    colProps: { span: 24 },
  },
  {
    field: 'd3',
    label: 'Câu báo nhận (hệ thống gửi thay AI)',
    component: 'Divider',
    colProps: { span: 24 },
  },
  {
    field: 'outOfHoursAck',
    label: 'Ngoài giờ làm việc',
    component: 'InputTextArea',
    componentProps: { rows: 2, maxlength: 500, placeholder: 'Để trống thì không gửi' },
    colProps: { span: 24 },
  },
  {
    field: 'handoffAck',
    label: 'Khi chuyển nhân viên (trong giờ)',
    component: 'InputTextArea',
    componentProps: { rows: 2, maxlength: 500, placeholder: 'Để trống thì không gửi' },
    colProps: { span: 24 },
  },
  {
    field: 'ackCooldownHours',
    label: 'Không gửi lại câu báo nhận trong',
    component: 'InputNumber',
    componentProps: num({ min: 0, max: 168, addonAfter: 'giờ' }),
  },
  { field: 'd4', label: 'Tư vấn viên', component: 'Divider', colProps: { span: 24 } },
  {
    field: 'personaName',
    label: 'Tên tư vấn viên AI',
    component: 'Input',
    required: true,
    componentProps: { maxlength: 50 },
  },
  {
    field: 'companyName',
    label: 'Tên công ty',
    component: 'Input',
    required: true,
    componentProps: { maxlength: 100 },
  },
  { field: 'd5', label: 'Hội thoại', component: 'Divider', colProps: { span: 24 } },
  {
    field: 'debounceSeconds',
    label: 'Chờ gộp tin của khách',
    component: 'InputNumber',
    componentProps: num({ min: 0, max: 120, addonAfter: 'giây' }),
  },
  {
    field: 'autoHandbackHours',
    label: 'Tự trả hội thoại lại cho AI sau',
    component: 'InputNumber',
    componentProps: num({ min: 0, max: 720, addonAfter: 'giờ (0 = không tự trả)' }),
  },
  {
    field: 'typingIndicator',
    label: 'Hiện "đang soạn tin" khi chờ AI',
    component: 'Switch',
  },
  {
    field: 'typingCharsPerSecond',
    label: 'Tốc độ gõ giả lập',
    helpMessage:
      'Câu trả lời dài được tách thành nhiều tin; trước mỗi tin chờ số ký tự chia cho tốc độ này',
    component: 'InputNumber',
    componentProps: num({ min: 1, max: 30, step: 0.5, addonAfter: 'ký tự / giây' }),
  },
  {
    field: 'typingMaxSeconds',
    label: 'Chờ tối đa trước mỗi tin',
    component: 'InputNumber',
    componentProps: num({ min: 0, max: 120, addonAfter: 'giây' }),
  },
  {
    field: 'd7',
    label: 'Nhắc khách khi khách im lặng',
    component: 'Divider',
    colProps: { span: 24 },
  },
  {
    field: 'followUpEnabled',
    label: 'Bật nhắc khách',
    helpMessage:
      'Khách chưa trả lời tin của bot thì bot nhắc kèm video công trình / ảnh báo giá chưa gửi. Chỉ trong 24 giờ kể từ tin cuối của khách (luật Messenger); khách hoặc nhân viên nhắn thì dừng',
    component: 'Switch',
  },
  {
    field: 'followUpIntervalHours',
    label: 'Nhắc sau khi khách im lặng',
    helpMessage: 'Tính từ tin cuối của bot, kể cả tin nhắc trước: nhắc lặp lại theo khoảng này',
    component: 'InputNumber',
    componentProps: num({ min: 1, max: 12, step: 0.5, addonAfter: 'giờ' }),
  },
  {
    field: 'followUpMaxCount',
    label: 'Số lần nhắc tối đa trong 24 giờ',
    component: 'InputNumber',
    componentProps: num({ min: 0, max: 10, precision: 0, addonAfter: 'lần (0 = không giới hạn)' }),
  },
  {
    field: 'followUpAutoSend',
    label: 'Tự gửi tin nhắc',
    helpMessage: 'Tắt thì tin nhắc thành nháp, nhân viên xem rồi gửi trên trang Chat với khách',
    component: 'Switch',
  },
  {
    field: 'followUpStart',
    label: 'Được nhắc từ',
    component: 'TimePicker',
    required: true,
    componentProps: { format: 'HH:mm', valueFormat: 'HH:mm', style: { width: '100%' } },
  },
  {
    field: 'followUpEnd',
    label: 'Được nhắc đến',
    component: 'TimePicker',
    required: true,
    componentProps: { format: 'HH:mm', valueFormat: 'HH:mm', style: { width: '100%' } },
  },
  {
    field: 'followUpNotifyStaff',
    label: 'Báo nhân viên khi hết 24 giờ',
    helpMessage:
      'Khách vẫn im lặng khi hết 24 giờ (bot không nhắn được nữa) và đã có số điện thoại, gói đề xuất hoặc mức tiềm năng WARM / HOT',
    component: 'Switch',
  },
  { field: 'd6', label: 'Model AI', component: 'Divider', colProps: { span: 24 } },
  {
    field: 'model',
    label: 'Model trả lời khách',
    component: 'Select',
    required: true,
    componentProps: { options: options(MODEL_LABELS) },
  },
  {
    field: 'effort',
    label: 'Mức suy nghĩ',
    component: 'Select',
    required: true,
    componentProps: { options: options(EFFORT_LABELS) },
  },
  {
    field: 'assistantModel',
    label: 'Model AI hỗ trợ soạn',
    component: 'Select',
    required: true,
    componentProps: { options: options(MODEL_LABELS) },
  },
  {
    field: 'maxAttempts',
    label: 'Gọi AI lỗi quá số lần thì chuyển nhân viên',
    component: 'InputNumber',
    componentProps: num({ min: 1, max: 10, precision: 0, addonAfter: 'lần' }),
  },
  {
    field: 'retryBackoffSeconds',
    label: 'Chờ trước lần thử lại đầu',
    component: 'InputNumber',
    componentProps: num({ min: 1, max: 600, addonAfter: 'giây (sau gấp đôi)' }),
  },
];

/** Giá trị mặc định đọc được: danh sách, giờ, bật / tắt */
export function defaultText(field: string, v: any): string {
  if (Array.isArray(v)) {
    const labels = field === 'businessDays' ? DAY_LABELS : INTENT_LABELS;
    return v.length ? v.map((x) => labels[x] ?? x).join(', ') : '(trống)';
  }
  if (typeof v === 'boolean') return v ? 'bật' : 'tắt';
  if (field === 'model' || field === 'assistantModel') return MODEL_LABELS[v] ?? v;
  if (field === 'effort') return EFFORT_LABELS[v] ?? v;
  return v === null || v === undefined || v === '' ? '(trống)' : String(v);
}
