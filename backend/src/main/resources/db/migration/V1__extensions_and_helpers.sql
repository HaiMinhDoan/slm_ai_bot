-- =====================================================================
-- V1 — Extension và hàm tiện ích dùng chung
-- =====================================================================
-- Quy ước (mục 29 của kế hoạch): tên bảng / cột tiếng Việt không dấu, snake_case;
-- mốc thời gian TIMESTAMPTZ, kết thúc bằng _luc; enum dùng VARCHAR + CHECK (không dùng ENUM của PostgreSQL).
-- gen_random_uuid() có sẵn từ PostgreSQL 13, không cần pgcrypto.
-- =====================================================================

CREATE EXTENSION IF NOT EXISTS unaccent;   -- bỏ dấu tiếng Việt khi tìm kiếm (mục 5.7, 7.4)
CREATE EXTENSION IF NOT EXISTS pg_trgm;    -- tìm gần đúng, gõ sai chính tả


-- ---------------------------------------------------------------------
-- f_unaccent: bọc unaccent() thành IMMUTABLE để dùng được trong generated column và index.
-- Chỉ định thẳng từ điển 'public.unaccent' nên kết quả không phụ thuộc search_path.
-- Đối xứng với VietnameseTextUtil.unaccent() ở tầng Java.
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION f_unaccent(text)
    RETURNS text
    LANGUAGE sql
    IMMUTABLE
    PARALLEL SAFE
    STRICT
AS $$
SELECT public.unaccent('public.unaccent', $1)
$$;


-- ---------------------------------------------------------------------
-- Tự cập nhật sua_luc mỗi lần UPDATE, kể cả khi sửa bằng SQL trực tiếp.
-- ---------------------------------------------------------------------
CREATE OR REPLACE FUNCTION dat_sua_luc()
    RETURNS trigger
    LANGUAGE plpgsql
AS $$
BEGIN
    NEW.sua_luc = now();
    RETURN NEW;
END;
$$;

-- Gắn trigger dat_sua_luc cho một bảng có cột sua_luc. Gọi ở cuối mỗi migration tạo bảng mới.
CREATE OR REPLACE FUNCTION gan_trigger_sua_luc(p_bang text)
    RETURNS void
    LANGUAGE plpgsql
AS $$
BEGIN
    EXECUTE format(
        'DROP TRIGGER IF EXISTS trg_%1$s_sua_luc ON %1$I;
         CREATE TRIGGER trg_%1$s_sua_luc
             BEFORE UPDATE ON %1$I
             FOR EACH ROW EXECUTE FUNCTION dat_sua_luc();',
        p_bang);
END;
$$;
