# KIẾN TRÚC NỀN TẢNG AI CUSTOMER CARE, SALES & CONTENT

## Dành cho doanh nghiệp điện mặt trời

**Phiên bản:** 1.0\
**Mục tiêu:** Xây dựng nền tảng AI hợp nhất Customer 360, CSKH, bán
hàng, dữ liệu điện mặt trời, Zalo OA, Facebook, kho kiến thức, kho video
và kho văn phong/conversation mẫu.

------------------------------------------------------------------------

# 1. Tầm nhìn hệ thống

Không xây một chatbot đơn lẻ.

Hệ thống là một nền tảng **AI Customer Intelligence Platform** trong đó:

-   CRM/Customer 360 là nguồn sự thật về khách hàng.
-   Hệ thống bảng giá `slm_v2` là nguồn sự thật về vật tư, trọn gói và giá
    bán; bot chỉ đồng bộ về để đọc (mục 4.2).
-   Solar API là nguồn sự thật về dữ liệu sản lượng và tình trạng hệ
    thống.
-   Knowledge Base là nguồn sự thật về chính sách, quy trình, cách tư vấn
    và FAQ (mục 5).
-   Media Library là nguồn tài sản nội dung (ảnh, video) để AI chọn media phù hợp.
-   Style & Human Conversation Library là nguồn để AI học cách hành văn
    và cách dẫn dắt hội thoại.
-   Claude là bộ máy suy luận/ngôn ngữ.
-   AI Orchestrator quyết định cần dùng dữ liệu nào và workflow nào.
-   Zalo/Facebook là các kênh giao tiếp.
-   Human Sales/Support là tầng tiếp nhận các trường hợp cần người thật.

Nguyên tắc cốt lõi:

> **AI không tự bịa kiến thức và không tự ý gửi thông tin ra ngoài. AI
> đề xuất; Orchestrator + Policy Engine kiểm tra; Channel Adapter mới
> thực hiện gửi.**

------------------------------------------------------------------------

# 2. Bài toán nghiệp vụ

Doanh nghiệp có khoảng 3.000 khách hàng/số điện thoại và các nhóm chính:

1.  Khách đã lắp đặt.
2.  Khách cần CSKH sau bán hàng.
3.  Khách có thể giới thiệu người quen.
4.  Khách cần báo cáo điện năng tuần/tháng.
5.  Khách chưa lắp đặt nhưng đang tư vấn.
6.  Lead mới từ Facebook.
7.  Lead mới từ Zalo.
8.  Khách hỏi kỹ thuật/sự cố.
9.  Khách cần tư vấn sản phẩm, battery, inverter, tối ưu tự dùng.
10. Khách cần được chuyển cho sales/technician.

Hệ thống cần:

-   Tự động CSKH.
-   Tự động gửi báo cáo sản lượng.
-   Tự động tư vấn cơ bản.
-   Tự động chăm sóc lead.
-   Tự động lựa chọn ảnh/video phù hợp.
-   Tự động lên kế hoạch nội dung.
-   Tự động trả lời Facebook/Zalo.
-   Khi lấy được số điện thoại Facebook lead, gọi API nội bộ để báo
    sales.
-   Giữ lại phong cách giao tiếp của người thật.
-   Học từ những cuộc hội thoại tốt đã có.
-   Đo hiệu quả từng nội dung/video/campaign.

------------------------------------------------------------------------

# 3. Kiến trúc tổng thể

``` text
┌────────────────────────────────────────────────────────────────────────────┐
│ KÊNH     Facebook (Page + Messenger) · Zalo OA · TikTok                    │
│          tùy chọn rủi ro cao: Facebook Groups, Zalo cá nhân (mục 54.6)     │
└──────────────┬───────────────────────────────────────────▲─────────────────┘
       webhook │                                           │ Send / Publish API
┌──────────────▼───────────────────────────────────────────┴─────────────────┐
│ slm_ai: 1 ứng dụng Spring Boot + 1 worker (modular monolith)               │
│                                                                            │
│ adapters ─► conversation ─► orchestrator ⇄ Claude API                      │
│                             ├─► tools: sizing · catalog · lead · handoff   │
│                             └─► MediaResolver + Policy ─► adapter gửi đi   │
│ conversation: gộp tin, khóa theo hội thoại, AI hay người phụ trách         │
│                                                                            │
│ chạy nền: knowledge loader · catalog-sync · media index · lead-dispatch    │
│           trợ lý nội dung · publish scheduler · energy (Phase 4)           │
└───────┬──────────────┬──────────────┬──────────────┬──────────────┬────────┘
        │              │              │              │              │
    PostgreSQL     MinIO / S3     slm_v2         Sales API      Vben Admin
    slm_ai         media, HTTPS   view ai_v_*    báo lead       admin-api
    dữ liệu + job                 chỉ đọc
```

- Một ứng dụng Spring Boot chia module (modular monolith) cộng một worker; không tách microservice ở giai đoạn đầu.
- Job (gộp tin, follow-up, đăng bài, đồng bộ) lưu trong PostgreSQL; chưa cần Redis hay Kafka (mục 30).
- Kiến thức nằm trong prompt có cache (mục 5.4); chưa dùng vector search (mục 5.7).
- Một model Claude cho hội thoại với khách, chọn bằng bộ test (mục 20).
- Luồng trả lời: mục 19. Orchestrator: mục 32. Policy Engine: mục 33. Kênh: mục 54.
------------------------------------------------------------------------

# 4. Các module chính

## 4.1 Customer 360

Lưu toàn bộ thông tin liên quan đến khách hàng:

-   ID khách hàng.
-   Họ tên.
-   Số điện thoại.
-   Email.
-   Định danh theo kênh (bảng `dinh_danh_kenh`, mục 29): PSID Facebook theo
    Page, Zalo user_id theo OA, ID người dùng TikTok.
-   Giai đoạn khách hàng.
-   Sales phụ trách.
-   Thông tin hệ thống điện mặt trời.
-   Công suất kWp.
-   Inverter.
-   Battery.
-   Ngày lắp đặt.
-   Bảo hành.
-   Lịch sử tương tác.
-   Lịch sử mua hàng.
-   Lịch sử support.
-   Lịch sử giới thiệu khách.
-   Preference/notification.
-   Conversation summary.

Các lifecycle:

``` text
LEAD
  ↓
CONSULTING
  ↓
WON
  ↓
INSTALLATION
  ↓
INSTALLED
  ↓
AFTER_SALES
  ↓
REFERRAL
```

Có thể có thêm:

``` text
LOST
DORMANT
SUPPORT
VIP
```

**Nguồn dữ liệu khách hiện có (chưa chốt, mục 59.2).** Khoảng 3.000 khách hiện có (nhóm 1–4 ở mục 2) đang nằm ở `slm_v2` / Sales API, không nằm trong bot. Trước khi làm các chức năng cho khách đã lắp đặt, cần chốt: hệ nào là nguồn gốc của khách hàng và hệ thống điện của khách, bot nhập một lần hay đồng bộ định kỳ, khớp theo số điện thoại thế nào. Cho tới khi chốt, `khach_hang` của bot chỉ chứa khách nhắn tin qua các kênh.

## 4.2 Product Catalog (đồng bộ từ slm_v2)

Vật tư, trọn gói và bảng giá đã được quản lý trong hệ thống `slm_v2` (PostgreSQL). Bot **không quản lý lại** dữ liệu này: chỉ đồng bộ về một bản chỉ đọc, chuẩn hóa, kiểm tra, rồi cho AI tra cứu qua tool. Mọi thay đổi giá và gói vẫn làm trên `slm_v2`.

### 4.2.1 Bảng nguồn cần đồng bộ

``` text
nganh_hang (hotline sale / kỹ thuật)
 └─ nhom_vat_tu      TAM_PIN, BIEN_TAN, PIN_LUU_TRU, HE_KHUNG_NHOM, HE_DAY_DIEN,
     │               TU_DIEN, HE_TIEP_DIA, TRON_GOI_LAP_DAT
     └─ vat_tu ── thuong_hieu
          ├─ anh_vat_tu → tep_tin (ảnh trên MinIO)
          └─ thong_tin_gia (giá bán theo cơ sở, có lịch sử)
nhom_tron_goi        tổ hợp hãng tấm pin – inverter – pin lưu trữ
 └─ tron_goi ── co_so (HN / HCM; mỗi gói có một bản cho từng cơ sở)
      └─ vat_tu_tron_goi (vật tư, số lượng, bảo hành, hiển thị cho khách)
```

Chỉ đồng bộ các bảng trên. Bot không đọc khách hàng, hợp đồng, hoa hồng hay người dùng của `slm_v2`.

### 4.2.2 Quy tắc nghiệp vụ bắt buộc

1. **Không đồng bộ giá nhập và biên lợi nhuận.** `thong_tin_gia.ds_gia` chứa cả `giaBan` và `giaNhap`; `gm`, `gm_tong` là biên lợi nhuận. Chặn ngay từ nguồn bằng view (mục 4.2.5), không chỉ lọc trong code của bot.
2. **`thong_tin_gia` là lịch sử giá.** Một vật tư có nhiều dòng cùng `trang_thai = 1`; giá hiện hành là dòng có `tao_luc` mới nhất.
3. **Giá gói là `tron_goi.tong_gia`**, không phải tổng `gia × so_luong` của các dòng vật tư (ví dụ gói 97: cộng các dòng ra 84,0 tr, `tong_gia` là 90,8 tr). AI chỉ báo `tong_gia`; giá từng dòng trong gói không được đồng bộ.
4. **`vat_tu_tron_goi.duoc_xem`** đánh dấu thành phần hiển thị cho khách (tấm pin, inverter, pin lưu trữ, nhân công). Dùng cột này để trả lời "gói gồm những gì".
5. **Đơn vị:** `tron_goi.cong_suat_he_thong` tính bằng W (5000 = 5 kWp). `san_luong_toi_thieu`/`san_luong_toi_da` là kWh/tháng (cần xác nhận, mục 4.2.8).
6. **Giá theo cơ sở:** gói đã tách riêng theo `co_so`; giá lẻ vật tư nằm trong `ds_gia` theo `maCoSo`. Hai cơ sở thực chất là hai vùng giá: **HN = miền Bắc, HCM = miền Nam**; tỉnh của khách được quy đổi sang miền theo `rules/regions.yaml` (mục 4.2.9).

### 4.2.3 Cơ chế đồng bộ

Bảng nguồn không có cột thời điểm cập nhật (`updated_at`), không có trigger và `wal_level = replica`, nên không làm được đồng bộ tăng dần hay CDC. Tổng dữ liệu chỉ khoảng 2.500 dòng, vì vậy mỗi lần kéo toàn bộ rồi so hash.

``` text
slm_v2 (nguồn)                            slm_ai (DB riêng của bot)
 view ai_v_* + user ai_sync (SELECT)  ─►  CatalogSyncJob (mỗi 10 phút + nút "Sync now" trên admin)
                                            1. đọc mọi view trong 1 transaction REPEATABLE READ, READ ONLY
                                            2. chuẩn hóa (mục 4.2.4)
                                            3. kiểm tra → gắn cảnh báo, đặt recommendable
                                            4. upsert theo id_nguon, bỏ qua dòng có ma_bam không đổi;
                                               dòng biến mất ở nguồn → dang_dung = false (không xóa)
                                            5. tạo/cập nhật media SLM_V2 cho ảnh gói và vật tư (mục 6.3 C)
                                            6. tính phien_ban_catalog = hash các ma_bam;
                                               đổi → dựng lại bản tóm tắt catalog (mục 5.3)
                                            7. ghi dm_lich_su_dong_bo
```

- Thứ tự upsert: thương hiệu → cơ sở → vật tư → giá → gói → thành phần gói.
- Lỗi giữa chừng: rollback toàn bộ lần đồng bộ, giữ dữ liệu cũ và báo trên admin. Bot vẫn tư vấn bằng bản catalog trước đó.
- `tao_luc` ở nguồn là `timestamp without time zone`: chốt một múi giờ khi chuyển sang `TIMESTAMPTZ`.

### 4.2.4 Chuẩn hóa và kiểm tra dữ liệu

Chuẩn hóa:

- `cong_suat_he_thong` (W) → `cong_suat_kwp`.
- `loai_he_thong`: `On-Grid` → `ON_GRID`, `Hy-Brid` → `HYBRID`. `loai_pha`: `1 pha` → 1, `3 pha` → 3.
- Thông số JSON (`du_lieu_rieng`): tách số và đơn vị (`"8 kW"` → `{value: 8, unit: "kW"}`); giá trị giữ chỗ (`"abc"`, `0`) → `null`.
- Giá lẻ: lấy dòng giá mới nhất, tách theo `maCoSo`.

Cảnh báo hiện trên admin. Với loại **chặn**, gói hoặc vật tư không được gợi ý cho đến khi sửa ở nguồn:

| Mã | Điều kiện | Mức |
|---|---|---|
| `CAPACITY_SUSPICIOUS` | `cong_suat_he_thong < 1000` W và không tính được từ tấm pin | Chặn |
| `CAPACITY_FROM_PANELS` | `cong_suat_he_thong < 1000` W: dùng số tấm × công suất tấm (gói 101 ghi 9 → 8,75 kWp) | Cảnh báo |
| `CAPACITY_MISMATCH` | `cong_suat_he_thong` lệch quá 3% so với số tấm × công suất tấm | Cảnh báo |
| `UNKNOWN_SYSTEM_TYPE` / `UNKNOWN_PHASE` | Giá trị ngoài danh sách chuẩn hóa | Chặn |
| `MISSING_PRICE` | `tong_gia` rỗng hoặc bằng 0 | Chặn |
| `INACTIVE_MATERIAL_IN_PACKAGE` | Thành phần hiển thị cho khách (`duoc_xem`) là vật tư đã ngừng | Chặn |
| `INACTIVE_ACCESSORY_IN_PACKAGE` | Phụ kiện không hiển thị cho khách là vật tư đã ngừng (gói 105, 106, 214, 215: dây AC, bộ chống phát ngược) | Cảnh báo |
| `MISSING_BATTERY` | Hệ Hybrid không có pin lưu trữ có dung lượng | Chặn |
| `NAME_PHASE_MISMATCH` | Tên gói ghi "1 pha" nhưng `loai_pha` là 3 pha hoặc ngược lại (gói 172) | Cảnh báo |
| `MISSING_BRANCH_PRICE` | Giá lẻ vật tư thiếu một cơ sở | Cảnh báo; không báo giá lẻ ở cơ sở thiếu |
| `DUPLICATE_BRANCH_PRICE` | Một cơ sở xuất hiện 2 lần trong `ds_gia` | Cảnh báo |
| `SPEC_PLACEHOLDER` | Thông số là giá trị giữ chỗ | Cảnh báo |
| `MISSING_IMAGE` | Gói không có ảnh | Cảnh báo |

Sản lượng min / max của `slm_v2` chỉ lưu để đối chiếu, không xét cảnh báo: sản lượng nói với khách tính theo PV out EVN (mục 60.3). Lần đồng bộ thử với dữ liệu thật (2026-10-28): 74 / 74 gói đang bán được gợi ý; cảnh báo `CAPACITY_FROM_PANELS` (101), `INACTIVE_ACCESSORY_IN_PACKAGE` (105, 106, 214, 215), `NAME_PHASE_MISMATCH` (172).

### 4.2.5 View và tài khoản phía slm_v2

Chạy trên `slm_v2` bằng tài khoản quản trị. View chạy bằng quyền của chủ view, nên `ai_sync` chỉ cần SELECT trên view và không có quyền gì trên bảng gốc. Nhờ vậy `giaNhap`, `gm`, `gm_tong` không bao giờ ra khỏi DB nguồn.

``` sql
CREATE VIEW ai_v_nganh_hang AS
SELECT id, ma, ten, sdt_sale, sdt_tech, trang_thai FROM nganh_hang;

CREATE VIEW ai_v_co_so AS
SELECT id, ma, ten, dc_van_phong, sdt, trang_thai FROM co_so;

CREATE VIEW ai_v_thuong_hieu AS
SELECT id, ten, ten_quoc_te, quoc_gia, mo_ta, tep_tin_id, trang_thai FROM thuong_hieu;

CREATE VIEW ai_v_nhom_vat_tu AS                       -- bỏ gm
SELECT id, ma, ten, nghanh_hang_id, thuoc_tinh_rieng, vat_tu_chinh, trang_thai FROM nhom_vat_tu;

CREATE VIEW ai_v_vat_tu AS                            -- bỏ gm, sheet_link, mo_ta_hop_dong
SELECT id, ma, ten, nhom_vat_tu_id, thuong_hieu_id, don_vi, mo_ta_bao_gia,
       du_lieu_rieng, thoi_gian_bao_hanh, vat_tu_chinh, trang_thai
FROM vat_tu;

CREATE VIEW ai_v_gia_hien_hanh AS                     -- chỉ giá bán, chỉ dòng mới nhất
SELECT DISTINCT ON (vat_tu_id) vat_tu_id, tao_luc AS hieu_luc_tu,
       (SELECT jsonb_agg(jsonb_build_object('maCoSo', e->>'maCoSo', 'giaBan', e->'giaBan'))
          FROM jsonb_array_elements(ds_gia) e) AS gia_ban
FROM thong_tin_gia
WHERE trang_thai = 1
ORDER BY vat_tu_id, tao_luc DESC, id DESC;

CREATE VIEW ai_v_anh_vat_tu AS
SELECT id, vat_tu_id, tep_tin_id, anh_chinh, trang_thai FROM anh_vat_tu;

CREATE VIEW ai_v_nhom_tron_goi AS
SELECT id, ten, nghanh_hang_id, thuong_hieu_tam_pin, thuong_hieu_inverter,
       thuong_hieu_pin_luu_tru, trang_thai
FROM nhom_tron_goi;

CREATE VIEW ai_v_tron_goi AS                          -- bỏ gm_tong
SELECT id, co_so_id, nhom_tron_goi_id, ten, tep_tin_id, loai_he_thong, loai_pha,
       cong_suat_he_thong, san_luong_toi_thieu, san_luong_toi_da, mo_ta,
       tong_gia, gia_khung_sat, ban_chay, trang_thai
FROM tron_goi;

CREATE VIEW ai_v_vat_tu_tron_goi AS                   -- bỏ gia, gm
SELECT id, tron_goi_id, vat_tu_id, so_luong, thoi_gian_bao_hanh, duoc_bao_hanh, duoc_xem, trang_thai
FROM vat_tu_tron_goi;

CREATE VIEW ai_v_tep_tin_anh AS                       -- chỉ file ảnh mà gói / vật tư / hãng dùng
SELECT t.id, t.duong_dan, t.loai_tep_tin, t.duoi_tep, t.kich_co, t.sua_luc
FROM tep_tin t
WHERE t.id IN (SELECT tep_tin_id FROM tron_goi
               UNION SELECT tep_tin_id FROM anh_vat_tu
               UNION SELECT tep_tin_id FROM thuong_hieu);

CREATE ROLE ai_sync LOGIN PASSWORD '<mật khẩu mạnh>';
GRANT CONNECT ON DATABASE slm_v2 TO ai_sync;
GRANT USAGE ON SCHEMA public TO ai_sync;
GRANT SELECT ON ai_v_nganh_hang, ai_v_co_so, ai_v_thuong_hieu, ai_v_nhom_vat_tu, ai_v_vat_tu,
                ai_v_gia_hien_hanh, ai_v_anh_vat_tu, ai_v_nhom_tron_goi, ai_v_tron_goi,
                ai_v_vat_tu_tron_goi, ai_v_tep_tin_anh
      TO ai_sync;
```

Phần SELECT của các view đã được chạy thử (chỉ đọc) trên `slm_v2` ngày 2026-10-20.

**Đã tạo trên `slm_v2` ngày 2026-10-27** bằng [deploy/slm_v2/ai_views.sql](deploy/slm_v2/ai_views.sql) (một transaction, có thêm `ai_v_mien`: tên miền web theo cơ sở, mục 60.4). Đã kiểm tra bằng `ai_sync`: đọc view được; đọc bảng gốc, đọc `gm_tong`, ghi dữ liệu đều bị từ chối; view giá không còn `giaNhap`. Thông tin kết nối ở `.env`: `SLM_V2_DB_URL`, `SLM_V2_DB_USERNAME`, `SLM_V2_DB_PASSWORD`.

Đi kèm: đổi mật khẩu tài khoản `postgres`, và giới hạn IP trong `pg_hba.conf` để chỉ máy chủ bot dùng được `ai_sync`.

### 4.2.6 AI dùng catalog thế nào

Giá, gói và thông số **không** nằm trong prompt. Claude gọi tool, backend trả số liệu từ các bảng catalog:

| Tool | Việc |
|---|---|
| `tim_goi_phu_hop(khu_vuc, loai_he_thong, so_pha, san_luong_can, dien_tich_mai, loai_mai, can_luu_tru)` | Backend quy đổi tỉnh → miền → cơ sở giá (mục 4.2.9); `SizingService` tính theo công thức của doanh nghiệp, lọc gói `dang_dung` và `duoc_goi_y`, trả 1–3 gói kèm `tong_gia`, thành phần `duoc_xem`, bảo hành, ảnh chính; khi thông tin chưa đủ thì trả dải giá theo khoảng hóa đơn (nếu đã biết), loại hệ, số pha và cơ sở, không trả dải giá của toàn catalog (mục 58.2, 59.3) |
| `chi_tiet_goi(goi_id)` | Thành phần hiển thị, bảo hành, giá khung sắt nếu có |
| `tra_cuu_vat_tu(ma hoặc từ khóa)` | Thông số đã chuẩn hóa, bảo hành, giá lẻ theo cơ sở (nếu có) |

- Mọi con số về giá, công suất, sản lượng trong câu trả lời phải có nguồn hợp lệ; Policy Engine kiểm tra trước khi gửi theo quy tắc ở mục 33.
- Mỗi lần báo giá đi kèm điều kiện báo giá do doanh nghiệp chốt (VAT, vận chuyển / lắp đặt ở tỉnh xa, giá cuối sau khảo sát, hiệu lực; mục 4.2.8). Tool trả kèm `hieu_luc_tu` của giá để biết báo giá dựa trên bản catalog nào.
- Nếu mái cần khung sắt, công thức cộng thêm `gia_khung_sat` (hiện mọi gói đều đang là 0).
- Bản tóm tắt catalog (danh sách hãng, loại hệ, dải kWp theo cơ sở, địa chỉ và hotline; **không có giá**) được đưa vào prompt để AI biết công ty đang bán gì (mục 5.4).

### 4.2.7 Hiện trạng dữ liệu (kiểm tra ngày 19–20/10/2026)

- 143 vật tư (102 đang dùng), 22 thương hiệu, 79 gói (74 đang dùng), 1.877 dòng thành phần gói.
- Gói đang dùng: On-Grid 1 pha 6, On-Grid 3 pha 22, Hy-Brid 1 pha 30, Hy-Brid 3 pha 16; giá từ 53,6 tr đến 838,6 tr.
- Gói 101 "Hy-Brid 9 kWp" có `cong_suat_he_thong = 9` (đúng ra phải là 9000).
- 4 gói đang dùng chứa vật tư đã ngừng.
- 29 vật tư đang dùng chỉ có giá HN trong dòng giá mới nhất; 2 vật tư bị lặp giá HN.
- Gói 97/98 (Hy-Brid 5 kWp): bản HN có sản lượng 500–700, cao hơn bản HCM 400–600, ngược với mức trung bình.
- Toàn bộ gói và vật tư đang dùng chưa có mô tả, nên nội dung tư vấn lấy từ Knowledge (mục 5).
- Ảnh đang được phục vụ qua MinIO bằng HTTP, chưa có domain HTTPS.

### 4.2.8 Cần xác nhận

- `san_luong_toi_thieu`/`san_luong_toi_da` có phải kWh/tháng không.
- Danh sách tỉnh thuộc miền Bắc / miền Nam (doanh nghiệp sẽ cung cấp; mục 4.2.9).
- Sửa gói 101 và các lỗi dữ liệu trên ở DB nguồn.
- Nhập giá khung cho mái bằng (`gia_khung_sat` hiện bằng 0 ở mọi gói, mục 58.4).
- **Công thức sizing của doanh nghiệp** (hóa đơn → kWh theo bậc EVN → kWh/ngày → kWp, hệ số sản lượng theo miền, diện tích mái theo loại mái). Chưa có công thức thì chưa làm được `tim_goi_phu_hop`; đây là việc chặn của Phase 0.
- **Điều kiện báo giá:** giá đã gồm VAT chưa (đã chốt 2026-10-29: `tong_gia` đã gồm VAT); có phụ phí vận chuyển / lắp đặt ở tỉnh xa không; chiết khấu của sales so với giá niêm yết; báo giá có hiệu lực bao lâu. AI luôn nói rõ giá là giá niêm yết, giá cuối do nhân viên báo sau khảo sát.
- **Vùng phục vụ:** tỉnh nào công ty lắp đặt được (mục 4.2.9). Đã chốt 2026-10-29: cả 34 tỉnh.

### 4.2.9 Quy đổi tỉnh → miền → cơ sở giá

Hai cơ sở `HN` và `HCM` trong `slm_v2` thực chất dùng để phân **giá miền Bắc** và **giá miền Nam**, không phải nơi khách ở.

``` text
Khách nói tỉnh/thành ─► rules/regions.yaml ─► miền (BAC / NAM) ─► cơ sở giá (HN / HCM) ─► lọc gói, giá lẻ
```

- Danh sách tỉnh thuộc miền Bắc / miền Nam **do doanh nghiệp cung cấp**; các tỉnh miền Trung được xếp vào một trong hai miền theo danh sách này.
- Lưu ở `knowledge/rules/regions.yaml`, sửa và phát hành qua Knowledge Editor như mọi tài liệu (mục 5.3). Khi phát hành, hệ thống kiểm tra: mỗi tỉnh chỉ thuộc một miền, mỗi miền trỏ tới một cơ sở có trong catalog.
- Danh sách nên theo đơn vị hành chính hiện hành (sau sắp xếp tỉnh năm 2025) và kèm **tên cũ, tên viết tắt, tên không dấu** làm alias, vì khách thường nói theo tên cũ hoặc gõ tắt.
- AI không chọn cơ sở: AI chỉ ghi tỉnh khách nói (`khu_vuc`), backend quy đổi. Tỉnh không có trong danh sách hoặc không rõ: AI hỏi lại một câu; vẫn không xác định được thì chuyển nhân viên, không tự đoán miền.
- **Vùng phục vụ:** cả 34 tỉnh đều phục vụ (doanh nghiệp chốt 2026-10-29), nên hiện chưa có cờ. Khi có tỉnh ngừng phục vụ thì thêm cờ `serviced` cho mỗi tỉnh. Tỉnh công ty không lắp đặt: AI nói rõ, không báo giá gói, ghi lead với lý do "ngoài vùng phục vụ" để sales quyết định (mục 59.3).
- `ho_so_tu_van.thong_tin_da_thu` lưu cả tỉnh và miền đã quy đổi; lead gửi Sales API kèm cả hai.

Bản nháp:

``` yaml
regions:
  BAC: { co_so: HN }
  NAM: { co_so: HCM }
provinces:                   # doanh nghiệp cung cấp danh sách đầy đủ
  - { name: "Hà Nội", region: BAC, serviced: true, aliases: ["HN", "Ha Noi", "Hà Tây"] }
  - { name: "TP. Hồ Chí Minh", region: NAM, serviced: true, aliases: ["HCM", "Sài Gòn", "SG", "Ho Chi Minh"] }
  # ...
unknown_province: ASK_AGAIN_THEN_HANDOFF
outside_service_area: INFORM_AND_LOG_LEAD
```

Bảng phía bot: mục 29 (Catalog).

------------------------------------------------------------------------

# 5. Knowledge Base

Knowledge Base là nguồn sự thật về chính sách, quy trình, cách tư vấn và phần giải thích sản phẩm. Số liệu (giá, gói, thông số, công suất) **không** nằm ở đây mà lấy từ catalog (mục 4.2) và công thức sizing qua tool.

Nguyên tắc: **con số đi qua tool, lời văn đi qua prompt.**

## 5.1 Phân loại kiến thức theo cách AI dùng

| Loại | Ví dụ | Nguồn | AI dùng thế nào |
|---|---|---|---|
| Số liệu | Giá, gói, thông số, bảo hành từng vật tư | `slm_v2` (mục 4.2) | Chỉ qua tool, không bao giờ nằm trong prompt |
| Công thức | Tính kWp, giới hạn theo diện tích mái, hệ số theo loại mái | `rules/sizing-rules.yaml` | Java (`SizingService`) tính, tool trả kết quả |
| Quy tắc tư vấn | Cần hỏi gì, hỏi theo thứ tự nào, khi nào chuyển người | `playbooks/` | Luôn có trong prompt |
| Phân loại khách | Mức tiềm năng HOT / WARM / COLD / OFF_SEGMENT | `rules/lead-scoring.yaml` (mục 58.5) | AI trích tín hiệu, backend tính mức |
| Chính sách | Bảo hành, thanh toán, quy trình lắp đặt, bảo trì | `policies/` | Luôn có trong prompt |
| FAQ / giải thích | On-grid hay hybrid, pin lưu trữ, vệ sinh tấm pin | `faq/` | Trong prompt; chuyển sang tra cứu khi lớn (mục 5.7) |
| Văn phong | Xưng hô, độ dài, emoji, câu cấm, chat mẫu | `core/`, `examples/` | Luôn có trong prompt |
| Thông tin công ty | Cơ sở HN/HCM, địa chỉ, hotline | `slm_v2` (`co_so`, `nganh_hang`) | Bản tóm tắt catalog |
| Media | Ảnh gói, video giải thích | Media Library (mục 6) | Danh mục media trong prompt; backend kiểm tra trước khi gửi (mục 7) |

## 5.2 Nơi lưu: tài liệu có phiên bản trong DB

Kiến thức được lưu trong PostgreSQL và sửa trực tiếp trên admin (mục 5.9); mỗi lần sửa là một phiên bản. Bot chỉ dùng **bản phát hành** đang kích hoạt (mục 5.3), nên sửa nháp không ảnh hưởng tới khách.

Tài liệu được tổ chức theo cây `ma_tai_lieu` dưới đây. Đây cũng là cấu trúc thư mục khi nhập lần đầu từ repo (`POST /admin/knowledge/import`) hoặc khi xuất bản đang chạy ra zip/Markdown để sao lưu.

``` text
knowledge/
├── core/                  # luôn nạp, gần như không đổi
│   ├── identity.md        # tên tư vấn viên (Huyền), phạm vi; khách hỏi có phải bot thì nói thật
│   ├── voice.md           # xưng hô anh/chị – em, độ dài, emoji, câu cấm
│   └── guardrails.md      # không bịa số, không hứa ROI, các trường hợp phải chuyển người
├── playbooks/             # cách dẫn dắt hội thoại
│   ├── tu-van-lap-moi.md  # chiến lược bán hàng (mục 58)
│   ├── khach-hoi-gia.md
│   ├── khach-chua-san-sang.md
│   ├── chuyen-nhan-vien.md
│   ├── thu-hut-chu-y.md   # thông tin nhắc và thu hút sự chú ý (mục 58.6)
│   └── gui-media.md       # khi nào gửi ảnh/video, giới hạn mỗi tin (mục 7.3)
├── policies/              # bao-hanh, thanh-toan, quy-trinh-lap-dat, bao-tri-ve-sinh
├── faq/                   # ongrid-vs-hybrid, pin-luu-tru, ...
├── examples/              # 10–20 đoạn chat mẫu tốt
├── content/               # chỉ cho Trợ lý nội dung (mục 26), không nạp vào bot chăm sóc khách
│   ├── chien-luoc.md      # trụ cột nội dung, tỷ lệ giáo dục / case study / bán hàng, hook, CTA
│   └── theo-kenh.md       # định dạng và văn phong bài đăng theo từng kênh
├── guide/                 # chỉ cho AI hỗ trợ soạn (mục 5.10), không nạp vào bot
│   ├── huong-dan-viet.md  # cách viết để AI đọc hiểu dễ
│   ├── thuat-ngu.md       # bảng thuật ngữ thống nhất
│   └── mau/               # mẫu theo loại tài liệu: faq, playbook, policy, example
└── rules/                 # cho code đọc, KHÔNG đưa nguyên vào prompt
    ├── sizing-rules.yaml  # công thức, hệ số, loại mái, giới hạn
    ├── consult-slots.yaml # thông tin cần thu thập (mục 58.4, 58.3)
    ├── lead-scoring.yaml  # phân loại mức tiềm năng (mục 58.5)
    ├── regions.yaml       # tỉnh → miền Bắc / Nam → cơ sở giá HN / HCM (mục 4.2.9)
    ├── topics.yaml        # bộ chủ đề dùng chung cho tài liệu và media
    └── content-schedule.yaml # quy tắc lịch đăng bài theo kênh (mục 26.4)
```

Mỗi tài liệu có các trường khai báo. Trên admin, các trường này nhập bằng form (không gõ YAML tay); khi xuất ra file thì ghi ở phần đầu:

``` yaml
---
ma_tai_lieu: policy.bao-hanh
loai_tai_lieu: policy # core | playbook | policy | faq | example | content | rule | guide | template
tieu_de: Chính sách bảo hành
chu_de: [bao-hanh]    # theo rules/topics.yaml
cac_kenh: [messenger, comment]
---
```

`consult-slots.yaml` là nguồn duy nhất định nghĩa các thông tin cần thu thập khi tư vấn (mục 58.4) và các tín hiệu phân loại khách (mục 58.3). Từ file này hệ thống sinh ra ba thứ:

- input schema của tool `tim_goi_phu_hop` và `cap_nhat_ho_so_tu_van`;
- danh sách cần hỏi đưa vào prompt;
- bộ kiểm tra dữ liệu của `ho_so_tu_van`.

Nhờ vậy ba chỗ này không bao giờ lệch nhau.

## 5.3 Phát hành và đồng bộ

Sửa xong không áp dụng ngay. Các bản nháp được gom thành **một bản phát hành**, kiểm tra, rồi mới kích hoạt.

``` text
Sửa trên admin (tay hoặc AI hỗ trợ, mục 5.10) ──► bản nháp, lưu bao nhiêu lần cũng được
      │
      ▼
Chọn các bản nháp ──► "Tạo bản phát hành" (bản nháp được đóng băng thành phiên bản FROZEN)
      │
      ▼
KnowledgeReleaseJob (chạy nền, admin xem tiến độ từng bước)
  1. Kiểm tra trường khai báo: ma_tai_lieu, chu_de có trong topics.yaml, cac_kenh hợp lệ, liên kết tới media còn tồn tại
  2. YAML kiểm tra theo schema; sizing-rules và lead-scoring phải chạy đúng bộ ca thử;
     regions: mỗi tỉnh một miền, mỗi miền trỏ tới cơ sở có trong catalog
  3. Ghép thử prompt, đếm token bằng count_tokens; chặn nếu vượt ngân sách (mục 5.4)
  4. Kiểm tra tức thì (mục 5.10 A): con số giá/công suất trong tài liệu, viết không dấu, thuật ngữ lệch
  5. Chạy các câu trong bộ test liên quan tới tài liệu đã sửa, so với bản đang chạy (bật từ Phase 2)
      │  đạt → READY            lỗi → FAILED, kèm báo cáo
      ▼
Admin bấm "Kích hoạt" (hoặc hẹn giờ)
      │  một transaction: bản mới ACTIVE, bản cũ SUPERSEDED, rồi NOTIFY knowledge_release
      ▼
Ứng dụng nhận thông báo (dự phòng: tự kiểm tra mỗi 30 giây)
  → dựng lại khối prompt ②; dựng lại cả định nghĩa tool nếu consult-slots đổi
  → nạp lại SizingService / lead-scoring / ScheduleRuleChecker nếu rules đổi
  → làm ấm cache bằng một request max_tokens = 0
  → lượt AI tiếp theo dùng phien_ban_kt mới; hội thoại đang dở chuyển sang bản mới ở lượt sau
```

- Phát hành theo bộ: sửa playbook và FAQ cùng lúc thì lên chung một bản, tránh giai đoạn bot dùng playbook mới với FAQ cũ.
- Bản phát hành không bao giờ bị sửa. **Rollback** là kích hoạt lại một bản cũ, xong ngay lập tức.
- Mỗi lượt gọi AI ghi `phien_ban_kt`, `phien_ban_catalog` và `phien_ban_media` vào `luot_goi_ai`, nên luôn truy ra được AI đã dựa vào phiên bản nào khi trả lời.

Hai nguồn khác cấp cho prompt cũng có phiên bản riêng:

``` text
slm_v2 ─► catalog-sync ─► CatalogDigestBuilder ─► bản tóm tắt catalog (phien_ban_catalog)
                          hãng, loại hệ, dải kWp, cơ sở, hotline; KHÔNG có giá

media ─► MediaIndexBuilder ─► danh mục media (phien_ban_media)
               mục APPROVED có nguồn ACTIVE: mã ngắn, tom_tat, dung_khi, tranh_khi (mục 7.1)
```

## 5.4 Ghép prompt và cache

``` text
① tools          định nghĩa cố định
② system         core + playbooks + policies + faq + examples   ◄ cache 1 giờ (phien_ban_kt)
③ system         tóm tắt catalog + danh mục media               ◄ cache 1 giờ (phien_ban_catalog, phien_ban_media)
④ messages       hồ sơ tư vấn + lịch sử + tin mới               ◄ cache tự động theo lượt
```

- **Thứ tự theo độ ổn định.** Catalog và media đổi thường xuyên hơn kiến thức (mỗi lần đồng bộ hoặc duyệt media), nên đặt sau. Khi đó chỉ cache của ③ bị làm mới, không ảnh hưởng ②.
- **Nội dung phải giống hệt nhau từng byte giữa các lượt.** Sắp xếp tài liệu theo `id`; không chèn timestamp hay dữ liệu thay đổi vào ② và ③.
- **Cache 1 giờ vì khách trả lời thưa.** Ghi cache 1 giờ tốn gấp đôi giá input, còn đọc cache chỉ bằng 0,1 lần. Với Sonnet 5.5, phần kiến thức lõi khoảng 30K token tốn chừng $0,006 mỗi lượt khi đọc cache.
- **Ngân sách token:** phần ② giữ dưới khoảng 40K token; loader chặn nếu vượt.
- Theo dõi `cache_read_input_tokens` trong `luot_goi_ai`: nếu luôn bằng 0 thì có thứ gì đó đang làm thay đổi phần đầu của prompt.

## 5.5 Thứ tự ưu tiên khi các nguồn mâu thuẫn

1. **Kết quả tool** (catalog, sizing): mọi con số.
2. **`guardrails` và `policies`.**
3. **`playbooks` và `faq`.**
4. **Kiến thức chung của model:** chỉ được giải thích khái niệm. Không đưa ra số liệu, không cam kết.

AI không tự đoán chính sách, giá hoặc thông số kỹ thuật. Nếu câu hỏi không có trong kiến thức, AI nói sẽ kiểm tra lại hoặc chuyển nhân viên, và ghi nhận một lỗ hổng kiến thức. Kết quả AI trả về có thêm hai trường (`knowledge_gap` là `null` khi đủ căn cứ):

``` json
{
  "sources": ["policy.bao-hanh", "faq.pin-luu-tru"],
  "knowledge_gap": { "question": "...", "reason": "..." }
}
```

## 5.6 Vòng cập nhật kiến thức

``` text
AI không có căn cứ ─► kt_lo_hong ─► "nhờ AI viết nháp" hoặc soạn tay ─► bản nháp ─► phát hành ─► kích hoạt
Nhân viên sửa nháp ─► kt_sua_ban_nhap    ─► kt_ung_vien_vi_du ─► admin duyệt ─► examples/
```

- Mỗi câu trong bộ test (eval) được gắn với các `ma_tai_lieu` liên quan; bước 5 khi phát hành chỉ chạy những câu liên quan tới tài liệu đã sửa.
- Soạn FAQ ban đầu: xuất danh sách câu hỏi thật từ inbox Facebook, nhờ AI viết nháp (mục 5.10 B), admin duyệt lại.
- Không tự động biến mọi chỉnh sửa thành luật (mục 35).

## 5.7 Khi nào mới cần RAG

Chỉ chuyển khi phần ② vượt khoảng 40K token, hoặc FAQ vượt khoảng 150 mục:

1. Chuyển `faq/` sang tool `tra_cuu_kien_thuc(truy_van)`, lúc đầu dùng full-text search của PostgreSQL có `unaccent` cho tiếng Việt.
2. Khi cần thì thêm pgvector cùng một nhà cung cấp embedding (Claude không có model embedding).
3. `core/`, `playbooks/`, `policies/` vẫn giữ trong prompt.

## 5.8 Tách file rule tư vấn của doanh nghiệp

| Phần trong file rule | Đi vào |
|---|---|
| Công thức, hệ số, diện tích/kWp theo loại mái, giới hạn | `rules/sizing-rules.yaml` → `SizingService` có unit test |
| Danh sách thông tin cần hỏi | `rules/consult-slots.yaml` |
| Cách hỏi, cách giải thích, khi nào chuyển người | `playbooks/tu-van-lap-moi.md` |

## 5.9 Trình soạn kiến thức trên admin (Knowledge Editor)

- **Danh sách tài liệu** theo loại, hiện phiên bản đang chạy, cờ "có nháp", cảnh báo kiểm tra.
- **Trình soạn Markdown có xem trước** (ví dụ `md-editor-v3`); tài liệu YAML dùng trình soạn có kiểm tra schema.
- **Trường khai báo nhập bằng form:** chọn `chu_de` từ `topics.yaml`, chọn `cac_kenh`; không gõ YAML tay.
- **Đếm token** tạm tính khi gõ, đếm chính xác khi lưu; hiện phần trăm ngân sách đã dùng.
- **Lịch sử phiên bản**, diff giữa hai bản, nút "khôi phục bản này" (tạo bản nháp mới từ bản cũ).
- **Chống ghi đè:** bản nháp ghi lại bản gốc đã sửa từ đó (`phien_ban_goc_id`); nếu trong lúc sửa đã có bản mới được phát hành thì báo xung đột và cho xem diff.
- **Quyền:** `KNOWLEDGE_EDITOR` sửa nháp; `KNOWLEDGE_PUBLISHER` phát hành, rollback. Ai sửa được kiến thức là điều khiển được bot nói gì với khách, nên cần đăng nhập mạnh và audit mọi lần phát hành.

## 5.10 AI hỗ trợ soạn

### A. Khi nhập trực tiếp: mẫu, kiểm tra và góp ý

**Mẫu theo loại tài liệu** (`guide/mau/`), chèn sẵn khi tạo tài liệu mới. Ví dụ mẫu FAQ:

``` markdown
# <Câu hỏi chính, viết như khách hỏi>

## Khách thường hỏi
- <cách hỏi 1>
- <cách hỏi 2>

## Trả lời ngắn
<1–3 câu, đúng trọng tâm>

## Giải thích thêm (khi khách hỏi tiếp)
<...>

## Không được nói
- <cam kết, con số không có nguồn...>

## Chuyển nhân viên khi
- <...>
```

Mẫu playbook:

``` markdown
# <Tên tình huống>

## Mục tiêu
## Khi nào dùng
## Các bước
1. ...
## Câu hỏi mẫu (mỗi lần hỏi 1 câu)
## Nên
## Không nên
## Ví dụ
```

**Kiểm tra tức thì** khi gõ (code chạy, không gọi AI):

| Kiểm tra | Mức |
|---|---|
| Thiếu mục bắt buộc của mẫu | Cảnh báo |
| Có con số giá (triệu, đ, VNĐ) hoặc công suất cụ thể | Cảnh báo: số liệu phải lấy từ catalog qua tool |
| Đoạn văn quá dài (hơn 5 câu) | Gợi ý tách |
| Câu mẫu gửi khách chứa nhiều câu hỏi | Gợi ý tách, theo nguyên tắc mỗi lần một câu (mục 58.2) |
| Viết không dấu | Cảnh báo |
| Thuật ngữ lệch bảng thuật ngữ (ví dụ "hybrid" / "Hy-Brid" / "lai") | Gợi ý thống nhất |
| `ma_tai_lieu`, mã media, topic không tồn tại | Chặn khi phát hành |
| Tổng token vượt ngân sách | Chặn khi phát hành |
| Trùng chủ đề với tài liệu khác | Cảnh báo, kèm link tới tài liệu đó |

**Nút "AI góp ý":** Claude đọc bản nháp cùng `guide/huong-dan-viet.md` và toàn bộ kiến thức đang chạy, rồi trả danh sách góp ý: chỗ mơ hồ, mâu thuẫn với tài liệu khác, thiếu trường hợp, câu nên viết lại. Mỗi góp ý có nút "Áp dụng" để cập nhật bản nháp.

**Hướng dẫn viết** (`guide/huong-dan-viet.md`), dùng cho cả người soạn lẫn AI hỗ trợ soạn:

- Một tài liệu một chủ đề; tiêu đề nói rõ tình huống.
- Viết quy tắc rõ ràng: "Khi … thì …", "Luôn …", "Không bao giờ …", kèm lý do ngắn, vì AI làm theo tốt hơn khi hiểu vì sao.
- Câu ngắn; các bước viết thành danh sách đánh số.
- Có ví dụ câu trả lời mẫu đúng văn phong.
- Không ghi giá, công suất, thông số cụ thể: những số này lấy từ catalog qua tool.
- Dùng đúng thuật ngữ trong `guide/thuat-ngu.md`; viết tiếng Việt có dấu.
- Ghi rõ khi nào phải chuyển nhân viên.

### B. Nhập thô yêu cầu → AI viết → xem trước → duyệt

``` text
Admin nhập yêu cầu thô, ví dụ: "Mái bằng thì phải báo khách là phát sinh phần khung,
nhân viên báo giá sau khảo sát", có thể chọn tài liệu đích hoặc để AI tự tìm
      │
      ▼
AI hỗ trợ soạn đọc: hướng dẫn viết + mẫu + thuật ngữ + topics + toàn bộ kiến thức đang chạy
      │
      ▼
Đề xuất thay đổi (kt_de_xuat_sua): sửa tài liệu nào / tạo tài liệu mới,
nội dung mới, tóm tắt thay đổi, lý do, mâu thuẫn tìm thấy
      │
      ▼
Xem trước: bản hiển thị + diff với bản đang chạy + kết quả kiểm tra tức thì
           + thay đổi số token + cảnh báo mâu thuẫn
      │
      ├── "Sửa tiếp" trong chat ("ngắn hơn", "thêm ví dụ") ──► đề xuất mới
      ├── Từ chối ──► không có gì thay đổi
      └── Duyệt ──► tạo bản nháp (co_ai_ho_tro = true) ──► phát hành như mục 5.3
                    (nút "Duyệt & phát hành": chạy luôn mục 5.3 và tự kích hoạt nếu mọi bước đạt)
```

- AI **không tự phát hành**; mọi thay đổi chỉ áp dụng sau khi người duyệt.
- Thay đổi ở tài liệu YAML (`sizing-rules`, `lead-scoring`, `consult-slots`) được tô nổi bật từng con số và phải qua schema cùng bộ ca thử.
- Model: Claude Opus 5.5 (ít lượt, cần viết tốt); phiên chat lưu ở `tl_phien_chat` với `loai_tro_ly = KNOWLEDGE`; mỗi lượt ghi `luot_goi_ai` với `muc_dich = KNOWLEDGE_ASSISTANT`.
- Cũng dùng được từ `kt_lo_hong`: bấm "nhờ AI viết nháp" trên một lỗ hổng kiến thức là mở sẵn phiên với câu hỏi đó.

Bảng dữ liệu: mục 29 (Knowledge, Assistant, AI Runs).

------------------------------------------------------------------------

# 6. Media Library

Thư viện media dùng chung cho **ảnh và video**, không chỉ lưu URL YouTube. Một mục media có thể có nhiều nguồn: file tự upload, link YouTube, Facebook, TikTok, hoặc ảnh đồng bộ từ `slm_v2`.

## 6.1 Mô hình dữ liệu

``` text
media                            media_nguon                               media_lien_ket
(nội dung: 1 dòng/mục)  1─N      (nơi chứa: upload/YT/FB/TikTok)           (gắn với gói, vật tư, hãng, tài liệu kiến thức)
 loai_media, tieu_de, tom_tat,   url_nguon, id_ngoai,                      PACKAGE / MATERIAL / BRAND / KNOWLEDGE_DOC
 dung_khi, chu_de, trang_thai    duong_dan_luu_tru, trang_thai_truy_cap    vai trò: ảnh chính, gallery, giải thích...
                                     │
                                     └─1─N─ media_tham_chieu_kenh (attachment_id đã upload lên từng kênh/Page)
```

Tách nội dung (`media`) khỏi nguồn (`media_nguon`) vì cùng một video có thể vừa có file gốc, vừa đăng YouTube, vừa đăng trên Page:

- AI chọn **nội dung**, không chọn link.
- Backend chọn **nguồn** phù hợp nhất với kênh đang gửi.
- Tiêu đề, mô tả, transcript, `tom_tat`, `dung_khi`, `chu_de` chỉ lưu một lần.

Schema chi tiết ở mục 29.

## 6.2 Loại media, nguồn và trạng thái

| Trường | Giá trị |
|---|---|
| `media.loai_media` | `IMAGE`, `VIDEO` |
| `media_nguon.loai_nguon` | `UPLOAD` (file tự upload lên MinIO/S3), `YOUTUBE`, `FACEBOOK`, `TIKTOK`, `SLM_V2` (ảnh gói/vật tư đồng bộ từ hệ thống bảng giá, chỉ đọc) |
| `media.trang_thai` | `DRAFT`, `APPROVED`, `ARCHIVED`; AI chỉ dùng mục `APPROVED` |
| `media.quyen_su_dung` | `OWNED`, `LICENSED`, `LINK_ONLY`, `NEEDS_REVIEW` |
| `media_nguon.trang_thai_xu_ly` | `PENDING`, `PROCESSING`, `READY`, `FAILED` |
| `media_nguon.trang_thai_truy_cap` | `ACTIVE`, `PRIVATE`, `UNAVAILABLE`, `DELETED`, `UNKNOWN` |

**Metadata để AI chọn đúng (mục 7):**

| Trường | Ví dụ | Vai trò |
|---|---|---|
| `tom_tat` | "Giải thích hệ Hybrid lưu điện ban ngày để dùng buổi tối, minh họa nhà 5 kWp + pin 5 kWh" | AI biết media nói gì |
| `dung_khi` | "Khách đi làm ban ngày, dùng điện chủ yếu buổi tối; khách hỏi pin lưu trữ có đáng không" | AI biết **khi nào** nên gửi |
| `tranh_khi` | "Khách chỉ dùng điện ban ngày; khách đang khiếu nại" | Tránh gửi sai ngữ cảnh (tùy chọn) |
| `chu_de` | `[hybrid, pin-luu-tru]` | Lấy từ bộ chủ đề cố định, không gõ tự do |
| `mo_ta`, `loi_thoai` | Mô tả chi tiết, lời thoại video | Nguồn để viết `tom_tat`; dùng cho tìm kiếm khi thư viện lớn |

- Mục `APPROVED` bắt buộc có `tom_tat` và `dung_khi` (ràng buộc trong DDL, mục 29).
- **Bộ chủ đề cố định** nằm ở `knowledge/rules/topics.yaml` (hybrid, on-grid, pin-luu-tru, lap-dat, bao-tri, bao-hanh, hoa-don, case-study...). Tài liệu kiến thức dùng chung bộ chủ đề này (trường `chu_de` ở phần đầu khai báo, mục 5.2), nên tài liệu và media khớp được với nhau.
- Admin không phải viết tay từ đầu: với video, Claude đọc tiêu đề, mô tả và transcript (nếu có) để viết nháp `tom_tat`, `dung_khi`, `tranh_khi`, `chu_de`; với ảnh, Claude nhìn ảnh để viết. Admin sửa rồi duyệt.

## 6.3 Đưa media vào thư viện

**A. Tự upload ảnh hoặc video**

1. Vben gọi `POST /admin/media/uploads`. Backend kiểm tra loại file và dung lượng, tạo item `DRAFT`, trả về URL upload có thời hạn (MinIO, bucket riêng `slm-ai-media`).
2. Trình duyệt upload thẳng lên MinIO, không đi qua backend.
3. Gọi `POST /admin/media/uploads/{id}/complete`. Worker kiểm tra định dạng thật của file và tính sha256 để chống trùng.
   - Ảnh: đọc kích thước, tạo thumbnail.
   - Video: `ffprobe` lấy thời lượng và codec, cắt một khung hình làm thumbnail.
   - Xong thì chuyển `trang_thai_xu_ly = READY`.
4. Claude viết nháp `tom_tat`, `dung_khi`, `tranh_khi`, `chu_de` (ảnh: nhìn ảnh; video: từ tiêu đề, mô tả, transcript nếu có). Admin sửa, gắn vào gói hoặc tài liệu kiến thức, rồi duyệt `APPROVED`.

**B. Dán link YouTube / Facebook / TikTok**

| Nền tảng | Dạng URL nhận diện | Lấy metadata |
|---|---|---|
| YouTube | `watch?v=`, `youtu.be/`, `shorts/`, `embed/` | oEmbed công khai |
| Facebook | `/videos/{id}`, `watch/?v=`, `reel/{id}`, `fb.watch/…` (mở link rút gọn ra link thật) | Video của Page mình: Graph API bằng page token. Video khác: oEmbed (cần app token) |
| TikTok | `/@user/video/{id}`, `vt.`/`vm.tiktok.com/…` (mở link rút gọn ra link thật) | oEmbed công khai |

- Link được chuẩn hóa thành `id_ngoai`; unique `(loai_nguon, id_ngoai)` chống dán trùng.
- Sau khi lấy metadata, Claude viết nháp `tom_tat`, `dung_khi`, `tranh_khi`, `chu_de` từ tiêu đề, mô tả (và transcript nếu có); admin sửa và duyệt.
- Nguồn ngoài mặc định `LINK_ONLY`: chỉ gửi link, **không tải về rồi đăng lại**.
- Job định kỳ kiểm tra link còn xem được không. Link chết chuyển `UNAVAILABLE`: AI không gợi ý nữa nhưng vẫn giữ lịch sử.

**C. Ảnh đồng bộ từ `slm_v2`**

- `catalog-sync` tạo media `SLM_V2` từ `tron_goi.tep_tin_id` và `anh_vat_tu` (`id_ngoai = tep_tin.id`), gắn `media_lien_ket` vai trò `MAIN_IMAGE`/`GALLERY` vào gói hoặc vật tư tương ứng.
- Khi AI đề xuất một gói thì có sẵn ảnh để gửi kèm, không phải upload lại.
- Loại này chỉ đọc, cập nhật theo đợt đồng bộ.
- Sync tự điền `tom_tat` (tên gói/vật tư) và `dung_khi` ("gửi kèm khi giới thiệu gói/vật tư này"). Các mục này được gắn tự động (mục 7.2), không nằm trong danh mục media.

**D. Nhập ảnh combo từ Google Drive (đã làm 2026-10-26)**

- Nguồn: bảng combo của doanh nghiệp (link Drive, link Facebook, link web theo `tron_goi.id`). Bảng **không commit** lên repo công khai (`.gitignore`: `*.xlsx`, `local-data/`); admin dán danh sách JSON tạo từ bảng vào trang **Thư viện media → Nhập combo từ Drive** (`POST /api/v1/admin/media/imports/package-images`, tối đa 10 mục mỗi lần, có chạy thử).
- Ảnh Drive được tải về MinIO và lưu như `UPLOAD`: `url_nguon` = link Drive gốc, `id_ngoai = gdrive:<file_id>`, key theo nội dung `media/images/<2 ký tự đầu sha256>/<sha256>.<đuôi>`. Không thêm loại nguồn mới vì bản trong MinIO mới là bản dùng.
- An toàn khi tải: chỉ lấy file id rồi tự dựng URL tải; chỉ đi theo chuyển hướng HTTPS tới `drive.google.com` / `drive.usercontent.google.com` (tối đa 5 lần); tối đa 15 MB; kiểm tra magic bytes JPEG/PNG/WebP (Drive trả trang HTML đăng nhập với mã 200 khi file chưa chia sẻ công khai).
- Link Facebook (`/share/r/…` reel, `/share/v/…` video) lưu là `FACEBOOK`, `LINK_ONLY`, `id_ngoai = share-r:<mã>` / `share-v:<mã>`; server chưa tải gì về.
- Liên kết `PACKAGE` với **cả gói HN và gói HCM cùng tên** (`MAIN_IMAGE` cho ảnh, `RELATED` cho video). Mỗi gói chỉ một ảnh chính (unique index); gói đã có ảnh chính khác thì giữ nguyên và báo lại. Ảnh `SLM_V2` khi đồng bộ catalog vào sau sẽ là `GALLERY`.
- Media nhập vào ở trạng thái `DRAFT`, quyền `OWNED` (ảnh của công ty): AI chưa dùng cho tới khi admin viết `tom_tat`, `dung_khi` và duyệt.
- Chạy lại nhiều lần an toàn: trùng `gdrive:` id hoặc sha256 thì báo `EXISTS`, chỉ bổ sung liên kết còn thiếu.

## 6.4 Gửi media qua kênh

Backend chọn nguồn theo kênh. Thứ tự mặc định cho Messenger (cấu hình được):

| Loại | Thứ tự ưu tiên | Cách gửi |
|---|---|---|
| Ảnh | `UPLOAD` / `SLM_V2` | Tệp đính kèm, dùng lại `attachment_id` lưu trong `media_tham_chieu_kenh` |
| Video | `FACEBOOK` → `UPLOAD` (nếu dưới giới hạn đính kèm của Messenger, khoảng 25MB) → `YOUTUBE` → `TIKTOK` | Video Page và link ngoài gửi dạng link có preview; file nhỏ gửi đính kèm |

- Video upload dung lượng lớn: nên đăng lên Page rồi thêm nguồn `FACEBOOK` cho cùng mục.
- Không gửi link `http://IP:9000` cho khách; media đi qua domain HTTPS, đặt reverse proxy trước MinIO.
- Zalo OA và TikTok DM: chỉ gửi loại media mà adapter khai báo trong capability (mục 54.1); mặc định ảnh gửi đính kèm nếu được hỗ trợ, video gửi dạng link.
- Đăng lại media lên Facebook/TikTok/Zalo (mục 55) chỉ dùng nguồn `UPLOAD` có `quyen_su_dung` là `OWNED` hoặc `LICENSED`. URL YouTube/Facebook/TikTok không được coi là file nguồn để tải lên lại.

------------------------------------------------------------------------

# 7. Media Selection

AI chỉ hiểu một media qua phần chữ mô tả nó (`tom_tat`, `dung_khi`, `tranh_khi`, `chu_de`; mục 6.2). Vì vậy chọn đúng media cần đủ ba thứ: metadata mô tả đúng nội dung, AI nhìn thấy các ứng viên kèm metadata đó, và quy tắc chọn rõ ràng.

## 7.1 Thư viện nhỏ: danh mục media trong prompt

Khi thư viện còn dưới khoảng 150 mục, AI không cần đi tìm. `MediaIndexBuilder` dựng một **danh mục media** gọn, đặt trong phần prompt có cache cùng bản tóm tắt catalog (mục 5.4), khoảng 60 token mỗi mục:

``` text
[M07] VIDEO 3:20 · Hệ Hybrid: dùng điện mặt trời vào buổi tối
  chủ đề: hybrid, pin-luu-tru · giai đoạn: LEAD, CONSULTING · liên quan: faq.pin-luu-tru
  dùng khi: khách đi làm ban ngày, dùng điện chủ yếu buổi tối, hỏi pin lưu trữ
  tránh khi: khách chỉ dùng điện ban ngày
[M03] VIDEO 2:10 · On-grid hoạt động thế nào
  chủ đề: on-grid · liên quan: faq.ongrid-vs-hybrid
  dùng khi: khách dùng nhiều điện ban ngày, hỏi cơ bản về điện mặt trời
```

- Chỉ chứa mục `APPROVED`, có ít nhất một nguồn `ACTIVE`, và kênh hiện tại nằm trong `kenh_cho_phep`. Không chứa các mục chỉ có nguồn `SLM_V2` (ảnh gói/vật tư được gắn tự động, mục 7.2).
- Mã ngắn (`M07`) lưu ở `media.ma_danh_muc`; backend ánh xạ mã này về `media.id`.
- Danh mục ghi sẵn tài liệu liên quan của từng mục (lấy từ `media_lien_ket`), nên khi AI trả lời dựa trên tài liệu nào thì thấy ngay media đi kèm tài liệu đó.
- Sắp xếp cố định theo mã để nội dung giống hệt nhau giữa các lượt. Khi media được duyệt, lưu trữ hoặc bị đánh dấu link chết thì danh mục được dựng lại, `phien_ban_media` đổi và chỉ phần cache ③ bị làm mới.
- Danh sách media đã gửi trong hội thoại này (lấy từ `media_lan_gui`) nằm ở phần động của prompt, không nằm trong cache.

Claude đọc cả hội thoại cùng danh mục, hiểu theo ngữ nghĩa chứ không khớp từ khóa, rồi chọn mã media hoặc không chọn gì:

``` text
Prompt (cache): danh mục media ──► Claude: attachments: [{id: "M07", reason: "..."}]
                                          ↓
MediaResolver kiểm tra: còn APPROVED / ACTIVE? kênh cho phép? đã gửi trong hội thoại chưa?
                        đúng giới hạn số media mỗi tin?
                                          ↓
Chọn nguồn theo kênh (mục 6.4) ──► gửi ──► ghi media_lan_gui
```

Với cách này không cần tool tìm kiếm: `MediaResolver` là một bước của backend sau khi Claude trả kết quả, không phải tool cho AI gọi. Mã không có trong danh mục hoặc không qua kiểm tra thì bị bỏ, lý do được ghi vào `luot_goi_ai.ket_qua_policy`.

## 7.2 Media được gắn tự động

Không phải lúc nào AI cũng phải chọn:

- **Ảnh gói:** khi `tim_goi_phu_hop` trả về gói, backend tự gắn ảnh `MAIN_IMAGE` của gói đó (nguồn `SLM_V2` hoặc ảnh upload thay thế).
- **Ảnh vật tư:** khi `tra_cuu_vat_tu` trả về vật tư có ảnh chính, ảnh được gửi kèm nếu khách hỏi về chính vật tư đó.

Các lần gắn tự động vẫn đi qua `MediaResolver` và được ghi vào `media_lan_gui` với `chon_boi = AUTO`.

## 7.3 Quy tắc chọn

Ghi trong `knowledge/playbooks/gui-media.md`:

- Chỉ gửi khi media trả lời đúng câu hỏi hoặc mối lo hiện tại của khách.
- Mỗi tin tối đa 1 video và 1–2 ảnh. Không gửi lại media đã gửi trong hội thoại.
- Câu trả lời phải tự đứng được khi khách không mở video; video chỉ là bổ trợ.
- Giới thiệu ngắn video nói về gì và dài bao lâu.
- Không có mục phù hợp thì không gửi. Gửi sai còn tệ hơn không gửi.

## 7.4 Thư viện lớn: chuyển sang tìm kiếm

Khi vượt khoảng 150 mục, hoặc danh mục vượt khoảng 10K token:

1. Bỏ danh mục khỏi prompt, thêm tool `tim_media(truy_van, chu_de?, loai_media?)`: full-text search (PostgreSQL + `unaccent`) trên `tom_tat`, `dung_khi`, `loi_thoai`, lọc theo `chu_de`, kênh và trạng thái.
2. Tool trả về 5–8 ứng viên **kèm `tom_tat`, `dung_khi`, `tranh_khi` và thời lượng**; Claude chọn như mục 7.1, `MediaResolver` vẫn kiểm tra trước khi gửi.
3. Khi cần thì thêm embedding (Claude không có model embedding).

## 7.5 Đo và sửa khi AI chọn sai

- Bộ test có các câu kèm media kỳ vọng, ví dụ "pin lưu trữ hoạt động thế nào" phải ra M07 hoặc một mục có chủ đề `pin-luu-tru`.
- Inbox admin hiện "AI đã gửi M07 vì …" (lấy từ `reason`). Chọn sai thường do `dung_khi` hoặc `tranh_khi` viết chưa rõ: sửa metadata, không cần sửa code.
- `media_lan_gui` ghi lần gửi, khách có trả lời sau đó không, có tạo lead không; dùng để xếp hạng media khi đủ dữ liệu.

## 7.6 Ví dụ

> Nhà em ban ngày ít người ở, có nên lắp điện mặt trời không?

AI dùng:

-   Knowledge về self-consumption (`faq.ongrid-vs-hybrid`).
-   M07 trong danh mục: "dùng khi khách đi làm ban ngày, dùng điện chủ yếu buổi tối".
-   Nếu tư vấn tới gói cụ thể: ảnh `MAIN_IMAGE` của gói Hy-Brid được gắn tự động.

AI không chọn M03 vì "dùng khi" của M03 là khách dùng nhiều điện ban ngày, ngược với tình huống này.

------------------------------------------------------------------------

# 8. Không phải câu hỏi nào cũng gửi media

AI cần quyết định:

``` text
MEDIA_REQUIRED = YES
MEDIA_REQUIRED = NO
MEDIA_REQUIRED = OPTIONAL
```

Ví dụ:

> "Bảo hành bao lâu?"

→ Không nhất thiết gửi media.

> "Pin lưu trữ hoạt động thế nào?"

→ Có thể gửi video giải thích.

> "Gói 5 kWp gồm những gì?"

→ Gửi ảnh gói (`MAIN_IMAGE`) kèm danh sách thành phần.

------------------------------------------------------------------------

# 9. Style & Human Conversation Library

Đây là phần giúp AI có phong cách giống cách công ty thực sự giao tiếp. Phải có 3 tầng:

``` text
Brand Voice            → knowledge/core/voice.md
+
Human Writing Style    → knowledge/examples/   (đoạn chat mẫu đã duyệt)
+
Conversation Strategy  → knowledge/playbooks/
```

Ba tầng này là một phần của Knowledge Base (mục 5): cùng cách lưu, cùng phiên bản, nằm trong prompt có cache. Giai đoạn đầu không dùng bảng riêng có embedding hay vector search.

Nguồn đoạn chat mẫu:

- Bản nháp AI được nhân viên sửa (`kt_sua_ban_nhap`, mục 35).
- Chat cũ có chất lượng tốt, nhập vào hàng chờ duyệt `kt_ung_vien_vi_du` (mục 29) sau khi ẩn tên, số điện thoại, địa chỉ.

Chỉ admin mới đưa một ứng viên vào `examples/` (mục 14).
------------------------------------------------------------------------

# 10. Brand Voice

Ví dụ:

``` text
Tone:
Thân thiện
Gần gũi
Có chuyên môn nhưng dễ hiểu

Cách xưng hô:
Công ty → anh/chị

Ưu tiên:
- Tư vấn trước, bán hàng sau
- Hỏi nhu cầu trước
- Giải thích đơn giản
- Không gây áp lực

Không:
- Ép mua
- Nói quá quảng cáo
- Hứa điều chưa chắc chắn
- Dùng thuật ngữ khó nếu không cần
```

Lưu trong `knowledge/core/voice.md` (mục 5.2), gồm: giọng văn, xưng hô, độ dài câu, cách dùng emoji, cách bán hàng, mẫu câu nên dùng và câu cấm. Văn phong riêng từng kênh nằm ở mục 15.
------------------------------------------------------------------------

# 11. Human Writing Examples

Những đoạn văn/đoạn chat thật có chất lượng tốt, lưu thành tài liệu trong `knowledge/examples/` (ví dụ `messenger.md`, `comment.md`). Mỗi đoạn ghi kênh, chủ đề, giai đoạn khách:

``` text
Channel:
Facebook

Topic:
Battery

Stage:
LEAD

Quality:
5/5
```

AI dùng những ví dụ này để học cách hành văn. Ban đầu chỉ cần 10–20 đoạn chọn lọc; nhiều hơn chưa chắc tốt hơn mà tốn ngân sách token (mục 5.4).
------------------------------------------------------------------------

# 12. Human Conversation Examples

Đây là dữ liệu quan trọng hơn các đoạn văn đơn lẻ: một cặp tin khách – câu trả lời của người thật, kèm ngữ cảnh và kết quả.

Ví dụ:

``` text
Customer:
Nhà em tiền điện khoảng 2 triệu,
lắp hết bao nhiêu anh?

Human:
Dạ, để em tính tương đối sát cho nhà mình nhé.
Anh cho em hỏi nhà mình thường dùng điện
nhiều vào ban ngày hay buổi tối ạ?

Outcome:
Customer continued conversation

Quality:
5/5
```

Ứng viên nằm ở bảng `kt_ung_vien_vi_du` (mục 29); đoạn đã duyệt được đưa vào `knowledge/examples/`.
------------------------------------------------------------------------

# 13. Conversation Strategy

AI không chỉ học câu chữ.

AI phải học:

``` text
Khách hỏi giá
      ↓
Báo khoảng giá theo loại hệ (tool) + hỏi 1 câu
      ↓
Hỏi nhu cầu, mỗi lần 1 câu
      ↓
Tìm thời gian sử dụng điện
      ↓
Xác định pain point
      ↓
Giải thích
      ↓
Đề xuất gói cụ thể
      ↓
Báo giá chính xác của gói (tool)
```

Lưu thành tài liệu playbook trong `knowledge/playbooks/` (ví dụ `khach-hoi-gia.md`): các bước, câu hỏi nên hỏi, nên làm, không nên làm, ví dụ.

Ví dụ:

``` text
PLAYBOOK:
Khách hỏi giá

1. Báo khoảng giá theo loại hệ (tool) + hỏi 1 câu
2. Hỏi hóa đơn điện
3. Hỏi thời gian sử dụng điện (ngày / đêm)
4. Hỏi khu vực, 1 pha / 3 pha, loại mái
5. Xác định giải pháp (tool tim_goi_phu_hop)
6. Tư vấn, báo giá chính xác của gói
7. Lấy thông tin liên hệ
8. Chuyển sales theo mức tiềm năng (mục 58.5)
```

Thông tin cần thu thập ở bước 2–4 lấy từ `consult-slots.yaml` (mục 5.2), để playbook và tool luôn khớp nhau. Chiến lược bán hàng hiện hành và cách phân loại khách: mục 58.
------------------------------------------------------------------------

# 14. Chất lượng dữ liệu hội thoại

Không được đưa toàn bộ chat cũ vào AI một cách mù quáng.

Mỗi ứng viên trong `kt_ung_vien_vi_du` được chấm:

``` text
diem_chat_luong

5 = Excellent
4 = Good
3 = Normal
2 = Poor
1 = Do not use
```

- Chỉ đưa vào `knowledge/examples/` các đoạn điểm 4–5, do admin chọn.
- Điểm 3 giữ lại để tham khảo, không đưa vào prompt.
- Điểm ≤ 2 không bao giờ dùng làm ví dụ văn phong.
- Trước khi duyệt phải ẩn tên, số điện thoại, địa chỉ và mọi thông tin nhận diện khách.
------------------------------------------------------------------------

# 15. Style theo từng kênh

Không dùng một văn phong cho mọi kênh.

## Facebook Comment

-   Ngắn.
-   Tự nhiên.
-   Có thể công khai.
-   Không hỏi thông tin nhạy cảm.

## Facebook Messenger

-   Thân thiện.
-   Hỏi đáp từng bước.
-   Tư vấn sâu hơn.

## Zalo

-   CSKH.
-   Rõ ràng.
-   Có thể gửi tài liệu/video.
-   Phù hợp chăm sóc khách cũ.
-   Báo cáo, nhắc lịch gửi chủ động qua ZNS theo template đã duyệt (mục 54.4).

## TikTok

-   Caption ngắn, câu đầu là hook.
-   Hashtag theo chủ đề, không nhồi nhét.
-   DM: như Messenger nhưng ngắn hơn.

## Báo cáo điện năng

-   Chuyên nghiệp.
-   Có số liệu.
-   Dễ hiểu.
-   Có khuyến nghị.

## Marketing

-   Có hook.
-   Nêu vấn đề.
-   Giải pháp.
-   CTA.

------------------------------------------------------------------------

# 16. AI Personas

Có thể ghi lại cách tư vấn của những nhân viên hiệu quả:

``` text
AI Persona
-------------------
Tên:
Sales A

Tone:
Thân thiện

Strength:
Tư vấn nhẹ nhàng

Approach:
Hỏi nhu cầu → phân tích → đề xuất
```

Có thể chọn:

``` text
Company Default
Sales Style A
Sales Style B
CSKH Style
Marketing Style
```

Persona lưu thành các phần trong `knowledge/core/voice.md`, kèm vài đoạn mẫu trong `examples/`. Giai đoạn đầu chỉ dùng `Company Default`.

**Danh tính (doanh nghiệp chốt ngày 2026-10-24):** AI tự giới thiệu bằng tên tư vấn viên cấu hình được (`AI_PERSONA_NAME`, mặc định "Huyền"): "em là Huyền, tư vấn viên điện mặt trời SolarMax", không nhắc chữ AI trong lời chào. Giới hạn bắt buộc (`core/identity.md`):

- Khách hỏi thật lòng đang nói chuyện với người hay máy thì AI luôn nói thật là trợ lý tư vấn tự động, kèm lời mời nói chuyện với nhân viên.
- Không bao giờ khẳng định mình là người thật, không bịa chuyện đời tư, không nhận làm việc chỉ người làm được (gọi điện, khảo sát).
- Không dùng tên của một nhân viên có thật.

**Xưng hô:** AI gọi khách là anh hoặc chị theo thứ tự: khách tự xưng → tên Facebook rõ giới (chữ đệm "Thị" / "Văn", tên điển hình) → không chắc thì "anh/chị". Tên Facebook lấy một lần qua User Profile API của Messenger, lưu ở `dinh_danh_kenh.ten_hien_thi`. API này cần Advanced Access cho tính năng **Business Asset User Profile Access** (xin trong App Review cùng `pages_messaging`); chưa được duyệt thì API không trả tên và AI chỉ dựa vào cách khách tự xưng; cách gọi đã xác định lưu ở `khach_hang.so_thich.danh_xung` và đổi ngay khi khách tự xưng khác (`core/voice.md`).
------------------------------------------------------------------------

# 17. AI Memory

Hệ thống có 5 nhóm memory chính:

``` text
                    AI MEMORY
                       │
      ┌────────────────┼─────────────────┐
      │                │                 │
      ▼                ▼                 ▼
Customer Memory   Knowledge Memory   Style Memory
      │                │                 │
      │                │                 │
      ▼                ▼                 ▼
Lịch sử khách     Sản phẩm/FAQ       Văn phong
Nhu cầu           Chính sách         Chat mẫu
Solar data        Kỹ thuật           Strategy
      │                │                 │
      └────────────────┼─────────────────┘
                       │
                       ▼
                 Content Memory
                       │
                 Media / Posts
```

Quan trọng:

**Customer Memory và Style Memory phải tách biệt.**

Ví dụ:

> Anh A thích câu trả lời ngắn.

là Customer Memory.

> Công ty thường hỏi nhu cầu trước khi báo giá.

là Style Memory.

------------------------------------------------------------------------

# 18. Kết quả có cấu trúc của AI

Không tách "Response Planner" thành một lời gọi AI riêng. Trong cùng một lượt gọi, Claude trả về một kết quả có cấu trúc (structured output); backend đọc từng trường để quyết định bước tiếp theo.

Kết quả gồm đúng những gì trước đây Response Planner quyết định:

1.  Mục tiêu của lượt này (hỏi thêm, giải thích, đề xuất gói, xin thông tin liên hệ).
2.  Loại câu hỏi (`intent`, danh sách ở mục 35), dùng để quyết định gửi tự động hay để nháp.
3.  Câu trả lời gửi khách.
4.  Thông tin tư vấn mới thu thập được, câu vừa hỏi khách, và tín hiệu phân loại khách (mục 58.3).
5.  Media đính kèm và lý do (mục 7).
6.  Tài liệu kiến thức đã dựa vào.
7.  Lỗ hổng kiến thức, nếu có.
8.  Có cần chuyển nhân viên không, vì sao.

Ví dụ:

``` json
{
  "goal": "qualify_lead",
  "intent": "HOI_GIA",
  "reply": "Dạ, để em tính sát cho nhà mình nhé. Nhà mình dùng điện nhiều vào ban ngày hay buổi tối ạ?",
  "collected": { "hoa_don_thang": 2000000, "khu_vuc": "Hà Nội" },
  "question_asked": "ty_le_ngay_dem",
  "qualification": {
    "research_level": { "value": "SO_BO", "evidence": "khách hỏi on-grid khác hybrid thế nào" },
    "budget_vnd": null,
    "timeline": null
  },
  "attachments": [],
  "sources": ["playbook.khach-hoi-gia"],
  "knowledge_gap": null,
  "handoff": { "needed": false, "reason": null }
}
```

Chỉ tách thêm bước lập kế hoạch riêng khi bộ test (mục 35) cho thấy trả lời tốt hơn rõ rệt, vì mỗi bước thêm là thêm độ trễ và chi phí.
------------------------------------------------------------------------

# 19. Luồng AI trả lời khách

``` text
Webhook ─► xác thực chữ ký ─► su_kien_webhook (chống trùng) ─► trả 200
        ↓
Nhận diện khách (dinh_danh_kenh → khach_hang) ─► lưu tin_nhan (IN)
        ↓
Gộp tin: hoi_thoai.han_tra_loi_luc = bây giờ + 6 giây (mỗi tin mới lùi hạn lại)
        ↓
Worker lấy hội thoại tới hạn, khóa dòng (FOR UPDATE SKIP LOCKED):
mỗi hội thoại chỉ một tiến trình xử lý tại một thời điểm
        ↓
nguoi_phu_trach = HUMAN_OWNED? ─► dừng
        ↓
Orchestrator ghép ngữ cảnh (mục 5.4, 32) ─► Claude + tools ─► kết quả có cấu trúc (mục 18)
        ↓
MediaResolver + Policy Engine (mục 7.1, 33)
        ↓
┌──────────────┬────────────────────┬──────────────────┬──────────────────────────────┐
▼              ▼                    ▼                  ▼
Gửi ngay       Nháp chờ duyệt       Chuyển nhân viên   Không gửi
(AUTO)         (DRAFT)              (HUMAN_OWNED)      (ngoài cửa sổ → tạo việc cho sales)
        ↓
Ghi tin_nhan (OUT), luot_goi_ai, media_lan_gui, kt_lo_hong, ho_so_tu_van
```

- Thời gian gộp tin cấu hình được (5–8 giây).
- Nếu khách nhắn thêm trong lúc AI đang xử lý, kết quả cũ bị bỏ và hội thoại được xử lý lại với đủ các tin.
- **Tác dụng phụ của tool chỉ áp dụng khi lượt được chốt.** Tool đọc chạy ngay. Tool ghi (`cap_nhat_ho_so_tu_van`, `tao_lead`, `chuyen_nhan_vien`) chỉ ghi vào danh sách chờ của lượt; khi lượt qua Policy và được chốt (gửi, lưu nháp hoặc chuyển người) thì mới áp dụng, trong cùng transaction với `tin_nhan` (OUT) và `luot_goi_ai`. Lượt bị bỏ thì danh sách chờ bị hủy. Khóa chống trùng của lead tính theo `hoi_thoai_id` + số điện thoại, nên chạy lại không tạo lead trùng.
- Nhân viên trả lời từ Page Inbox (sự kiện echo) → `HUMAN_OWNED` ngay, kể cả khi AI đang soạn. Echo do chính ứng dụng gửi (cùng `app_id`) **không** tính là nhân viên trả lời.
- **Trả hội thoại lại cho AI:** nhân viên bấm "Trả lại AI" trên admin; hoặc tự động khi đã quá `N` giờ (cấu hình, ví dụ 24 giờ) kể từ tin cuối của nhân viên (`nhan_vien_tin_cuoi_luc`). Khi tự trả lại, tin tiếp theo của khách đi theo chế độ `DRAFT` và báo nhân viên phụ trách trước, không để AI chen vào giữa lúc nhân viên đang theo.
- Chế độ `DRAFT`: bản nháp hiện trong inbox admin; nhân viên gửi, sửa (lưu `kt_sua_ban_nhap`) hoặc bỏ.
- Chế độ mặc định của mỗi loại câu hỏi và các trường hợp một lượt `AUTO` chuyển sang `DRAFT`: mục 35.
- **Công tắc dừng toàn cục** (mục 33): khi bật, mọi lượt đi theo `DRAFT` bất kể cấu hình.
------------------------------------------------------------------------

# 20. Chọn model Claude

## 20.1 Một model cho hội thoại với khách

Toàn bộ hội thoại với khách dùng **một model**, chọn bằng bộ test (mục 35). Chạy bộ test với model mạnh nhất (Opus 5.5) trước để biết mức chất lượng tối đa, rồi thử Sonnet 5.5 và Haiku 5.5; chỉ hạ model khi model nhỏ hơn đạt ngang trên bộ test. Chi phí AI chỉ khoảng $8–260/tháng (mục 37), nhỏ so với giá trị một lead, nên khi chưa đủ số liệu thì ưu tiên chất lượng. Lý do dùng một model:

- Lượng tin thấp nên chênh lệch chi phí giữa các cách chia model nhỏ (mục 37).
- Định tuyến theo từng tin cần một bước phân loại. Phân loại nhầm thì câu hỏi rơi vào model yếu hơn; trả lời sai về giá hay bảo hành đắt hơn nhiều so với tiền token tiết kiệm được.
- Prompt cache tính riêng theo từng model. Chia tin cho nhiều model làm cache nguội thường xuyên hơn và tăng phí ghi cache cho khối kiến thức.
- Giọng văn nhất quán trong một hội thoại; chỉ phải chạy một bộ test.
- Cần giảm chi phí hay độ trễ thì hạ mức `effort` trước khi tính chuyện đổi model.

Khách VIP hoặc trường hợp phức tạp được chuyển cho người thật, không chuyển sang model lớn hơn.

**Thử nghiệm ngày 2026-10-24:** 4 lượt đầu trên Opus 5.5 (effort `medium`) mất 8, 112, 11 và 82 giây cho cùng cỡ đầu ra (800–1.200 token): tốc độ sinh của API lúc nhanh lúc chậm, quá lâu với chat bán hàng. Chuyển sang thử **Sonnet 5.5, effort `low`** (tài liệu Anthropic khuyên `low` cho chat), giá bằng một nửa. Theo dõi `luot_goi_ai.do_tre_ms` và chất lượng trả lời; quay lại Opus bằng `AI_MODEL` / `AI_EFFORT` nếu chất lượng giảm rõ. Trong lúc chờ Claude, "đang soạn tin" được bật lại mỗi 10 giây.

## 20.2 Model khác cho việc khác

| Việc | Model gợi ý | Lý do |
|---|---|---|
| Hội thoại với khách | Một model chọn bằng bộ test | Mục 20.1 |
| Trợ lý nội dung (mục 26) | Opus 5.5 | Ít lượt dùng, cần viết và lập chiến lược tốt |
| Việc chạy nền: tóm tắt hội thoại, gom nhóm lỗ hổng kiến thức, nháp metadata media | Haiku 5.5 qua Batch API (giảm 50%) | Không trực tiếp với khách, không cần nhanh |

Mỗi việc có prompt và cache riêng; không đổi model giữa chừng một hội thoại.

## 20.3 Khi nào mới định tuyến theo từng tin

Chỉ khi cả hai điều kiện cùng đúng:

1. Chi phí AI hằng tháng đáng kể so với các chi phí khác (ZNS, server, công người).
2. Số liệu trong `luot_goi_ai` và bộ test cho thấy một nhóm câu hỏi rõ ràng mà model rẻ hơn trả lời tốt ngang model đang dùng.

Khi đó mới định tuyến riêng nhóm câu hỏi ấy:

``` text
Haiku          → nhóm câu hỏi đơn giản đã được chứng minh bằng bộ test
Sonnet / Opus  → phần còn lại
```
------------------------------------------------------------------------

# 21. Energy Intelligence

Solar API hiện tại của doanh nghiệp:

``` text
Solar API
    ↓
Energy Service
    ↓
Analytics
    ↓
Customer Energy Profile
```

AI không nên tự tính các chỉ số kỹ thuật.

Energy Engine tính:

-   Production.
-   Expected production.
-   Performance.
-   Self-consumption.
-   Grid export.
-   Anomaly.
-   Peak usage.
-   Suggested usage window.

Claude chỉ chuyển kết quả thành ngôn ngữ dễ hiểu.


## 21.1 Nguồn dữ liệu

- Solar API hiện tại của doanh nghiệp.
- API cloud của nhà cung cấp thiết bị, gọi theo **mã thiết bị / mã trạm** của từng hệ: lấy sản lượng từ inverter và số liệu đồng hồ đo tiêu thụ (nếu hệ có lắp). Mỗi hãng một adapter cùng một interface nội bộ; catalog hiện có Huawei, Solis, INVT, Auxsol, Deye, Growatt, Luxpower.
- Dữ liệu bức xạ / thời tiết theo khu vực để tính sản lượng kỳ vọng.
- API key của từng hãng lưu trong secret manager.

## 21.2 Quy tắc

- Tỷ lệ tự dùng và điện phát lên lưới chỉ tính khi hệ có đồng hồ đo tiêu thụ; hệ không có thì báo cáo chỉ gồm sản lượng.
- Sản lượng thấp phải được so với thời tiết trước khi coi là bất thường, để tránh cảnh báo giả vào tuần mưa.
- Bất thường chuyển thành việc cho kỹ thuật viên trước, không gửi thẳng cảnh báo cho khách.
- Báo cáo gửi qua Zalo ZNS kèm link trang báo cáo web (mục 22, 54.4).

## 21.3 Dữ liệu dự kiến (chốt khi làm Phase 4)

Tên bảng theo quy ước ở mục 29, tiền tố `nl_` (năng lượng):

- `nl_he_thong` (EN: `solar_systems`): hệ của khách (một khách có thể có nhiều hệ), công suất, ngày lắp, hãng inverter, có đồng hồ đo tiêu thụ hay không.
- `nl_thiet_bi` (EN: `solar_devices`): mã thiết bị / mã trạm theo từng hãng, dùng để gọi API.
- `nl_so_lieu_ngay` (EN: `energy_daily`): số liệu theo ngày: sản lượng, tiêu thụ, phát lưới, sản lượng kỳ vọng, cờ bất thường.
------------------------------------------------------------------------

# 22. Báo cáo tuần/tháng

Ví dụ:

``` text
Sản lượng:
1.250 kWh

Tỷ lệ tự dùng:
82%

Điện dư:
225 kWh

Khuyến nghị:
Ưu tiên máy giặt, bình nóng lạnh,
EV charging trong khoảng 10h–14h.

Video:
"Cách tối ưu điện mặt trời cho gia đình"
```

Luồng:

``` text
Solar API / API hãng inverter (mục 21.1)
    ↓
Energy Analytics: Java tính chỉ số, phát hiện bất thường (mục 21)
    ↓
Customer Metrics
    ↓
Claude: diễn giải số liệu thành lời, khuyến nghị
        (kiến thức trong prompt có cache + danh mục media, mục 5.4, 7; văn phong báo cáo, mục 15)
    ↓
Personalized Report (trang báo cáo web)
    ↓
Zalo (ZNS + link trang báo cáo web, mục 54.4)
```

Mọi con số trong báo cáo lấy từ Energy Analytics; Claude không tự tính (mục 21).

------------------------------------------------------------------------

# 23. Facebook Lead Automation

``` text
Facebook
    ↓
Comment / Messenger
    ↓
AI Classification
    ↓
Warm Response
    ↓
Conversation
    ↓
Qualify Lead
    ↓
Ask Phone
    ↓
Customer gives phone
    ↓
CRM
    ↓
Your Sales API
    ↓
Sales notification
```

Sales API nên nhận:

``` text
customer_name
phone
facebook_psid
facebook_page_id
conversation_id
customer_need
ai_summary
product_interest
estimated_bill
recommended_solution
province
region
lead_level
lead_level_reason
research_level
budget
timeline
```

------------------------------------------------------------------------

# 24. Zalo Automation

Zalo OA là channel adapter riêng:

``` text
Zalo OA
   │
   ├── Incoming Message
   ├── Webhook
   ├── Reply
   ├── Broadcast
   ├── Template Message
   └── Content
```

Zalo OpenAPI/ZBS cần được kiểm tra theo quyền OA, loại tin và chính sách
hiện hành trước khi triển khai từng workflow.

Không dùng automation tài khoản Zalo cá nhân để thay thế OA. Zalo cá nhân chỉ
là tùy chọn rủi ro cao, tắt mặc định (mục 54.6). Đặc tả Zalo OA: mục 54.4.

------------------------------------------------------------------------

# 25. Content Intelligence

Đây là module giúp AI tự tạo nội dung.

``` text
Content Intelligence
        │
        ├── Knowledge (mục 5)
        ├── Media Library (mục 6)
        ├── Văn phong (core/voice.md)
        ├── Customer Segments
        └── Campaign Goals
                 ↓
          Trợ lý nội dung (mục 26)
                 ↓
          Bản nháp theo kênh
                 ↓
       Human Approval / Auto
                 ↓
          Facebook / Zalo / TikTok
```

------------------------------------------------------------------------

# 26. Content Planner và Trợ lý nội dung

## 26.1 Kế hoạch theo chiến dịch

Admin đặt mục tiêu (trên form hoặc qua Trợ lý nội dung, mục 26.2):

``` text
Tháng 10

Mục tiêu:
- Tăng lead
- Quảng bá battery
- Chăm sóc khách cũ
- Giới thiệu sản phẩm X
```

AI đề xuất lịch:

``` text
MON:
Battery

WED:
Case Study

FRI:
Solar Optimization
```

Mỗi bài (`nd_bai_dang`) có:

``` text
Post
 ├── Knowledge source
 ├── Media
 ├── Product
 ├── CTA
 ├── Target segment
 ├── Campaign
 └── Bản đăng theo từng kênh (nd_ban_dang_kenh: caption, hashtag, giờ đăng)
```

Workflow:

``` text
SUGGESTED / DRAFT   (AI hoặc người tạo)
 ↓
PENDING_APPROVAL    (kiểm tra quyền media, capability kênh, quy tắc lịch)
 ↓
SCHEDULED           (chỉ khi admin bấm duyệt)
 ↓
PUBLISHED / FAILED / MANUAL_REQUIRED
```

Sau khi hệ thống ổn định, các campaign đã được phê duyệt có thể chạy tự động theo policy.

## 26.2 Trợ lý nội dung (chat trong Vben Admin)

Trợ lý nội bộ để admin hỏi và giao việc bằng chat, ví dụ:

- "Hôm nay nên đăng video gì, lên kênh nào?"
- "Lập lịch tuần sau cho chiến dịch pin lưu trữ."
- "Viết caption TikTok cho M07, ngắn hơn bản Facebook."
- "Tuần trước bài nào hiệu quả nhất, vì sao?"
- "Còn video nào chưa đăng lên Page?"

Trợ lý **tách hẳn khỏi bot chăm sóc khách**: prompt riêng, bộ tool riêng, chỉ người dùng admin có quyền `CONTENT_ADMIN` truy cập, mọi phiên chat đều được lưu (`tl_phien_chat`, `tl_tin_nhan`).

``` text
Admin: "Hôm nay nên đăng video gì?"
        ↓
Content Assistant (Claude) ── đọc: knowledge/content/ + core/voice.md + rules/content-schedule.yaml
        │                    tool chỉ đọc: lịch đăng · chiến dịch · media · hiệu quả · câu hỏi khách · catalog
        ↓
2–3 phương án: media, kênh, giờ đăng, caption theo kênh, CTA, lý do
        ↓
Admin chọn / sửa trong chat ──► AI tạo bài NHÁP (nd_bai_dang + nd_ban_dang_kenh theo kênh)
        ↓
Thẻ bài trong chat: xem trước từng kênh, cảnh báo quy tắc lịch, kiểm tra capability
        ↓
Admin bấm "Duyệt & lên lịch" ──► SCHEDULED
        ↓
PublishScheduler đến giờ ──► adapter từng kênh ──► PUBLISHED / FAILED / MANUAL_REQUIRED ──► số liệu
```

## 26.3 Tool của Trợ lý nội dung

| Tool | Loại | Trả về |
|---|---|---|
| `xem_lich_dang(tu_ngay, den_ngay, kenh?)` | Đọc | Bài đã đăng hoặc đã lên lịch: chủ đề, loại nội dung, kênh, giờ |
| `xem_chien_dich(trang_thai?)` | Đọc | Chiến dịch đang chạy, mục tiêu, chủ đề ưu tiên, nhóm khách |
| `xem_media(chu_de?, loai_media?, chua_dang_len_kenh?)` | Đọc | Media `APPROVED`, `tom_tat`, `dung_khi`, quyền sử dụng, lần đăng gần nhất theo kênh |
| `hieu_qua_noi_dung(tu_ngay, den_ngay, kenh?)` | Đọc | Số liệu bài đăng trong phạm vi API cho phép, tin nhắn và lead phát sinh sau bài |
| `cau_hoi_khach_pho_bien(so_ngay)` | Đọc | Chủ đề khách hỏi nhiều, tổng hợp từ hội thoại và `kt_lo_hong`; **không** trả nội dung chat nguyên văn, tên hay số điện thoại |
| `xem_kha_nang_kenh(kenh)` | Đọc | Capability thực tế của tài khoản: đăng video, đăng ảnh, lên lịch, chỉ upload nháp, giới hạn file (mục 54.2) |
| `tra_cuu_goi(...)`, `tra_cuu_vat_tu(...)` | Đọc | Như tool catalog của bot (mục 4.2.6); mọi con số trong caption phải lấy từ đây |
| `tao_ban_nhap(media_id, kenh[], caption_theo_kenh, cta, gio_de_xuat, chien_dich?, ly_do)` | Ghi nháp | Bài `DRAFT` + một `nd_ban_dang_kenh` cho mỗi kênh, hiện thành thẻ trong chat |
| `sua_ban_nhap(bai_dang_id, ...)` | Ghi nháp | Cập nhật bài còn ở `DRAFT` |
| `tao_ke_hoach(chien_dich, tu_ngay, den_ngay)` | Ghi nháp | Nhiều bài `SUGGESTED` theo quy tắc lịch, để admin duyệt từng bài |

**Không có tool lên lịch, đăng hay xóa bài.** Các việc đó chỉ xảy ra khi admin bấm nút trên giao diện, đúng nguyên tắc ở mục 33.

## 26.4 Căn cứ chiến lược

Trợ lý chỉ nói được "chiến lược" khi có căn cứ:

- `knowledge/content/chien-luoc.md`: trụ cột nội dung, tỷ lệ bài giáo dục / case study / bán hàng, cách viết hook và CTA, những điều không được hứa.
- `knowledge/content/theo-kenh.md`: định dạng và văn phong bài theo từng kênh (Page, TikTok, Zalo OA).
- `knowledge/rules/content-schedule.yaml`: quy tắc lịch, code kiểm tra được. Ví dụ (doanh nghiệp tự chỉnh):

``` yaml
timezone: Asia/Ho_Chi_Minh
channels:
  FACEBOOK_PAGE: { max_posts_per_day: 2, windows: ["11:00-12:30", "19:30-21:30"] }
  TIKTOK:        { max_posts_per_day: 1, windows: ["18:00-22:00"] }
  ZALO_OA:       { max_posts_per_week: 2, windows: ["09:00-10:00"] }
min_gap_same_topic_days: 3        # không đăng cùng chủ đề trong 3 ngày trên cùng kênh
content_mix_per_week:             # tỷ lệ mục tiêu
  EDUCATION: 0.6
  CASE_STUDY: 0.2
  PROMOTION: 0.2
```

- Dữ liệu thật từ các tool đọc ở mục 26.3: đã đăng gì, bài nào hiệu quả, media nào chưa dùng, khách đang hỏi gì.

Khi lý do đề xuất dựa trên số liệu, trợ lý phải nêu số liệu đó (ví dụ "12 khách hỏi về pin lưu trữ trong 7 ngày") để admin kiểm tra được.

## 26.5 Kiểm tra trước khi lên lịch

Khi admin bấm "Duyệt & lên lịch", backend (`ScheduleRuleChecker`) kiểm tra:

| Kiểm tra | Nếu không đạt |
|---|---|
| Media `APPROVED`; nguồn dùng để đăng là `UPLOAD` có `quyen_su_dung` `OWNED` hoặc `LICENSED` (mục 55.2) | Chặn |
| Kênh và tài khoản có capability tương ứng (đăng video/ảnh, lên lịch) | Chặn; nếu kênh chỉ hỗ trợ đăng tay thì chuyển `MANUAL_REQUIRED` và cho tải file + caption |
| Caption không có con số giá/công suất ngoài kết quả tool catalog, không cam kết ROI/bảo hành ngoài Knowledge | Chặn |
| Giờ đăng nằm trong khung giờ của kênh | Cảnh báo |
| Vượt số bài mỗi ngày/tuần, trùng chủ đề trong khoảng giãn cách | Cảnh báo |
| Kênh là tùy chọn rủi ro cao (mục 54.6) | Chỉ hiện khi đã bật cờ; luôn cần duyệt từng bài |

Cảnh báo được lưu vào `nd_ban_dang_kenh.canh_bao_lich`; admin vẫn có thể lên lịch sau khi xác nhận.

## 26.6 Ví dụ

> **Admin:** Hôm nay nên đăng video gì?
>
> **Trợ lý:** **Gợi ý 1 (nên chọn):** M07 "Hybrid dùng điện buổi tối" lên Page lúc 20:00 và TikTok lúc 21:00.
> Lý do: mục tiêu tháng 10 là quảng bá pin lưu trữ; 7 ngày qua chưa có bài nào về pin; 12 khách hỏi về pin lưu trữ trong tuần; M07 chưa đăng lên Page.
> Caption Page: "Ban ngày đi làm, tối mới dùng điện nhiều? …" · Caption TikTok: "Tối mới bật điều hòa thì điện mặt trời có ích gì? …" · CTA: nhắn tin để được tính công suất.
>
> **Gợi ý 2:** ảnh case study gói 10 kWp 3 pha lên Page lúc 11:30 …
>
> **Admin:** Lấy gợi ý 1, caption Page ngắn lại.
>
> **Trợ lý:** *(gọi `tao_ban_nhap`)* Đã tạo bản nháp, xem thẻ bên dưới để duyệt và lên lịch.

## 26.7 Gợi ý buổi sáng

- `DailyContentBriefJob` chạy mỗi sáng (ví dụ 07:30), gọi Trợ lý với yêu cầu cố định "gợi ý nội dung hôm nay", tạo tối đa 3 bài `SUGGESTED` và gửi thông báo trên admin.
- Job này **không tự đăng**. Admin mở chat để chỉnh hoặc bỏ qua; bài `SUGGESTED` không được duyệt sẽ tự lưu trữ sau 1 ngày.

## 26.8 Prompt, model và lưu trữ hội thoại

- Prompt riêng của Trợ lý: `core/voice.md` + `knowledge/content/` + `rules/topics.yaml` + `rules/content-schedule.yaml`, đặt trong phần có cache; không dùng khối ② của bot chăm sóc khách (mục 5.4).
- Lượng dùng thấp (một vài người, vài chục lượt mỗi ngày) nên có thể dùng model mạnh hơn bot khách, ví dụ Claude Opus 5.5; chốt bằng bộ test như bot khách.
- `tl_tin_nhan` lưu **nguyên khối nội dung** API trả về (text, tool_use, tool_result) và chỉ nối thêm, không sửa lịch sử, để phát lại hội thoại đúng và giữ cache.
- Mỗi lượt gọi được ghi vào `luot_goi_ai` với `muc_dich = CONTENT_ASSISTANT` hoặc `DAILY_BRIEF`.

## 26.9 Module Java

``` text
services/content/
  ContentAssistantService.java      # vòng chat + tool use, stream về Vben
  ContentAssistantTools.java        # các tool ở mục 26.3
  ScheduleRuleChecker.java          # mục 26.5, đọc rules/content-schedule.yaml
  DailyContentBriefJob.java         # mục 26.7
  ContentMetricsSyncJob.java        # lấy số liệu bài đăng theo lịch, trong phạm vi API cho phép
  ContentPlannerService.java
  ContentApprovalService.java
  PublishScheduler.java
```

API cho Vben: `POST /admin/assistant/sessions`, `POST /admin/assistant/sessions/{id}/messages` (stream), `POST /admin/content/posts/{id}/approve-schedule`.

## 26.10 Lộ trình và điều kiện

- Làm sau khi Messenger MVP chạy ổn và Media Library đã có dữ liệu.
- Bước đầu: chỉ Facebook Page (đăng video/ảnh), các kênh khác hiển thị `MANUAL_REQUIRED`.
- Quyền cần xin: Facebook `pages_manage_posts`, `pages_read_engagement`; TikTok Content Posting API (sau audit); Zalo OA theo quyền được cấp (mục 54).
- Dữ liệu: mục 29 (Content, Assistant).

------------------------------------------------------------------------

# 27. Media Recommendation Engine

Cách AI chọn media: mục 7. Mỗi mục media cần có metadata:

``` text
media_id
loai_media          IMAGE | VIDEO
tom_tat             media nói gì
dung_khi            khi nào nên gửi
tranh_khi           khi nào không gửi
chu_de              theo knowledge/rules/topics.yaml
cac_giai_doan_khach
kenh_cho_phep
media_lien_ket      gói / vật tư / hãng / tài liệu kiến thức
trang_thai          chỉ dùng APPROVED
quyen_su_dung
trang_thai_truy_cap theo từng nguồn
```

AI chọn media dựa trên:

``` text
Relevance
+
Customer Stage
+
Intent
+
Channel
+
Campaign
+
Historical Performance (từ media_lan_gui, khi đủ dữ liệu)
```

Không chỉ dựa trên similarity.

------------------------------------------------------------------------

# 28. Feedback Loop

Sau khi AI gửi media:

``` text
AI chọn M07
        ↓
Khách nhận
        ↓
Khách phản hồi?
        ↓
Có lấy số điện thoại / tạo lead?
        ↓
Có chốt sale?
```

Không có bảng riêng cho việc này. Dữ liệu lấy từ các bảng đã có (mục 29):

| Câu hỏi | Nguồn |
|---|---|
| AI đã gửi media nào, vì sao | `media_lan_gui` (`chon_boi`, `ly_do`, `luot_goi_ai_id`) |
| Khách có phản hồi sau khi nhận không | `media_lan_gui.khach_tra_loi_luc` |
| Có tạo lead không | `media_lan_gui.tao_lead`, `khach_tiem_nang` cùng `hoi_thoai_id` |
| Có chốt sale không | Kết quả Sales API / CRM trả về theo lead |
| Khách có bấm xem không | Chỉ khi nền tảng cung cấp; Messenger không báo lượt bấm, nên không dựa vào chỉ số này |

Sau đó dùng dữ liệu này để đánh giá từng media khi đủ số lượng (mục 7.5).

Ví dụ:

``` text
M07

Số lần gửi: 342
Khách phản hồi: 120
Để lại số điện thoại / lead: 19
Chốt: 4
```

------------------------------------------------------------------------

# 29. Database chính

## Quy ước đặt tên

- Tên bảng tiếng Việt không dấu, dạng `snake_case`, giống quy ước của `slm_v2` (`vat_tu`, `tron_goi`).
- Tiền tố theo nhóm: `dm_` danh mục (catalog đồng bộ từ `slm_v2`), `kt_` kiến thức, `media_` thư viện media, `nd_` nội dung đăng bài, `tl_` trợ lý trong admin, `nl_` năng lượng (Phase 4, mục 21.3).
- Tên cột cũng tiếng Việt không dấu, theo quy ước của `slm_v2`: thời điểm kết thúc bằng `_luc` (`tao_luc`, `sua_luc`), `trang_thai`, `ten`, `ma`, `mo_ta`, `sdt`; khóa ngoại dạng `<bảng>_id` (`khach_hang_id`, `hoi_thoai_id`, `vat_tu_id`).
- Mỗi lệnh `CREATE TABLE` có một dòng `-- EN:` ghi tên bảng tiếng Anh; mỗi dòng cột có comment `[EN: ...]` ghi tên cột tiếng Anh, để code và các phiên AI khác tra cứu dễ.
- Giữ nguyên tiếng Anh: `id`, `email`, `model`, `cta`, `caption`, `hashtags`, `attachment_id`, `media_id`; giá trị enum (`APPROVED`, `DRAFT`...); key JSON trong kết quả của AI (mục 18); key YAML của các file `rules/`; payload gửi Sales API (mục 23, 47); tên trường của API nền tảng (`user_id` của Zalo, `campaign_id` của TikTok...).

| Tên bảng | Tên tiếng Anh (EN) | Nội dung |
|---|---|---|
| `khach_hang` | `customers` | Khách hàng, consent, opt-out |
| `dinh_danh_kenh` | `channel_identities` | Định danh khách trên từng kênh |
| `khach_hang_lich_su_gop` | `customer_merges` | Lịch sử gộp hai bản ghi khách trùng |
| `dm_thuong_hieu` | `catalog_brands` | Thương hiệu (đồng bộ) |
| `dm_co_so` | `catalog_branches` | Cơ sở giá: HN miền Bắc, HCM miền Nam |
| `dm_vat_tu` | `catalog_materials` | Vật tư (đồng bộ) |
| `dm_gia_vat_tu` | `catalog_material_prices` | Giá bán hiện hành của vật tư theo cơ sở |
| `dm_tron_goi` | `catalog_packages` | Trọn gói (đồng bộ) |
| `dm_vat_tu_tron_goi` | `catalog_package_items` | Thành phần của trọn gói |
| `dm_lich_su_dong_bo` | `catalog_sync_runs` | Lịch sử các lần đồng bộ catalog |
| `kt_tai_lieu` | `knowledge_docs` | Tài liệu kiến thức |
| `kt_phien_ban` | `knowledge_doc_versions` | Phiên bản tài liệu |
| `kt_phat_hanh` | `knowledge_releases` | Bản phát hành kiến thức |
| `kt_phat_hanh_chi_tiet` | `knowledge_release_items` | Bản phát hành gồm phiên bản nào |
| `kt_lo_hong` | `knowledge_gaps` | Lỗ hổng kiến thức |
| `kt_sua_ban_nhap` | `draft_edits` | Nhân viên sửa bản nháp của AI |
| `kt_ung_vien_vi_du` | `example_candidates` | Hàng chờ duyệt đoạn chat mẫu |
| `kt_de_xuat_sua` | `knowledge_change_proposals` | Đề xuất sửa kiến thức của AI hỗ trợ soạn |
| `luot_goi_ai` | `ai_runs` | Mỗi lượt gọi Claude |
| `media` | `media_items` | Mục media: nội dung, tom_tat, dung_khi |
| `media_nguon` | `media_sources` | Nguồn của mục media |
| `media_lien_ket` | `media_links` | Liên kết media với gói, vật tư, tài liệu |
| `media_tham_chieu_kenh` | `media_channel_refs` | attachment_id đã upload lên từng kênh |
| `media_lan_gui` | `media_deliveries` | Mỗi lần gửi media cho khách |
| `nd_chien_dich` | `content_campaigns` | Chiến dịch nội dung |
| `nd_bai_dang` | `content_posts` | Bài đăng (một ý tưởng, nhiều kênh) |
| `nd_ban_dang_kenh` | `content_publications` | Bản đăng của bài trên từng kênh |
| `nd_so_lieu_ban_dang` | `content_publication_metrics` | Số liệu bản đăng |
| `tl_phien_chat` | `assistant_sessions` | Phiên chat của trợ lý trong admin |
| `tl_tin_nhan` | `assistant_messages` | Tin nhắn trong phiên trợ lý |
| `su_kien_webhook` | `webhook_events` | Sự kiện webhook thô |
| `hoi_thoai` | `conversations` | Hội thoại với khách |
| `tin_nhan` | `messages` | Tin nhắn trong hội thoại |
| `ho_so_tu_van` | `consult_profiles` | Hồ sơ tư vấn, mức tiềm năng |
| `khach_tiem_nang` | `leads` | Lead gửi Sales API |
| `lich_follow_up` | `lead_follow_up_jobs` | Lịch follow-up khách tiềm năng |
| `lich_follow_up_su_kien` | `lead_follow_up_events` | Nhật ký lịch follow-up |

## Từ điển cột

Tên cột tiếng Việt ↔ tên tiếng Anh (cùng một tên tiếng Anh luôn dịch cùng một cách ở mọi bảng).

| Cột | EN | Cột | EN |
|---|---|---|---|
| `attachment_id` | `attachment_id` | `ma_tai_lieu_de_xuat` | `proposed_doc_key` |
| `bai_dang_id` | `post_id` | `ma_tham_chieu_sales_api` | `sales_api_ref` |
| `ban_chay` | `best_seller` | `mau_thuan` | `conflicts` |
| `ban_dang_id` | `publication_id` | `media_chon_id` | `selected_media_id` |
| `bao_cao_bo_test` | `eval_report` | `media_id` | `media_id` |
| `bao_cao_kiem_tra` | `validation_report` | `media_nguon_id` | `media_source_id` |
| `bao_cao_kiem_tra_nhanh` | `lint_report` | `mien` | `region` |
| `bao_hanh_thang` | `warranty_months` | `mo_ta` | `description` |
| `bat_dau_luc` | `started_at` | `mo_ta_bao_gia` | `quote_description` |
| `bat_dau_ngay` | `starts_on` | `model` | `model` |
| `bucket_luu_tru` | `storage_bucket` | `muc_dich` | `purpose` |
| `cac_giai_doan_khach` | `customer_stages` | `muc_tiem_nang` | `lead_level` |
| `cac_kenh` | `channels` | `muc_tieu` | `goal` |
| `cac_lan_goi_tool` | `tool_calls` | `muc_tieu` | `objective` |
| `canh_bao` | `warnings` | `ngu_canh` | `context` |
| `canh_bao_lich` | `rule_warnings` | `nguoi_dung_id` | `user_id` |
| `caption` | `caption` | `nguoi_duyet` | `approved_by` |
| `cau_hoi` | `question` | `nguoi_duyet` | `reviewed_by` |
| `che_do_tra_loi` | `reply_mode` | `nguoi_gui` | `sender_type` |
| `chi_so` | `metrics` | `nguoi_kich_hoat` | `activated_by` |
| `chien_dich_id` | `campaign_id` | `nguoi_phu_trach` | `ownership` |
| `chieu` | `direction` | `nguoi_quyet_dinh` | `decided_by` |
| `chieu_cao` | `height` | `nguoi_tao` | `created_by` |
| `chieu_rong` | `width` | `nguon` | `source` |
| `chon_boi` | `selected_by` | `nguon_tham_chieu` | `sources` |
| `chu_de` | `topics` | `nhan_luc` | `received_at` |
| `co_ai_ho_tro` | `ai_assisted` | `nhan_vien_id` | `staff_id` |
| `co_lo_hong_kt` | `knowledge_gap` | `nhan_vien_phu_trach_id` | `owner_staff_id` |
| `cong_suat_kwp` | `capacity_kwp` | `nhan_vien_sales_id` | `sales_id` |
| `cta` | `cta` | `nhom_khach_muc_tieu` | `target_segments` |
| `da_dua_vao_vi_du` | `promoted` | `noi_dung` | `content` |
| `da_luu_tru` | `archived` | `noi_dung_ai` | `ai_text` |
| `da_quan_tam` | `is_follower` | `noi_dung_de_xuat` | `proposed_content` |
| `dang_dung` | `active` | `noi_dung_nguoi_sua` | `human_text` |
| `dang_luc` | `published_at` | `phan_loai` | `qualification` |
| `den_han_luc` | `due_at` | `phat_hanh_id` | `release_id` |
| `dia_chi_van_phong` | `office_address` | `phien_ban_catalog` | `catalog_version` |
| `diem_chat_luong` | `quality_score` | `phien_ban_goc_id` | `base_version_id` |
| `dinh_dang` | `format` | `phien_ban_id` | `version_id` |
| `dinh_danh_kenh_id` | `channel_identity_id` | `phien_ban_kt` | `kb_version` |
| `dinh_kem` | `attachments` | `phien_ban_media` | `media_version` |
| `do_tin_cay` | `confidence` | `phien_ban_prompt` | `prompt_version` |
| `do_tre_ms` | `latency_ms` | `phien_ban_tao_ra_id` | `resulting_version_id` |
| `don_vi` | `unit` | `phien_chat_id` | `assistant_session_id` |
| `dong_bo_luc` | `synced_at` | `phien_chat_id` | `session_id` |
| `dong_y` | `consent` | `quoc_gia` | `country` |
| `du_lieu_su_kien` | `event_data` | `quyen_su_dung` | `rights_status` |
| `du_lieu_tho` | `payload` | `quyet_dinh_json` | `decision_json` |
| `dung_khi` | `use_when` | `quyet_dinh_luc` | `decided_at` |
| `dung_luong_byte` | `size_bytes` | `san_luong_thang_max` | `monthly_kwh_max` |
| `duoc_bao_hanh` | `has_warranty` | `san_luong_thang_min` | `monthly_kwh_min` |
| `duoc_goi_y` | `recommendable` | `sdt` | `phone` |
| `duong_dan_luu_tru` | `object_key` | `so_cap_nhat` | `updated_count` |
| `duyet_luc` | `approved_at` | `so_cau_da_tra_loi` | `answered_questions` |
| `email` | `email` | `so_lan_thu` | `attempt_count` |
| `ghi_chu` | `notes` | `so_lan_thu_lai` | `retry_count` |
| `ghi_chu_thay_doi` | `change_note` | `so_lead` | `leads` |
| `gia_ban` | `sale_price` | `so_luong` | `quantity` |
| `gia_khung_sat` | `steel_frame_price` | `so_ngung` | `deactivated_count` |
| `giai_doan` | `stage` | `so_pha` | `phase` |
| `giai_doan_khach` | `customer_stage` | `so_phien_ban` | `version_no` |
| `giai_quyet_luc` | `resolved_at` | `so_them_moi` | `inserted_count` |
| `gui_luc` | `sent_at` | `so_thich` | `preferences` |
| `han_tra_loi_luc` | `reply_due_at` | `so_tin_nhan_den` | `inbound_messages` |
| `hanh_dong` | `action` | `so_token` | `token_count` |
| `hashtags` | `hashtags` | `sua_ban_nhap_id` | `draft_edit_id` |
| `hen_dang_luc` | `scheduled_at` | `sua_luc` | `updated_at` |
| `hen_kich_hoat_luc` | `scheduled_activate_at` | `tac_gia` | `author` |
| `hien_thi_khach` | `customer_visible` | `tai_khoan_id` | `account_id` |
| `hieu_luc_tu` | `effective_from` | `tai_len_luc` | `uploaded_at` |
| `hoi_thoai_id` | `conversation_id` | `tai_lieu_chien_luoc_ids` | `strategy_document_ids` |
| `huy_luc` | `cancelled_at` | `tai_lieu_id` | `doc_id` |
| `id` | `id` | `tao_boi` | `created_by_type` |
| `id_bai_dang_ngoai` | `external_post_id` | `tao_lead` | `lead_created` |
| `id_ngoai` | `external_id` | `tao_luc` | `created_at` |
| `id_nguoi_dung_ngoai` | `external_user_id` | `ten` | `name` |
| `id_nguon` | `source_id` | `ten_hien_thi` | `display_name` |
| `id_tin_nhan_ngoai` | `external_message_id` | `ten_nhom_goi` | `family_name` |
| `kenh` | `channel` | `ten_quoc_te` | `name_intl` |
| `kenh_cho_phep` | `allowed_channels` | `ten_tac_gia` | `author_name` |
| `kenh_nguon` | `source_channel` | `thoi_luong_giay` | `duration_seconds` |
| `ket_qua` | `outcome` | `thong_bao_loi` | `error_message` |
| `ket_qua_policy` | `policy_result` | `thong_so` | `specs` |
| `ket_thuc_luc` | `finished_at` | `thong_tin_con_thieu` | `missing_slots` |
| `ket_thuc_ngay` | `ends_on` | `thong_tin_da_thu` | `slots` |
| `khach_hang_id` | `customer_id` | `thu_tu` | `sort_order` |
| `khach_tra_loi_luc` | `customer_replied_at` | `thuong_hieu_id` | `brand_id` |
| `khoa_chong_trung` | `idempotency_key` | `thuong_hieu_inverter_id` | `inverter_brand_id` |
| `kich_hoat_luc` | `activated_at` | `thuong_hieu_pin_luu_tru_id` | `battery_brand_id` |
| `kiem_tra_cuoi_luc` | `last_checked_at` | `thuong_hieu_tam_pin_id` | `panel_brand_id` |
| `kieu_kich_hoat` | `trigger_type` | `tieu_de` | `title` |
| `lay_luc` | `fetched_at` | `tin_cuoi_luc` | `last_message_at` |
| `lich_follow_up_id` | `follow_up_job_id` | `tin_den_cuoi_luc` | `last_inbound_at` |
| `lien_ket_boi` | `linked_by` | `tin_khach` | `customer_message` |
| `loai_doi_tuong` | `target_type` | `tin_khach_cuoi_luc` | `last_customer_message_at` |
| `loai_he_thong` | `system_type` | `tin_nhan_id` | `message_id` |
| `loai_media` | `media_type` | `tinh` | `province` |
| `loai_mime` | `mime_type` | `token_dau_ra` | `output_tokens` |
| `loai_nguon` | `source_type` | `token_dau_vao` | `input_tokens` |
| `loai_noi_dung` | `content_type` | `token_doc_cache` | `cache_read_tokens` |
| `loai_su_kien` | `event_type` | `token_ghi_cache` | `cache_write_tokens` |
| `loai_tai_lieu` | `doc_type` | `tom_tat` | `summary` |
| `loai_tro_ly` | `assistant_type` | `tom_tat_nhu_cau` | `need_summary` |
| `loi_gan_nhat` | `last_error` | `tom_tat_thay_doi` | `change_summary` |
| `loi_thoai` | `transcript` | `tom_tat_tin_nhan` | `message_brief` |
| `luot_goi_ai_id` | `ai_run_id` | `tong_gia` | `total_price` |
| `ly_do` | `reason` | `tong_token` | `token_total` |
| `ly_do_de_xuat` | `rationale` | `tra_loi_cua_nguoi` | `human_response` |
| `ly_do_huy` | `cancel_reason` | `trang_thai` | `status` |
| `ly_do_muc_tiem_nang` | `lead_level_reason` | `trang_thai_truy_cap` | `availability_status` |
| `ly_do_quyet_dinh` | `decision_reason` | `trang_thai_xu_ly` | `processing_status` |
| `ma` | `code` | `tranh_khi` | `avoid_when` |
| `ma_bam` | `content_hash` | `tron_goi_de_xuat_ids` | `recommended_package_ids` |
| `ma_bam_sha256` | `checksum_sha256` | `tron_goi_id` | `package_id` |
| `ma_co_so` | `branch_code` | `tu_choi_lien_he_luc` | `opt_out_at` |
| `ma_danh_muc` | `index_code` | `url_anh_thu_nho` | `thumbnail_url` |
| `ma_doi_tuong` | `target_key` | `url_bai_dang_ngoai` | `external_post_url` |
| `ma_loi` | `error_code` | `url_nguon` | `source_url` |
| `ma_nhom` | `group_code` | `vai_tro` | `role` |
| `ma_su_kien` | `event_key` | `vat_tu_chinh` | `is_main` |
| `ma_tai_lieu` | `doc_key` | `vat_tu_id` | `material_id` |
| `ma_tai_lieu_bo_sung` | `resolved_doc_key` | `xu_ly_luc` | `processed_at` |
| `ma_tai_lieu_da_dua_vao` | `promoted_doc_key` | `y_dinh` | `intent` |
| `gop_vao_id` | `merged_into_id` | `khach_hang_bi_gop_id` | `merged_customer_id` |
| `tiep_quan_luc` | `taken_over_at` | `nhan_vien_tin_cuoi_luc` | `last_staff_message_at` |

## Customers

``` sql
-- EN: customers — customer master record (Customer 360)
CREATE TABLE khach_hang (
    id                  UUID PRIMARY KEY,
    ten                 VARCHAR(200),                         -- [EN: name]
    sdt                 VARCHAR(20) UNIQUE,                   -- [EN: phone] chuẩn hóa dạng 0xxxxxxxxx; NULL khi chưa có. Một số = một khách (số dùng chung trong gia đình coi là một hộ)
    email               VARCHAR(200),
    giai_doan           VARCHAR(20) NOT NULL DEFAULT 'LEAD',  -- [EN: stage] mục 4.1
    nhan_vien_sales_id  VARCHAR(100),                         -- [EN: sales_id]
    so_thich            JSONB NOT NULL DEFAULT '{}',          -- [EN: preferences] ví dụ: thích trả lời ngắn (Customer Memory, mục 17)
    dong_y              JSONB NOT NULL DEFAULT '{}',          -- [EN: consent] theo kênh / mục đích: {"MESSENGER": {"marketing": false}}
    tu_choi_lien_he_luc TIMESTAMPTZ,                          -- [EN: opt_out_at] khách yêu cầu ngừng liên hệ
    gop_vao_id          UUID REFERENCES khach_hang(id),       -- [EN: merged_into_id] khác NULL: bản ghi đã bị gộp vào khách khác, không dùng nữa
    tao_luc             TIMESTAMPTZ NOT NULL DEFAULT now(),   -- [EN: created_at]
    sua_luc             TIMESTAMPTZ NOT NULL DEFAULT now()    -- [EN: updated_at]
);
```

## Channel Identities

Định danh của khách trên từng kênh, thay cho các cột `zalo_uid`, `facebook_user_id`, `facebook_url` trong `khach_hang`. Một khách có thể có nhiều định danh (nhắn nhiều Page/OA, nhiều kênh).

``` sql
-- EN: channel_identities — customer identity per channel (PSID, Zalo user_id, TikTok id)
CREATE TABLE dinh_danh_kenh (
    id                  UUID PRIMARY KEY,
    khach_hang_id       UUID NOT NULL REFERENCES khach_hang(id),  -- [EN: customer_id]
    kenh                VARCHAR(20) NOT NULL                      -- [EN: channel]
                        CHECK (kenh IN ('MESSENGER','FACEBOOK_COMMENT','ZALO_OA','TIKTOK_DM','TIKTOK_COMMENT',
                                        'FACEBOOK_GROUP','ZALO_PERSONAL')),  -- mã kênh hội thoại, mục 54.1
    tai_khoan_id        VARCHAR(100) NOT NULL,                    -- [EN: account_id] page_id / oa_id / tài khoản TikTok của công ty
    id_nguoi_dung_ngoai VARCHAR(200) NOT NULL,                    -- [EN: external_user_id] PSID / Zalo user_id theo OA / ID người dùng TikTok
    ten_hien_thi        VARCHAR(200),                             -- [EN: display_name]
    da_quan_tam         BOOLEAN,                                  -- [EN: is_follower] Zalo: đã quan tâm OA chưa
    tin_den_cuoi_luc    TIMESTAMPTZ,                              -- [EN: last_inbound_at] tin cuối của khách, để tính cửa sổ nhắn tin
    lien_ket_boi        VARCHAR(16) NOT NULL DEFAULT 'AUTO'       -- [EN: linked_by]
                        CHECK (lien_ket_boi IN ('AUTO','PHONE_MATCH','HUMAN')),
    tao_luc             TIMESTAMPTZ NOT NULL DEFAULT now(),       -- [EN: created_at]
    UNIQUE (kenh, tai_khoan_id, id_nguoi_dung_ngoai)
);
CREATE INDEX idx_dinh_danh_kenh_khach_hang_id ON dinh_danh_kenh (khach_hang_id);
```

### Gộp khách trùng

Mỗi định danh kênh mới tạo một `khach_hang` tạm (chưa có số điện thoại). Khi khách cho số điện thoại mà số đó đã thuộc khách khác:

1. Giữ bản ghi đã có số (hoặc bản ghi cũ hơn); bản ghi còn lại là bản bị gộp.
2. Trong một transaction: chuyển `dinh_danh_kenh`, `hoi_thoai`, `ho_so_tu_van`, `khach_tiem_nang`, `lich_follow_up` sang bản giữ lại; gộp `so_thich`, `dong_y` (lấy giá trị chặt hơn: đã từ chối thì giữ từ chối); đặt `gop_vao_id` cho bản bị gộp; ghi `khach_hang_lich_su_gop`.
3. Gộp tự động chỉ khi số trùng khớp sau chuẩn hóa (`PHONE_MATCH`). Mọi trường hợp khác (cùng tên, nghi trùng) chỉ gợi ý trên admin, người duyệt mới gộp (`HUMAN`).
4. Không tách lại tự động; nếu gộp nhầm, nhân viên tách bằng tay dựa trên `khach_hang_lich_su_gop`.

``` sql
-- EN: customer_merges — log of merged duplicate customers
CREATE TABLE khach_hang_lich_su_gop (
    id                   BIGSERIAL PRIMARY KEY,
    khach_hang_id        UUID NOT NULL REFERENCES khach_hang(id),         -- [EN: customer_id] bản ghi giữ lại
    khach_hang_bi_gop_id UUID NOT NULL REFERENCES khach_hang(id),         -- [EN: merged_customer_id]
    ly_do                VARCHAR(16) NOT NULL CHECK (ly_do IN ('PHONE_MATCH','HUMAN')),  -- [EN: reason]
    du_lieu_su_kien      JSONB,                                           -- [EN: event_data] id các bản ghi đã chuyển
    nguoi_tao            VARCHAR(100),                                    -- [EN: created_by] NULL khi tự động
    tao_luc              TIMESTAMPTZ NOT NULL DEFAULT now()               -- [EN: created_at]
);
```

## Catalog (đồng bộ từ slm_v2)

Bản chỉ đọc, do `CatalogSyncJob` ghi (mục 4.2). `id_nguon` là id bên `slm_v2`.

Migration V7 (2026-10-28) tạo các bảng dưới đây, khác bản thiết kế ở: `dm_tron_goi` thêm `nhom_tron_goi_id`, `so_tam_pin`, `cong_suat_tam_wp`, `cong_suat_inverter_kw`, `luu_tru_kwh`, `tep_tin_id`; `dm_vat_tu_tron_goi` thêm `ma_bam`, `dong_bo_luc`; `dm_vat_tu.ma` không UNIQUE; thêm `dm_ten_mien` (tên miền website theo cơ sở), `nl_bang_pvout`, `nl_pvout_tinh` (PV out EVN theo năm), `ho_so_tu_van.gia_thap_nhat_da_bao`; chưa tạo `dm_gia_vat_tu` (mục 60.4).

``` sql
-- EN: catalog_brands — brands synced from slm_v2
CREATE TABLE dm_thuong_hieu (
    id_nguon    INT PRIMARY KEY,        -- [EN: source_id]
    ten         VARCHAR(200) NOT NULL,  -- [EN: name]
    ten_quoc_te VARCHAR(200),           -- [EN: name_intl]
    quoc_gia    VARCHAR(200),           -- [EN: country]
    mo_ta       TEXT,                   -- [EN: description]
    dang_dung   BOOLEAN NOT NULL,       -- [EN: active]
    ma_bam      CHAR(64) NOT NULL,      -- [EN: content_hash]
    dong_bo_luc TIMESTAMPTZ NOT NULL    -- [EN: synced_at]
);

-- EN: catalog_branches — price regions synced from slm_v2 (HN = North, HCM = South)
CREATE TABLE dm_co_so (                        -- co_so
    id_nguon      INT PRIMARY KEY,  -- [EN: source_id]
    ma           VARCHAR(20) NOT NULL UNIQUE,        -- [EN: code] HN = giá miền Bắc, HCM = giá miền Nam (mục 4.2.9)
    ten           VARCHAR(200) NOT NULL,  -- [EN: name]
    dia_chi_van_phong TEXT,  -- [EN: office_address]
    sdt          VARCHAR(20),  -- [EN: phone]
    dang_dung         BOOLEAN NOT NULL,  -- [EN: active]
    ma_bam   CHAR(64) NOT NULL,  -- [EN: content_hash]
    dong_bo_luc      TIMESTAMPTZ NOT NULL  -- [EN: synced_at]
);

-- EN: catalog_materials — materials synced from slm_v2
CREATE TABLE dm_vat_tu (
    id_nguon       INT PRIMARY KEY,                          -- [EN: source_id]
    ma             VARCHAR(200) NOT NULL UNIQUE,             -- [EN: code] vat_tu.ma
    ten            VARCHAR(400) NOT NULL,                    -- [EN: name]
    ma_nhom        VARCHAR(200),                             -- [EN: group_code] nhom_vat_tu.ma: TAM_PIN, BIEN_TAN...
    thuong_hieu_id INT REFERENCES dm_thuong_hieu(id_nguon),  -- [EN: brand_id]
    don_vi         VARCHAR(20),                              -- [EN: unit]
    bao_hanh_thang INT,                                      -- [EN: warranty_months]
    vat_tu_chinh   BOOLEAN,                                  -- [EN: is_main]
    thong_so       JSONB NOT NULL DEFAULT '{}',              -- [EN: specs] đã chuẩn hóa: {"cong_suat": {"value": 600, "unit": "Wp"}}
    mo_ta_bao_gia  TEXT,                                     -- [EN: quote_description]
    dang_dung      BOOLEAN NOT NULL,                         -- [EN: active]
    canh_bao       TEXT[] NOT NULL DEFAULT '{}',             -- [EN: warnings]
    ma_bam         CHAR(64) NOT NULL,                        -- [EN: content_hash]
    dong_bo_luc    TIMESTAMPTZ NOT NULL                      -- [EN: synced_at]
);

-- EN: catalog_material_prices — current sale price per material per branch
CREATE TABLE dm_gia_vat_tu (                 -- chỉ giá bán hiện hành
    vat_tu_id    INT NOT NULL REFERENCES dm_vat_tu(id_nguon) ON DELETE CASCADE,  -- [EN: material_id]
    ma_co_so    VARCHAR(20) NOT NULL,  -- [EN: branch_code]
    gia_ban     NUMERIC(14,0) NOT NULL,  -- [EN: sale_price]
    hieu_luc_tu TIMESTAMPTZ NOT NULL,               -- [EN: effective_from] tao_luc của dòng giá mới nhất
    PRIMARY KEY (vat_tu_id, ma_co_so)
);

-- EN: catalog_packages — solar packages synced from slm_v2
CREATE TABLE dm_tron_goi (
    id_nguon                   INT PRIMARY KEY,                             -- [EN: source_id]
    ma_co_so                   VARCHAR(20) NOT NULL,                        -- [EN: branch_code] HN / HCM
    ten_nhom_goi               VARCHAR(400),                                -- [EN: family_name] nhom_tron_goi.ten
    thuong_hieu_tam_pin_id     INT REFERENCES dm_thuong_hieu(id_nguon),     -- [EN: panel_brand_id]
    thuong_hieu_inverter_id    INT REFERENCES dm_thuong_hieu(id_nguon),     -- [EN: inverter_brand_id]
    thuong_hieu_pin_luu_tru_id INT REFERENCES dm_thuong_hieu(id_nguon),     -- [EN: battery_brand_id]
    ten                        VARCHAR(400) NOT NULL,                       -- [EN: name]
    loai_he_thong              VARCHAR(16) CHECK (loai_he_thong IN ('ON_GRID','HYBRID')),  -- [EN: system_type] NULL nếu không nhận diện được
    so_pha                     SMALLINT    CHECK (so_pha IN (1,3)),         -- [EN: phase]
    cong_suat_kwp              NUMERIC(8,3),                                -- [EN: capacity_kwp]
    san_luong_thang_min        NUMERIC(10,1),                               -- [EN: monthly_kwh_min]
    san_luong_thang_max        NUMERIC(10,1),                               -- [EN: monthly_kwh_max]
    tong_gia                   NUMERIC(14,0),                               -- [EN: total_price] tong_gia: giá chính thức của gói
    gia_khung_sat              NUMERIC(14,0) NOT NULL DEFAULT 0,            -- [EN: steel_frame_price]
    ban_chay                   BOOLEAN,                                     -- [EN: best_seller]
    mo_ta                      TEXT,                                        -- [EN: description]
    dang_dung                  BOOLEAN NOT NULL,                            -- [EN: active]
    duoc_goi_y                 BOOLEAN NOT NULL,                            -- [EN: recommendable] false nếu có cảnh báo loại chặn
    canh_bao                   TEXT[] NOT NULL DEFAULT '{}',                -- [EN: warnings]
    ma_bam                     CHAR(64) NOT NULL,                           -- [EN: content_hash]
    dong_bo_luc                TIMESTAMPTZ NOT NULL                         -- [EN: synced_at]
);
CREATE INDEX idx_dm_tron_goi_ma_co_so_loai_he_thong_so_pha
    ON dm_tron_goi (ma_co_so, loai_he_thong, so_pha) WHERE dang_dung AND duoc_goi_y;

-- EN: catalog_package_items — package line items
CREATE TABLE dm_vat_tu_tron_goi (
    id_nguon       INT PRIMARY KEY,                                         -- [EN: source_id] vat_tu_tron_goi.id
    tron_goi_id    INT NOT NULL REFERENCES dm_tron_goi(id_nguon) ON DELETE CASCADE,  -- [EN: package_id]
    vat_tu_id      INT NOT NULL REFERENCES dm_vat_tu(id_nguon),             -- [EN: material_id]
    so_luong       INT,                                                     -- [EN: quantity]
    bao_hanh_thang INT,                                                     -- [EN: warranty_months]
    duoc_bao_hanh  BOOLEAN,                                                 -- [EN: has_warranty]
    hien_thi_khach BOOLEAN NOT NULL,                                        -- [EN: customer_visible] duoc_xem
    dang_dung      BOOLEAN NOT NULL                                         -- [EN: active]
);
CREATE INDEX idx_dm_vat_tu_tron_goi_tron_goi_id ON dm_vat_tu_tron_goi (tron_goi_id);

-- EN: catalog_sync_runs — catalog sync run log
CREATE TABLE dm_lich_su_dong_bo (
    id                BIGSERIAL PRIMARY KEY,
    kieu_kich_hoat    VARCHAR(16) NOT NULL CHECK (kieu_kich_hoat IN ('SCHEDULED','MANUAL')),  -- [EN: trigger_type]
    trang_thai        VARCHAR(16) NOT NULL CHECK (trang_thai IN ('RUNNING','SUCCESS','FAILED')),  -- [EN: status]
    bat_dau_luc       TIMESTAMPTZ NOT NULL DEFAULT now(),                   -- [EN: started_at]
    ket_thuc_luc      TIMESTAMPTZ,                                          -- [EN: finished_at]
    so_them_moi       INT,                                                  -- [EN: inserted_count]
    so_cap_nhat       INT,                                                  -- [EN: updated_count]
    so_ngung          INT,                                                  -- [EN: deactivated_count]
    canh_bao          JSONB,                                                -- [EN: warnings] {mã cảnh báo: [source_id...]}
    thong_bao_loi     TEXT,                                                 -- [EN: error_message]
    phien_ban_catalog CHAR(64)                                              -- [EN: catalog_version]
);
```

## Knowledge

Kiến thức lưu trong DB, có phiên bản và bản phát hành (mục 5.2, 5.3).

``` sql
-- EN: knowledge_docs — knowledge document identity
CREATE TABLE kt_tai_lieu (                          -- một tài liệu; nội dung nằm ở các phiên bản
    id         BIGSERIAL PRIMARY KEY,
    ma_tai_lieu    VARCHAR(200) NOT NULL UNIQUE,           -- [EN: doc_key] policy.bao-hanh
    loai_tai_lieu   VARCHAR(16)  NOT NULL  -- [EN: doc_type]
               CHECK (loai_tai_lieu IN ('CORE','PLAYBOOK','POLICY','FAQ','EXAMPLE','CONTENT','RULE','GUIDE','TEMPLATE')),
    dinh_dang     VARCHAR(8)   NOT NULL CHECK (dinh_dang IN ('MARKDOWN','YAML')),  -- [EN: format]
    da_luu_tru   BOOLEAN NOT NULL DEFAULT false,  -- [EN: archived]
    nguoi_tao VARCHAR(100),  -- [EN: created_by]
    tao_luc TIMESTAMPTZ NOT NULL DEFAULT now()  -- [EN: created_at]
);

-- EN: knowledge_doc_versions — knowledge document versions
CREATE TABLE kt_phien_ban (
    id               BIGSERIAL PRIMARY KEY,
    tai_lieu_id      BIGINT NOT NULL REFERENCES kt_tai_lieu(id),            -- [EN: doc_id]
    so_phien_ban     INT NOT NULL,                                          -- [EN: version_no]
    trang_thai       VARCHAR(16) NOT NULL CHECK (trang_thai IN ('DRAFT','FROZEN','DISCARDED')),  -- [EN: status]
    tieu_de          VARCHAR(300) NOT NULL,                                 -- [EN: title]
    chu_de           TEXT[] NOT NULL DEFAULT '{}',                          -- [EN: topics]
    cac_kenh         TEXT[] NOT NULL DEFAULT '{}',                          -- [EN: channels]
    noi_dung         TEXT NOT NULL,                                         -- [EN: content] Markdown hoặc YAML
    ma_bam           CHAR(64) NOT NULL,                                     -- [EN: content_hash]
    so_token         INT,                                                   -- [EN: token_count]
    phien_ban_goc_id BIGINT REFERENCES kt_phien_ban(id),                    -- [EN: base_version_id] sửa từ bản nào: diff, phát hiện xung đột
    ghi_chu_thay_doi TEXT,                                                  -- [EN: change_note]
    tac_gia          VARCHAR(100) NOT NULL,                                 -- [EN: author]
    co_ai_ho_tro     BOOLEAN NOT NULL DEFAULT false,                        -- [EN: ai_assisted] tạo từ đề xuất của AI hỗ trợ soạn (mục 5.10)
    tao_luc          TIMESTAMPTZ NOT NULL DEFAULT now(),                    -- [EN: created_at]
    sua_luc          TIMESTAMPTZ NOT NULL DEFAULT now(),                    -- [EN: updated_at]
    UNIQUE (tai_lieu_id, so_phien_ban)
);
CREATE UNIQUE INDEX uq_kt_phien_ban_mot_nhap ON kt_phien_ban (tai_lieu_id) WHERE trang_thai = 'DRAFT';

-- EN: knowledge_releases — immutable knowledge release snapshot
CREATE TABLE kt_phat_hanh (                      -- bản phát hành: ảnh chụp bất biến
    id                    BIGSERIAL PRIMARY KEY,
    phien_ban_kt            CHAR(64) NOT NULL UNIQUE,    -- [EN: kb_version] hash các phiên bản trong bản phát hành
    trang_thai                VARCHAR(16) NOT NULL  -- [EN: status]
                          CHECK (trang_thai IN ('VALIDATING','READY','ACTIVE','FAILED','SUPERSEDED')),
    ghi_chu                 TEXT,  -- [EN: notes]
    tong_token           INT,  -- [EN: token_total]
    bao_cao_kiem_tra     JSONB,                       -- [EN: validation_report] kết quả bước 1–4 (mục 5.3)
    bao_cao_bo_test           JSONB,                       -- [EN: eval_report] kết quả bộ test (bước 5)
    nguoi_tao            VARCHAR(100) NOT NULL,  -- [EN: created_by]
    nguoi_kich_hoat          VARCHAR(100),  -- [EN: activated_by]
    hen_kich_hoat_luc TIMESTAMPTZ,  -- [EN: scheduled_activate_at]
    tao_luc            TIMESTAMPTZ NOT NULL DEFAULT now(),  -- [EN: created_at]
    kich_hoat_luc          TIMESTAMPTZ  -- [EN: activated_at]
);
CREATE UNIQUE INDEX uq_kt_phat_hanh_mot_dang_chay ON kt_phat_hanh (trang_thai) WHERE trang_thai = 'ACTIVE';

-- EN: knowledge_release_items — doc version included in a release
CREATE TABLE kt_phat_hanh_chi_tiet (                 -- bản phát hành gồm phiên bản nào của tài liệu nào
    phat_hanh_id BIGINT NOT NULL REFERENCES kt_phat_hanh(id) ON DELETE CASCADE,  -- [EN: release_id]
    tai_lieu_id     BIGINT NOT NULL REFERENCES kt_tai_lieu(id),  -- [EN: doc_id]
    phien_ban_id BIGINT NOT NULL REFERENCES kt_phien_ban(id),  -- [EN: version_id]
    PRIMARY KEY (phat_hanh_id, tai_lieu_id)
);

-- EN: knowledge_gaps — questions the AI could not answer from knowledge
CREATE TABLE kt_lo_hong (
    id                  BIGSERIAL PRIMARY KEY,
    hoi_thoai_id        UUID NOT NULL,                                      -- [EN: conversation_id]
    tin_nhan_id         UUID,                                               -- [EN: message_id]
    cau_hoi             TEXT NOT NULL,                                      -- [EN: question]
    ly_do               TEXT,                                               -- [EN: reason]
    trang_thai          VARCHAR(16) NOT NULL DEFAULT 'NEW' CHECK (trang_thai IN ('NEW','DONE','IGNORED')),  -- [EN: status]
    ma_tai_lieu_bo_sung VARCHAR(200),                                       -- [EN: resolved_doc_key]
    tao_luc             TIMESTAMPTZ NOT NULL DEFAULT now(),                 -- [EN: created_at]
    giai_quyet_luc      TIMESTAMPTZ                                         -- [EN: resolved_at]
);

-- EN: draft_edits — staff edits of AI reply drafts
CREATE TABLE kt_sua_ban_nhap (
    id                 BIGSERIAL PRIMARY KEY,
    tin_nhan_id        UUID NOT NULL,                      -- [EN: message_id]
    luot_goi_ai_id     UUID,                               -- [EN: ai_run_id]
    noi_dung_ai        TEXT NOT NULL,                      -- [EN: ai_text]
    noi_dung_nguoi_sua TEXT NOT NULL,                      -- [EN: human_text]
    da_dua_vao_vi_du   BOOLEAN NOT NULL DEFAULT false,     -- [EN: promoted] đã đưa vào kt_ung_vien_vi_du
    tao_luc            TIMESTAMPTZ NOT NULL DEFAULT now()  -- [EN: created_at]
);
```

Phiên bản "đang chạy" của một tài liệu là phiên bản có trong bản phát hành `ACTIVE`; vì vậy rollback chỉ cần đổi bản phát hành, không phải sửa trạng thái từng phiên bản.

## AI Runs

Mỗi lần gọi Claude là một dòng, dùng cho audit, bộ test và theo dõi chi phí.

``` sql
-- EN: ai_runs — one row per Claude API call (audit, cost)
CREATE TABLE luot_goi_ai (
    id                UUID PRIMARY KEY,
    muc_dich          VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER_CHAT'  -- [EN: purpose]
                      CHECK (muc_dich IN ('CUSTOMER_CHAT','CONTENT_ASSISTANT','DAILY_BRIEF','KNOWLEDGE_ASSISTANT','RELEASE_EVAL')),
    hoi_thoai_id      UUID,                                         -- [EN: conversation_id] hội thoại khách hoặc phiên trợ lý; NULL với DAILY_BRIEF, RELEASE_EVAL
    model             VARCHAR(64) NOT NULL,
    phien_ban_kt      CHAR(64) NOT NULL,                            -- [EN: kb_version]
    phien_ban_catalog CHAR(64) NOT NULL,                            -- [EN: catalog_version]
    phien_ban_media   CHAR(64) NOT NULL,                            -- [EN: media_version]
    phien_ban_prompt  VARCHAR(64),                                  -- [EN: prompt_version]
    token_dau_vao     INT,                                          -- [EN: input_tokens]
    token_dau_ra      INT,                                          -- [EN: output_tokens]
    token_doc_cache   INT,                                          -- [EN: cache_read_tokens]
    token_ghi_cache   INT,                                          -- [EN: cache_write_tokens]
    cac_lan_goi_tool  JSONB,                                        -- [EN: tool_calls] tên tool, input, tóm tắt output
    nguon_tham_chieu  TEXT[],                                       -- [EN: sources] doc_key AI dẫn nguồn
    co_lo_hong_kt     BOOLEAN NOT NULL DEFAULT false,               -- [EN: knowledge_gap]
    ket_qua_policy    JSONB,                                        -- [EN: policy_result]
    do_tre_ms         INT,                                          -- [EN: latency_ms]
    ket_qua_ai        JSONB,                                        -- [EN: ai_output] kết quả có cấu trúc (mục 18), để kiểm tra lại và làm bộ test
    ly_do_dung        VARCHAR(40),                                  -- [EN: stop_reason] end_turn, refusal, max_tokens...
    loi               TEXT,                                         -- [EN: error] lỗi gọi Claude hoặc lỗi đọc kết quả
    tao_luc           TIMESTAMPTZ NOT NULL DEFAULT now()            -- [EN: created_at]
);
CREATE INDEX idx_luot_goi_ai_hoi_thoai_id_tao_luc ON luot_goi_ai (hoi_thoai_id, tao_luc);
```

`hoi_thoai_id` trỏ tới `hoi_thoai` (với `CUSTOMER_CHAT`) hoặc `tl_phien_chat` (với `CONTENT_ASSISTANT`, `KNOWLEDGE_ASSISTANT`), nên không đặt khóa ngoại; ứng dụng kiểm tra khi ghi.

## Media

Thay cho bảng `videos` / `video_assets` trước đây. Thiết kế xem mục 6.

``` sql
-- EN: media_items — media library item (content metadata)
CREATE TABLE media (
    id                  UUID PRIMARY KEY,
    loai_media          VARCHAR(16)  NOT NULL CHECK (loai_media IN ('IMAGE','VIDEO')),  -- [EN: media_type]
    tieu_de             VARCHAR(300) NOT NULL,                              -- [EN: title]
    ma_danh_muc         VARCHAR(10) UNIQUE,                                 -- [EN: index_code] mã ngắn trong danh mục media: M07
    tom_tat             TEXT,                                               -- [EN: summary] media nói gì (AI đọc trong danh mục)
    dung_khi            TEXT,                                               -- [EN: use_when] khi nào nên gửi
    tranh_khi           TEXT,                                               -- [EN: avoid_when] khi nào không gửi
    mo_ta               TEXT,                                               -- [EN: description] mô tả chi tiết; với ảnh = mô tả ảnh
    loi_thoai           TEXT,                                               -- [EN: transcript] video, nếu có
    chu_de              TEXT[] NOT NULL DEFAULT '{}',                       -- [EN: topics] theo knowledge/rules/topics.yaml
    cac_giai_doan_khach TEXT[] NOT NULL DEFAULT '{}',                       -- [EN: customer_stages] LEAD, INSTALLED...
    kenh_cho_phep       TEXT[] NOT NULL DEFAULT '{MESSENGER}',              -- [EN: allowed_channels]
    trang_thai          VARCHAR(16) NOT NULL DEFAULT 'DRAFT'                -- [EN: status]
                        CHECK (trang_thai IN ('DRAFT','APPROVED','ARCHIVED')),
    quyen_su_dung       VARCHAR(16) NOT NULL DEFAULT 'NEEDS_REVIEW'         -- [EN: rights_status]
                        CHECK (quyen_su_dung IN ('OWNED','LICENSED','LINK_ONLY','NEEDS_REVIEW')),
    nguoi_tao           VARCHAR(100),                                       -- [EN: created_by]
    nguoi_duyet         VARCHAR(100),                                       -- [EN: approved_by]
    duyet_luc           TIMESTAMPTZ,                                        -- [EN: approved_at]
    tao_luc             TIMESTAMPTZ NOT NULL DEFAULT now(),                 -- [EN: created_at]
    sua_luc             TIMESTAMPTZ NOT NULL DEFAULT now(),                 -- [EN: updated_at]
    CONSTRAINT chk_da_duyet_co_tom_tat
        CHECK (trang_thai <> 'APPROVED' OR (tom_tat IS NOT NULL AND dung_khi IS NOT NULL))
);
CREATE INDEX idx_media_chu_de ON media USING GIN (chu_de);

-- EN: media_sources — where a media item lives (upload / YouTube / Facebook / TikTok / slm_v2)
CREATE TABLE media_nguon (
    id                  UUID PRIMARY KEY,
    media_id            UUID NOT NULL REFERENCES media(id) ON DELETE CASCADE,
    loai_nguon          VARCHAR(16) NOT NULL                    -- [EN: source_type]
                        CHECK (loai_nguon IN ('UPLOAD','YOUTUBE','FACEBOOK','TIKTOK','SLM_V2')),
    -- nguồn ngoài (YouTube/Facebook/TikTok) hoặc ảnh từ slm_v2 (id_ngoai = tep_tin.id)
    url_nguon           TEXT,                                   -- [EN: source_url]
    id_ngoai            VARCHAR(200),                           -- [EN: external_id]
    ten_tac_gia         VARCHAR(200),                           -- [EN: author_name]
    -- file tự upload
    bucket_luu_tru      VARCHAR(100),                           -- [EN: storage_bucket]
    duong_dan_luu_tru   VARCHAR(500),                           -- [EN: object_key]
    loai_mime           VARCHAR(100),                           -- [EN: mime_type]
    dung_luong_byte     BIGINT,                                 -- [EN: size_bytes]
    ma_bam_sha256       CHAR(64),                               -- [EN: checksum_sha256]
    -- thông số kỹ thuật
    chieu_rong          INT,                                    -- [EN: width]
    chieu_cao           INT,                                    -- [EN: height]
    thoi_luong_giay     INT,                                    -- [EN: duration_seconds]
    url_anh_thu_nho     TEXT,                                   -- [EN: thumbnail_url]
    trang_thai_xu_ly    VARCHAR(16) NOT NULL DEFAULT 'PENDING'  -- [EN: processing_status]
                        CHECK (trang_thai_xu_ly IN ('PENDING','PROCESSING','READY','FAILED')),
    trang_thai_truy_cap VARCHAR(16) NOT NULL DEFAULT 'UNKNOWN'  -- [EN: availability_status]
                        CHECK (trang_thai_truy_cap IN ('ACTIVE','PRIVATE','UNAVAILABLE','DELETED','UNKNOWN')),
    kiem_tra_cuoi_luc   TIMESTAMPTZ,                            -- [EN: last_checked_at]
    tao_luc             TIMESTAMPTZ NOT NULL DEFAULT now(),     -- [EN: created_at]
    sua_luc             TIMESTAMPTZ NOT NULL DEFAULT now(),     -- [EN: updated_at]
    CONSTRAINT chk_tai_len_co_duong_dan CHECK (loai_nguon <> 'UPLOAD' OR duong_dan_luu_tru IS NOT NULL),
    CONSTRAINT chk_nguon_ngoai_co_id   CHECK (loai_nguon = 'UPLOAD'
                                            OR (id_ngoai IS NOT NULL AND url_nguon IS NOT NULL))
);
CREATE UNIQUE INDEX uq_media_nguon_loai_nguon_id_ngoai ON media_nguon (loai_nguon, id_ngoai)
    WHERE id_ngoai IS NOT NULL;                 -- chống dán trùng link
CREATE UNIQUE INDEX uq_media_nguon_ma_bam_sha256 ON media_nguon (ma_bam_sha256)
    WHERE ma_bam_sha256 IS NOT NULL;             -- chống upload trùng file
CREATE INDEX idx_media_nguon_media_id ON media_nguon (media_id);

-- EN: media_links — links media to packages, materials, brands, knowledge docs
CREATE TABLE media_lien_ket (
    media_id       UUID NOT NULL REFERENCES media(id) ON DELETE CASCADE,
    loai_doi_tuong VARCHAR(20) NOT NULL                    -- [EN: target_type]
                   CHECK (loai_doi_tuong IN ('PACKAGE','MATERIAL','BRAND','KNOWLEDGE_DOC')),
    ma_doi_tuong   VARCHAR(200) NOT NULL,                  -- [EN: target_key] source_id bên catalog hoặc doc_key bên knowledge
    vai_tro        VARCHAR(20) NOT NULL DEFAULT 'RELATED'  -- [EN: role]
                   CHECK (vai_tro IN ('MAIN_IMAGE','GALLERY','EXPLAINER','CASE_STUDY','GUIDE','RELATED')),
    thu_tu         INT NOT NULL DEFAULT 0,                 -- [EN: sort_order]
    PRIMARY KEY (media_id, loai_doi_tuong, ma_doi_tuong, vai_tro)
);
CREATE INDEX idx_media_lien_ket_loai_doi_tuong_ma_doi_tuong ON media_lien_ket (loai_doi_tuong, ma_doi_tuong);
-- Mỗi gói / vật tư chỉ một ảnh chính; ảnh thêm (kể cả ảnh slm_v2 khi đồng bộ catalog) là GALLERY
CREATE UNIQUE INDEX uq_media_lien_ket_anh_chinh ON media_lien_ket (loai_doi_tuong, ma_doi_tuong)
    WHERE vai_tro = 'MAIN_IMAGE';

-- EN: media_channel_refs — cached platform attachment ids per channel account
CREATE TABLE media_tham_chieu_kenh (
    media_nguon_id UUID NOT NULL REFERENCES media_nguon(id) ON DELETE CASCADE,  -- [EN: media_source_id]
    kenh           VARCHAR(20)  NOT NULL,                                   -- [EN: channel] MESSENGER, ZALO...
    tai_khoan_id   VARCHAR(100) NOT NULL,                                   -- [EN: account_id] page_id / oa_id
    attachment_id  VARCHAR(200) NOT NULL,                                   -- dùng lại, không upload lại mỗi lần gửi
    tai_len_luc    TIMESTAMPTZ NOT NULL DEFAULT now(),                      -- [EN: uploaded_at]
    PRIMARY KEY (media_nguon_id, kenh, tai_khoan_id)
);

-- EN: media_deliveries — each time a media item is sent to a customer
CREATE TABLE media_lan_gui (                       -- mỗi lần gửi media cho khách
    id                  BIGSERIAL PRIMARY KEY,
    media_id            UUID NOT NULL REFERENCES media(id),
    media_nguon_id     UUID REFERENCES media_nguon(id),  -- [EN: media_source_id]
    hoi_thoai_id     UUID NOT NULL,  -- [EN: conversation_id]
    tin_nhan_id          UUID,  -- [EN: message_id]
    luot_goi_ai_id           UUID,  -- [EN: ai_run_id]
    kenh             VARCHAR(20) NOT NULL,  -- [EN: channel]
    chon_boi         VARCHAR(16) NOT NULL CHECK (chon_boi IN ('AI','AUTO','HUMAN')),  -- [EN: selected_by]
    ly_do              TEXT,                         -- [EN: reason] lý do AI chọn
    gui_luc             TIMESTAMPTZ NOT NULL DEFAULT now(),  -- [EN: sent_at]
    khach_tra_loi_luc TIMESTAMPTZ,                  -- [EN: customer_replied_at] tin đầu tiên của khách sau khi gửi
    tao_lead        BOOLEAN NOT NULL DEFAULT false  -- [EN: lead_created]
);
CREATE INDEX idx_media_lan_gui_hoi_thoai_id_media_id ON media_lan_gui (hoi_thoai_id, media_id);
CREATE INDEX idx_media_lan_gui_media_id_gui_luc ON media_lan_gui (media_id, gui_luc);
```

`media_lien_ket.ma_doi_tuong` không có khóa ngoại thật vì trỏ sang nhiều loại đối tượng. Ứng dụng kiểm tra key khi ghi; job đồng bộ catalog gắn cờ các link mồ côi.

`media_lan_gui` cần có ngay từ Phase 1 để không gửi lại media trong cùng hội thoại; các cột đo hiệu quả (`khach_tra_loi_luc`, `tao_lead`) được điền dần.

## Content

Chiến dịch, bài đăng và bản đăng theo từng kênh (mục 26, 55).

``` sql
-- EN: content_campaigns — content campaigns
CREATE TABLE nd_chien_dich (
    id                  UUID PRIMARY KEY,
    ten                 VARCHAR(200) NOT NULL,                 -- [EN: name]
    muc_tieu            TEXT,                                  -- [EN: goal] tăng lead, quảng bá pin lưu trữ...
    chu_de              TEXT[] NOT NULL DEFAULT '{}',          -- [EN: topics] theo knowledge/rules/topics.yaml
    nhom_khach_muc_tieu TEXT[] NOT NULL DEFAULT '{}',          -- [EN: target_segments]
    bat_dau_ngay        DATE,                                  -- [EN: starts_on]
    ket_thuc_ngay       DATE,                                  -- [EN: ends_on]
    trang_thai          VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'  -- [EN: status]
                        CHECK (trang_thai IN ('DRAFT','ACTIVE','DONE','CANCELLED')),
    nguoi_tao           VARCHAR(100),                          -- [EN: created_by]
    tao_luc             TIMESTAMPTZ NOT NULL DEFAULT now()     -- [EN: created_at]
);

-- EN: content_posts — content post idea (one post, many channels)
CREATE TABLE nd_bai_dang (                           -- một ý tưởng bài, đăng lên một hoặc nhiều kênh
    id                   UUID PRIMARY KEY,
    chien_dich_id          UUID REFERENCES nd_chien_dich(id),  -- [EN: campaign_id]
    media_id             UUID REFERENCES media(id),
    tieu_de                VARCHAR(300) NOT NULL,        -- [EN: title] tên nội bộ
    loai_noi_dung         VARCHAR(20) NOT NULL  -- [EN: content_type]
                         CHECK (loai_noi_dung IN ('EDUCATION','CASE_STUDY','PROMOTION','CARE','OTHER')),
    chu_de               TEXT[] NOT NULL DEFAULT '{}',  -- [EN: topics]
    cta                  TEXT,
    ly_do_de_xuat            TEXT,                         -- [EN: rationale] lý do / chiến lược, kèm số liệu đã dùng
    tao_boi      VARCHAR(10) NOT NULL CHECK (tao_boi IN ('AI','HUMAN')),  -- [EN: created_by_type]
    phien_chat_id UUID,  -- [EN: assistant_session_id]
    trang_thai               VARCHAR(16) NOT NULL DEFAULT 'DRAFT'  -- [EN: status]
                         CHECK (trang_thai IN ('SUGGESTED','DRAFT','APPROVED','ARCHIVED')),
    nguoi_duyet          VARCHAR(100),  -- [EN: approved_by]
    duyet_luc          TIMESTAMPTZ,  -- [EN: approved_at]
    tao_luc           TIMESTAMPTZ NOT NULL DEFAULT now(),  -- [EN: created_at]
    sua_luc           TIMESTAMPTZ NOT NULL DEFAULT now()  -- [EN: updated_at]
);

-- EN: content_publications — per-channel publication of a post
CREATE TABLE nd_ban_dang_kenh (                    -- bản đăng của một bài trên một kênh (mục 55.3)
    id                UUID PRIMARY KEY,
    bai_dang_id           UUID NOT NULL REFERENCES nd_bai_dang(id) ON DELETE CASCADE,  -- [EN: post_id]
    media_id          UUID REFERENCES media(id),
    media_nguon_id   UUID REFERENCES media_nguon(id),  -- [EN: media_source_id]
    kenh           VARCHAR(20) NOT NULL  -- [EN: channel]
                      CHECK (kenh IN ('FACEBOOK_PAGE','TIKTOK','ZALO_OA','FACEBOOK_GROUP')),
    tai_khoan_id        VARCHAR(100) NOT NULL,           -- [EN: account_id] page_id / tài khoản TikTok / oa_id
    caption           TEXT,
    hashtags          TEXT[] NOT NULL DEFAULT '{}',
    hen_dang_luc      TIMESTAMPTZ,  -- [EN: scheduled_at]
    trang_thai            VARCHAR(20) NOT NULL DEFAULT 'DRAFT'  -- [EN: status]
                      CHECK (trang_thai IN ('DRAFT','PENDING_APPROVAL','SCHEDULED','UPLOADING','PUBLISHING',
                                        'PUBLISHED','FAILED','CANCELLED','MANUAL_REQUIRED')),
    canh_bao_lich     TEXT[] NOT NULL DEFAULT '{}',    -- [EN: rule_warnings] vi phạm mềm quy tắc lịch (mục 26.5)
    khoa_chong_trung   VARCHAR(160) NOT NULL UNIQUE,  -- [EN: idempotency_key]
    id_bai_dang_ngoai  VARCHAR(200),  -- [EN: external_post_id]
    url_bai_dang_ngoai TEXT,  -- [EN: external_post_url]
    ma_loi        VARCHAR(100),  -- [EN: error_code]
    thong_bao_loi     TEXT,                            -- [EN: error_message] đã làm sạch thông tin nhạy cảm
    so_lan_thu_lai       INT NOT NULL DEFAULT 0,  -- [EN: retry_count]
    nguoi_duyet       VARCHAR(100),  -- [EN: approved_by]
    duyet_luc       TIMESTAMPTZ,  -- [EN: approved_at]
    dang_luc      TIMESTAMPTZ,  -- [EN: published_at]
    tao_luc        TIMESTAMPTZ NOT NULL DEFAULT now(),  -- [EN: created_at]
    sua_luc        TIMESTAMPTZ NOT NULL DEFAULT now()  -- [EN: updated_at]
);
CREATE INDEX idx_nd_ban_dang_kenh_trang_thai_hen_dang_luc ON nd_ban_dang_kenh (trang_thai, hen_dang_luc);
CREATE INDEX idx_nd_ban_dang_kenh_bai_dang_id ON nd_ban_dang_kenh (bai_dang_id);

-- EN: content_publication_metrics — metrics snapshots per publication
CREATE TABLE nd_so_lieu_ban_dang (             -- chỉ những chỉ số API cho phép
    ban_dang_id UUID NOT NULL REFERENCES nd_ban_dang_kenh(id) ON DELETE CASCADE,  -- [EN: publication_id]
    lay_luc     TIMESTAMPTZ NOT NULL DEFAULT now(),  -- [EN: fetched_at]
    chi_so        JSONB NOT NULL,                     -- [EN: metrics] {"views":..., "reactions":..., "comments":..., "shares":...}
    so_tin_nhan_den INT,                              -- [EN: inbound_messages] tin nhắn qua link ref của bài hoặc ước tính sau bài
    so_lead          INT,  -- [EN: leads]
    PRIMARY KEY (ban_dang_id, lay_luc)
);
```

## Assistant

Phiên chat của Trợ lý nội dung (mục 26.8) và AI hỗ trợ soạn kiến thức (mục 5.10).

``` sql
-- EN: assistant_sessions — admin assistant chat sessions (content / knowledge)
CREATE TABLE tl_phien_chat (
    id            UUID PRIMARY KEY,
    nguoi_dung_id VARCHAR(100) NOT NULL,                                    -- [EN: user_id] admin đăng nhập
    loai_tro_ly   VARCHAR(16) NOT NULL CHECK (loai_tro_ly IN ('CONTENT','KNOWLEDGE')),  -- [EN: assistant_type]
    tieu_de       VARCHAR(200),                                             -- [EN: title]
    tao_luc       TIMESTAMPTZ NOT NULL DEFAULT now(),                       -- [EN: created_at]
    tin_cuoi_luc  TIMESTAMPTZ                                               -- [EN: last_message_at]
);

-- EN: assistant_messages — assistant chat messages, append-only
CREATE TABLE tl_tin_nhan (                      -- chỉ nối thêm, không sửa lịch sử
    id         BIGSERIAL PRIMARY KEY,
    phien_chat_id UUID NOT NULL REFERENCES tl_phien_chat(id) ON DELETE CASCADE,  -- [EN: session_id]
    vai_tro       VARCHAR(16) NOT NULL CHECK (vai_tro IN ('USER','ASSISTANT')),  -- [EN: role]
    noi_dung    JSONB NOT NULL,                         -- [EN: content] nguyên khối nội dung API (text, tool_use, tool_result)
    luot_goi_ai_id  UUID,  -- [EN: ai_run_id]
    tao_luc TIMESTAMPTZ NOT NULL DEFAULT now()  -- [EN: created_at]
);
CREATE INDEX idx_tl_tin_nhan_phien_chat_id_id ON tl_tin_nhan (phien_chat_id, id);

-- EN: knowledge_change_proposals — AI authoring assistant change proposals
CREATE TABLE kt_de_xuat_sua (             -- đề xuất của AI hỗ trợ soạn (mục 5.10 B)
    id                   BIGSERIAL PRIMARY KEY,
    phien_chat_id           UUID NOT NULL REFERENCES tl_phien_chat(id),  -- [EN: session_id]
    tai_lieu_id               BIGINT REFERENCES kt_tai_lieu(id),          -- [EN: doc_id] NULL khi tạo tài liệu mới
    ma_tai_lieu_de_xuat     VARCHAR(200),  -- [EN: proposed_doc_key]
    hanh_dong               VARCHAR(8) NOT NULL CHECK (hanh_dong IN ('CREATE','UPDATE','RETIRE')),  -- [EN: action]
    phien_ban_goc_id      BIGINT REFERENCES kt_phien_ban(id),  -- [EN: base_version_id]
    noi_dung_de_xuat     TEXT,  -- [EN: proposed_content]
    tom_tat_thay_doi       TEXT NOT NULL,  -- [EN: change_summary]
    bao_cao_kiem_tra_nhanh          JSONB,  -- [EN: lint_report]
    mau_thuan            JSONB,                    -- [EN: conflicts] mâu thuẫn với tài liệu khác AI tìm thấy
    trang_thai               VARCHAR(16) NOT NULL DEFAULT 'PROPOSED'  -- [EN: status]
                         CHECK (trang_thai IN ('PROPOSED','ACCEPTED','REJECTED','SUPERSEDED')),
    phien_ban_tao_ra_id BIGINT REFERENCES kt_phien_ban(id),  -- [EN: resulting_version_id]
    nguoi_quyet_dinh           VARCHAR(100),  -- [EN: decided_by]
    quyet_dinh_luc           TIMESTAMPTZ,  -- [EN: decided_at]
    tao_luc           TIMESTAMPTZ NOT NULL DEFAULT now()  -- [EN: created_at]
);
```

## Webhook Events

``` sql
-- EN: webhook_events — raw inbound webhook events, dedup
CREATE TABLE su_kien_webhook (                          -- sự kiện thô, ghi trước khi xử lý
    id            BIGSERIAL PRIMARY KEY,
    kenh       VARCHAR(20)  NOT NULL,  -- [EN: channel]
    tai_khoan_id    VARCHAR(100) NOT NULL,               -- [EN: account_id] page_id / oa_id / tài khoản TikTok
    ma_su_kien     VARCHAR(200) NOT NULL,               -- [EN: event_key] message id / event id từ nền tảng
    du_lieu_tho       JSONB NOT NULL,                      -- [EN: payload] đã che dữ liệu không cần thiết
    trang_thai        VARCHAR(16) NOT NULL DEFAULT 'RECEIVED'  -- [EN: status]
                  CHECK (trang_thai IN ('RECEIVED','PROCESSED','IGNORED','FAILED')),
    thong_bao_loi TEXT,  -- [EN: error_message]
    nhan_luc   TIMESTAMPTZ NOT NULL DEFAULT now(),  -- [EN: received_at]
    xu_ly_luc  TIMESTAMPTZ,  -- [EN: processed_at]
    UNIQUE (kenh, ma_su_kien)
);
```

## Conversations

``` sql
-- EN: conversations — customer conversations
CREATE TABLE hoi_thoai (
    id                     UUID PRIMARY KEY,
    khach_hang_id          UUID NOT NULL REFERENCES khach_hang(id),         -- [EN: customer_id]
    dinh_danh_kenh_id      UUID NOT NULL REFERENCES dinh_danh_kenh(id),     -- [EN: channel_identity_id]
    kenh                   VARCHAR(20) NOT NULL,                            -- [EN: channel]
    nguoi_phu_trach        VARCHAR(16) NOT NULL DEFAULT 'AI_ACTIVE'         -- [EN: ownership]
                           CHECK (nguoi_phu_trach IN ('AI_ACTIVE','HUMAN_OWNED')),
    nhan_vien_phu_trach_id VARCHAR(100),                                    -- [EN: owner_staff_id]
    tiep_quan_luc          TIMESTAMPTZ,                                     -- [EN: taken_over_at] lúc chuyển HUMAN_OWNED
    nhan_vien_tin_cuoi_luc TIMESTAMPTZ,                                     -- [EN: last_staff_message_at] để tự trả lại AI (mục 19)
    che_do_tra_loi         VARCHAR(8) NOT NULL DEFAULT 'AUTO' CHECK (che_do_tra_loi IN ('DRAFT','AUTO')),  -- [EN: reply_mode] AUTO: theo loại câu hỏi (mục 35); DRAFT: ép nháp cả hội thoại
    trang_thai             VARCHAR(8) NOT NULL DEFAULT 'OPEN' CHECK (trang_thai IN ('OPEN','CLOSED')),  -- [EN: status]
    y_dinh                 VARCHAR(50),                                     -- [EN: intent]
    tom_tat                TEXT,                                            -- [EN: summary] tóm tắt khi hội thoại dài
    tin_khach_cuoi_luc     TIMESTAMPTZ,                                     -- [EN: last_customer_message_at] tính cửa sổ 24 giờ / cửa sổ tin tư vấn
    han_tra_loi_luc        TIMESTAMPTZ,                                     -- [EN: reply_due_at] gộp tin: xử lý khi tới hạn (mục 19); trong lúc AI xử lý là hạn giữ lượt
    tu_tra_lai_luc         TIMESTAMPTZ,                                     -- [EN: auto_handback_at] lượt AI đầu tiên sau khi tự trả lại đi theo DRAFT (mục 19)
    tao_luc                TIMESTAMPTZ NOT NULL DEFAULT now(),              -- [EN: created_at]
    sua_luc                TIMESTAMPTZ NOT NULL DEFAULT now()               -- [EN: updated_at]
);
CREATE INDEX idx_hoi_thoai_han_tra_loi_luc ON hoi_thoai (han_tra_loi_luc) WHERE han_tra_loi_luc IS NOT NULL;
CREATE INDEX idx_hoi_thoai_khach_hang_id ON hoi_thoai (khach_hang_id);
```

## Messages

``` sql
-- EN: messages — conversation messages
CREATE TABLE tin_nhan (
    id                UUID PRIMARY KEY,
    hoi_thoai_id      UUID NOT NULL REFERENCES hoi_thoai(id) ON DELETE CASCADE,  -- [EN: conversation_id]
    chieu             VARCHAR(4)  NOT NULL CHECK (chieu IN ('IN','OUT')),   -- [EN: direction]
    nguoi_gui         VARCHAR(16) NOT NULL CHECK (nguoi_gui IN ('CUSTOMER','AI','STAFF','SYSTEM')),  -- [EN: sender_type]
    nhan_vien_id      VARCHAR(100),                                         -- [EN: staff_id]
    id_tin_nhan_ngoai VARCHAR(200),                                         -- [EN: external_message_id]
    noi_dung          TEXT,                                                 -- [EN: content]
    dinh_kem          JSONB NOT NULL DEFAULT '[]',                          -- [EN: attachments]
    trang_thai        VARCHAR(16) NOT NULL                                  -- [EN: status]
                      CHECK (trang_thai IN ('RECEIVED','PROCESSED','DRAFT','SENT','FAILED','DISCARDED')),
    luot_goi_ai_id    UUID,                                                 -- [EN: ai_run_id]
    tao_luc           TIMESTAMPTZ NOT NULL DEFAULT now(),                   -- [EN: created_at]
    gui_luc           TIMESTAMPTZ                                           -- [EN: sent_at]
);
CREATE UNIQUE INDEX uq_tin_nhan_hoi_thoai_id_id_tin_nhan_ngoai ON tin_nhan (hoi_thoai_id, id_tin_nhan_ngoai)
    WHERE id_tin_nhan_ngoai IS NOT NULL;
CREATE INDEX idx_tin_nhan_hoi_thoai_id_tao_luc ON tin_nhan (hoi_thoai_id, tao_luc);
```

## Consult Profiles

``` sql
-- EN: consult_profiles — collected consultation info and lead level
CREATE TABLE ho_so_tu_van (                        -- thông tin tư vấn đã thu thập (consult-slots.yaml)
    hoi_thoai_id         UUID PRIMARY KEY REFERENCES hoi_thoai(id) ON DELETE CASCADE,  -- [EN: conversation_id]
    khach_hang_id             UUID NOT NULL REFERENCES khach_hang(id),  -- [EN: customer_id]
    thong_tin_da_thu                   JSONB NOT NULL DEFAULT '{}', -- [EN: slots] {"khu_vuc": "Hà Nội", "mien": "BAC", "hoa_don_thang": 2000000, ...}
    thong_tin_con_thieu           TEXT[] NOT NULL DEFAULT '{}',  -- [EN: missing_slots]
    so_cau_da_tra_loi      INT NOT NULL DEFAULT 0,      -- [EN: answered_questions] số câu hỏi của AI khách đã trả lời (mục 58.5)
    phan_loai           JSONB NOT NULL DEFAULT '{}', -- [EN: qualification] research_level (+ căn cứ), budget, timeline (mục 58.3)
    muc_tiem_nang              VARCHAR(16) CHECK (muc_tiem_nang IN ('HOT','WARM','COLD','OFF_SEGMENT')),  -- [EN: lead_level]
    ly_do_muc_tiem_nang       TEXT,  -- [EN: lead_level_reason]
    tron_goi_de_xuat_ids INT[]  NOT NULL DEFAULT '{}',  -- [EN: recommended_package_ids]
    cau_hoi_cuoi         VARCHAR(50),  -- [EN: last_question_asked] câu AI vừa hỏi ở tin đã gửi; tin sau của khách trả lời đúng câu này thì so_cau_da_tra_loi tăng
    sua_luc              TIMESTAMPTZ NOT NULL DEFAULT now()  -- [EN: updated_at]
);
```

## Thông báo nhân viên

Mục 34 "Thông báo cho nhân viên". Migration V5.

``` sql
-- EN: notifications — staff notifications (AI handoff, AI draft, HOT lead, send failure...)
CREATE TABLE thong_bao (
    id                BIGSERIAL PRIMARY KEY,
    loai              VARCHAR(32)  NOT NULL                                     -- [EN: type]
                      CHECK (loai IN ('AI_HANDOFF','AI_DRAFT','HANDBACK_REPLY','LEAD_HOT','SEND_FAILED','DUPLICATE_PHONE')),
    muc_do            VARCHAR(10)  NOT NULL DEFAULT 'INFO'                      -- [EN: severity]
                      CHECK (muc_do IN ('INFO','WARNING','URGENT')),
    tieu_de           VARCHAR(300) NOT NULL,                                    -- [EN: title]
    noi_dung          TEXT,                                                     -- [EN: body] không chứa số điện thoại
    du_lieu           JSONB NOT NULL DEFAULT '{}',                              -- [EN: payload] lý do, nháp AI, sđt (chỉ trả qua REST)
    nguoi_nhan_id     UUID REFERENCES nguoi_dung(id) ON DELETE CASCADE,         -- [EN: recipient_id] NULL = mọi STAFF và SYSTEM_ADMIN
    hoi_thoai_id      UUID,                                                     -- [EN: conversation_id]
    khach_hang_id     UUID,                                                     -- [EN: customer_id]
    khoa_chong_trung  VARCHAR(200) NOT NULL UNIQUE,                             -- [EN: dedupe_key] ví dụ AI_DRAFT:{tin_nhan_id}
    tao_luc           TIMESTAMPTZ  NOT NULL DEFAULT now()                       -- [EN: created_at]
);
CREATE INDEX idx_thong_bao_nguoi_nhan_id_id ON thong_bao (nguoi_nhan_id, id DESC);

-- EN: notification_reads — per-user read state
CREATE TABLE thong_bao_da_doc (
    nguoi_dung_id  UUID   NOT NULL REFERENCES nguoi_dung(id) ON DELETE CASCADE,  -- [EN: user_id]
    thong_bao_id   BIGINT NOT NULL REFERENCES thong_bao(id) ON DELETE CASCADE,   -- [EN: notification_id]
    doc_luc        TIMESTAMPTZ NOT NULL DEFAULT now(),                           -- [EN: read_at]
    PRIMARY KEY (nguoi_dung_id, thong_bao_id)
);
```

## Leads

``` sql
-- EN: leads — leads sent to the Sales API
CREATE TABLE khach_tiem_nang (
    id                      UUID PRIMARY KEY,
    khach_hang_id           UUID NOT NULL REFERENCES khach_hang(id),        -- [EN: customer_id]
    hoi_thoai_id            UUID REFERENCES hoi_thoai(id),                  -- [EN: conversation_id]
    kenh_nguon              VARCHAR(20) NOT NULL,                           -- [EN: source_channel] mã kênh mục 54.1: MESSENGER, ZALO_OA, TIKTOK_LEAD...
    tinh                    VARCHAR(100),                                   -- [EN: province] tỉnh khách nói
    mien                    VARCHAR(8) CHECK (mien IN ('BAC','NAM')),       -- [EN: region] theo rules/regions.yaml
    sdt                     VARCHAR(20) NOT NULL,                           -- [EN: phone] đã chuẩn hóa
    tom_tat_nhu_cau         TEXT,                                           -- [EN: need_summary]
    muc_tiem_nang           VARCHAR(16) CHECK (muc_tiem_nang IN ('HOT','WARM','COLD','OFF_SEGMENT')),  -- [EN: lead_level]
    ly_do_muc_tiem_nang     TEXT,                                           -- [EN: lead_level_reason]
    phan_loai               JSONB NOT NULL DEFAULT '{}',                    -- [EN: qualification] research_level, budget, timeline
    tron_goi_de_xuat_ids    INT[] NOT NULL DEFAULT '{}',                    -- [EN: recommended_package_ids]
    trang_thai              VARCHAR(16) NOT NULL DEFAULT 'NEW'              -- [EN: status]
                            CHECK (trang_thai IN ('NEW','SENT','FAILED','DUPLICATE')),
    ma_tham_chieu_sales_api VARCHAR(200),                                   -- [EN: sales_api_ref] ID phía Sales API trả về
    so_lan_thu              INT NOT NULL DEFAULT 0,                         -- [EN: attempt_count]
    khoa_chong_trung        VARCHAR(160) NOT NULL UNIQUE,                   -- [EN: idempotency_key]
    loi_gan_nhat            TEXT,                                           -- [EN: last_error]
    tao_luc                 TIMESTAMPTZ NOT NULL DEFAULT now(),             -- [EN: created_at]
    gui_luc                 TIMESTAMPTZ                                     -- [EN: sent_at]
);
```

## Example Candidates

Các bảng `style_profiles`, `writing_examples`, `conversation_examples`, `conversation_playbooks` trước đây đã chuyển thành tài liệu trong `knowledge/` (mục 9–16). Bảng dưới đây chỉ là hàng chờ duyệt ví dụ.

``` sql
-- EN: example_candidates — review queue for conversation examples
CREATE TABLE kt_ung_vien_vi_du (                      -- duyệt xong mới đưa vào knowledge/examples/
    id               BIGSERIAL PRIMARY KEY,
    nguon           VARCHAR(16) NOT NULL CHECK (nguon IN ('DRAFT_EDIT','IMPORTED_CHAT','MANUAL')),  -- [EN: source]
    sua_ban_nhap_id    BIGINT REFERENCES kt_sua_ban_nhap(id),  -- [EN: draft_edit_id]
    kenh          VARCHAR(20),  -- [EN: channel]
    chu_de           TEXT[] NOT NULL DEFAULT '{}',  -- [EN: topics]
    giai_doan_khach   VARCHAR(20),  -- [EN: customer_stage]
    tin_khach TEXT NOT NULL,                    -- [EN: customer_message] đã ẩn tên, số điện thoại, địa chỉ
    tra_loi_cua_nguoi   TEXT NOT NULL,  -- [EN: human_response]
    ngu_canh          TEXT,  -- [EN: context]
    ket_qua          TEXT,                             -- [EN: outcome] khách trả lời tiếp, để lại số, chốt...
    diem_chat_luong    SMALLINT CHECK (diem_chat_luong BETWEEN 1 AND 5),  -- [EN: quality_score]
    trang_thai           VARCHAR(16) NOT NULL DEFAULT 'NEW' CHECK (trang_thai IN ('NEW','PROMOTED','REJECTED')),  -- [EN: status]
    ma_tai_lieu_da_dua_vao VARCHAR(200),  -- [EN: promoted_doc_key]
    nguoi_duyet      VARCHAR(100),  -- [EN: reviewed_by]
    tao_luc       TIMESTAMPTZ NOT NULL DEFAULT now()  -- [EN: created_at]
);
```

Đo hiệu quả media (mục 28) dùng `media_lan_gui` và `khach_tiem_nang`, không có bảng riêng.

------------------------------------------------------------------------

# 30. Công nghệ đề xuất

| Thành phần | Công nghệ |
|---|---|
| Backend | Java 17 + Spring Boot 3.5 (cùng phiên bản với `slm_v2`, chốt 2026-10-20); một ứng dụng chia module (modular monolith) + một worker |
| API | REST; stream (SSE) cho chat của Trợ lý nội dung |
| AI | Claude API qua Anthropic Java SDK; model chọn bằng bộ test (mục 20) |
| Database | PostgreSQL 16 + Flyway |
| Job / lịch | Lưu trong PostgreSQL (Quartz JDBC hoặc JobRunr): gộp tin, follow-up, đăng bài, đồng bộ |
| Cache, giới hạn tần suất | Trong ứng dụng (ví dụ Caffeine, Bucket4j); thêm Redis khi chạy nhiều instance |
| Vector | Chưa dùng; pgvector + nhà cung cấp embedding khi kiến thức vượt ngân sách (mục 5.7) |
| Frontend | Vben Admin + Vue 3 + TypeScript |
| Object storage | MinIO / S3, phục vụ media qua domain HTTPS (reverse proxy) |
| Facebook | Meta Graph API (Messenger Platform, Pages API) |
| Zalo | Zalo OA OpenAPI + Webhook + ZNS |
| TikTok | Content Posting API, Lead Generation API, Business Messaging API (khi đủ quyền) |
| Repo / MCP bên thứ ba | Chỉ để tham khảo; production viết lại bằng Java + Spring Boot (mục 46.1) |
| Tùy chọn rủi ro cao | Facebook Groups adapter Java (port từ facebook-go); Zalo cá nhân: chờ quyết định, có thể là ngoại lệ worker Node.js (mục 46.1). Cả hai tắt mặc định (mục 54.6) |
| Dữ liệu nguồn | `slm_v2` qua view `ai_v_*` (mục 4.2); Solar API và API hãng inverter (Phase 4) |
| Monitoring | Micrometer + Prometheus + Grafana; log tập trung bằng Loki |
| Bảo mật | Spring Security + JWT (Keycloak nếu cần SSO); secret manager cho token, cookie, API key |
| Deploy | Docker Compose |
------------------------------------------------------------------------

# 31. Service boundaries

Một ứng dụng Spring Boot chia module theo các thư mục dưới đây (modular monolith), cộng một worker; không tách microservice ở giai đoạn đầu.

Đây là cấu trúc chuẩn duy nhất; các mục 26.9, 53, 55.4, 56.5, 57.5 đều theo cấu trúc này. Gói Java gốc chốt khi tạo project; các đường dẫn trong tài liệu ghi tương đối so với gói gốc.

``` text
/apps
  /api              # REST cho Vben Admin + webhook các kênh
  /worker           # job nền: gộp tin, follow-up, đăng bài, đồng bộ, media (lịch trong PostgreSQL)
  /admin            # Vben Admin (mục 34)

/services
  /customer         # Customer 360, dinh_danh_kenh
  /conversation     # hội thoại, gộp tin, khóa, người phụ trách
  /ai               # orchestrator, ghép prompt, MediaResolver, luot_goi_ai
  /policy           # Policy Engine (mục 33)
  /catalog          # đồng bộ slm_v2, tool catalog
  /sizing           # SizingService (rules/sizing-rules.yaml)
  /knowledge        # Knowledge Base, phát hành, AI hỗ trợ soạn (văn phong, playbook, ví dụ cũng ở đây)
  /media            # Media Library
  /content          # Content Planner, Trợ lý nội dung, xuất bản (mục 26, 55)
  /lead             # tạo lead, lead-scoring, gửi Sales API (mục 23, 58.5)
  /leadnurturing    # follow-up (mục 56)
  /energy           # Phase 4 (mục 21)

/adapters
  /facebook         # Page + Messenger + đăng bài, Meta Graph API
  /zalo             # Zalo OA + ZNS
  /tiktok           # messaging / publishing / lead tách riêng (mục 57.5)
  /slm_v2           # đọc view ai_v_*
  /sales_api
  /solar            # Solar API, API hãng inverter (Phase 4)
  /facebook_groups  # tùy chọn rủi ro cao, tắt mặc định (mục 54.6)

/infrastructure
  /postgres         # dữ liệu + job queue
  /storage          # MinIO / S3
  /monitoring
```

Zalo cá nhân (`zca-js`), nếu được duyệt ngoại lệ, là worker Node.js riêng ngoài cấu trúc này (mục 46.1).

------------------------------------------------------------------------

# 32. AI Orchestrator

Orchestrator là code Java, không phải một lời gọi AI. Nó làm phần **tất định**; Claude quyết định phần **ngữ nghĩa** thông qua tool và kết quả có cấu trúc (mục 18).

Orchestrator quyết định bằng code:

- Hội thoại đang do AI hay người phụ trách (`nguoi_phu_trach`); `HUMAN_OWNED` thì dừng.
- Còn trong cửa sổ nhắn tin của kênh không (mục 54.2); chế độ `DRAFT` hay `AUTO` theo cấu hình.
- Ghép ngữ cảnh cố định (mục 5.4): kiến thức lõi, tóm tắt catalog + danh mục media, hồ sơ tư vấn, media đã gửi, lịch sử hội thoại (tóm tắt khi dài).
- Bộ tool được phép cho kênh và hội thoại đó.

Claude quyết định qua tool:

- Cần hỏi thêm gì; có cần tra catalog hay tính công suất không; gửi media nào; có chuyển người không.

Tool của bot chăm sóc khách:

| Tool | Việc |
|---|---|
| `tim_goi_phu_hop(...)` | Tính công suất và tìm gói (mục 4.2.6) |
| `chi_tiet_goi(goi_id)` | Thành phần, bảo hành, giá khung sắt |
| `tra_cuu_vat_tu(ma hoặc từ khóa)` | Thông số, bảo hành, giá lẻ theo cơ sở |
| `cap_nhat_ho_so_tu_van(thong_tin)` | Lưu thông tin đã thu thập vào `ho_so_tu_van`, kiểm tra theo `consult-slots.yaml` |
| `tao_lead(so_dien_thoai, nhu_cau)` | Chuẩn hóa số điện thoại Việt Nam, tạo `khach_tiem_nang`; `lead-dispatch` gửi Sales API có retry và idempotency |
| `chuyen_nhan_vien(ly_do)` | Chuyển `HUMAN_OWNED`, báo nhân viên |

- Không tool nào gửi tin ra kênh; gửi chỉ xảy ra sau Policy Engine (mục 33).
- Tool đọc dữ liệu khách luôn bị backend khóa theo khách của hội thoại hiện tại, không nhận `khach_hang_id` từ model, để chặn prompt injection kiểu "cho tôi xem thông tin khách khác".
------------------------------------------------------------------------

# 33. Policy Engine

Claude không được gọi trực tiếp:

``` text
send_zalo()
send_facebook()
```

Thay vào đó:

``` text
Claude
 ↓
AI Orchestrator
 ↓
Policy Engine
 ↓
Deduplication
 ↓
Rate Limit
 ↓
Permission Check
 ↓
Channel Adapter
 ↓
Send
```

Policy kiểm tra:

-   Khách có được phép liên hệ không?
-   Kênh có cho phép không?
-   Có gửi trùng không?
-   Tần suất có vượt giới hạn không?
-   Nội dung có vi phạm policy không?
-   Có cần human approval không?
-   Video có được phép dùng ở channel đó không?
-   Con số trong câu trả lời có nguồn hợp lệ không (dưới đây)?
-   Công tắc dừng toàn cục có đang bật không?

## 33.1 Kiểm tra con số

1. **Tách số:** chuẩn hóa cách viết tiếng Việt ("90,8 triệu", "90.800.000đ", "~91tr", "5 kWp", "500 kWh") thành giá trị + đơn vị. Số viết bằng chữ ("chín mươi triệu") bị cấm trong văn phong (`core/voice.md`), nên coi là lỗi.
2. **Chỉ kiểm tra các loại số rủi ro:** tiền (đ, nghìn, triệu, tr), công suất (W, kW, kWp), điện năng (kWh), phần trăm, số năm / tháng bảo hành.
3. **Nguồn hợp lệ** của mỗi số:
   - kết quả tool trong hội thoại này, của bản catalog hiện hành;
   - tin của chính khách (ví dụ nhắc lại "hóa đơn 2 triệu");
   - tài liệu kiến thức đang phát hành (ví dụ năm bảo hành trong `policies/`).
4. **Cho phép làm tròn** theo quy tắc cố định (ví dụ 90.800.000 ↔ "90,8 triệu" ↔ "khoảng 91 triệu"); ngoài ra phải khớp.
5. **Không qua kiểm tra:** không bỏ tin, mà chuyển lượt đó sang `DRAFT` kèm lý do trong `luot_goi_ai.ket_qua_policy`. Tỷ lệ lượt bị chặn theo dõi trên dashboard để chỉnh bộ tách số, tránh chặn nhầm quá nhiều.

## 33.2 Công tắc dừng toàn cục

- Một cờ trên admin (quyền cao nhất): khi bật, mọi lượt đi theo `DRAFT`, Lead Nurturing và đăng bài tự động tạm dừng; tin nhận vẫn được lưu.
- Tự bật khi có dấu hiệu bất thường: tỷ lệ lỗi Claude API, tỷ lệ lượt bị Policy chặn hoặc tỷ lệ nhân viên báo BAD vượt ngưỡng trong một khoảng thời gian (cấu hình).
- Mỗi lần bật / tắt gửi cảnh báo cho admin và ghi audit log.

------------------------------------------------------------------------

# 34. Admin Dashboard — Vben Admin + Vue 3

Admin Dashboard sử dụng **Vben Admin (Vue 3 + TypeScript)**, không dùng Next.js.

## Công nghệ giao diện

- Vben Admin làm nền tảng admin UI.
- Vue 3 + TypeScript + Vite.
- Dùng UI component library theo template Vben đã chọn; nếu dùng bản Vben Ant Design Vue thì thống nhất component của Ant Design Vue, không trộn nhiều UI framework tùy tiện.
- Vue Router cho điều hướng; Pinia cho state dùng chung; Axios hoặc HTTP client hiện có cho REST API.
- Backend vẫn là Java 17 + Spring Boot 3.5; frontend gọi REST API qua `/api` hoặc base URL cấu hình theo môi trường.
- JWT/OAuth2 được kiểm tra tại backend; menu/route permission ở frontend chỉ phục vụ UX, không thay thế authorization phía backend.

## Các module chính

```text
Vben Admin
│
├── Overview / KPI Dashboard
├── Customer 360
├── AI Inbox / Conversation Timeline
├── Lead Management & Lead Nurturing
├── Sales Handoff
├── Energy / Solar Production
├── Channel Accounts (Facebook / Zalo / TikTok; capability, nhãn rủi ro cao)
├── Campaign & Content Planner (lịch nội dung theo kênh)
├── Trợ lý nội dung (chat AI, mục 26)
├── Media Library (ảnh/video upload + link YouTube/Facebook/TikTok)
├── Video Publishing Jobs
├── Knowledge Editor (soạn, phiên bản, phát hành, AI hỗ trợ soạn; mục 5.9–5.10)
├── Brand Voice & Writing Style
├── Conversation Examples
├── Sales Playbooks / Strategy
├── AI Personas & Agent Settings
├── AI Evaluation / Human Review
├── Follow-up Queue & Policy Blocks
├── AI Cost & Usage
├── Audit Logs
└── System Settings / Roles / Permissions
```

## Thông báo cho nhân viên (đã làm 2026-10-26)

Trước khi có inbox admin, nhân viên vẫn trả lời khách trong Meta Business Suite; trang admin báo cho nhân viên biết lúc nào cần vào. Chỉ khi tab admin đang mở (doanh nghiệp chọn không làm Web Push).

| Loại | Khi nào | Mức |
|---|---|---|
| `AI_HANDOFF` | AI chuyển hội thoại cho nhân viên (sự cố, khiếu nại, khách muốn gặp người, AI lỗi 3 lần) | URGENT |
| `AI_DRAFT` | Lượt AI chỉ lưu nháp (loại câu hỏi không tự động, con số chưa có nguồn, quá 24 giờ…) | WARNING |
| `HANDBACK_REPLY` | Khách nhắn lại sau khi hội thoại tự trả lại AI (lượt đầu là nháp, mục 19) | WARNING |
| `LEAD_HOT` | Lead vừa chuyển sang HOT (chỉ lúc chuyển, không lặp mỗi lượt) | URGENT |
| `SEND_FAILED` | Gửi tin AI / tin báo nhận ra kênh thất bại | WARNING |
| `DUPLICATE_PHONE` | Khách gửi số đã thuộc khách khác: cần gộp khách | INFO |

- **Ghi:** `NotificationService.notify()` chạy trong transaction của nghiệp vụ (lượt AI, lead), `INSERT … ON CONFLICT (khoa_chong_trung) DO NOTHING`; transaction rollback thì không có thông báo. Nội dung ngắn (hiện cả ở thông báo hệ điều hành) không chứa số điện thoại; chi tiết (nháp AI, lý do, tin cuối của khách, số điện thoại) trong `du_lieu`, chỉ trả qua REST.
- **Đẩy:** sau khi commit, `NotificationSseHub` đẩy qua Server-Sent Events `GET /api/v1/notifications/stream` (xác thực bằng header `Authorization`, frontend đọc bằng `fetch`). Ping mỗi 20 giây; token bị thu hồi (đăng xuất) hoặc hết hạn thì gửi `auth-expired` và đóng. Header `X-Accel-Buffering: no` + location riêng trong `nginx-cms.conf` để nginx không gom buffer. Một instance; chạy nhiều instance thì cần kênh phát chung (Redis / PostgreSQL `NOTIFY`).
- **REST:** danh sách (lọc chưa đọc / loại), số chưa đọc, chi tiết, đánh dấu đã đọc, đọc hết tới `maxId`. Trạng thái đọc theo từng người; chỉ hiện thông báo 14 ngày gần nhất.
- **Trình duyệt:** chuông trên thanh tiêu đề có số chưa đọc; trang **Thông báo** có bảng, drawer chi tiết (nháp AI có nút sao chép, nút "Mở hộp thư Meta"), thẻ cài đặt. Âm thanh phát bằng Web Audio (không cần file), mở khoá ở lần bấm đầu tiên, nhiều tab chỉ một tab kêu; thông báo hệ điều hành khi tab đang ẩn (chỉ chạy trên HTTPS, xin quyền khi người dùng bật).
- **Người nhận:** vai trò STAFF và SYSTEM_ADMIN.

## Frontend folder suggestion

```text
apps/admin/
  src/
    api/                 # typed clients for Spring Boot APIs
    router/              # routes and route metadata
    store/               # Pinia stores
    views/
      dashboard/
      customers/
      inbox/
      leads/
      nurturing/
      channels/
      campaigns/
      content-assistant/
      media-library/
      publishing/
      knowledge-base/
      playbooks/
      ai-evaluation/
      ai-usage/
      system/
    components/
    types/
```

## Tích hợp với backend

- Định nghĩa API client/type riêng theo từng module; không gọi Claude API trực tiếp từ trình duyệt.
- Upload ảnh/video qua backend hoặc presigned URL có thời hạn; quyền upload/download được kiểm tra phía server.
- Dùng phân trang, filter, sort phía server cho bảng khách hàng, hội thoại, video và lịch follow-up.
- Các thao tác nhạy cảm như bật auto-send, thay đổi giới hạn liên hệ, xóa nội dung hoặc thay đổi tài khoản kênh cần quyền riêng và audit log.
- Giữ API backend độc lập với Vben để sau này có thể thay frontend mà không phải viết lại nghiệp vụ.

------------------------------------------------------------------------

# 35. AI Evaluation

Sau khi AI trả lời:

``` text
[ GOOD ]
[ BAD ]
[ EDIT ]
```

Nếu nhân viên sửa:

``` text
AI output
    ↓
Human edit
    ↓
Difference
    ↓
Training candidate
    ↓
Admin review
    ↓
Style / Example Library
```

Không tự động biến mọi chỉnh sửa thành luật.


## Loại câu hỏi

AI trả loại câu hỏi của lượt trong trường `intent` (mục 18); backend lưu vào `hoi_thoai.y_dinh` và dùng để chọn `AUTO` / `DRAFT`.

| `intent` | Nội dung | Chế độ mặc định |
|---|---|---|
| `CHAO_HOI` | Chào hỏi, câu chung, hỏi công ty | `AUTO` |
| `TU_VAN_LAP_MOI` | Thu thập thông tin tư vấn, đề xuất gói | `AUTO` |
| `HOI_GIA` | Hỏi giá, khoảng giá | `AUTO` |
| `GIAI_THICH` | On-grid / hybrid, pin lưu trữ, cách hoạt động (FAQ) | `AUTO` |
| `CHINH_SACH` | Bảo hành, thanh toán, quy trình lắp đặt | `AUTO` |
| `KY_THUAT_SU_CO` | Khách đã lắp báo lỗi, sản lượng thấp | Luôn chuyển nhân viên |
| `KHIEU_NAI` | Phàn nàn, tranh chấp | Luôn chuyển nhân viên |
| `KHAC` | Không thuộc nhóm nào | `DRAFT` |

Doanh nghiệp chốt ngày 2026-10-20: năm loại đầu gửi tự động ngay từ Phase 1.

- Chế độ tính theo **lượt**. Một lượt thuộc loại `AUTO` vẫn chuyển sang `DRAFT` khi:
  - không qua kiểm tra của Policy Engine, kể cả kiểm tra con số (mục 33, 33.1);
  - AI yêu cầu chuyển nhân viên (`handoff.needed`);
  - công tắc dừng toàn cục đang bật (mục 33.2);
  - hội thoại đang `HUMAN_OWNED`, hoặc là lượt đầu sau khi tự trả lại AI (mục 19);
  - hội thoại bị đặt `che_do_tra_loi = DRAFT` (ép nháp cả hội thoại, mục 29).
- Khi một lượt rơi vào `DRAFT` ngoài giờ làm việc, gửi một câu báo nhận ("em đã ghi nhận, nhân viên sẽ phản hồi trong giờ làm việc") theo template cố định, để khách không chờ trong im lặng.
- Nhân viên vẫn xem được mọi tin AI đã gửi trên inbox admin và chấm GOOD / BAD / EDIT; các chấm này là nguồn dữ liệu để phát hiện lỗi.

## Bộ test và điều kiện gửi tự động

- **Bộ test:** 100–200 câu hỏi thật (ẩn thông tin khách), mỗi câu ghi hành vi kỳ vọng: con số phải lấy từ tool, trường hợp phải chuyển người, media kỳ vọng, tài liệu liên quan.
- **Kịch bản nhiều lượt:** 30–50 hội thoại mẫu, mỗi kịch bản có hồ sơ khách (tỉnh, số pha, hóa đơn, ngày/đêm, loại mái, mức sẵn sàng). Claude đóng vai khách theo hồ sơ; bot chạy như thật với tool thật trên catalog cố định.
- **Chấm điểm:**
  - Bằng code: mỗi tin chỉ một câu hỏi; thu đủ 5 thông tin tối thiểu trong số lượt cho phép; mức tiềm năng cuối đúng kỳ vọng; con số qua kiểm tra mục 33.1; chuyển người đúng lúc.
  - Bằng model chấm theo tiêu chí viết sẵn: văn phong, độ ngắn gọn, không gây áp lực.
  - Người duyệt đọc mẫu ngẫu nhiên mỗi lần chạy để kiểm tra lại model chấm.
- Chạy lại bộ test trong CI mỗi khi kiến thức, prompt, model hoặc tool thay đổi.
- **Điều kiện trước khi chạy thật** (con số ví dụ, doanh nghiệp tự chỉnh): bộ test đạt trên từng loại `AUTO`, không có lỗi sai thông tin (giá, bảo hành, thông số), không có ca phải chuyển người mà bot tự trả lời. Chưa đạt loại nào thì loại đó chạy `DRAFT` cho tới khi đạt.
- **Quay về `DRAFT` khi chạy thật:** phát hiện lỗi sai thông tin ở một loại, hoặc tỷ lệ nhân viên chấm BAD / EDIT của loại đó vượt ngưỡng (ví dụ 10% trong 7 ngày) → loại đó tự quay về `DRAFT` và báo admin; bật lại sau khi đã sửa kiến thức / prompt và chạy lại bộ test.
------------------------------------------------------------------------

# 36. Cost Control

Chi phí AI dự kiến nhỏ (mục 37); khoản lớn là công người. Vì vậy chỉ tối ưu khi số liệu thật trong `luot_goi_ai` cho thấy cần. Riêng mục 1–3 nên làm ngay vì không ảnh hưởng chất lượng.

Theo thứ tự nên làm:

1.  **Prompt caching** cho khối kiến thức và tóm tắt catalog / danh mục media (mục 5.4); theo dõi `token_doc_cache` trong `luot_goi_ai`.
2.  **Ngân sách token**: kiến thức lõi khoảng 40K, danh mục media khoảng 10K.
3.  **Tóm tắt hội thoại dài** thay vì gửi toàn bộ lịch sử.
4.  **Hạ `effort`** cho lượt chat thông thường.
5.  **Batch API** cho việc chạy nền (mục 20.2).
6.  **Chọn model bằng bộ test** (mục 20); chỉ định tuyến nhiều model khi đủ điều kiện ở mục 20.3.

Đo theo **chi phí mỗi hội thoại hoàn tất** (có lead, hoặc khách được trả lời xong), không chỉ theo từng lượt gọi.
------------------------------------------------------------------------

# 37. Chi phí AI sơ bộ

Giả định mỗi lượt trả lời đọc khoảng 30K token kiến thức từ cache, thêm 3K token mới và sinh 400 token đầu ra. Với 10.000 lượt/tháng (khoảng 1.000 hội thoại × 10 lượt), chưa tính phí ghi cache và token suy nghĩ:

| Model | Ước tính/tháng |
|---|---|
| Haiku 5.5 | ~$8 |
| Sonnet 5.5 | ~$160 |
| Opus 5.5 | ~$260 |

Giá tham chiếu tháng 10/2026; kiểm tra lại bảng giá Anthropic khi chốt. Chi phí thật đo từ `luot_goi_ai` (token, cache) sau khi chạy.

Chi phí cần tính riêng:

-   Claude.
-   Zalo ZNS / tin truyền thông.
-   Meta (nếu chạy quảng cáo).
-   VPS.
-   PostgreSQL.
-   Storage (MinIO/S3) và băng thông media.
-   Monitoring.
-   Backup.
-   Công biên soạn, cập nhật kiến thức và bảng giá, duyệt nội dung: thường là khoản lớn nhất.
------------------------------------------------------------------------

# 38. MVP đề xuất

Lộ trình cho một người phát triển, Facebook làm trước.

**Lưu ý:** phạm vi dưới đây lớn so với một người phát triển. Phương án rút gọn và các phần nên hoãn ở mục 59.6–59.7, chờ chốt.

## Phase 0: Chuẩn bị dữ liệu và nội dung

- **Chốt các quyết định chặn** (mục 59.8): nguồn dữ liệu khách cũ, công thức sizing, điều kiện báo giá, vùng phục vụ, pháp lý dữ liệu cá nhân.
- **Đặt KPI mục tiêu và tiêu chí qua phase:** thời gian phản hồi đầu tiên, tỷ lệ hội thoại có số điện thoại, tỷ lệ lead → khảo sát → chốt, % lượt `AUTO`, tỷ lệ nhân viên sửa nháp. Mỗi phase chỉ mở rộng khi phase trước đạt.
- Tạo view `ai_v_*` và user `ai_sync` trên `slm_v2`; sửa các lỗi dữ liệu nguồn (mục 4.2.7).
- Đồng bộ catalog; viết `SizingService` theo công thức của doanh nghiệp, có unit test.
- Nhập danh sách tỉnh thuộc miền Bắc / miền Nam vào `rules/regions.yaml` (mục 4.2.9).
- Kiến thức v1: `core/`, playbook tư vấn lắp mới, chính sách bảo hành, khoảng 10 FAQ; nhập chiến lược bán hàng thành tài liệu (mục 58.7).
- Bộ test 100–200 câu hỏi thật lấy từ inbox Facebook (mục 35).
- Tạo Meta App, xác minh doanh nghiệp, nộp App Review (mục 54.3).
- Đo số liệu nền: thời gian phản hồi hiện tại, tỷ lệ lead → khảo sát → chốt.

## Phase 1: Facebook Messenger, gửi tự động có kiểm soát

- Webhook, hội thoại, gộp tin, khóa, nhân viên tiếp quản (mục 19).
- Claude + tool catalog / sizing / lead / handoff (mục 32); ảnh gói gắn tự động; vài video đầu tiên trong Media Library.
- Gửi tự động cho 5 loại câu hỏi `CHAO_HOI`, `TU_VAN_LAP_MOI`, `HOI_GIA`, `GIAI_THICH`, `CHINH_SACH` sau khi bộ test đạt; `KHAC` để nháp; sự cố kỹ thuật và khiếu nại chuyển nhân viên (mục 35). Policy kiểm tra con số và công tắc dừng toàn cục có từ ngày đầu (mục 33.1, 33.2). Lead → Sales API.
- Inbox admin, `luot_goi_ai`, `kt_sua_ban_nhap`.
- Phân loại mức tiềm năng (mục 58.5), gửi kèm lead.
- Knowledge Editor cơ bản: sửa, phiên bản, phát hành, rollback; mẫu và kiểm tra tức thì (mục 5.9, 5.10 A).

## Phase 2: Tự động có kiểm soát và nội dung

- Xem lại `KHAC` và các loại đang bị quay về `DRAFT` theo số liệu chấm của nhân viên (mục 35).
- Trả lời comment + private reply.
- Danh mục media đầy đủ (mục 7).
- Trợ lý nội dung + đăng bài lên Page (mục 26).
- Lead Nurturing dạng việc cho sales (mục 56).
- Chạy bộ test khi phát hành kiến thức; AI góp ý và AI viết từ yêu cầu thô (mục 5.3 bước 5, 5.10).

## Phase 3: Zalo OA

- Chờ doanh nghiệp quyết định có dùng Zalo OA hay không (mục 46.1).
- Tin tư vấn trong cửa sổ, lead, template ZNS (mục 54.4).

## Phase 4: Năng lượng

- Adapter API hãng inverter / đồng hồ đo tiêu thụ theo mã thiết bị (mục 21).
- Báo cáo tháng qua ZNS + trang báo cáo web; cảnh báo bất thường → kỹ thuật viên.

## Phase 5: TikTok

- Đăng video sau audit, lead từ Lead Generation, DM khi có Business Account (mục 54.5, 57).

## Sau đó, khi có số liệu

- Xếp hạng media và nội dung theo hiệu quả (`media_lan_gui`, `nd_so_lieu_ban_dang`).
- RAG khi kiến thức vượt ngân sách token (mục 5.7).
- Định tuyến nhiều model khi đủ điều kiện (mục 20.3).

**Không thuộc lộ trình:** tùy chọn rủi ro cao (mục 54.6) chỉ bật khi doanh nghiệp quyết định riêng.
------------------------------------------------------------------------

# 39. Kiến trúc cuối cùng

Sơ đồ tổng thể ở mục 3. Vòng đời một câu trả lời:

``` text
Khách ─► Kênh ─► Webhook ─► Conversation (gộp tin, khóa) ─► Orchestrator
                                                                │ ngữ cảnh: kiến thức (cache) + tóm tắt catalog
                                                                │           + danh mục media + hồ sơ tư vấn
                                                                ▼
                                                        Claude + tools (sizing, catalog, lead, handoff)
                                                                │ reply · attachments · sources · knowledge_gap · handoff
                                                                ▼
Khách ◄─ Kênh ◄─ Adapter ◄───────────────────────────── MediaResolver + Policy Engine
                                                                │
                                                                └─► nháp chờ duyệt / chuyển nhân viên / kt_lo_hong / luot_goi_ai
```

Phản hồi của nhân viên (sửa nháp, chấm GOOD/BAD/EDIT) quay lại thành ví dụ và kiến thức qua mục 5.6 và 35.
------------------------------------------------------------------------

# 40. Nguyên tắc kiến trúc quan trọng nhất

Hệ thống phải giữ 6 lớp độc lập:

``` text
CUSTOMER
    ↓
KNOWLEDGE
    ↓
MEDIA
    ↓
STYLE
    ↓
STRATEGY
    ↓
AI
```

Không đưa tất cả vào một prompt khổng lồ: số liệu (giá, gói, công suất) đi
qua tool; kiến thức lõi có ngân sách token và được cache (mục 5).

Claude chỉ là lớp tạo ngôn ngữ và suy luận.

**Tài sản trí tuệ thực sự của doanh nghiệp nằm ở:**

1.  Dữ liệu khách hàng.
2.  Knowledge Base.
3.  Solar data.
4.  Media Library.
5.  Văn phong thương hiệu.
6.  Hàng nghìn cuộc hội thoại người thật.
7.  Conversation Playbooks.
8.  Feedback của sales.
9.  Hiệu quả từng nội dung.
10. Lịch sử customer journey.

Đó là thứ khiến hệ thống AI ngày càng phù hợp với doanh nghiệp thay vì
chỉ là một chatbot Claude thông thường.

------------------------------------------------------------------------

# 41. Kết luận

Mục tiêu cuối cùng:

``` text
                  DOANH NGHIỆP
                       │
        ┌──────────────┼───────────────┐
        ▼              ▼               ▼
    CUSTOMER       KNOWLEDGE         CONTENT
       360             │               │
        │              │               │
        │          MEDIA LIBRARY       │
        │              │               │
        └──────────────┼───────────────┘
                       │
                 STYLE LIBRARY
                       │
             HUMAN CONVERSATIONS
                       │
                       ▼
                AI ORCHESTRATOR
                       │
                    CLAUDE
                       │
             ┌─────────┴─────────┐
             ▼                   ▼
           ZALO              FACEBOOK
             │                   │
             └─────────┬─────────┘
                       ▼
                 SALES / SUPPORT
                       │
                       ▼
                FEEDBACK LOOP
```

Đây là nền tảng có thể phát triển từ khoảng 3.000 khách hiện tại lên quy
mô lớn hơn mà không phải xây lại kiến trúc từ đầu.


---

# 42. Facebook Repository / Adapter đề xuất

Phần Facebook nên tách thành **hai lớp**, vì Facebook Groups và Facebook Page/Messenger có bản chất kỹ thuật khác nhau.

## 42.1 Repository chính cho Facebook Page / Meta Business

### `oliverames/meta-mcp-server`

Repository:
https://github.com/oliverames/meta-mcp-server

Đây là **nguồn tham khảo chính** khi viết adapter Java cho lớp **Facebook Page / Meta Business** (mục 46.1).

Repository hiện mô tả đây là MCP server cho Meta Business Platform và cung cấp số lượng lớn tools cho Facebook Pages, Instagram, Threads, Ads Manager, Commerce, Conversions API, Insights và các chức năng liên quan.

Trong kiến trúc của công ty, repo nằm **ngoài** đường chạy production:

```text
AI Orchestrator
       │
       ▼
Policy Engine
       │
       ▼
MetaGraphFacebookAdapter (Java)   ◄── đọc & port logic từ oliverames/meta-mcp-server
       │
       ▼
Meta Graph API
       │
       ▼
Facebook Page / Messenger
```

Các nhóm chức năng nên đọc để port:

- Facebook Page.
- Đăng nội dung.
- Quản lý comment.
- Moderation.
- Messaging/conversation tùy API permission và phiên bản repository.
- Insights.
- Các chức năng Meta Business khác khi cần.

**Khuyến nghị:** Đây là repository Meta/Facebook nên ưu tiên đọc đầu tiên cho phần Page/Business, rồi port phần cần dùng sang Java.

Không deploy repository này và không để Claude gọi nó. Adapter Java gọi thẳng Meta Graph API, nên repo thay đổi hay ngừng bảo trì cũng không ảnh hưởng hệ thống.

---

# 43. Chiến lược Java và repo Facebook Groups

## 43.1 Java Backend Strategy — dùng repo sẵn làm tham khảo, viết lại bằng Java

### Mục tiêu

Hệ thống **không bỏ các repo Facebook đã chọn**. Thay vào đó:

1. Dùng repo có sẵn làm **reference implementation / source of logic**.
2. Giữ nguyên các flow, API operation, authentication flow, request/response mapping, pagination, retry, error handling và business capabilities cần thiết.
3. Viết lại phần backend/adapter cần tích hợp bằng **Java 17 + Spring Boot 3.5** (mục 30).
4. Không để Claude hoặc business logic phụ thuộc trực tiếp vào MCP repo.

### Kiến trúc sau khi chuyển sang Java

```text
Vben Admin (Vue 3 + TypeScript)
      │
      ▼
Java Spring Boot Backend (modules theo mục 31)
      │
      ├── customer · conversation · ai (orchestrator) · policy
      ├── catalog · sizing · knowledge · media · content
      ├── lead · leadnurturing · energy
      └── adapters
              ├── facebook          (Meta Graph API chính thức)
              ├── facebook_groups   (tùy chọn rủi ro cao, tắt mặc định)
              ├── zalo · tiktok
              └── slm_v2 · sales_api · solar

Facebook repos (chỉ đọc, nằm ở references/, không deploy)
      │
      ├── oliverames/meta-mcp-server
      │       └── reference cho Meta/Facebook Business operations
      │
      └── teslashibe/facebook-go
              └── reference cho Facebook Groups/internal GraphQL
```

### Nguyên tắc quan trọng

Không thể lấy binary/code Go rồi “đổi sang Java” trực tiếp. Cách làm đúng là **port implementation**: đọc code và license của repo, xác định logic cần dùng, sau đó implement cùng capability bằng Java.

Ví dụ:

```text
Repo Go/TypeScript
       ↓
Phân tích source + LICENSE
       ↓
Xác định API / auth / request / response / retry / pagination
       ↓
Java implementation
       ↓
Spring Boot Adapter
       ↓
Meta / Facebook
```

### Java stack

Bảng công nghệ đầy đủ ở mục 30. Phần riêng cho adapter:

| Thành phần | Công nghệ |
|---|---|
| REST/Webhook | Spring Web |
| HTTP Client | Spring WebClient |
| ORM | Spring Data JPA / Hibernate |
| Retry, giới hạn tần suất | Trong ứng dụng (ví dụ Resilience4j, Bucket4j); job lưu trong PostgreSQL (mục 30) |

### Facebook Adapter

```text
AI Orchestrator
       │
       ▼
FacebookService (Java interface)
       │
       ├── MetaGraphFacebookAdapter
       │       └── Meta Graph API
       │
       └── FacebookGroupsAdapter
               └── logic port từ facebook-go
```

AI chỉ gọi interface nghiệp vụ, ví dụ:

```java
facebookService.sendMessage(...);
facebookService.getConversation(...);
facebookService.createPagePost(...);
facebookService.getComments(...);
facebookGroupsService.searchGroups(...);
facebookGroupsService.readPosts(...);
```

Nhờ vậy nếu repo Facebook thay đổi, chỉ cần sửa adapter; Customer Service, AI Orchestrator và database không phải thay đổi.

### `oliverames/meta-mcp-server`

Repo này vẫn được giữ làm **nguồn tham khảo chính cho Facebook/Meta Business operations**. Không nhất thiết phải deploy MCP server trong production. Các capability phù hợp sẽ được port vào `MetaGraphFacebookAdapter` bằng Java.

Các nhóm logic cần xem xét:
- Facebook Page operations
- Messenger/conversation operations
- comments/moderation
- publishing
- insights
- authentication/authorization flow
- request/response mapping
- pagination/rate-limit/error handling

### `teslashibe/facebook-go`

Repo này được giữ làm **reference implementation cho Facebook Groups**. Phần internal GraphQL/cookie-session cần được tách thành `FacebookGroupsAdapter` riêng, không trộn với adapter Meta Graph API chính thức.

```text
FacebookGroupsAdapter.java
    ├── Authentication/session handling
    ├── GraphQL request builder
    ├── Group search/discovery
    ├── Feed/posts
    ├── Comments
    ├── Reactions
    └── Retry/error handling
```

Do đây là Facebook internal GraphQL/cookie-session, implementation này cần được đánh giá lại mỗi khi Facebook thay đổi và phải tuân thủ quyền sử dụng/tài khoản/chính sách tương ứng.

### License và compliance

Trước khi port hoặc copy bất kỳ phần code nào, kiểm tra `LICENSE` của từng repo. Ưu tiên **học/port logic và API behavior** thay vì copy nguyên source nếu license hoặc dependency có hạn chế. Với code đưa vào sản phẩm thương mại, cần lưu lại license/notice theo yêu cầu của từng dependency.

### MCP có còn cần không?

Không. Theo quy tắc ở mục 46.1, production chạy hoàn toàn bằng Java adapters; MCP bên thứ ba chỉ dùng trên máy dev để tham khảo và thử API:

```text
Claude
  ↓
AI Orchestrator
  ↓
Policy Engine
  ↓
Java Facebook Adapter
  ↓
Meta/Facebook
```

Đây là hướng được chọn cho phiên bản Java của hệ thống.

## 43.2 Facebook Groups: `teslashibe/facebook-go`

Repository:

https://github.com/teslashibe/facebook-go

Đây là nguồn tham khảo cho **Facebook Groups** (tùy chọn rủi ro cao, tắt mặc định, mục 54.6), không phải repository chính cho Page/Messenger.

Repository hiện cung cấp Go client cho Facebook internal GraphQL API, với Groups surface hỗ trợ:

- Search groups.
- Discover groups.
- Joined groups.
- Get group.
- Join group.
- Leave group.
- Read group feed.
- Read posts.
- Read comments.
- Create post.
- Create comment.
- React.
- Trend analysis.
- MCP tools.

Kiến trúc:

```text
FacebookGroupsAdapter (Java, port từ facebook-go)
        │
        ▼
Facebook internal GraphQL
        │
        ▼
Facebook Groups
```

Repository sử dụng cookie/session authentication và Facebook internal GraphQL, không phải official Graph API.

**Do đó adapter port từ repo này phải tách riêng và có rủi ro bảo trì cao hơn official API.**

Không chạy `facebook-go` ở production và không để nó trở thành dependency của hệ thống (mục 46.1).

---

# 44. Repository cũ/alternative: HagaiHen/facebook-mcp-server

### `HagaiHen/facebook-mcp-server`

Repository:

https://github.com/HagaiHen/facebook-mcp-server

Repository này là MCP server cho Facebook Page dựa trên Facebook Graph API, tập trung vào các chức năng như:

- Post.
- Comment moderation.
- Insights.
- Sentiment filtering.

Trong kiến trúc mới, repo này **chỉ dùng để đối chiếu** khi viết `MetaGraphFacebookAdapter`, không phải nguồn tham khảo chính.

Lý do:

- Phạm vi nhỏ hơn.
- Repository tập trung vào Page automation.
- `oliverames/meta-mcp-server` có phạm vi Meta Business rộng hơn.

Giữ repository này ở mức tham khảo phụ: code Python nhỏ, dễ đọc, tiện đối chiếu cách gọi Graph API. Không fork để chạy (mục 46.1).

---

# 45. Quyết định cuối cùng cho Facebook

Tôi đề xuất:

```text
                AI Orchestrator
                       │
                 Policy Engine
                       │
           ┌───────────┴────────────────┐
           ▼                            ▼
 MetaGraphFacebookAdapter      FacebookGroupsAdapter
 (Java, Meta Graph API)        (Java, tắt mặc định, mục 54.6)
           │                            │
           ▼                            ▼
      PAGE / MSG                     GROUPS

 Tham khảo khi viết (không deploy):
   meta-mcp-server ─► MetaGraphFacebookAdapter
   facebook-go     ─► FacebookGroupsAdapter
```

Trong đó:

### Tham khảo chính

**`oliverames/meta-mcp-server`**

Nguồn tham khảo để viết adapter Java cho Facebook Page/Meta Business.

### Tùy chọn rủi ro cao (tắt mặc định, mục 54.6)

**`teslashibe/facebook-go`**

Nguồn tham khảo để port `FacebookGroupsAdapter`, chỉ khi công ty cần:

- Tìm group.
- Theo dõi group.
- Đọc bài.
- Phân tích chủ đề.
- Đăng/comment trong những trường hợp được phép, có người duyệt từng lần (mục 54.6).

### Tham khảo phụ

**`HagaiHen/facebook-mcp-server`**

Dùng để đối chiếu cách gọi Graph API cho Page.

---

# 46. Không gắn repository trực tiếp vào AI

Không nên:

```text
Claude
  ↓
facebook-go
```

hoặc:

```text
Claude
  ↓
meta-mcp-server
```

Production nên là:

```text
                     Claude
                       │
                       ▼
               AI Orchestrator
                       │
                 Policy Engine
                       │
              Facebook adapters (Java)
                       │
          ┌────────────┴─────────────────┐
          ▼                              ▼
 MetaGraphFacebookAdapter       FacebookGroupsAdapter
 → Meta Graph API               (port từ facebook-go, tắt mặc định)
          │                              │
          ▼                              ▼
       PAGE/MSG                        GROUPS
```

Lợi ích:

- AI không có quyền Facebook trực tiếp.
- Dễ rate limit.
- Dễ audit.
- Dễ chống gửi trùng.
- Dễ đổi repository.
- Dễ chuyển từ repository sang official API.
- Tách Page và Group rõ ràng.
- Token/cookie không nằm trong prompt hoặc AI context.

## 46.1 Quy tắc chung: toàn bộ tích hợp viết bằng Java + Spring Boot

Áp dụng cho mọi kênh (Facebook, Zalo, TikTok) và mọi repo / MCP được nhắc trong kế hoạch.

1. **Mọi code tích hợp chạy production đều viết bằng Java 17 + Spring Boot 3.5**, nằm trong cùng ứng dụng (mục 30, 31). Không chạy MCP server, binary Go hay Node.js của bên thứ ba ở production.
2. **Repo và MCP bên thứ ba chỉ là tài liệu tham khảo:** đọc để biết endpoint, luồng xác thực, request/response, pagination, retry, rate limit và các lỗi hay gặp; sau đó viết adapter Java gọi **API chính thức**.
3. **Quy trình port** (chi tiết ở mục 43):
   1. Đọc source và `LICENSE` của repo.
   2. Liệt kê các chức năng thực sự cần dùng.
   3. Viết interface nghiệp vụ Java (ví dụ `FacebookService`, `VideoPublishingAdapter`) và adapter dùng Spring WebClient.
   4. Test với API thật hoặc môi trường sandbox / dev mode của nền tảng.
   5. Ghi bảng đối chiếu "hàm trong repo → method Java" ngay trong mã nguồn (ví dụ `docs/porting/<ten-repo>.md`).
4. **License:** không copy nguyên code khi license không cho phép; ưu tiên viết lại theo hành vi của API. Ghi nguồn tham khảo và license vào file `NOTICE`.
5. **MCP chính thức do nền tảng host** (ví dụ TikTok for Business MCP Server): không đi qua MCP, mà gọi REST API tương ứng từ Java.
6. **Khi cần MCP** (cho công cụ phát triển, hoặc để AI bên ngoài gọi tool): expose chính các hàm Java thành MCP tool bằng MCP Java SDK / Spring AI, trong cùng ứng dụng. Bot chăm sóc khách dùng tool use của Claude API trực tiếp (mục 32), nên production không cần MCP.
7. Repo và MCP vẫn được dùng **trên máy dev** để thử API, khám phá dữ liệu, đối chiếu kết quả; không deploy chúng lên server. Bản clone để đọc nằm ở thư mục `references/` của repo, chia theo kênh (`facebook/`, `zalo/`, `tiktok/`, `rui-ro-cao/`). Bản clone được commit vào repo dưới dạng file thường (bỏ `.git` và file build sẵn); cập nhật bằng `bash references/clone.sh --update`; danh sách, commit, license và "nên đọc gì" ở `references/README.md`.

**Ngoại lệ đang chờ quyết định: Zalo cá nhân (`zca-js`)**

- Doanh nghiệp quyết định có dùng **Zalo OA (Zalo doanh nghiệp)** hay không:
  - **Dùng Zalo OA:** kênh Zalo đi theo mục 54.4 bằng adapter Java; không cần `zca-js`.
  - **Không dùng Zalo OA:** xem xét cho phép **ngoại lệ duy nhất** của quy tắc này: chạy `zca-js` thành worker Node.js riêng. Lý do: port sang Java tốn công và phải bảo trì liên tục, vì thư viện giả lập giao thức Zalo Web (đăng nhập bằng QR, mã hóa, websocket) và thay đổi theo Zalo.
- Nếu cho phép ngoại lệ, worker Node.js phải:
  - chạy tiến trình / container riêng, không chung với ứng dụng Java;
  - không chứa nghiệp vụ: chỉ nhận và gửi tin; mọi quyết định (AI, policy, duyệt) vẫn ở backend Java;
  - nói chuyện với backend qua API nội bộ có xác thực hoặc qua queue;
  - tuân thủ đủ 8 quy tắc của tùy chọn rủi ro cao (mục 54.6).
- Cho tới khi có quyết định, Zalo cá nhân không nằm trong lộ trình (mục 38).

---

# 47. Facebook Lead Flow

```text
Facebook Page / Messenger
          │
          ▼
       Webhook
          │
          ▼
 Conversation Service (gộp tin, khóa, người phụ trách)
          │
          ▼
 Customer Identification (PSID → khach_hang)
          │
          ▼
 Orchestrator: kiến thức (cache) + catalog + media + hồ sơ tư vấn
          │
          ▼
 Claude + tools (sizing, catalog, lead, handoff)
          │
          ▼
 MediaResolver + Policy Engine
          │
          ▼
       Messenger
          │
          ▼
     Lead Qualification
          │
          ▼
       Ask Phone
          │
          ▼
       Phone received
          │
          ▼
         CRM
          │
          ▼
      Sales API
          │
          ▼
    Human Sales Rep
```

Thông tin gửi Sales API:

```text
customer_name
phone
facebook_psid
facebook_page_id
conversation_id
customer_need
product_interest
estimated_monthly_bill
recommended_solution
ai_summary
province
region
lead_level
lead_level_reason
research_level
budget
timeline
source = facebook
```

---

# 48. Facebook Content Flow

```text
Content Planner / Trợ lý nội dung (mục 26)
      │  kiến thức content/ + core/voice.md + rules/content-schedule.yaml (prompt có cache)
      │  tool đọc: media, lịch đăng, hiệu quả, catalog (mục 26.3)
      ▼
Claude
      │
      ▼
Bản nháp (nd_bai_dang + nd_ban_dang_kenh)
      │
      ▼
ScheduleRuleChecker + Policy (mục 26.5)
      │
      ▼
Admin duyệt & lên lịch
      │
      ▼
PublishScheduler → FacebookPublishingAdapter (Meta Graph API)
      │
      ▼
Facebook Page
```

---

# 49. Facebook Group Monitoring Flow

**Tùy chọn rủi ro cao, tắt mặc định (mục 54.6).** Chỉ dùng khi doanh nghiệp có nhu cầu và quyền/phương thức phù hợp:

```text
FacebookGroupsAdapter (Java, port từ facebook-go)
     │
     ▼
Group Discovery
     │
     ▼
Relevant Groups
     │
     ▼
Read Posts
     │
     ▼
AI Classification
     │
     ├── Solar question
     ├── Battery question
     ├── Competitor discussion
     ├── Customer problem
     └── Irrelevant
     │
     ▼
Kiến thức (prompt có cache, mục 5.4)
+
Văn phong comment (mục 15)
+
Policy
     │
     ▼
Người duyệt từng lần (mục 54.6, quy tắc 4)
     │
     ▼
Comment
```

Không nên thiết kế hệ thống theo hướng spam hoặc tự động tham gia/đăng hàng loạt. Group automation cần được kiểm tra quyền, chính sách Facebook và quy định từng group trước khi chạy.

---

# 50. Repository Decision Table

Cả ba repo chỉ để đọc và port, không chạy ở production (mục 46.1).

| Repository | Vai trò | Ưu tiên | Port thành |
|---|---|---:|---|
| `oliverames/meta-mcp-server` | Meta/Facebook Business | **1** | `MetaGraphFacebookAdapter` (Java) |
| `teslashibe/facebook-go` | Facebook Groups | Rủi ro cao, tắt mặc định (mục 54.6) | `FacebookGroupsAdapter` (Java), tách riêng |
| `HagaiHen/facebook-mcp-server` | Page, đối chiếu | 3 | Không port riêng; dùng để đối chiếu |

Repository URLs:

- `oliverames/meta-mcp-server`: https://github.com/oliverames/meta-mcp-server
- `teslashibe/facebook-go`: https://github.com/teslashibe/facebook-go
- `HagaiHen/facebook-mcp-server`: https://github.com/HagaiHen/facebook-mcp-server

---

# 51. Lưu ý bảo mật Facebook

Không lưu trực tiếp access token/cookie trong PostgreSQL.

Nên:

```text
Secret Manager
     │
     ├── Meta access token
     ├── Facebook credentials
     └── Group session cookies
```

Application chỉ nhận secret khi cần.

Đặc biệt với `facebook-go`, cookie/session credential có quyền rất mạnh. Không:

- Commit vào Git.
- Đưa vào database dạng plaintext.
- Đưa vào prompt Claude.
- Gửi cho frontend.
- Log ra console.
- Đưa vào error tracking.

---

# 52. Quyết định kiến trúc Facebook cuối cùng

```text
                  AI CUSTOMER PLATFORM
                           │
                           ▼
                   AI ORCHESTRATOR
                           │
                    POLICY ENGINE
                           │
               FACEBOOK ADAPTERS (Java)
                           │
              ┌────────────┴────────────┐
              │                         │
              ▼                         ▼
 MetaGraphFacebookAdapter      FacebookGroupsAdapter
 (Meta Graph API)              (tắt mặc định, mục 54.6)
              │                         │
              ▼                         ▼
       Facebook Page             Facebook Groups
       Messenger                 Group Feed
       Comments                  Posts
       Insights                  Comments
       Content                   Trends

 Tham khảo (không deploy): oliverames/meta-mcp-server · teslashibe/facebook-go
```

**Kết luận:** lấy **`oliverames/meta-mcp-server` làm nguồn tham khảo chính cho Facebook/Meta**, `teslashibe/facebook-go` làm nguồn tham khảo cho adapter Groups riêng (tùy chọn rủi ro cao), còn `HagaiHen/facebook-mcp-server` để đối chiếu. Repository không phải là "bộ não AI" và không chạy ở production; chúng chỉ là nguồn tham khảo cho các adapter Java bên dưới Policy Engine.


---

## 52.1 Quyết định công nghệ cuối cùng

**Backend chính: Java 17 + Spring Boot 3.5** (mục 30).

**Repo Facebook:** vẫn sử dụng các repo đã đề xuất làm nguồn tham khảo logic; không loại bỏ repo chỉ vì backend Java. Phần cần thiết được port thành Java adapters trong hệ thống (mục 46.1).

**Ưu tiên tham khảo:** `oliverames/meta-mcp-server` cho Meta/Facebook Business; `teslashibe/facebook-go` cho Facebook Groups/internal GraphQL (rủi ro cao); `HagaiHen/facebook-mcp-server` để đối chiếu.

**Mục tiêu:** giữ lợi thế của code/repo có sẵn nhưng hệ thống lõi, database, AI orchestration, policy, workflow và adapters thuộc quyền kiểm soát của hệ thống Java.

---

# 53. TikTok Direct Messages — chăm sóc và trả lời tin nhắn

## Mục tiêu

Bổ sung TikTok vào cùng nền tảng Customer 360 để hệ thống có thể nhận tin nhắn, đồng bộ hội thoại, dùng Claude trả lời theo Knowledge Base và văn phong chung (mục 5, 15), chuyển lead nóng cho sales và lưu toàn bộ lịch sử vào CRM.

**Lưu ý:** TikTok DM API không đồng nghĩa với API đăng video. Cần kiểm tra tài khoản, thị trường, quyền ứng dụng và điều kiện Business Messaging trước khi triển khai. Không giả định mọi tài khoản cá nhân đều có thể tự động hóa DM.

## Repo và tài liệu tham khảo

### 1. Ưu tiên: TikTok for Business Business Messaging API + MCP được TikTok tài liệu hóa

- Tài liệu chính thức: https://business-api.tiktok.com/gateway/docs/index?doc_id=1739473020978177
- Tài liệu về quản lý direct messages: https://business-api.tiktok.com/gateway/docs/index?doc_id=1751443956638721

Tài liệu TikTok for Business hiện liệt kê các công cụ Business Messaging/MCP liên quan đến:

- Liệt kê conversations.
- Lấy danh sách messages trong conversation.
- Gửi message vào conversation.
- Webhook cho sự kiện direct message.
- Upload/download media theo khả năng được cấp.
- Cấu hình automatic messages và Comment-to-Message.

Đây là hướng nên kiểm tra đầu tiên vì dùng API/quyền chính thức. Tuy nhiên, phải đăng ký ứng dụng, cấp quyền, hoàn tất OAuth và xác minh tài khoản có đủ điều kiện. API có giới hạn cửa sổ trả lời và quy tắc nhắn tin; chỉ phản hồi trong phạm vi được TikTok cho phép.

### 2. Repo OpenAPI để làm reference khi viết Java adapter

- `justomat/tiktok-business-api-docs`: https://github.com/justomat/tiktok-business-api-docs

Repo này cung cấp bản OpenAPI tham khảo cho TikTok Business API, bao gồm messaging, conversations, webhook và các API liên quan. **Đây không phải MCP server hoàn chỉnh**, nhưng hữu ích để tạo Java HTTP client/DTO hoặc sinh client từ OpenAPI. Luôn đối chiếu với tài liệu TikTok chính thức trước khi dùng vì repo bên thứ ba có thể chậm cập nhật.

### 3. Repo MCP liên quan nhưng không đáp ứng DM

- `postoncehq/tiktok-mcp`: https://github.com/postoncehq/tiktok-mcp

Repo này phục vụ tạo/lên lịch/đăng video và slideshow thông qua dịch vụ PostOnce. Theo README, nó **không hỗ trợ đọc hoặc trả lời TikTok DM**. Chỉ dùng nếu sau này cần luồng đăng nội dung, không chọn làm adapter tin nhắn.

- `HasData/tiktok-mcp`: https://github.com/HasData/tiktok-mcp

Repo này đọc dữ liệu TikTok công khai (profile, video, comments, search); README ghi rõ là read-only, không đăng bài, không đăng nhập tài khoản và không gửi DM. Không dùng cho customer-care inbox.

**Kết luận lựa chọn:** Hiện chưa xác minh được một repo GitHub mã nguồn mở có sẵn MCP chuyên biệt, đầy đủ để đọc và trả lời TikTok DM. Dùng TikTok Business Messaging API chính thức qua `TikTokMessagingAdapter` viết bằng Spring Boot, khi tài khoản đủ điều kiện (mục 46.1). MCP do TikTok công bố chỉ dùng để tham khảo; nếu cần MCP thì tự expose các hàm Java thành MCP tool.

## Kiến trúc TikTok

```text
TikTok Business Account
        │
        ▼
TikTok Business Messaging API / Webhooks
        │
        ▼
TikTok Messaging Adapter (Java / Spring Boot)
        │
        ├── OAuth + token refresh
        ├── Webhook validation / event handling
        ├── Conversation sync + pagination
        ├── Message history
        ├── Send reply + media capability checks
        ├── Rate limit + retry/backoff
        └── Messaging-window / permission checks
        │
        ▼
Conversation Service ─── Customer 360 / PostgreSQL
        │
        ▼
AI Orchestrator (mục 32)
        ├── Lead qualification (mục 58)
        ├── Kiến thức + playbook + văn phong (prompt có cache, mục 5.4)
        ├── Danh mục media (mục 7)
        └── Cùng một model với các kênh khác (mục 20.1)
        │
        ▼
Policy Engine
        ├── Auto-send only for allowed low-risk cases
        ├── Human approval for sensitive/uncertain replies
        └── Escalate hot leads / complaints / technical risks
        │
        ▼
TikTok Messaging Adapter → TikTok DM
```

## Java package structure

Theo cấu trúc chuẩn ở mục 31; phần TikTok nằm trong `adapters/tiktok/` (danh sách class đầy đủ ở mục 57.5):

```text
adapters/tiktok/
├── config
├── auth              # OAuth, lưu tham chiếu secret, refresh token
├── webhook           # sự kiện DM, lead
├── client            # HTTP client TikTok Business API
├── messaging         # đồng bộ hội thoại, gửi trả lời và media
├── dto
└── TikTokMessagingAdapter.java
```

Hội thoại, khách hàng, AI, kiến thức và policy dùng chung module trong `services/` như các kênh khác.

## Các tool nội bộ nên cung cấp

Nếu cần MCP, expose các thao tác nhỏ và có quyền kiểm soát:

- `tiktok_list_conversations`
- `tiktok_get_messages`
- `tiktok_send_reply`
- `tiktok_upload_message_media` (chỉ khi được hỗ trợ/cấp quyền)
- `tiktok_get_account_capabilities`
- `tiktok_get_comment_to_message_status`
- `tiktok_set_comment_to_message` (chỉ cho admin/được cấp quyền)

Không cấp tool gửi tin hàng loạt tùy ý cho model. `tiktok_send_reply` phải kiểm tra conversation, quyền, thời hạn phản hồi, giới hạn gửi và trạng thái opt-in/consent phù hợp trước khi gọi API.

## Luồng nhận lead TikTok

```text
TikTok DM / đủ điều kiện Comment-to-Message
        ↓
Webhook → deduplicate event
        ↓
Identify customer / merge Customer 360
        ↓
AI phân loại nhu cầu + lấy knowledge/style/video
        ↓
Soạn trả lời
        ↓
Policy Engine
   ┌────┴─────────┐
   ▼              ▼
Auto reply     Human review
   └────┬─────────┘
        ↓
Ghi hội thoại + summary + lead score
        ↓
Nếu lead nóng/đủ số điện thoại → Sales API
```

## Điều kiện và giới hạn cần xác minh trước khi phát triển

- TikTok Business Account và khu vực được hỗ trợ.
- Quyền Business Messaging được phê duyệt cho app và token.
- Quy trình OAuth, refresh token, lưu secret an toàn.
- Chính sách cửa sổ nhắn tin/giới hạn trả lời hiện hành; không gửi tin lạnh nếu API không cho phép.
- Webhook subscription và xác minh chữ ký theo tài liệu hiện hành.
- Quy tắc lưu dữ liệu cá nhân, consent và thời hạn lưu trữ.
- Không dùng cookie scraping hoặc reverse-engineering làm đường production mặc định; dễ hỏng và có rủi ro điều khoản/tài khoản.

---

# 54. Cập nhật kiến trúc đa kênh: Facebook + Zalo + TikTok

```text
                         Vben Admin (Vue 3 + TypeScript) Dashboard
                                   │
                                   ▼
              Java / Spring Boot (modular monolith + worker, mục 31)
                                   │
            ┌──────────────────────┼──────────────────────┐
            ▼                      ▼                      ▼
      Customer 360            AI Orchestrator        Job nền (worker)
            │        kiến thức · catalog · media     follow-up, đăng bài,
            │        · văn phong (mục 5.4)           đồng bộ
            └──────────────────────┼──────────────────────┘
                                   ▼
                             Policy Engine
                                   │
                         Channel + Publishing Adapter Layer
             ┌─────────────────────┼─────────────────────┐
             ▼                     ▼                     ▼
      Facebook Adapter       Zalo Adapter         TikTok Adapter
      ├─ Meta Graph API      ├─ OA + ZNS          ├─ Business Messaging API
      ├─ Page/Messenger      └─ Zalo cá nhân*     ├─ Content Posting API
      └─ Groups adapter*                           └─ Lead Generation API
             │                     │                     │
             └─────────────────────┼─────────────────────┘
                                   ▼
               PostgreSQL (dữ liệu + job) · MinIO · slm_v2
                                   │
                         Solar API + Sales API
```

`*` Facebook Groups/internal GraphQL và Zalo cá nhân (`zca-js`; nếu được duyệt thì chạy worker Node.js riêng, mục 46.1, 54.6) là lựa chọn rủi ro cao hơn API chính thức; giữ tách biệt, bật riêng theo cấu hình, không coi là nền tảng ổn định như official API; quy tắc bắt buộc ở mục 54.6. TikTok DM ưu tiên Business Messaging API chính thức, không đồng nhất với các MCP chỉ đọc dữ liệu công khai hoặc chỉ đăng video.

## Bảng phân loại repo TikTok

| Repo/tài liệu | Dùng cho | Đọc DM | Trả lời DM | Khuyến nghị |
|---|---|---:|---:|---|
| TikTok for Business docs/MCP | Business Messaging chính thức | Có theo quyền | Có theo quyền/cửa sổ tin nhắn | Ưu tiên xác minh và dùng |
| `justomat/tiktok-business-api-docs` | OpenAPI reference | Theo API mô tả | Theo API mô tả | Dùng để xây Java client, đối chiếu official docs |
| `postoncehq/tiktok-mcp` | Đăng/lên lịch video qua PostOnce | Không | Không | Không dùng cho DM |
| `HasData/tiktok-mcp` | Tìm kiếm/đọc dữ liệu công khai | Không | Không | Chỉ phục vụ research |

Nguồn tham khảo:

- TikTok for Business API/MCP docs: https://business-api.tiktok.com/gateway/docs/index?doc_id=1739473020978177
- TikTok Business Messaging docs: https://business-api.tiktok.com/gateway/docs/index?doc_id=1751443956638721
- OpenAPI reference: https://github.com/justomat/tiktok-business-api-docs
- TikTok posting MCP: https://github.com/postoncehq/tiktok-mcp
- Public-data TikTok MCP: https://github.com/HasData/tiktok-mcp

**Quyết định:** Backend lõi tiếp tục là Java 17 + Spring Boot 3.5. TikTok được thêm thành channel adapter độc lập, chia sẻ Customer 360, Conversation Service, AI Orchestrator, Knowledge Base, Policy Engine và Sales API với Facebook/Zalo. Trước khi cam kết tự động hóa DM, cần xác minh quyền truy cập thật của tài khoản/app TikTok tại thị trường Việt Nam.

## 54.1 Mã kênh dùng chung

| Mã | Loại | Dùng ở | Mức |
|---|---|---|---|
| `MESSENGER` | Nhắn tin | Hội thoại, `dinh_danh_kenh`, `media.kenh_cho_phep` | Chính thức |
| `FACEBOOK_COMMENT` | Bình luận | Trả lời comment trên Page | Chính thức |
| `FACEBOOK_PAGE` | Đăng bài | `nd_ban_dang_kenh` | Chính thức |
| `ZALO_OA` | Nhắn tin + đăng bài | Hội thoại, ZNS, bài viết OA | Chính thức |
| `TIKTOK_DM` | Nhắn tin | Hội thoại (khi có Business Messaging API) | Chính thức, phụ thuộc quyền |
| `TIKTOK_COMMENT` | Bình luận | Trả lời comment video | Chính thức, phụ thuộc quyền |
| `TIKTOK` | Đăng bài | `nd_ban_dang_kenh` | Chính thức, phụ thuộc audit |
| `TIKTOK_LEAD` | Lead từ form / quảng cáo | `khach_tiem_nang.kenh_nguon` (mục 57.2) | Chính thức, phụ thuộc quyền |
| `FACEBOOK_GROUP` | Đọc / bình luận / đăng | Group adapter | **Rủi ro cao** (mục 54.6) |
| `ZALO_PERSONAL` | Nhắn tin | Zalo cá nhân | **Rủi ro cao** (mục 54.6) |

Cách dùng mã:

- Kênh hội thoại (`hoi_thoai.kenh`, `dinh_danh_kenh.kenh`, `media.kenh_cho_phep`, `media_lan_gui.kenh`, `lich_follow_up.kenh`): `MESSENGER`, `FACEBOOK_COMMENT`, `ZALO_OA`, `TIKTOK_DM`, `TIKTOK_COMMENT`, `FACEBOOK_GROUP`, `ZALO_PERSONAL`.
- Kênh đăng bài (`nd_ban_dang_kenh.kenh`): `FACEBOOK_PAGE`, `ZALO_OA`, `TIKTOK`, `FACEBOOK_GROUP`.
- Nguồn lead (`khach_tiem_nang.kenh_nguon`): mã kênh hội thoại nơi khách để lại số, hoặc `TIKTOK_LEAD`.
- Mọi chỗ trong tài liệu, JSON và code đều dùng đúng các mã này (không dùng biến thể như `facebook_messenger`).

Mọi adapter cài đặt `getCapabilities(accountId)` và trả về capability **thực tế** của tài khoản đã kết nối. AI Orchestrator, Policy Engine, Trợ lý nội dung và giao diện admin đều đọc capability này; không gọi chức năng nào đang `false`.

## 54.2 Ma trận khả năng theo kênh

| Chức năng | Facebook | Zalo OA | TikTok |
|---|---|---|---|
| Phase | **1** (làm trước) | 3 | Sau cùng |
| Kết nối tài khoản, quyền | Meta App + App Review (mục 54.3) | OA + app Zalo for Developers (mục 54.4) | Accounts API / Content Posting API / Business Messaging API (mục 57.8) |
| Định danh khách | PSID theo Page | user_id theo OA | ID người dùng do API trả về |
| Nhận và trả lời tin nhắn | Webhook Messenger, tự do trong 24 giờ | Tin tư vấn trong cửa sổ tương tác | Chỉ Business Account đã được cấp quyền |
| Gửi chủ động | Ngoài 24 giờ chỉ tag `HUMAN_AGENT` (người thật) → tạo việc cho sales | **ZNS** theo template đã duyệt, có phí | Không giả định có; tạo việc cho nhân viên |
| Bình luận | Trả lời công khai + trả lời riêng (private reply) | — | Tùy API cho phép |
| Lead → Sales API | Số điện thoại trong chat (mục 23, 47) | Số điện thoại trong chat | Lead Generation API/webhook (mục 57.2) |
| Gửi media trong chat | Mục 6.4 | Ảnh nếu API hỗ trợ, video gửi link | Theo capability |
| Đăng và lên lịch bài | Video/ảnh lên Page, backend giữ lịch | Bài viết OA nếu được cấp; Zalo Short không giả định có API | Direct post sau audit; chưa audit → upload nháp (`MANUAL_REQUIRED`) |
| Số liệu | Insights bài đăng trong phạm vi quyền; link `m.me?ref=` để đo tin nhắn từ bài | Theo API OA | Theo API (mục 57.1) |
| Văn phong | Mục 15 (comment, Messenger) | Mục 15 (Zalo) | Mục 15 (TikTok) |

### Tham chiếu repo / MCP / tài liệu theo chức năng

Nguyên tắc: adapter Java gọi **API chính thức**; repo và MCP bên thứ ba chỉ dùng làm tham khảo (mục 46.1). Trước khi dùng repo nào phải kiểm tra license, lịch sử commit và tình trạng bảo trì.

| Kênh | Chức năng | API / tài liệu chính thức | Repo / MCP tham khảo |
|---|---|---|---|
| Facebook | Nhận và trả lời tin nhắn, echo, webhook | [Messenger Platform reference](https://developers.facebook.com/documentation/business-messaging/messenger-platform/reference), [Webhook events](https://developers.facebook.com/documentation/business-messaging/messenger-platform/webhooks/webhook-events) | [`oliverames/meta-mcp-server`](https://github.com/oliverames/meta-mcp-server) (ưu tiên, mục 42); [`HagaiHen/facebook-mcp-server`](https://github.com/HagaiHen/facebook-mcp-server) (thay thế, mục 44) |
| Facebook | Bình luận, private reply, đăng bài lên Page, insights | [Pages API: posts & comments](https://developers.facebook.com/documentation/pages-api/posts), [Webhooks for Pages](https://developers.facebook.com/documentation/pages-api/webhooks-for-pages) | `oliverames/meta-mcp-server` |
| Facebook Groups | Đọc / bình luận / đăng trong group (**rủi ro cao**) | Không có (Meta đã ngừng Groups API) | [`teslashibe/facebook-go`](https://github.com/teslashibe/facebook-go) (mục 43, 54.6) |
| Zalo OA | Tin tư vấn, quản lý người quan tâm, bài viết OA | [Zalo for Developers](https://developers.zalo.me/docs): mục Official Account API | [`zaloplatform/zalo-java-sdk`](https://github.com/zaloplatform/zalo-java-sdk) (chính thức nhưng cũ); [`@warriorteam/redai-zalo-sdk`](https://unpkg.com/@warriorteam/redai-zalo-sdk@1.39.2/README.md) (cộng đồng, OA API v3) |
| Zalo OA | ZNS (báo cáo, nhắc lịch) | [Zalo for Developers](https://developers.zalo.me/docs): mục Zalo Notification Service | Repo demo E2EE ZNS (C#) trong [tổ chức zaloplatform](https://github.com/zaloplatform) |
| Zalo cá nhân | Nhắn tin bằng tài khoản cá nhân (**rủi ro cao**) | Không có API chính thức | [`RFS-ADRENO/zca-js`](https://github.com/RFS-ADRENO/zca-js) (mục 54.6; chờ quyết định, mục 46.1) |
| TikTok | Tin nhắn trực tiếp (DM) | [Business Messaging API](https://business-api.tiktok.com/portal/docs/business-messaging-api-get-started/v1.3) | [`justomat/tiktok-business-api-docs`](https://github.com/justomat/tiktok-business-api-docs) (OpenAPI để sinh client Java, mục 53) |
| TikTok | Đăng video | [Content Posting API](https://developers.tiktok.com/docs/en/content-posting-api-get-started) | [`postoncehq/tiktok-mcp`](https://github.com/postoncehq/tiktok-mcp) (chỉ tham khảo luồng đăng, mục 55.5) |
| TikTok | Tài khoản, bình luận, insights | [Accounts API](https://business-api.tiktok.com/portal/docs/accounts-api-overview/v1.3) | — |
| TikTok | Quảng cáo, lead | TikTok API for Business, Lead Generation API (gọi từ Java) | [TikTok for Business MCP Server](https://ads-useast2a.tiktok.com/resources/help/article/about-tiktok-for-business-mcp-server?lang=en) (tham khảo phạm vi tool) |
| TikTok | Nghiên cứu nội dung công khai | — | [`HasData/tiktok-mcp`](https://github.com/HasData/tiktok-mcp) (chỉ đọc, không gửi DM, mục 57.7) |

Không tìm thấy MCP chính thức cho Zalo OA (kiểm tra ngày 2026-10-20). Nếu cần, tự expose các hàm của Zalo adapter thành MCP tool như cách mục 53 đề xuất cho TikTok.

## 54.3 Facebook (Page + Messenger) — Phase 1

**Tài khoản và quyền**

- Meta App gắn với Business Manager đã xác minh doanh nghiệp; Page được kết nối bằng page token lưu trong secret manager (mục 51).
- Quyền dự kiến cần xin trong App Review (đối chiếu lại tài liệu Meta khi nộp): `pages_show_list`, `pages_manage_metadata` (đăng ký webhook), `pages_messaging`, `pages_read_engagement`, `pages_manage_engagement` (trả lời comment), `pages_manage_posts` (đăng bài, cho mục 26 và 55).
- Ở chế độ dev, app chạy được với tài khoản có vai trò trong app, nên vẫn code và test được trong lúc chờ duyệt.

**Định danh**

- Messenger chỉ trả **PSID** (ID theo từng Page), không trả link profile. Lưu vào `dinh_danh_kenh` (`kenh = MESSENGER`, `tai_khoan_id = page_id`).
- Số điện thoại chỉ có khi khách tự cung cấp trong chat.

**Webhook**

- Các sự kiện cần đăng ký: tin nhắn, postback, `message_echoes`, referral (link `m.me?ref=...`), comment mới trên Page (`feed`).
- Xác thực chữ ký `X-Hub-Signature-256`, trả 200 ngay, lưu sự kiện thô rồi xử lý bất đồng bộ, chống trùng theo message ID.
- `message_echoes` từ Page Inbox nghĩa là nhân viên đang trả lời: chuyển hội thoại sang `HUMAN_OWNED`, AI dừng cho đến khi được trả lại (luật trả lại ở mục 19). Echo cũng được gửi cho tin do chính ứng dụng gửi qua Send API: so `app_id` của echo với app của mình và bỏ qua những echo đó, nếu không bot sẽ tự chuyển hội thoại của mình sang người.

**Quy tắc nhắn tin**

- Trong 24 giờ kể từ tin cuối của khách: nhắn tự do.
- Ngoài 24 giờ: chỉ được dùng tag `HUMAN_AGENT` (trong 7 ngày, dành cho người thật trả lời). AI không tự gửi ngoài cửa sổ; Lead Nurturing (mục 56) chuyển thành việc gọi điện cho sales.

**Bình luận**

- AI trả lời công khai ngắn theo văn phong comment (mục 15), không hỏi thông tin nhạy cảm công khai.
- Có thể gửi **một** tin nhắn riêng (private reply) cho người comment trong vòng 7 ngày để kéo vào Messenger.

**Đăng bài**

- Video và ảnh lên Page qua Graph API. Backend tự giữ lịch (`PublishScheduler`) để thống nhất cách làm cho cả 3 kênh và hủy/đổi giờ được.

**Tài liệu chính thức**

- [Messenger Platform reference](https://developers.facebook.com/documentation/business-messaging/messenger-platform/reference): Send API, Attachment Upload API, Messenger Profile API.
- [Webhook events](https://developers.facebook.com/documentation/business-messaging/messenger-platform/webhooks/webhook-events), [message_echoes](https://developers.facebook.com/documentation/business-messaging/messenger-platform/webhooks/webhook-events/message-echoes); webhook phải chạy HTTPS với chứng chỉ hợp lệ, không dùng chứng chỉ tự ký.
- [Quick start](https://developers.facebook.com/documentation/business-messaging/messenger-platform/getting-started/quick-start).
- [Pages API overview](https://developers.facebook.com/documentation/pages-api/overview), [posts & comments](https://developers.facebook.com/documentation/pages-api/posts), [Webhooks for Pages](https://developers.facebook.com/documentation/pages-api/webhooks-for-pages).

Meta đang chuyển tài liệu từ đường dẫn `/docs/...` sang `/documentation/...`; khi link cũ báo 404 thì tìm theo đường dẫn mới.

## 54.4 Zalo OA — Phase 3

**Tài khoản và quyền**

- OA đã xác thực, app trên Zalo for Developers, OAuth access token/refresh token của OA lưu trong secret manager.

**Định danh**

- `user_id` là định danh **theo từng OA**, không phải số điện thoại. Chỉ có khi khách nhắn tin hoặc quan tâm OA.
- Liên kết với khách trong Customer 360 khi khách cung cấp số điện thoại, hoặc qua quyền lấy số điện thoại nếu Zalo cấp (cần xác minh).
- Cần biết trong khoảng 3.000 khách hiện có bao nhiêu người đã quan tâm OA trước khi thiết kế chăm sóc khách cũ qua Zalo.

**Các loại tin**

| Loại | Nội dung | Khi nào | Dùng cho |
|---|---|---|---|
| Tin tư vấn | Tự do, AI soạn | Chỉ trong cửa sổ tương tác sau tin của khách, theo chính sách OA hiện hành | Chat tư vấn |
| ZNS | Template được duyệt trước, chỉ thay tham số, tính phí theo tin, gửi theo số điện thoại | Bất kỳ lúc nào, đúng loại template | Báo cáo sản lượng, nhắc bảo trì, xác nhận lịch khảo sát |
| Tin truyền thông | Theo gói và quy định OA | Chiến dịch đã duyệt | Thông báo chương trình |

- AI **không** viết nội dung ZNS tự do. Báo cáo sản lượng (mục 22) gửi bằng ZNS gồm số liệu chính + link tới trang báo cáo web, nơi đặt phần diễn giải Claude viết.
- Webhook: tin nhắn của người dùng, quan tâm / bỏ quan tâm OA; xác thực theo tài liệu Zalo.

**Đăng bài**

- Bài viết OA nếu tài khoản được cấp quyền. Video ngắn trên Zalo (Zalo Short) không giả định có API: đánh dấu `MANUAL_REQUIRED`, cho tải file và caption để đăng tay.

**Cần xác minh trước khi code:** số khách đã quan tâm OA, chính sách cửa sổ tin tư vấn và giá ZNS hiện hành, quyền lấy số điện thoại, quyền đăng bài viết.

**Repo và tài liệu**

- Tài liệu chính thức: [Zalo for Developers](https://developers.zalo.me/docs), phần Official Account API và Zalo Notification Service (ZNS).
- [`zaloplatform/zalo-java-sdk`](https://github.com/zaloplatform/zalo-java-sdk): SDK Java **chính thức**, gồm nhắn tin OA, quản lý người quan tâm, bài viết, Social API, Login; README không nhắc ZNS. Repo ít commit, không có release, nhiều khả năng đã cũ, nên dùng làm tham khảo; adapter viết theo tài liệu OA API hiện hành.
- Các SDK chính thức khác trong [tổ chức zaloplatform](https://github.com/zaloplatform): PHP, .NET, Android, và demo mã hóa E2EE cho ZNS (C#).
- [`@warriorteam/redai-zalo-sdk`](https://unpkg.com/@warriorteam/redai-zalo-sdk@1.39.2/README.md): SDK TypeScript của cộng đồng, ghi là hỗ trợ OA API v3.0; dùng để đối chiếu endpoint, **không phải bản chính thức**.
- Chưa có MCP chính thức cho Zalo OA.

## 54.5 TikTok — sau cùng

Chi tiết ở mục 53, 55 và 57. Tóm tắt:

- Tài khoản công ty đang là **Personal Account**: đăng video qua Content Posting API sau khi app qua audit; chưa audit thì chỉ upload nháp → `MANUAL_REQUIRED`.
- DM tự động chỉ khi chuyển sang **Business Account** và được cấp Business Messaging API; trước đó nhân viên trả lời trong app TikTok.
- Lead từ quảng cáo/form qua Lead Generation API hoặc webhook, chống trùng theo số điện thoại.
- Định danh: ID người dùng do API trả về theo tài khoản business, lưu ở `dinh_danh_kenh` (`kenh = TIKTOK_DM`).

## 54.6 Tùy chọn rủi ro cao: Facebook Groups và Zalo cá nhân

Hai kênh này **được giữ trong kế hoạch dưới dạng tùy chọn**, không thuộc lộ trình MVP và không làm nền cho chức năng cốt lõi.

**Vì sao rủi ro cao**

- Không đi qua API chính thức: Meta đã ngừng Groups API từ năm 2024, nên `facebook-go` dùng cookie/session và GraphQL nội bộ (mục 43); Zalo cá nhân không có API tự động hóa chính thức.
- Có thể trái điều khoản nền tảng, dẫn tới khóa tài khoản; dễ hỏng mỗi khi nền tảng thay đổi.

**Quy tắc bắt buộc khi bật**

1. Tắt mặc định. Bật bằng cờ cấu hình riêng cho từng tính năng; chỉ người có quyền cao nhất được bật.
2. Adapter và worker riêng, không dùng chung token/session với kênh chính thức; lỗi ở đây không được ảnh hưởng kênh chính.
3. Dùng tài khoản riêng, không dùng tài khoản quản trị Page/Business Manager hay tài khoản cá nhân chính của nhân viên.
4. Mọi hành động gửi ra ngoài (bình luận, đăng bài, nhắn tin) đều cần người duyệt **từng lần**; AI chỉ soạn nháp. Không tự tham gia nhóm hàng loạt, không nhắn hàng loạt.
5. Giới hạn tần suất chặt; có nút dừng khẩn cấp; tự tắt khi gặp checkpoint, captcha hoặc dấu hiệu bị hạn chế.
6. Cookie/session lưu trong secret manager (mục 51); không đưa vào prompt, log hay frontend.
7. Admin hiển thị nhãn "Rủi ro cao" ở mọi màn hình liên quan; audit log đầy đủ.
8. Trợ lý nội dung chỉ thấy các kênh này khi cờ đang bật (mục 26.5).

**Repo cho hai tùy chọn này**

| Tùy chọn | Repo | Cách chạy |
|---|---|---|
| Facebook Groups | [`teslashibe/facebook-go`](https://github.com/teslashibe/facebook-go) (Go) | Port phần cần dùng sang `FacebookGroupsAdapter` Java (mục 43, 46.1) |
| Zalo cá nhân | [`RFS-ADRENO/zca-js`](https://github.com/RFS-ADRENO/zca-js) (Node.js, MIT, không chính thức, giả lập Zalo Web) | **Chờ quyết định** (mục 46.1): dùng Zalo OA thì không cần; không dùng Zalo OA thì xem xét ngoại lệ chạy worker Node.js riêng, không chứa nghiệp vụ. README của chính dự án cảnh báo có thể bị khóa tài khoản |

Cả hai đều tuân theo 8 quy tắc ở trên; lỗi hay bị chặn ở adapter / worker của các tùy chọn này không được ảnh hưởng tới kênh chính thức.


# 55. Mở rộng: đăng video trực tiếp từ Media Library lên các kênh

Yêu cầu này là một chức năng riêng của hệ thống, không chỉ là AI gửi link YouTube. Admin có thể tải video lên kho nội bộ, quản lý metadata, lên lịch, duyệt nội dung và yêu cầu hệ thống xuất bản video lên những nền tảng được API/đối tác cho phép. Cùng một video có thể có nhiều trạng thái xuất bản độc lập theo từng kênh.

## 55.1 Luồng tổng thể

```text
Admin / Content Planner / Campaign Scheduler
                    │
                    ▼
             Media Library UI
                    │
       ┌────────────┴────────────┐
       ▼                         ▼
 Upload video file          Import video URL
       │                         │
       └────────────┬────────────┘
                    ▼
       MinIO/S3 + Media DB (mục 29)
                    │
          Validate / Transcode
          (FFmpeg worker, optional)
                    │
                    ▼
         Draft → Review → Approved
                    │
                    ▼
            Publish Job Queue
                    │
          Policy + Channel Capability Check
                    │
       ┌────────────┼───────────────┬──────────────┐
       ▼            ▼               ▼              ▼
 Facebook       TikTok          Zalo OA        Other channels
 Adapter        Adapter         Adapter        (future)
       │            │               │
       ▼            ▼               ▼
 Meta API    TikTok Content    Zalo OA Article API
             Posting API      (video article, if allowed)
                    │
                    ▼
         Publish status + post URL/ID
                    │
                    ▼
       Analytics / Leads / Sales attribution
```

**Phân biệt hai nhóm TikTok:** `TikTokMessagingAdapter` xử lý DM; `TikTokPublishingAdapter` đăng video. Không giả định MCP phục vụ DM cũng có chức năng đăng video.

## 55.2 Nguồn media dùng để xuất bản

Xuất bản dùng chung Media Library (mục 6 và bảng ở mục 29), không có bảng video riêng.

- File để upload lên nền tảng đích là `media_nguon` loại `UPLOAD`: `duong_dan_luu_tru` trên MinIO/S3, `loai_mime`, `dung_luong_byte`, `thoi_luong_giay`, `chieu_rong`, `chieu_cao`, `ma_bam_sha256`, `trang_thai_xu_ly`. Không lưu file binary trong PostgreSQL.
- Chỉ xuất bản khi `media.trang_thai = APPROVED` và `quyen_su_dung` là `OWNED` hoặc `LICENSED`.
- Nguồn `YOUTUBE`, `FACEBOOK`, `TIKTOK` là link tham chiếu, không phải file nguồn hợp lệ để upload lại. Không tự động tải/rip video từ các nền tảng này để đăng chéo.
- Nếu công ty có file gốc của một video đã đăng YouTube hoặc Facebook, thêm nguồn `UPLOAD` vào cùng `media`. AI vẫn quản lý một mục duy nhất, còn bộ xuất bản dùng nguồn `UPLOAD`.
- Bản chuyển mã (transcode) cho từng nền tảng được lưu thành một nguồn `UPLOAD` bổ sung của cùng mục.
- Ảnh xuất bản theo cùng cơ chế (ví dụ bài đăng ảnh trên Page).
- Sau khi đăng thành công trên Page, có thể thêm nguồn `FACEBOOK` (ID bài/video vừa đăng) vào cùng mục để AI gửi link video Page cho khách trên Messenger.

## 55.3 Theo dõi trạng thái xuất bản riêng cho từng kênh

Tạo bảng `nd_ban_dang_kenh` (DDL ở mục 29, phần Content):

| Trường | Ý nghĩa |
|---|---|
| `id` | ID bản xuất bản |
| `bai_dang_id` | Bài đăng gốc (`nd_bai_dang`, mục 26) |
| `media_id` | Mục media trong thư viện |
| `media_nguon_id` | Nguồn `UPLOAD` đã dùng để đăng |
| `kenh` | `FACEBOOK_PAGE`, `TIKTOK`, `ZALO_OA`; tùy chọn rủi ro cao: `FACEBOOK_GROUP` (mã kênh: mục 54.1) |
| `tai_khoan_id` | Trang/tài khoản đích đã kết nối |
| `caption` | Caption đã duyệt cho kênh đó |
| `hashtags` | Hashtag nếu nền tảng hỗ trợ |
| `hen_dang_luc` | Thời điểm dự kiến đăng |
| `trang_thai` | `DRAFT`, `PENDING_APPROVAL`, `SCHEDULED`, `UPLOADING`, `PUBLISHING`, `PUBLISHED`, `FAILED`, `CANCELLED`, `MANUAL_REQUIRED` (kênh chỉ hỗ trợ đăng tay) |
| `id_bai_dang_ngoai` | ID bài đăng phía nền tảng |
| `url_bai_dang_ngoai` | URL bài đăng nếu có |
| `ma_loi`, `thong_bao_loi` | Lỗi đã được làm sạch thông tin nhạy cảm |
| `so_lan_thu_lai` | Số lần thử lại |
| `dang_luc` | Thời gian đăng thành công |
| `tao_luc`, `sua_luc` | Thời gian |

Nên có idempotency key để worker retry không vô tình tạo bài đăng trùng. Lưu log audit cho ai tạo, ai duyệt, ai hủy và nội dung nào đã gửi đi.

## 55.4 Java/Spring Boot modules cần bổ sung

```text
services/content/
  ContentPlannerService.java
  ContentApprovalService.java
  PublishScheduler.java
  PublishJobService.java

services/media/
  MediaLibraryService.java
  MediaUploadService.java            # presigned URL, complete, chống trùng sha256
  MediaValidationService.java
  MediaLinkImportService.java        # nhận diện URL YouTube/Facebook/TikTok, oEmbed
  MediaAvailabilityChecker.java      # job kiểm tra link chết
  MediaTranscodingService.java       # tùy chọn, dùng FFmpeg worker

adapters/
  facebook/FacebookPublishingAdapter.java
  tiktok/TikTokPublishingAdapter.java
  zalo/ZaloOaPublishingAdapter.java

apps/worker/
  MediaProcessingWorker.java
  ContentPublishingWorker.java
```

Đường dẫn theo cấu trúc chuẩn ở mục 31.

Định nghĩa interface nội bộ, không để logic AI phụ thuộc vào API riêng của từng mạng xã hội:

```java
public interface VideoPublishingAdapter {
    PublishCapability getCapabilities(String accountId);
    PublishResult publish(PublishVideoCommand command);
    PublishStatus getStatus(String externalPostId);
}
```

Mỗi adapter phải khai báo capability thực tế: upload video, đăng ngay, lên lịch, lấy trạng thái, lấy URL bài đăng, giới hạn file/độ dài/định dạng. Không gọi chức năng nếu API/tài khoản không hỗ trợ.

## 55.5 Repo và API nên dùng

- **TikTok:** đăng bằng `TikTokPublishingAdapter` (Java) gọi Content Posting API chính thức (mục 46.1); kiểm tra app review, OAuth, quyền đăng video, loại tài khoản và giới hạn hiện hành. `postoncehq/tiktok-mcp` chỉ để tham khảo luồng đăng / lên lịch, không đăng qua dịch vụ PostOnce. Không nhầm với Business Messaging API dành cho DM.
- **Facebook:** ưu tiên Meta Graph API/Pages API chính thức cho đăng video lên Page; quyền ứng dụng và loại đích đăng quyết định khả năng thực hiện.
- **Zalo OA:** adapter chỉ triển khai chức năng video/article mà API OA và tài khoản thực tế được cấp quyền hỗ trợ. Không khẳng định đây là API đăng Zalo Short; đăng video OA và đăng video ngắn trên bề mặt Zalo Short là hai khả năng khác nhau.
- **MCP:** nếu cần, expose các hàm nội bộ như `video_library_search`, `video_publish_draft`, `video_schedule_publish`, `video_get_publish_status` thành MCP tool ngay trong ứng dụng Java (MCP Java SDK, mục 46.1). Phần xử lý file, phân quyền, duyệt, retry và audit vẫn do backend Java quản lý.

Repo tham khảo đã có trong tài liệu:
- TikTok posting (chỉ tham khảo luồng): https://github.com/postoncehq/tiktok-mcp
- TikTok Business API OpenAPI reference: https://github.com/justomat/tiktok-business-api-docs
- Meta Business MCP reference: https://github.com/oliverames/meta-mcp-server
- Facebook Groups reference (không phải adapter đăng Page chính thức): https://github.com/teslashibe/facebook-go

## 55.6 Quy tắc an toàn khi tự động đăng

1. Mặc định `DRAFT → HUMAN_APPROVAL → SCHEDULED → PUBLISHED`; chỉ bật auto-publish cho chiến dịch đã được duyệt trước.
2. Kiểm tra quyền sử dụng video, nhạc, hình ảnh, người xuất hiện và quyền tái đăng trên từng nền tảng.
3. Tạo caption/hashtag riêng theo kênh; không sao chép nguyên văn khi nền tảng có quy định hoặc giới hạn khác nhau.
4. Kiểm tra codec, kích thước, thời lượng, tỷ lệ khung hình và dung lượng trước upload; chỉ transcode khi cần.
5. Dùng queue để retry có giới hạn, exponential backoff và idempotency; không retry vô hạn.
6. Token/access key được lưu trong secret manager hoặc cơ chế mã hóa phù hợp, không đưa vào prompt/log.
7. Ghi nhận `id_bai_dang_ngoai`, URL, trạng thái lỗi, lượt xem/tương tác nếu API cho phép, và liên kết hiệu quả nội dung với lead/sale.
8. Nếu API chính thức không hỗ trợ một bề mặt cụ thể (ví dụ Zalo Short), UI phải báo “chưa hỗ trợ đăng tự động” và cho phép xuất file/caption để đăng thủ công, thay vì giả vờ đăng thành công.

**Kết luận cập nhật:** hệ thống hỗ trợ cả chăm sóc hội thoại và xuất bản nội dung. `Media Library` là nguồn tài sản trung tâm; `Content Planner` chọn/soạn nội dung; `Content Publishing Service` điều phối lịch, duyệt và trạng thái; adapter từng nền tảng thực hiện upload/publish theo đúng API và quyền tài khoản. TikTok DM và TikTok video publishing là hai adapter/chức năng riêng.

---

# 56. AI Lead Nurturing: tự động theo dõi khách tiềm năng chưa phản hồi

Đây là module riêng để xử lý tình huống khách có nhu cầu nhưng ngừng trả lời, có thể quên phản hồi hoặc đang cân nhắc. Mục tiêu là tiếp tục hỗ trợ đúng nhu cầu và đúng thời điểm, không gửi tin dồn dập hay biến mọi lead thành chiến dịch spam.

## 56.1 Mục tiêu và nguyên tắc

- AI phân tích ngữ cảnh hội thoại, giai đoạn mua hàng, mức độ quan tâm, câu hỏi còn bỏ ngỏ, phản hồi trước đây và lịch sử tương tác để **đề xuất** thời điểm follow-up, mục tiêu và cách tiếp cận.
- Chiến lược bán hàng, playbook, ví dụ tin nhắn tốt/xấu, văn phong công ty và quy tắc xử lý từ chối là tài liệu trong Knowledge Base (`playbooks/`, `examples/`, `core/voice.md`; mục 5, 58.7), có phiên bản và bản phát hành.
- Planner dùng cùng khối kiến thức trong prompt có cache như bot chăm sóc khách (mục 5.4), không dùng RAG. Khi kiến thức vượt ngân sách token thì chuyển sang tra cứu như mục 5.7.
- AI đề xuất kế hoạch; **Follow-up Policy Engine + Scheduler** kiểm tra điều kiện, lưu lịch và thực thi. Không để LLM tự tạo timer tùy ý hoặc tự gửi tin ngoài cơ chế kiểm soát của backend.
- Tin nhắn có thể kèm ảnh hoặc video trong Media Library (file upload hoặc link YouTube/Facebook/TikTok) nếu liên quan đến sản phẩm khách đang tìm hiểu và kênh cho phép gửi nội dung đó.
- Tôn trọng opt-out/đề nghị ngừng liên hệ, chính sách kênh, consent và khung thời gian nhắn tin. Nếu kênh không cho phép gửi chủ động hoặc đã hết messaging window, không được tìm cách lách; chuyển sang tác vụ hợp lệ như nhắc nhân viên hoặc chờ khách chủ động liên hệ.

## 56.2 Luồng xử lý

```text
Tin nhắn mới / hội thoại được cập nhật
                 │
                 ▼
        Conversation Service
                 │
                 ▼
 Customer 360 + Intent + Lead Scoring
                 │
                 ▼
 Kiểm tra trạng thái hội thoại và phản hồi gần nhất
                 │
        ┌────────┴─────────┐
        ▼                  ▼
 Khách đã phản hồi     Khách im lặng và còn tiềm năng
        │                  │
 Hủy lịch đang chờ       ▼
 Cập nhật nhu cầu   Lead Nurturing Planner
                           │
      Kiến thức + playbook + văn phong (cache)
      + danh mục media + hồ sơ tư vấn
                           │
                           ▼
       Đề xuất: thời điểm / mục tiêu / kênh /
       nội dung / media / lý do / độ tin cậy
                           │
                           ▼
               Follow-up Policy Engine
                           │
             ┌─────────────┴──────────────┐
             ▼                            ▼
      Đủ điều kiện                  Không đủ điều kiện
             │                            │
     Persist follow-up job        Hủy / chờ / nhắc nhân viên
             │
             ▼
     Follow-up Scheduler + Queue
             │
             ▼
     Kiểm tra lại trước khi gửi:
     có phản hồi mới? opt-out? consent?
     cửa sổ nhắn tin? giới hạn tần suất?
             │
       ┌─────┴─────┐
       ▼           ▼
     Gửi được    Không gửi
       │           │
       ▼           ▼
 Channel Adapter  Hủy / hoãn / human task
       │
       ▼
 Ghi kết quả, tương tác, phản hồi, lead/sale
       │
       ▼
 Điều chỉnh kế hoạch follow-up tiếp theo
```

## 56.3 AI tự quyết định sau bao lâu?

AI không nên được cấu hình một khoảng thời gian cố định cho mọi khách. Planner có thể đề xuất `next_action_at` dựa trên:

- Mức tiềm năng của khách (mục 58.5) và lý do; khách đang hỏi giá/đề nghị báo giá khác với khách chỉ xem nội dung chung.
- Ý định mua, sản phẩm quan tâm (On-Grid, Hybrid, pin lưu trữ, công suất), mức độ cấp bách và câu hỏi đang chờ trả lời.
- Nội dung tin nhắn gần nhất, thời điểm khách thường tương tác nếu có đủ dữ liệu hợp lệ, và số lần follow-up đã gửi.
- Kết quả trước đây: khách có mở/click/trả lời video hay tin nhắn tương tự không; chiến dịch nào tạo lead hoặc đơn hàng thực tế.
- Playbook trong Knowledge Base (mục 5, 58): mục tiêu từng bước, cách tháo gỡ băn khoăn, cách gửi case study/video hữu ích, cách kết thúc lịch sự khi khách chưa sẵn sàng.
- Thời gian im lặng, ngày/giờ gửi hợp lý, múi giờ, chính sách kênh, consent và giới hạn tần suất.

AI trả về một kế hoạch có cấu trúc, ví dụ:

```json
{
  "decision": "SCHEDULE_FOLLOW_UP",
  "reason": "Khách đang cân nhắc pin lưu trữ và chưa trả lời câu hỏi so sánh chi phí",
  "next_action_at": "2026-10-20T10:00:00+07:00",
  "objective": "Giúp khách hiểu lợi ích sử dụng điện buổi tối",
  "channel": "MESSENGER",
  "content_type": "VIDEO_WITH_SOFT_MESSAGE",
  "media": { "id": "M07", "reason": "khách quan tâm pin lưu trữ, dùng điện chủ yếu buổi tối" },
  "message_brief": "Nhắc lại mối quan tâm trước đó, chia sẻ video hữu ích, không gây áp lực mua hàng",
  "confidence": 0.86,
  "requires_human_approval": false
}
```

Đây là ví dụ về schema, không phải thời gian mặc định cho mọi khách. `channel` dùng mã kênh ở mục 54.1; `media.id` là mã trong danh mục media và vẫn đi qua `MediaResolver` trước khi gửi (mục 7.1). Backend phải xác thực JSON, timezone, thời điểm tối thiểu/tối đa, confidence và policy trước khi tạo lịch. Nếu dữ liệu thiếu hoặc độ tin cậy thấp, hỏi nhân viên hoặc dùng lịch đã được doanh nghiệp phê duyệt; không bịa thông tin về khách.

## 56.4 Ví dụ chiến lược follow-up

Các khoảng thời gian dưới đây chỉ là **ví dụ để cấu hình và thử nghiệm**, không phải lịch cứng:

1. **Khách hỏi chi tiết/báo giá rồi im lặng:** đợi một khoảng thời gian phù hợp do planner đề xuất; tiếp cận lại bằng một câu hỏi mở hoặc làm rõ đúng điểm khách còn băn khoăn.
2. **Khách quan tâm pin lưu trữ:** có thể gửi video minh họa lợi ích sử dụng điện vào buổi tối, nếu video đúng sản phẩm, có quyền sử dụng và gửi được trên kênh đó.
3. **Khách so sánh giá hoặc hoàn vốn:** gửi bảng giải thích/case study đã được xác thực; không tự tạo cam kết tiết kiệm, ROI, thời gian hoàn vốn hoặc bảo hành chưa có nguồn.
4. **Khách chưa sẵn sàng:** chuyển sang lịch chăm sóc nhẹ hơn hoặc chờ chiến dịch đã được duyệt, thay vì liên tục nhắc chốt đơn.
5. **Khách đã trả lời:** hủy follow-up đang chờ, cập nhật hội thoại và để AI xử lý ngữ cảnh mới.
6. **Khách từ chối, yêu cầu ngừng liên hệ, hoặc lead đã chuyển sang nhân viên phụ trách:** dừng/hủy tin tự động theo chính sách và trạng thái CRM; tránh AI và nhân viên cùng gửi tin trùng nhau.

Nội dung nhắc khéo nên mang lại giá trị thực cho khách và liên quan trực tiếp tới nhu cầu họ đã thể hiện. Không giả vờ rằng nhân viên vừa xem hoặc nhớ một sự kiện không có trong lịch sử; không dùng áp lực giả, khan hiếm giả hay thông tin cá nhân nhạy cảm để thao túng quyết định.

## 56.5 Cấu trúc Java/Spring Boot

```text
services/
  leadnurturing/
    LeadNurturingService.java
    LeadNurturingPlanner.java
    FollowUpDecision.java
    FollowUpJobService.java
    FollowUpScheduler.java
    FollowUpPolicyEngine.java
    FollowUpSuppressionService.java
    FollowUpOutcomeService.java
    HumanTaskService.java          # tạo việc cho sales khi không được gửi tự động
  lead/
    LeadScoringService.java        # tính mức theo rules/lead-scoring.yaml (mục 58.5)
```

Không có service RAG hay gợi ý media riêng: planner dùng khối kiến thức có cache (mục 5.4) và danh mục media (mục 7); gửi đi dùng chung `MediaResolver` + Policy Engine + channel adapter (mục 7.1, 33); job lưu trong PostgreSQL (mục 30).

Trách nhiệm:

- `LeadNurturingPlanner`: gọi Claude với ngữ cảnh tối thiểu cần thiết; yêu cầu kết quả JSON theo schema; tạo đề xuất chứ không trực tiếp gửi tin.
- `FollowUpPolicyEngine`: kiểm tra kênh, consent/opt-out, giới hạn số lần, thời gian yên lặng, lead ownership, nội dung được phép và yêu cầu duyệt.
- `FollowUpJobService`: lưu job bền vững trong PostgreSQL, idempotency key, trạng thái và lịch sử quyết định.
- `FollowUpScheduler`: tìm job đến hạn và chạy trên worker, dùng cơ chế job lưu trong PostgreSQL chung của hệ thống (Quartz JDBC hoặc JobRunr, mục 30). Không giữ lịch trong RAM.
- `FollowUpSuppressionService`: hủy hoặc bỏ qua job khi có tin nhắn mới, khách opt-out, lead đã chuyển đổi, nhân viên đang xử lý hoặc trạng thái kênh không còn hợp lệ.
- `FollowUpOutcomeService`: ghi nhận đã gửi, lỗi, phản hồi, click (nếu nền tảng cung cấp), lead tạo mới và sale; dữ liệu này dùng để cải thiện playbook.
- Channel adapters: chịu trách nhiệm gửi tin qua API/kênh hợp lệ. Mỗi adapter phải báo capability thực tế; không giả định mọi kênh đều cho phép gửi media hoặc chủ động nhắn sau thời gian dài.

## 56.6 Database đề xuất

```sql
-- EN: lead_follow_up_jobs — scheduled lead follow-up jobs
CREATE TABLE lich_follow_up (
    id                      UUID PRIMARY KEY,
    khach_hang_id           UUID NOT NULL REFERENCES khach_hang(id),  -- [EN: customer_id]
    hoi_thoai_id            UUID NOT NULL REFERENCES hoi_thoai(id),   -- [EN: conversation_id]
    kenh                    VARCHAR(20) NOT NULL,                -- [EN: channel] mã kênh hội thoại, mục 54.1
    trang_thai              VARCHAR(32) NOT NULL,                -- [EN: status]
    -- PLANNED, SCHEDULED, PROCESSING, SENT, CANCELLED,
    -- SUPPRESSED, FAILED, NEEDS_HUMAN_REVIEW
    den_han_luc             TIMESTAMPTZ NOT NULL,                -- [EN: due_at]
    muc_tieu                TEXT,                                -- [EN: objective]
    tom_tat_tin_nhan        TEXT,                                -- [EN: message_brief]
    loai_noi_dung           VARCHAR(40),                         -- [EN: content_type]
    media_chon_id           UUID REFERENCES media(id),           -- [EN: selected_media_id]
    tai_lieu_chien_luoc_ids JSONB,                               -- [EN: strategy_document_ids]
    quyet_dinh_json         JSONB,                               -- [EN: decision_json]
    ly_do_quyet_dinh        TEXT,                                -- [EN: decision_reason]
    do_tin_cay              NUMERIC(5,4),                        -- [EN: confidence]
    so_lan_thu              INT NOT NULL DEFAULT 0,              -- [EN: attempt_count]
    khoa_chong_trung        VARCHAR(160) NOT NULL UNIQUE,        -- [EN: idempotency_key]
    kiem_tra_cuoi_luc       TIMESTAMPTZ,                         -- [EN: last_checked_at]
    gui_luc                 TIMESTAMPTZ,                         -- [EN: sent_at]
    huy_luc                 TIMESTAMPTZ,                         -- [EN: cancelled_at]
    ly_do_huy               TEXT,                                -- [EN: cancel_reason]
    tao_luc                 TIMESTAMPTZ NOT NULL DEFAULT NOW(),  -- [EN: created_at]
    sua_luc                 TIMESTAMPTZ NOT NULL DEFAULT NOW()   -- [EN: updated_at]
);

CREATE INDEX idx_lich_follow_up_den_han
ON lich_follow_up (trang_thai, den_han_luc);

-- EN: lead_follow_up_events — follow-up job event log
CREATE TABLE lich_follow_up_su_kien (
    id                UUID PRIMARY KEY,
    lich_follow_up_id UUID NOT NULL REFERENCES lich_follow_up(id),  -- [EN: follow_up_job_id]
    loai_su_kien      VARCHAR(48) NOT NULL,                         -- [EN: event_type]
    du_lieu_su_kien   JSONB,                                        -- [EN: event_data]
    tao_luc           TIMESTAMPTZ NOT NULL DEFAULT NOW()            -- [EN: created_at]
);
```

Nên có thêm cấu hình theo doanh nghiệp/kênh về giới hạn follow-up, khoảng nghỉ tối thiểu, khung giờ liên hệ, số lần thử tối đa và thời gian giữ lead ở trạng thái nurture.

## 56.7 Điều kiện bắt buộc kiểm tra ngay trước khi gửi

1. Đã có tin nhắn mới từ khách kể từ lúc lập lịch chưa? Nếu có, hủy job cũ và đánh giá lại.
2. Khách có yêu cầu ngừng liên hệ, opt-out hoặc có trạng thái không cho phép tiếp thị không?
3. Kênh và tài khoản có quyền gửi tin ở thời điểm hiện tại không; messaging window/loại tin có cho phép không? Nếu không, không gửi và không tìm cách lách.
4. Đã vượt giới hạn tần suất hoặc số lần follow-up chưa? Có job khác cho cùng khách/hội thoại không?
5. Nội dung có đúng sự thật, đúng sản phẩm, không lộ dữ liệu của khách khác, không đưa ra cam kết giá/tiết kiệm/bảo hành không có căn cứ không?
6. Media có phù hợp với chủ đề và được phép chia sẻ trên kênh đó không? Với link YouTube/Facebook/TikTok, chỉ gửi link còn `ACTIVE`; không coi các URL này là file nguồn được phép tải xuống và đăng lại.
7. Lead đã chuyển cho sales, đã có giao dịch, đang khiếu nại hoặc cần người thật xử lý chưa? Nếu có, áp dụng quy tắc bàn giao và tránh nhắn tự động chồng chéo.
8. Lưu kết quả gửi và lỗi; retry có giới hạn, backoff và idempotency để tránh gửi trùng.

## 56.8 Dashboard vận hành

Admin cần xem được:

- Follow-up đang chờ, đến hạn, đã gửi, bị hủy, bị chặn bởi policy và cần duyệt.
- Lý do AI chọn thời điểm/nội dung; tài liệu chiến lược đã truy xuất; media được chọn.
- Timeline hội thoại, lịch follow-up tiếp theo và nút tạm dừng/hủy/tiếp quản thủ công.
- Tỷ lệ phản hồi, opt-out, chuyển thành lead, tạo cơ hội bán hàng và chốt sale theo chiến lược/video/kênh.
- Nút chỉnh sửa playbook, ngưỡng lead score, giới hạn tần suất và chế độ `draft-only`, `human-approval`, `auto-send-approved-cases`.

## 56.9 Phạm vi MVP khuyến nghị

- **Ràng buộc kênh quyết định phạm vi:** Messenger ngoài 24 giờ chỉ người thật được nhắn (tag `HUMAN_AGENT`), Zalo OA chỉ có tin tư vấn trong cửa sổ hoặc ZNS theo template, TikTok DM chưa có quyền. Trên thực tế phần lớn follow-up là **việc cho sales**, không phải tin tự động.
- Giai đoạn đầu: dùng **luật cố định** thay cho planner AI, ví dụ `WARM` im lặng quá X giờ → tạo việc cho sales kèm tóm tắt hội thoại và gợi ý nội dung; kiểm tra phản hồi mới / opt-out trước khi nhắc. Lịch follow-up vẫn lưu bền vững.
- Planner AI (mục 56.3) chỉ làm khi có kênh cho phép gửi chủ động hợp lệ và đã có số liệu cho thấy luật cố định chưa đủ.
- Sau khi có dữ liệu thực: bật auto-send cho nhóm tình huống rủi ro thấp đã được phê duyệt, với giới hạn rõ ràng và audit log.
- Chỉ mở tự động gửi trên những kênh mà API/tài khoản hiện tại cho phép. Nếu kênh không hỗ trợ tin chủ động hợp lệ, hệ thống vẫn có thể tạo nhiệm vụ nhắc nhân viên hoặc chờ khách phản hồi.

**Kết luận:** Lead Nurturing là workflow độc lập tích hợp với Customer 360, Claude, Knowledge Base (mục 5), Media Library, Scheduler, Policy Engine và các channel adapter. AI quyết định đề xuất *khi nào, vì sao và nên gửi nội dung gì*; backend kiểm soát *có được phép gửi hay không, có còn phù hợp tại thời điểm gửi hay không*.

# 57. TikTok — phạm vi tích hợp end-to-end cho hệ thống điện mặt trời

Mục tiêu của phần này là xem TikTok như một nguồn thu hút khách hàng và một kênh nội dung trong cùng hệ thống Customer 360, không phải một module đăng video tách rời.

## 57.1 Các luồng TikTok cần quản lý

| Luồng | Hệ thống xử lý | Điều kiện / giới hạn |
|---|---|---|
| Đăng video organic | Content Planner → duyệt nội dung → TikTok Publishing Adapter → theo dõi trạng thái | Dùng TikTok Content Posting API hoặc nhà cung cấp được phê duyệt; kiểm tra app review, OAuth, quyền tài khoản và quy định hiện hành |
| Video quảng cáo / chiến dịch trả phí | Lưu campaign reference, landing page và mã chiến dịch; đối soát lead về Customer 360 | Quản lý qua TikTok for Business API khi tài khoản và app được cấp quyền; không mặc định mọi tài khoản đều có quyền |
| TikTok Lead Generation / Instant Form | Webhook hoặc API đồng bộ lead → chuẩn hóa dữ liệu → chống trùng → Customer 360 → phân công sales | Xác minh quyền Lead Generation, webhook/API và phạm vi truy cập của advertiser trước khi triển khai |
| Tin nhắn trực tiếp (DM) | Đồng bộ hội thoại → phân loại ý định → Claude + Knowledge Base (mục 5) → trả lời hoặc chuyển người thật | Chỉ tự động đọc/gửi khi Business Messaging API và quyền thực tế cho phép; nếu không, tạo nhiệm vụ cho nhân viên |
| Bình luận video | Thu thập/trả lời bình luận nếu API hỗ trợ; phân loại câu hỏi về giá, công suất, bảo hành và nhu cầu lắp đặt | Không giả định API cho phép đọc/trả lời mọi bình luận hoặc tự chuyển bình luận thành DM |
| Phân tích hiệu quả | Lưu ID video/campaign, số liệu API cho phép, lead, cuộc gọi, khảo sát và kết quả bán hàng | Chỉ hiển thị chỉ số mà API hoặc hệ thống nội bộ thực sự cung cấp; phân biệt số liệu nền tảng và số liệu CRM |

## 57.2 Luồng TikTok Lead Generation

1. Khách hàng gửi biểu mẫu TikTok Lead Generation/Instant Form hoặc để lại thông tin qua landing page.
2. `TikTokLeadWebhookController` tiếp nhận webhook (nếu được cấp quyền), xác thực chữ ký/token theo tài liệu API và ghi sự kiện vào hàng đợi/inbox bền vững.
3. `TikTokLeadSyncJob` lấy chi tiết lead theo quyền API; chuẩn hóa tên, số điện thoại, khu vực, nhu cầu, thời điểm gửi và mã chiến dịch.
4. `LeadDeduplicationService` tìm khách hàng hiện có theo số điện thoại/email đã chuẩn hóa. Không tạo khách hàng trùng nếu đã tồn tại; lưu nguồn lead và lịch sử tương tác mới.
5. `LeadScoringService` tính mức tiềm năng theo `rules/lead-scoring.yaml` (mục 58.5), dùng thông tin khách tự cung cấp trong form (khu vực, thời điểm, ngân sách nếu có) và lịch sử tương tác.
6. `SalesAssignmentService` phân công theo khu vực, tải công việc hoặc quy tắc doanh nghiệp; gửi thông báo cho nhân viên kèm nguồn TikTok, campaign/form ID và thông tin khách được phép sử dụng.
7. AI chỉ tạo nội dung tư vấn/follow-up phù hợp với consent và quyền liên hệ của kênh. Nếu TikTok không cho phép chủ động nhắn tin, tạo tác vụ gọi điện hoặc liên hệ qua kênh khác khi có căn cứ đồng ý phù hợp.
8. Khi sale cập nhật kết quả, lưu trạng thái khảo sát, báo giá, thắng/thua và lý do để đo hiệu quả theo chiến dịch.

## 57.3 Dữ liệu bổ sung nên lưu

- `kenh_nguon` theo mã kênh mục 54.1: `TIKTOK_DM`, `TIKTOK_COMMENT` hoặc `TIKTOK_LEAD`.
- `hinh_thuc_nguon` (EN: `source_kind`) = `ORGANIC | LEAD_FORM | PAID_AD | LANDING_PAGE | DM | COMMENT`. Không đặt tên `loai_nguon` để tránh trùng với `media_nguon.loai_nguon`.
- `external_lead_id`, `advertiser_id`, `campaign_id`, `adgroup_id`, `ad_id`, `form_id`, `video_id` khi API cung cấp.
- `consent_status`, `consent_timestamp`, `consent_source`, `privacy_notice_version` nếu có thu thập thông tin đồng ý.
- `first_touch_at`, `last_touch_at`, `assigned_sales_user_id`, `lead_status`, `conversion_status`.
- Bản ghi webhook thô đã loại bỏ/che dữ liệu không cần thiết, trạng thái xử lý, số lần thử và idempotency key để chống đồng bộ trùng.

Các ID trường quảng cáo chỉ lưu khi thực tế có trong payload/API. Không tự dựng ID giả và không coi việc khách điền form là đồng ý nhận mọi loại tin marketing trên mọi kênh.

## 57.4 Màn hình Vben Admin cần bổ sung

- **TikTok Accounts:** trạng thái kết nối, advertiser/account ID, phạm vi OAuth đã cấp, hạn token và lỗi đồng bộ.
- **TikTok Leads:** danh sách lead, bộ lọc campaign/form/khu vực/trạng thái, chống trùng, phân công sales và lịch sử xử lý.
- **TikTok Content:** video, caption, hashtag, lịch đăng, trạng thái duyệt/xuất bản và lỗi từ nền tảng.
- **TikTok Inbox:** chỉ hiển thị DM/bình luận nếu tích hợp có quyền lấy dữ liệu; nếu không có API phù hợp, không giả lập một inbox đầy đủ.
- **TikTok Analytics:** so sánh video/campaign với lead đủ điều kiện, lịch hẹn khảo sát, báo giá và doanh thu từ CRM; ghi rõ dữ liệu nào là ước tính/nội bộ.

## 57.5 Java modules đề xuất

Theo cấu trúc chuẩn ở mục 31:

```text
adapters/tiktok/
    TikTokOAuthService.java
    TikTokCapabilityService.java
    TikTokWebhookController.java
    TikTokWebhookVerificationService.java
    TikTokLeadSyncService.java
    TikTokLeadMapper.java
    TikTokLeadDeduplicationService.java
    TikTokMessagingAdapter.java
    TikTokPublishingAdapter.java
    TikTokAnalyticsSyncService.java
services/lead/
    LeadScoringService.java
    LeadAssignmentService.java
    LeadAttributionService.java
```

Giữ `TikTokMessagingAdapter`, `TikTokPublishingAdapter` và `TikTokLeadSyncService` tách biệt. Mỗi adapter khai báo capability theo tài khoản/app đã kết nối để AI Orchestrator và Policy Engine biết tính năng nào thực sự khả dụng.

## 57.6 Kế hoạch triển khai an toàn

- **Giai đoạn 1:** Media Library (video cho TikTok) + lịch đăng/duyệt video bằng API được cấp quyền; nhập lead từ form/landing page có thể kiểm soát; chuyển lead cho sales.
- **Giai đoạn 2:** đồng bộ Lead Generation API/webhook và attribution theo campaign sau khi xác nhận quyền truy cập.
- **Giai đoạn 3:** DM, bình luận và phản hồi tự động chỉ khi tài khoản/app đủ điều kiện; bắt đầu ở chế độ AI draft + nhân viên duyệt.
- **Giai đoạn 4:** tối ưu theo tỷ lệ lead đủ điều kiện, khảo sát, báo giá và chốt hợp đồng; chỉ tự động hóa thêm sau khi đã có audit log và số liệu kiểm chứng.

Trước khi viết adapter production, lập checklist xác minh trực tiếp trong TikTok for Business/Developer Portal: thị trường Việt Nam, loại tài khoản, quyền app đã duyệt, OAuth scopes, webhook event, rate limit, cửa sổ nhắn tin, quy tắc lưu dữ liệu và điều khoản sử dụng. Nếu một API không hỗ trợ use case, chuyển sang quy trình thủ công hoặc kênh được cho phép; không dùng cách lách quyền truy cập.


## 57.7 Repo / MCP cho TikTok và cách dùng

Theo quy tắc ở mục 46.1: mọi chức năng TikTok chạy production đều là adapter Java gọi API chính thức; repo và MCP chỉ dùng làm tài liệu tham khảo.

| Nhu cầu | Chạy production bằng | Tham khảo (không chạy ở production) | Không nên giả định |
|---|---|---|---|
| Đăng video lên tài khoản công ty | `TikTokPublishingAdapter` (Java) → [Content Posting API](https://developers.tiktok.com/docs/en/content-posting-api-get-started) | [`postoncehq/tiktok-mcp`](https://github.com/postoncehq/tiktok-mcp): luồng đăng / lên lịch | Ứng dụng chưa qua audit có thể bị giới hạn bài đăng ở chế độ riêng tư |
| Nhận lead từ quảng cáo / form | `TikTokLeadSyncService` (Java) → Lead Generation API + webhook | [`justomat/tiktok-business-api-docs`](https://github.com/justomat/tiktok-business-api-docs) (OpenAPI) | Không coi dữ liệu công khai là lead |
| Tin nhắn trực tiếp, bình luận | `TikTokMessagingAdapter` (Java) → Business Messaging API / Accounts API nếu được cấp quyền; chưa có quyền thì nhân viên xử lý trong app, AI soạn nháp | `justomat/tiktok-business-api-docs` | Không coi scraper dữ liệu công khai là inbox |
| Quản lý quảng cáo, báo cáo (sau MVP) | `TikTokAdsService` (Java) → REST API của TikTok API for Business | [TikTok for Business MCP Server](https://ads-useast2a.tiktok.com/resources/help/article/about-tiktok-for-business-mcp-server?lang=en): tham khảo phạm vi tool | Không mặc định mọi tài khoản, thị trường hoặc chiến dịch đều đủ điều kiện |
| Nghiên cứu nội dung công khai (tùy chọn) | Nếu cần: Java gọi REST API của nhà cung cấp dữ liệu đã được duyệt về điều khoản, chi phí, chính sách dữ liệu | [`HasData/tiktok-mcp`](https://github.com/HasData/tiktok-mcp) | Chỉ đọc dữ liệu công khai; không đăng, không gửi DM |

### Cách nối với hệ thống Java

```text
Vben Admin / AI Orchestrator
          |
          +--> Java TikTokPublishingAdapter --> Content Posting API
          |
          +--> Java TikTokLeadSyncService   --> Lead Generation API / Webhook
          |
          +--> Java TikTokMessagingAdapter  --> Business Messaging API (nếu được cấp quyền)
          |
          +--> Java TikTokAdsService        --> TikTok API for Business (quảng cáo, sau MVP)
```

- Không cho model tự gọi công cụ có tác động bên ngoài mà không qua kiểm tra quyền. Đăng video, thay đổi ngân sách quảng cáo, gửi tin hoặc thay đổi chiến dịch phải qua `Policy Engine`, phân quyền và audit log; MVP có bước duyệt của nhân viên.
- Giữ secret / API key ở backend hoặc secret manager, không đặt trong Vben Admin hay trình duyệt.
- Nếu muốn dùng MCP cho công cụ phát triển, tự expose các hàm Java ở trên bằng MCP Java SDK (mục 46.1).

## 57.8 Phương án triển khai dành riêng cho tài khoản TikTok của công ty

Mục tiêu thực tế là đăng video lên tài khoản TikTok công ty và chăm sóc người đã tương tác với công ty. Tình trạng hiện tại: công ty đang dùng **Personal Account để kinh doanh**. Không cần kết luận rằng tài khoản này không đăng được qua API: TikTok for Business Accounts API mô tả khả năng hỗ trợ publishing, insights và comment moderation cho cả Business Account lẫn Personal Account khi advertiser/app được cấp quyền. Tuy nhiên, quyền Accounts API có quy trình xét duyệt riêng; từ 2026-03-20, cần hoàn thành Accounts API Access Application Form trong các trường hợp được nêu tại tài liệu chính thức: https://business-api.tiktok.com/portal/docs/accounts-api-overview/v1.3.

**Khuyến nghị thực tế:** giữ tài khoản hiện tại trong giai đoạn kiểm tra khả năng đăng video; đồng thời đánh giá việc chuyển sang hoặc xác minh **Business Account** để có lộ trình rõ ràng hơn cho chăm sóc DM. TikTok mô tả Business Messaging API là dành cho Business Accounts đã được cấp quyền, vì vậy không được giả định Personal Account hiện tại sẽ có API DM. Trước khi chuyển đổi, kiểm tra tác động tới âm thanh/nhạc thương mại, tính năng tài khoản và chiến lược nội dung. Không thu thập mật khẩu TikTok, không lưu session/cookie cá nhân và không dùng automation trình duyệt để vượt giới hạn nền tảng.

### A. Đăng video từ thư viện tập trung

1. Nhân viên tải video lên Media Library (MinIO/S3) hoặc liên kết tài nguyên đã có quyền sử dụng.
2. AI đề xuất caption, hashtag, nội dung mô tả và lịch đăng theo Brand Voice; lưu nguồn video và quyền sử dụng.
3. Nhân viên xem trước và duyệt caption, video, quyền riêng tư và thông tin công khai trước khi đăng.
4. `TikTokPublishingAdapter` dùng OAuth token của tài khoản công ty để gọi TikTok Content Posting API theo phương thức được cấp quyền: direct post hoặc upload draft.
5. Lưu request ID/publish ID, trạng thái, lỗi, thời điểm và post URL khi có; webhook hoặc tác vụ polling cập nhật trạng thái.
6. Nếu app chưa qua audit hoặc chỉ có quyền upload draft, giao diện phải hiển thị rõ rằng người dùng cần hoàn tất bước đăng trong ứng dụng TikTok; không hiển thị thành công giả.

Tài liệu chính thức: https://developers.tiktok.com/docs/en/content-posting-api-get-started và https://developers.tiktok.com/docs/en/content-posting-api-reference-direct-post. Direct Post cần app, sản phẩm Content Posting API, scope `video.publish`, sự ủy quyền của chủ tài khoản và audit theo yêu cầu TikTok. Với `PULL_FROM_URL`, URL/domain cần được xác minh theo yêu cầu của nền tảng.

### B. Chăm sóc khách hàng qua DM và bình luận

1. **Không đồng nhất quyền đăng video với quyền DM.** TikTok Accounts API có thể hỗ trợ đăng video/bình luận/insights cho Personal Account khi được phê duyệt; Business Messaging API được mô tả dành cho Business Accounts đã được cấp quyền. Xem tài liệu Accounts API: https://business-api.tiktok.com/portal/docs/accounts-api-overview/v1.3 và Business Messaging API: https://business-api.tiktok.com/portal/bm-api/education-hub.
2. Kiểm tra trong TikTok for Business/Developer Portal xem tài khoản công ty đã là Business Account/được xác minh khi cần, app có được cấp Business Messaging API, quyền đọc/gửi tin nhắn và webhook hay không. TikTok có thể yêu cầu Business Center và xác minh doanh nghiệp cho một số quyền quản lý DM.
3. Nếu được cấp quyền, `TikTokMessagingAdapter` nhận sự kiện qua webhook, xác thực sự kiện, ghi inbox bền vững rồi cập nhật hội thoại và Customer 360.
4. AI phân loại ý định: hỏi giá điện mặt trời, công suất, pin lưu trữ, bảo hành, khảo sát mái, hóa đơn tiền điện hoặc yêu cầu nhân viên gọi lại.
5. AI soạn câu trả lời dựa trên Knowledge Base, bảng giá/API nội bộ và Brand Voice. Không tự bịa giá, sản lượng, thời gian hoàn vốn hoặc cam kết kỹ thuật.
6. Với MVP, mặc định AI tạo bản nháp để nhân viên duyệt. Chỉ bật gửi tự động cho các tình huống rủi ro thấp sau khi đã kiểm thử, có giới hạn tần suất, chính sách từ chối liên hệ và nhật ký kiểm toán.
7. Khi có ý định mua rõ ràng, yêu cầu thông tin liên hệ phù hợp, lưu nguồn TikTok/campaign/video và chuyển lead cho sales. Không tự ý lấy số điện thoại hoặc nhắn ngoài nền tảng nếu chưa có căn cứ đồng ý hợp lệ.
8. Nếu không được cấp quyền Messaging API, dùng quy trình nhân viên xử lý trong ứng dụng TikTok; hệ thống chỉ quản lý lead được gửi hợp lệ qua form/landing page hoặc được nhập thủ công. Không giả định MCP nghiên cứu video có thể đọc/gửi DM.

Tài liệu chính thức: https://business-api.tiktok.com/portal/docs/business-messaging-api-get-started/v1.3 và https://business-api.tiktok.com/portal/bm-api/education-hub. TikTok mô tả Business Messaging API dành cho Business Accounts được ủy quyền. Không tự động giả định API này hoạt động cho Personal Account. Phạm vi tính năng, khả năng mở API tại thị trường Việt Nam, quy tắc gửi tin và điều kiện tài khoản cần được xác nhận trong Developer/Business Portal trước khi phát triển production. Nếu chưa có quyền DM API, MVP dùng hộp thư vận hành thủ công trong ứng dụng TikTok và AI chỉ tạo bản nháp/tác vụ follow-up từ dữ liệu khách hàng được thu thập hợp lệ.

### C. Màn hình kết nối tài khoản và kiểm tra quyền

Tạo trang `TikTok Account Connection` trong Vben Admin hiển thị:

- Trạng thái kết nối và tài khoản đã ủy quyền (không hiển thị token bí mật).
- Danh sách scope được cấp và trạng thái phê duyệt/audit của ứng dụng.
- Khả năng thực tế: đăng video trực tiếp, tải bản nháp, đọc/gửi DM, nhận webhook, đọc/phản hồi bình luận, đồng bộ lead.
- Trạng thái token, thời hạn và luồng kết nối lại; token được mã hóa/lưu ở backend.
- Nút kiểm tra kết nối, xem lỗi API, lịch sử đăng bài và lịch sử webhook.
- Tắt các chức năng chưa được cấp quyền; không cho AI gọi tool tương ứng khi capability là `false`.

### D. API/adapter và bảng dữ liệu cần có

- `TikTokOAuthService`: OAuth, refresh/revoke token và lưu thông tin credential an toàn.
- `TikTokCapabilityService`: kiểm tra capability theo tài khoản/app/scope.
- `TikTokPublishingAdapter`: khởi tạo đăng, upload, truy vấn trạng thái và xử lý retry có idempotency.
- `TikTokMessagingAdapter`: đồng bộ hội thoại/tin nhắn và gửi phản hồi trong phạm vi được cấp quyền.
- `TikTokWebhookController` + `TikTokWebhookVerificationService`: xác thực, chống xử lý sự kiện trùng và ghi nhận lỗi.
- Dùng chung các bảng của hệ thống (mục 29): `su_kien_webhook`, `hoi_thoai`, `tin_nhan`, `dinh_danh_kenh`, `nd_ban_dang_kenh`, `khach_tiem_nang`. Tài khoản kênh đã kết nối (scope, capability, tham chiếu tới secret) cần thêm bảng `tai_khoan_kenh` (EN: `channel_accounts`) khi làm adapter đầu tiên; token không lưu trong bảng mà nằm ở secret manager.

### E. Tiêu chí hoàn thành MVP

- Kết nối được tài khoản TikTok công ty bằng OAuth chính thức.
- Hiển thị đúng scope/capability được cấp, không giả định quyền chưa được duyệt.
- Đăng được video thử nghiệm theo luồng TikTok cho phép; trạng thái lỗi/thành công được đồng bộ chính xác.
- Nếu chỉ upload draft, hướng dẫn nhân viên hoàn tất đăng trong TikTok.
- Nếu Messaging API được cấp, nhận webhook và trả lời một hội thoại thử nghiệm với bước duyệt của nhân viên; nếu chưa được cấp, có fallback xử lý thủ công.
- Lead từ form/landing page được đồng bộ, chống trùng và gắn nguồn TikTok.
- Có audit log, retry có kiểm soát, chống gửi trùng, quản lý token và quy trình thu hồi quyền.

------------------------------------------------------------------------

# 58. Chiến lược AI for Sale

Chiến lược do doanh nghiệp cung cấp ngày 2026-10-20. Khi triển khai (Phase 0), nội dung này được nhập vào Knowledge thành các tài liệu ở mục 58.7; từ đó về sau sửa trên Knowledge Editor (mục 5.9), không sửa ở đây.

## 58.1 Nguyên văn (lưu để đối chiếu)

> **1. Mục tiêu**
> - AI cần đánh giá và phân loại được mức độ tiềm năng mua hàng của khách hàng thông qua: 1. Mức độ tương tác của khách hàng với việc cung cấp thông tin và trả lời các câu hỏi của AI / 2. Cung cấp số mobile cho AI
>
> **2. Gợi ý cách đánh giá và phân loại**
> - Trả lời câu hỏi của khách thật logic và ngắn gọn. Và mỗi lần trả lời, tận dụng hỏi thêm câu hỏi để đánh giá mức độ muốn tìm hiểu về sản phẩm cúa khách hàng.
> - Hỏi khách hàng, không ôm đồm nhiều câu hỏi ở mỗi lần hỏi. Mỗi lần hỏi chỉ hỏi 1 câu.
> - Khách hàng sẵn sàng trả lời và cung cấp 3 đến 5 câu hỏi của bạn, thì cơ bản là khách hàng có nhu cầu thật.
> - Minh bạch giá bán trong câu trả lời, để đánh giá khách hàng ở phân khúc nào? Tránh chăm sóc khách hàng nhầm phân khúc.
>
> **3. 3 câu hỏi cần trả lời, sau khi tư vấn với khách hàng.**
> - Khách hàng đã tìm hiểu về điện mặt trời chưa? Nếu đã tìm hiểu, thì phán đoán xem đã tìm hiểu kĩ chưa, dựa trên cách trả lời của khách hàng
> - Khách hàng sẵn sàng đầu tư ngân sách bao nhiêu?
> - Khách hàng dự định khi nào sẽ đầu tư? Tuần tới, tháng tới, hay năm tới.
>
> **4. Những thông tin tối thiểu cần biết để đưa được giải pháp tốt nhất cho khách hàng:**
> - Khu vực lắp đặt (chỉ cần thành phố, tỉnh nào?)
> - Điện gia đình là 1 pha hay 3 pha
> - Hoá đơn tiền điện mỗi tháng là bao nhiêu? (Phải nhẩm ra được số điện dùng mỗi ngày)
> - Tỷ lệ dùng điện giữa ban ngày và ban đêm. Nếu ban ngày nhiều, thì lưu trữ sẽ ít hơn.
> - Mái thuộc loại nào: Mái tôn, mái ngói, và mái bằng. (Mái tôn và ngói, thì trọn gói 100%. Mái bằng sẽ phát sinh phần khung)
>
> **5. Những thông tin cung cấp để nhắc nhở và lấy sự chú ý của khách hàng để phán đoán.**
> *(chưa điền)*

## 58.2 Mục tiêu và cách tư vấn

AI đánh giá và phân loại **mức độ tiềm năng mua hàng** của khách dựa trên hai tín hiệu:

1. Mức độ tương tác: khách có sẵn lòng cung cấp thông tin và trả lời các câu hỏi của AI không.
2. Khách có cung cấp số điện thoại di động không.

Cách tư vấn:

- Trả lời câu hỏi của khách logic, ngắn gọn.
- Mỗi lần trả lời, hỏi thêm **đúng một câu** để thu thêm thông tin và đo mức độ quan tâm. Không ôm nhiều câu hỏi trong một tin.
- Khách sẵn lòng trả lời từ 3 câu hỏi của AI trở lên thì cơ bản là có nhu cầu thật.
- **Minh bạch giá** để biết khách ở phân khúc nào, tránh chăm sóc nhầm phân khúc:
  - Khi chưa đủ thông tin: đưa **khoảng giá** theo loại hệ / số pha / khu vực (tool `tim_goi_phu_hop` trả dải giá khi thông tin chưa đủ, mục 4.2.6).
  - Khi đủ thông tin tối thiểu (mục 58.4): đưa giá chính xác của gói đề xuất (`tong_gia`).
  - Mọi con số giá đều lấy từ tool, không tự nhẩm.
  - Luôn kèm điều kiện báo giá (mục 4.2.8): giá niêm yết, đã / chưa gồm VAT, giá cuối do nhân viên báo sau khảo sát. Như vậy sales vẫn có chỗ điều chỉnh và khách không coi con số của AI là cam kết.
- **Mỗi lần hỏi một câu không có nghĩa là lượt nào cũng phải hỏi.** Khi khách đang hỏi để hiểu (ví dụ hybrid khác on-grid thế nào), trả lời cho đủ trước; chỉ hỏi thêm khi câu hỏi đó giúp tư vấn. Tiêu chí "3 câu đã trả lời" dùng để đo mức quan tâm, không phải để AI cố hỏi cho đủ.

**Thay đổi so với thiết kế trước:** playbook "khách hỏi giá" ở mục 13 trước đây là "không báo giá ngay". Chiến lược này thay thế bằng "báo khoảng giá sớm và hỏi một câu"; mục 13 đã được cập nhật theo.

## 58.3 Ba câu hỏi cần trả lời sau khi tư vấn

| Câu hỏi | Trường lưu | Cách AI xác định |
|---|---|---|
| Khách đã tìm hiểu về điện mặt trời chưa, kỹ đến đâu? | `research_level`: `CHUA` / `SO_BO` / `KY` | Phán đoán từ cách khách hỏi và trả lời: dùng đúng thuật ngữ (kWp, hybrid, hoàn vốn), so sánh hãng, hỏi kỹ thuật cụ thể. Bắt buộc ghi kèm câu của khách làm căn cứ |
| Khách sẵn sàng đầu tư ngân sách bao nhiêu? | `budget_vnd` (một số hoặc khoảng) | Khách nói trực tiếp, hoặc qua phản ứng khi được báo khoảng giá |
| Khách dự định khi nào đầu tư? | `timeline`: `TUAN_TOI` / `THANG_TOI` / `VAI_THANG` / `NAM_TOI` / `CHUA_RO` | Hỏi tự nhiên, đúng thời điểm, không hỏi dồn |

AI trả các tín hiệu này trong trường `qualification` của kết quả có cấu trúc (mục 18); backend lưu vào `ho_so_tu_van.phan_loai`.

## 58.4 Thông tin tối thiểu để đưa giải pháp

| Thông tin | Ghi chú |
|---|---|
| Khu vực lắp đặt | Chỉ cần tỉnh / thành phố. Backend quy đổi sang miền Bắc (giá HN) hoặc miền Nam (giá HCM) theo danh sách tỉnh của doanh nghiệp (mục 4.2.9) |
| Điện 1 pha hay 3 pha | Lọc gói theo `so_pha` |
| Hóa đơn tiền điện mỗi tháng | `SizingService` quy đổi ra kWh/tháng theo biểu giá bậc thang EVN, rồi ra số điện dùng mỗi ngày. AI dùng kết quả của tool, không tự nhẩm |
| Tỷ lệ dùng điện ngày / đêm | Dùng ban ngày nhiều thì cần ít lưu trữ hơn (On-Grid hoặc Hybrid pin nhỏ); dùng buổi tối nhiều thì cần lưu trữ |
| Loại mái | Mái tôn, mái ngói: trọn gói 100%. Mái bằng: phát sinh phần khung (`gia_khung_sat`) |

- **Diện tích mái** không bắt buộc ở bước đầu; chỉ hỏi khi cần kiểm tra mái đủ chỗ cho gói đề xuất (theo công thức của doanh nghiệp).
- **Lưu ý dữ liệu:** `tron_goi.gia_khung_sat` hiện bằng 0 ở mọi gói. Trước khi có số liệu ở `slm_v2`, với mái bằng AI chỉ nói "có phát sinh phần khung, nhân viên sẽ báo sau khảo sát" và không đưa con số.

## 58.5 Phân loại mức độ tiềm năng

Đề xuất ban đầu, doanh nghiệp duyệt và chỉnh trong `rules/lead-scoring.yaml`:

| Mức | Điều kiện |
|---|---|
| `HOT` (nóng) | Đã cho số điện thoại **và** (dự định trong tuần / tháng tới **hoặc** đã đủ thông tin tối thiểu) |
| `OFF_SEGMENT` (sai phân khúc) | Đã biết khoảng giá, ngân sách khách nêu thấp hơn rõ rệt gói rẻ nhất phù hợp |
| `WARM` (ấm) | Đã trả lời từ 3 câu hỏi của AI trở lên; hoặc đã cho số điện thoại nhưng dự định vài tháng / năm tới |
| `COLD` (nguội) | Trả lời dưới 3 câu, hoặc hỏi giá xong rồi im |

Xét theo thứ tự `HOT` → `OFF_SEGMENT` → `WARM` → `COLD`. Khách `HOT` nhưng ngân sách thấp vẫn là `HOT`, kèm cờ "ngân sách thấp".

**Cách tính:**

- AI chỉ **trích tín hiệu**: câu hỏi vừa hỏi (`question_asked`), thông tin khách vừa cung cấp (`collected`), `qualification`.
- Backend **tính mức** theo `lead-scoring.yaml`, tất định và có lịch sử phiên bản:
  - `so_cau_da_tra_loi` tăng khi tin tiếp theo của khách trả lời đúng thông tin AI vừa hỏi.
  - Mức và lý do lưu ở `ho_so_tu_van`, tính lại sau mỗi lượt.
- Khách cho số điện thoại thì luôn tạo lead (mục 23), gửi kèm mức, lý do, `research_level`, ngân sách, thời điểm.

**Hành động theo mức:**

| Mức | Hành động |
|---|---|
| `HOT` | Báo sales ngay |
| `WARM` | Lead Nurturing (mục 56) |
| `COLD` | Chỉ chăm sóc nhẹ; không follow-up dồn dập |
| `OFF_SEGMENT` | Không follow-up chủ động; có thể giới thiệu gói nhỏ nhất phù hợp nếu có |

## 58.6 Thông tin để nhắc và thu hút sự chú ý

Mục 5 của chiến lược **chưa được doanh nghiệp điền**. Các nhóm đề xuất dưới đây cần duyệt trước Phase 1, và chỉ dùng số liệu có nguồn:

- Ước tính tiết kiệm theo đúng hóa đơn khách vừa nêu (từ `SizingService`, không tự bịa).
- Case study nhà tương tự: cùng khu vực, cùng mức hóa đơn (Media Library, Knowledge).
- Video ngắn đúng mối quan tâm của khách (danh mục media, mục 7).
- Chính sách bảo hành, trọn gói, thời gian lắp đặt (`policies/`).
- Chương trình ưu đãi đang chạy, chỉ khi có trong Knowledge và còn hạn.

## 58.7 Chuyển thành tài liệu Knowledge khi triển khai

| Phần | Tài liệu |
|---|---|
| 58.2 | `playbooks/tu-van-lap-moi.md` (mục tiêu, mỗi lần một câu, minh bạch giá) và cập nhật `playbooks/khach-hoi-gia.md` |
| 58.3, 58.4 | `rules/consult-slots.yaml` (thông tin tối thiểu + `qualification`) |
| 58.5 | `rules/lead-scoring.yaml` + phần "phân loại khách" trong playbook |
| 58.6 | `playbooks/thu-hut-chu-y.md`, sau khi doanh nghiệp bổ sung |

Bản nháp `rules/consult-slots.yaml`:

``` yaml
slots:                      # thông tin tối thiểu (mục 58.4)
  khu_vuc:        { required: true,  type: province, ask: "Nhà mình ở tỉnh/thành nào ạ?" }   # → miền theo rules/regions.yaml
  so_pha:         { required: true,  type: enum, values: [1, 3], ask: "Điện nhà mình là 1 pha hay 3 pha ạ?" }
  hoa_don_thang:  { required: true,  type: vnd, ask: "Tiền điện mỗi tháng nhà mình khoảng bao nhiêu ạ?" }
  ty_le_ngay_dem: { required: true,  type: enum, values: [NGAY_NHIEU, CAN_BANG, DEM_NHIEU],
                    ask: "Nhà mình dùng điện nhiều vào ban ngày hay buổi tối ạ?" }
  loai_mai:       { required: true,  type: enum, values: [TON, NGOI, BANG], ask: "Mái nhà mình là mái tôn, mái ngói hay mái bằng ạ?" }
  dien_tich_mai:  { required: false, type: m2, ask_when: "cần kiểm tra mái đủ chỗ cho gói đề xuất" }
qualification:              # ba câu hỏi sau tư vấn (mục 58.3)
  research_level: { type: enum, values: [CHUA, SO_BO, KY], evidence: required }
  budget_vnd:     { type: vnd_range }
  timeline:       { type: enum, values: [TUAN_TOI, THANG_TOI, VAI_THANG, NAM_TOI, CHUA_RO] }
rules:
  one_question_per_message: true
```

Bản nháp `rules/lead-scoring.yaml`:

``` yaml
order: [HOT, OFF_SEGMENT, WARM, COLD]
levels:
  HOT:
    all: [has_phone]
    any: [timeline_in: [TUAN_TOI, THANG_TOI], minimum_info_complete]
  OFF_SEGMENT:
    all: [price_range_shown, budget_known, budget_below_cheapest_match_ratio: 0.8]
  WARM:
    any: [answered_questions_at_least: 3, has_phone]
  COLD:
    default: true
flags:
  low_budget: [budget_below_cheapest_match_ratio: 0.8]
```

Các câu `ask` chỉ là câu gợi ý; AI diễn đạt lại theo văn phong (mục 10, 15) và ngữ cảnh hội thoại.

------------------------------------------------------------------------

# 59. Phản biện hệ thống và quyết định cần chốt

Kết quả rà soát kiến trúc ngày 2026-10-20, không xét rủi ro của các repo / MCP tham khảo. Mỗi vấn đề có trạng thái:

- **Đã áp dụng:** đã sửa vào các mục liên quan của tài liệu.
- **Chờ doanh nghiệp:** cần doanh nghiệp quyết định hoặc cung cấp dữ liệu.
- **Chờ chốt:** đề xuất kỹ thuật / phạm vi, cần người phụ trách dự án đồng ý.

## 59.1 Bảng tổng hợp

| # | Vấn đề | Mức | Trạng thái | Mục liên quan |
|---|---|---|---|---|
| 1 | Phạm vi quá lớn so với một người phát triển | Cao | Chờ chốt (59.6, 59.7) | 38 |
| 2 | Khách đã lắp đặt không có nguồn dữ liệu | Cao | Chờ doanh nghiệp | 2, 4.1 |
| 3 | Ràng buộc kênh làm Lead Nurturing gần như chỉ còn việc cho sales | Cao | Đã áp dụng | 56.9 |
| 4 | Chế độ `DRAFT` làm mất lợi ích trả lời 24/7; "loại câu hỏi" chưa định nghĩa | Cao | Đã áp dụng; doanh nghiệp chốt 5 loại `AUTO` (2026-10-20) | 18, 35, 38 |
| 5 | Kiểm tra "mọi con số phải có trong tool" khó làm, dễ chặn nhầm | Trung bình | Đã áp dụng | 4.2.6, 33.1 |
| 6 | Công thức sizing chưa có; dải giá khi thiếu thông tin quá rộng | Cao | Chờ doanh nghiệp | 4.2.6, 4.2.8 |
| 7 | Báo giá thiếu điều kiện (VAT, phụ phí, chiết khấu, hiệu lực, vùng phục vụ) | Cao | Chờ doanh nghiệp; cách AI nói đã áp dụng | 4.2.8, 4.2.9, 58.2 |
| 8 | Chuyển nhân viên không có đường trả lại AI; echo của chính app | Trung bình | Đã áp dụng | 19, 29, 54.3 |
| 9 | Hủy lượt nhưng tool ghi đã chạy | Trung bình | Đã áp dụng | 19 |
| 10 | Gộp khách trùng số điện thoại chưa có quy trình | Trung bình | Đã áp dụng | 29 |
| 11 | Nhân viên phải dùng hai inbox | Trung bình | Đã chốt: inbox admin là nơi làm việc chính (2026-10-20) | 34 |
| 12 | Bộ test câu đơn lẻ không đo được hội thoại nhiều lượt | Trung bình | Đã áp dụng | 35 |
| 13 | Chọn model bắt đầu từ model rẻ nhất là ngược | Thấp | Đã áp dụng | 20.1, 36 |
| 14 | Thiếu công tắc dừng toàn cục và KPI mục tiêu | Trung bình | Công tắc đã áp dụng; KPI chờ doanh nghiệp | 33.2, 38 |
| 15 | Chưa có phần pháp lý dữ liệu cá nhân | Cao | Chờ doanh nghiệp | 59.5 |

## 59.2 Vấn đề nền tảng

**1. Phạm vi so với nguồn lực.** Phase 1 hiện gồm webhook, gộp tin, khóa, tiếp quản, 6 tool, catalog sync, SizingService, inbox admin, Knowledge Editor có phiên bản / phát hành / rollback / lint và phân loại lead; Vben Admin có khoảng 20 module. Với một người phát triển, rủi ro lớn nhất là nhiều tháng chưa có khách nào được trả lời. Đề xuất: MVP rút gọn ở mục 59.7, mỗi phase có KPI mục tiêu và tiêu chí qua phase (mục 38).

**2. Khách đã lắp đặt không có nguồn dữ liệu.** Nhóm 1–4 ở mục 2 là phần lớn trong khoảng 3.000 khách, nhưng bot không đọc khách hàng / hợp đồng của `slm_v2` và chưa có kế hoạch nhập khách cũ. Cần doanh nghiệp chốt:

- Hệ nào là nguồn gốc của khách hàng và hệ thống điện của khách (`slm_v2`, Sales API hay CRM khác)?
- Bot nhập một lần hay đồng bộ định kỳ? Đồng bộ những trường nào (tên, số điện thoại, tỉnh, công suất, ngày lắp, hãng inverter, sales phụ trách)?
- Nếu đồng bộ từ `slm_v2`: thêm view `ai_v_khach_hang` / `ai_v_hop_dong` chỉ gồm các trường cần thiết, cùng cách làm ở mục 4.2.5.

Cho tới khi chốt, các chức năng cho khách đã lắp đặt (CSKH sau bán, báo cáo điện năng, giới thiệu) không có dữ liệu để chạy.

**3. Ràng buộc kênh và Lead Nurturing.** Messenger ngoài 24 giờ chỉ người thật được nhắn; Zalo OA chỉ có tin tư vấn trong cửa sổ hoặc ZNS theo template; TikTok DM chưa có quyền. Planner AI ở mục 56 vì vậy chủ yếu sinh việc cho sales. Đã đổi phạm vi MVP của mục 56 sang luật cố định (mục 56.9).

**4. `DRAFT` và loại câu hỏi.** Nếu mọi tin đều chờ nhân viên duyệt thì tin lúc 22h chờ tới sáng, mất lợi ích chính là trả lời ngay. Đã áp dụng: danh sách `intent` và chế độ theo từng loại (mục 35), `intent` trong kết quả AI (mục 18), câu báo nhận ngoài giờ khi lượt rơi vào `DRAFT`.

**Đã chốt (2026-10-20):** gửi tự động ngay từ Phase 1 cho `CHAO_HOI`, `TU_VAN_LAP_MOI`, `HOI_GIA`, `GIAI_THICH`, `CHINH_SACH`, sau khi bộ test đạt. `KHAC` để nháp; sự cố kỹ thuật và khiếu nại luôn chuyển nhân viên. Lượt nào không qua Policy (kể cả kiểm tra con số) tự chuyển `DRAFT`; loại nào có lỗi sai thông tin hoặc tỷ lệ BAD / EDIT vượt ngưỡng tự quay về `DRAFT`; công tắc dừng toàn cục (mục 33.2) đưa tất cả về `DRAFT` khi cần (mục 35).

## 59.3 Lỗ hổng thiết kế

| # | Vấn đề | Cách xử lý |
|---|---|---|
| 5 | Kiểm tra con số | Mục 33.1: chỉ kiểm tra loại số rủi ro, nguồn hợp lệ gồm tool, tin của khách và Knowledge; lệch thì chuyển `DRAFT`, không bỏ tin |
| 6 | Sizing và dải giá | Công thức sizing là việc chặn của Phase 0 (mục 4.2.8). Dải giá khi thiếu thông tin tính theo khoảng hóa đơn (mục 4.2.6), không dùng dải 53,6–838,6 triệu của cả catalog. Số liệu nguồn còn lỗi (gói 101, gói 97/98, đơn vị sản lượng) phải sửa trước |
| 7 | Điều kiện báo giá, vùng phục vụ | Doanh nghiệp chốt VAT, phụ phí, chiết khấu, hiệu lực (mục 4.2.8) và danh sách tỉnh phục vụ (cờ `serviced`, mục 4.2.9). AI luôn nói giá niêm yết, giá cuối sau khảo sát (mục 58.2) |
| 8 | Trả hội thoại lại cho AI | Nút "Trả lại AI" hoặc tự trả sau N giờ, lượt đầu sau khi trả đi `DRAFT` (mục 19); cột `tiep_quan_luc`, `nhan_vien_tin_cuoi_luc` (mục 29); lọc echo theo `app_id` (mục 54.3) |
| 9 | Tác dụng phụ của tool | Tool ghi chỉ áp dụng khi lượt được chốt, cùng transaction (mục 19) |
| 10 | Gộp khách trùng | Quy trình gộp, cột `gop_vao_id`, bảng `khach_hang_lich_su_gop`; một số điện thoại = một khách / hộ (mục 29) |

**11. Hai inbox (đã chốt 2026-10-20: inbox admin là nơi làm việc chính).** Nhân viên quen trả lời trong Meta Business Suite; bản nháp AI lại nằm ở inbox admin. Nếu nhân viên bỏ qua inbox admin thì không có dữ liệu `kt_sua_ban_nhap` để bật `AUTO`. Cần chọn một trong hai:

- **Inbox admin là nơi làm việc chính:** nhân viên trả lời mọi tin Messenger từ admin; Meta Business Suite chỉ dùng khi admin lỗi. Inbox admin cần đủ tiện (thông báo, lọc, phím tắt) để thay được.
- **Giữ Meta Business Suite:** AI trả lời `AUTO` cho 5 loại câu hỏi đã chốt (mục 35); phần còn lại AI ghi nháp thành ghi chú nội bộ / việc cho nhân viên, nhân viên trả lời trong Meta Business Suite và hệ thống thu tin trả lời qua echo để so với nháp.

## 59.4 Đánh giá và vận hành

- **12. Bộ test nhiều lượt:** đã thêm kịch bản hội thoại nhiều lượt, khách giả lập và cách chấm (mục 35).
- **13. Chọn model:** chạy bộ test với Opus 5.5 trước, chỉ hạ model khi đạt ngang (mục 20.1); tối ưu chi phí chỉ khi số liệu cho thấy cần (mục 36).
- **14. Vận hành:** công tắc dừng toàn cục, tự bật theo ngưỡng (mục 33.2). KPI mục tiêu và tiêu chí qua phase do doanh nghiệp đặt ở Phase 0 (mục 38).

## 59.5 Pháp lý dữ liệu cá nhân

Nội dung chat chứa tên, số điện thoại, địa chỉ và được gửi tới Claude API (máy chủ ở nước ngoài), tức là xử lý và chuyển dữ liệu cá nhân ra nước ngoài. Cần người phụ trách pháp chế xác nhận theo Nghị định 13/2023/NĐ-CP và Luật Bảo vệ dữ liệu cá nhân (hiệu lực từ 2026) trước khi chạy với khách thật, tối thiểu các việc:

- Hồ sơ đánh giá tác động xử lý dữ liệu cá nhân và hồ sơ đánh giá tác động chuyển dữ liệu ra nước ngoài.
- Thông báo cho khách: đang trò chuyện với trợ lý AI (đã có ở `core/identity.md`) và dữ liệu được xử lý thế nào; cơ chế đồng ý hoặc căn cứ pháp lý thay thế.
- **Thời hạn lưu:** `tin_nhan`, `su_kien_webhook`, `luot_goi_ai`, nội dung trong `tl_tin_nhan`. Có job xóa / ẩn danh khi hết hạn.
- **Quyền của khách:** xem, xóa dữ liệu; quy trình xử lý yêu cầu trên admin.
- **Giảm dữ liệu gửi đi:** chỉ gửi phần hội thoại cần thiết; che số điện thoại trong lịch sử gửi cho Claude sau khi đã lưu vào `ho_so_tu_van`.
- Kiểm tra điều khoản lưu trữ dữ liệu của Anthropic phù hợp với yêu cầu trên.

## 59.6 Phần nên cắt hoặc hoãn

| Phần | Lý do | Thay bằng |
|---|---|---|
| Trợ lý nội dung + PublishScheduler cho 3 kênh (mục 26, 55) | Meta Business Suite đã lên lịch đăng được; số bài ít | Đăng tay; làm lại khi lượng nội dung và kênh tăng |
| Media Library đủ 5 loại nguồn, chống trùng sha256, transcode, kiểm tra link chết (mục 6) | Chỉ có vài chục video | `UPLOAD` (ảnh) + link YouTube; ảnh gói từ `SLM_V2` |
| AI góp ý, AI viết từ yêu cầu thô, phát hiện xung đột, hẹn giờ kích hoạt (mục 5.9–5.10) | Ít người sửa kiến thức | Sửa, phát hành, rollback, đếm token; mẫu tài liệu |
| Planner AI của Lead Nurturing (mục 56.3) | Ràng buộc kênh (59.2 mục 3) | Luật cố định → việc cho sales (mục 56.9) |
| Adapter 7 hãng inverter (mục 21) | Mỗi hãng một API riêng | 1–2 hãng có nhiều khách nhất |
| Persona, phong cách theo nhân viên (mục 16) | Chưa cần | `Company Default` |

## 59.7 MVP rút gọn đề xuất

Mục tiêu: có khách thật được trả lời trên Messenger sớm nhất có thể, đo được giá trị trước khi làm tiếp.

1. **Dữ liệu:** view `ai_v_*`, catalog sync, `SizingService` có unit test, `regions.yaml` có vùng phục vụ.
2. **Kiến thức:** tài liệu Markdown / YAML nhập vào DB, có phát hành và rollback; chưa cần editor đầy đủ (sửa bằng form đơn giản hoặc nhập lại từ repo).
3. **Hội thoại Messenger:**
   - webhook, gộp tin, khóa, tiếp quản và trả lại;
   - tool catalog / sizing / lead / handoff;
   - Policy (kiểm tra con số);
   - `AUTO` cho 5 loại câu hỏi đã chốt, `DRAFT` cho `KHAC`, chuyển nhân viên cho sự cố kỹ thuật và khiếu nại;
   - công tắc dừng toàn cục.
4. **Lead → Sales API**, kèm mức tiềm năng.
5. **Inbox admin tối thiểu** (hoặc phương án Meta Business Suite, 59.3 mục 11), dashboard `luot_goi_ai` cơ bản.
6. **Bộ test** câu đơn lẻ + kịch bản nhiều lượt.

Mọi phần còn lại của mục 38 làm sau khi MVP đạt KPI mục tiêu.

## 59.8 Việc cần chốt trước khi code

| Việc | Người quyết định | Chặn phần nào |
|---|---|---|
| Nguồn dữ liệu khách cũ và cách đồng bộ (59.2) | Doanh nghiệp | CSKH khách cũ, báo cáo điện năng |
| Công thức sizing, đơn vị sản lượng, sửa dữ liệu nguồn (4.2.7, 4.2.8) | Doanh nghiệp | Tool `tim_goi_phu_hop`, báo giá |
| Điều kiện báo giá: VAT, phụ phí, chiết khấu, hiệu lực (4.2.8) | Doanh nghiệp | Mọi câu trả lời có giá |
| Danh sách tỉnh theo miền và vùng phục vụ (4.2.9) | Doanh nghiệp | Quy đổi giá |
| Pháp lý dữ liệu cá nhân (59.5) | Doanh nghiệp + pháp chế | Chạy với khách thật |
| ~~Inbox làm việc của nhân viên (59.3 mục 11)~~ Đã chốt: inbox admin | Doanh nghiệp | Inbox admin, dữ liệu sửa nháp |
| KPI mục tiêu và tiêu chí qua phase (38) | Doanh nghiệp | Quyết định mở rộng |
| MVP rút gọn và các phần hoãn (59.6, 59.7) | Người phụ trách dự án | Lộ trình |

------------------------------------------------------------------------

# 60. Việc cần làm (cập nhật 2026-10-29)

## 60.1 Đã có

| Phần | Trạng thái |
|---|---|
| Webhook Messenger: lưu khách / hội thoại / tin, gộp tin, khoá lượt, hiện "đang soạn tin" | Chạy thật trên Trang Solarmax. App Meta ở chế độ Development: chỉ tài khoản có vai trò trong app được bot trả lời |
| Nhân viên tiếp quản: trả lời từ hộp thư Meta thì bot nhường (nhận echo), tự trả lại AI sau thời gian chờ | Chạy thật |
| AI trả lời (Sonnet 5.5, effort `low`), xưng "Huyền", nói thật là trợ lý tự động khi khách hỏi. Gửi tự động 5 loại (`CHAO_HOI`, `TU_VAN_LAP_MOI`, `HOI_GIA`, `GIAI_THICH`, `CHINH_SACH`), `KHAC` thành nháp, `KY_THUAT_SU_CO` / `KHIEU_NAI` chuyển nhân viên | Chạy thật |
| Policy: kiểm tra con số, link; công tắc dừng toàn cục (mục 33) | Chạy thật. Công tắc mới là biến `AI_FORCE_DRAFT` trong `.env`, chưa có nút trên web |
| Hồ sơ tư vấn, lead, mức tiềm năng (mục 58.5), cảnh báo trùng số điện thoại; nhật ký mỗi lượt AI (`luot_goi_ai`: token, lần gọi tool) | Chạy thật |
| Mốc báo giá (60.4): đồng bộ catalog slm_v2 mỗi 10 phút kèm cảnh báo dữ liệu, PV out EVN theo năm và nhắc nạp năm mới, `SizingService`, tool `tim_goi_phu_hop` / `chi_tiet_goi`, link theo miền, giá đã gồm VAT | Chạy thật từ 2026-10-29 |
| Trang admin `https://fs-wms.io.vn`: đăng nhập, người dùng và vai trò, nhật ký thao tác | Chạy thật (HTTPS qua nginx-proxy-manager) |
| Thông báo nhân viên (mục 34): chuông, âm thanh, thông báo trình duyệt qua SSE | Chạy thật |
| Catalog & báo giá trên admin: *Gói từ slm_v2*, *Thử báo giá*, *PV out EVN*. Dựng theo khuôn bảng của fsignai (`BasicTable` / `useTable`, form tìm kiếm, phân trang). Gói sửa, bật / tắt, xoá mềm và khôi phục được ngay trên admin (60.4) | Đã viết 2026-10-29, chờ triển khai bản mới |
| Thư viện media (mục 6.3 D): nhập ảnh combo từ Google Drive vào MinIO (bucket `slm-ai-media`) | Đã nạp 24 ảnh và 11 link video Facebook (2026-10-29), đang ở trạng thái `DRAFT` nên AI chưa dùng |
| Kiến thức: `core/` (identity, voice, guardrails), playbook `khach-hoi-gia`, `tu-van-lap-moi` | Nạp từ file trong repo, chưa có trình soạn trên web |

## 60.2 Còn thiếu

**Cho MVP (Phase 0–1, mục 38, 59.7):**
1. **Inbox admin** (doanh nghiệp yêu cầu ghi lại để làm sau): danh sách hội thoại, xem tin; duyệt / sửa / gửi nháp AI ngay trên trang (thay việc sao chép sang Meta Business Suite); nút "Trả lại AI"; chấm GOOD / BAD / EDIT; danh sách lead; công tắc dừng trên web; gán nhân viên phụ trách (để thông báo `HANDBACK_REPLY` gửi đúng người).
2. **Lead → Sales API:** adapter `salesapi` còn trống; lead mới nằm trong CSDL của bot, chưa có trang xem lead (làm cùng Inbox admin).
3. **Media cho AI:** admin viết `tom_tat` / `dung_khi` và duyệt ảnh combo; quyết định bot có được gửi link video Facebook không (link slmsolar.vn / slmsolar.com.vn đã cho phép ở mốc báo giá; Policy chỉ cho link có trong kết quả tool, tin trang đã gửi và trang chủ theo miền); gửi ảnh qua Messenger (Attachment Upload API, không cần MinIO công khai). 24 ảnh combo đã nạp đang chờ duyệt.
4. **Kiến thức v1:** chính sách bảo hành, khoảng 10 FAQ, chiến lược bán hàng (mục 58.7); Knowledge Editor cơ bản: sửa, phiên bản, phát hành, rollback (mục 5.9).
5. **Bộ test** 100–200 câu hỏi thật và kịch bản nhiều lượt (mục 35); KPI mục tiêu và số liệu nền (mục 38, Phase 0).
6. **Dashboard:** trang *Phân tích* hiện là trang còn sót của fsignai (gọi `/api/v1/admin/dashboard`, lỗi 404). Thay bằng số liệu của bot từ `luot_goi_ai`: chi phí AI, tỷ lệ gửi tự động, tỷ lệ nhân viên sửa nháp.
7. **Ra khách thật:**
   - App Review + xác minh doanh nghiệp (3–6 tuần): `pages_messaging`, Business Asset User Profile Access (lấy tên khách để xưng anh / chị); trang chính sách quyền riêng tư và xoá dữ liệu; chuyển webhook sang tên miền công ty.
   - Token: chuyển Page token sang System User (token hiện gắn với tài khoản cá nhân của người quản trị Trang).
8. **Phía doanh nghiệp:**
   - Pháp lý dữ liệu cá nhân (mục 59.5).
   - Sửa dữ liệu trong slm_v2: gói 134 / 135, 140 / 141, 175 / 176 là **40 tấm** (25 kWp), slm_v2 đang ghi 39 tấm / 24,375 kWp; gói 101 `cong_suat_he_thong = 9` (đúng 8750); gói 101 / 102 lệch giá 600 nghìn giữa HN và HCM; gói 180 (HN) và 181 (HCM) cùng dòng nhưng cấu hình khác, ảnh của 180 là ảnh hệ 181.

**Thứ tự đề xuất (2026-10-29):**
1. Doanh nghiệp nộp App Review ngay, vì phải chờ 3–6 tuần.
2. Song song: Inbox admin. Đây là việc nhân viên dùng hằng ngày; các lần chấm, sửa nháp cũng là dữ liệu cho bộ test.
3. Sau đó: Lead → Sales API, duyệt và gửi ảnh, bộ test, dashboard của bot.

**Các phase sau (chưa bắt đầu, mục 38):**
- Phase 2: trả lời comment + private reply, Trợ lý nội dung và đăng bài, Lead Nurturing dạng việc cho sales, chạy bộ test khi phát hành kiến thức.
- Phase 3–5: Zalo OA (chờ doanh nghiệp quyết định), năng lượng (adapter inverter, báo cáo tháng), TikTok. Các adapter `zalo`, `solar`, `tiktok`, `facebookgroups` hiện là thư mục trống.
- CSKH khách cũ (Customer 360): chờ doanh nghiệp chốt nguồn dữ liệu khách cũ (mục 59.2).

## 60.3 Số liệu và công thức tính (doanh nghiệp chốt 2026-10-26)

**Hệ số PV out theo tỉnh:** dùng bảng chính thức **"Hệ số PVout lý thuyết của các tháng trong năm 2026"** của EVN (công văn 364/EVN-KDMBĐ ngày 20/1/2026, nguồn Viện Khoa học Khí tượng Thủy văn và Biến đổi khí hậu, văn bản 637/VKTTVBĐKH-KHTC ngày 14/11/2025), đủ 34 tỉnh sau sáp nhập, theo từng tháng (kWh/kWp). Cả năm: thấp nhất Hưng Yên 901, Hải Phòng 902, Hà Nội 912; TP Hồ Chí Minh 1.426; cao nhất Lâm Đồng 1.583. Bảng công thức của doanh nghiệp dùng 79,58 kWh/kWp/tháng cho Hà Nội (≈ 955/năm, cao hơn EVN 4,7%). Đã chốt: thay hệ số cố định 2,7 kWh/kWp/ngày bằng `PVout năm của tỉnh / 365` (Hà Nội 2,50; TP HCM 3,91; Lâm Đồng 4,34); sản lượng tháng nói với khách = kWp × PVout tháng đó.

**Miền giá (34 tỉnh sau sáp nhập, Nghị quyết 202/2025/QH15), đã chốt:**
- Bắc (giá HN, link `slmsolar.vn`): 15 tỉnh Bắc Bộ (Tuyên Quang, Cao Bằng, Lai Châu, Lào Cai, Thái Nguyên, Điện Biên, Lạng Sơn, Sơn La, Phú Thọ, Bắc Ninh, Quảng Ninh, Hà Nội, Hải Phòng, Hưng Yên, Ninh Bình) và 5 tỉnh Bắc Trung Bộ (Thanh Hóa, Nghệ An, Hà Tĩnh, Quảng Trị, Huế).
- Nam (giá HCM, link `slmsolar.com.vn`): Đà Nẵng trở vào (Đà Nẵng, Quảng Ngãi, Gia Lai, Đắk Lắk, Khánh Hòa, Lâm Đồng, Đồng Nai, Tây Ninh, TP Hồ Chí Minh, Đồng Tháp, An Giang, Vĩnh Long, Cần Thơ, Cà Mau).
- Khách hay nói tên tỉnh cũ: backend đổi 63 tỉnh cũ sang 34 tỉnh mới (ví dụ Bình Dương, Bà Rịa - Vũng Tàu → TP Hồ Chí Minh; Quảng Nam → Đà Nẵng; Hà Nam, Nam Định → Ninh Bình).

**Công thức chọn gói tạm (doanh nghiệp đồng ý "dùng tạm công thức chung"):**
1. Điện dùng mỗi ngày `E = hoá đơn tháng / 3.500 / 30` (kWh).
2. Tỷ lệ dùng đêm theo `ty_le_ngay_dem`: ban ngày nhiều 30%, cân bằng 50%, buổi tối nhiều 70%.
3. Pin cần `= E × tỷ lệ đêm / 0,9` (xả sâu 90%); chọn gói có dung lượng lưu trữ nhỏ nhất ≥ con số này, không có thì lấy lớn nhất và ghi chú.
4. Công suất cần `kWp = E / (PVout năm của tỉnh / 365)`; chọn gói có kWp nhỏ nhất ≥ 90% công suất cần; biết diện tích mái thì giới hạn `kWp ≤ S / 5`.
5. Loại hệ: dùng đêm ≥ 50% hoặc khách cần dự phòng mất điện → Hybrid; ban ngày nhiều → On-Grid (rẻ hơn), kèm phương án Hybrid pin nhỏ.
6. Ví dụ: Hà Nội, hoá đơn 2 triệu, dùng tối nhiều → E ≈ 19 kWh/ngày; pin ≈ 14,8 kWh → 16 kWh; kWp ≈ 7,6 → gói 8,75 kWp 1 pha Hybrid 16 kWh (gói 105, 144,2 triệu).

**Sản lượng nói với khách (đã chốt):** tính theo PV out EVN của tỉnh (nhất quán, có nguồn chính thức), kể cả sản lượng của từng gói = kWp của gói × PV out; không đọc `san_luong_toi_thieu / toi_da` của slm_v2 cho tới khi được rà lại.



## 60.4 Mốc báo giá (viết 2026-10-28, chạy thật từ 2026-10-29)

**Đồng bộ catalog** (`services/catalog`, `adapters/slmv2`, migration V7):
- `CatalogSyncJob` mỗi 10 phút (lần đầu 30 giây sau khởi động) và nút "Đồng bộ ngay" trên trang *Catalog & báo giá → Gói từ slm_v2*. Đọc 12 view `ai_v_*` (tài khoản theo `SLM_V2_DB_USERNAME`, hiện là `postgres` theo quyết định của doanh nghiệp 2026-10-29) trong một transaction REPEATABLE READ, READ ONLY; mỗi lần mở một kết nối rồi đóng.
- Ghi `dm_thuong_hieu`, `dm_co_so`, `dm_vat_tu`, `dm_tron_goi`, `dm_vat_tu_tron_goi`, `dm_ten_mien`, lịch sử ở `dm_lich_su_dong_bo`. Upsert theo `id_nguon`, bỏ qua dòng có `ma_bam` không đổi; dòng biến mất ở nguồn thì ngừng, không xoá.
- `dm_tron_goi` có thêm cột tính từ thành phần: `so_tam_pin`, `cong_suat_tam_wp`, `cong_suat_inverter_kw`, `luu_tru_kwh` (cộng `dung_luong` của nhóm `PIN_LUU_TRU`).
- `dm_gia_vat_tu` (giá lẻ vật tư) và tool `tra_cuu_vat_tu` để sau: chưa có nhu cầu.
- An toàn: lỗi thì rollback, giữ bản cũ, báo quản trị (`CATALOG_SYNC_FAILED`, tối đa một lần mỗi ngày). Không ghi khi slm_v2 trả 0 gói đang bán hoặc số gói giảm quá một nửa.

**Tỉnh và PV out** (`services/sizing`):
- `data/tinh-thanh.json`: 34 tỉnh, 63 tỉnh cũ, tên thường gọi (Sài Gòn, Vũng Tàu, Nha Trang, Đà Lạt...), miền và cơ sở giá. Tìm tỉnh trong địa chỉ dài, ưu tiên tên đứng sau cùng; không nhận ra thì không đoán.
- `nl_bang_pvout` + `nl_pvout_tinh`: bảng PV out theo năm, đã nạp bảng EVN 2026. Năm chưa có bảng thì dùng bảng gần nhất trước đó.
- **Nạp lại mỗi năm** (doanh nghiệp yêu cầu 2026-10-28): trang *Catalog & báo giá → PV out EVN* cho dán bảng của công văn mới, kiểm tra đủ 34 tỉnh × 12 tháng, so với năm trước rồi mới lưu, ghi nhật ký thao tác. `PvoutReminderJob` báo quản trị (`DATA_UPDATE_DUE`): từ 1/12 nhắc chuẩn bị bảng năm sau; từ 1/2 năm mới vẫn chưa có thì cảnh báo bot đang dùng bảng năm trước.

**Tool cho Claude** (`CustomerChatTools`, `PackageAdvisor`), schema strict, vòng lặp tối đa 3 lần gọi tool mỗi lượt (vòng sau tắt tool):
- `tim_goi_phu_hop`: chưa có tiền điện → giá khởi điểm theo loại hệ / số pha của miền khách và gói bán chạy; có tiền điện → gói rẻ nhất đáp ứng công thức 60.3, chưa biết tỉnh hoặc ngày / đêm thì trả khoảng (gói nhỏ nhất và lớn nhất). Kèm sản lượng theo PV out tháng của tỉnh, tiền tiết kiệm và hoàn vốn ước tính (điện tự dùng × 3.500 đ/kWh, không tính bán điện dư), link theo miền, điều kiện báo giá, thông tin còn thiếu. Trường null được điền từ hồ sơ tư vấn.
- `chi_tiet_goi`: thành phần hiển thị cho khách, hãng, bảo hành, giá, link, sản lượng; gói của miền kia thì đổi sang gói cùng tên ở miền của khách.
- Mọi con số trả về kèm chuỗi viết sẵn ("144,2 triệu đồng", "8,75 kWp") để AI chép nguyên văn. Policy coi kết quả tool, tin trang đã gửi là nguồn con số. Link chỉ được gửi khi có trong kết quả tool, tin trang đã gửi, hoặc là trang chủ theo miền; link khác thì DRAFT (`LINK_NOT_ALLOWED`).
- Lượt có gửi báo giá ghi `ho_so_tu_van.tron_goi_de_xuat_ids` và `gia_thap_nhat_da_bao`, để tính `OFF_SEGMENT` và cờ ngân sách thấp (mục 58.5). Lead có thêm miền (`khach_tiem_nang.mien`).
- Catalog chưa đồng bộ lần nào thì không đưa tool vào prompt; ngữ cảnh lượt báo "chưa có công cụ báo giá", AI xin số điện thoại như trước.
- Playbook `khach-hoi-gia`: báo giá sớm rồi hỏi đúng một câu, ví dụ "Không biết hoá đơn tiền điện nhà mình khoảng bao nhiêu một tháng để em tư vấn thêm ạ?".

**Sửa, tắt, xoá mềm gói trên admin** (V8, 2026-10-29, theo khuôn trang của fsignai):
- `dm_tron_goi` vẫn là bản sao y nguyên slm_v2 (slm_v2 chỉ đọc). Điều chỉnh của admin nằm riêng ở `dm_tron_goi_dieu_chinh` (tên, loại hệ, số pha, kWp, số tấm, Wp, kWh, giá, bán chạy, cờ `tat`, ghi chú, `xoa_luc`); `CatalogSyncJob` không ghi vào bảng này nên đồng bộ không đè mất.
- `CatalogRepository` ghép hai bảng khi đọc: tool báo giá và trang admin cùng thấy một số liệu. Trường admin sửa thì bỏ cảnh báo về chính trường đó, thiếu pin và tên lệch số pha tính lại; gói được gợi ý khi đang bán ở slm_v2, không tắt, không xoá và không còn cảnh báo chặn.
- Trường nhập trùng số liệu slm_v2 lưu NULL, để khi slm_v2 sửa thì bot nhận ngay. Form sửa ghi số liệu slm_v2 cạnh từng ô.
- API: `GET/PUT /admin/catalog/packages/{id}`, `POST /packages/set-active`, `DELETE /packages/{id}` (xoá mềm), `POST /packages/{id}/restore`; quản trị hệ thống và quản trị nội dung, nhân viên trực chat chỉ xem. Mọi thao tác ghi nhật ký (`catalog.package.*`).

**Quyết định của doanh nghiệp (2026-10-26, 2026-10-28), bot báo giá tất cả gói đang bán trong slm_v2:**
1. **Hoá đơn → kWh:** giá phẳng **3.500 đ/kWh** (bảng công thức), không dùng biểu giá bậc thang.
2. **Giá:** theo `tong_gia` trong slm_v2, cơ sở theo miền của khách (HN = miền Bắc, HCM = miền Nam). Mái bằng: `gia_khung_sat` đang 0 nên AI nói "phát sinh phần khung, nhân viên báo sau khảo sát".
3. **`san_luong_toi_thieu / toi_da`:** là sản lượng điện (kWh/tháng) tối thiểu và tối đa gói có thể đạt. Lưu ý dữ liệu: quy ra kWh/kWp/tháng dao động 52–140 giữa các gói, có cặp gói cùng số tấm nhưng khoảng sản lượng đảo nhau giữa HN và HCM (98/99 so với 97/100), và cao hơn mức PV out tối đa của tháng nắng nhất (ví dụ 5 kWp ở Hà Nội tối đa khoảng 550 kWh/tháng theo EVN, slm_v2 ghi 700).
4. **Bot được gửi link:** khách miền Nam → `slmsolar.com.vn`, miền Bắc → **chỉ `slmsolar.vn`** (doanh nghiệp đổi ngày 2026-10-28, bỏ phương án ngẫu nhiên với `slmsolar.com`), ví dụ `https://slmsolar.vn/products/158`. Khớp bảng `mien` của slm_v2 (`ten_mien` → `co_so_id`; `thong_tin_ten_mien` có hotline, email theo tên miền). Link sản phẩm: `https://<tên miền>/products/<tron_goi.id của cơ sở đó>`; đồng bộ thêm view `ai_v_mien`.
5. **Công thức pin:** chưa có, dùng tạm công thức chung (đề xuất ở 60.3).
6. **Đã chốt thêm (2026-10-26):** miền giá chia tại Đà Nẵng (Đà Nẵng trở vào là Nam); sản lượng và tiền tiết kiệm tính theo PV out EVN của tỉnh khách, kể cả sản lượng của từng gói (không đọc `san_luong_toi_thieu / toi_da` cho tới khi slm_v2 rà lại); công thức chọn gói tạm ở 60.3.

**Doanh nghiệp đã chốt (2026-10-29):** `tong_gia` đã gồm VAT, câu điều kiện báo giá (`app.pricing.quote-conditions`) nói rõ "giá niêm yết đã gồm VAT"; cả 34 tỉnh đều phục vụ, nên chưa cần cờ `serviced` (mục 4.2.9).
