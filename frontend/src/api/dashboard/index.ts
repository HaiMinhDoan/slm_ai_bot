import { defHttp } from '@/utils/http/axios';

/** Một chỉ số chính kèm số của khoảng liền trước để tính tăng/giảm */
export interface Kpi {
  current: number;
  previous: number;
}

export interface NamedValue {
  name: string | null;
  value: number;
  extra?: number;
}

export interface DailyPoint {
  date: string;
  pageViews: number;
  visitors: number;
  signedInVisitors: number;
  newUsers: number;
  revenue: number;
  paidOrders: number;
  learners: number;
  minutesStudied: number;
}

/** Khớp DashboardResponse của backend — mọi mốc ngày theo giờ Việt Nam */
export interface DashboardModel {
  days: number;
  fromDate: string;
  toDate: string;

  pageViews: Kpi;
  visitors: Kpi;
  sessions: Kpi;
  activeUsers: Kpi;
  pagesPerSession: number;
  returningVisitorRate: number;
  dau: number;
  wau: number;
  mau: number;
  stickiness: number;

  totalUsers: number;
  newUsers: Kpi;
  premiumActive: number;
  premiumRate: number;

  revenue: Kpi;
  paidOrders: Kpi;
  payingUsers: Kpi;
  revenueAllTime: number;
  ordersCreated: number;
  ordersPending: number;
  ordersExpired: number;
  conversionRate: number;
  arppu: number;
  unmatchedTransactions: number;

  learning: {
    minutesStudied: number;
    lessonsCompleted: number;
    signsReviewed: number;
    quizzesTaken: number;
    aiChecksDone: number;
    learners: number;
  };
  forumPosts: number;
  forumComments: number;

  daily: DailyPoint[];
  hourly: number[];
  topRoutes: NamedValue[];
  devices: NamedValue[];
  referrers: NamedValue[];
  accountKinds: NamedValue[];
  recentPayments: { fullName: string; email: string; amount: number; paidAt: string; paymentCode: string }[];
}

export const dashboardApi = (days: number) =>
  defHttp.get<DashboardModel>({ url: '/api/v1/admin/dashboard', params: { days } });
