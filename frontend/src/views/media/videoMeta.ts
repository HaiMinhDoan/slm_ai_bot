import type { VideoMeta } from '@/api/media/model/mediaModel';

/** Cạnh dài của ảnh thu nhỏ cắt từ video */
const THUMB_EDGE = 480;
const TIMEOUT_MS = 15_000;

/**
 * Đọc thời lượng, kích thước và cắt một khung hình làm ảnh thu nhỏ ngay trên trình duyệt (server chưa có ffmpeg).
 * Trình duyệt không đọc được định dạng (ví dụ một số file .mov) thì trả rỗng, video vẫn tải lên được.
 */
export function readVideoMeta(file: File): Promise<VideoMeta> {
  return new Promise((resolve) => {
    const url = URL.createObjectURL(file);
    const video = document.createElement('video');
    const meta: VideoMeta = {};
    let done = false;
    const finish = () => {
      if (done) return;
      done = true;
      URL.revokeObjectURL(url);
      video.removeAttribute('src');
      resolve(meta);
    };
    const timer = window.setTimeout(finish, TIMEOUT_MS);
    video.preload = 'metadata';
    video.muted = true;
    video.onerror = () => {
      window.clearTimeout(timer);
      finish();
    };
    video.onloadedmetadata = () => {
      if (Number.isFinite(video.duration)) meta.durationSeconds = Math.round(video.duration);
      meta.width = video.videoWidth || undefined;
      meta.height = video.videoHeight || undefined;
      // Khung hình ở giây thứ 1 (hoặc giữa video nếu ngắn hơn) thường không bị đen như khung đầu
      video.currentTime = Math.min(1, (video.duration || 0) / 2);
    };
    video.onseeked = () => {
      const scale = Math.min(
        1,
        THUMB_EDGE / Math.max(video.videoWidth || 1, video.videoHeight || 1),
      );
      const canvas = document.createElement('canvas');
      canvas.width = Math.max(1, Math.round((video.videoWidth || 1) * scale));
      canvas.height = Math.max(1, Math.round((video.videoHeight || 1) * scale));
      canvas.getContext('2d')?.drawImage(video, 0, 0, canvas.width, canvas.height);
      canvas.toBlob(
        (blob) => {
          if (blob) meta.thumbnail = blob;
          window.clearTimeout(timer);
          finish();
        },
        'image/jpeg',
        0.8,
      );
    };
    video.src = url;
  });
}

export const isVideoFile = (file: File) =>
  file.type.startsWith('video/') || /\.(mp4|mov|webm)$/i.test(file.name);

/** Định dạng file nhận: khớp kiểm tra magic bytes của backend */
export const ACCEPT_IMAGES = 'image/jpeg,image/png,image/webp';
export const ACCEPT_VIDEOS = 'video/mp4,video/quicktime,video/webm';

/** "12,3 MB" / "540 KB" */
export function sizeText(bytes?: number): string {
  if (!bytes) return '';
  return bytes >= 1024 * 1024
    ? `${(bytes / 1024 / 1024).toFixed(1).replace('.', ',')} MB`
    : `${Math.round(bytes / 1024)} KB`;
}

/** 95 → "1:35" */
export function durationText(seconds?: number): string {
  if (!seconds && seconds !== 0) return '';
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m}:${String(s).padStart(2, '0')}`;
}
