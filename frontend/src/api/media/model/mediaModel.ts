/** Khớp MediaAdminController của backend (/api/v1/admin/media) */
export interface MediaSourceModel {
  id: string;
  sourceType: 'UPLOAD' | 'YOUTUBE' | 'FACEBOOK' | 'TIKTOK' | 'SLM_V2';
  sourceUrl?: string;
  externalId?: string;
  mimeType?: string;
  sizeBytes?: number;
  width?: number;
  height?: number;
  processingStatus: string;
  availabilityStatus: string;
  /** Có file lưu trong hệ thống (xem trước được) */
  stored: boolean;
}

export interface MediaLinkModel {
  targetType: 'PACKAGE' | 'MATERIAL' | 'BRAND' | 'KNOWLEDGE_DOC';
  targetKey: string;
  role: string;
}

export interface MediaModel {
  id: string;
  type: 'IMAGE' | 'VIDEO';
  title: string;
  status: 'DRAFT' | 'APPROVED' | 'ARCHIVED';
  rights: string;
  createdBy?: string;
  createdAt: string;
  sources: MediaSourceModel[];
  links: MediaLinkModel[];
}

/** Một combo cần nhập: id gói slm_v2 (HN, HCM), link Drive, link Facebook, link web */
export interface PackageImportItem {
  tronGoiId: number;
  tronGoiIdHcm?: number;
  title: string;
  driveUrl?: string;
  facebookUrl?: string;
  webUrl?: string;
}

export type ImportOutcome = 'CREATED' | 'EXISTS' | 'WOULD_CREATE' | 'SKIPPED' | 'FAILED';

export interface ImportPartResult {
  outcome: ImportOutcome;
  mediaId?: string;
  note?: string;
}

export interface PackageImportResult {
  tronGoiId: number;
  tronGoiIdHcm?: number;
  image: ImportPartResult;
  video: ImportPartResult;
  warnings: string[];
}
