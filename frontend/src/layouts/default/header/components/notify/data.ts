export interface ListItem {
  id: string;
  avatar: string;
  // Tiêu đề thông báo
  title: string;
  // Gạch ngang tiêu đề khi đã đọc
  titleDelete?: boolean;
  datetime: string;
  type: string;
  read?: boolean;
  description: string;
  clickClose?: boolean;
  extra?: string;
  color?: string;
}

export interface TabItem {
  key: string;
  name: string;
  list: ListItem[];
  unreadlist?: ListItem[];
}

/**
 * Dữ liệu mẫu cho chuông thông báo trên thanh tiêu đề.
 *
 * Đây là dữ liệu TĨNH, chưa nối với API. Bảng `notifications` đã có trong CSDL;
 * khi dựng endpoint /api/v1/notifications thì thay mảng này bằng lời gọi API.
 *
 * Bản gốc của vue-vben-admin dùng dữ liệu tiếng Trung và ảnh đại diện tải từ
 * CDN của Alipay. Đã bỏ cả hai: nội dung chuyển sang tiếng Việt đúng ngữ cảnh
 * SignAI, ảnh để trống để không phụ thuộc tài nguyên bên thứ ba.
 */
export const tabListData: TabItem[] = [
  {
    key: '1',
    name: 'Thông báo',
    list: [
      {
        id: '000000001',
        avatar: '',
        title: 'Có 12 từ vựng mới chờ duyệt',
        description: '',
        datetime: 'Hôm nay',
        type: '1',
      },
      {
        id: '000000002',
        avatar: '',
        title: 'Một tài khoản khai là giáo viên VSL đang chờ xác minh',
        description: '',
        datetime: 'Hôm qua',
        type: '1',
      },
      {
        id: '000000003',
        avatar: '',
        title: 'Đã nạp xong 4.362 video từ từ điển Bộ GD&ĐT',
        description: '',
        datetime: '3 ngày trước',
        type: '1',
      },
    ],
  },
  {
    key: '2',
    name: 'Tin nhắn',
    list: [
      {
        id: '000000006',
        avatar: '',
        title: 'Có bình luận mới trong diễn đàn',
        description: 'Một người học hỏi về khác biệt ký hiệu giữa miền Bắc và miền Nam',
        datetime: 'Hôm nay',
        type: '2',
        clickClose: true,
      },
      {
        id: '000000007',
        avatar: '',
        title: 'Bình luận bằng video đang chờ kiểm duyệt',
        description: 'Video do người dùng tự quay, cần kiểm duyệt trước khi hiển thị',
        datetime: 'Hôm qua',
        type: '2',
        clickClose: true,
      },
    ],
  },
  {
    key: '3',
    name: 'Việc cần làm',
    list: [
      {
        id: '000000009',
        avatar: '',
        title: 'Phân loại từ loại cho 238 từ',
        description: 'Những từ có nhãn không rõ ràng khi nạp từ điển Bộ GD&ĐT',
        datetime: '',
        extra: 'Chưa bắt đầu',
        color: '',
        type: '3',
      },
      {
        id: '000000010',
        avatar: '',
        title: 'Duyệt và xuất bản từ vựng',
        description: '3.322 từ đang ở trạng thái chưa xuất bản',
        datetime: '',
        extra: 'Đang làm',
        color: 'blue',
        type: '3',
      },
      {
        id: '000000011',
        avatar: '',
        title: 'Gán chủ đề cho từ vựng',
        description: 'Từ chưa có chủ đề sẽ không vào được bộ đề trộn theo chủ đề',
        datetime: '',
        extra: 'Cần làm sớm',
        color: 'gold',
        type: '3',
      },
    ],
  },
];
