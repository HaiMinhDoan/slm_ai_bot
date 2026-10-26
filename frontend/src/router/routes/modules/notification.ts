import type { AppRouteModule } from '@/router/types';

import { LAYOUT } from '@/router/constant';
import { RoleEnum } from '@/enums/roleEnum';

/** Trang "Thông báo": nháp AI chờ duyệt, chuyển nhân viên, khách HOT... (STAFF, SYSTEM_ADMIN) */
const notification: AppRouteModule = {
  path: '/notifications',
  name: 'Notifications',
  component: LAYOUT,
  redirect: '/notifications/index',
  meta: {
    hideChildrenInMenu: true,
    orderNo: 20,
    icon: 'ant-design:bell-outlined',
    title: 'Thông báo',
    roles: [RoleEnum.SYSTEM_ADMIN, RoleEnum.STAFF],
  },
  children: [
    {
      path: 'index',
      name: 'NotificationList',
      component: () => import('@/views/notification/index.vue'),
      meta: {
        title: 'Thông báo',
        icon: 'ant-design:bell-outlined',
        hideMenu: true,
      },
    },
  ],
};

export default notification;
