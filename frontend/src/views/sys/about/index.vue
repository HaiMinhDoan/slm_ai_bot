<template>
  <PageWrapper title="Giới thiệu">
    <template #headerContent>
      <div class="flex justify-between items-center">
        <span class="flex-1">
          SignAI là nền tảng học Ngôn ngữ ký hiệu Việt Nam (VSL): tra cứu từ vựng theo chủ đề và
          vùng miền, học theo bài, luyện tập trước camera và trao đổi trong diễn đàn. Giao diện quản
          trị dựng trên
          <a :href="GITHUB_URL" target="_blank">{{ name }}</a>
          — Vue 3, Vite, Ant Design Vue và TypeScript.
        </span>
      </div>
    </template>
    <Description @register="infoRegister" class="enter-y" />
    <Description @register="register" class="my-4 enter-y" />
    <Description @register="registerDev" class="enter-y" />
  </PageWrapper>
</template>
<script lang="ts" setup>
  import { h } from 'vue';
  import { Tag } from 'ant-design-vue';
  import { PageWrapper } from '@/components/Page';
  import { Description, DescItem, useDescription } from '@/components/Description';
  import { GITHUB_URL, SITE_URL, DOC_URL } from '@/settings/siteSetting';

  const { pkg, lastBuildTime } = __APP_INFO__;

  const { dependencies, devDependencies, name, version } = pkg;

  const schema: DescItem[] = [];
  const devSchema: DescItem[] = [];

  const commonTagRender = (color: string) => (curVal) => h(Tag, { color }, () => curVal);
  const commonLinkRender = (text: string) => (href) => h('a', { href, target: '_blank' }, text);

  const infoSchema: DescItem[] = [
    {
      label: 'Phiên bản',
      field: 'version',
      render: commonTagRender('blue'),
    },
    {
      label: 'Lần build gần nhất',
      field: 'lastBuildTime',
      render: commonTagRender('blue'),
    },
    {
      label: 'Tài liệu',
      field: 'doc',
      render: commonLinkRender('Xem tài liệu'),
    },
    {
      label: 'Bản xem thử',
      field: 'preview',
      render: commonLinkRender('Mở bản xem thử'),
    },
    {
      label: 'Github',
      field: 'github',
      render: commonLinkRender('Github'),
    },
  ];

  const infoData = {
    version,
    lastBuildTime,
    doc: DOC_URL,
    preview: SITE_URL,
    github: GITHUB_URL,
  };

  Object.keys(dependencies).forEach((key) => {
    schema.push({ field: key, label: key });
  });

  Object.keys(devDependencies).forEach((key) => {
    devSchema.push({ field: key, label: key });
  });

  const [register] = useDescription({
    title: 'Thư viện chạy thật',
    data: dependencies,
    schema: schema,
    column: 3,
  });

  const [registerDev] = useDescription({
    title: 'Thư viện dùng khi phát triển',
    data: devDependencies,
    schema: devSchema,
    column: 3,
  });

  const [infoRegister] = useDescription({
    title: 'Thông tin dự án',
    data: infoData,
    schema: infoSchema,
    column: 2,
  });
</script>
