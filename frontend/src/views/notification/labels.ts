import type {
  NotificationSeverity,
  NotificationType,
} from '@/api/notification/model/notificationModel';

export const TYPE_LABELS: Record<NotificationType, string> = {
  AI_HANDOFF: 'Cần nhân viên',
  AI_DRAFT: 'Nháp AI chờ duyệt',
  HANDBACK_REPLY: 'Khách nhắn lại sau khi trả AI',
  LEAD_HOT: 'Khách HOT',
  SEND_FAILED: 'Gửi tin lỗi',
  DUPLICATE_PHONE: 'Trùng số điện thoại',
};

export const SEVERITY_COLORS: Record<NotificationSeverity, string> = {
  URGENT: 'red',
  WARNING: 'orange',
  INFO: 'blue',
};

export const TYPE_OPTIONS = (Object.keys(TYPE_LABELS) as NotificationType[]).map((value) => ({
  label: TYPE_LABELS[value],
  value,
}));

/** Hộp thư Meta Business Suite của Trang (nơi nhân viên đang trả lời khách) */
export function metaInboxUrl(pageId?: string): string | undefined {
  return pageId
    ? `https://business.facebook.com/latest/inbox/all?asset_id=${encodeURIComponent(pageId)}`
    : undefined;
}
