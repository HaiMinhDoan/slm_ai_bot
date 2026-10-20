-- =====================================================================
-- V2 — Tài khoản người dùng nội bộ (nhân viên, quản trị viên), vai trò, token, nhật ký thao tác
-- Khách hàng KHÔNG nằm ở đây (khách là khach_hang, mục 29).
-- =====================================================================

-- EN: roles — internal user roles (code constants in RoleCode.java)
CREATE TABLE vai_tro (
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ma        VARCHAR(50)  NOT NULL UNIQUE,                 -- [EN: code] SYSTEM_ADMIN, STAFF...
    ten       VARCHAR(150) NOT NULL,                        -- [EN: name]
    mo_ta     TEXT,                                         -- [EN: description]
    he_thong  BOOLEAN      NOT NULL DEFAULT false,          -- [EN: is_system] vai trò lõi, không cho xóa
    tao_luc   TIMESTAMPTZ  NOT NULL DEFAULT now(),          -- [EN: created_at]
    sua_luc   TIMESTAMPTZ  NOT NULL DEFAULT now()           -- [EN: updated_at]
);

-- EN: users — internal users of the admin (staff, admins)
CREATE TABLE nguoi_dung (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email              VARCHAR(255) NOT NULL,
    email_chuan_hoa    VARCHAR(255) GENERATED ALWAYS AS (lower(email)) STORED,  -- [EN: email_normalized]
    mat_khau_bam       VARCHAR(255) NOT NULL,                                   -- [EN: password_hash] BCrypt
    ho_ten             VARCHAR(150) NOT NULL,                                   -- [EN: full_name]
    trang_thai         VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE'                   -- [EN: status]
                       CHECK (trang_thai IN ('ACTIVE','LOCKED','DISABLED')),
    ly_do_khoa         TEXT,                                                    -- [EN: lock_reason]
    dang_nhap_cuoi_luc TIMESTAMPTZ,                                             -- [EN: last_login_at]
    tao_luc            TIMESTAMPTZ  NOT NULL DEFAULT now(),                     -- [EN: created_at]
    sua_luc            TIMESTAMPTZ  NOT NULL DEFAULT now()                      -- [EN: updated_at]
);
CREATE UNIQUE INDEX uq_nguoi_dung_email_chuan_hoa ON nguoi_dung (email_chuan_hoa);

-- EN: user_roles — roles granted to a user
CREATE TABLE nguoi_dung_vai_tro (
    nguoi_dung_id UUID NOT NULL REFERENCES nguoi_dung(id) ON DELETE CASCADE,    -- [EN: user_id]
    vai_tro_id    UUID NOT NULL REFERENCES vai_tro(id) ON DELETE CASCADE,       -- [EN: role_id]
    nguoi_cap_id  UUID REFERENCES nguoi_dung(id) ON DELETE SET NULL,            -- [EN: granted_by]
    cap_luc       TIMESTAMPTZ NOT NULL DEFAULT now(),                           -- [EN: granted_at]
    PRIMARY KEY (nguoi_dung_id, vai_tro_id)
);

-- EN: refresh_tokens — refresh tokens (hash only), rotated on every refresh
CREATE TABLE token_lam_moi (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nguoi_dung_id UUID NOT NULL REFERENCES nguoi_dung(id) ON DELETE CASCADE,    -- [EN: user_id]
    ma_bam_token  CHAR(64)    NOT NULL UNIQUE,                                  -- [EN: token_hash] SHA-256, không lưu token gốc
    het_han_luc   TIMESTAMPTZ NOT NULL,                                         -- [EN: expires_at]
    thu_hoi_luc   TIMESTAMPTZ,                                                  -- [EN: revoked_at]
    user_agent    VARCHAR(255),
    dia_chi_ip    VARCHAR(64),                                                  -- [EN: ip_address]
    tao_luc       TIMESTAMPTZ NOT NULL DEFAULT now()                            -- [EN: created_at]
);
CREATE INDEX idx_token_lam_moi_nguoi_dung_id ON token_lam_moi (nguoi_dung_id) WHERE thu_hoi_luc IS NULL;
CREATE INDEX idx_token_lam_moi_het_han_luc ON token_lam_moi (het_han_luc);

-- EN: audit_logs — sensitive admin actions (mục 5.9, 34, 54.6)
CREATE TABLE nhat_ky_thao_tac (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nguoi_thuc_hien_id UUID REFERENCES nguoi_dung(id) ON DELETE SET NULL,       -- [EN: actor_id] NULL = hệ thống
    hanh_dong          VARCHAR(100) NOT NULL,                                   -- [EN: action] user.roles.update, knowledge.release.activate...
    loai_doi_tuong     VARCHAR(100),                                            -- [EN: target_type]
    ma_doi_tuong       VARCHAR(200),                                            -- [EN: target_key] id dạng chuỗi (UUID, INT hoặc mã)
    du_lieu_truoc      JSONB,                                                   -- [EN: before_data]
    du_lieu_sau        JSONB,                                                   -- [EN: after_data]
    dia_chi_ip         VARCHAR(64),                                             -- [EN: ip_address]
    tao_luc            TIMESTAMPTZ NOT NULL DEFAULT now()                       -- [EN: created_at]
);
CREATE INDEX idx_nhat_ky_thao_tac_nguoi_thuc_hien_id_tao_luc ON nhat_ky_thao_tac (nguoi_thuc_hien_id, tao_luc DESC);
CREATE INDEX idx_nhat_ky_thao_tac_loai_doi_tuong_ma_doi_tuong ON nhat_ky_thao_tac (loai_doi_tuong, ma_doi_tuong, tao_luc DESC);

SELECT gan_trigger_sua_luc(t) FROM (VALUES ('vai_tro'), ('nguoi_dung')) AS x(t);


-- ---------------------------------------------------------------------
-- Vai trò lõi. Phải khớp RoleCode.java.
-- Tài khoản SYSTEM_ADMIN đầu tiên do ứng dụng tạo từ ADMIN_EMAIL / ADMIN_PASSWORD (không ghi mật khẩu vào migration).
-- ---------------------------------------------------------------------
INSERT INTO vai_tro (ma, ten, mo_ta, he_thong) VALUES
    ('SYSTEM_ADMIN',        'Quản trị hệ thống',     'Người dùng, vai trò, cấu hình, tùy chọn rủi ro cao', true),
    ('STAFF',               'Nhân viên hội thoại',   'Inbox, duyệt nháp, tiếp quản hội thoại',             true),
    ('KNOWLEDGE_EDITOR',    'Biên tập kiến thức',    'Sửa bản nháp kiến thức',                             true),
    ('KNOWLEDGE_PUBLISHER', 'Phát hành kiến thức',   'Phát hành, rollback kiến thức',                      true),
    ('CONTENT_ADMIN',       'Quản lý nội dung',      'Trợ lý nội dung, duyệt và lên lịch bài đăng',        true);
