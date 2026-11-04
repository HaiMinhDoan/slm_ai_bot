-- =====================================================================
-- V11 — Trang chat với khách trên admin (inbox, mục 19, 34, 60.2)
--   * kt_sua_ban_nhap: nhân viên sửa nháp AI rồi mới gửi (DDL mục 29, thêm nguoi_sua);
--   * thông báo mới: nhân viên chuyển hội thoại sang người, khách nhắn khi hội thoại do nhân viên phụ trách.
-- =====================================================================

-- EN: draft_edits — staff edits of AI reply drafts
CREATE TABLE kt_sua_ban_nhap (
    id                 BIGSERIAL PRIMARY KEY,
    tin_nhan_id        UUID NOT NULL REFERENCES tin_nhan(id),  -- [EN: message_id]
    luot_goi_ai_id     UUID,                                   -- [EN: ai_run_id]
    noi_dung_ai        TEXT NOT NULL,                          -- [EN: ai_text]
    noi_dung_nguoi_sua TEXT NOT NULL,                          -- [EN: human_text]
    nguoi_sua          VARCHAR(100) NOT NULL,                  -- [EN: edited_by] email nhân viên
    da_dua_vao_vi_du   BOOLEAN NOT NULL DEFAULT false,         -- [EN: promoted] đã đưa vào kt_ung_vien_vi_du
    tao_luc            TIMESTAMPTZ NOT NULL DEFAULT now()      -- [EN: created_at]
);
CREATE INDEX idx_kt_sua_ban_nhap_tin_nhan_id ON kt_sua_ban_nhap (tin_nhan_id);

ALTER TABLE thong_bao DROP CONSTRAINT thong_bao_loai_check;
ALTER TABLE thong_bao ADD CONSTRAINT thong_bao_loai_check
    CHECK (loai IN ('AI_HANDOFF','AI_DRAFT','HANDBACK_REPLY','LEAD_HOT','SEND_FAILED','DUPLICATE_PHONE',
                    'CATALOG_SYNC_FAILED','DATA_UPDATE_DUE','AI_KILL_SWITCH','KNOWLEDGE_RELEASE',
                    'STAFF_HANDOFF','CUSTOMER_WAITING'));
