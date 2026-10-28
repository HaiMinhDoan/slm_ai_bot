import type { AppRouteModule } from '@/router/types';

import { LAYOUT } from '@/router/constant';
import { RoleEnum } from '@/enums/roleEnum';

/** Catalog đồng bộ từ slm_v2, thử báo giá, bảng PV out EVN (mục 4.2, 60.3) */
const catalog: AppRouteModule = {
  path: '/catalog',
  name: 'Catalog',
  component: LAYOUT,
  redirect: '/catalog/index',
  meta: {
    hideChildrenInMenu: true,
    orderNo: 35,
    icon: 'ant-design:shopping-outlined',
    title: 'Catalog & báo giá',
    roles: [RoleEnum.SYSTEM_ADMIN, RoleEnum.CONTENT_ADMIN, RoleEnum.STAFF],
  },
  children: [
    {
      path: 'index',
      name: 'CatalogPricing',
      component: () => import('@/views/catalog/index.vue'),
      meta: {
        title: 'Catalog & báo giá',
        icon: 'ant-design:shopping-outlined',
        hideMenu: true,
      },
    },
  ],
};

export default catalog;
