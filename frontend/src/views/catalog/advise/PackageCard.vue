<template>
  <Card size="small" :title="heading ?? pkg.ten">
    <template #extra>
      <a @click="emit('detail', pkg.goi_id)">Chi tiết</a>
    </template>
    <div v-if="heading" class="name">{{ pkg.ten }}</div>
    <div class="price">{{ pkg.gia }}</div>
    <div class="line">{{ pkg.loai_he_thong }} · {{ pkg.so_pha }} · {{ pkg.cong_suat }}</div>
    <div v-if="pkg.tam_pin" class="line">Tấm pin: {{ pkg.tam_pin }}</div>
    <div v-if="pkg.inverter" class="line">Inverter: {{ pkg.inverter }}</div>
    <div v-if="pkg.luu_tru" class="line">
      Pin lưu trữ: {{ pkg.luu_tru
      }}<span v-if="pkg.hang_pin_luu_tru"> {{ pkg.hang_pin_luu_tru }}</span>
    </div>
    <template v-if="pkg.san_luong_uoc_tinh">
      <div class="line">
        Sản lượng: {{ pkg.san_luong_uoc_tinh.trung_binh }} ({{
          pkg.san_luong_uoc_tinh.thang_thap_nhat
        }}
        đến {{ pkg.san_luong_uoc_tinh.thang_cao_nhat }})
      </div>
    </template>
    <div v-if="pkg.tiet_kiem_uoc_tinh" class="line">
      Tiết kiệm: {{ pkg.tiet_kiem_uoc_tinh.moi_thang_trung_binh }}/tháng · hoàn vốn
      {{ pkg.tiet_kiem_uoc_tinh.hoan_von }}
    </div>
    <div v-if="pkg.ly_do" class="muted">{{ pkg.ly_do }}</div>
    <div v-if="pkg.luu_y" class="warn">{{ pkg.luu_y }}</div>
    <div v-if="pkg.link" class="line">
      <a :href="pkg.link" target="_blank" rel="noopener noreferrer">{{ pkg.link }}</a>
    </div>
    <Tag v-if="pkg.ban_chay" color="gold" class="mt-1">Bán chạy</Tag>
  </Card>
</template>

<script lang="ts" setup>
  import { Card, Tag } from 'ant-design-vue';

  defineProps<{ pkg: Recordable; heading?: string }>();
  const emit = defineEmits<{ (e: 'detail', id: number): void }>();
</script>

<style lang="less" scoped>
  .name {
    margin-bottom: 4px;
    font-weight: 500;
  }

  .price {
    color: #d4380d;
    font-size: 16px;
    font-weight: 600;
  }

  .line {
    font-size: 13px;
  }

  .muted {
    margin-top: 4px;
    color: #888;
    font-size: 12px;
  }

  .warn {
    margin-top: 4px;
    color: #d46b08;
    font-size: 12px;
  }
</style>
