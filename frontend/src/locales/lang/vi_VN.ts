import { genMessage } from '../helper';
import antdLocale from 'ant-design-vue/es/locale/vi_VN';

const modules = import.meta.glob('./vi-VN/**/*.{json,ts,js}', { eager: true });

export default {
  message: {
    ...genMessage(modules as Recordable<Recordable>, 'vi-VN'),
    // Không cần ghi đè DatePicker như bản zh_CN cũ: vi_VN của Ant Design Vue
    // đã có sẵn tên thứ và tháng bằng tiếng Việt.
    antdLocale,
  },
  dateLocale: null,
  dateLocaleName: 'vi',
};
