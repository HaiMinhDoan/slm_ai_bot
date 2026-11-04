-- =====================================================================
-- V12 — Đồng bộ đủ phần sản phẩm của slm_v2 (2026-11-04): ngành hàng, nhóm vật tư, nhóm gói, lịch sử giá bán vật
-- tư, giá bán từng vật tư trong gói, ảnh vật tư, logo hãng, ảnh gói, hotline / email theo tên miền.
-- Vẫn KHÔNG đồng bộ giá nhập (giaNhap), biên lợi nhuận (gm, gm_tong) (mục 4.2.2): lọc ngay trong câu đọc slm_v2.
-- Bảng dm_* chỉ do CatalogSyncJob ghi; id_nguon là id bên slm_v2.
-- =====================================================================

-- EN: catalog_product_lines — product lines (nganh_hang)
CREATE TABLE dm_nganh_hang (
    id_nguon     INT PRIMARY KEY,        -- [EN: source_id]
    ma           VARCHAR(100),           -- [EN: code]
    ten          VARCHAR(400) NOT NULL,  -- [EN: name]
    sdt_sale     VARCHAR(50),            -- [EN: sales_phone]
    sdt_ky_thuat VARCHAR(50),            -- [EN: tech_phone]
    dang_dung    BOOLEAN NOT NULL,       -- [EN: active]
    ma_bam       CHAR(64) NOT NULL,      -- [EN: content_hash]
    dong_bo_luc  TIMESTAMPTZ NOT NULL    -- [EN: synced_at]
);

-- EN: catalog_material_groups — material groups (TAM_PIN, BIEN_TAN...) with their attribute template
CREATE TABLE dm_nhom_vat_tu (
    id_nguon      INT PRIMARY KEY,               -- [EN: source_id]
    ma            VARCHAR(200),                  -- [EN: code]
    ten           VARCHAR(400) NOT NULL,         -- [EN: name]
    nganh_hang_id INT,                           -- [EN: product_line_id]
    thuoc_tinh    JSONB NOT NULL DEFAULT '{}',   -- [EN: attribute_template] khuôn thông số của nhóm (thuoc_tinh_rieng)
    vat_tu_chinh  BOOLEAN,                       -- [EN: is_main] nhóm thiết bị chính
    dang_dung     BOOLEAN NOT NULL,              -- [EN: active]
    ma_bam        CHAR(64) NOT NULL,             -- [EN: content_hash]
    dong_bo_luc   TIMESTAMPTZ NOT NULL           -- [EN: synced_at]
);

-- EN: catalog_package_families — package families (brand combination of panel / inverter / battery)
CREATE TABLE dm_nhom_tron_goi (
    id_nguon                   INT PRIMARY KEY,        -- [EN: source_id]
    ten                        VARCHAR(400) NOT NULL,  -- [EN: name]
    nganh_hang_id              INT,                    -- [EN: product_line_id]
    thuong_hieu_tam_pin_id     INT,                    -- [EN: panel_brand_id]
    thuong_hieu_inverter_id    INT,                    -- [EN: inverter_brand_id]
    thuong_hieu_pin_luu_tru_id INT,                    -- [EN: battery_brand_id]
    dang_dung                  BOOLEAN NOT NULL,       -- [EN: active]
    ma_bam                     CHAR(64) NOT NULL,      -- [EN: content_hash]
    dong_bo_luc                TIMESTAMPTZ NOT NULL    -- [EN: synced_at]
);

-- EN: catalog_material_prices — material sale price history (thong_tin_gia, sale price only)
CREATE TABLE dm_gia_vat_tu (
    id_nguon    INT PRIMARY KEY,                                 -- [EN: source_id] thong_tin_gia.id
    vat_tu_id   INT NOT NULL REFERENCES dm_vat_tu(id_nguon),     -- [EN: material_id]
    hieu_luc_tu TIMESTAMP,                                       -- [EN: effective_from] thong_tin_gia.tao_luc (giờ của slm_v2)
    gia_ban     JSONB NOT NULL DEFAULT '{}',                     -- [EN: sale_prices] {"HN": 27000000, "HCM": 26500000}
    dang_dung   BOOLEAN NOT NULL,                                -- [EN: active]
    ma_bam      CHAR(64) NOT NULL,                               -- [EN: content_hash]
    dong_bo_luc TIMESTAMPTZ NOT NULL                             -- [EN: synced_at]
);
-- Giá hiện hành của một vật tư = dòng đang dùng có hieu_luc_tu mới nhất
CREATE INDEX idx_dm_gia_vat_tu_vat_tu_id_hieu_luc_tu ON dm_gia_vat_tu (vat_tu_id, hieu_luc_tu DESC);

-- EN: catalog_material_images — material images (file URL on slm_v2 MinIO)
CREATE TABLE dm_anh_vat_tu (
    id_nguon    INT PRIMARY KEY,                              -- [EN: source_id] anh_vat_tu.id
    vat_tu_id   INT NOT NULL REFERENCES dm_vat_tu(id_nguon),  -- [EN: material_id]
    tep_tin_id  INT,                                          -- [EN: file_id]
    duong_dan   TEXT,                                         -- [EN: url] tep_tin.duong_dan (http, MinIO của slm_v2)
    anh_chinh   BOOLEAN,                                      -- [EN: is_main]
    dang_dung   BOOLEAN NOT NULL,                             -- [EN: active]
    ma_bam      CHAR(64) NOT NULL,                            -- [EN: content_hash]
    dong_bo_luc TIMESTAMPTZ NOT NULL                          -- [EN: synced_at]
);
CREATE INDEX idx_dm_anh_vat_tu_vat_tu_id ON dm_anh_vat_tu (vat_tu_id);

-- EN: catalog_domain_contacts — hotline / email per website domain (thong_tin_ten_mien)
CREATE TABLE dm_ten_mien_lien_he (
    id_nguon    INT PRIMARY KEY,                                -- [EN: source_id]
    ten_mien_id INT NOT NULL REFERENCES dm_ten_mien(id_nguon),  -- [EN: domain_id]
    sdt         VARCHAR(50),                                    -- [EN: phone]
    email       VARCHAR(200),                                   -- [EN: email]
    dang_dung   BOOLEAN NOT NULL,                               -- [EN: active]
    ma_bam      CHAR(64) NOT NULL,                              -- [EN: content_hash]
    dong_bo_luc TIMESTAMPTZ NOT NULL                            -- [EN: synced_at]
);

ALTER TABLE dm_thuong_hieu
    ADD COLUMN tep_tin_id INT,   -- [EN: logo_file_id]
    ADD COLUMN anh_url    TEXT;  -- [EN: logo_url]

ALTER TABLE dm_tron_goi
    ADD COLUMN anh_url TEXT;     -- [EN: image_url] ảnh gói (tep_tin_id)

ALTER TABLE dm_vat_tu_tron_goi
    ADD COLUMN gia_ban NUMERIC(14,0),  -- [EN: sale_price] giá bán một đơn vị lúc dựng gói (vat_tu_tron_goi.gia)
    ADD COLUMN mo_ta   TEXT;           -- [EN: description]
