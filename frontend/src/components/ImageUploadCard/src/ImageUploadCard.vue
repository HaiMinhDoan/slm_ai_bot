<template>
  <div class="image-upload-card">
    <Upload
      v-model:file-list="fileList"
      list-type="picture-card"
      :accept="accept"
      :max-count="1"
      :disabled="disabled || uploading"
      :before-upload="beforeUpload"
      @preview="handlePreview"
      @remove="handleRemove"
    >
      <div v-if="!fileList.length">
        <LoadingOutlined v-if="uploading" />
        <PlusOutlined v-else />
        <div style="margin-top: 8px">{{ uploading ? 'Đang tải…' : text }}</div>
      </div>
    </Upload>

    <div v-if="hint" class="upload-hint">{{ hint }}</div>

    <Modal :open="previewOpen" :title="previewTitle" :footer="null" @cancel="previewOpen = false">
      <img :src="previewImage" :alt="previewTitle" style="width: 100%" />
    </Modal>
  </div>
</template>

<script lang="ts" setup>
  /**
   * Ô tải ảnh dạng thẻ (picture-card) dùng chung cho mọi chỗ upload ảnh trong CMS.
   *
   * Khác với thẻ Upload trần: ảnh hiện ngay thành thẻ vuông xem được, có nút xem to
   * và nút xoá, kèm một dòng nhắc định dạng — thay cho nút "Tải ảnh" nhỏ trước đây.
   *
   * Việc tải lên KHÔNG do component tự gửi: mỗi chỗ có endpoint riêng (ảnh bước,
   * ảnh đại diện video…), nên truyền vào hàm `upload`. Component chỉ lo phần nhìn:
   * kiểm tra định dạng/dung lượng, quay vòng chờ, và hỏi lại trước khi xoá.
   */
  import { computed, ref, watch } from 'vue';
  import { Modal, Upload } from 'ant-design-vue';
  import type { UploadFile } from 'ant-design-vue';
  import { LoadingOutlined, PlusOutlined } from '@ant-design/icons-vue';
  import { useMessage } from '@/hooks/web/useMessage';

  defineOptions({ name: 'ImageUploadCard' });

  const props = withDefaults(
    defineProps<{
      /** URL ảnh hiện có; rỗng thì hiện ô dấu cộng */
      value?: string;
      /** Gửi tệp lên server. Trả về promise để component biết lúc nào quay xong vòng chờ */
      upload: (file: File) => Promise<any>;
      /** Bỏ ảnh hiện có. Không truyền thì thẻ ảnh không có nút xoá */
      remove?: () => Promise<any>;
      text?: string;
      hint?: string;
      accept?: string;
      /** Giới hạn dung lượng, tính bằng MB */
      maxSize?: number;
      alt?: string;
      disabled?: boolean;
      /** Câu hỏi xác nhận trước khi xoá; để rỗng là xoá thẳng */
      confirmText?: string;
    }>(),
    {
      value: '',
      text: 'Tải ảnh lên',
      hint: 'Tối đa 1 ảnh, định dạng: JPG, PNG, GIF',
      accept: 'image/*',
      maxSize: 5,
      alt: 'Ảnh đã tải lên',
      disabled: false,
      confirmText: 'Xoá ảnh này?',
    },
  );

  const { createMessage } = useMessage();

  const uploading = ref(false);
  const previewOpen = ref(false);
  const previewImage = ref('');
  const previewTitle = computed(() => props.alt);

  const fileList = ref<UploadFile[]>([]);

  // Nguồn sự thật là URL từ server, không phải danh sách bên trong antd: tải xong
  // thì cha gọi lại API rồi truyền URL mới xuống, thẻ ảnh theo đó mà đổi.
  watch(
    () => props.value,
    (url) => {
      fileList.value = url
        ? [{ uid: url, name: url.slice(url.lastIndexOf('/') + 1), status: 'done', url }]
        : [];
    },
    { immediate: true },
  );

  async function beforeUpload(file: File) {
    if (!file.type.startsWith('image/')) {
      createMessage.error('Chỉ nhận tệp ảnh (JPG, PNG, GIF, WEBP)');
      return Upload.LIST_IGNORE;
    }
    if (file.size / 1024 / 1024 > props.maxSize) {
      createMessage.error(`Ảnh nặng quá ${props.maxSize}MB, chọn ảnh nhẹ hơn nhé`);
      return Upload.LIST_IGNORE;
    }

    uploading.value = true;
    try {
      await props.upload(file);
    } finally {
      uploading.value = false;
    }
    // Trả false để antd không tự gửi lên: đã gửi bằng API riêng ở trên
    return false;
  }

  async function handlePreview(file: UploadFile) {
    previewImage.value = file.url || '';
    previewOpen.value = !!previewImage.value;
  }

  /**
   * Trả false là antd giữ nguyên thẻ ảnh. Luôn trả false rồi tự gọi API xoá, vì nếu
   * để antd bỏ thẻ trước mà API lỗi thì màn hình nói đã xoá trong khi server vẫn còn ảnh.
   */
  function handleRemove() {
    if (!props.remove || props.disabled) {
      return false;
    }
    if (!props.confirmText) {
      void props.remove();
      return false;
    }
    Modal.confirm({
      title: props.confirmText,
      okText: 'Xoá',
      okType: 'danger',
      cancelText: 'Huỷ',
      onOk: () => props.remove?.(),
    });
    return false;
  }
</script>

<style lang="less" scoped>
  .image-upload-card {
    display: inline-block;
  }

  .upload-hint {
    max-width: 220px;
    margin-top: -4px;
    color: @text-color-secondary;
    font-size: 12px;
    line-height: 1.4;
  }
</style>
