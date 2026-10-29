import type { BasicColumn, FormSchema } from '@/components/Table';
import { h } from 'vue';
import { Tag } from 'ant-design-vue';
import type { SystemType } from '@/api/catalog/model/catalogModel';
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
  { label: 'Bị chặn', value: 'BLOCKED' },
  { label: 'Ngừng bán', value: 'INACTIVE' },
  { label: 'Có cảnh báo dữ liệu', value: 'WARNING' },
];

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
        !record.active
          ? h(Tag, {}, () => 'Ngừng bán')
          : record.recommendable
            ? h(Tag, { color: 'green' }, () => 'Được gợi ý')
            : h(Tag, { color: 'red' }, () => 'Bị chặn'),
      ];
      if (record.bestSeller) tags.push(h(Tag, { color: 'gold' }, () => 'Bán chạy'));
      return h('span', {}, tags);
    },
  },
  // Nhãn cảnh báo lấy từ API nên vẽ ở slot bodyCell của trang
  {
    title: 'Cảnh báo dữ liệu',
    dataIndex: 'warnings',
    key: 'warnings',
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
