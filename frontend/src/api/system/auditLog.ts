import { defHttp } from '@/utils/http/axios';
import type { PageResult } from '@/api/model/baseModel';
import type { AuditLogModel } from './model/auditLogModel';

export const auditLogFilterApi = (params: {
  actorId?: string;
  action?: string;
  targetType?: string;
  page?: number;
  size?: number;
}) => defHttp.get<PageResult<AuditLogModel>>({ url: '/api/v1/admin/audit-logs', params });
