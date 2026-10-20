import type { DropMenu } from '../components/Dropdown';
import type { LocaleSetting, LocaleType } from '#/config';

export const LOCALE: { [key: string]: LocaleType } = {
  VI_VN: 'vi_VN',
  EN_US: 'en',
};

export const localeSetting: LocaleSetting = {
  showPicker: true,
  // Ngôn ngữ mặc định: tiếng Việt — người dùng của SignAI là người Việt
  locale: LOCALE.VI_VN,
  // Ngôn ngữ dự phòng khi thiếu bản dịch. Để 'en' chứ không để 'vi_VN':
  // nếu một khoá chưa được dịch, hiện tiếng Anh vẫn đọc được, còn để trùng
  // với ngôn ngữ chính thì vue-i18n in ra chính cái khoá (ví dụ
  // "routes.demo.comp.basic") và người dùng không hiểu gì.
  fallback: LOCALE.EN_US,
  // Chỉ hai ngôn ngữ: tiếng Việt và tiếng Anh
  availableLocales: [LOCALE.VI_VN, LOCALE.EN_US],
};

// Danh sách ngôn ngữ hiển thị ở nút chọn ngôn ngữ trên thanh tiêu đề
export const localeList: DropMenu[] = [
  {
    text: 'Tiếng Việt',
    event: LOCALE.VI_VN,
  },
  {
    text: 'English',
    event: LOCALE.EN_US,
  },
];
