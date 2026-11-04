<template>
  <PageWrapper title="Quy tắc" contentBackground contentClass="p-4">
    <template #headerContent>
      Quy tắc cho code đọc (không đưa nguyên vào prompt) và hướng dẫn hệ thống cho AI. Sửa xong là bản nháp;
      bot chỉ dùng sau khi phát hành và kích hoạt ở
      <a @click="go('/knowledge/releases')">trang Phát hành</a>.
    </template>
    <Spin :spinning="loading">
      <Tabs v-model:activeKey="tab">
        <TabPane v-if="rules['bao-gia']" key="bao-gia" tab="Báo giá">
          <RuleFormTab :detail="rules['bao-gia']" :def="PRICING" @saved="reload" />
        </TabPane>
        <TabPane v-if="rules['phan-loai-khach']" key="phan-loai-khach" tab="Phân loại khách">
          <RuleFormTab :detail="rules['phan-loai-khach']" :def="LEAD_SCORING" @saved="reload" />
        </TabPane>
        <TabPane v-if="rules['tinh-mien']" key="tinh-mien" tab="Tỉnh → miền → bảng giá">
          <RegionsTab :detail="rules['tinh-mien']" @saved="reload" />
        </TabPane>
        <TabPane v-if="rules['kiem-tra-truoc-khi-gui']" key="kiem-tra" tab="Kiểm tra trước khi gửi">
          <RuleFormTab :detail="rules['kiem-tra-truoc-khi-gui']" :def="REPLY_POLICY" @saved="reload" />
        </TabPane>
        <TabPane v-if="prompt" key="prompt" tab="Hướng dẫn hệ thống cho AI">
          <PromptTab :detail="prompt" @saved="reload" />
        </TabPane>
        <TabPane key="slots" tab="Thông tin cần hỏi">
          <ConsultSlotsTab />
        </TabPane>
      </Tabs>
    </Spin>
  </PageWrapper>
</template>

<script lang="ts" setup>
  import { onMounted, ref } from 'vue';
  import { Spin, Tabs } from 'ant-design-vue';
  import { PageWrapper } from '@/components/Page';
  import { useGo } from '@/hooks/web/usePage';
  import { knowledgeDocByKeyApi, knowledgeRulesApi } from '@/api/knowledge';
  import type { KnowledgeDocDetail } from '@/api/knowledge/model/knowledgeModel';
  import RuleFormTab from './RuleFormTab.vue';
  import RegionsTab from './RegionsTab.vue';
  import PromptTab from './PromptTab.vue';
  import ConsultSlotsTab from './ConsultSlotsTab.vue';
  import { LEAD_SCORING, PRICING, REPLY_POLICY } from './rules.data';

  defineOptions({ name: 'KnowledgeRules' });

  const TabPane = Tabs.TabPane;
  const go = useGo();
  const tab = ref('bao-gia');
  const loading = ref(false);
  /** Theo tên rule: bao-gia, phan-loai-khach, tinh-mien, kiem-tra-truoc-khi-gui */
  const rules = ref<Record<string, KnowledgeDocDetail>>({});
  const prompt = ref<KnowledgeDocDetail>();

  async function reload() {
    loading.value = true;
    try {
      const [list, p] = await Promise.all([knowledgeRulesApi(), knowledgeDocByKeyApi('prompts/customer-chat')]);
      rules.value = Object.fromEntries(list.map((d) => [d.doc.key.replace('rules/', ''), d]));
      prompt.value = p;
    } finally {
      loading.value = false;
    }
  }

  onMounted(reload);
</script>
