/** Lỗi trả về từ backend (ResponseData): mã HTTP, lời báo, dữ liệu kèm (ví dụ diff khi xung đột) */
export interface ApiError {
  status?: number;
  message: string;
  data?: any;
}

export function apiError(e: any): ApiError {
  const res = e?.response;
  return {
    status: res?.status,
    message: res?.data?.message || e?.message || 'Có lỗi xảy ra',
    data: res?.data?.data,
  };
}
