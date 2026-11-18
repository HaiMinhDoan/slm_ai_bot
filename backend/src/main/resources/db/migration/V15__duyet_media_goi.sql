-- V15: duyệt media gói đã nhập (24 ảnh báo giá từ Google Drive, 11 video Facebook; 2026-10-29) để bot gửi được cho
-- khách (2026-11-18, người dùng yêu cầu "bỏ nháp những media đi"). Duyệt cần tóm tắt và "dùng khi"
-- (chk_da_duyet_co_tom_tat): điền câu mô tả theo tên gói, admin sửa lại được trên trang Media.
-- Chỉ chạm media còn Nháp, chưa xoá, chưa có tóm tắt / dùng khi, quyền sử dụng đã rõ và có gắn với gói;
-- CSDL mới (chưa nhập media) thì không đổi gì.

CREATE TEMP TABLE v15_duyet ON COMMIT DROP AS
SELECT m.id,
       m.loai_media,
       m.tieu_de,
       regexp_replace(m.tieu_de, '^Video:\s*', '') AS ten_goi,
       (SELECT COALESCE(max(substring(x.ma_danh_muc FROM 2)::int), 0)
        FROM media x WHERE x.ma_danh_muc ~ '^M[0-9]+$')
           + row_number() OVER (ORDER BY m.loai_media, m.tieu_de, m.tao_luc) AS so_ma
FROM media m
WHERE m.trang_thai = 'DRAFT'
  AND m.xoa_luc IS NULL
  AND m.tom_tat IS NULL
  AND m.dung_khi IS NULL
  AND m.ma_danh_muc IS NULL
  AND m.quyen_su_dung <> 'NEEDS_REVIEW'
  AND EXISTS (SELECT 1 FROM media_lien_ket l WHERE l.media_id = m.id AND l.loai_doi_tuong = 'PACKAGE');

UPDATE media m SET
    tom_tat = CASE WHEN d.loai_media = 'IMAGE'
        THEN 'Ảnh báo giá ' || d.ten_goi || ': tấm pin, biến tần, pin lưu trữ (nếu có), giá trọn gói và danh mục thiết bị.'
        ELSE 'Video công trình thực tế lắp ' || d.ten_goi || '.' END,
    dung_khi = CASE WHEN d.loai_media = 'IMAGE'
        THEN 'Gửi kèm khi đề xuất hoặc báo giá gói ' || d.ten_goi || ' (hệ thống tự gửi theo link gói).'
        ELSE 'Khách muốn xem công trình thực tế, hỏi lắp xong trông thế nào, hoặc vừa được đề xuất gói '
             || d.ten_goi || ' hay gói cùng loại.' END,
    tranh_khi = CASE WHEN d.loai_media = 'VIDEO'
        THEN 'Khách đang phàn nàn, báo sự cố hoặc hỏi bảo hành.' END,
    chu_de = ARRAY(SELECT DISTINCT unnest(m.chu_de || ARRAY[
        CASE WHEN d.loai_media = 'IMAGE' THEN 'bang-gia' ELSE 'cong-trinh' END,
        CASE WHEN d.tieu_de ILIKE '%hy-brid%' OR d.tieu_de ILIKE '%hybrid%' THEN 'hybrid' ELSE 'on-grid' END]::text[])
        ORDER BY 1),
    ma_danh_muc = 'M' || lpad(d.so_ma::text, 2, '0'),
    trang_thai = 'APPROVED',
    nguoi_duyet = 'system (V15)',
    duyet_luc = now(),
    sua_boi = 'system (V15)'
FROM v15_duyet d
WHERE m.id = d.id;

INSERT INTO nhat_ky_thao_tac (hanh_dong, loai_doi_tuong, ma_doi_tuong, du_lieu_truoc, du_lieu_sau)
SELECT 'media.update', 'media', m.id::text,
       jsonb_build_object('trang_thai', 'DRAFT'),
       jsonb_build_object('trang_thai', m.trang_thai, 'ma_danh_muc', m.ma_danh_muc, 'tom_tat', m.tom_tat,
                          'dung_khi', m.dung_khi, 'chu_de', m.chu_de, 'nguon', 'V15__duyet_media_goi')
FROM media m JOIN v15_duyet d ON d.id = m.id;
