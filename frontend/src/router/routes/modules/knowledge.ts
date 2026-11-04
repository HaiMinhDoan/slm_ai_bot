import type { AppRouteModule } from '@/router/types';

import { LAYOUT } from '@/router/constant';
import { RoleEnum } from '@/enums/roleEnum';

/** Kiến thức, quy tắc, phát hành và cài đặt AI (kế hoạch mục 5.2–5.10, 33.2) */
const knowledge: AppRouteModule = {
  path: '/knowledge',
  name: 'Knowledge',
  component: LAYOUT,
  redirect: '/knowledge/docs',
  meta: {
    orderNo: 30,
    icon: 'ant-design:read-outlined',
    title: 'Kiến thức & quy tắc',
    roles: [RoleEnum.SYSTEM_ADMIN, RoleEnum.CONTENT_ADMIN],
  },
  children: [
    {
      path: 'docs',
      name: 'KnowledgeDocs',
      component: () => import('@/views/knowledge/docs/index.vue'),
      meta: { title: 'Tài liệu kiến thức', icon: 'ant-design:file-text-outlined' },
    },
    {
      path: 'rules',
      name: 'KnowledgeRules',
      component: () => import('@/views/knowledge/rules/index.vue'),
      meta: { title: 'Quy tắc', icon: 'ant-design:control-outlined' },
    },
    {
      path: 'releases',
      name: 'KnowledgeReleases',
      component: () => import('@/views/knowledge/releases/index.vue'),
      meta: { title: 'Phát hành', icon: 'ant-design:cloud-upload-outlined' },
    },
    {
      path: 'settings',
      name: 'KnowledgeAiSettings',
      component: () => import('@/views/knowledge/settings/index.vue'),
      meta: { title: 'Cài đặt AI', icon: 'ant-design:setting-outlined' },
    },
  ],
};

export default knowledge;
