/**
 * Kiểu dữ liệu cho module Xác thực.
 * Khớp 1-1 với DTO backend (vn.slmglobal.slmai.services.auth.dto).
 */

export type UserStatus = 'ACTIVE' | 'LOCKED' | 'DISABLED';

export interface LoginParams {
  email: string;
  password: string;
}

export interface RoleInfo {
  roleName: string;
  value: string;
}

export interface UserProfile {
  // Phần khớp GetUserInfoModel của vben
  userId: string;
  username: string;
  realName: string;
  avatar?: string;
  desc?: string;
  homePath?: string;
  roles: RoleInfo[];

  email: string;
  status: UserStatus;
  lastLoginAt?: string;
}

export interface AuthTokenResult {
  /** Tên là `token` (không phải accessToken) để khớp LoginResultModel của vben */
  token: string;
  refreshToken: string;
  tokenType: string;
  /** Thời gian sống của access token, tính bằng giây */
  expiresIn: number;
  user: UserProfile;
}

export interface ChangePasswordParams {
  currentPassword: string;
  newPassword: string;
}
