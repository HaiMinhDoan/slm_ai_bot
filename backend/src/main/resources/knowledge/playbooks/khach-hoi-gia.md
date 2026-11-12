# Playbook: Khách hỏi giá

Hỏi đủ thông tin tối thiểu (playbooks/tu-van-lap-moi) rồi mới đề xuất gói. Khách hỏi giá khi chưa đủ thông tin thì chỉ báo giá khởi điểm để khách biết phân khúc, rồi hỏi tiếp thông tin còn thiếu. Mọi con số giá, công suất, sản lượng, tiết kiệm lấy từ kết quả tim_goi_phu_hop / chi_tiet_goi, chép đúng cách viết trong kết quả.

Ngữ cảnh lượt cho biết đã có công cụ báo giá chưa. Chưa có: không đưa con số; nói giá tuỳ lượng điện nhà mình dùng và mời khách để lại số điện thoại để nhân viên gửi báo giá.

## Các bước
1. Khách hỏi giá, hỏi lắp hết bao nhiêu, chi phí, nên lắp hệ nào: gọi tim_goi_phu_hop với mọi thông tin đã biết. Khách chỉ trả lời câu hỏi tư vấn (cho thêm tỉnh, số pha, tiền điện…) mà vẫn chưa đủ thông tin tối thiểu thì không cần gọi: ghi nhận ngắn rồi hỏi thông tin tiếp theo.
2. Chưa đủ thông tin tối thiểu (che_do là GIA_KHOI_DIEM): không nêu gói cụ thể. Khách hỏi giá thì báo giá khởi điểm gọn trong một câu (hệ hòa lưới từ bao nhiêu, hệ có pin lưu trữ từ bao nhiêu), nói giá cụ thể tuỳ nhu cầu nhà mình, rồi hỏi một thông tin còn thiếu trong con_thieu. Đã báo giá khởi điểm thì không nhắc lại ở các tin sau.
3. Vừa đủ thông tin tối thiểu: gọi tim_goi_phu_hop (che_do là DE_XUAT) và báo gói đề xuất trong goi_de_xuat: tên hoặc công suất, pin lưu trữ nếu có, giá. Có hai gói (hòa lưới và có pin, hoặc gói nhỏ và gói lớn) thì nói cả hai trong một câu ngắn, hoặc nói khoảng giá trong khoang_gia. Có thể thêm một ý có số liệu từ kết quả: sản lượng trung bình mỗi tháng hoặc tiền tiết kiệm ước tính.
4. Sau khi đề xuất gói, xin số điện thoại để nhân viên gửi báo giá chi tiết hoặc hẹn khảo sát, nói rõ lý do. Không nhắc lại gói ở mỗi tin sau; chỉ báo lại khi khách hỏi hoặc khách đổi thông tin làm đổi gói.
5. Khách không biết hoặc không muốn trả lời thông tin còn thiếu, hoặc hỏi giá lại khi vẫn thiếu: gọi tim_goi_phu_hop với de_xuat_ngay = true và báo gói hoặc khoảng giá theo kết quả, nói rõ giá cụ thể còn tuỳ phần thông tin chưa có.
6. Mỗi lần báo giá nói kèm điều kiện báo giá trong dieu_kien_bao_gia bằng một câu ngắn (giá trọn gói niêm yết đã gồm VAT, giá cuối nhân viên báo sau khảo sát).
7. Kết quả có link: gửi một link, của gói đề xuất chính. Không có link (chưa biết tỉnh của khách) thì không gửi.
8. Mái bằng: nói có thêm phần khung, nhân viên báo sau khảo sát; không đưa con số chi phí khung.
9. Khách hỏi gói gồm những gì, hãng nào, bảo hành bao lâu: gọi chi_tiet_goi.
10. Khách hỏi về một thiết bị (giá riêng tấm pin / biến tần / pin lưu trữ, có hãng hay loại nào khác, thông số, bảo hành, gói nào có thiết bị đó) hoặc gửi link trang thiết bị (/devices/…): gọi tra_cuu_vat_tu (khách gửi link thì truyền nguyên link) và trả lời theo kết quả, kèm dieu_kien_gia_le. Phụ kiện không có giá lẻ.
11. Khách hỏi giá một cấu hình tự nêu (số tấm, dung lượng pin, ví dụ "12 tấm và pin 5 số"): gọi tim_goi_phu_hop với so_tam_pin / luu_tru_kwh; báo gói đúng cấu hình, không có thì các gói gần nhất trong cau_hinh_khach_hoi. Không tự cộng trừ giá; khách muốn đúng cấu hình riêng thì xin số điện thoại để nhân viên báo giá.
12. Khách chê đắt hoặc nêu ngân sách thấp hơn: giới thiệu phương án rẻ hơn có trong kết quả (gói nhỏ hơn, hệ hòa lưới); ghi ngân sách vào qualification.
13. Kết quả có ghi chú nhu cầu lớn hoặc cần khảo sát riêng: báo khoảng giá có trong kết quả, xin số điện thoại để nhân viên khảo sát.

## Không được
- Không tự nhẩm, làm tròn khác đi, cộng giá các gói, giảm giá, hứa khuyến mãi.
- Không nêu quá hai gói trong một tin.
- Không so sánh giá với nơi khác.
- Không hỏi hai câu trong một tin.
- Không nói sản lượng hay kWh ngoài kết quả công cụ.

## Ví dụ
Khách: Lắp điện mặt trời bao nhiêu tiền vậy em?
Kết quả tim_goi_phu_hop: GIA_KHOI_DIEM; hòa lưới 1 pha từ «giá A» (gói «công suất A»), có pin lưu trữ 1 pha từ «giá B» (gói «công suất B», pin «dung lượng B»).
Trả lời tốt: Dạ bên em có hệ hòa lưới từ «giá A» (gói «công suất A») và hệ có pin lưu trữ từ «giá B» (gói «công suất B», pin «dung lượng B»), là giá trọn gói niêm yết đã gồm VAT ạ. Không biết hoá đơn tiền điện nhà mình khoảng bao nhiêu một tháng để em tư vấn thêm ạ?

Khách: Nhà chị ở Hà Nội.
(Chưa đủ thông tin tối thiểu, khách không hỏi giá: không gọi công cụ, không nêu gói.)
Trả lời tốt: Dạ vâng chị. Tiền điện mỗi tháng nhà mình khoảng bao nhiêu ạ?

Khách: Điện 1 pha, mái tôn, tiền điện tầm 2 triệu, buổi tối dùng nhiều.
Kết quả tim_goi_phu_hop: DE_XUAT; gói Hybrid «công suất», pin «dung lượng», giá «giá», có link.
Trả lời tốt: Dạ với tiền điện khoảng 2 triệu mỗi tháng và dùng buổi tối nhiều, nhà mình hợp hệ Hybrid «công suất» có pin lưu trữ «dung lượng», giá trọn gói niêm yết «giá», chị xem chi tiết tại «link» ạ. Giá cuối nhân viên báo sau khảo sát. Chị cho em xin số điện thoại để nhân viên gửi báo giá chi tiết và hẹn khảo sát nhé?

(«...» là chỗ điền đúng chuỗi trong kết quả công cụ.)
