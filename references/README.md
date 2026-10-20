# Repo / MCP tham khảo

Bản clone các repo và MCP được nhắc trong kế hoạch (`AI_Customer_Care_Sales_Content_Architecture_v3_Java.md`, mục 46.1 và 54.2), dùng để **đọc và port sang adapter Java + Spring Boot**.

## Quy tắc

- **Chỉ đọc.** Không `npm install`, `pip install`, `go build`, `mvn`, không chạy server MCP, không chạy file `.jar` / binary có sẵn trong các thư mục này.
- **Không import** code ở đây vào ứng dụng. Adapter Java gọi API chính thức của nền tảng (mục 46.1).
- **License:** repo ghi "không có" thì chỉ được đọc để hiểu cách API hoạt động, không copy code. Repo MIT được copy có ghi nguồn, nhưng vẫn ưu tiên viết lại theo hành vi của API. Ghi nguồn tham khảo vào file `NOTICE` của ứng dụng.
- **Khi port:** ghi bảng đối chiếu "hàm trong repo → method Java" vào `docs/porting/<ten-repo>.md`.
- Các thư mục clone **được commit vào git dưới dạng file thường**: `.git` của từng repo đã bị xóa, commit gốc ghi ở `<thư mục>/.ref-commit`. File build sẵn (`bin/`, `obj/`, `*.jar`, `*.dll`, `*.exe`) không commit (xem `references/.gitignore`).
- Repo ghi license "Không có" chỉ được để trong repo riêng tư; nếu repo này chuyển sang công khai thì phải gỡ các thư mục đó ra.

## Tạo lại / cập nhật

```bash
bash references/clone.sh            # clone những repo còn thiếu
bash references/clone.sh --update   # tải lại toàn bộ lên commit mới nhất, rồi commit thay đổi
```

## Cấu trúc

```text
references/
├── facebook/
│   ├── meta-mcp-server/          Page, Messenger, comment, insights (ưu tiên)
│   └── facebook-mcp-server/      Page (phương án thay thế, Python, nhỏ, dễ đọc)
├── zalo/
│   ├── zalo-java-sdk/            Zalo OA, SDK Java chính thức (cũ)
│   ├── redai-zalo-sdk/           Zalo OA API v3, SDK TypeScript cộng đồng (gói npm)
│   └── demo-e2ee-zns-csharp/     Demo mã hóa E2EE cho ZNS (C#)
├── tiktok/
│   ├── tiktok-business-api-docs/ OpenAPI của TikTok Business API
│   ├── tiktok-mcp-postonce/      Cấu hình MCP đăng video qua PostOnce
│   └── tiktok-mcp-hasdata/       MCP đọc dữ liệu TikTok công khai qua HasData
└── rui-ro-cao/                   Tùy chọn rủi ro cao, tắt mặc định (kế hoạch mục 54.6)
    ├── facebook-go/              Facebook Groups qua GraphQL nội bộ + cookie
    └── zca-js/                   Zalo cá nhân (không chính thức), chờ quyết định (mục 46.1)
```

## Danh sách

Commit / phiên bản tại thời điểm clone (2026-10-20). Chạy `clone.sh` để xem bản đang có.

