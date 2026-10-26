-- Thông báo cho nhân viên trên trang admin: âm thanh + thông báo trình duyệt + chuông (kế hoạch mục 34, 2026-10-26).
-- Ghi trong cùng transaction với nghiệp vụ, đẩy qua SSE sau khi commit.

-- EN: notifications — staff notifications (AI handoff, AI draft, HOT lead, send failure...)
CREATE TABLE thong_bao (
    id                BIGSERIAL PRIMARY KEY,
    loai              VARCHAR(32)  NOT NULL                                     -- [EN: type]
                      CHECK (loai IN ('AI_HANDOFF','AI_DRAFT','HANDBACK_REPLY','LEAD_HOT','SEND_FAILED','DUPLICATE_PHONE')),
    muc_do            VARCHAR(10)  NOT NULL DEFAULT 'INFO'                      -- [EN: severity]
                      CHECK (muc_do IN ('INFO','WARNING','URGENT')),
    tieu_de           VARCHAR(300) NOT NULL,                                    -- [EN: title]
    noi_dung          TEXT,                                                     -- [EN: body] không chứa số điện thoại
    du_lieu           JSONB NOT NULL DEFAULT '{}',                              -- [EN: payload] lý do, nháp AI, sđt (chỉ trả qua REST)
    nguoi_nhan_id     UUID REFERENCES nguoi_dung(id) ON DELETE CASCADE,         -- [EN: recipient_id] NULL = mọi STAFF và SYSTEM_ADMIN
    hoi_thoai_id      UUID,                                                     -- [EN: conversation_id]
    khach_hang_id     UUID,                                                     -- [EN: customer_id]
    khoa_chong_trung  VARCHAR(200) NOT NULL UNIQUE,                             -- [EN: dedupe_key] ví dụ AI_DRAFT:{tin_nhan_id}
    tao_luc           TIMESTAMPTZ  NOT NULL DEFAULT now()                       -- [EN: created_at]
);
CREATE INDEX idx_thong_bao_nguoi_nhan_id_id ON thong_bao (nguoi_nhan_id, id DESC);
CREATE INDEX idx_thong_bao_tao_luc ON thong_bao (tao_luc);

-- EN: notification_reads — per-user read state
CREATE TABLE thong_bao_da_doc (
    nguoi_dung_id  UUID   NOT NULL REFERENCES nguoi_dung(id) ON DELETE CASCADE,  -- [EN: user_id]
    thong_bao_id   BIGINT NOT NULL REFERENCES thong_bao(id) ON DELETE CASCADE,   -- [EN: notification_id]
    doc_luc        TIMESTAMPTZ NOT NULL DEFAULT now(),                           -- [EN: read_at]
    PRIMARY KEY (nguoi_dung_id, thong_bao_id)
);
