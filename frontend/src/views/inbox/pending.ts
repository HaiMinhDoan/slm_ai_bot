import { ref } from 'vue';

/**
 * Hội thoại cần mở khi vào trang Chat với khách (ví dụ từ trang Thông báo). Không dùng query ?id= vì thanh tab của
 * vben khoá theo cả query nên sinh thêm một tab trùng.
 */
export const pendingConversationId = ref<string>();

export function openConversationLater(id?: string) {
  if (id) pendingConversationId.value = id;
}
