import type { FormSchema } from '@/components/Form';
import { TIMELINE_LABELS, options } from '../shared/labels';

/**
 * Form của từng rule. Rule lưu tỷ lệ dạng số thập phân (0,3); form hiện phần trăm (30%) cho dễ đọc, đổi qua lại ở
 * toForm / toValue. Tên trường YAML giữ tiếng Anh như backend (RuleCodec).
 */
export interface RuleFormDef {
  name: string;
  title: string;
  description: string;
  schemas: FormSchema[];
  /** Tên hiển thị cho đường dẫn YAML trong lỗi kiểm tra; đường dẫn trùng tên ô form thì lấy nhãn của ô */
  paths?: Record<string, string>;
  toForm: (v: Recordable) => Recordable;
  toValue: (f: Recordable, original: Recordable) => Recordable;
}

const pct = (v?: number) => (v === undefined || v === null ? undefined : Math.round(v * 1000) / 10);
const ratio = (v?: number) =>
  v === undefined || v === null ? undefined : Math.round(v * 10) / 1000;
const num = (props: Recordable = {}) => ({ style: { width: '100%' }, ...props });

export const PRICING: RuleFormDef = {
  name: 'bao-gia',
  title: 'Báo giá',
  description:
    'Thông số công cụ chọn gói và báo giá dùng khi khách hỏi giá (giá từng gói vẫn lấy từ slm_v2). Đổi xong thử ở trang Catalog & báo giá → Thử báo giá sau khi phát hành.',
  schemas: [
    { field: 'd1', label: 'Quy đổi hoá đơn', component: 'Divider', colProps: { span: 24 } },
    {
      field: 'price_per_kwh',
      label: 'Giá điện quy đổi',
      component: 'InputNumber',
      required: true,
      componentProps: num({ min: 500, max: 20000, step: 100, addonAfter: 'đ/kWh' }),
      helpMessage:
        'Hoá đơn tiền điện chia giá này ra số kWh dùng mỗi tháng (giá phẳng, không theo bậc thang EVN).',
    },
    { field: 'd2', label: 'Chọn gói', component: 'Divider', colProps: { span: 24 } },
    {
      field: 'coverage',
      label: 'Đề xuất đầu: hệ làm ra',
      component: 'InputNumber',
      required: true,
      componentProps: num({ min: 30, max: 150, step: 5, addonAfter: '% điện nhà dùng' }),
      helpMessage:
        'Công suất cần = điện nhà dùng × con số này ÷ sản lượng 1 kWp ở tỉnh của khách (PV out). Chọn gói có công suất gần nhất.',
    },
    {
      field: 'cheaper_coverage',
      label: 'Khách chê đắt: hệ làm ra',
      component: 'InputNumber',
      required: true,
      componentProps: num({ min: 30, max: 150, step: 5, addonAfter: '% điện nhà dùng' }),
      helpMessage:
        'Khách chê đắt hoặc nêu ngân sách thấp: tính lại với con số này, chọn gói gần nhất trong các gói rẻ hơn gói đề xuất đầu. Phải nhỏ hơn mức đề xuất đầu.',
    },
    {
      field: 'battery_NGAY_NHIEU',
      label: 'Pin: khách dùng ngày nhiều',
      component: 'InputNumber',
      required: true,
      componentProps: num({ min: 0, max: 20, step: 0.5, addonAfter: 'kWh mỗi triệu tiền điện' }),
      helpMessage:
        'Pin cần = tiền điện (triệu) × con số này, chọn gói có pin gần nhất. 0 là không cần pin: báo hệ hòa lưới kèm Hybrid pin nhỏ. Ba con số phải tăng dần.',
    },
    {
      field: 'battery_CAN_BANG',
      label: 'Pin: khách dùng cân bằng',
      component: 'InputNumber',
      required: true,
      componentProps: num({ min: 0, max: 20, step: 0.5, addonAfter: 'kWh mỗi triệu tiền điện' }),
    },
    {
      field: 'battery_DEM_NHIEU',
      label: 'Pin: khách dùng đêm nhiều',
      component: 'InputNumber',
      required: true,
      componentProps: num({ min: 0, max: 20, step: 0.5, addonAfter: 'kWh mỗi triệu tiền điện' }),
    },
    {
      field: 'roof_area_per_kwp',
      label: 'Diện tích mái cho 1 kWp',
      component: 'InputNumber',
      required: true,
      componentProps: num({ min: 1, max: 20, step: 0.5, addonAfter: 'm²' }),
      helpMessage: 'Công suất tối đa theo mái = diện tích mái / con số này.',
    },
    {
      field: 'large_system_kwp',
      label: 'Hệ lớn từ',
      component: 'InputNumber',
      required: true,
      componentProps: num({ min: 1, max: 1000, step: 1, addonAfter: 'kWp' }),
      helpMessage: 'Nhu cầu lớn hơn mức này thì ghi chú để nhân viên khảo sát, báo giá riêng.',
    },
    { field: 'd4', label: 'Ước tính tiền tiết kiệm', component: 'Divider', colProps: { span: 24 } },
    {
      field: 'night_NGAY_NHIEU',
      label: 'Điện đêm: khách dùng ngày nhiều',
      component: 'InputNumber',
      required: true,
      componentProps: num({ min: 1, max: 99, step: 5, addonAfter: '%' }),
      helpMessage:
        'Phần điện khách dùng ban đêm, chỉ dùng để ước tính tiền tiết kiệm báo khách (không ảnh hưởng việc chọn gói). Ba tỷ lệ phải tăng dần.',
    },
    {
      field: 'night_CAN_BANG',
      label: 'Điện đêm: khách dùng cân bằng',
      component: 'InputNumber',
      required: true,
      componentProps: num({ min: 1, max: 99, step: 5, addonAfter: '%' }),
    },
    {
      field: 'night_DEM_NHIEU',
      label: 'Điện đêm: khách dùng đêm nhiều',
      component: 'InputNumber',
      required: true,
      componentProps: num({ min: 1, max: 99, step: 5, addonAfter: '%' }),
    },
    {
      field: 'battery_depth',
      label: 'Độ xả sâu của pin',
      component: 'InputNumber',
      required: true,
      componentProps: num({ min: 10, max: 100, step: 5, addonAfter: '%' }),
      helpMessage: 'Phần dung lượng pin dùng được, để ước tính điện pin trữ cho buổi tối.',
    },
    {
      field: 'd3',
      label: 'Link và lời nhắn kèm báo giá',
      component: 'Divider',
      colProps: { span: 24 },
    },
    {
      field: 'domain_BAC',
      label: 'Website miền Bắc',
      component: 'Input',
      required: true,
      componentProps: { placeholder: 'slmsolar.vn' },
      helpMessage: 'Link gói gửi khách: https://<tên miền>/products/<mã gói>. Chỉ ghi tên miền.',
    },
    {
      field: 'domain_NAM',
      label: 'Website miền Nam',
      component: 'Input',
      required: true,
      componentProps: { placeholder: 'slmsolar.com.vn' },
    },
    {
      field: 'quote_conditions',
      label: 'Điều kiện báo giá',
      component: 'InputTextArea',
      required: true,
      componentProps: { rows: 3, maxlength: 1000 },
      colProps: { span: 24 },
      helpMessage: 'Câu đi kèm mọi lần báo giá.',
    },
    {
      field: 'flat_roof_note',
      label: 'Ghi chú mái bằng',
      component: 'InputTextArea',
      componentProps: { rows: 2, maxlength: 1000 },
      colProps: { span: 24 },
    },
  ],
  paths: {
    night_share: 'Điện đêm',
    'night_share.NGAY_NHIEU': 'Điện đêm: khách dùng ngày nhiều',
    'night_share.CAN_BANG': 'Điện đêm: khách dùng cân bằng',
    'night_share.DEM_NHIEU': 'Điện đêm: khách dùng đêm nhiều',
    battery_per_million: 'Pin mỗi triệu tiền điện',
    'battery_per_million.NGAY_NHIEU': 'Pin: khách dùng ngày nhiều',
    'battery_per_million.CAN_BANG': 'Pin: khách dùng cân bằng',
    'battery_per_million.DEM_NHIEU': 'Pin: khách dùng đêm nhiều',
    'product_link_domains.BAC': 'Website miền Bắc',
    'product_link_domains.NAM': 'Website miền Nam',
  },
  toForm: (v) => ({
    price_per_kwh: v.price_per_kwh,
    night_NGAY_NHIEU: pct(v.night_share?.NGAY_NHIEU),
    night_CAN_BANG: pct(v.night_share?.CAN_BANG),
    night_DEM_NHIEU: pct(v.night_share?.DEM_NHIEU),
    battery_depth: pct(v.battery_depth),
    // Bản phát hành trước 2026-11-12 chưa có ba trường dưới: hiện giá trị mặc định của backend
    coverage: pct(v.coverage ?? 1),
    cheaper_coverage: pct(v.cheaper_coverage ?? 0.8),
    battery_NGAY_NHIEU: v.battery_per_million?.NGAY_NHIEU ?? 0,
    battery_CAN_BANG: v.battery_per_million?.CAN_BANG ?? 2.5,
    battery_DEM_NHIEU: v.battery_per_million?.DEM_NHIEU ?? 4,
    roof_area_per_kwp: v.roof_area_per_kwp,
    large_system_kwp: v.large_system_kwp,
    domain_BAC: v.product_link_domains?.BAC,
    domain_NAM: v.product_link_domains?.NAM,
    quote_conditions: v.quote_conditions,
    flat_roof_note: v.flat_roof_note,
  }),
  toValue: (f) => ({
    price_per_kwh: f.price_per_kwh,
    night_share: {
      NGAY_NHIEU: ratio(f.night_NGAY_NHIEU),
      CAN_BANG: ratio(f.night_CAN_BANG),
      DEM_NHIEU: ratio(f.night_DEM_NHIEU),
    },
    battery_depth: ratio(f.battery_depth),
    coverage: ratio(f.coverage),
    cheaper_coverage: ratio(f.cheaper_coverage),
    battery_per_million: {
      NGAY_NHIEU: f.battery_NGAY_NHIEU,
      CAN_BANG: f.battery_CAN_BANG,
      DEM_NHIEU: f.battery_DEM_NHIEU,
    },
    roof_area_per_kwp: f.roof_area_per_kwp,
    large_system_kwp: f.large_system_kwp,
    product_link_domains: { BAC: f.domain_BAC, NAM: f.domain_NAM },
    quote_conditions: f.quote_conditions,
    flat_roof_note: f.flat_roof_note || null,
  }),
};

