-- Mốc báo giá (kế hoạch mục 4.2, 29, 60.2, 60.3, 2026-10-28):
--   catalog đồng bộ từ slm_v2 qua view ai_v_* (tài khoản chỉ đọc ai_sync), bảng PV out EVN theo năm,
--   giá gói rẻ nhất đã báo cho khách (OFF_SEGMENT, mục 58.5), hai loại thông báo mới.
-- Bảng dm_* chỉ do CatalogSyncJob ghi; id_nguon là id bên slm_v2. Không đồng bộ giá nhập, biên lợi nhuận (mục 4.2.2).

-- EN: catalog_brands — brands synced from slm_v2
CREATE TABLE dm_thuong_hieu (
    id_nguon    INT PRIMARY KEY,        -- [EN: source_id]
    ten         VARCHAR(200) NOT NULL,  -- [EN: name]
    ten_quoc_te VARCHAR(200),           -- [EN: name_intl]
    quoc_gia    VARCHAR(200),           -- [EN: country]
    mo_ta       TEXT,                   -- [EN: description]
    dang_dung   BOOLEAN NOT NULL,       -- [EN: active]
    ma_bam      CHAR(64) NOT NULL,      -- [EN: content_hash]
    dong_bo_luc TIMESTAMPTZ NOT NULL    -- [EN: synced_at]
);

-- EN: catalog_branches — price regions synced from slm_v2 (HN = North, HCM = South)
CREATE TABLE dm_co_so (
    id_nguon          INT PRIMARY KEY,              -- [EN: source_id]
    ma                VARCHAR(20) NOT NULL UNIQUE,  -- [EN: code] HN = giá miền Bắc, HCM = giá miền Nam (mục 4.2.9)
    ten               VARCHAR(200) NOT NULL,        -- [EN: name]
    dia_chi_van_phong TEXT,                         -- [EN: office_address]
    sdt               VARCHAR(20),                  -- [EN: phone]
    dang_dung         BOOLEAN NOT NULL,             -- [EN: active]
    ma_bam            CHAR(64) NOT NULL,            -- [EN: content_hash]
    dong_bo_luc       TIMESTAMPTZ NOT NULL          -- [EN: synced_at]
);

-- EN: catalog_materials — materials synced from slm_v2
CREATE TABLE dm_vat_tu (
    id_nguon       INT PRIMARY KEY,                          -- [EN: source_id]
    ma             VARCHAR(200) NOT NULL,                    -- [EN: code] vat_tu.ma
    ten            VARCHAR(400) NOT NULL,                    -- [EN: name]
    ma_nhom        VARCHAR(200),                             -- [EN: group_code] nhom_vat_tu.ma: TAM_PIN, BIEN_TAN...
    thuong_hieu_id INT REFERENCES dm_thuong_hieu(id_nguon),  -- [EN: brand_id]
    don_vi         VARCHAR(20),                              -- [EN: unit]
    bao_hanh_thang INT,                                      -- [EN: warranty_months]
    vat_tu_chinh   BOOLEAN,                                  -- [EN: is_main]
    thong_so       JSONB NOT NULL DEFAULT '{}',              -- [EN: specs] đã chuẩn hóa: {"cong_suat": {"value": 600, "unit": "Wp"}}
    mo_ta_bao_gia  TEXT,                                     -- [EN: quote_description]
    dang_dung      BOOLEAN NOT NULL,                         -- [EN: active]
    canh_bao       TEXT[] NOT NULL DEFAULT '{}',             -- [EN: warnings]
    ma_bam         CHAR(64) NOT NULL,                        -- [EN: content_hash]
    dong_bo_luc    TIMESTAMPTZ NOT NULL                      -- [EN: synced_at]
);
CREATE INDEX idx_dm_vat_tu_ma ON dm_vat_tu (ma);

