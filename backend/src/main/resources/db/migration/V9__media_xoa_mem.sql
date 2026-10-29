-- Sửa, xoá mềm media trên admin (2026-10-29). Xoá mềm giữ nguyên file trong MinIO, nguồn và liên kết,
-- để khôi phục được; danh sách admin mặc định ẩn mục đã xoá, danh mục media cho AI (mục 7.1) cũng phải bỏ qua.

ALTER TABLE media
    ADD COLUMN xoa_luc TIMESTAMPTZ,      -- [EN: deleted_at]
    ADD COLUMN xoa_boi VARCHAR(255),     -- [EN: deleted_by] email
    ADD COLUMN sua_boi VARCHAR(255);     -- [EN: updated_by] email người sửa gần nhất
