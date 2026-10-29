import type { FormSchema } from '@/components/Form';
import type { DescItem } from '@/components/Description';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';
import {
  DAY_NIGHT_LABELS,
  PHASE_OPTIONS,
  ROOF_LABELS,
  SYSTEM_TYPE_LABELS,
  options,
} from '../labels';

/** Tên trường trong kết quả tool (tiếng Việt không dấu) → nhãn hiển thị */
export const KEY_LABELS: Record<string, string> = {
  tinh: 'Tỉnh',
  tinh_cu: 'Tên cũ',
  khach_noi: 'Khách nói',
  mien: 'Miền',
  bang_gia: 'Bảng giá',
  hoa_don_thang: 'Hoá đơn',
  gia_dien_quy_doi: 'Giá điện quy đổi',
  dien_dung_ngay: 'Điện dùng mỗi ngày',
  dien_dung_thang: 'Điện dùng mỗi tháng',
  ty_le_dung_dem: 'Tỷ lệ dùng đêm',
  cong_suat_can: 'Công suất cần',
  pin_can_neu_dung_hybrid: 'Pin cần (Hybrid)',
  cong_suat_toi_da_theo_mai: 'Tối đa theo mái',
  pvout: 'PV out',
  khoang_gia: 'Khoảng giá',
  con_thieu: 'Còn thiếu',
  khu_vuc: 'tỉnh',
  so_pha: 'số pha',
  ty_le_ngay_dem: 'ngày / đêm',
  loai_mai: 'loại mái',
};

export const adviseFormSchema: FormSchema[] = [
  {
    field: 'province',
    label: 'Tỉnh / thành',
    component: 'Input',
    componentProps: { placeholder: 'Tên mới hoặc cũ, ví dụ: Bình Dương, Hà Nội', allowClear: true },
  },
  {
    field: 'monthlyBillVnd',
    label: 'Tiền điện / tháng',
    component: 'InputNumber',
    componentProps: {
      min: 0,
      step: 100000,
      style: { width: '100%' },
      addonAfter: 'đồng',
      formatter: (v: any) => (v ? Number(v).toLocaleString('vi-VN') : ''),
      parser: (v: string) => Number(v.replace(/\D/g, '')),
    },
  },
  {
    field: 'phases',
    label: 'Số pha',
    component: 'Select',
    componentProps: { options: PHASE_OPTIONS, allowClear: true },
  },
  {
    field: 'dayNight',
    label: 'Dùng điện',
    component: 'Select',
    componentProps: { options: options(DAY_NIGHT_LABELS), allowClear: true },
  },
  {
    field: 'roofType',
    label: 'Loại mái',
    component: 'Select',
    componentProps: { options: options(ROOF_LABELS), allowClear: true },
  },
  {
    field: 'roofAreaM2',
    label: 'Diện tích mái',
    component: 'InputNumber',
    componentProps: { min: 0, style: { width: '100%' }, addonAfter: 'm²' },
  },
  {
    field: 'systemType',
    label: 'Khách muốn',
    component: 'Select',
    componentProps: { options: options(SYSTEM_TYPE_LABELS), allowClear: true },
  },
  {
    field: 'backupNeeded',
    label: 'Dự phòng mất điện',
    component: 'Select',
    // Ant Design Vue không nhận option value kiểu boolean, đổi lại khi gửi
    componentProps: {
      options: [
        { label: 'Có', value: 'true' },
        { label: 'Không', value: 'false' },
      ],
      allowClear: true,
    },
  },
];

/** Gộp khu_vuc, uoc_tinh, khoang_gia, con_thieu của kết quả thành dữ liệu cho Description */
export function summaryOf(result: Recordable): { data: Recordable; schema: DescItem[] } {
  const data: Recordable = { ...(result.khu_vuc ?? {}), ...(result.uoc_tinh ?? {}) };
  if (result.khoang_gia) data.khoang_gia = `${result.khoang_gia.tu} đến ${result.khoang_gia.den}`;
  if (result.con_thieu?.length) data.con_thieu = result.con_thieu;
  const schema: DescItem[] = Object.keys(data).map((field) => ({
    field,
    label: KEY_LABELS[field] ?? field,
    render:
      field === 'con_thieu'
        ? (val: string[]) =>
            h(
              'span',
              {},
              val.map((c) => h(Tag, { key: c }, () => KEY_LABELS[c] ?? c)),
            )
        : undefined,
  }));
  return { data, schema };
}

export const itemColumns = [
  { title: 'Thành phần', dataIndex: 'ten' },
  { title: 'Hãng', dataIndex: 'hang', width: 120 },
  { title: 'Số lượng', dataIndex: 'so_luong', width: 100 },
  { title: 'Bảo hành', dataIndex: 'bao_hanh', width: 140 },
];
