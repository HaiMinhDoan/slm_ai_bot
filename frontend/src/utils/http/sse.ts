/**
 * Đọc Server-Sent Events bằng fetch. EventSource của trình duyệt không gửi được header Authorization, nên dùng
 * fetch + ReadableStream và tự phân tích định dạng SSE (id / event / data, dòng ":" là chú thích / ping).
 *
 * - Mất kết nối (mạng, server khởi động lại, hết thời gian sống của stream): tự kết nối lại, chờ 1s → 30s.
 * - 401 / 403 hoặc sự kiện "auth-expired": dừng hẳn (token hết hạn, đăng xuất, không đủ quyền).
 */

export interface SseHandlers {
  /** Mỗi lần kết nối thành công (kể cả kết nối lại): nơi tải lại dữ liệu có thể đã lỡ */
  onOpen?: () => void;
  onEvent: (event: string, data: string, id?: string) => void;
  /** Dừng hẳn, không kết nối lại */
  onStop?: (reason: 'unauthorized' | 'forbidden' | 'auth-expired' | 'closed') => void;
}

export interface SseConnection {
  close: () => void;
}

const MIN_RETRY_MS = 1_000;
const MAX_RETRY_MS = 30_000;

export function openAuthorizedStream(
  url: string,
  getToken: () => string | undefined,
  handlers: SseHandlers,
): SseConnection {
  let closed = false;
  let controller: AbortController | null = null;
  let retryTimer: ReturnType<typeof setTimeout> | null = null;
  let retryMs = MIN_RETRY_MS;

  const stop = (reason: Parameters<NonNullable<SseHandlers['onStop']>>[0]) => {
    if (closed) return;
    closed = true;
    controller?.abort();
    if (retryTimer) clearTimeout(retryTimer);
    handlers.onStop?.(reason);
  };

  const scheduleRetry = () => {
    if (closed) return;
    retryTimer = setTimeout(connect, retryMs);
    retryMs = Math.min(retryMs * 2, MAX_RETRY_MS);
  };

  async function connect() {
    if (closed) return;
    const token = getToken();
    if (!token) {
      stop('unauthorized');
      return;
    }
    controller = new AbortController();
    try {
      const res = await fetch(url, {
        headers: { Authorization: `Bearer ${token}`, Accept: 'text/event-stream' },
        cache: 'no-store',
        signal: controller.signal,
      });
      if (res.status === 401) return stop('unauthorized');
      if (res.status === 403) return stop('forbidden');
      if (!res.ok || !res.body) return scheduleRetry();

      retryMs = MIN_RETRY_MS;
      handlers.onOpen?.();
      await readEvents(res.body, (event, data, id) => {
        if (event === 'auth-expired') {
          stop('auth-expired');
          return;
        }
        handlers.onEvent(event, data, id);
      });
      scheduleRetry();
    } catch {
      // AbortError khi đóng chủ động; lỗi mạng thì thử lại
      scheduleRetry();
    }
  }

  connect();
  return { close: () => stop('closed') };
}

/** Tách luồng byte thành các sự kiện SSE (cách nhau bằng một dòng trống). */
async function readEvents(
  body: ReadableStream<Uint8Array>,
  emit: (event: string, data: string, id?: string) => void,
) {
  const reader = body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';
  for (;;) {
    const { value, done } = await reader.read();
    if (done) return;
    buffer += decoder.decode(value, { stream: true });
    let sep: RegExpExecArray | null;
    const boundary = /\r?\n\r?\n/;
    while ((sep = boundary.exec(buffer))) {
      const block = buffer.slice(0, sep.index);
      buffer = buffer.slice(sep.index + sep[0].length);
      let event = 'message';
      let id: string | undefined;
      const data: string[] = [];
      for (const line of block.split(/\r?\n/)) {
        if (!line || line.startsWith(':')) continue;
        const colon = line.indexOf(':');
        const field = colon < 0 ? line : line.slice(0, colon);
        let value = colon < 0 ? '' : line.slice(colon + 1);
        if (value.startsWith(' ')) value = value.slice(1);
        if (field === 'event') event = value;
        else if (field === 'data') data.push(value);
        else if (field === 'id') id = value;
      }
      if (data.length > 0) emit(event, data.join('\n'), id);
    }
  }
}
