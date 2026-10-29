import type { AppRouteModule } from '@/router/types';

import { LAYOUT } from '@/router/constant';
import { RoleEnum } from '@/enums/roleEnum';

/** Catalog đồng bộ từ slm_v2, thử báo giá, bảng PV out EVN (mục 4.2, 60.3) */
const catalog: AppRouteModule = {
  path: '/catalog',
  name: 'Catalog',
  component: LAYOUT,
  redirect: '/catalog/packages',
  meta: {
    orderNo: 35,
    icon: 'ant-design:shopping-outlined',
    title: 'Catalog & báo giá',
    roles: [RoleEnum.SYSTEM_ADMIN, RoleEnum.CONTENT_ADMIN, RoleEnum.STAFF],
  },
  children: [
    {
      path: 'packages',
      name: 'CatalogPackages',
      component: () => import('@/views/catalog/packages/index.vue'),
      meta: {
        title: 'Gói từ slm_v2',
        icon: 'ant-design:appstore-outlined',
      },
    },
    {
      path: 'advise',
      name: 'CatalogAdvise',
      component: () => import('@/views/catalog/advise/index.vue'),
      meta: {
        title: 'Thử báo giá',
        icon: 'ant-design:calculator-outlined',
      },
    },
    {
      path: 'pvout',
      name: 'CatalogPvout',
      component: () => import('@/views/catalog/pvout/index.vue'),
      meta: {
        title: 'PV out EVN',
        icon: 'ant-design:line-chart-outlined',
      },
    },
  ],
};

export default catalog;
