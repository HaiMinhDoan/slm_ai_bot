import type { BasicColumn, FormSchema } from '@/components/Table';
import { Tag } from 'ant-design-vue';
import { h } from 'vue';

export const STATUS_OPTIONS = [
  { label: 'Đang hoạt động', value: 'ACTIVE' },
  { label: 'Tạm khóa', value: 'LOCKED' },
  { label: 'Vô hiệu hóa', value: 'DISABLED' },
];

export const STATUS_COLOR: Record<string, string> = {
  ACTIVE: 'success',
  LOCKED: 'error',
  DISABLED: 'default',
};

export const statusLabel = (status: string) =>
  STATUS_OPTIONS.find((o) => o.value === status)?.label ?? status;

export const columns: BasicColumn[] = [
  { title: 'Họ tên', dataIndex: 'fullName', width: 180 },
  { title: 'Email', dataIndex: 'email', width: 220 },
  {
    title: 'Vai trò',
    dataIndex: 'roleCodes',
    width: 260,
    customRender: ({ record }) =>
      h(
        'span',
        {},
        (record.roleCodes as string[]).map((c) => h(Tag, { key: c }, () => c)),
      ),
  },
  {
    title: 'Trạng thái',
    dataIndex: 'status',
    width: 130,
    customRender: ({ record }) =>
      h(Tag, { color: STATUS_COLOR[record.status] ?? 'default' }, () => statusLabel(record.status)),
  },
  {
    title: 'Đăng nhập gần nhất',
    dataIndex: 'lastLoginAt',
    width: 160,
    customRender: ({ record }) =>
      record.lastLoginAt ? new Date(record.lastLoginAt).toLocaleString('vi-VN') : '—',
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'keyword',
    label: 'Tìm kiếm',
    component: 'Input',
    componentProps: { placeholder: 'Tên hoặc email' },
    colProps: { span: 8 },
  },
  {
    field: 'status',
    label: 'Trạng thái',
    component: 'Select',
    componentProps: { options: STATUS_OPTIONS, allowClear: true, placeholder: 'Tất cả' },
    colProps: { span: 6 },
  },
];