export const LEAD_SCORING: RuleFormDef = {
  name: 'phan-loai-khach',
  title: 'Phân loại khách',
  description:
    'Mức tiềm năng (mục 58.5): HOT khi đã cho số điện thoại và (dự định lắp sớm hoặc đã đủ thông tin tối thiểu); OFF_SEGMENT khi ngân sách thấp; WARM khi trả lời đủ số câu hỏi hoặc đã cho số; còn lại COLD.',
  schemas: [
    {
      field: 'soon_timelines',
      label: 'Thời điểm lắp coi là sớm',
      component: 'CheckboxGroup',
      componentProps: { options: options(TIMELINE_LABELS) },
      colProps: { span: 24 },
      helpMessage: 'Khách có số điện thoại và dự định lắp vào các thời điểm này thì là HOT.',
    },
    {
      field: 'warm_answered_questions',
      label: 'Thành WARM khi trả lời đủ',
      component: 'InputNumber',
      required: true,
      componentProps: num({ min: 1, max: 10, precision: 0, addonAfter: 'câu hỏi' }),
    },
    {
      field: 'low_budget_ratio',
      label: 'Ngân sách thấp khi dưới',
      component: 'InputNumber',
      required: true,
      componentProps: num({ min: 1, max: 100, step: 5, addonAfter: '% giá gói rẻ nhất đã báo' }),
    },
  ],
  toForm: (v) => ({
    soon_timelines: v.soon_timelines ?? [],
    warm_answered_questions: v.warm_answered_questions,
    low_budget_ratio: pct(v.low_budget_ratio),
  }),
  toValue: (f) => ({
    soon_timelines: f.soon_timelines ?? [],
    warm_answered_questions: f.warm_answered_questions,
    low_budget_ratio: ratio(f.low_budget_ratio),
  }),
};

