import { message } from 'ant-design-vue';

// `navigator.clipboard` có thể không dùng được do cấu hình trình duyệt hoặc do
// trình duyệt cũ, nên vẫn cần đường dự phòng bằng document.execCommand
export function copyText(text: string, prompt: string | null = 'Đã sao chép!') {
  if (navigator.clipboard) {
    return navigator.clipboard
      .writeText(text)
      .then(() => {
        prompt && message.success(prompt);
      })
      .catch((error) => {
        message.error('Sao chép thất bại! ' + error.message);
        return error;
      });
  }
  if (Reflect.has(document, 'execCommand')) {
    return new Promise<void>((resolve, reject) => {
      try {
        const textArea = document.createElement('textarea');
        textArea.value = text;
        // Trên Safari của điện thoại, bấm nút sao chép làm cả trang giật một cái
        textArea.style.width = '0';
        textArea.style.position = 'fixed';
        textArea.style.left = '-999px';
        textArea.style.top = '10px';
        textArea.setAttribute('readonly', 'readonly');
        document.body.appendChild(textArea);
        textArea.select();
        document.execCommand('copy');
        document.body.removeChild(textArea);

        prompt && message.success(prompt);
        resolve();
      } catch (error) {
        message.error('Sao chép thất bại! ' + error.message);
        reject(error);
      }
    });
  }
  return Promise.reject(
    'Trình duyệt không hỗ trợ "navigator.clipboard" lẫn "document.execCommand", không sao chép được!',
  );
}
