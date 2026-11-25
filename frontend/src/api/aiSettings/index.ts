import { defHttp } from '@/utils/http/axios';

export interface KillSwitch {
  on: boolean;
  reason?: string;
  updatedBy?: string;
  updatedAt?: string;
}

/** Cài đặt vận hành AI (mục 33.2): có hiệu lực ngay, không qua phát hành */
export interface AiSettings {
  killSwitch: KillSwitch;
  autoIntents: string[];
  handoffIntents: string[];
  businessStart: string;
  businessEnd: string;
  businessDays: string[];
  outOfHoursAck?: string;
  handoffAck?: string;
  ackCooldownHours: number;
  personaName: string;
  companyName: string;
  typingIndicator: boolean;
  typingCharsPerSecond: number;
  typingMaxSeconds: number;
  debounceSeconds: number;
  autoHandbackHours: number;
  model: string;
  effort: string;
  assistantModel: string;
  maxAttempts: number;
  retryBackoffSeconds: number;
  /** Nhắc khách khi khách im lặng (mục 56.9) */
  followUpEnabled: boolean;
  followUpIntervalHours: number;
  /** 0 = không giới hạn trong cửa sổ 24 giờ */
  followUpMaxCount: number;
  followUpAutoSend: boolean;
  followUpStart: string;
  followUpEnd: string;
  followUpNotifyStaff: boolean;
}

export interface AiSettingsView {
  values: AiSettings;
  defaults: AiSettings;
  /** Khoá snake_case đang ghi đè giá trị trong application.yml */
  overridden: string[];
  intents: string[];
  models: string[];
  efforts: string[];
}

const BASE = '/api/v1/admin/ai-settings';

export const aiSettingsApi = () => defHttp.get<AiSettingsView>({ url: BASE });

export const aiSettingsSaveApi = (data: AiSettings) =>
  defHttp.put({ url: BASE, data }, { errorMessageMode: 'modal', successMessageMode: 'none' });

export const aiKillSwitchApi = (on: boolean, reason?: string) =>
  defHttp.post<KillSwitch>(
    { url: `${BASE}/kill-switch`, data: { on, reason } },
    { errorMessageMode: 'modal' },
  );
