export type KnowledgeDocType =
  | 'CORE'
  | 'PLAYBOOK'
  | 'POLICY'
  | 'FAQ'
  | 'EXAMPLE'
  | 'PROMPT'
  | 'RULE'
  | 'GUIDE'
  | 'TEMPLATE'
  | 'CONTENT';

export interface KnowledgeDocRow {
  id: number;
  key: string;
  type: KnowledgeDocType;
  typeLabel: string;
  format: 'MARKDOWN' | 'YAML';
  title: string;
  /** Phiên bản bot đang dùng; null = chưa phát hành */
  activeVersionNo?: number;
  latestVersionNo?: number;
  draftVersionId?: number;
  draftAuthor?: string;
  draftUpdatedAt?: string;
  archived: boolean;
  botReads: boolean;
}

export interface KnowledgeVersionSummary {
  id: number;
  versionNo: number;
  status: 'DRAFT' | 'FROZEN' | 'DISCARDED';
  title: string;
  contentHash: string;
  baseVersionId?: number;
  changeNote?: string;
  author: string;
  aiAssisted: boolean;
  createdAt: string;
  updatedAt: string;
  active: boolean;
}

export interface KnowledgeVersion extends KnowledgeVersionSummary {
  docId: number;
  content: string;
}

export interface KnowledgeDocDetail {
  doc: KnowledgeDocRow;
  active?: KnowledgeVersion;
  latest?: KnowledgeVersion;
  draft?: KnowledgeVersion;
  versions: KnowledgeVersionSummary[];
  /** Rule YAML đọc thành JSON cho form */
  value?: Recordable;
  ruleErrors: string[];
}

export interface KnowledgeDocFilter {
  page: number;
  size: number;
  type?: KnowledgeDocType;
  keyword?: string;
  status?: 'DRAFT' | 'NOT_RELEASED' | 'ARCHIVED';
}

export interface KnowledgeCreateModel {
  type: KnowledgeDocType;
  key: string;
  title: string;
  content?: string;
}

export interface KnowledgeSaveDraftModel {
  title: string;
  content?: string;
  value?: Recordable;
  changeNote?: string;
  baseVersionId?: number;
  expectedDraftUpdatedAt?: string;
  force?: boolean;
}

export interface KnowledgeDiffModel {
  from?: KnowledgeVersionSummary;
  to?: KnowledgeVersionSummary;
  diff: string;
}

export interface KnowledgeLintIssue {
  level: 'ERROR' | 'WARNING' | 'INFO';
  code: string;
  message: string;
  line?: number;
}

export interface KnowledgeTokenCount {
  tokens: number;
  budget: number;
  percent: number;
  estimated: boolean;
}

/** 409 khi lưu nháp: đã có phiên bản mới hơn hoặc người khác vừa lưu */
export interface KnowledgeConflict {
  reason: 'BASE_CHANGED' | 'DRAFT_CHANGED';
  current?: KnowledgeVersion;
  diff: string;
}

export interface ConsultSlot {
  key: string;
  label: string;
  required: boolean;
  values: string[];
}

export interface ConsultSlotsModel {
  slots: ConsultSlot[];
  qualification: ConsultSlot[];
  note: string;
}

// ------------------------------------------------------------------ phát hành

export interface KnowledgeDraftRow {
  versionId: number;
  docId: number;
  key: string;
  typeLabel: string;
  title: string;
  versionNo: number;
  activeVersionNo?: number;
  author: string;
  updatedAt: string;
  changeNote?: string;
  aiAssisted: boolean;
  archived: boolean;
}

export interface ReleaseRequestModel {
  /** Bỏ trống = mọi bản nháp */
  versionIds?: number[];
  notes?: string;
}

export interface ReleaseCheck {
  step: string;
  level: 'ERROR' | 'WARNING' | 'OK';
  docKey?: string;
  message: string;
}

export interface ReleaseChange {
  key: string;
  title: string;
  action: 'ADDED' | 'UPDATED' | 'REMOVED';
  fromVersionNo?: number;
  toVersionNo?: number;
}

export interface ReleaseReport {
  ok: boolean;
  checks: ReleaseCheck[];
  changes: ReleaseChange[];
  tokenTotal?: number;
  tokenBudget: number;
  tokenEstimated: boolean;
  kbVersion: string;
}

export type ReleaseStatus = 'VALIDATING' | 'READY' | 'ACTIVE' | 'FAILED' | 'SUPERSEDED';

export interface ReleaseRow {
  id: number;
  kbVersion: string;
  status: ReleaseStatus;
  notes?: string;
  tokenTotal?: number;
  createdBy: string;
  createdAt: string;
  activatedBy?: string;
  activatedAt?: string;
  docCount: number;
}

export interface ReleaseItemRow {
  docId: number;
  key: string;
  typeLabel: string;
  title: string;
  versionNo: number;
  versionId: number;
}

export interface ReleaseDetail {
  release: ReleaseRow;
  report?: ReleaseReport;
  items: ReleaseItemRow[];
}

// ------------------------------------------------------------------ AI hỗ trợ soạn

export interface AssistantSessionRow {
  id: string;
  title: string;
  createdAt: string;
  lastMessageAt?: string;
}

export interface AssistantMessage {
  id: number;
  role: 'USER' | 'ASSISTANT';
  text?: string;
  error?: string;
  proposalIds: number[];
  createdAt: string;
}

export interface AssistantProposal {
  id: number;
  action: 'CREATE' | 'UPDATE' | 'RETIRE';
  docId?: number;
  docKey: string;
  docType?: KnowledgeDocType;
  typeLabel?: string;
  title: string;
  content?: string;
  summary: string;
  reason?: string;
  conflicts: string[];
  lint?: KnowledgeLintIssue[];
  status: 'PROPOSED' | 'ACCEPTED' | 'REJECTED' | 'SUPERSEDED';
  diff?: string;
  canAccept: boolean;
  hasOtherDraft: boolean;
  resultVersionId?: number;
  createdAt: string;
}

export interface AssistantSession {
  id: string;
  title: string;
  createdAt: string;
  /** AI đang trả lời: hỏi lại sau vài giây */
  pending: boolean;
  messages: AssistantMessage[];
  proposals: AssistantProposal[];
}
