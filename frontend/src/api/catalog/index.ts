import { defHttp } from '@/utils/http/axios';
import type {
  AdviceRequestModel,
  CatalogPackageModel,
  CatalogStatusModel,
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

export const catalogPackagesApi = () =>
  defHttp.get<CatalogPackageModel[]>({ url: `${CATALOG}/packages` });

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
