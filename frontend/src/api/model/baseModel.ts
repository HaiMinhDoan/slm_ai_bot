export interface BasicPageParams {
  page: number;
  pageSize: number;
}

export interface BasicFetchResult<T> {
  items: T[];
  total: number;
}

/** Khớp PageResponse của backend (vn.slmglobal.slmai.common.web.PageResponse) */
export interface PageResult<T> extends BasicFetchResult<T> {
  page: number;
  size: number;
  totalPages: number;
}
