/** Khớp InboxController của backend (/api/v1/admin/inbox) */
export type ChannelCode =
  | 'MESSENGER'
  | 'FACEBOOK_COMMENT'
  | 'ZALO_OA'
  | 'TIKTOK_DM'
  | 'TIKTOK_COMMENT'
  | 'FACEBOOK_GROUP'
  | 'ZALO_PERSONAL';

export type Ownership = 'AI_ACTIVE' | 'HUMAN_OWNED';
/** NEEDS_STAFF: nhân viên phụ trách hoặc có nháp / tin lỗi; STAFF; AI; DRAFT */
export type InboxView = 'NEEDS_STAFF' | 'STAFF' | 'AI' | 'DRAFT';
export type SenderType = 'CUSTOMER' | 'AI' | 'STAFF' | 'SYSTEM';
export type MessageStatus = 'RECEIVED' | 'PROCESSED' | 'DRAFT' | 'SENT' | 'FAILED' | 'DISCARDED';
export type LeadLevel = 'HOT' | 'WARM' | 'COLD' | 'OFF_SEGMENT';

export interface InboxFilter {
  channel?: ChannelCode;
  view?: InboxView;
  keyword?: string;
  page?: number;
  size?: number;
}

export interface ConversationRow {
  id: string;
  channel: ChannelCode;
  ownership: Ownership;
  status: string;
  intent?: string;
  customerId: string;
  customerName?: string;
  phone?: string;
  leadLevel?: LeadLevel;
  lastSender?: SenderType;
  lastText?: string;
  lastStatus?: MessageStatus;
  lastAt?: string;
  draftCount: number;
  customerWaiting: boolean;
  lastCustomerMessageAt?: string;
  takenOverAt?: string;
}

export interface ChannelStats {
  channel: ChannelCode;
  total: number;
  needsStaff: number;
  drafts: number;
  staff: number;
}

export interface ConversationInfo {
  id: string;
  channel: ChannelCode;
  ownership: Ownership;
  status: string;
  intent?: string;
  summary?: string;
  takenOverAt?: string;
  autoHandbackAt?: string;
  createdAt: string;
  /** AI đang chờ / đang soạn trả lời các tin khách còn chờ */
  aiReplying: boolean;
}

export interface CustomerInfo {
  id: string;
  name?: string;
  displayName?: string;
  phone?: string;
  email?: string;
  salutation?: string;
  pageId?: string;
}

export interface ConsultProfileInfo {
  slots: Recordable;
  missing: string[];
  answeredQuestions: number;
  qualification: Recordable;
  leadLevel?: LeadLevel;
  leadLevelReason?: string;
  packages: { id: number; name: string }[];
  cheapestQuotedPrice?: number;
  lastQuestion?: string;
}

export interface LeadInfo {
  phone: string;
  province?: string;
  region?: string;
  needSummary?: string;
  leadLevel?: LeadLevel;
  leadLevelReason?: string;
  status: string;
  createdAt: string;
}

export interface SendWindow {
  channelSupported: boolean;
  open: boolean;
  endsAt?: string;
  maxLength: number;
}

export interface ChatMessage {
  id: string;
  direction: 'IN' | 'OUT';
  senderType: SenderType;
  staffId?: string;
  content?: string;
  attachments: Recordable[];
  status: MessageStatus;
  createdAt: string;
  sentAt?: string;
  /** Lý do AI không gửi tự động (nháp) */
  reasons: string[];
}

export interface ConversationDetail {
  conversation: ConversationInfo;
  customer: CustomerInfo;
  profile?: ConsultProfileInfo;
  lead?: LeadInfo;
  window: SendWindow;
  messages: ChatMessage[];
}
