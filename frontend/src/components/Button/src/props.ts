const validColors = ['primary', 'error', 'warning', 'success', ''] as const;
type ButtonColorType = (typeof validColors)[number];

export const buttonProps = {
  color: {
    type: String as PropType<ButtonColorType>,
    validator: (v) => validColors.includes(v),
    default: '',
  },
  loading: { type: Boolean },
  disabled: { type: Boolean },
  /**
   * Text before icon.
   */
  preIcon: { type: String },
  /**
   * Text after icon.
   */
  postIcon: { type: String },
  /**
   * preIcon and postIcon icon size.
   * @default: 14
   */
  iconSize: { type: Number, default: 14 },
  /**
   * Nút chuyển tiếp thẳng sự kiện click của Ant Design (kèm MouseEvent), nên kiểu phải nhận tham số.
   * Khai `() => any` khiến mọi `@click="fn(a, b)"` hoặc `@click="fn($event)"` báo lỗi kiểu, dù chạy vẫn đúng.
   */
  onClick: {
    type: [Function, Array] as PropType<((...args: any[]) => any) | ((...args: any[]) => any)[]>,
    default: null,
  },
  text: { type: String },
};
