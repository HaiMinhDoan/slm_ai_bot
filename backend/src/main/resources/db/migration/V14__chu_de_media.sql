-- Trang quản lý media đầy đủ (2026-11-12): bộ chủ đề cố định sửa trên admin (mục 6.2, dùng chung cho media và
-- tài liệu kiến thức), ảnh thu nhỏ của video tải lên (trình duyệt cắt một khung hình lúc tải lên).

-- EN: topics — fixed topic list for media and knowledge docs
CREATE TABLE chu_de (
    ma          VARCHAR(50) PRIMARY KEY,                -- [EN: code] chữ thường không dấu, gạch nối: pin-luu-tru
    ten         VARCHAR(100) NOT NULL,                  -- [EN: name]
    mo_ta       TEXT,                                   -- [EN: description] khi nào gắn chủ đề này
    thu_tu      INT NOT NULL DEFAULT 0,                 -- [EN: sort_order]
    dang_dung   BOOLEAN NOT NULL DEFAULT true,          -- [EN: active] tắt thì không chọn mới được, media cũ giữ nguyên
    sua_boi     VARCHAR(255),                           -- [EN: updated_by] email
    tao_luc     TIMESTAMPTZ NOT NULL DEFAULT now(),     -- [EN: created_at]
    sua_luc     TIMESTAMPTZ NOT NULL DEFAULT now(),     -- [EN: updated_at]
    CONSTRAINT chk_chu_de_ma CHECK (ma ~ '^[a-z0-9]+(-[a-z0-9]+)*$')
);
SELECT gan_trigger_sua_luc('chu_de');

INSERT INTO chu_de (ma, ten, mo_ta, thu_tu) VALUES
    ('hybrid', 'Hệ Hybrid', 'Hệ có pin lưu trữ, dùng điện buổi tối, dự phòng mất điện', 10),
    ('on-grid', 'Hệ hòa lưới', 'Hệ không pin, dùng điện ban ngày', 20),
    ('pin-luu-tru', 'Pin lưu trữ', 'Pin, dung lượng, thời gian dùng buổi tối', 30),
    ('tam-pin', 'Tấm pin', 'Tấm pin mặt trời, hãng, công suất', 40),
    ('bien-tan', 'Biến tần', 'Inverter, hãng, công suất', 50),
    ('lap-dat', 'Lắp đặt', 'Thi công, quy trình, thời gian lắp', 60),
    ('cong-trinh', 'Công trình thực tế', 'Nhà khách hàng đã lắp, case study', 70),
    ('bao-tri', 'Bảo trì, vệ sinh', 'Vệ sinh tấm pin, kiểm tra định kỳ', 80),
    ('bao-hanh', 'Bảo hành', 'Chính sách và thời hạn bảo hành', 90),
    ('hoa-don', 'Hoá đơn, tiết kiệm', 'Tiền điện trước và sau khi lắp, hoàn vốn', 100),
    ('bang-gia', 'Bảng giá, gói', 'Ảnh gói, combo, bảng giá', 110);

-- Ảnh thu nhỏ của nguồn video tải lên (object key trong MinIO); ảnh thì dùng chính file
ALTER TABLE media_nguon
    ADD COLUMN duong_dan_anh_thu_nho VARCHAR(500);         -- [EN: thumbnail_object_key]