export const REPLY_POLICY: RuleFormDef = {
  name: 'kiem-tra-truoc-khi-gui',
  title: 'Kiểm tra trước khi gửi',
  description:
    'Tin AI vi phạm một trong các luật dưới đây thì không gửi tự động mà lưu nháp chờ nhân viên duyệt (mục 33). Con số trong tin luôn phải có nguồn: kết quả công cụ, tài liệu, tin của khách.',
  schemas: [
    {
      field: 'max_length',
      label: 'Độ dài tối đa một tin',
      component: 'InputNumber',
      required: true,
      componentProps: num({ min: 100, max: 2000, step: 50, precision: 0, addonAfter: 'ký tự' }),
      helpMessage:
        'Messenger giới hạn 2000 ký tự; kênh có giới hạn thấp hơn thì lấy giới hạn của kênh.',
    },
    {
      field: 'one_question_only',
      label: 'Mỗi tin chỉ một câu hỏi',
      component: 'Switch',
      helpMessage: 'Đếm dấu "?": hơn một câu hỏi thì lưu nháp (mục 58.2).',
    },
    {
      field: 'number_tolerance',
      label: 'Sai lệch cho phép khi đối chiếu số',
      component: 'InputNumber',
      required: true,
      componentProps: num({ min: 0, max: 10, step: 0.5, addonAfter: '%' }),
      helpMessage: 'Ví dụ 1%: 90.800.000 đ được viết "khoảng 91 triệu".',
    },
    {
      field: 'extra_allowed_links',
      label: 'Link luôn được gửi',
      component: 'Select',
      componentProps: {
        mode: 'tags',
        placeholder: 'Ví dụ: slmsolar.vn/lien-he',
        tokenSeparators: [',', ' '],
      },
      colProps: { span: 24 },
      helpMessage:
        'Ngoài link trong kết quả công cụ báo giá, link trang đã gửi trước đó và trang chủ website theo miền. Chỉ nhận đuôi .vn .com .net .org.',
    },
  ],
  toForm: (v) => ({
    max_length: v.max_length,
    one_question_only: !!v.one_question_only,
    number_tolerance: pct(v.number_tolerance),
    extra_allowed_links: v.extra_allowed_links ?? [],
  }),
  toValue: (f) => ({
    max_length: f.max_length,
    one_question_only: !!f.one_question_only,
    number_tolerance: ratio(f.number_tolerance),
    extra_allowed_links: f.extra_allowed_links ?? [],
  }),
};