-- EN: catalog_packages — solar packages synced from slm_v2
CREATE TABLE dm_tron_goi (
    id_nguon                   INT PRIMARY KEY,                             -- [EN: source_id] = id sản phẩm trên website (link /products/{id})
    ma_co_so                   VARCHAR(20) NOT NULL,                        -- [EN: branch_code] HN / HCM
    nhom_tron_goi_id           INT,                                         -- [EN: family_id] gói HN và HCM cùng tên, cùng nhóm là một cặp
    ten_nhom_goi               VARCHAR(400),                                -- [EN: family_name] nhom_tron_goi.ten
    thuong_hieu_tam_pin_id     INT REFERENCES dm_thuong_hieu(id_nguon),     -- [EN: panel_brand_id]
    thuong_hieu_inverter_id    INT REFERENCES dm_thuong_hieu(id_nguon),     -- [EN: inverter_brand_id]
    thuong_hieu_pin_luu_tru_id INT REFERENCES dm_thuong_hieu(id_nguon),     -- [EN: battery_brand_id]
    ten                        VARCHAR(400) NOT NULL,                       -- [EN: name]
    loai_he_thong              VARCHAR(16) CHECK (loai_he_thong IN ('ON_GRID','HYBRID')),  -- [EN: system_type] NULL nếu không nhận diện được
    so_pha                     SMALLINT    CHECK (so_pha IN (1,3)),         -- [EN: phase]
    cong_suat_kwp              NUMERIC(8,3),                                -- [EN: capacity_kwp]
    so_tam_pin                 INT,                                         -- [EN: panel_count] tổng số tấm pin trong gói
    cong_suat_tam_wp           NUMERIC(8,1),                                -- [EN: panel_wp] công suất một tấm (tấm đầu tiên)
    cong_suat_inverter_kw      NUMERIC(8,2),                                -- [EN: inverter_kw]
    luu_tru_kwh                NUMERIC(8,2) NOT NULL DEFAULT 0,             -- [EN: storage_kwh] tổng dung lượng pin lưu trữ
    san_luong_thang_min        NUMERIC(10,1),                               -- [EN: monthly_kwh_min] lưu để đối chiếu, AI không dùng (mục 60.2)
    san_luong_thang_max        NUMERIC(10,1),                               -- [EN: monthly_kwh_max]
    tong_gia                   NUMERIC(14,0),                               -- [EN: total_price] tong_gia: giá chính thức của gói
    gia_khung_sat              NUMERIC(14,0) NOT NULL DEFAULT 0,            -- [EN: steel_frame_price]
    ban_chay                   BOOLEAN,                                     -- [EN: best_seller]
    tep_tin_id                 INT,                                         -- [EN: image_file_id] ảnh gói ở slm_v2
    mo_ta                      TEXT,                                        -- [EN: description]
    dang_dung                  BOOLEAN NOT NULL,                            -- [EN: active]
    duoc_goi_y                 BOOLEAN NOT NULL,                            -- [EN: recommendable] false nếu có cảnh báo loại chặn
    canh_bao                   TEXT[] NOT NULL DEFAULT '{}',                -- [EN: warnings]
    ma_bam                     CHAR(64) NOT NULL,                           -- [EN: content_hash]
    dong_bo_luc                TIMESTAMPTZ NOT NULL                         -- [EN: synced_at]
);
CREATE INDEX idx_dm_tron_goi_ma_co_so_loai_he_thong_so_pha
    ON dm_tron_goi (ma_co_so, loai_he_thong, so_pha) WHERE dang_dung AND duoc_goi_y;

-- EN: catalog_package_items — package line items
CREATE TABLE dm_vat_tu_tron_goi (
    id_nguon       INT PRIMARY KEY,                                         -- [EN: source_id] vat_tu_tron_goi.id
    tron_goi_id    INT NOT NULL REFERENCES dm_tron_goi(id_nguon) ON DELETE CASCADE,  -- [EN: package_id]
    vat_tu_id      INT NOT NULL REFERENCES dm_vat_tu(id_nguon),             -- [EN: material_id]
    so_luong       INT,                                                     -- [EN: quantity]
    bao_hanh_thang INT,                                                     -- [EN: warranty_months]
    duoc_bao_hanh  BOOLEAN,                                                 -- [EN: has_warranty]
    hien_thi_khach BOOLEAN NOT NULL,                                        -- [EN: customer_visible] duoc_xem
    dang_dung      BOOLEAN NOT NULL,                                        -- [EN: active]
    ma_bam         CHAR(64) NOT NULL,                                       -- [EN: content_hash]
    dong_bo_luc    TIMESTAMPTZ NOT NULL                                     -- [EN: synced_at]
);
CREATE INDEX idx_dm_vat_tu_tron_goi_tron_goi_id ON dm_vat_tu_tron_goi (tron_goi_id);

