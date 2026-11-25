-- V16: phân loại media (2026-11-25): QUOTE = ảnh báo giá gói, GENERAL = media thường. Lưu dạng chuỗi, không ràng buộc
-- danh sách trong CSDL để sau này thêm loại mới không cần migration (danh sách hợp lệ nằm ở
-- MediaAdminService.CATEGORIES và nhãn ở trang admin).

ALTER TABLE media
    ADD COLUMN phan_loai VARCHAR(32) NOT NULL DEFAULT 'GENERAL';    -- [EN: category]

-- Ảnh đang là ảnh chính của gói là ảnh báo giá (24 ảnh combo nhập từ Google Drive); còn lại là media thường
UPDATE media m SET phan_loai = 'QUOTE'
WHERE m.loai_media = 'IMAGE'
  AND EXISTS (SELECT 1 FROM media_lien_ket l
              WHERE l.media_id = m.id AND l.loai_doi_tuong = 'PACKAGE' AND l.vai_tro = 'MAIN_IMAGE');
