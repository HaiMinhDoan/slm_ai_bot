interface GroupItem {
  title: string;
  icon: string;
  color: string;
  desc: string;
  date: string;
  group: string;
}

interface NavItem {
  title: string;
  icon: string;
  color: string;
}

interface DynamicInfoItem {
  avatar: string;
  name: string;
  date: string;
  desc: string;
}

/**
 * Dữ liệu MẪU cho trang Không gian làm việc.
 *
 * Bản gốc của vue-vben-admin là dữ liệu trình diễn bằng tiếng Trung (tên người
 * Trung Quốc, tên nhóm lập trình, danh ngôn). Đã thay bằng nội dung tiếng Việt
 * đúng ngữ cảnh SignAI, nhưng vẫn là dữ liệu TĨNH — chưa nối API.
 */
export const navItems: NavItem[] = [
  {
    title: 'Trang chủ',
    icon: 'ion:home-outline',
    color: '#1fdaca',
  },
  {
    title: 'Từ vựng',
    icon: 'ion:book-outline',
    color: '#bf0c2c',
  },
  {
    title: 'Chủ đề',
    icon: 'ion:layers-outline',
    color: '#e18525',
  },
  {
    title: 'Luyện tập',
    icon: 'ion:videocam-outline',
    color: '#3fb27f',
  },
  {
    title: 'Diễn đàn',
    icon: 'ion:chatbubbles-outline',
    color: '#4daf1bc9',
  },
  {
    title: 'Thống kê',
    icon: 'ion:bar-chart-outline',
    color: '#00d8ff',
  },
];

export const dynamicInfoItems: DynamicInfoItem[] = [
  {
    avatar: 'dynamic-avatar-1|svg',
    name: 'Minh',
    date: 'Vừa xong',
    desc: `đã hoàn thành bài <a>Bảng chữ cái</a>`,
  },
  {
    avatar: 'dynamic-avatar-2|svg',
    name: 'Lan',
    date: '1 giờ trước',
    desc: `đã đạt chuỗi <a>7 ngày học liên tiếp</a>`,
  },
  {
    avatar: 'dynamic-avatar-3|svg',
    name: 'Hoà',
    date: '1 ngày trước',
    desc: `đăng câu hỏi trong <a>Diễn đàn</a>`,
  },
  {
    avatar: 'dynamic-avatar-4|svg',
    name: 'Thu',
    date: '2 ngày trước',
    desc: `gửi video luyện ký hiệu <a>cảm ơn</a>`,
  },
  {
    avatar: 'dynamic-avatar-5|svg',
    name: 'Dũng',
    date: '3 ngày trước',
    desc: `trả lời câu hỏi <a>Ký hiệu miền Bắc và miền Nam khác nhau thế nào?</a>`,
  },
  {
    avatar: 'dynamic-avatar-6|svg',
    name: 'Mai',
    date: '1 tuần trước',
    desc: `học xong chủ đề <a>Gia đình</a>`,
  },
  {
    avatar: 'dynamic-avatar-1|svg',
    name: 'Minh',
    date: '1 tuần trước',
    desc: `bắt đầu chủ đề <a>Trường học</a>`,
  },
  {
    avatar: 'dynamic-avatar-1|svg',
    name: 'Nam',
    date: '2 tuần trước',
    desc: `hoàn thành bài kiểm tra <a>Số đếm</a>`,
  },
];

export const groupItems: GroupItem[] = [
  {
    title: 'Bảng chữ cái',
    icon: 'ion:text-outline',
    color: '',
    desc: 'Chữ cái ngón tay và dấu thanh tiếng Việt.',
    group: 'Cơ bản',
    date: 'Cập nhật gần đây',
  },
  {
    title: 'Gia đình',
    icon: 'ion:people-outline',
    color: '#3fb27f',
    desc: 'Cách gọi người thân trong gia đình.',
    group: 'Cơ bản',
    date: 'Cập nhật gần đây',
  },
  {
    title: 'Giao tiếp',
    icon: 'ion:chatbubbles-outline',
    color: '#e18525',
    desc: 'Chào hỏi, cảm ơn, xin lỗi và hội thoại hằng ngày.',
    group: 'Cơ bản',
    date: 'Cập nhật gần đây',
  },
  {
    title: 'Số đếm',
    icon: 'ion:calculator-outline',
    color: '#bf0c2c',
    desc: 'Số đếm, ngày tháng và thời gian.',
    group: 'Cơ bản',
    date: 'Cập nhật gần đây',
  },
  {
    title: 'Trường học',
    icon: 'ion:school-outline',
    color: '#00d8ff',
    desc: 'Từ vựng dùng trong lớp học và nhà trường.',
    group: 'Chuyên đề',
    date: 'Cập nhật gần đây',
  },
  {
    title: 'Y tế',
    icon: 'ion:medkit-outline',
    color: '#EBD94E',
    desc: 'Từ vựng cần khi đi khám bệnh.',
    group: 'Chuyên đề',
    date: 'Cập nhật gần đây',
  },
];
