import type { App } from 'vue';
import { Button } from './Button';
import { Input, Layout } from 'ant-design-vue';
import VXETable from 'vxe-table';
import VXEUI from 'vxe-pc-ui';
import vxeEnUS from 'vxe-pc-ui/lib/language/en-US';

/**
 * vxe-table và vxe-pc-ui mặc định dùng tiếng Trung.
 *
 * Hai thư viện này được đăng ký toàn cục nên mọi bảng vxe (nút phân trang, ô
 * lọc, menu chuột phải, thông báo "không có dữ liệu"...) sẽ hiện tiếng Trung
 * nếu không khai báo ngôn ngữ. Gói ngôn ngữ của vxe chỉ có en-US, es-ES, ja-JP,
 * pt-BR và zh-CN — không có tiếng Việt — nên dùng tiếng Anh.
 *
 * Đặt ở ngoài hàm để chạy ngay khi nạp module, trước khi bất kỳ component vxe
 * nào được dựng.
 */
VXEUI.setI18n('en-US', vxeEnUS);
VXEUI.setLanguage('en-US');

export function registerGlobComp(app: App) {
  app.use(Input).use(Button).use(Layout).use(VXETable).use(VXEUI);
}
