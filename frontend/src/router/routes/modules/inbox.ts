import type { AppRouteModule } from '@/router/types';

import { LAYOUT } from '@/router/constant';
import { RoleEnum } from '@/enums/roleEnum';

/** Trang "Chat với khách": hội thoại theo kênh, chuyển AI / nhân viên, xử lý nháp AI (STAFF, SYSTEM_ADMIN) */
const inbox: AppRouteModule = {
  path: '/inbox',
  name: 'Inbox',
  component: LAYOUT,
  redirect: '/inbox/index',
  meta: {
    hideChildrenInMenu: true,
    orderNo: 15,
    icon: 'ant-design:message-outlined',
    title: 'Chat với khách',
    roles: [RoleEnum.SYSTEM_ADMIN, RoleEnum.STAFF],
  },
  children: [
    {
      path: 'index',
      name: 'InboxList',
      component: () => import('@/views/inbox/index.vue'),
      meta: {
        title: 'Chat với khách',
        icon: 'ant-design:message-outlined',
        hideMenu: true,
      },
    },
  ],
};

export default inbox;
