/**
 * Chuông thông báo phát bằng Web Audio (không dùng file âm thanh: repo công khai, tránh vướng bản quyền).
 *
 * Trình duyệt chặn phát âm thanh khi người dùng chưa tương tác với trang, nên AudioContext được "mở khoá" ở lần
 * click / gõ phím đầu tiên. Nhiều tab admin cùng mở thì chỉ một tab kêu cho mỗi thông báo (navigator.locks).
 */

type Severity = 'INFO' | 'WARNING' | 'URGENT';

let ctx: AudioContext | null = null;
let installed = false;

function audioContext(): AudioContext | null {
  if (ctx) return ctx;
  const Ctor = window.AudioContext || (window as any).webkitAudioContext;
  if (!Ctor) return null;
  ctx = new Ctor();
  return ctx;
}

/** Gắn sự kiện mở khoá âm thanh ở tương tác đầu tiên (gọi một lần khi vào trang admin). */
export function installSoundUnlock() {
  if (installed) return;
  installed = true;
  const unlock = () => {
    const c = audioContext();
    c?.resume().catch(() => undefined);
    window.removeEventListener('pointerdown', unlock, true);
    window.removeEventListener('keydown', unlock, true);
  };
  window.addEventListener('pointerdown', unlock, true);
  window.addEventListener('keydown', unlock, true);
}

/** Âm thanh đã được trình duyệt cho phép chưa (để trang cài đặt nhắc người dùng bấm vào trang). */
export function isSoundUnlocked(): boolean {
  return !!ctx && ctx.state === 'running';
}

function tone(c: AudioContext, freq: number, start: number, duration: number, volume: number) {
  const osc = c.createOscillator();
  const gain = c.createGain();
  osc.type = 'sine';
  osc.frequency.setValueAtTime(freq, start);
  gain.gain.setValueAtTime(0.0001, start);
  gain.gain.exponentialRampToValueAtTime(volume, start + 0.02);
  gain.gain.exponentialRampToValueAtTime(0.0001, start + duration);
  osc.connect(gain).connect(c.destination);
  osc.start(start);
  osc.stop(start + duration + 0.05);
}

function chime(severity: Severity) {
  const c = audioContext();
  if (!c || c.state !== 'running') return;
  const t = c.currentTime + 0.01;
  // INFO: 2 nốt nhẹ; WARNING: 2 nốt rõ; URGENT: 3 nốt, lặp lại một lần
  const notes = severity === 'INFO' ? [660, 880] : [880, 1320];
  const volume = severity === 'INFO' ? 0.12 : 0.25;
  notes.forEach((f, i) => tone(c, f, t + i * 0.18, 0.35, volume));
  if (severity === 'URGENT') {
    tone(c, 1760, t + 0.36, 0.4, volume);
    [880, 1320, 1760].forEach((f, i) => tone(c, f, t + 0.9 + i * 0.18, 0.35, volume));
  }
}

/** Phát chuông cho thông báo {@code id}; nhiều tab cùng nhận thì chỉ tab giành được khoá kêu. */
export function playNotificationSound(id: number | string, severity: Severity = 'WARNING') {
  const locks = (navigator as any).locks;
  if (!locks?.request) {
    chime(severity);
    return;
  }
  locks
    .request(`slm-notify-sound-${id}`, { ifAvailable: true }, async (lock: unknown) => {
      if (!lock) return;
      chime(severity);
      // Giữ khoá một lúc để tab nhận chậm hơn không kêu lại
      await new Promise((r) => setTimeout(r, 3000));
    })
    .catch(() => chime(severity));
}
