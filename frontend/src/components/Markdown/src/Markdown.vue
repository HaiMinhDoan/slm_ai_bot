<template>
  <div ref="wrapRef"></div>
</template>
<script lang="ts" setup>
  import './adapter.js';
  import type { Ref } from 'vue';
  import {
    ref,
    unref,
    nextTick,
    computed,
    watch,
    onBeforeUnmount,
    onDeactivated,
    useAttrs,
  } from 'vue';
  import Vditor from 'vditor';
  import 'vditor/dist/index.css';
  import { useModalContext } from '../../Modal';
  import { useRootSetting } from '@/hooks/setting/useRootSetting';
  import { onMountedOrActivated } from '@vben/hooks';
  import { getTheme } from './getTheme';

  type Lang = 'zh_CN' | 'en_US' | 'ja_JP' | 'ko_KR' | undefined;

  defineOptions({ inheritAttrs: false });

  const props = defineProps({
    height: { type: Number, default: 360 },
    value: { type: String, default: '' },
  });

  const emit = defineEmits(['change', 'get', 'update:value']);

  const attrs = useAttrs();

  const wrapRef = ref(null);
  const vditorRef = ref(null) as Ref<Vditor | null>;
  const initedRef = ref(false);

  const modalFn = useModalContext();

  const { getDarkMode } = useRootSetting();
  const valueRef = ref(props.value || '');

  watch(
    [() => getDarkMode.value, () => initedRef.value],
    ([val, inited]) => {
      if (!inited) {
        return;
      }
      instance
        .getVditor()
        ?.setTheme(getTheme(val) as any, getTheme(val, 'content'), getTheme(val, 'code'));
    },
    {
      immediate: true,
      flush: 'post',
    },
  );

  watch(
    () => props.value,
    (v) => {
      if (v !== valueRef.value) {
        instance.getVditor()?.setValue(v);
      }
      valueRef.value = v;
    },
  );

  // Vditor chỉ có gói ngôn ngữ zh_CN / en_US / ja_JP / ko_KR — không có tiếng
  // Việt. Trang này chỉ còn tiếng Việt và tiếng Anh, nên thanh công cụ soạn
  // thảo luôn dùng en_US. Mặc định cũ là zh_CN, để nguyên thì người dùng Việt
  // sẽ thấy nút bấm bằng tiếng Trung.
  const getCurrentLang = computed((): Lang => 'en_US');

  function init() {
    const wrapEl = unref(wrapRef);
    if (!wrapEl) return;
    const bindValue = { ...attrs, ...props };
    const insEditor = new Vditor(wrapEl, {
      // 设置外观主题
      theme: getTheme(getDarkMode.value) as any,
      lang: unref(getCurrentLang),
      mode: 'sv',
      fullscreen: {
        index: 520,
      },
      preview: {
        theme: {
          // 设置内容主题
          current: getTheme(getDarkMode.value, 'content'),
        },
        hljs: {
          // 设置代码块主题
          style: getTheme(getDarkMode.value, 'code'),
        },
        actions: [],
      },
      input: (v) => {
        valueRef.value = v;
        emit('update:value', v);
        emit('change', v);
      },
      after: () => {
        nextTick(() => {
          modalFn?.redoModalHeight?.();
          insEditor.setValue(valueRef.value);
          vditorRef.value = insEditor;
          initedRef.value = true;
          emit('get', instance);
        });
      },
      blur: () => {
        //unref(vditorRef)?.setValue(props.value);
      },
      ...bindValue,
      cache: {
        enable: false,
      },
    });
  }

  const instance = {
    getVditor: (): Vditor => vditorRef.value!,
  };

  function destroy() {
    const vditorInstance = unref(vditorRef);
    if (!vditorInstance) return;
    try {
      vditorInstance?.destroy?.();
    } catch (error) {
      //
    }
    vditorRef.value = null;
    initedRef.value = false;
  }

  onMountedOrActivated(init);

  onBeforeUnmount(destroy);
  onDeactivated(destroy);
</script>
