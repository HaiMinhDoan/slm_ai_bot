-- V17: bot nhắc khách khi khách im lặng (2026-11-25, mục 56.9, 60.2 số 6). Bản rút gọn của lich_follow_up (mục 56.6):
-- mỗi dòng là một lần nhắc trong cửa sổ 24 giờ của Messenger, hoặc một lần báo nhân viên khi cửa sổ đã đóng mà khách
-- vẫn im lặng. Xoá hội thoại / khách thì xoá theo.

-- EN: customer_follow_ups — follow-up nudges and staff alerts for silent customers
CREATE TABLE lich_nhac_khach (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hoi_thoai_id      UUID NOT NULL REFERENCES hoi_thoai(id) ON DELETE CASCADE,     -- [EN: conversation_id]
    khach_hang_id     UUID NOT NULL REFERENCES khach_hang(id) ON DELETE CASCADE,    -- [EN: customer_id]
    loai              VARCHAR(16) NOT NULL                                          -- [EN: kind]
                      CHECK (loai IN ('NHAC_KHACH','BAO_NHAN_VIEN')),
    trang_thai        VARCHAR(16) NOT NULL DEFAULT 'PENDING'                        -- [EN: status]
                      CHECK (trang_thai IN ('PENDING','SENT','DRAFT','SKIPPED','CANCELLED','FAILED')),
    lan_thu           INT NOT NULL DEFAULT 1,                                       -- [EN: attempt_no] lần nhắc thứ mấy trong cửa sổ
    moc_tin_khach     TIMESTAMPTZ NOT NULL,                                         -- [EN: window_anchor] tin_khach_cuoi_luc lúc lập lịch
    khoa_chong_trung  VARCHAR(160) NOT NULL UNIQUE,                                 -- [EN: idempotency_key]
    tin_nhan_id       UUID REFERENCES tin_nhan(id) ON DELETE SET NULL,              -- [EN: message_id] tin nhắc đã ghi
    luot_goi_ai_id    UUID REFERENCES luot_goi_ai(id) ON DELETE SET NULL,           -- [EN: ai_run_id]
    ly_do             TEXT,                                                         -- [EN: reason] lý do bỏ / huỷ / lỗi
    xu_ly_luc         TIMESTAMPTZ,                                                  -- [EN: processed_at]
    tao_luc           TIMESTAMPTZ NOT NULL DEFAULT now(),                           -- [EN: created_at]
    sua_luc           TIMESTAMPTZ NOT NULL DEFAULT now()                            -- [EN: updated_at]
);
SELECT gan_trigger_sua_luc('lich_nhac_khach');

CREATE INDEX idx_lich_nhac_khach_hoi_thoai ON lich_nhac_khach (hoi_thoai_id, tao_luc DESC);
CREATE INDEX idx_lich_nhac_khach_cho ON lich_nhac_khach (hoi_thoai_id) WHERE trang_thai = 'PENDING';
CREATE INDEX idx_lich_nhac_khach_tin_nhan ON lich_nhac_khach (tin_nhan_id) WHERE tin_nhan_id IS NOT NULL;

-- Lượt AI viết tin nhắc
ALTER TABLE luot_goi_ai DROP CONSTRAINT luot_goi_ai_muc_dich_check;
ALTER TABLE luot_goi_ai ADD CONSTRAINT luot_goi_ai_muc_dich_check
    CHECK (muc_dich IN ('CUSTOMER_CHAT','CONTENT_ASSISTANT','DAILY_BRIEF','KNOWLEDGE_ASSISTANT','RELEASE_EVAL',
                        'FOLLOW_UP'));

-- Khách im lặng tới khi hết cửa sổ 24 giờ: báo nhân viên
ALTER TABLE thong_bao DROP CONSTRAINT thong_bao_loai_check;
ALTER TABLE thong_bao ADD CONSTRAINT thong_bao_loai_check
    CHECK (loai IN ('AI_HANDOFF','AI_DRAFT','HANDBACK_REPLY','LEAD_HOT','SEND_FAILED','DUPLICATE_PHONE',
                    'CATALOG_SYNC_FAILED','DATA_UPDATE_DUE','AI_KILL_SWITCH','KNOWLEDGE_RELEASE',
                    'STAFF_HANDOFF','CUSTOMER_WAITING','CUSTOMER_SILENT'));
