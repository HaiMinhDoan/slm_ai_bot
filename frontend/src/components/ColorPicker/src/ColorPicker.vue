<template>
  <Input
    :style="{ width }"
    placeholder="#FFB99A"
    :class="prefixCls"
    v-model:value="currentValue"
    :allowClear="allowClear"
    @change="handleInputChange"
  >
    <template #addonBefore>
      <Popover
        placement="bottomLeft"
        trigger="click"
        v-model:open="visible"
        :overlayClassName="`${prefixCls}-popover`"
      >
        <template #content>
          <ul class="flex flex-wrap p-2" style="width: 220px">
            <li
              v-for="c in colors"
              :key="c.value"
              :class="[`${prefixCls}-swatch`, isSameColor(c.value) ? 'is-active' : '']"
              :style="{ background: c.value }"
              :title="c.label"
              @click="handlePick(c.value)"
            />
          </ul>
          <div :class="`${prefixCls}-custom`">
            <input type="color" :value="normalizedHex" @input="handleNativePick" />
            <span>Tuỳ chỉnh khác…</span>
          </div>
        </template>
        <div :class="`${prefixCls}-trigger`" :style="{ background: currentValue || '#ffffff' }" />
      </Popover>
    </template>
  </Input>
</template>
<script lang="ts">
  import { defineComponent } from 'vue';

  /**
   * Bảng màu mặc định lấy từ chính token thiết kế của portal (tokens.css) —
   * để CMS chọn màu vẫn khớp hệ màu đã dùng thật trên bản đồ đảo, không bịa số hex mới.
   */
  export const DEFAULT_COLORS = [
    { label: 'Xanh dương', value: '#006398' },
    { label: 'Xanh lá', value: '#006C4A' },
    { label: 'Cam đất', value: '#855300' },
    { label: 'Xanh da trời', value: '#5BB8FE' },
    { label: 'Xanh mint', value: '#50C594' },
    { label: 'Hổ phách', value: '#F59E0B' },
    { label: 'Đào', value: '#FFDDB8' },
    { label: 'Mint sáng', value: '#85F8C4' },
    { label: 'Da trời nhạt', value: '#CCE5FF' },
    { label: 'Oải hương', value: '#E7EEFF' },
  ];

  export default defineComponent({ name: 'ColorPicker' });
</script>
<script lang="ts" setup>
  import { ref, computed, watchEffect, watch } from 'vue';
  import { Input, Popover } from 'ant-design-vue';
  import { useDesign } from '@/hooks/web/useDesign';

  export interface Props {
    value?: string;
    width?: string;
    allowClear?: boolean;
    colors?: { label: string; value: string }[];
  }

  const props = withDefaults(defineProps<Props>(), {
    value: '',
    width: '100%',
    allowClear: true,
    colors: () => DEFAULT_COLORS,
  });

  defineOptions({ inheritAttrs: false });

  const emit = defineEmits(['change', 'update:value']);

  const { prefixCls } = useDesign('color-picker');

  const currentValue = ref('');
  const visible = ref(false);

  watchEffect(() => {
    currentValue.value = props.value ?? '';
  });

  watch(
    () => currentValue.value,
    (v) => {
      emit('update:value', v);
      emit('change', v);
    },
  );

  const normalizedHex = computed(() =>
    /^#[0-9a-fA-F]{6}$/.test(currentValue.value) ? currentValue.value : '#ffffff',
  );

  function isSameColor(v: string) {
    return currentValue.value?.toLowerCase() === v.toLowerCase();
  }

  function handlePick(v: string) {
    currentValue.value = v;
    visible.value = false;
  }

  function handleNativePick(e: Event) {
    currentValue.value = (e.target as HTMLInputElement).value;
  }

  function handleInputChange(e: Event) {
    currentValue.value = (e.target as HTMLInputElement).value;
  }
</script>
<style lang="less">
  @prefix-cls: ~'@{namespace}-color-picker';

  .@{prefix-cls} {
    .ant-input-group-addon {
      padding: 0 4px;
    }

    &-trigger {
      width: 22px;
      height: 22px;
      border-radius: 6px;
      border: 1px solid rgba(0, 0, 0, 0.15);
      cursor: pointer;
    }

    &-swatch {
      width: 28px;
      height: 28px;
      border-radius: 6px;
      margin: 4px;
      cursor: pointer;
      border: 2px solid transparent;
      list-style: none;

      &:hover {
        border-color: rgba(0, 0, 0, 0.25);
      }

      &.is-active {
        border-color: #1890ff;
      }
    }

    &-custom {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px;
      border-top: 1px solid #f0f0f0;

      input[type='color'] {
        width: 28px;
        height: 28px;
        padding: 0;
        border: none;
        background: none;
        cursor: pointer;
      }

      span {
        font-size: 12px;
        color: #666;
      }
    }

    &-popover .ant-popover-inner-content {
      padding: 4px 0;
    }
  }
</style>
