/** Một dòng trong danh sách thông báo ở chuông trên thanh tiêu đề (NoticeList.vue) */
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
