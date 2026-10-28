<template>
  <PageWrapper dense contentFullHeight>
    <Tabs v-model:activeKey="tab" class="catalog-tabs">
      <TabPane key="packages" tab="Gói đồng bộ từ slm_v2">
        <PackagesTab />
      </TabPane>
      <TabPane key="advise" tab="Thử báo giá">
        <AdviseTab />
      </TabPane>
      <TabPane key="pvout" tab="PV out EVN">
        <PvoutTab />
      </TabPane>
    </Tabs>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { ref, watch } from 'vue';
  import { useRoute } from 'vue-router';
  import { Tabs } from 'ant-design-vue';
  import { PageWrapper } from '@/components/Page';
  import PackagesTab from './PackagesTab.vue';
  import AdviseTab from './AdviseTab.vue';
  import PvoutTab from './PvoutTab.vue';

  defineOptions({ name: 'CatalogPricing' });

  const TabPane = Tabs.TabPane;
  const TABS = ['packages', 'advise', 'pvout'];

  const route = useRoute();
  const tab = ref('packages');
  watch(
    () => route.query.tab,
    (t) => {
      if (typeof t === 'string' && TABS.includes(t)) tab.value = t;
    },
    { immediate: true },
  );
</script>

<style lang="less" scoped>
  .catalog-tabs {
    padding: 0 12px 12px;
    background: #fff;
  }
</style>
