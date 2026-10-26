import type { AppRouteModule } from '@/router/types';

import { LAYOUT } from '@/router/constant';
import { RoleEnum } from '@/enums/roleEnum';

/** Thư viện media: ảnh / video combo, nhập từ Google Drive (SYSTEM_ADMIN, CONTENT_ADMIN) */
const media: AppRouteModule = {
  path: '/media',
  name: 'Media',
  component: LAYOUT,
  redirect: '/media/index',
  meta: {
    hideChildrenInMenu: true,
    orderNo: 40,
    icon: 'ant-design:picture-outlined',
    title: 'Thư viện media',
    roles: [RoleEnum.SYSTEM_ADMIN, RoleEnum.CONTENT_ADMIN],
  },
  children: [
    {
      path: 'index',
      name: 'MediaLibrary',
      component: () => import('@/views/media/index.vue'),
      meta: {
        title: 'Thư viện media',
        icon: 'ant-design:picture-outlined',
        hideMenu: true,
      },
    },
  ],
};

export default media;
