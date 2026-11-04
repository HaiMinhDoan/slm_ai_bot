<template>
  <div>
    <RuleHeader
      :detail="detail"
      :description="def.description"
      :errors="(errors.length ? errors : detail.ruleErrors ?? []).map((e) => humanizeRuleError(e, def))"
      :saving="saving"
      @save="handleSave()"
      @discard="discard"
      @diff="diff"
      @yaml="handleYaml"
    />
    <BasicForm @register="registerForm" />
    <DiffModal @register="registerDiff" @ok="handleSave(true)" />
    <VersionModal @register="registerVersion" />
  </div>
</template>

<script lang="ts" setup>
  import { onMounted, watch } from 'vue';
  import { BasicForm, useForm } from '@/components/Form';
  import { useModal } from '@/components/Modal';
  import type { KnowledgeDocDetail } from '@/api/knowledge/model/knowledgeModel';
  import DiffModal from '../shared/DiffModal.vue';
  import VersionModal from '../shared/VersionModal.vue';
  import RuleHeader from './RuleHeader.vue';
  import { humanizeRuleError, type RuleFormDef } from './rules.data';
  import { useRuleSave } from './useRuleSave';

  defineOptions({ name: 'KnowledgeRuleFormTab' });

  const props = defineProps<{ detail: KnowledgeDocDetail; def: RuleFormDef }>();
  const emit = defineEmits(['saved']);

  const [registerDiff, { openModal: openDiffModal }] = useModal();
  const [registerVersion, { openModal: openVersion }] = useModal();
  const { saving, errors, save, discard, diff } = useRuleSave(
    props.def.name,
    () => props.detail,
    (d) => emit('saved', d),
    (data) => openDiffModal(true, data),
  );

  const [registerForm, { setFieldsValue, validate }] = useForm({
    labelWidth: 260,
    baseColProps: { span: 12 },
    schemas: props.def.schemas,
    showActionButtonGroup: false,
  });

  function fill() {
    if (props.detail.value) {
      setFieldsValue(props.def.toForm(props.detail.value));
    }
  }

  onMounted(fill);
  watch(() => props.detail, fill);

  async function handleSave(force = false) {
    const f = await validate();
    await save(props.def.toValue(f, props.detail.value ?? {}), force);
  }

  function handleYaml() {
    const v = props.detail.draft ?? props.detail.latest;
    if (v) openVersion(true, { versionId: v.id, key: props.detail.doc.key, yaml: true });
  }
</script>
