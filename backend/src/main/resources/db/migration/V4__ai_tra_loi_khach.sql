-- Mốc 2: AI trả lời khách (mục 18, 19, 33, 35, 58). Bổ sung cột cho các bảng của V3.

-- Lượt đầu sau khi tự trả hội thoại lại cho AI đi theo DRAFT (mục 19); xoá khi lượt đó được chốt
ALTER TABLE hoi_thoai
    ADD COLUMN tu_tra_lai_luc TIMESTAMPTZ;                   -- [EN: auto_handback_at]

-- Tự trả lại AI: tìm hội thoại HUMAN_OWNED đã quá N giờ kể từ tin cuối của nhân viên
CREATE INDEX idx_hoi_thoai_human_owned ON hoi_thoai (nhan_vien_tin_cuoi_luc, tiep_quan_luc)
    WHERE nguoi_phu_trach = 'HUMAN_OWNED' AND trang_thai = 'OPEN';

-- Câu AI vừa hỏi khách: tin sau của khách trả lời đúng câu này thì so_cau_da_tra_loi tăng (mục 58.5)
ALTER TABLE ho_so_tu_van
    ADD COLUMN cau_hoi_cuoi VARCHAR(50);                     -- [EN: last_question_asked]

ALTER TABLE luot_goi_ai
    ADD COLUMN ket_qua_ai JSONB,                             -- [EN: ai_output] kết quả có cấu trúc (mục 18), để kiểm tra và làm bộ test
    ADD COLUMN ly_do_dung VARCHAR(40),                       -- [EN: stop_reason] end_turn, refusal, max_tokens...
    ADD COLUMN loi TEXT;                                     -- [EN: error] lỗi gọi Claude hoặc lỗi đọc kết quả
