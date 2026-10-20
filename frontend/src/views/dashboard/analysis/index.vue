<template>
  <div class="dash p-4">
    <!-- ===== Bộ lọc: một hàng duy nhất phía trên mọi biểu đồ ===== -->
    <div class="dash-toolbar">
      <div>
        <h2 class="dash-title">Tổng quan</h2>
        <p v-if="data" class="dash-range">
          {{ formatDay(data.fromDate) }} – {{ formatDay(data.toDate) }} · so với {{ data.days }} ngày liền trước
        </p>
      </div>
      <div class="dash-actions">
        <RadioGroup v-model:value="days" button-style="solid">
          <RadioButton v-for="d in RANGES" :key="d.value" :value="d.value">{{ d.label }}</RadioButton>
        </RadioGroup>
        <Button :loading="loading" @click="load">
          <template #icon><Icon icon="ant-design:reload-outlined" /></template>
          Làm mới
        </Button>
      </div>
    </div>

    <Alert v-if="error" type="error" show-icon :message="error" class="!mb-4" />

    <Spin :spinning="loading && !data">
      <template v-if="data">
        <!-- ===== Truy cập ===== -->
        <h3 class="dash-section">Truy cập</h3>
        <div class="dash-grid dash-grid--4">
          <KpiTile title="Lượt xem trang" icon="ant-design:eye-outlined" :kpi="data.pageViews"
            :sub="`${data.pagesPerSession} trang / phiên`" />
          <KpiTile title="Khách truy cập" icon="ant-design:global-outlined" :kpi="data.visitors"
            :sub="`${data.returningVisitorRate}% là khách quay lại`" />
          <KpiTile title="Phiên truy cập" icon="ant-design:field-time-outlined" :kpi="data.sessions" />
          <KpiTile title="Người dùng hoạt động" icon="ant-design:user-switch-outlined" :kpi="data.activeUsers"
            sub="đăng nhập hoặc có học" />
        </div>
        <div class="dash-strip">
          <span><b>{{ num(data.dau) }}</b> hoạt động hôm nay</span>
          <span><b>{{ num(data.wau) }}</b> trong 7 ngày</span>
          <span><b>{{ num(data.mau) }}</b> trong 30 ngày</span>
          <span>
            Độ gắn bó DAU/MAU <b>{{ data.stickiness }}%</b>
            <Tooltip title="Tỉ lệ người dùng của tháng quay lại mỗi ngày. Trên 20% là tốt với ứng dụng học.">
              <Icon icon="ant-design:question-circle-outlined" :size="13" />
            </Tooltip>
          </span>
        </div>

        <!-- ===== Doanh thu ===== -->
        <h3 class="dash-section">Doanh thu</h3>
        <div class="dash-grid dash-grid--4">
          <KpiTile title="Doanh thu" icon="ant-design:dollar-circle-outlined" :kpi="data.revenue" :format="money"
            :sub="`Tổng từ trước: ${money(data.revenueAllTime)}`" />
          <KpiTile title="Đơn đã thanh toán" icon="ant-design:check-circle-outlined" :kpi="data.paidOrders"
            :sub="`trên ${num(data.ordersCreated)} đơn đã tạo`" />
          <KpiTile title="Người trả tiền" icon="ant-design:crown-outlined" :kpi="data.payingUsers"
            :sub="`TB ${money(data.arppu)} / người`" />
          <KpiTile title="Tỉ lệ chuyển đổi" icon="ant-design:rise-outlined" :value="`${data.conversionRate}%`"
            sub="đơn tạo ra được thanh toán" />
        </div>
        <div class="dash-strip">
          <span><b>{{ num(data.ordersPending) }}</b> đơn đang chờ chuyển khoản</span>
          <span><b>{{ num(data.ordersExpired) }}</b> đơn hết hạn không trả</span>
          <span v-if="data.unmatchedTransactions > 0" class="dash-warn">
            <Icon icon="ant-design:warning-outlined" :size="13" />
            <b>{{ num(data.unmatchedTransactions) }}</b> giao dịch tiền vào chưa khớp đơn —
            <RouterLink to="/system/payments?tab=transactions">đối soát</RouterLink>
          </span>
          <span v-else><Icon icon="ant-design:check-outlined" :size="13" /> Mọi giao dịch đã khớp đơn</span>
        </div>

        <!-- ===== Người dùng ===== -->
        <h3 class="dash-section">Người dùng & học tập</h3>
        <div class="dash-grid dash-grid--4">
          <KpiTile title="Tổng người dùng" icon="ant-design:team-outlined" :value="data.totalUsers" />
          <KpiTile title="Đăng ký mới" icon="ant-design:user-add-outlined" :kpi="data.newUsers" />
          <KpiTile title="Đang dùng Premium" icon="ant-design:star-outlined" :value="data.premiumActive"
            :sub="`${data.premiumRate}% người dùng`" />
          <KpiTile title="Người có học" icon="ant-design:read-outlined" :value="data.learning.learners"
            :sub="`${num(data.learning.minutesStudied)} phút học`" />
        </div>

        <!-- ===== Xu hướng theo ngày ===== -->
        <Card class="!mt-4" :tab-list="TREND_TABS" :active-tab-key="trendTab" @tab-change="(k) => (trendTab = k)">
          <DashChart :option="trendOption" height="320px" :label="trendLabel" />
        </Card>

        <div class="dash-grid dash-grid--2 !mt-4">
          <Card title="Khung giờ truy cập (giờ Việt Nam)" size="small">
            <DashChart :option="hourlyOption" height="260px" label="Lượt xem trang theo giờ trong ngày" />
          </Card>
          <Card title="Tính năng được xem nhiều" size="small">
            <DashChart :option="routesOption" height="260px" label="Lượt xem theo tính năng" />
          </Card>
        </div>

        <div class="dash-grid dash-grid--3 !mt-4">
          <Card title="Thiết bị" size="small">
            <DashChart :option="devicesOption" height="240px" label="Khách truy cập theo thiết bị" />
          </Card>
          <Card title="Nguồn truy cập (theo phiên)" size="small">
            <DashChart :option="referrersOption" height="240px" label="Phiên truy cập theo nguồn" />
          </Card>
          <Card title="Loại tài khoản" size="small">
            <DashChart :option="kindsOption" height="240px" label="Người dùng theo loại tài khoản" />
          </Card>
        </div>

        <div class="dash-grid dash-grid--2 !mt-4">
          <Card title="Hoạt động học & cộng đồng" size="small">
            <ul class="dash-list">
              <li><span>Phút học</span><b>{{ num(data.learning.minutesStudied) }}</b></li>
              <li><span>Bài học hoàn thành</span><b>{{ num(data.learning.lessonsCompleted) }}</b></li>
              <li><span>Lượt ôn từ</span><b>{{ num(data.learning.signsReviewed) }}</b></li>
              <li><span>Bài kiểm tra đã làm</span><b>{{ num(data.learning.quizzesTaken) }}</b></li>
              <li><span>Lượt chấm điểm AI</span><b>{{ num(data.learning.aiChecksDone) }}</b></li>
              <li><span>Bài viết diễn đàn mới</span><b>{{ num(data.forumPosts) }}</b></li>
              <li><span>Bình luận mới</span><b>{{ num(data.forumComments) }}</b></li>
            </ul>
          </Card>
          <Card title="Thanh toán gần nhất" size="small">
            <template #extra><RouterLink to="/system/payments">Xem tất cả</RouterLink></template>
            <Empty v-if="!data.recentPayments.length" description="Chưa có giao dịch nào" />
            <ul v-else class="dash-list">
              <li v-for="p in data.recentPayments" :key="p.paymentCode">
                <span class="dash-pay-who">
                  <b>{{ p.fullName }}</b>
                  <small>{{ p.email }} · {{ formatDateTime(p.paidAt) }}</small>
                </span>
                <b>{{ money(p.amount) }}</b>
              </li>
            </ul>
          </Card>
        </div>
      </template>
    </Spin>
  </div>
