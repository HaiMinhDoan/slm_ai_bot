<template>
  <div class="lg:flex">
    <Avatar :src="userinfo.avatar || headerImg" :size="72" class="!mx-auto !block" />
    <div class="md:ml-6 flex flex-col justify-center md:mt-0 mt-2">
      <h1 class="md:text-lg text-md">{{ greeting }}, {{ userinfo.realName }}!</h1>
      <span class="text-secondary"> Hôm nay học tiếp vài ký hiệu mới nhé. </span>
    </div>
    <div class="flex flex-1 justify-end md:mt-0 mt-4">
      <div class="flex flex-col justify-center text-right">
        <span class="text-secondary"> Bài hôm nay </span>
        <span class="text-2xl">2/10</span>
      </div>

      <div class="flex flex-col justify-center text-right md:mx-16 mx-12">
        <span class="text-secondary"> Chuỗi ngày học </span>
        <span class="text-2xl">8</span>
      </div>
      <div class="flex flex-col justify-center text-right md:mr-10 mr-4">
        <span class="text-secondary"> Từ đã thuộc </span>
        <span class="text-2xl">300</span>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
  import { computed } from 'vue';
  import { Avatar } from 'ant-design-vue';
  import { useUserStore } from '@/store/modules/user';
  import headerImg from '@/assets/images/header.jpg';

  const userStore = useUserStore();
  const userinfo = computed(() => userStore.getUserInfo);

  /**
   * Bản gốc của vben chào cứng "早安" (chào buổi sáng) kèm một dòng dự báo thời
   * tiết bịa sẵn. Dòng thời tiết đã bỏ hẳn vì nó là số liệu giả; lời chào thì
   * đổi theo giờ thật trên máy người dùng.
   *
   * Ba con số bên phải vẫn là số mẫu, chưa nối API. Khi dựng endpoint tiến độ
   * học thì thay bằng dữ liệu thật của người dùng.
   */
  const greeting = computed(() => {
    const hour = new Date().getHours();
    if (hour < 11) return 'Chào buổi sáng';
    if (hour < 13) return 'Chào buổi trưa';
    if (hour < 18) return 'Chào buổi chiều';
    return 'Chào buổi tối';
  });
</script>
