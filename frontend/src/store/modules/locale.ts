import type { LocaleSetting, LocaleType } from '#/config';

import { defineStore } from 'pinia';
import { store } from '@/store';

import { LOCALE_KEY } from '@/enums/cacheEnum';
import { createLocalStorage } from '@/utils/cache';
import { localeSetting } from '@/settings/localeSetting';

const ls = createLocalStorage();

/**
 * Ngôn ngữ được lưu trong localStorage từ lần truy cập trước, nên nó có thể là
 * một giá trị mà bản hiện tại không còn hỗ trợ — điển hình là 'zh_CN' của những
 * trình duyệt đã mở trang trước khi bỏ tiếng Trung.
 *
 * setupI18n() nạp ngôn ngữ bằng import động `./lang/${locale}.ts`. Nếu để
 * nguyên giá trị cũ thì import đó không tìm thấy tệp và ứng dụng chết ngay lúc
 * khởi động — người dùng thấy trang trắng, xoá cache mới vào lại được. Vì vậy
 * phải lọc lại theo danh sách ngôn ngữ đang thực sự có.
 */
function readLocaleSetting(): LocaleSetting {
  const cached = ls.get(LOCALE_KEY) as LocaleSetting | null;
  if (!cached) {
    return localeSetting;
  }
  const supported = localeSetting.availableLocales as readonly LocaleType[];
  if (!cached.locale || !supported.includes(cached.locale)) {
    return { ...cached, locale: localeSetting.locale };
  }
  return cached;
}

const lsLocaleSetting = readLocaleSetting();

interface LocaleState {
  localInfo: LocaleSetting;
}

export const useLocaleStore = defineStore({
  id: 'app-locale',
  state: (): LocaleState => ({
    localInfo: lsLocaleSetting,
  }),
  getters: {
    getShowPicker(state): boolean {
      return !!state.localInfo?.showPicker;
    },
    getLocale(state): LocaleType {
      return state.localInfo?.locale ?? 'vi_VN';
    },
  },
  actions: {
    /**
     * Set up multilingual information and cache
     * @param info multilingual info
     */
    setLocaleInfo(info: Partial<LocaleSetting>) {
      this.localInfo = { ...this.localInfo, ...info };
      ls.set(LOCALE_KEY, this.localInfo);
    },
    /**
     * Initialize multilingual information and load the existing configuration from the local cache
     */
    initLocale() {
      this.setLocaleInfo({
        ...localeSetting,
        ...this.localInfo,
      });
    },
  },
});

// Need to be used outside the setup
export function useLocaleStoreWithOut() {
  return useLocaleStore(store);
}