/** "đường_dẫn: lời báo" của backend → "Nhãn trên form: lời báo" */
export function humanizeRuleError(
  error: string,
  def: Pick<RuleFormDef, 'schemas' | 'paths'>,
): string {
  const at = error.indexOf(': ');
  if (at < 0) return error;
  const path = error.slice(0, at);
  const label =
    def.paths?.[path] ?? (def.schemas.find((s) => s.field === path)?.label as string | undefined);
  return label ? `${label}: ${error.slice(at + 2)}` : error;
}

export const REGIONS_FORM: FormSchema[] = [
  {
    field: 'nguon',
    label: 'Nguồn danh mục',
    component: 'InputTextArea',
    componentProps: { rows: 2 },
    colProps: { span: 24 },
  },
  { field: 'bac_ten', label: 'Tên miền Bắc', component: 'Input', required: true },
  {
    field: 'bac_co_so',
    label: 'Cơ sở giá miền Bắc',
    component: 'Input',
    required: true,
    helpMessage: 'Mã cơ sở trong slm_v2 (HN): bảng giá dùng cho tỉnh thuộc miền này.',
  },
  { field: 'nam_ten', label: 'Tên miền Nam', component: 'Input', required: true },
  {
    field: 'nam_co_so',
    label: 'Cơ sở giá miền Nam',
    component: 'Input',
    required: true,
    helpMessage: 'Mã cơ sở trong slm_v2 (HCM).',
  },
];

export const PROVINCE_FORM: FormSchema[] = [
  { field: 'ma', label: 'Mã tỉnh', component: 'Input', dynamicDisabled: true },
  { field: 'ten', label: 'Tên tỉnh', component: 'Input', required: true },
  {
    field: 'mien',
    label: 'Miền giá',
    component: 'Select',
    required: true,
    componentProps: {
      options: [
        { label: 'Miền Bắc (bảng giá Hà Nội)', value: 'BAC' },
        { label: 'Miền Nam (bảng giá TP.HCM)', value: 'NAM' },
      ],
    },
  },
  {
    field: 'tinh_cu',
    label: 'Tỉnh cũ đã sáp nhập',
    component: 'Select',
    componentProps: { mode: 'tags', tokenSeparators: [','] },
    helpMessage: 'Khách nói tên tỉnh cũ (trước 2025) vẫn nhận ra đúng tỉnh mới.',
  },
  {
    field: 'ten_khac',
    label: 'Tên gọi khác',
    component: 'Select',
    componentProps: { mode: 'tags', tokenSeparators: [','] },
    helpMessage: 'Tên viết tắt, tên thành phố lớn: "Sài Gòn", "HCM", "Vũng Tàu"...',
  },
];
