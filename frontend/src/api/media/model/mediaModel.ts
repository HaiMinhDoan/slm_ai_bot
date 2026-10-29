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

export type MediaStatus = 'DRAFT' | 'APPROVED' | 'ARCHIVED';
export type MediaRights = 'OWNED' | 'LICENSED' | 'LINK_ONLY' | 'NEEDS_REVIEW';

export interface MediaModel {
  id: string;
  type: 'IMAGE' | 'VIDEO';
  title: string;
  /** Mã trong danh mục media cho AI (M07), cấp ở lần duyệt đầu */
  indexCode?: string;
  status: MediaStatus;
  rights: MediaRights;
  summary?: string;
  useWhen?: string;
  avoidWhen?: string;
  description?: string;
  transcript?: string;
  createdBy?: string;
  createdAt: string;
  updatedBy?: string;
  updatedAt?: string;
  approvedBy?: string;
  approvedAt?: string;
  /** Xoá mềm: ẩn khỏi danh sách mặc định, khôi phục được */
  deleted: boolean;
  deletedBy?: string;
  deletedAt?: string;
  sources: MediaSourceModel[];
  links: MediaLinkModel[];
}

/** Bộ lọc danh sách; status DELETED = chỉ mục đã xoá mềm */
export interface MediaFilterModel {
  keyword?: string;
  type?: 'IMAGE' | 'VIDEO';
  status?: MediaStatus | 'DELETED';
  packageId?: number;
  page?: number;
  size?: number;
}

/** Nội dung form sửa (PUT /admin/media/{id}) */
export interface MediaUpdateModel {
  title: string;
  status: MediaStatus;
  rights: MediaRights;
  summary?: string;
  useWhen?: string;
  avoidWhen?: string;
  description?: string;
  transcript?: string;
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
