import { defHttp } from '@/utils/http/axios';
import type { PageResult } from '@/api/model/baseModel';
import type {
  AssistantProposal,
  AssistantSession,
  AssistantSessionRow,
  ConsultSlotsModel,
  KnowledgeCreateModel,
  KnowledgeDiffModel,
  KnowledgeDocDetail,
  KnowledgeDocFilter,
  KnowledgeDocRow,
  KnowledgeDocType,
  KnowledgeDraftRow,
  KnowledgeLintIssue,
  KnowledgeSaveDraftModel,
  KnowledgeTokenCount,
  KnowledgeVersion,
  ReleaseDetail,
  ReleaseReport,
  ReleaseRequestModel,
  ReleaseRow,
} from './model/knowledgeModel';

const BASE = '/api/v1/admin/knowledge';

export const knowledgeDocsApi = (params: KnowledgeDocFilter) =>
  defHttp.get<PageResult<KnowledgeDocRow>>({ url: `${BASE}/docs`, params });

export const knowledgeDocApi = (id: number) =>
  defHttp.get<KnowledgeDocDetail>({ url: `${BASE}/docs/${id}` });

export const knowledgeDocByKeyApi = (key: string) =>
  defHttp.get<KnowledgeDocDetail>({ url: `${BASE}/docs/by-key`, params: { key } });

export const knowledgeCreateApi = (data: KnowledgeCreateModel) =>
  defHttp.post<KnowledgeDocDetail>({ url: `${BASE}/docs`, data }, { errorMessageMode: 'message' });

/** Không tự báo lỗi: trang soạn tự xử lý 409 (xung đột) và lỗi rule */
export const knowledgeSaveDraftApi = (id: number, data: KnowledgeSaveDraftModel) =>
  defHttp.put<KnowledgeDocDetail>(
    { url: `${BASE}/docs/${id}/draft`, data },
    { errorMessageMode: 'none' },
  );

export const knowledgeDiscardDraftApi = (id: number) =>
  defHttp.delete<KnowledgeDocDetail>(
    { url: `${BASE}/docs/${id}/draft` },
    { errorMessageMode: 'message' },
  );

export const knowledgeArchiveApi = (id: number, archived: boolean) =>
  defHttp.post<KnowledgeDocDetail>(
    { url: `${BASE}/docs/${id}/archive`, params: { archived } },
    { errorMessageMode: 'message' },
  );

export const knowledgeRestoreApi = (id: number, versionId: number) =>
  defHttp.post<KnowledgeDocDetail>(
    { url: `${BASE}/docs/${id}/restore/${versionId}` },
    { errorMessageMode: 'message' },
  );

export const knowledgeDiffApi = (id: number, from?: number, to?: number) =>
  defHttp.get<KnowledgeDiffModel>({ url: `${BASE}/docs/${id}/diff`, params: { from, to } });

export const knowledgeVersionApi = (versionId: number) =>
  defHttp.get<KnowledgeVersion>({ url: `${BASE}/versions/${versionId}` });

export const knowledgeLintApi = (data: { type?: KnowledgeDocType; key: string; content: string }) =>
  defHttp.post<KnowledgeLintIssue[]>({ url: `${BASE}/lint`, data });

export const knowledgeCountTokensApi = (data: { key: string; content: string }) =>
  defHttp.post<KnowledgeTokenCount>({ url: `${BASE}/count-tokens`, data, timeout: 60_000 });

export const knowledgeRulesApi = () => defHttp.get<KnowledgeDocDetail[]>({ url: `${BASE}/rules` });

/** name: bao-gia, phan-loai-khach, tinh-mien, kiem-tra-truoc-khi-gui */
export const knowledgeSaveRuleApi = (name: string, data: KnowledgeSaveDraftModel) =>
  defHttp.put<KnowledgeDocDetail>(
    { url: `${BASE}/rules/${name}`, data },
    { errorMessageMode: 'none' },
  );

export const consultSlotsApi = () =>
  defHttp.get<ConsultSlotsModel>({ url: `${BASE}/consult-slots` });

// ------------------------------------------------------------------ phát hành

export const releaseDraftsApi = () =>
  defHttp.get<KnowledgeDraftRow[]>({ url: `${BASE}/releases/drafts` });

export const releasesApi = (params: { page: number; size: number }) =>
  defHttp.get<PageResult<ReleaseRow>>({ url: `${BASE}/releases`, params });

export const releaseApi = (id: number) =>
  defHttp.get<ReleaseDetail>({ url: `${BASE}/releases/${id}` });

export const releaseValidateApi = (data: ReleaseRequestModel) =>
  defHttp.post<ReleaseReport>({ url: `${BASE}/releases/validate`, data, timeout: 120_000 });

export const releaseCreateApi = (data: ReleaseRequestModel) =>
  defHttp.post<ReleaseRow>(
    { url: `${BASE}/releases`, data, timeout: 120_000 },
    { errorMessageMode: 'modal' },
  );

export const releaseActivateApi = (id: number) =>
  defHttp.post<ReleaseRow>(
    { url: `${BASE}/releases/${id}/activate` },
    { errorMessageMode: 'modal' },
  );

// ------------------------------------------------------------------ AI hỗ trợ soạn

const ASSISTANT = `${BASE}/assistant`;

export const assistantSessionsApi = () =>
  defHttp.get<AssistantSessionRow[]>({ url: `${ASSISTANT}/sessions` });

export const assistantStartApi = (data: { title?: string; docId?: number }) =>
  defHttp.post<AssistantSession>({ url: `${ASSISTANT}/sessions`, data });

export const assistantSessionApi = (id: string) =>
  defHttp.get<AssistantSession>({ url: `${ASSISTANT}/sessions/${id}` });

export const assistantSendApi = (id: string, data: { text: string; docId?: number }) =>
  defHttp.post<AssistantSession>(
    { url: `${ASSISTANT}/sessions/${id}/messages`, data },
    { errorMessageMode: 'message' },
  );

export const assistantAcceptApi = (id: number, force = false) =>
  defHttp.post<AssistantProposal>(
    { url: `${ASSISTANT}/proposals/${id}/accept`, params: { force } },
    { errorMessageMode: 'none' },
  );

export const assistantRejectApi = (id: number) =>
  defHttp.post<AssistantProposal>({ url: `${ASSISTANT}/proposals/${id}/reject` });
