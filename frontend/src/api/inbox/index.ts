import { defHttp } from '@/utils/http/axios';
import type { PageResult } from '@/api/model/baseModel';
import type {
  ChannelStats,
  ConversationDetail,
  ConversationRow,
  InboxFilter,
} from './model/inboxModel';

const BASE = '/api/v1/admin/inbox';

export const inboxConversationsApi = (params: InboxFilter) =>
  defHttp.get<PageResult<ConversationRow>>({ url: `${BASE}/conversations`, params });

export const inboxStatsApi = () => defHttp.get<ChannelStats[]>({ url: `${BASE}/stats` });

export const inboxConversationApi = (id: string) =>
  defHttp.get<ConversationDetail>({ url: `${BASE}/conversations/${id}` });

export const inboxToStaffApi = (id: string, note?: string) =>
  defHttp.post<ConversationDetail>({ url: `${BASE}/conversations/${id}/to-staff`, data: { note } });

export const inboxToAiApi = (id: string) =>
  defHttp.post<ConversationDetail>({ url: `${BASE}/conversations/${id}/to-ai` });

/** text rỗng = gửi nguyên nháp. Lỗi hiện tại chỗ (quá 24 giờ, Send API lỗi), không bật thông báo chung */
export const inboxSendDraftApi = (messageId: string, text?: string) =>
  defHttp.post<ConversationDetail>(
    { url: `${BASE}/messages/${messageId}/send`, data: { text } },
    { errorMessageMode: 'none' },
  );

export const inboxDiscardDraftApi = (messageId: string) =>
  defHttp.post<ConversationDetail>({ url: `${BASE}/messages/${messageId}/discard` });
