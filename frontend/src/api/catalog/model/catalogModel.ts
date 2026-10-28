/** Khớp CatalogAdminController và PvoutAdminController của backend */

export type SystemType = 'ON_GRID' | 'HYBRID';
export type DayNight = 'NGAY_NHIEU' | 'CAN_BANG' | 'DEM_NHIEU';
export type RoofType = 'TON' | 'NGOI' | 'BANG';

export interface SyncRunModel {
  id: number;
  trigger: 'SCHEDULED' | 'MANUAL';
  status: 'RUNNING' | 'SUCCESS' | 'FAILED';
  startedAt: string;
  finishedAt?: string;
  inserted?: number;
  updated?: number;
  deactivated?: number;
  /** mã cảnh báo → id gói / vật tư */
  warnings?: Record<string, number[]>;
  error?: string;
  version?: string;
  triggeredBy?: string;
}

export interface CatalogStatusModel {
  configured: boolean;
  version?: string;
  lastSuccess?: SyncRunModel;
  runs: SyncRunModel[];
}

export interface CatalogPackageModel {
  id: number;
  branchCode: string;
  familyId?: number;
  familyName?: string;
  name: string;
  systemType?: SystemType;
  phases?: number;
  kwp?: number;
  panelCount?: number;
  panelWp?: number;
  inverterKw?: number;
  storageKwh: number;
  totalPrice?: number;
  steelFramePrice: number;
  bestSeller: boolean;
  active: boolean;
  recommendable: boolean;
  warnings: string[];
  panelBrand?: string;
  inverterBrand?: string;
  batteryBrand?: string;
}

export type WarningLabels = Record<string, { label: string; blocking: boolean }>;

/** Đầu vào tool tim_goi_phu_hop; trường chưa biết để trống */
export interface AdviceRequestModel {
  province?: string;
  phases?: number;
  monthlyBillVnd?: number;
  dayNight?: DayNight;
  roofType?: RoofType;
  roofAreaM2?: number;
  systemType?: SystemType;
  backupNeeded?: boolean;
}

export interface PvoutTableInfoModel {
  year: number;
  source: string;
  note?: string;
  importedBy?: string;
  importedAt: string;
  provinceCount: number;
}

export interface PvoutProvinceRowModel {
  code: string;
  name: string;
  region: string;
  monthly: number[];
  annual: number;
}

export interface PvoutTableModel {
  year: number;
  source: string;
  rows: PvoutProvinceRowModel[];
}

export interface PvoutImportRequestModel {
  year: number;
  source: string;
  note?: string;
  rows: { province: string; monthly: number[] }[];
  dryRun: boolean;
}

export interface PvoutImportResultModel {
  year: number;
  saved: boolean;
  errors: string[];
  comparedYear?: number;
  rows: {
    code: string;
    name: string;
    monthly: number[];
    annual: number;
    previousAnnual?: number;
    changePercent?: number;
  }[];
}
