import type { UserStatus } from '@/api/auth/model/authModel';

export type { UserStatus };

/** Khớp UserAdminResponse của backend */
export interface UserAdminModel {
  id: string;
  email: string;
  fullName: string;
  status: UserStatus;
  lockReason?: string;
  roleCodes: string[];
  lastLoginAt?: string;
  createdAt: string;
}

export interface RoleOption {
  code: string;
  name: string;
  description?: string;
}

export interface CreateUserParams {
  email: string;
  fullName: string;
  password: string;
  roleCodes: string[];
}
