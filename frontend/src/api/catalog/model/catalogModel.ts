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
  /** Admin tắt gói trong bot (không gợi ý, không báo giá) */
  disabled: boolean;
  /** Admin đã xoá mềm */
  deleted: boolean;
  /** Các trường bot đang dùng khác slm_v2 (name, systemType, phases, kwp, panelCount, panelWp, storageKwh, totalPrice, bestSeller) */
  edited: string[];
  note?: string;
}

/** Số liệu gốc từ slm_v2 của các trường sửa được */
export interface PackageSourceModel {
  name: string;
  systemType?: SystemType;
  phases?: number;
  kwp?: number;
  panelCount?: number;
  panelWp?: number;
  storageKwh: number;
  totalPrice?: number;
  bestSeller: boolean;
}

/** Điều chỉnh đang lưu ở bot; trường số liệu null là dùng slm_v2 */
export interface PackageOverrideModel
  extends Partial<Omit<PackageSourceModel, 'storageKwh' | 'bestSeller'>> {
  storageKwh?: number;
  bestSeller?: boolean;
  disabled: boolean;
  note?: string;
  deletedAt?: string;
  deletedBy?: string;
  updatedBy?: string;
  updatedAt?: string;
}

export interface PackageEditModel {
  current: CatalogPackageModel;
  source: PackageSourceModel;
  override?: PackageOverrideModel;
}

/** Giá trị mong muốn của từng trường; để trống hoặc trùng slm_v2 thì bot dùng slm_v2 */
export interface PackageUpdateModel {
  name?: string;
  systemType?: SystemType;
  phases?: number;
  kwp?: number;
  panelCount?: number;
  panelWp?: number;
  storageKwh?: number;
  totalPrice?: number;
  bestSeller?: boolean;
  enabled: boolean;
  note?: string;
}

/** Bộ lọc và phân trang của danh sách gói (page đếm từ 0 như Spring) */
export interface CatalogPackageFilterModel {
  keyword?: string;
  branchCode?: string;
  systemType?: SystemType;
  phases?: number;
  status?: 'RECOMMENDABLE' | 'BLOCKED' | 'DISABLED' | 'INACTIVE' | 'EDITED' | 'WARNING' | 'DELETED';
  page?: number;
  size?: number;
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
