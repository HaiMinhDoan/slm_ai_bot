-- =====================================================================
-- V3 — Khách hàng, định danh kênh, webhook, hội thoại, tin nhắn, hồ sơ tư vấn, lead, lượt gọi AI
-- DDL theo mục 29 của kế hoạch. Bổ sung so với tài liệu:
--   * DEFAULT gen_random_uuid() cho khóa UUID (giống V2);
--   * trigger sua_luc (V1);
--   * 2 index cho worker: sự kiện webhook chưa xử lý, hội thoại đang mở theo định danh kênh.
-- =====================================================================

-- EN: customers — customer master record (Customer 360)
CREATE TABLE khach_hang (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ten                 VARCHAR(200),                         -- [EN: name]
    sdt                 VARCHAR(20) UNIQUE,                   -- [EN: phone] chuẩn hóa dạng 0xxxxxxxxx; NULL khi chưa có. Một số = một khách / hộ
    email               VARCHAR(200),
    giai_doan           VARCHAR(20) NOT NULL DEFAULT 'LEAD',  -- [EN: stage] mục 4.1
    nhan_vien_sales_id  VARCHAR(100),                         -- [EN: sales_id]
    so_thich            JSONB NOT NULL DEFAULT '{}',          -- [EN: preferences] Customer Memory, mục 17
    dong_y              JSONB NOT NULL DEFAULT '{}',          -- [EN: consent] theo kênh / mục đích
    tu_choi_lien_he_luc TIMESTAMPTZ,                          -- [EN: opt_out_at]
    gop_vao_id          UUID REFERENCES khach_hang(id),       -- [EN: merged_into_id] khác NULL: đã gộp vào khách khác
    tao_luc             TIMESTAMPTZ NOT NULL DEFAULT now(),   -- [EN: created_at]
    sua_luc             TIMESTAMPTZ NOT NULL DEFAULT now()    -- [EN: updated_at]
);

-- EN: customer_merges — log of merged duplicate customers
CREATE TABLE khach_hang_lich_su_gop (
    id                   BIGSERIAL PRIMARY KEY,
    khach_hang_id        UUID NOT NULL REFERENCES khach_hang(id),          -- [EN: customer_id] bản ghi giữ lại
    khach_hang_bi_gop_id UUID NOT NULL REFERENCES khach_hang(id),          -- [EN: merged_customer_id]
    ly_do                VARCHAR(16) NOT NULL CHECK (ly_do IN ('PHONE_MATCH','HUMAN')),  -- [EN: reason]
    du_lieu_su_kien      JSONB,                                            -- [EN: event_data] id các bản ghi đã chuyển
    nguoi_tao            VARCHAR(100),                                     -- [EN: created_by] NULL khi tự động
    tao_luc              TIMESTAMPTZ NOT NULL DEFAULT now()                -- [EN: created_at]
);

-- EN: channel_identities — customer identity per channel (PSID, Zalo user_id, TikTok id)
CREATE TABLE dinh_danh_kenh (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    khach_hang_id       UUID NOT NULL REFERENCES khach_hang(id),  -- [EN: customer_id]
    kenh                VARCHAR(20) NOT NULL                      -- [EN: channel] mã kênh hội thoại, mục 54.1
                        CHECK (kenh IN ('MESSENGER','FACEBOOK_COMMENT','ZALO_OA','TIKTOK_DM','TIKTOK_COMMENT',
                                        'FACEBOOK_GROUP','ZALO_PERSONAL')),
    tai_khoan_id        VARCHAR(100) NOT NULL,                    -- [EN: account_id] page_id / oa_id / tài khoản TikTok
    id_nguoi_dung_ngoai VARCHAR(200) NOT NULL,                    -- [EN: external_user_id] PSID / Zalo user_id / ID TikTok
    ten_hien_thi        VARCHAR(200),                             -- [EN: display_name]
    da_quan_tam         BOOLEAN,                                  -- [EN: is_follower] Zalo: đã quan tâm OA chưa
    tin_den_cuoi_luc    TIMESTAMPTZ,                              -- [EN: last_inbound_at] để tính cửa sổ nhắn tin
    lien_ket_boi        VARCHAR(16) NOT NULL DEFAULT 'AUTO'       -- [EN: linked_by]
                        CHECK (lien_ket_boi IN ('AUTO','PHONE_MATCH','HUMAN')),
    tao_luc             TIMESTAMPTZ NOT NULL DEFAULT now(),       -- [EN: created_at]
    UNIQUE (kenh, tai_khoan_id, id_nguoi_dung_ngoai)
);
CREATE INDEX idx_dinh_danh_kenh_khach_hang_id ON dinh_danh_kenh (khach_hang_id);

