# Hướng dẫn viết tài liệu kiến thức

Tài liệu này dành cho người soạn và cho AI hỗ trợ soạn. Bot chăm sóc khách không đọc tài liệu này.

## Bot đọc tài liệu thế nào

- Bot đọc toàn bộ tài liệu loại Cốt lõi, Cách tư vấn, Chính sách, Hỏi đáp, Chat mẫu trong mỗi lượt trả lời, theo thứ tự mã tài liệu.
- Khi các tài liệu mâu thuẫn: Giới hạn bắt buộc (core/guardrails) và Chính sách thắng; sau đó tới Cách tư vấn và Hỏi đáp.
- Bot chỉ dùng bản đã phát hành và kích hoạt. Bản nháp không ảnh hưởng tới khách.

## Quy tắc viết

- Một tài liệu một chủ đề; tiêu đề (dòng `# ...` đầu tiên) nói rõ tình huống.
- Viết quy tắc rõ ràng: "Khi … thì …", "Luôn …", "Không bao giờ …", kèm lý do ngắn, vì AI làm theo tốt hơn khi hiểu vì sao.
- Câu ngắn; các bước viết thành danh sách đánh số.
- Có ví dụ câu trả lời mẫu đúng văn phong (xưng em, gọi khách anh/chị, mỗi tin một câu hỏi).
- Ghi rõ khi nào phải chuyển nhân viên.
- Viết tiếng Việt có dấu; dùng đúng thuật ngữ trong guide/thuat-ngu.

## Không ghi con số

- Không ghi giá, công suất, sản lượng, thời gian hoàn vốn, thông số cụ thể của gói hay vật tư. Các số này bot lấy từ bảng giá (slm_v2) qua công cụ báo giá, nên luôn đúng bảng giá hiện hành.
- Số trong tài liệu được bot coi là nguồn hợp lệ khi kiểm tra con số, nên một con số cũ ghi trong tài liệu có thể lọt ra ngoài.
- Thời hạn bảo hành, điều kiện thanh toán… chỉ ghi khi là chính sách chính thức của công ty.

## Tên công ty và tên tư vấn viên

- Viết `{{ten_cong_ty}}` và `{{ten_tu_van_vien}}` thay cho tên thật; hệ thống thay bằng tên trong Cài đặt AI.

## Mã tài liệu

| Thư mục | Loại | Dùng cho |
|---|---|---|
| core/ | Cốt lõi | Danh tính, văn phong, giới hạn bắt buộc |
| playbooks/ | Cách tư vấn | Cách dẫn dắt một tình huống |
| policies/ | Chính sách | Bảo hành, thanh toán, quy trình lắp đặt, bảo trì |
| faq/ | Hỏi đáp | Câu hỏi khách hay hỏi |
| examples/ | Chat mẫu | Đoạn chat tốt làm mẫu văn phong |

Mã viết thường, không dấu, nối bằng gạch ngang: `policies/bao-hanh`, `faq/pin-luu-tru`.
