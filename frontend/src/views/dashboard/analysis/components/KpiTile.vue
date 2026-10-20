<template>
  <div class="kpi">
    <div class="kpi-title">
      <Icon :icon="icon" :size="18" class="kpi-icon" />
      <span>{{ title }}</span>
    </div>
    <div class="kpi-value">{{ display }}</div>
    <div class="kpi-foot">
      <!-- Tăng/giảm nói bằng mũi tên + chữ, không chỉ bằng màu -->
      <span v-if="delta" class="kpi-delta" :class="`kpi-delta--${delta.dir}`" :title="`Kỳ trước: ${previousText}`">
        <Icon :icon="delta.icon" :size="12" />
        {{ delta.text }}
      </span>
      <span class="kpi-sub">{{ sub }}</span>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import Icon from '@/components/Icon/Icon.vue';
  import type { Kpi } from '@/api/dashboard';

  const props = withDefaults(
    defineProps<{
      title: string;
      icon: string;
      /** Có kpi thì hiện thêm tăng/giảm so với kỳ trước */
      kpi?: Kpi;
      value?: number | string;
      format?: (v: number) => string;
      sub?: string;
    }>(),
    { sub: '' },
  );

  const fmt = (v: number) => (props.format ? props.format(v) : v.toLocaleString('vi-VN'));

  const display = computed(() => {
    const v = props.kpi ? props.kpi.current : props.value;
    if (typeof v === 'number') return fmt(v);
    return v ?? '—';
  });

  const previousText = computed(() => (props.kpi ? fmt(props.kpi.previous) : ''));

  const delta = computed(() => {
    if (!props.kpi) return null;
    const { current, previous } = props.kpi;
    if (previous === 0) {
      return current === 0
        ? { dir: 'flat', icon: 'ant-design:minus-outlined', text: 'Không đổi' }
        : { dir: 'up', icon: 'ant-design:arrow-up-outlined', text: 'Mới có' };
    }
    const pct = ((current - previous) / previous) * 100;
    if (Math.abs(pct) < 0.05) return { dir: 'flat', icon: 'ant-design:minus-outlined', text: '0%' };
    return pct > 0
      ? { dir: 'up', icon: 'ant-design:arrow-up-outlined', text: `+${pct.toFixed(1)}%` }
      : { dir: 'down', icon: 'ant-design:arrow-down-outlined', text: `${pct.toFixed(1)}%` };
  });
</script>

<style lang="less" scoped>
  .kpi {
    display: flex;
    flex-direction: column;
    gap: 6px;
    height: 100%;
    padding: 16px 18px;
    border: 1px solid @border-color-base;
    border-radius: 10px;
    background: @component-background;
  }

  .kpi-title {
    display: flex;
    align-items: center;
    gap: 6px;
    color: @text-color-secondary;
    font-size: 13px;
  }

  .kpi-icon {
    opacity: 0.75;
  }

  .kpi-value {
    font-size: 26px;
    font-weight: 600;
    font-variant-numeric: tabular-nums;
    line-height: 1.2;
  }

  .kpi-foot {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 8px;
    min-height: 20px;
    font-size: 12px;
  }

  .kpi-delta {
    display: inline-flex;
    align-items: center;
    gap: 2px;
    padding: 0 6px;
    border-radius: 4px;
    font-weight: 600;
  }

  .kpi-delta--up {
    background: rgb(12 163 12 / 12%);
    color: #087a08;
  }

  .kpi-delta--down {
    background: rgb(208 59 59 / 12%);
    color: #b02f2f;
  }

  .kpi-delta--flat {
    background: rgb(137 135 129 / 15%);
    color: #6b6a65;
  }

  // Nền tối: cùng hai màu trạng thái nhưng bậc sáng hơn, chữ đậm trên nền tối vẫn đọc được
  html[data-theme='dark'] {
    .kpi-delta--up {
      background: rgb(12 163 12 / 22%);
      color: #5fd35f;
    }

    .kpi-delta--down {
      background: rgb(208 59 59 / 25%);
      color: #ff8a8a;
    }

    .kpi-delta--flat {
      color: #c3c2b7;
    }
  }

  .kpi-sub {
    color: @text-color-secondary;
  }
</style>