-- EN: webhook_events — raw inbound webhook events, dedup
CREATE TABLE su_kien_webhook (
    id            BIGSERIAL PRIMARY KEY,
    kenh          VARCHAR(20)  NOT NULL,                  -- [EN: channel]
    tai_khoan_id  VARCHAR(100) NOT NULL,                  -- [EN: account_id] page_id / oa_id / tài khoản TikTok
    ma_su_kien    VARCHAR(200) NOT NULL,                  -- [EN: event_key] message id / event id từ nền tảng
    du_lieu_tho   JSONB NOT NULL,                         -- [EN: payload] một sự kiện, không phải cả lô webhook
    trang_thai    VARCHAR(16) NOT NULL DEFAULT 'RECEIVED' -- [EN: status]
                  CHECK (trang_thai IN ('RECEIVED','PROCESSED','IGNORED','FAILED')),
    thong_bao_loi TEXT,                                   -- [EN: error_message]
    nhan_luc      TIMESTAMPTZ NOT NULL DEFAULT now(),     -- [EN: received_at]
    xu_ly_luc     TIMESTAMPTZ,                            -- [EN: processed_at]
    UNIQUE (kenh, ma_su_kien)
);
-- Worker chỉ quét sự kiện chưa xử lý
CREATE INDEX idx_su_kien_webhook_cho_xu_ly ON su_kien_webhook (id) WHERE trang_thai = 'RECEIVED';

-- EN: conversations — customer conversations
CREATE TABLE hoi_thoai (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    khach_hang_id          UUID NOT NULL REFERENCES khach_hang(id),         -- [EN: customer_id]
    dinh_danh_kenh_id      UUID NOT NULL REFERENCES dinh_danh_kenh(id),     -- [EN: channel_identity_id]
    kenh                   VARCHAR(20) NOT NULL,                            -- [EN: channel]
    nguoi_phu_trach        VARCHAR(16) NOT NULL DEFAULT 'AI_ACTIVE'         -- [EN: ownership]
                           CHECK (nguoi_phu_trach IN ('AI_ACTIVE','HUMAN_OWNED')),
    nhan_vien_phu_trach_id VARCHAR(100),                                    -- [EN: owner_staff_id]
    tiep_quan_luc          TIMESTAMPTZ,                                     -- [EN: taken_over_at]
    nhan_vien_tin_cuoi_luc TIMESTAMPTZ,                                     -- [EN: last_staff_message_at] để tự trả lại AI
    che_do_tra_loi         VARCHAR(8) NOT NULL DEFAULT 'AUTO'               -- [EN: reply_mode]
                           CHECK (che_do_tra_loi IN ('DRAFT','AUTO')),
    trang_thai             VARCHAR(8) NOT NULL DEFAULT 'OPEN'               -- [EN: status]
                           CHECK (trang_thai IN ('OPEN','CLOSED')),
    y_dinh                 VARCHAR(50),                                     -- [EN: intent]
    tom_tat                TEXT,                                            -- [EN: summary]
    tin_khach_cuoi_luc     TIMESTAMPTZ,                                     -- [EN: last_customer_message_at] cửa sổ 24 giờ
    han_tra_loi_luc        TIMESTAMPTZ,                                     -- [EN: reply_due_at] gộp tin (mục 19)
    tao_luc                TIMESTAMPTZ NOT NULL DEFAULT now(),              -- [EN: created_at]
    sua_luc                TIMESTAMPTZ NOT NULL DEFAULT now()               -- [EN: updated_at]
);
CREATE INDEX idx_hoi_thoai_han_tra_loi_luc ON hoi_thoai (han_tra_loi_luc) WHERE han_tra_loi_luc IS NOT NULL;
CREATE INDEX idx_hoi_thoai_khach_hang_id ON hoi_thoai (khach_hang_id);
-- Tìm hội thoại đang mở của một định danh kênh khi có tin mới
CREATE INDEX idx_hoi_thoai_dinh_danh_kenh_id_mo ON hoi_thoai (dinh_danh_kenh_id) WHERE trang_thai = 'OPEN';

