import type { BasicColumn, FormSchema } from '@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';
import type { PackageSourceModel, SystemType } from '@/api/catalog/model/catalogModel';
import {
  BRANCH_LABELS,
  PHASE_OPTIONS,
  SYSTEM_TYPE_LABELS,
  decimal,
  millions,
  options,
} from '../labels';

export const STATUS_OPTIONS = [
  { label: 'Được gợi ý', value: 'RECOMMENDABLE' },
  { label: 'Bị chặn vì dữ liệu', value: 'BLOCKED' },
  { label: 'Đã tắt trong bot', value: 'DISABLED' },
  { label: 'Ngừng bán ở slm_v2', value: 'INACTIVE' },
  { label: 'Đã sửa', value: 'EDITED' },
  { label: 'Có cảnh báo dữ liệu', value: 'WARNING' },
  { label: 'Đã xoá', value: 'DELETED' },
];

/** Tên trường trong CatalogPackageModel.edited → nhãn hiển thị */
export const EDIT_FIELD_LABELS: Record<string, string> = {
  name: 'tên',
  systemType: 'loại hệ',
  phases: 'số pha',
  kwp: 'công suất',
  panelCount: 'số tấm pin',
  panelWp: 'công suất tấm',
  storageKwh: 'pin lưu trữ',
  totalPrice: 'giá',
  bestSeller: 'bán chạy',
};

const money = (v?: number) => (v ? Number(v).toLocaleString('vi-VN') : '');

/** Số liệu slm_v2 của một trường, hiện cạnh ô nhập trong form sửa */
export function sourceText(field: string, source: PackageSourceModel): string {
  const v = source[field as keyof PackageSourceModel];
  if (v == null || v === '') return 'trống';
  switch (field) {
    case 'systemType':
      return SYSTEM_TYPE_LABELS[v as SystemType];
    case 'phases':
      return v + ' pha';
    case 'totalPrice':
      return money(v as number) + ' đồng';
    case 'bestSeller':
      return v ? 'có' : 'không';
    case 'name':
      return String(v);
    default:
      return decimal(v as number);
  }
}

export const columns: BasicColumn[] = [
  { title: 'Mã', dataIndex: 'id', width: 70, fixed: 'left' },
  { title: 'Cơ sở', dataIndex: 'branchCode', width: 70 },
  {
    title: 'Tên gói',
    dataIndex: 'name',
    width: 320,
    align: 'left',
    ellipsis: false,
    customRender: ({ record }) =>
      h('div', {}, [
        h('div', {}, record.name),
        h('div', { style: 'color:#888;font-size:12px' }, record.familyName ?? ''),
      ]),
  },
  {
    title: 'Loại',
    dataIndex: 'systemType',
    width: 140,
    customRender: ({ record }) =>
      record.systemType
        ? SYSTEM_TYPE_LABELS[record.systemType as SystemType]
        : h(Tag, { color: 'red' }, () => 'Chưa rõ'),
  },
  { title: 'Pha', dataIndex: 'phases', width: 60 },
  {
    title: 'kWp',
    dataIndex: 'kwp',
    width: 80,
    customRender: ({ record }) => decimal(record.kwp),
  },
  {
    title: 'Tấm pin',
    dataIndex: 'panelCount',
    width: 120,
    customRender: ({ record }) =>
      record.panelCount ? `${record.panelCount} × ${decimal(record.panelWp, 0)} Wp` : '',
  },
  {
    title: 'Pin (kWh)',
    dataIndex: 'storageKwh',
    width: 90,
    customRender: ({ record }) => (record.storageKwh ? decimal(record.storageKwh) : ''),
  },
  {
    title: 'Giá',
    dataIndex: 'totalPrice',
    width: 100,
    customRender: ({ record }) => millions(record.totalPrice),
  },
  {
    title: 'Trạng thái',
    dataIndex: 'recommendable',
    width: 180,
    customRender: ({ record }) => {
      const tags = [
        record.deleted
          ? h(Tag, {}, () => 'Đã xoá')
          : !record.active
            ? h(Tag, {}, () => 'Ngừng bán')
            : record.disabled
              ? h(Tag, { color: 'orange' }, () => 'Đã tắt')
              : record.recommendable
                ? h(Tag, { color: 'green' }, () => 'Được gợi ý')
                : h(Tag, { color: 'red' }, () => 'Bị chặn'),
      ];
      if (record.edited?.length) {
        const fields = (record.edited as string[]).map((f) => EDIT_FIELD_LABELS[f] ?? f).join(', ');
        tags.push(h(Tag, { color: 'blue', title: 'Đã sửa: ' + fields }, () => 'Đã sửa'));
      }
      if (record.bestSeller) tags.push(h(Tag, { color: 'gold' }, () => 'Bán chạy'));
      return h('span', {}, tags);
    },
  },
  // Nhãn cảnh báo lấy từ API nên vẽ ở slot bodyCell của trang
  {
    title: 'Cảnh báo dữ liệu',
    dataIndex: 'warnings',
    key: 'warnings',
    width: 220,
    align: 'left',
    ellipsis: false,
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'keyword',
    label: 'Tìm kiếm',
    component: 'Input',
    componentProps: { placeholder: 'Tên gói hoặc mã' },
    colProps: { span: 6 },
  },
  {
    field: 'branchCode',
    label: 'Cơ sở giá',
    component: 'Select',
    componentProps: { options: options(BRANCH_LABELS), allowClear: true, placeholder: 'Tất cả' },
    colProps: { span: 6 },
  },
  {
    field: 'systemType',
    label: 'Loại hệ',
    component: 'Select',
    componentProps: {
      options: options(SYSTEM_TYPE_LABELS),
      allowClear: true,
      placeholder: 'Tất cả',
    },
    colProps: { span: 6 },
  },
  {
    field: 'phases',
    label: 'Số pha',
    component: 'Select',
    componentProps: { options: PHASE_OPTIONS, allowClear: true, placeholder: 'Tất cả' },
    colProps: { span: 6 },
  },
  {
    field: 'status',
    label: 'Trạng thái',
    component: 'Select',
    componentProps: { options: STATUS_OPTIONS, allowClear: true, placeholder: 'Tất cả' },
    colProps: { span: 6 },
  },
];

