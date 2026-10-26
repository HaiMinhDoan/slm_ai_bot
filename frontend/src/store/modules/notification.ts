import { defineStore } from 'pinia';
import { notification as antNotification } from 'ant-design-vue';
import { store } from '@/store';
import { router } from '@/router';
import { RoleEnum } from '@/enums/roleEnum';
import { getToken } from '@/utils/auth';
import { useGlobSetting } from '@/hooks/setting';
import { openAuthorizedStream, type SseConnection } from '@/utils/http/sse';
import { installSoundUnlock, playNotificationSound } from '@/utils/notifySound';
import {
  NOTIFICATION_STREAM_PATH,
  notificationListApi,
  notificationMarkAllReadApi,
  notificationMarkReadApi,
  notificationUnreadCountApi,
} from '@/api/notification';
import type {
  NotificationModel,
  NotificationStreamEvent,
} from '@/api/notification/model/notificationModel';

const SETTINGS_KEY = 'SLM_NOTIFY_SETTINGS';
const LATEST_SIZE = 10;
/** Vai trò nhận thông báo (khớp @RequireAuth(roles = STAFF) ở backend, SYSTEM_ADMIN luôn qua) */
export const NOTIFICATION_ROLES = [RoleEnum.STAFF, RoleEnum.SYSTEM_ADMIN];

export interface NotificationSettings {
  sound: boolean;
  desktop: boolean;
}

interface NotificationState {
  unreadCount: number;
  latest: NotificationModel[];
  connected: boolean;
  settings: NotificationSettings;
}

let connection: SseConnection | null = null;

function loadSettings(): NotificationSettings {
  try {
    const raw = localStorage.getItem(SETTINGS_KEY);
    if (raw) return { sound: true, desktop: false, ...JSON.parse(raw) };
  } catch {
    // localStorage bị chặn: dùng mặc định
  }
  return { sound: true, desktop: false };
}

/** Thông báo của hệ điều hành chỉ chạy trên HTTPS (hoặc localhost) và khi trình duyệt có hỗ trợ */
export function desktopNotificationSupported(): boolean {
  return typeof window !== 'undefined' && 'Notification' in window && window.isSecureContext;
}

export const useNotificationStore = defineStore({
  id: 'app-notification',
  state: (): NotificationState => ({
    unreadCount: 0,
    latest: [],
    connected: false,
    settings: loadSettings(),
  }),
  actions: {
    /** Mở stream khi vào layout admin (chỉ người có vai trò nhận thông báo). */
    start(roles: RoleEnum[]) {
      if (connection || !roles.some((r) => NOTIFICATION_ROLES.includes(r))) return;
      installSoundUnlock();
      const { apiUrl } = useGlobSetting();
      const token = () => getToken() as string | undefined;
      connection = openAuthorizedStream(`${apiUrl}${NOTIFICATION_STREAM_PATH}`, token, {
        onOpen: () => {
          this.connected = true;
          this.refresh();
        },
        onEvent: (event, data) => this.handleEvent(event, data),
        onStop: () => {
          this.connected = false;
          connection = null;
        },
      });
    },

    /** Đóng stream (đăng xuất, token hết hạn). */
    stop() {
      connection?.close();
      connection = null;
      this.connected = false;
      this.unreadCount = 0;
      this.latest = [];
    },

    /** Tải lại số chưa đọc và danh sách mới nhất (sau mỗi lần kết nối, sau khi đánh dấu đọc). */
    async refresh() {
      try {
        const [count, page] = await Promise.all([
          notificationUnreadCountApi(),
          notificationListApi({ page: 0, size: LATEST_SIZE }),
        ]);
        this.unreadCount = count.count;
        this.latest = page.items;
      } catch {
        // Lỗi mạng tạm thời: lần kết nối sau sẽ tải lại
      }
    },

    handleEvent(event: string, data: string) {
      let payload: any;
      try {
        payload = JSON.parse(data);
      } catch {
        return;
      }
      if (event === 'ready') {
        this.unreadCount = payload.unreadCount ?? this.unreadCount;
        return;
      }
      if (event !== 'notification') return;
      const n = payload as NotificationStreamEvent;
      if (this.latest.some((x) => x.id === n.id)) return;
      this.latest = [{ ...n, read: false }, ...this.latest].slice(0, LATEST_SIZE);
      this.unreadCount += 1;
      this.alert(n);
    },

    /** Chuông + thông báo: tab đang ẩn thì dùng thông báo của hệ điều hành, đang xem thì hiện ngay trong trang. */
    alert(n: NotificationStreamEvent) {
      if (this.settings.sound) {
        playNotificationSound(n.id, n.severity);
      }
      const open = () => {
        window.focus();
        router.push({ path: '/notifications/index', query: { id: String(n.id) } });
      };
      const canDesktop =
        this.settings.desktop &&
        desktopNotificationSupported() &&
        Notification.permission === 'granted';
      if (document.hidden && canDesktop) {
        // Cùng tag: nhiều tab cùng nhận chỉ hiện một thông báo
        const os = new Notification(n.title, {
          body: n.body ?? '',
          tag: `slm-notify-${n.id}`,
          requireInteraction: n.severity === 'URGENT',
        });
        os.onclick = () => {
          open();
          os.close();
        };
        return;
      }
      if (!document.hidden) {
        const show =
          n.severity === 'URGENT'
            ? antNotification.error
            : n.severity === 'WARNING'
              ? antNotification.warning
              : antNotification.info;
        show({
          key: `slm-notify-${n.id}`,
          message: n.title,
          description: n.body,
          duration: n.severity === 'URGENT' ? 0 : 8,
          onClick: () => {
            antNotification.close(`slm-notify-${n.id}`);
            open();
          },
        });
      }
    },

    async markRead(id: number) {
      const item = this.latest.find((x) => x.id === id);
      if (item?.read) return;
      await notificationMarkReadApi(id);
      if (item) item.read = true;
      this.unreadCount = Math.max(0, this.unreadCount - 1);
    },

    async markAllRead() {
      const maxId = this.latest.reduce((m, x) => Math.max(m, x.id), 0);
      if (!maxId) return;
      await notificationMarkAllReadApi(maxId);
      await this.refresh();
    },

    setSound(enabled: boolean) {
      this.settings = { ...this.settings, sound: enabled };
      this.saveSettings();
    },

    /** Bật thông báo hệ điều hành: phải gọi từ thao tác bấm của người dùng để trình duyệt hỏi quyền. */
    async setDesktop(enabled: boolean): Promise<boolean> {
      if (enabled) {
        if (!desktopNotificationSupported()) return false;
        const permission =
          Notification.permission === 'granted'
            ? 'granted'
            : await Notification.requestPermission();
        if (permission !== 'granted') return false;
      }
      this.settings = { ...this.settings, desktop: enabled };
      this.saveSettings();
      return true;
    },

    saveSettings() {
      try {
        localStorage.setItem(SETTINGS_KEY, JSON.stringify(this.settings));
      } catch {
        // localStorage bị chặn: chỉ giữ trong phiên
      }
    },
  },
});

// Dùng ngoài setup (store khác, router guard)
export function useNotificationStoreWithOut() {
  return useNotificationStore(store);
}
