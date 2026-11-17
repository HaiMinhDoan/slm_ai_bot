<template>
  <div>
    <div v-for="row in rows" :key="row.uid" class="link-row">
      <Select
        v-model:value="row.targetType"
        :options="options(TARGET_LABELS)"
        style="width: 110px"
        @change="resetTarget(row)"
      />
      <Select
        :value="row.targetKey"
        showSearch
        :filterOption="false"
        :options="row.options"
        :loading="row.loading"
        placeholder="Gõ để tìm"
        :notFoundContent="row.loading ? 'Đang tìm…' : 'Không có'"
        class="target"
        @search="(kw) => searchTargets(row, kw)"
        @focus="() => !row.options.length && searchTargets(row, '')"
        @change="(v, opt) => selectTarget(row, v, opt)"
      />
      <Select v-model:value="row.role" :options="roleOptions" style="width: 170px" />
      <InputNumber v-model:value="row.sortOrder" :min="0" style="width: 80px" placeholder="Thứ tự" />
      <Button type="link" danger @click="removeRow(row)">Gỡ</Button>
    </div>
    <Button preIcon="ant-design:plus-outlined" @click="addRow">Thêm liên kết</Button>
  </div>
</template>

<script lang="ts" setup>
  import { computed, watch } from 'vue';
  import { InputNumber, Select } from 'ant-design-vue';
  import { useDebounceFn } from '@vueuse/core';
  import { Button } from '@/components/Button';
  import { useMessage } from '@/hooks/web/useMessage';
  import { mediaLinkTargetsApi } from '@/api/media';
  import type { LinkRole } from '@/api/media/model/mediaModel';
  import { IMAGE_ONLY_ROLES, ROLE_LABELS, TARGET_LABELS, options } from './media.data';
  import { newLinkRow, type LinkRow } from './linkRows';

  /**
   * Sửa danh sách liên kết của media với gói / vật tư / hãng / tài liệu. Chọn gói ở miền này thì thêm gói cùng tên
   * ở miền kia. mediaType VIDEO thì ẩn vai trò chỉ dành cho ảnh; không truyền (thêm nhiều loại file) thì hiện hết,
   * backend kiểm tra từng media.
   */
  const props = defineProps<{ mediaType?: 'IMAGE' | 'VIDEO' }>();
  const rows = defineModel<LinkRow[]>('rows', { required: true });
  const { createMessage } = useMessage();

  const roleOptions = computed(() =>
    options(ROLE_LABELS).filter(
      (o) => props.mediaType !== 'VIDEO' || !IMAGE_ONLY_ROLES.includes(o.value as LinkRole),
    ),
  );

  function addRow() {
    rows.value = [...rows.value, newLinkRow({ role: props.mediaType === 'IMAGE' ? 'GALLERY' : 'RELATED' })];
  }

  // Thành video (ví dụ vừa chọn file video): vai trò chỉ dành cho ảnh đổi thành Liên quan
  watch(
    () => props.mediaType,
    (type) => {
      if (type !== 'VIDEO') return;
      rows.value.forEach((r) => {
        if (IMAGE_ONLY_ROLES.includes(r.role)) r.role = 'RELATED';
      });
    },
  );

  function removeRow(row: LinkRow) {
    rows.value = rows.value.filter((r) => r.uid !== row.uid);
  }

  function resetTarget(row: LinkRow) {
    row.targetKey = undefined;
    row.options = [];
  }

  const searchTargets = useDebounceFn(async (row: LinkRow, keyword: string) => {
    row.loading = true;
    try {
      const found = await mediaLinkTargetsApi(row.targetType, keyword || undefined);
      row.options = found.map((t) => ({
        value: t.key,
        label: t.label,
        twinKey: t.twinKey,
        twinLabel: t.twinLabel,
      }));
    } finally {
      row.loading = false;
    }
  }, 300);

  function selectTarget(row: LinkRow, value: any, option: any) {
    row.targetKey = value as string;
    const twinKey: string | undefined = option?.twinKey;
    const exists = (key: string) =>
      rows.value.some((r) => r.targetType === 'PACKAGE' && r.targetKey === key);
    if (row.targetType === 'PACKAGE' && twinKey && !exists(twinKey)) {
      rows.value = [
        ...rows.value,
        newLinkRow({
          targetKey: twinKey,
          role: row.role,
          sortOrder: row.sortOrder,
          options: [{ value: twinKey, label: option.twinLabel ?? twinKey }],
        }),
      ];
      createMessage.info('Đã thêm gói cùng tên ở miền kia: ' + (option.twinLabel ?? twinKey));
    }
  }
</script>

<style lang="less" scoped>
  .link-row {
    display: flex;
    gap: 8px;
    margin-bottom: 8px;

    .target {
      flex: 1;
      min-width: 0;
    }
  }
</style>