/**
 * Form sửa gói. Giá trị điền sẵn là số liệu bot đang dùng; để trống hoặc nhập trùng slm_v2
 * thì bot dùng lại số liệu slm_v2 (helpMessage của từng ô ghi giá trị slm_v2).
 */
export const packageFormSchema: FormSchema[] = [
  {
    field: 'enabled',
    label: 'Bot dùng gói này',
    component: 'Switch',
    defaultValue: true,
    helpMessage: 'Tắt thì bot không gợi ý, không báo giá gói này. slm_v2 không đổi.',
    colProps: { span: 24 },
  },
  {
    field: 'name',
    label: 'Tên gói',
    component: 'Input',
    required: true,
    colProps: { span: 24 },
  },
  {
    field: 'systemType',
    label: 'Loại hệ',
    component: 'Select',
    componentProps: { options: options(SYSTEM_TYPE_LABELS), allowClear: true },
    colProps: { span: 12 },
  },
  {
    field: 'phases',
    label: 'Số pha',
    component: 'Select',
    componentProps: { options: PHASE_OPTIONS, allowClear: true },
    colProps: { span: 12 },
  },
  {
    field: 'kwp',
    label: 'Công suất (kWp)',
    component: 'InputNumber',
    // Dấu phẩy thập phân như cách đọc của người Việt (8,75 kWp). Bước 1: ô số hiện số chữ số thập phân
    // theo bước nhảy, bước 0,125 sẽ hiện 5,000 (dễ đọc thành năm nghìn)
    componentProps: { min: 0.1, step: 1, decimalSeparator: ',', style: { width: '100%' } },
    colProps: { span: 12 },
  },
  {
    field: 'storageKwh',
    label: 'Pin lưu trữ (kWh)',
    component: 'InputNumber',
    componentProps: { min: 0, step: 1, decimalSeparator: ',', style: { width: '100%' } },
    colProps: { span: 12 },
  },
  {
    field: 'panelCount',
    label: 'Số tấm pin',
    component: 'InputNumber',
    componentProps: { min: 1, precision: 0, style: { width: '100%' } },
    colProps: { span: 12 },
  },
  {
    field: 'panelWp',
    label: 'Công suất tấm (Wp)',
    component: 'InputNumber',
    componentProps: { min: 1, precision: 0, style: { width: '100%' } },
    colProps: { span: 12 },
  },
  {
    field: 'totalPrice',
    label: 'Giá trọn gói',
    component: 'InputNumber',
    componentProps: {
      min: 1,
      step: 100000,
      style: { width: '100%' },
      addonAfter: 'đồng',
      formatter: (v: any) => money(v),
      parser: (v: string) => Number(v.replace(/\D/g, '')),
    },
    colProps: { span: 12 },
  },
  {
    field: 'bestSeller',
    label: 'Bán chạy',
    component: 'Switch',
    colProps: { span: 12 },
  },
  {
    field: 'note',
    label: 'Ghi chú',
    component: 'InputTextArea',
    componentProps: { rows: 3, placeholder: 'Lý do sửa hoặc tắt, để người sau biết' },
    colProps: { span: 24 },
  },
];

/** Các ô có số liệu slm_v2 để đối chiếu */
export const SOURCE_FIELDS = [
  'name',
  'systemType',
  'phases',
  'kwp',
  'storageKwh',
  'panelCount',
  'panelWp',
  'totalPrice',
  'bestSeller',
];
