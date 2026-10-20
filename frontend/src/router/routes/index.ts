import type { AppRouteRecordRaw, AppRouteModule } from '@/router/types';

import { PAGE_NOT_FOUND_ROUTE, REDIRECT_ROUTE } from '@/router/routes/basic';

import { mainOutRoutes } from './mainOut';
import { PageEnum } from '@/enums/pageEnum';
import { t } from '@/hooks/web/useI18n';

// Nạp tất cả module route bằng import.meta.glob (tính năng riêng của Vite).
//
// Loại trừ demo, form-design và hooks: đó là các trang trình diễn của
// vue-vben-admin, toàn bộ nội dung bằng tiếng Trung (hơn 2.000 dòng) và SignAI
// không dùng tới. Bỏ route đi thì chúng biến khỏi menu, khỏi trang web, và
// cũng không còn bị đóng gói vào bản build — nhanh và gọn hơn nhiều so với
// việc dịch từng trang sang tiếng Việt rồi vẫn xoá.
//
// Tệp nguồn vẫn nằm nguyên trong src/views để tham khảo. Muốn bật lại trang
// nào, xoá dòng loại trừ tương ứng bên dưới.
const modules = import.meta.glob(
  [
    './modules/**/*.ts',
    '!./modules/demo/**',
    '!./modules/form-design/**',
    '!./modules/hooks/**',
  ],
  { eager: true },
);
const routeModuleList: AppRouteModule[] = [];

// 加入到路由集合中
Object.keys(modules).forEach((key) => {
  const mod = (modules as Recordable)[key].default || {};
  const modList = Array.isArray(mod) ? [...mod] : [mod];
  routeModuleList.push(...modList);
});

export const asyncRoutes = [PAGE_NOT_FOUND_ROUTE, ...routeModuleList];

// 根路由
export const RootRoute: AppRouteRecordRaw = {
  path: '/',
  name: 'Root',
  redirect: PageEnum.BASE_HOME,
  meta: {
    title: 'Root',
  },
};

export const LoginRoute: AppRouteRecordRaw = {
  path: '/login',
  name: 'Login',
  component: () => import('@/views/sys/login/Login.vue'),
  meta: {
    title: t('routes.basic.login'),
  },
};

// Basic routing without permission
// 未经许可的基本路由
export const basicRoutes = [
  LoginRoute,
  RootRoute,
  ...mainOutRoutes,
  REDIRECT_ROUTE,
  PAGE_NOT_FOUND_ROUTE,
];
