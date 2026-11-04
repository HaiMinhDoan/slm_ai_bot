import type { BasicColumn, FormSchema } from '@/components/Table';
import { catalogMaterialGroupsApi } from '@/api/catalog';
import { formatToDateTime } from '@/utils/dateUtil';
import { BRANCH_LABELS, PRICE_BRANCHES, vnd } from '../labels';

export const columns: BasicColumn[] = [
  { title: 'Ảnh', dataIndex: 'imageFileId', width: 70 },
  { title: 'Vật tư', dataIndex: 'name', align: 'left', ellipsis: false, width: 320 },
  { title: 'Nhóm', dataIndex: 'groupName', width: 130 },
  { title: 'Hãng', dataIndex: 'brand', width: 120 },
  ...PRICE_BRANCHES.map(
    (b): BasicColumn => ({
      title: `Giá bán ${BRANCH_LABELS[b] ?? b}`,
      dataIndex: `price_${b}`,
      width: 150,
      align: 'right',
      customRender: ({ record }) => vnd(record.salePrices?.[b]),
    }),
  ),
  {
    title: 'Đổi giá gần nhất',
    dataIndex: 'priceFrom',
    width: 150,
    customRender: ({ record }) => (record.priceFrom ? formatToDateTime(record.priceFrom) : ''),
  },
  { title: 'Số gói dùng', dataIndex: 'packageCount', width: 100 },
  { title: 'Bảo hành', dataIndex: 'warrantyMonths', width: 100 },
  { title: 'Trạng thái', dataIndex: 'active', width: 110 },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'keyword',
    label: 'Tìm kiếm',
    component: 'Input',
    colProps: { span: 6 },
    componentProps: { placeholder: 'Tên hoặc mã vật tư' },
  },
  {
    field: 'groupCode',
    label: 'Nhóm',
    component: 'ApiSelect',
    colProps: { span: 6 },
    componentProps: {
      api: catalogMaterialGroupsApi,
      labelField: 'name',
      valueField: 'code',
      allowClear: true,
      placeholder: 'Tất cả',
    },
  },
  {
    field: 'usage',
    label: 'Trong gói',
    component: 'Select',
    colProps: { span: 6 },
    componentProps: {
      allowClear: true,
      placeholder: 'Tất cả',
      options: [
        { label: 'Có trong gói đang bán', value: 'USED' },
        { label: 'Không thuộc gói nào', value: 'UNUSED' },
      ],
    },
  },
  {
    field: 'status',
    label: 'Trạng thái',
    component: 'Select',
    colProps: { span: 6 },
    defaultValue: 'ACTIVE',
    componentProps: {
      options: [
        { label: 'Đang dùng', value: 'ACTIVE' },
        { label: 'Ngừng ở slm_v2', value: 'INACTIVE' },
        { label: 'Tất cả', value: 'ALL' },
      ],
    },
  },
];
