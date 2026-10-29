import type { DayNight, RoofType, SystemType } from '@/api/catalog/model/catalogModel';

export const SYSTEM_TYPE_LABELS: Record<SystemType, string> = {
  ON_GRID: 'On-Grid (hòa lưới)',
  HYBRID: 'Hybrid (có pin)',
};

export const DAY_NIGHT_LABELS: Record<DayNight, string> = {
  NGAY_NHIEU: 'Ban ngày nhiều',
  CAN_BANG: 'Cân bằng',
  DEM_NHIEU: 'Buổi tối nhiều',
};

export const ROOF_LABELS: Record<RoofType, string> = {
  TON: 'Mái tôn',
  NGOI: 'Mái ngói',
  BANG: 'Mái bằng',
};

export const BRANCH_LABELS: Record<string, string> = {
  HN: 'HN (miền Bắc)',
  HCM: 'HCM (miền Nam)',
};

export const PHASE_OPTIONS = [
  { label: '1 pha', value: 1 },
  { label: '3 pha', value: 3 },
];

export function options<T extends string>(labels: Record<T, string>) {
  return (Object.keys(labels) as T[]).map((value) => ({ value, label: labels[value] }));
}

/** 144200000 → "144,2 tr" */
export function millions(vnd?: number): string {
  if (vnd == null) return '';
  return (Math.round(vnd / 100_000) / 10).toLocaleString('vi-VN') + ' tr';
}

export function decimal(v?: number, digits = 2): string {
  return v == null ? '' : v.toLocaleString('vi-VN', { maximumFractionDigits: digits });
}