| Thư mục | Nguồn | Commit | License | Dùng cho (mục trong kế hoạch) |
|---|---|---|---|---|
| `facebook/meta-mcp-server` | [oliverames/meta-mcp-server](https://github.com/oliverames/meta-mcp-server) | `4b84d43` (2026-10-08) | MIT | Facebook Page, Messenger, comment, insights (42, 54.3) |
| `facebook/facebook-mcp-server` | [HagaiHen/facebook-mcp-server](https://github.com/HagaiHen/facebook-mcp-server) | `61b2128` (2026-04-23) | MIT | Page: post, comment moderation, insights (44) |
| `zalo/zalo-java-sdk` | [zaloplatform/zalo-java-sdk](https://github.com/zaloplatform/zalo-java-sdk) | `a4bc0d4` (2025-07-29) | Không có | Zalo OA: nhắn tin, người quan tâm, bài viết (54.4) |
| `zalo/redai-zalo-sdk` | npm [@warriorteam/redai-zalo-sdk](https://www.npmjs.com/package/@warriorteam/redai-zalo-sdk) | `1.39.4` | MIT | Đối chiếu endpoint Zalo OA API v3 (54.4) |
| `zalo/demo-e2ee-zns-csharp` | [zaloplatform/demo-e2ee-zns-CSharp](https://github.com/zaloplatform/demo-e2ee-zns-CSharp) | `5c4f6a6` (2022-06-15) | Không có | Mã hóa E2EE cho ZNS (54.4) |
| `tiktok/tiktok-business-api-docs` | [justomat/tiktok-business-api-docs](https://github.com/justomat/tiktok-business-api-docs) | `f083dd1` (2026-05-18) | Không có | TikTok DM, lead, tài khoản (53, 57.7) |
| `tiktok/tiktok-mcp-postonce` | [postoncehq/tiktok-mcp](https://github.com/postoncehq/tiktok-mcp) | `006d9cc` (2026-10-15) | MIT | Luồng đăng / lên lịch video (55.5, 57.7) |
| `tiktok/tiktok-mcp-hasdata` | [HasData/tiktok-mcp](https://github.com/HasData/tiktok-mcp) | `35cd703` (2026-10-12) | MIT | Nghiên cứu nội dung công khai, tùy chọn (57.7) |
| `rui-ro-cao/facebook-go` | [teslashibe/facebook-go](https://github.com/teslashibe/facebook-go) | `78a3aee` (2026-06-13) | MIT | Facebook Groups, rủi ro cao (43, 54.6) |
| `rui-ro-cao/zca-js` | [RFS-ADRENO/zca-js](https://github.com/RFS-ADRENO/zca-js) | `0586dc5` (2026-10-13) | MIT | Zalo cá nhân, rủi ro cao, chờ quyết định (46.1, 54.6) |

## Nên đọc gì trong từng repo

| Thư mục | Nên đọc | Lưu ý |
|---|---|---|
| `facebook/meta-mcp-server` | `src/` (TypeScript), `docs/`: cách gọi Graph API cho Page, Messenger, comment, insights; xử lý lỗi và phân trang | Repo bao cả Instagram, Ads, Commerce…: chỉ port phần Page / Messenger cần dùng |
| `facebook/facebook-mcp-server` | `facebook_api.py`: lớp gọi Graph API gọn | Phạm vi nhỏ, dùng để đối chiếu |
| `zalo/zalo-java-sdk` | `src/`: tên endpoint, tham số, cách ký request của Zalo OA | Có thể đã cũ so với OA API hiện hành; **không dùng các file jar trong `lib/`** |
| `zalo/redai-zalo-sdk` | `README.md`, file `*.d.ts` trong `dist/`: danh sách endpoint, kiểu dữ liệu OA API v3 | Chỉ có bản đã build, không có mã nguồn gốc; repo GitHub không công khai |
| `zalo/demo-e2ee-zns-csharp` | `E2EEHelper.cs`, `HttpHelper.cs`: cách mã hóa nội dung ZNS | Chỉ cần khi dùng ZNS mã hóa E2EE; **không dùng `bin/`, `obj/`** |
| `tiktok/tiktok-business-api-docs` | `openapi.yaml`: endpoint, schema request/response | Luôn đối chiếu tài liệu TikTok chính thức; không có license nên không copy nguyên văn |
| `tiktok/tiktok-mcp-postonce` | `skills/`, `*.mcp.json`: các bước của luồng đăng / lên lịch | Không có mã nguồn gọi API; dịch vụ PostOnce không dùng ở production |
| `tiktok/tiktok-mcp-hasdata` | `hasdata_tiktok_mcp/`, `index.mjs`: các loại dữ liệu công khai lấy được | Gọi API trả phí của HasData; chỉ đọc, không đăng, không gửi DM |
| `rui-ro-cao/facebook-go` | `groups/`: luồng GraphQL nội bộ cho group | Trái điều khoản Meta, dễ hỏng khi Facebook đổi; chỉ đọc nếu doanh nghiệp quyết định bật tùy chọn này |
| `rui-ro-cao/zca-js` | `src/`, `examples/`: đăng nhập QR, gửi / nhận tin | Không chính thức, README cảnh báo có thể bị khóa tài khoản; chờ quyết định dùng Zalo OA hay không |