-- EN: messages — conversation messages
CREATE TABLE tin_nhan (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    hoi_thoai_id      UUID NOT NULL REFERENCES hoi_thoai(id) ON DELETE CASCADE,  -- [EN: conversation_id]
    chieu             VARCHAR(4)  NOT NULL CHECK (chieu IN ('IN','OUT')),        -- [EN: direction]
    nguoi_gui         VARCHAR(16) NOT NULL                                       -- [EN: sender_type]
                      CHECK (nguoi_gui IN ('CUSTOMER','AI','STAFF','SYSTEM')),
    nhan_vien_id      VARCHAR(100),                                              -- [EN: staff_id]
    id_tin_nhan_ngoai VARCHAR(200),                                              -- [EN: external_message_id]
    noi_dung          TEXT,                                                      -- [EN: content]
    dinh_kem          JSONB NOT NULL DEFAULT '[]',                               -- [EN: attachments]
    trang_thai        VARCHAR(16) NOT NULL                                       -- [EN: status]
                      CHECK (trang_thai IN ('RECEIVED','PROCESSED','DRAFT','SENT','FAILED','DISCARDED')),
    luot_goi_ai_id    UUID,                                                      -- [EN: ai_run_id]
    tao_luc           TIMESTAMPTZ NOT NULL DEFAULT now(),                        -- [EN: created_at]
    gui_luc           TIMESTAMPTZ                                                -- [EN: sent_at]
);
CREATE UNIQUE INDEX uq_tin_nhan_hoi_thoai_id_id_tin_nhan_ngoai ON tin_nhan (hoi_thoai_id, id_tin_nhan_ngoai)
    WHERE id_tin_nhan_ngoai IS NOT NULL;
CREATE INDEX idx_tin_nhan_hoi_thoai_id_tao_luc ON tin_nhan (hoi_thoai_id, tao_luc);

-- EN: consult_profiles — collected consultation info and lead level
CREATE TABLE ho_so_tu_van (
    hoi_thoai_id         UUID PRIMARY KEY REFERENCES hoi_thoai(id) ON DELETE CASCADE,  -- [EN: conversation_id]
    khach_hang_id        UUID NOT NULL REFERENCES khach_hang(id),   -- [EN: customer_id]
    thong_tin_da_thu     JSONB  NOT NULL DEFAULT '{}',              -- [EN: slots] theo consult-slots.yaml
    thong_tin_con_thieu  TEXT[] NOT NULL DEFAULT '{}',              -- [EN: missing_slots]
    so_cau_da_tra_loi    INT    NOT NULL DEFAULT 0,                 -- [EN: answered_questions] mục 58.5
    phan_loai            JSONB  NOT NULL DEFAULT '{}',              -- [EN: qualification] mục 58.3
    muc_tiem_nang        VARCHAR(16)                                -- [EN: lead_level]
                         CHECK (muc_tiem_nang IN ('HOT','WARM','COLD','OFF_SEGMENT')),
    ly_do_muc_tiem_nang  TEXT,                                      -- [EN: lead_level_reason]
    tron_goi_de_xuat_ids INT[]  NOT NULL DEFAULT '{}',              -- [EN: recommended_package_ids]
    sua_luc              TIMESTAMPTZ NOT NULL DEFAULT now()         -- [EN: updated_at]
);

