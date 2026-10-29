import type { BasicColumn, FormSchema } from '@/components/Table';
import { pvoutListApi } from '@/api/catalog';
import { decimal } from '../labels';

export const columns: BasicColumn[] = [
  { title: 'Tỉnh', dataIndex: 'name', width: 140, fixed: 'left', align: 'left' },
  { title: 'Miền', dataIndex: 'region', width: 100 },
  ...Array.from(
    { length: 12 },
    // Mỗi cột một dataIndex riêng: bảng của vben nhận diện cột theo dataIndex,
    // 12 cột chung 'monthly' sẽ bị xáo thứ tự
    (_, i): BasicColumn => ({
      title: 'T' + (i + 1),
      dataIndex: 'm' + (i + 1),
      width: 64,
      customRender: ({ record }) => decimal(record.monthly[i], 1),
    }),
  ),
  {
    title: 'Cả năm',
    dataIndex: 'annual',
    width: 90,
    customRender: ({ record }) => decimal(record.annual, 0),
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    field: 'year',
    label: 'Năm',
    component: 'ApiSelect',
    componentProps: {
      api: pvoutListApi,
      labelField: 'year',
      valueField: 'year',
      // Nạp bảng năm mới xong thì danh sách năm phải có năm đó
      alwaysLoad: true,
      placeholder: 'Năm mới nhất',
    },
    colProps: { span: 6 },
  },
];

/** Tháng 12 thường chuẩn bị bảng cho năm sau, còn lại là năm hiện tại */
function defaultImportYear() {
  const now = new Date();
  return now.getMonth() === 11 ? now.getFullYear() + 1 : now.getFullYear();
}

export const importFormSchema: FormSchema[] = [
  {
    field: 'year',
    label: 'Năm áp dụng',
    component: 'InputNumber',
    required: true,
    defaultValue: defaultImportYear(),
    componentProps: { min: 2020, max: 2100, style: { width: '100%' } },
    colProps: { span: 8 },
  },
  {
    field: 'source',
    label: 'Văn bản',
    component: 'Input',
    required: true,
    componentProps: { placeholder: 'EVN, công văn số .../EVN-KDMBĐ ngày ...' },
    colProps: { span: 16 },
  },
  {
    field: 'note',
    label: 'Ghi chú',
    component: 'Input',
    colProps: { span: 24 },
  },
  {
    field: 'raw',
    label: 'Bảng PV out',
    component: 'InputTextArea',
    required: true,
    helpMessage: 'Mỗi dòng một tỉnh: tên tỉnh rồi 12 số từ tháng 1 đến tháng 12',
    componentProps: {
      rows: 10,
      placeholder: 'Tuyên Quang|49 37 52 68 108 109 120 112 107 93 80 63',
    },
    colProps: { span: 24 },
  },
];

export const previewColumns = [
  { title: 'Tỉnh', dataIndex: 'name', width: 140 },
  {
    title: 'Cả năm',
    dataIndex: 'annual',
    width: 90,
    customRender: ({ record }: { record: Recordable }) => decimal(record.annual, 0),
  },
  {
    title: 'Năm trước',
    dataIndex: 'previousAnnual',
    width: 90,
    customRender: ({ record }: { record: Recordable }) => decimal(record.previousAnnual, 0),
  },
  {
    title: 'Chênh',
    dataIndex: 'changePercent',
    width: 80,
    customRender: ({ record }: { record: Recordable }) =>
      record.changePercent == null
        ? ''
        : (record.changePercent > 0 ? '+' : '') + decimal(record.changePercent, 1) + '%',
  },
  {
    title: '12 tháng',
    key: 'monthly',
    customRender: ({ record }: { record: Recordable }) =>
      (record.monthly as number[]).map((v) => decimal(v, 1)).join(' · '),
  },
];

export type PvoutParseResult = {
  rows: { province: string; monthly: number[] }[];
  errors: string[];
};

/** "Tên tỉnh|12 số" hoặc "Tên tỉnh  12 số": tên là phần chữ đầu dòng, sau đó là các số */
export function parsePvoutText(raw: string): PvoutParseResult {
  const rows: PvoutParseResult['rows'] = [];
  const errors: string[] = [];
  raw
    .split(/\r?\n/)
    .map((l) => l.trim())
    .filter((l) => l)
    .forEach((line, i) => {
      const m = line.match(/^([^\d|\t;]+?)[\s|\t;]+([\d\s.,|\t;]+)$/);
      if (!m) {
        errors.push(`Dòng ${i + 1}: không đọc được "${line.slice(0, 40)}"`);
        return;
      }
      const monthly = m[2]
        .split(/[\s|\t;]+/)
        .filter((v) => v)
        .map((v) => Number(v.replace(',', '.')));
      if (monthly.length !== 12 || monthly.some((v) => Number.isNaN(v))) {
        errors.push(`Dòng ${i + 1} (${m[1].trim()}): cần đúng 12 số`);
        return;
      }
      rows.push({ province: m[1].trim(), monthly });
    });
  return { rows, errors };
}
