import type { AppRouteModule } from '@/router/types';

import { LAYOUT } from '@/router/constant';
import { RoleEnum } from '@/enums/roleEnum';

/** Menu "Hệ thống": người dùng nội bộ và nhật ký thao tác (chỉ SYSTEM_ADMIN) */
const system: AppRouteModule = {
  path: '/system',
  name: 'System',
  component: LAYOUT,
  redirect: '/system/user',
  meta: {
    orderNo: 90,
    icon: 'ant-design:setting-outlined',
    title: 'Hệ thống',
    roles: [RoleEnum.SYSTEM_ADMIN],
  },
  children: [
    {
      path: 'user',
      name: 'UserManagement',
      component: () => import('@/views/system/user/index.vue'),
      meta: {
        title: 'Người dùng & Vai trò',
        icon: 'ant-design:team-outlined',
      },
    },
    {
      path: 'audit-log',
      name: 'AuditLogViewer',
      component: () => import('@/views/system/audit-log/index.vue'),
      meta: {
        title: 'Nhật ký thao tác',
        icon: 'ant-design:history-outlined',
      },
    },
  ],
};

export default system;
