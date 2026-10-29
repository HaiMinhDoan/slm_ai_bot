import type { AxiosResponse } from 'axios';
import { defHttp } from '@/utils/http/axios';
import type { PageResult } from '@/api/model/baseModel';
import type {
  MediaFilterModel,
  MediaModel,
  MediaUpdateModel,
  PackageImportItem,
  PackageImportResult,
} from './model/mediaModel';

const BASE = '/api/v1/admin/media';

/** Backend nhận tối đa 10 mục mỗi lần (tải ảnh mất vài giây mỗi mục) */
export const MEDIA_IMPORT_BATCH = 10;

export const mediaListApi = (params: MediaFilterModel) =>
  defHttp.get<PageResult<MediaModel>>({ url: BASE, params });

export const mediaGetApi = (id: string) => defHttp.get<MediaModel>({ url: `${BASE}/${id}` });

export const mediaUpdateApi = (id: string, data: MediaUpdateModel) =>
  defHttp.put<MediaModel>({ url: `${BASE}/${id}`, data });

/** Xoá mềm: file, nguồn và liên kết giữ nguyên, khôi phục được */
export const mediaDeleteApi = (id: string) => defHttp.delete<void>({ url: `${BASE}/${id}` });

export const mediaRestoreApi = (id: string) =>
  defHttp.post<MediaModel>({ url: `${BASE}/${id}/restore` });

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