-- EN: catalog_domains — company websites per price branch (slm_v2 mien), for product links by region
CREATE TABLE dm_ten_mien (
    id_nguon    INT PRIMARY KEY,         -- [EN: source_id]
    ten_mien    VARCHAR(255) NOT NULL,   -- [EN: domain] slmsolar.vn, slmsolar.com.vn...
    ma_co_so    VARCHAR(20),             -- [EN: branch_code]
    dang_dung   BOOLEAN NOT NULL,        -- [EN: active]
    ma_bam      CHAR(64) NOT NULL,       -- [EN: content_hash]
    dong_bo_luc TIMESTAMPTZ NOT NULL     -- [EN: synced_at]
);

-- EN: catalog_sync_runs — catalog sync run log
CREATE TABLE dm_lich_su_dong_bo (
    id                BIGSERIAL PRIMARY KEY,
    kieu_kich_hoat    VARCHAR(16) NOT NULL CHECK (kieu_kich_hoat IN ('SCHEDULED','MANUAL')),  -- [EN: trigger_type]
    trang_thai        VARCHAR(16) NOT NULL CHECK (trang_thai IN ('RUNNING','SUCCESS','FAILED')),  -- [EN: status]
    bat_dau_luc       TIMESTAMPTZ NOT NULL DEFAULT now(),                   -- [EN: started_at]
    ket_thuc_luc      TIMESTAMPTZ,                                          -- [EN: finished_at]
    so_them_moi       INT,                                                  -- [EN: inserted_count]
    so_cap_nhat       INT,                                                  -- [EN: updated_count]
    so_ngung          INT,                                                  -- [EN: deactivated_count]
    canh_bao          JSONB,                                                -- [EN: warnings] {mã cảnh báo: [source_id...]}
    thong_bao_loi     TEXT,                                                 -- [EN: error_message]
    phien_ban_catalog CHAR(64),                                             -- [EN: catalog_version]
    nguoi_kich_hoat   VARCHAR(255)                                          -- [EN: triggered_by] email người bấm "Đồng bộ ngay"
);
CREATE INDEX idx_dm_lich_su_dong_bo_bat_dau_luc ON dm_lich_su_dong_bo (bat_dau_luc DESC);

-- EN: pvout_tables — EVN theoretical PVout table of one year (mục 60.3); nạp lại mỗi năm khi EVN công bố bảng mới
CREATE TABLE nl_bang_pvout (
    nam      SMALLINT PRIMARY KEY,                -- [EN: year] năm áp dụng
    nguon    TEXT NOT NULL,                       -- [EN: source] văn bản công bố
    ghi_chu  TEXT,                                -- [EN: note]
    nap_boi  VARCHAR(255),                        -- [EN: imported_by] email người nạp; NULL = migration
    nap_luc  TIMESTAMPTZ NOT NULL DEFAULT now()   -- [EN: imported_at]
);

-- EN: pvout_province_values — monthly PVout (kWh/kWp) per province (34 tỉnh, mã trong data/tinh-thanh.json)
CREATE TABLE nl_pvout_tinh (
    nam        SMALLINT NOT NULL REFERENCES nl_bang_pvout(nam) ON DELETE CASCADE,  -- [EN: year]
    ma_tinh    VARCHAR(40) NOT NULL,                                               -- [EN: province_code]
    theo_thang NUMERIC(6,1)[] NOT NULL CHECK (cardinality(theo_thang) = 12),      -- [EN: monthly] tháng 1..12
    PRIMARY KEY (nam, ma_tinh)
);

INSERT INTO nl_bang_pvout (nam, nguon, ghi_chu) VALUES
    (2026, 'EVN, công văn 364/EVN-KDMBĐ ngày 20/1/2026: "Hệ số PVout lý thuyết của các tháng trong năm 2026"; '
           'số liệu của Viện Khoa học Khí tượng Thủy văn và Biến đổi khí hậu (văn bản 637/VKTTVBĐKH-KHTC ngày 14/11/2025)',
     '34 tỉnh, thành phố sau sắp xếp theo Nghị quyết 202/2025/QH15; đơn vị kWh/kWp mỗi tháng');

