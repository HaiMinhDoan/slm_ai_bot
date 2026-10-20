<template>
  <div ref="chartRef" :style="{ width: '100%', height }" role="img" :aria-label="label"></div>
</template>

<script lang="ts" setup>
  /** Bọc useECharts: nhận option sẵn, vẽ lại mỗi khi option đổi (đổi khoảng ngày, bật chế độ tối) */
  import type { EChartsOption } from 'echarts';
  import { ref, watch, type Ref } from 'vue';
  import { useECharts } from '@/hooks/web/useECharts';

  const props = withDefaults(defineProps<{ option: EChartsOption; height?: string; label?: string }>(), {
    height: '300px',
    label: '',
  });

  const chartRef = ref<HTMLDivElement | null>(null);
  const { setOptions } = useECharts(chartRef as Ref<HTMLDivElement>);

  // clear = true: đổi từ 30 sang 7 ngày thì bỏ hẳn trục cũ, không trộn với dữ liệu trước
  watch(
    () => props.option,
    (o) => setOptions(o, true),
    { immediate: true, deep: true },
  );
</script>
