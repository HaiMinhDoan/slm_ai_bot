-- Kiến thức và quy tắc sửa trên admin (mục 5.2, 5.3, 5.9, 5.10, 33.2; 2026-11-03).
-- Tài liệu có phiên bản; bot chỉ dùng bản phát hành đang ACTIVE. Lần khởi động đầu, KnowledgeSeeder nhập kiến thức
-- trong repo (knowledge/**, prompts/customer-chat.md) và các rule đang chạy thành bản phát hành #1.
-- Mã tài liệu giữ kiểu đường dẫn đang dùng trong prompt: core/identity, playbooks/khach-hoi-gia, rules/bao-gia.

-- EN: knowledge_docs — knowledge document identity
CREATE TABLE kt_tai_lieu (                           -- một tài liệu; nội dung nằm ở các phiên bản
    id            BIGSERIAL PRIMARY KEY,
    ma_tai_lieu   VARCHAR(200) NOT NULL UNIQUE,      -- [EN: doc_key] playbooks/khach-hoi-gia
    loai_tai_lieu VARCHAR(16)  NOT NULL              -- [EN: doc_type]
                  CHECK (loai_tai_lieu IN ('CORE','PLAYBOOK','POLICY','FAQ','EXAMPLE','PROMPT','RULE','GUIDE',
                                           'TEMPLATE','CONTENT')),
    dinh_dang     VARCHAR(8)   NOT NULL CHECK (dinh_dang IN ('MARKDOWN','YAML')),  -- [EN: format]
    da_luu_tru    BOOLEAN NOT NULL DEFAULT false,    -- [EN: archived] không đưa vào bản phát hành mới
    nguoi_tao     VARCHAR(255),                      -- [EN: created_by] email
    tao_luc       TIMESTAMPTZ NOT NULL DEFAULT now() -- [EN: created_at]
);

-- EN: knowledge_doc_versions — knowledge document versions
CREATE TABLE kt_phien_ban (
    id               BIGSERIAL PRIMARY KEY,
    tai_lieu_id      BIGINT NOT NULL REFERENCES kt_tai_lieu(id),            -- [EN: doc_id]
    so_phien_ban     INT NOT NULL,                                          -- [EN: version_no]
    trang_thai       VARCHAR(16) NOT NULL CHECK (trang_thai IN ('DRAFT','FROZEN','DISCARDED')),  -- [EN: status]
    tieu_de          VARCHAR(300) NOT NULL,                                 -- [EN: title]
    chu_de           TEXT[] NOT NULL DEFAULT '{}',                          -- [EN: topics]
    cac_kenh         TEXT[] NOT NULL DEFAULT '{}',                          -- [EN: channels]
    noi_dung         TEXT NOT NULL,                                         -- [EN: content] Markdown hoặc YAML
    ma_bam           CHAR(64) NOT NULL,                                     -- [EN: content_hash]
    so_token         INT,                                                   -- [EN: token_count]
    phien_ban_goc_id BIGINT REFERENCES kt_phien_ban(id),                    -- [EN: base_version_id] sửa từ bản nào
    ghi_chu_thay_doi TEXT,                                                  -- [EN: change_note]
    tac_gia          VARCHAR(255) NOT NULL,                                 -- [EN: author] email
    co_ai_ho_tro     BOOLEAN NOT NULL DEFAULT false,                        -- [EN: ai_assisted] từ đề xuất AI (5.10)
    tao_luc          TIMESTAMPTZ NOT NULL DEFAULT now(),                    -- [EN: created_at]
    sua_luc          TIMESTAMPTZ NOT NULL DEFAULT now(),                    -- [EN: updated_at]
    UNIQUE (tai_lieu_id, so_phien_ban)
);
-- Mỗi tài liệu tối đa một bản nháp
CREATE UNIQUE INDEX uq_kt_phien_ban_mot_nhap ON kt_phien_ban (tai_lieu_id) WHERE trang_thai = 'DRAFT';
-- sua_luc do ứng dụng ghi (không dùng trigger dat_sua_luc): trang soạn gửi lại đúng giá trị này để phát hiện hai
-- người cùng sửa một bản nháp, trigger đổi giá trị sau khi lưu sẽ báo xung đột nhầm.

-- EN: knowledge_releases — immutable knowledge release snapshot
CREATE TABLE kt_phat_hanh (                          -- bản phát hành: ảnh chụp bất biến
    id                BIGSERIAL PRIMARY KEY,
    phien_ban_kt      CHAR(64) NOT NULL UNIQUE,      -- [EN: kb_version] hash các phiên bản trong bản phát hành
    trang_thai        VARCHAR(16) NOT NULL           -- [EN: status]
                      CHECK (trang_thai IN ('VALIDATING','READY','ACTIVE','FAILED','SUPERSEDED')),
    ghi_chu           TEXT,                          -- [EN: notes]
    tong_token        INT,                           -- [EN: token_total] khối kiến thức bot đọc
    bao_cao_kiem_tra  JSONB,                         -- [EN: validation_report]
    bao_cao_bo_test   JSONB,                         -- [EN: eval_report] bật khi có bộ test (mục 35)
    nguoi_tao         VARCHAR(255) NOT NULL,         -- [EN: created_by] email
    nguoi_kich_hoat   VARCHAR(255),                  -- [EN: activated_by] email
    hen_kich_hoat_luc TIMESTAMPTZ,                   -- [EN: scheduled_activate_at] chưa dùng
    tao_luc           TIMESTAMPTZ NOT NULL DEFAULT now(),  -- [EN: created_at]
    kich_hoat_luc     TIMESTAMPTZ                    -- [EN: activated_at] lần kích hoạt gần nhất
);
CREATE UNIQUE INDEX uq_kt_phat_hanh_mot_dang_chay ON kt_phat_hanh (trang_thai) WHERE trang_thai = 'ACTIVE';

-- EN: knowledge_release_items — doc version included in a release
CREATE TABLE kt_phat_hanh_chi_tiet (
    phat_hanh_id BIGINT NOT NULL REFERENCES kt_phat_hanh(id) ON DELETE CASCADE,  -- [EN: release_id]
    tai_lieu_id  BIGINT NOT NULL REFERENCES kt_tai_lieu(id),                     -- [EN: doc_id]
    phien_ban_id BIGINT NOT NULL REFERENCES kt_phien_ban(id),                    -- [EN: version_id]
    PRIMARY KEY (phat_hanh_id, tai_lieu_id)
);

-- EN: assistant_sessions — admin assistant chat sessions (content / knowledge)
CREATE TABLE tl_phien_chat (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nguoi_dung_id VARCHAR(100) NOT NULL,                                    -- [EN: user_id] email admin
    loai_tro_ly   VARCHAR(16) NOT NULL CHECK (loai_tro_ly IN ('CONTENT','KNOWLEDGE')),  -- [EN: assistant_type]
    tieu_de       VARCHAR(200),                                             -- [EN: title]
    tao_luc       TIMESTAMPTZ NOT NULL DEFAULT now(),                       -- [EN: created_at]
    tin_cuoi_luc  TIMESTAMPTZ                                               -- [EN: last_message_at]
);

-- EN: assistant_messages — assistant chat messages, append-only
CREATE TABLE tl_tin_nhan (                          -- chỉ nối thêm, không sửa lịch sử
    id             BIGSERIAL PRIMARY KEY,
    phien_chat_id  UUID NOT NULL REFERENCES tl_phien_chat(id) ON DELETE CASCADE,  -- [EN: session_id]
    vai_tro        VARCHAR(16) NOT NULL CHECK (vai_tro IN ('USER','ASSISTANT')),  -- [EN: role]
    noi_dung       JSONB NOT NULL,                  -- [EN: content]
    luot_goi_ai_id UUID,                            -- [EN: ai_run_id]
    tao_luc        TIMESTAMPTZ NOT NULL DEFAULT now()  -- [EN: created_at]
);
CREATE INDEX idx_tl_tin_nhan_phien_chat_id_id ON tl_tin_nhan (phien_chat_id, id);

-- EN: knowledge_change_proposals — AI authoring assistant change proposals
CREATE TABLE kt_de_xuat_sua (                       -- đề xuất của AI hỗ trợ soạn (mục 5.10 B)
    id                     BIGSERIAL PRIMARY KEY,
    phien_chat_id          UUID NOT NULL REFERENCES tl_phien_chat(id),  -- [EN: session_id]
    tai_lieu_id            BIGINT REFERENCES kt_tai_lieu(id),           -- [EN: doc_id] NULL khi tạo tài liệu mới
    ma_tai_lieu_de_xuat    VARCHAR(200),                                -- [EN: proposed_doc_key]
    loai_tai_lieu_de_xuat  VARCHAR(16),                                 -- [EN: proposed_doc_type]
    tieu_de_de_xuat        VARCHAR(300),                                -- [EN: proposed_title]
    hanh_dong              VARCHAR(8) NOT NULL CHECK (hanh_dong IN ('CREATE','UPDATE','RETIRE')),  -- [EN: action]
    phien_ban_goc_id       BIGINT REFERENCES kt_phien_ban(id),          -- [EN: base_version_id]
    noi_dung_de_xuat       TEXT,                                        -- [EN: proposed_content]
    tom_tat_thay_doi       TEXT NOT NULL,                               -- [EN: change_summary]
    ly_do                  TEXT,                                        -- [EN: reason]
    bao_cao_kiem_tra_nhanh JSONB,                                       -- [EN: lint_report]
    mau_thuan              JSONB,                                       -- [EN: conflicts]
    trang_thai             VARCHAR(16) NOT NULL DEFAULT 'PROPOSED'      -- [EN: status]
                           CHECK (trang_thai IN ('PROPOSED','ACCEPTED','REJECTED','SUPERSEDED')),
    phien_ban_tao_ra_id    BIGINT REFERENCES kt_phien_ban(id),          -- [EN: resulting_version_id]
    nguoi_quyet_dinh       VARCHAR(255),                                -- [EN: decided_by] email
    quyet_dinh_luc         TIMESTAMPTZ,                                 -- [EN: decided_at]
    tao_luc                TIMESTAMPTZ NOT NULL DEFAULT now()           -- [EN: created_at]
);
CREATE INDEX idx_kt_de_xuat_sua_phien_chat_id ON kt_de_xuat_sua (phien_chat_id);

-- EN: ai_settings — operational AI settings edited on admin, take effect immediately (no release)
CREATE TABLE cai_dat_ai (                           -- chỉ lưu giá trị ghi đè; thiếu thì dùng application.yml
    khoa    VARCHAR(64) PRIMARY KEY,                -- [EN: key] force_draft, auto_intents, business_hours...
    gia_tri JSONB NOT NULL,                         -- [EN: value]
    sua_boi VARCHAR(255),                           -- [EN: updated_by] email
    sua_luc TIMESTAMPTZ NOT NULL DEFAULT now()      -- [EN: updated_at]
);

-- Thông báo mới: bật / tắt công tắc dừng AI, kích hoạt bản phát hành kiến thức
ALTER TABLE thong_bao DROP CONSTRAINT thong_bao_loai_check;
ALTER TABLE thong_bao ADD CONSTRAINT thong_bao_loai_check
    CHECK (loai IN ('AI_HANDOFF','AI_DRAFT','HANDBACK_REPLY','LEAD_HOT','SEND_FAILED','DUPLICATE_PHONE',
                    'CATALOG_SYNC_FAILED','DATA_UPDATE_DUE','AI_KILL_SWITCH','KNOWLEDGE_RELEASE'));
