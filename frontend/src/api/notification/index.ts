import { defHttp } from '@/utils/http/axios';
import type { PageResult } from '@/api/model/baseModel';
import type { NotificationModel, NotificationType } from './model/notificationModel';

const BASE = '/api/v1/notifications';

/** Đường dẫn stream SSE (đọc bằng fetch để gửi được header Authorization) */
export const NOTIFICATION_STREAM_PATH = `${BASE}/stream`;

export const notificationListApi = (params: {
  unreadOnly?: boolean;
  type?: NotificationType;
  page?: number;
  size?: number;
}) => defHttp.get<PageResult<NotificationModel>>({ url: BASE, params });

export const notificationUnreadCountApi = () =>
  defHttp.get<{ count: number }>({ url: `${BASE}/unread-count` }, { errorMessageMode: 'none' });

export const notificationDetailApi = (id: number) =>
  defHttp.get<NotificationModel>({ url: `${BASE}/${id}` });

export const notificationMarkReadApi = (id: number) =>
  defHttp.post<boolean>({ url: `${BASE}/${id}/read` }, { errorMessageMode: 'none' });

/** Đọc hết tới maxId: thông báo đến sau lúc bấm vẫn giữ chưa đọc */
export const notificationMarkAllReadApi = (maxId: number) =>
  defHttp.post<{ marked: number }>({ url: `${BASE}/read-all?maxId=${maxId}` });
