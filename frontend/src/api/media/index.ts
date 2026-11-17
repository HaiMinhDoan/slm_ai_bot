import type { AxiosResponse } from 'axios';
import { defHttp } from '@/utils/http/axios';
import { ContentTypeEnum } from '@/enums/httpEnum';
import type { PageResult } from '@/api/model/baseModel';
import type {
  AvailabilityStatus,
  LinkTargetModel,
  LinkTargetType,
  MediaBulkRequest,
  MediaBulkResult,
  MediaCreateInfo,
  MediaCreateResult,
  MediaDraftModel,
  MediaFilterModel,
  MediaHistoryModel,
  MediaLinkInput,
  MediaLinksResult,
  MediaModel,
  MediaUpdateModel,
  PackageImportItem,
  PackageImportResult,
  TopicModel,
  VideoMeta,
} from './model/mediaModel';

const BASE = '/api/v1/admin/media';
/** Video tối đa 100 MB đi qua backend: chờ lâu hơn mặc định */
const UPLOAD_TIMEOUT = 600_000;

/** Mặc định gửi JSON: axios sẽ đổi FormData thành JSON nếu không đặt lại kiểu */
const MULTIPART = { 'Content-Type': ContentTypeEnum.FORM_DATA };

/** Backend nhận tối đa 10 mục mỗi lần (tải ảnh mất vài giây mỗi mục) */
export const MEDIA_IMPORT_BATCH = 10;

export const mediaListApi = (params: MediaFilterModel) =>
  defHttp.get<PageResult<MediaModel>>({ url: BASE, params });

export const mediaGetApi = (id: string) => defHttp.get<MediaModel>({ url: `${BASE}/${id}` });

export const mediaUpdateApi = (id: string, data: MediaUpdateModel) =>
  defHttp.put<MediaModel>({ url: `${BASE}/${id}`, data });

export const mediaUpdateLinksApi = (id: string, data: MediaLinkInput[]) =>
  defHttp.put<MediaLinksResult>({ url: `${BASE}/${id}/links`, data });

export const mediaHistoryApi = (id: string) =>
  defHttp.get<MediaHistoryModel[]>({ url: `${BASE}/${id}/history` });

export const mediaLinkTargetsApi = (type: LinkTargetType, keyword?: string) =>
  defHttp.get<LinkTargetModel[]>({ url: `${BASE}/link-targets`, params: { type, keyword } });

/** Xoá mềm: file, nguồn và liên kết giữ nguyên, khôi phục được */
export const mediaDeleteApi = (id: string) => defHttp.delete<void>({ url: `${BASE}/${id}` });

export const mediaRestoreApi = (id: string) =>
  defHttp.post<MediaModel>({ url: `${BASE}/${id}/restore` });

export const mediaBulkApi = (data: MediaBulkRequest) =>
  defHttp.post<MediaBulkResult[]>({ url: `${BASE}/bulk`, data, timeout: 120_000 });

/** AI viết nháp tóm tắt, dùng khi, tránh khi, mô tả, chủ đề; không lưu gì */
export const mediaAiDraftApi = (id: string) =>
  defHttp.post<MediaDraftModel>({ url: `${BASE}/${id}/ai-draft`, timeout: 180_000 });

function fileForm(file: File, meta?: VideoMeta, extra: Recordable = {}, info?: MediaCreateInfo) {
  const form = new FormData();
  form.append('file', file);
  if (meta?.thumbnail) form.append('thumbnail', meta.thumbnail, 'thumbnail.jpg');
  // Phần JSON: backend đọc bằng @RequestPart, cần đúng kiểu application/json
  if (info) form.append('info', new Blob([JSON.stringify(info)], { type: 'application/json' }));
  const params: Recordable = { ...extra };
  if (meta?.width) params.width = meta.width;
  if (meta?.height) params.height = meta.height;
  if (meta?.durationSeconds) params.durationSeconds = meta.durationSeconds;
  Object.entries(params).forEach(
    ([k, v]) => v !== undefined && v !== null && form.append(k, String(v)),
  );
  return form;
}

/** Thêm một media từ file kèm thông tin; lỗi của từng file trả trong kết quả, không hiện thông báo lỗi chung */
export const mediaUploadApi = (file: File, meta?: VideoMeta, info?: MediaCreateInfo) =>
  defHttp.post<MediaCreateResult>(
    {
      url: `${BASE}/uploads`,
      data: fileForm(file, meta, {}, info),
      headers: MULTIPART,
      timeout: UPLOAD_TIMEOUT,
    },
    { errorMessageMode: 'none' },
  );

/** Thêm một media từ link kèm thông tin; lỗi của từng link trả trong kết quả */
export const mediaAddByLinkApi = (url: string, info?: MediaCreateInfo) =>
  defHttp.post<MediaCreateResult>(
    // Có AI viết nháp thì chờ thêm thời gian Claude viết
    { url: `${BASE}/links`, data: { url, info }, timeout: 180_000 },
    { errorMessageMode: 'none' },
  );

export const mediaAddSourceFileApi = (
  id: string,
  file: File,
  meta?: VideoMeta,
  replaceSourceId?: string,
) =>
  defHttp.post<MediaModel>({
    url: `${BASE}/${id}/sources/upload`,
    data: fileForm(file, meta, { replaceSourceId }),
    headers: MULTIPART,
    timeout: UPLOAD_TIMEOUT,
  });

export const mediaAddSourceLinkApi = (id: string, url: string, replaceSourceId?: string) =>
  defHttp.post<MediaModel>({
    url: `${BASE}/${id}/sources/link`,
    data: { url, replaceSourceId },
    timeout: 60_000,
  });

export const mediaUpdateSourceApi = (
  id: string,
  sourceId: string,
  data: { availabilityStatus?: AvailabilityStatus; authorName?: string },
) => defHttp.put<MediaModel>({ url: `${BASE}/${id}/sources/${sourceId}`, data });

export const mediaDeleteSourceApi = (id: string, sourceId: string) =>
  defHttp.delete<MediaModel>({ url: `${BASE}/${id}/sources/${sourceId}` });

export const topicListApi = () => defHttp.get<TopicModel[]>({ url: `${BASE}/topics` });

export const topicCreateApi = (data: Partial<TopicModel>) =>
  defHttp.post<TopicModel>({ url: `${BASE}/topics`, data });

export const topicUpdateApi = (code: string, data: Partial<TopicModel>) =>
  defHttp.put<TopicModel>({ url: `${BASE}/topics/${code}`, data });

export const mediaImportPackagesApi = (items: PackageImportItem[], dryRun: boolean) =>
  defHttp.post<PackageImportResult[]>(
    { url: `${BASE}/imports/package-images`, data: { items, dryRun }, timeout: 120_000 },
    { errorMessageMode: 'modal' },
  );

/** Ảnh lưu trong hệ thống, tải dạng blob kèm token (thẻ img không gửi được header Authorization) */
export const mediaContentBlobApi = (mediaId: string, sourceId: string) =>
  defHttp.get<AxiosResponse<Blob>>(
    { url: `${BASE}/${mediaId}/sources/${sourceId}/content`, responseType: 'blob' },
    { isReturnNativeResponse: true, errorMessageMode: 'none' },
  );

/** Ảnh thu nhỏ của video tải lên */
export const mediaThumbnailBlobApi = (mediaId: string, sourceId: string) =>
  defHttp.get<AxiosResponse<Blob>>(
    { url: `${BASE}/${mediaId}/sources/${sourceId}/thumbnail`, responseType: 'blob' },
    { isReturnNativeResponse: true, errorMessageMode: 'none' },
  );
