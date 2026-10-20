/** Khớp AuditLogResponse của backend */
export interface AuditLogModel {
  id: string;
  actorId?: string;
  actorName?: string;
  action: string;
  targetType?: string;
  targetKey?: string;
  beforeData?: unknown;
  afterData?: unknown;
  ipAddress?: string;
  createdAt: string;
}
