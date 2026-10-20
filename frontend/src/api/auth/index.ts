import { defHttp } from '@/utils/http/axios';
import type { ErrorMessageMode } from '#/axios';
import type { AuthTokenResult, ChangePasswordParams, LoginParams, UserProfile } from './model/authModel';

/** Không có đăng ký công khai: tài khoản nhân viên do quản trị viên tạo (Hệ thống > Người dùng). */
enum Api {
  LOGIN = '/api/v1/auth/login',
  REFRESH = '/api/v1/auth/refresh',
  LOGOUT = '/api/v1/auth/logout',
  ME = '/api/v1/auth/me',
  CHANGE_PASSWORD = '/api/v1/auth/change-password',
}

export function loginApi(params: LoginParams, mode: ErrorMessageMode = 'modal') {
  return defHttp.post<AuthTokenResult>({ url: Api.LOGIN, data: params }, { errorMessageMode: mode });
}

/**
 * Làm mới access token.
 * Backend XOAY refresh token: mỗi lần gọi trả về refresh token mới và thu hồi cái cũ, nên phải lưu giá trị mới.
 */
export function refreshTokenApi(refreshToken: string) {
  return defHttp.post<AuthTokenResult>(
    { url: Api.REFRESH, data: { refreshToken } },
    { errorMessageMode: 'none' },
  );
}

export function logoutApi(refreshToken?: string) {
  return defHttp.post<void>(
    { url: Api.LOGOUT, data: { refreshToken: refreshToken ?? '' } },
    { errorMessageMode: 'none' },
  );
}

export function getUserProfileApi() {
  return defHttp.get<UserProfile>({ url: Api.ME }, { errorMessageMode: 'none' });
}

export function changePasswordApi(params: ChangePasswordParams) {
  return defHttp.post<void>(
    { url: Api.CHANGE_PASSWORD, data: params },
    { successMessageMode: 'message' },
  );
}
