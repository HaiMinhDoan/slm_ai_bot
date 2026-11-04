import type { EditorConfiguration } from 'codemirror';
import 'codemirror/addon/display/autorefresh';

/**
 * Cấu hình CodeEditor dựng trong modal / drawer / tab:
 * - fixedGutter: false: lề số dòng tính theo offsetWidth. Mặc định CodeMirror đo bằng getBoundingClientRect, đúng
 *   lúc modal đang phóng to (transform scale) nên số dòng đè lên chữ. Editor đã xuống dòng tự động nên không có
 *   cuộn ngang, bỏ cố định lề không mất gì.
 * - autoRefresh (addon có sẵn của codemirror): khung dựng lúc còn ẩn thì tự vẽ lại khi hiện ra.
 */
export const CODE_CONFIG: EditorConfiguration = { fixedGutter: false, autoRefresh: true };
