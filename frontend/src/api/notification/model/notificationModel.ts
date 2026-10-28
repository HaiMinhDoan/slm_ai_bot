/** Khớp NotificationController của backend (/api/v1/notifications) */
export type NotificationType =
  | 'AI_HANDOFF'
  | 'AI_DRAFT'
  | 'HANDBACK_REPLY'
  | 'LEAD_HOT'
  | 'SEND_FAILED'
  | 'DUPLICATE_PHONE'
  | 'CATALOG_SYNC_FAILED'
  | 'DATA_UPDATE_DUE';

export type NotificationSeverity = 'INFO' | 'WARNING' | 'URGENT';

export interface NotificationModel {
  id: number;
  type: NotificationType;
  severity: NotificationSeverity;
  title: string;
  body?: string;
  /** Chi tiết: nháp AI, lý do, tin cuối của khách, số điện thoại... */
  data?: Recordable;
  conversationId?: string;
  customerId?: string;
  createdAt: string;
  read: boolean;
}

/** Sự kiện "notification" trên stream SSE: chỉ thông tin tối thiểu, chi tiết lấy qua REST */
export interface NotificationStreamEvent {
  id: number;
  type: NotificationType;
  severity: NotificationSeverity;
  title: string;
  body?: string;
  conversationId?: string;
  createdAt: string;
}