</template>

<script lang="ts" setup>
  import type { EChartsOption } from 'echarts';
  import { computed, onMounted, ref, watch } from 'vue';
  // Dự án không tự đăng ký component Ant Design — import ngay trong file (xem UserDetailDrawer.vue)
  import { Alert, Button, Card, Empty, Radio, Spin, Tooltip } from 'ant-design-vue';
  import Icon from '@/components/Icon/Icon.vue';
  import { useRootSetting } from '@/hooks/setting/useRootSetting';
  import { dashboardApi, type DashboardModel, type NamedValue } from '@/api/dashboard';
  import KpiTile from './components/KpiTile.vue';
  import DashChart from './components/DashChart.vue';

  defineOptions({ name: 'Analysis' });

  const RadioGroup = Radio.Group;
  const RadioButton = Radio.Button;

  const RANGES = [
    { value: 7, label: '7 ngày' },
    { value: 30, label: '30 ngày' },
    { value: 90, label: '90 ngày' },
  ];
  const TREND_TABS = [
    { key: 'traffic', tab: 'Truy cập' },
    { key: 'users', tab: 'Người dùng' },
    { key: 'revenue', tab: 'Doanh thu' },
  ];

  /** Tên route của web học → tên tính năng đọc được */
  const ROUTE_LABEL: Record<string, string> = {
    home: 'Trang chủ',
    'course-list': 'Danh sách khoá học',
    'course-detail': 'Chi tiết khoá học',
    lesson: 'Bài học',
    flashcard: 'Ôn từ',
    'quiz-hub': 'Kiểm tra',
    'quiz-attempt': 'Làm bài kiểm tra',
    games: 'Góc trò chơi',
    'dictionary-search': 'Thư viện cử chỉ',
    'dictionary-detail': 'Chi tiết từ',
    'word-pack-journey': 'Gói từ',
    library: 'Thư viện của tôi',
    forum: 'Diễn đàn',
    'forum-post': 'Bài viết diễn đàn',
    pricing: 'Gói dịch vụ',
    'guardian-dashboard': 'Ba mẹ & thầy cô',
    'my-profile': 'Trang cá nhân của tôi',
    'user-profile': 'Trang cá nhân người khác',
    login: 'Đăng nhập',
    register: 'Đăng ký',
    onboarding: 'Làm quen',
    khac: 'Khác',
  };
  const DEVICE_LABEL: Record<string, string> = { DESKTOP: 'Máy tính', MOBILE: 'Điện thoại', TABLET: 'Máy tính bảng' };
  const KIND_LABEL: Record<string, string> = {
    CHILD: 'Học sinh',
    PARENT: 'Phụ huynh',
    TEACHER: 'Giáo viên',
    ADULT: 'Người lớn',
  };

  const days = ref(30);
  const data = ref<DashboardModel>();
  const loading = ref(false);
  const error = ref('');
  const trendTab = ref('traffic');

  const { getDarkMode } = useRootSetting();

  /**
   * Bảng màu đã kiểm tra mù màu (3 ô đầu của bảng phân loại, đạt mọi cặp ở cả hai chế độ).
   * Chế độ tối dùng bậc riêng cho nền tối chứ không tự đảo màu.
   */
  const theme = computed(() => {
    const dark = getDarkMode.value === 'dark';
    return {
      series: dark ? ['#3987e5', '#d95926', '#199e70'] : ['#2a78d6', '#eb6834', '#1baf7a'],
      grid: dark ? '#2c2c2a' : '#ecebe7',
      axis: dark ? '#383835' : '#c3c2b7',
      muted: '#898781',
      text: dark ? '#c3c2b7' : '#52514e',
      surface: dark ? '#151515' : '#ffffff',
    };
  });

  async function load() {
    loading.value = true;
    error.value = '';
    try {
      data.value = await dashboardApi(days.value);
    } catch (e) {
      error.value = (e as Error)?.message || 'Không tải được số liệu';
    } finally {
      loading.value = false;
    }
  }

  onMounted(load);
  watch(days, load);

  // ---------------------------------------------------------------- định dạng

  const num = (v: number) => v.toLocaleString('vi-VN');
  const money = (v: number) => `${v.toLocaleString('vi-VN')}đ`;
  /** Nhãn trục gọn: 1,2 tr / 150 k */
  const moneyShort = (v: number) =>
    v >= 1_000_000 ? `${(v / 1_000_000).toLocaleString('vi-VN', { maximumFractionDigits: 1 })} tr`
      : v >= 1000 ? `${Math.round(v / 1000)} k` : `${v}`;
  const formatDay = (iso: string) => {
    const [y, m, d] = iso.split('-');
    return `${d}/${m}/${y}`;
  };
  const shortDay = (iso: string) => {
    const [, m, d] = iso.split('-');
    return `${d}/${m}`;
  };
  const formatDateTime = (iso: string) =>
    new Date(iso).toLocaleString('vi-VN', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' });

  // ---------------------------------------------------------------- biểu đồ

  /** Phần khung chung: lưới mảnh, trục lùi về sau, tooltip theo trục */
  function base(): EChartsOption {
    const t = theme.value;
    return {
      textStyle: { fontFamily: 'inherit' },
      grid: { left: 8, right: 16, top: 36, bottom: 8, containLabel: true },
      tooltip: { trigger: 'axis', axisPointer: { type: 'line', lineStyle: { color: t.axis } } },
      legend: { top: 0, left: 0, icon: 'roundRect', itemWidth: 12, itemHeight: 4, textStyle: { color: t.text } },
    };
  }

  function categoryAxis(labels: string[]) {
    const t = theme.value;
    return {
      type: 'category' as const,
      data: labels,
      axisLine: { lineStyle: { color: t.axis } },
      axisTick: { show: false },
      axisLabel: { color: t.muted, hideOverlap: true },
    };
  }

  function valueAxis(formatter?: (v: number) => string) {
    const t = theme.value;
    return {
      type: 'value' as const,
      minInterval: 1,
      splitLine: { lineStyle: { color: t.grid, type: 'solid' as const } },
      axisLabel: { color: t.muted, formatter },
    };
  }

  function line(name: string, values: number[], color: string) {
    return {
      name,
      type: 'line' as const,
      data: values,
      smooth: false,
      showSymbol: false,
      symbolSize: 8,
      lineStyle: { width: 2, color },
      itemStyle: { color, borderColor: theme.value.surface, borderWidth: 2 },
      emphasis: { focus: 'series' as const },
    };
  }

  function bars(name: string, values: number[], color: string, horizontal = false) {
    return {
      name,
      type: 'bar' as const,
      data: values,
      barMaxWidth: 28,
      itemStyle: { color, borderRadius: horizontal ? [0, 4, 4, 0] : [4, 4, 0, 0] },
    };
  }

  const trendLabel = computed(
    () => ({ traffic: 'Lượt xem và khách theo ngày', users: 'Người dùng mới và người học theo ngày', revenue: 'Doanh thu theo ngày' })[
      trendTab.value
    ] ?? '',
  );

  const trendOption = computed<EChartsOption>(() => {
    const d = data.value?.daily ?? [];
    const t = theme.value;
    const x = categoryAxis(d.map((p) => shortDay(p.date)));
    if (trendTab.value === 'revenue') {
      // Một đơn vị (đồng) → một trục. Số đơn nằm trong tooltip, không vẽ trục thứ hai.
      return {
        ...base(),
        legend: { show: false },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'shadow' },
          formatter: (ps: any) => {
            const p = d[ps[0].dataIndex];
            return `${formatDay(p.date)}<br/>Doanh thu: <b>${money(p.revenue)}</b><br/>Đơn đã trả: <b>${p.paidOrders}</b>`;
          },
        },
        xAxis: x,
        yAxis: valueAxis(moneyShort),
        series: [bars('Doanh thu', d.map((p) => p.revenue), t.series[0])],
      };
    }
    if (trendTab.value === 'users') {
      return {
        ...base(),
        xAxis: x,
        yAxis: valueAxis(),
        series: [
          line('Người có học', d.map((p) => p.learners), t.series[0]),
          line('Khách đã đăng nhập', d.map((p) => p.signedInVisitors), t.series[2]),
          line('Đăng ký mới', d.map((p) => p.newUsers), t.series[1]),
        ],
      };
    }
    return {
      ...base(),
      xAxis: x,
      yAxis: valueAxis(),
      series: [
        line('Lượt xem trang', d.map((p) => p.pageViews), t.series[0]),
        line('Khách truy cập', d.map((p) => p.visitors), t.series[1]),
      ],
    };
  });

  const hourlyOption = computed<EChartsOption>(() => {
    const h = data.value?.hourly ?? [];
    return {
      ...base(),
      legend: { show: false },
      grid: { left: 8, right: 8, top: 12, bottom: 8, containLabel: true },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: (ps: any) => `${ps[0].dataIndex}:00 – ${ps[0].dataIndex}:59<br/><b>${num(ps[0].value)}</b> lượt xem` },
      xAxis: categoryAxis(h.map((_, i) => `${i}h`)),
      yAxis: valueAxis(),
      series: [bars('Lượt xem', h, theme.value.series[0])],
    };
  });

  /** Cột ngang một màu: hạng mục không có thứ tự tự nhiên nên không tô đậm nhạt theo giá trị */
  function hbar(items: NamedValue[], label: (n: string | null) => string, unit: string): EChartsOption {
    const rows = [...items].reverse(); // giá trị lớn nhất nằm trên cùng
    return {
      ...base(),
      legend: { show: false },
      grid: { left: 8, right: 40, top: 8, bottom: 8, containLabel: true },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: (ps: any) => `${ps[0].name}<br/><b>${num(ps[0].value)}</b> ${unit}` },
      xAxis: { ...valueAxis(), axisLabel: { show: false } },
      yAxis: { ...categoryAxis(rows.map((r) => label(r.name))), axisLabel: { color: theme.value.text, width: 130, overflow: 'truncate' } },
      series: [
        {
          ...bars(unit, rows.map((r) => r.value), theme.value.series[0], true),
          // Nhãn số ở đầu cột: ít cột, đọc nhanh hơn rê chuột từng cột
          label: { show: true, position: 'right', color: theme.value.text, formatter: (p: any) => num(p.value) },
        },
      ],
    };
  }

  const routesOption = computed(() => hbar((data.value?.topRoutes ?? []).slice(0, 8), (n) => ROUTE_LABEL[n ?? ''] ?? n ?? 'Khác', 'lượt xem'));
  const referrersOption = computed(() => hbar(data.value?.referrers ?? [], (n) => n ?? 'Vào thẳng / không rõ', 'phiên'));
  const kindsOption = computed(() => hbar(data.value?.accountKinds ?? [], (n) => KIND_LABEL[n ?? ''] ?? n ?? '—', 'người dùng'));

  /** Ba thiết bị: phần-trên-tổng nhìn một lần là hiểu — đúng chỗ cho biểu đồ vòng (≤ 3 lát) */
  const devicesOption = computed<EChartsOption>(() => {
    const t = theme.value;
    const order = ['DESKTOP', 'MOBILE', 'TABLET'];
    const items = order
      .map((k, i) => ({
        name: DEVICE_LABEL[k],
        value: data.value?.devices.find((d) => d.name === k)?.value ?? 0,
        itemStyle: { color: t.series[i] },
      }))
      .filter((i) => i.value > 0);
    return {
      textStyle: { fontFamily: 'inherit' },
      tooltip: { trigger: 'item', formatter: '{b}: <b>{c}</b> khách ({d}%)' },
      legend: { bottom: 0, icon: 'circle', itemWidth: 10, textStyle: { color: t.text } },
      series: [
        {
          type: 'pie',
          radius: ['48%', '72%'],
          center: ['50%', '44%'],
          // Khe 2px cùng màu nền giữa các lát, không kẻ viền
          itemStyle: { borderColor: t.surface, borderWidth: 2, borderRadius: 4 },
          // Nhãn % hiện sẵn: màu thứ ba tương phản thấp trên nền sáng, không để màu tự gánh nghĩa
          label: { show: true, formatter: '{d}%', color: t.text },
          labelLine: { length: 6, length2: 6 },
          data: items,
        },
      ],
    };
  });
