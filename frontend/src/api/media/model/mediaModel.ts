/** Khớp MediaAdminController của backend (/api/v1/admin/media) */
export type AvailabilityStatus = 'ACTIVE' | 'PRIVATE' | 'UNAVAILABLE' | 'DELETED' | 'UNKNOWN';

export interface MediaSourceModel {
  id: string;
  sourceType: 'UPLOAD' | 'YOUTUBE' | 'FACEBOOK' | 'TIKTOK' | 'SLM_V2';
  sourceUrl?: string;
  externalId?: string;
  authorName?: string;
  mimeType?: string;
  sizeBytes?: number;
  width?: number;
  height?: number;
  durationSeconds?: number;
  sha256?: string;
  /** Ảnh xem trước của link ngoài (YouTube, TikTok) */
  thumbnailUrl?: string;
  /** Video tải lên có ảnh thu nhỏ lưu trong hệ thống */
  hasThumbnail: boolean;
  processingStatus: string;
  availabilityStatus: AvailabilityStatus;
  lastCheckedAt?: string;
  createdAt?: string;
  /** Có file lưu trong hệ thống (xem trước được) */
  stored: boolean;
}

export type LinkTargetType = 'PACKAGE' | 'MATERIAL' | 'BRAND' | 'KNOWLEDGE_DOC';
export type LinkRole = 'MAIN_IMAGE' | 'GALLERY' | 'EXPLAINER' | 'CASE_STUDY' | 'GUIDE' | 'RELATED';

export interface MediaLinkModel {
  targetType: LinkTargetType;
  targetKey: string;
  role: LinkRole;
  sortOrder: number;
  /** Tên đối tượng; trống nếu đối tượng không còn */
  label?: string;
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
  topics: string[];
  customerStages: string[];
  allowedChannels: string[];
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
  topics: string[];
  customerStages: string[];
  allowedChannels: string[];
}

export interface MediaLinkInput {
  targetType: LinkTargetType;
  targetKey: string;
  role: LinkRole;
  sortOrder?: number;
}

export interface MediaLinksResult {
  media: MediaModel;
  /** Thay đổi phụ, ví dụ ảnh chính cũ của gói chuyển thành ảnh thêm */
  notes: string[];
}

/** Gói / vật tư / hãng / tài liệu để gắn; twin là gói cùng tên ở miền kia */
export interface LinkTargetModel {
  key: string;
  label: string;
  twinKey?: string;
  twinLabel?: string;
}

export type CreateOutcome = 'CREATED' | 'EXISTS' | 'FAILED';

/** Kết quả thêm một file / một link */
export interface MediaCreateResult {
  name: string;
  outcome: CreateOutcome;
  mediaId?: string;
  note?: string;
}

/** Thông số video trình duyệt đọc được */
export interface VideoMeta {
  width?: number;
  height?: number;
  durationSeconds?: number;
  thumbnail?: Blob;
}

export interface MediaHistoryModel {
  at: string;
  actorName?: string;
  action: string;
  before?: Recordable;
  after?: Recordable;
}

export type BulkAction =
  | 'SET_STATUS'
  | 'SET_RIGHTS'
  | 'ADD_TOPICS'
  | 'ADD_LINK'
  | 'DELETE'
  | 'RESTORE';

export interface MediaBulkRequest {
  ids: string[];
  action: BulkAction;
  status?: MediaStatus;
  rights?: MediaRights;
  topics?: string[];
  link?: MediaLinkInput;
}

export interface MediaBulkResult {
  id: string;
  title?: string;
  ok: boolean;
  note?: string;
}

export interface MediaDraftModel {
  summary: string;
  useWhen: string;
  avoidWhen: string;
  description: string;
  topics: string[];
  /** Claude đã xem ảnh (hoặc khung hình video) */
  sawImage: boolean;
}

export interface TopicModel {
  code: string;
  name: string;
  description?: string;
  sortOrder: number;
  active: boolean;
  updatedBy?: string;
  updatedAt?: string;
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
