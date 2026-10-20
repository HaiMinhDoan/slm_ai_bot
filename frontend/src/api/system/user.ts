import { defHttp } from '@/utils/http/axios';
import type { PageResult } from '@/api/model/baseModel';
import type { CreateUserParams, RoleOption, UserAdminModel, UserStatus } from './model/systemModel';

enum Api {
  BASE = '/api/v1/admin/users',
  ROLES = '/api/v1/admin/users/roles',
}

export const userFilterApi = (params: { keyword?: string; status?: string; page?: number; size?: number }) =>
  defHttp.get<PageResult<UserAdminModel>>({ url: Api.BASE, params });

export const userDetailApi = (id: string) => defHttp.get<UserAdminModel>({ url: `${Api.BASE}/${id}` });

export const userRoleOptionsApi = () =>
  defHttp.get<RoleOption[]>({ url: Api.ROLES }, { errorMessageMode: 'none' });

export const userCreateApi = (data: CreateUserParams) =>
  defHttp.post<UserAdminModel>({ url: Api.BASE, data }, { successMessageMode: 'message' });

export const userSetStatusApi = (id: string, status: UserStatus, lockReason?: string) =>
  defHttp.put<UserAdminModel>(
    { url: `${Api.BASE}/${id}/status`, data: { status, lockReason } },
    { successMessageMode: 'message' },
  );

export const userSetRolesApi = (id: string, roleCodes: string[]) =>
  defHttp.put<UserAdminModel>(
    { url: `${Api.BASE}/${id}/roles`, data: { roleCodes } },
    { successMessageMode: 'message' },
  );

export const userResetPasswordApi = (id: string, newPassword: string) =>
  defHttp.put<void>(
    { url: `${Api.BASE}/${id}/password`, data: { newPassword } },
    { successMessageMode: 'message' },
  );