-- EN: leads — leads sent to the Sales API
CREATE TABLE khach_tiem_nang (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    khach_hang_id           UUID NOT NULL REFERENCES khach_hang(id),        -- [EN: customer_id]
    hoi_thoai_id            UUID REFERENCES hoi_thoai(id),                  -- [EN: conversation_id]
    kenh_nguon              VARCHAR(20) NOT NULL,                           -- [EN: source_channel] mã kênh mục 54.1
    tinh                    VARCHAR(100),                                   -- [EN: province]
    mien                    VARCHAR(8) CHECK (mien IN ('BAC','NAM')),       -- [EN: region] rules/regions.yaml
    sdt                     VARCHAR(20) NOT NULL,                           -- [EN: phone] đã chuẩn hóa
    tom_tat_nhu_cau         TEXT,                                           -- [EN: need_summary]
    muc_tiem_nang           VARCHAR(16)                                     -- [EN: lead_level]
                            CHECK (muc_tiem_nang IN ('HOT','WARM','COLD','OFF_SEGMENT')),
    ly_do_muc_tiem_nang     TEXT,                                           -- [EN: lead_level_reason]
    phan_loai               JSONB NOT NULL DEFAULT '{}',                    -- [EN: qualification]
    tron_goi_de_xuat_ids    INT[] NOT NULL DEFAULT '{}',                    -- [EN: recommended_package_ids]
    trang_thai              VARCHAR(16) NOT NULL DEFAULT 'NEW'              -- [EN: status]
                            CHECK (trang_thai IN ('NEW','SENT','FAILED','DUPLICATE')),
    ma_tham_chieu_sales_api VARCHAR(200),                                   -- [EN: sales_api_ref]
    so_lan_thu              INT NOT NULL DEFAULT 0,                         -- [EN: attempt_count]
    khoa_chong_trung        VARCHAR(160) NOT NULL UNIQUE,                   -- [EN: idempotency_key] hoi_thoai_id + sdt (mục 19)
    loi_gan_nhat            TEXT,                                           -- [EN: last_error]
    tao_luc                 TIMESTAMPTZ NOT NULL DEFAULT now(),             -- [EN: created_at]
    gui_luc                 TIMESTAMPTZ                                     -- [EN: sent_at]
);

-- EN: ai_runs — one row per Claude API call (audit, cost)
CREATE TABLE luot_goi_ai (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    muc_dich          VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER_CHAT'  -- [EN: purpose]
                      CHECK (muc_dich IN ('CUSTOMER_CHAT','CONTENT_ASSISTANT','DAILY_BRIEF','KNOWLEDGE_ASSISTANT','RELEASE_EVAL')),
    hoi_thoai_id      UUID,                                         -- [EN: conversation_id] NULL với DAILY_BRIEF, RELEASE_EVAL
    model             VARCHAR(64) NOT NULL,
    phien_ban_kt      CHAR(64) NOT NULL,                            -- [EN: kb_version]
    phien_ban_catalog CHAR(64) NOT NULL,                            -- [EN: catalog_version]
    phien_ban_media   CHAR(64) NOT NULL,                            -- [EN: media_version]
    phien_ban_prompt  VARCHAR(64),                                  -- [EN: prompt_version]
    token_dau_vao     INT,                                          -- [EN: input_tokens]
    token_dau_ra      INT,                                          -- [EN: output_tokens]
    token_doc_cache   INT,                                          -- [EN: cache_read_tokens]
    token_ghi_cache   INT,                                          -- [EN: cache_write_tokens]
    cac_lan_goi_tool  JSONB,                                        -- [EN: tool_calls]
    nguon_tham_chieu  TEXT[],                                       -- [EN: sources] ma_tai_lieu AI dẫn nguồn
    co_lo_hong_kt     BOOLEAN NOT NULL DEFAULT false,               -- [EN: knowledge_gap]
    ket_qua_policy    JSONB,                                        -- [EN: policy_result]
    do_tre_ms         INT,                                          -- [EN: latency_ms]
    tao_luc           TIMESTAMPTZ NOT NULL DEFAULT now()            -- [EN: created_at]
);
CREATE INDEX idx_luot_goi_ai_hoi_thoai_id_tao_luc ON luot_goi_ai (hoi_thoai_id, tao_luc);

SELECT gan_trigger_sua_luc(t) FROM (VALUES ('khach_hang'), ('hoi_thoai'), ('ho_so_tu_van')) AS x(t);
