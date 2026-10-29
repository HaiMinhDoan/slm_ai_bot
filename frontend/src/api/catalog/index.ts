import { defHttp } from '@/utils/http/axios';
import type { PageResult } from '@/api/model/baseModel';
import type {
  AdviceRequestModel,
  CatalogPackageFilterModel,
  CatalogPackageModel,
  CatalogStatusModel,
  PackageEditModel,
  PackageUpdateModel,
  PvoutImportRequestModel,
  PvoutImportResultModel,
  PvoutTableInfoModel,
  PvoutTableModel,
  SyncRunModel,
  WarningLabels,
} from './model/catalogModel';

const CATALOG = '/api/v1/admin/catalog';
const PVOUT = '/api/v1/admin/pvout';

export const catalogStatusApi = () => defHttp.get<CatalogStatusModel>({ url: `${CATALOG}/status` });

/** Chờ tới khi đồng bộ xong (thường vài giây) */
export const catalogSyncApi = () =>
  defHttp.post<SyncRunModel>(
    { url: `${CATALOG}/sync`, timeout: 180_000 },
    { errorMessageMode: 'modal' },
  );

export const catalogPackagesApi = (params: CatalogPackageFilterModel) =>
  defHttp.get<PageResult<CatalogPackageModel>>({ url: `${CATALOG}/packages`, params });

/** Gói kèm số liệu slm_v2 và điều chỉnh của bot, để mở form sửa */
export const catalogPackageEditApi = (id: number) =>
  defHttp.get<PackageEditModel>({ url: `${CATALOG}/packages/${id}` });

/** Chỉ áp dụng cho bot: slm_v2 không đổi, đồng bộ không đè mất */
export const catalogPackageUpdateApi = (id: number, data: PackageUpdateModel) =>
  defHttp.put<CatalogPackageModel>({ url: `${CATALOG}/packages/${id}`, data });

export const catalogPackageSetActiveApi = (ids: number[], active: boolean) =>
  defHttp.post<{ affected: number }>({
    url: `${CATALOG}/packages/set-active`,
    data: { ids, active },
  });

/** Xoá mềm: ẩn khỏi danh sách và bot, khôi phục được */
export const catalogPackageDeleteApi = (id: number) =>
  defHttp.delete<void>({ url: `${CATALOG}/packages/${id}` });

export const catalogPackageRestoreApi = (id: number) =>
  defHttp.post<CatalogPackageModel>({ url: `${CATALOG}/packages/${id}/restore` });

export const catalogWarningLabelsApi = () =>
  defHttp.get<WarningLabels>({ url: `${CATALOG}/warning-labels` });

/** Kết quả đúng như AI nhận được từ tool tim_goi_phu_hop */
export const catalogAdviseApi = (data: AdviceRequestModel) =>
  defHttp.post<Recordable>({ url: `${CATALOG}/advise`, data });

export const catalogPackageDetailApi = (id: number, khuVuc?: string) =>
  defHttp.get<Recordable>({ url: `${CATALOG}/packages/${id}/detail`, params: { khuVuc } });

export const pvoutListApi = () => defHttp.get<PvoutTableInfoModel[]>({ url: PVOUT });

export const pvoutTableApi = (year: number) =>
  defHttp.get<PvoutTableModel>({ url: `${PVOUT}/${year}` });

export const pvoutImportApi = (data: PvoutImportRequestModel) =>
  defHttp.post<PvoutImportResultModel>(
    { url: `${PVOUT}/import`, data },
    { errorMessageMode: 'modal' },
  );