INSERT INTO nl_pvout_tinh (nam, ma_tinh, theo_thang) VALUES
    (2026, 'TUYEN_QUANG', '{49,37,52,68,108,109,120,112,107,93,80,63}'),
    (2026, 'CAO_BANG',    '{57,54,62,77,108,115,115,114,110,98,86,69}'),
    (2026, 'LAI_CHAU',    '{105,107,128,134,139,104,119,117,118,111,108,101}'),
    (2026, 'LAO_CAI',     '{89,92,104,111,120,113,114,108,101,92,91,88}'),
    (2026, 'THAI_NGUYEN', '{51,36,51,65,101,102,119,105,104,91,78,60}'),
    (2026, 'DIEN_BIEN',   '{110,112,129,133,139,112,123,118,123,117,111,104}'),
    (2026, 'LANG_SON',    '{58,42,55,68,99,103,118,103,103,91,79,63}'),
    (2026, 'SON_LA',      '{106,103,117,121,126,120,125,120,119,114,108,102}'),
    (2026, 'PHU_THO',     '{46,39,54,69,103,109,109,108,100,88,76,55}'),
    (2026, 'BAC_NINH',    '{47,35,49,63,98,100,108,102,95,89,75,55}'),
    (2026, 'QUANG_NINH',  '{55,43,57,69,98,102,109,105,100,95,81,63}'),
    (2026, 'HA_NOI',      '{44,33,47,63,100,105,110,104,96,85,73,52}'),
    (2026, 'HAI_PHONG',   '{47,33,47,61,97,101,106,102,92,87,74,55}'),
    (2026, 'HUNG_YEN',    '{45,31,45,62,99,106,109,103,93,84,72,52}'),
    (2026, 'NINH_BINH',   '{53,34,49,67,109,116,120,104,98,83,74,57}'),
    (2026, 'THANH_HOA',   '{64,45,61,80,123,129,132,115,107,90,82,66}'),
    (2026, 'NGHE_AN',     '{104,85,100,108,116,121,112,112,114,99,99,99}'),
    (2026, 'HA_TINH',     '{80,62,84,118,121,133,123,120,101,77,69,64}'),
    (2026, 'QUANG_TRI',   '{87,74,92,113,125,133,114,125,103,84,84,82}'),
    (2026, 'HUE',         '{93,83,99,116,131,135,119,135,106,89,88,84}'),
    (2026, 'DA_NANG',     '{97,86,113,131,144,139,125,139,120,95,95,86}'),
    (2026, 'QUANG_NGAI',  '{119,121,138,145,145,136,125,129,115,108,107,108}'),
    (2026, 'GIA_LAI',     '{120,116,138,150,143,133,119,141,119,113,106,98}'),
    (2026, 'DAK_LAK',     '{109,114,145,148,149,142,133,139,126,113,99,92}'),
    (2026, 'KHANH_HOA',   '{111,115,146,152,155,152,145,153,135,117,100,94}'),
    (2026, 'LAM_DONG',    '{139,139,161,152,142,129,118,130,115,121,117,120}'),
    (2026, 'DONG_NAI',    '{140,139,150,146,141,127,122,120,113,121,122,129}'),
    (2026, 'TAY_NINH',    '{113,106,123,122,130,124,115,125,105,114,110,110}'),
    (2026, 'HO_CHI_MINH', '{119,110,132,126,133,120,115,123,106,116,112,114}'),
    (2026, 'DONG_THAP',   '{118,111,126,127,124,112,110,118,99,106,104,107}'),
    (2026, 'AN_GIANG',    '{123,113,121,123,123,108,104,111,96,106,105,112}'),
    (2026, 'VINH_LONG',   '{112,111,128,124,121,109,104,112,97,100,100,103}'),
    (2026, 'CAN_THO',     '{111,108,121,121,118,103,99,106,94,97,97,102}'),
    (2026, 'CA_MAU',      '{109,104,117,120,119,99,96,100,90,97,95,101}');

-- Giá gói rẻ nhất phù hợp đã báo cho khách (tool tim_goi_phu_hop, lượt đã gửi): xét OFF_SEGMENT, cờ ngân sách thấp
ALTER TABLE ho_so_tu_van
    ADD COLUMN gia_thap_nhat_da_bao NUMERIC(14,0);           -- [EN: cheapest_quoted_price]

-- Thông báo mới: đồng bộ catalog lỗi, đến hạn nạp bảng PV out năm mới
ALTER TABLE thong_bao DROP CONSTRAINT thong_bao_loai_check;
ALTER TABLE thong_bao ADD CONSTRAINT thong_bao_loai_check
    CHECK (loai IN ('AI_HANDOFF','AI_DRAFT','HANDBACK_REPLY','LEAD_HOT','SEND_FAILED','DUPLICATE_PHONE',
                    'CATALOG_SYNC_FAILED','DATA_UPDATE_DUE'));
