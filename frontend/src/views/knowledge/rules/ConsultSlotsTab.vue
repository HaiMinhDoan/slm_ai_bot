<template>
  <div>
    <Alert type="info" show-icon class="mb-3" message="Chỉ xem" :description="data?.note" />
    <Description title="Thông tin AI cần hỏi khi tư vấn" :column="1" :data="slotData" :schema="schema" />
    <Description
      class="mt-4"
      title="Tín hiệu phân loại khách"
      :column="1"
      :data="qualificationData"
      :schema="schema2"
    />
  </div>
</template>

<script lang="ts" setup>
  import { computed, onMounted, reactive, ref } from 'vue';
  import { Alert } from 'ant-design-vue';
  import { Description } from '@/components/Description';
  import type { DescItem } from '@/components/Description';
  import { consultSlotsApi } from '@/api/knowledge';
  import type { ConsultSlot, ConsultSlotsModel } from '@/api/knowledge/model/knowledgeModel';

  defineOptions({ name: 'KnowledgeConsultSlotsTab' });

  const data = ref<ConsultSlotsModel>();

  const describe = (s: ConsultSlot) =>
    [s.required ? 'Bắt buộc (tối thiểu để báo giá)' : 'Không bắt buộc', s.values.join(' · ')]
      .filter((x) => x)
      .join(' — ');

  const toSchema = (list?: ConsultSlot[]): DescItem[] =>
    (list ?? []).map((s) => ({ field: s.key, label: `${s.label} (${s.key})` }));
  // Description của vben gọi toRefs(data): đưa reactive để khỏi cảnh báo
  const toData = (list?: ConsultSlot[]) =>
    reactive(Object.fromEntries((list ?? []).map((s) => [s.key, describe(s)])));

  const schema = computed(() => toSchema(data.value?.slots));
  const slotData = computed(() => toData(data.value?.slots));
  const schema2 = computed(() => toSchema(data.value?.qualification));
  const qualificationData = computed(() => toData(data.value?.qualification));

  onMounted(async () => {
    data.value = await consultSlotsApi();
  });
</script>
