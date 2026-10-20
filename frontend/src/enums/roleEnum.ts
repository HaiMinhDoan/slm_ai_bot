/** Khớp RoleCode.java ở backend và bảng vai_tro (V2__tai_khoan_quan_tri.sql) */
export enum RoleEnum {
  SYSTEM_ADMIN = 'SYSTEM_ADMIN',
  STAFF = 'STAFF',
  KNOWLEDGE_EDITOR = 'KNOWLEDGE_EDITOR',
  KNOWLEDGE_PUBLISHER = 'KNOWLEDGE_PUBLISHER',
  CONTENT_ADMIN = 'CONTENT_ADMIN',

  // Dùng trong trang demo của Vben
  SUPER = 'super',
  TEST = 'test',
}