</script>

<style lang="less" scoped>
  .dash-toolbar {
    display: flex;
    flex-wrap: wrap;
    align-items: flex-end;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 8px;
  }

  .dash-title {
    margin: 0;
    font-size: 20px;
    font-weight: 600;
  }

  .dash-range {
    margin: 2px 0 0;
    color: @text-color-secondary;
    font-size: 13px;
  }

  .dash-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .dash-section {
    margin: 20px 0 10px;
    font-size: 15px;
    font-weight: 600;
  }

  .dash-grid {
    display: grid;
    gap: 16px;
  }

  .dash-grid--4 {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .dash-grid--3 {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .dash-grid--2 {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dash-strip {
    display: flex;
    flex-wrap: wrap;
    gap: 6px 24px;
    margin-top: 10px;
    padding: 8px 14px;
    border-radius: 8px;
    background: @component-background;
    color: @text-color-secondary;
    font-size: 13px;

    b {
      color: @text-color-base;
      font-variant-numeric: tabular-nums;
    }

    span {
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }
  }

  .dash-warn {
    color: #b0610a;
  }

  .dash-list {
    margin: 0;
    padding: 0;
    list-style: none;

    li {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      padding: 8px 0;
      border-bottom: 1px solid @border-color-base;
      font-variant-numeric: tabular-nums;
    }

    li:last-child {
      border-bottom: none;
    }
  }

  .dash-pay-who {
    display: flex;
    flex-direction: column;
    min-width: 0;

    small {
      overflow: hidden;
      color: @text-color-secondary;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  @media (max-width: 1200px) {
    .dash-grid--4 {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .dash-grid--3 {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 720px) {
    .dash-grid--4,
    .dash-grid--3,
    .dash-grid--2 {
      grid-template-columns: minmax(0, 1fr);
    }
  }
</style>
