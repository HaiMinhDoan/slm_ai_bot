<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="title" width="900px" :showOkBtn="false">
    <div v-if="version" class="version-box">
      <CodeEditor
        v-if="yaml"
        :value="version.content"
        :mode="MODE.YAML"
        :config="CODE_CONFIG"
        readonly
        bordered
      />
      <MarkdownViewer v-else :value="version.content" />
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { BasicModal, useModalInner } from '@/components/Modal';
  import { MarkdownViewer } from '@/components/Markdown';
  import { CodeEditor, MODE } from '@/components/CodeEditor';
  import { CODE_CONFIG } from './codeEditor';
  import { knowledgeVersionApi } from '@/api/knowledge';
  import type { KnowledgeVersion } from '@/api/knowledge/model/knowledgeModel';

  defineOptions({ name: 'KnowledgeVersionModal' });

  /** Mở bằng openModal(true, { versionId, key, yaml }) */
  defineEmits(['register']);
  const version = ref<KnowledgeVersion>();
  const key = ref('');
  const yaml = ref(false);
  const title = computed(() =>
    version.value ? `${key.value} · v${version.value.versionNo}: ${version.value.title}` : 'Phiên bản',
  );

  const [registerModal, { changeLoading }] = useModalInner(async (data) => {
    version.value = undefined;
    key.value = data.key;
    yaml.value = !!data.yaml;
    changeLoading(true);
    try {
      version.value = await knowledgeVersionApi(data.versionId);
    } finally {
      changeLoading(false);
    }
  });
</script>

<style lang="less" scoped>
  .version-box {
    height: 65vh;
    overflow: auto;

    :deep(.CodeMirror) {
      height: 100%;
    }
  }
</style>
