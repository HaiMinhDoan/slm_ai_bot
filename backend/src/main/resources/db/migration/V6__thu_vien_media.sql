-- Thư viện media (kế hoạch mục 6, DDL mục 29): media, nguồn, liên kết với gói / vật tư / tài liệu.
-- media_tham_chieu_kenh và media_lan_gui tạo ở mốc gửi media qua Messenger.
-- Ảnh tải từ Google Drive lưu như UPLOAD (bản trong MinIO là bản dùng), ghi link gốc ở url_nguon
-- và id_ngoai = 'gdrive:<file_id>' để nhập lại không bị trùng.

-- EN: media_items — media library item (content metadata)
CREATE TABLE media (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    loai_media          VARCHAR(16)  NOT NULL CHECK (loai_media IN ('IMAGE','VIDEO')),  -- [EN: media_type]
    tieu_de             VARCHAR(300) NOT NULL,                              -- [EN: title]
    ma_danh_muc         VARCHAR(10) UNIQUE,                                 -- [EN: index_code] mã ngắn trong danh mục media: M07
    tom_tat             TEXT,                                               -- [EN: summary] media nói gì (AI đọc trong danh mục)
    dung_khi            TEXT,                                               -- [EN: use_when] khi nào nên gửi
    tranh_khi           TEXT,                                               -- [EN: avoid_when] khi nào không gửi
    mo_ta               TEXT,                                               -- [EN: description] mô tả chi tiết; với ảnh = mô tả ảnh
    loi_thoai           TEXT,                                               -- [EN: transcript] video, nếu có
    chu_de              TEXT[] NOT NULL DEFAULT '{}',                       -- [EN: topics] theo knowledge/rules/topics.yaml
    cac_giai_doan_khach TEXT[] NOT NULL DEFAULT '{}',                       -- [EN: customer_stages] LEAD, INSTALLED...
    kenh_cho_phep       TEXT[] NOT NULL DEFAULT '{MESSENGER}',              -- [EN: allowed_channels]
    trang_thai          VARCHAR(16) NOT NULL DEFAULT 'DRAFT'                -- [EN: status]
                        CHECK (trang_thai IN ('DRAFT','APPROVED','ARCHIVED')),
    quyen_su_dung       VARCHAR(16) NOT NULL DEFAULT 'NEEDS_REVIEW'         -- [EN: rights_status]
                        CHECK (quyen_su_dung IN ('OWNED','LICENSED','LINK_ONLY','NEEDS_REVIEW')),
    nguoi_tao           VARCHAR(100),                                       -- [EN: created_by]
    nguoi_duyet         VARCHAR(100),                                       -- [EN: approved_by]
    duyet_luc           TIMESTAMPTZ,                                        -- [EN: approved_at]
    tao_luc             TIMESTAMPTZ NOT NULL DEFAULT now(),                 -- [EN: created_at]
    sua_luc             TIMESTAMPTZ NOT NULL DEFAULT now(),                 -- [EN: updated_at]
    CONSTRAINT chk_da_duyet_co_tom_tat
        CHECK (trang_thai <> 'APPROVED' OR (tom_tat IS NOT NULL AND dung_khi IS NOT NULL))
);
CREATE INDEX idx_media_chu_de ON media USING GIN (chu_de);

-- EN: media_sources — where a media item lives (upload / YouTube / Facebook / TikTok / slm_v2)
CREATE TABLE media_nguon (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    media_id            UUID NOT NULL REFERENCES media(id) ON DELETE CASCADE,  -- [EN: media_id]
    loai_nguon          VARCHAR(16) NOT NULL                    -- [EN: source_type]
                        CHECK (loai_nguon IN ('UPLOAD','YOUTUBE','FACEBOOK','TIKTOK','SLM_V2')),
    url_nguon           TEXT,                                   -- [EN: source_url]
    id_ngoai            VARCHAR(200),                           -- [EN: external_id] gdrive:<id>, share-r:<mã>, tep_tin.id...
    ten_tac_gia         VARCHAR(200),                           -- [EN: author_name]
    bucket_luu_tru      VARCHAR(100),                           -- [EN: storage_bucket]
    duong_dan_luu_tru   VARCHAR(500),                           -- [EN: object_key]
    loai_mime           VARCHAR(100),                           -- [EN: mime_type]
    dung_luong_byte     BIGINT,                                 -- [EN: size_bytes]
    ma_bam_sha256       CHAR(64),                               -- [EN: checksum_sha256]
    chieu_rong          INT,                                    -- [EN: width]
    chieu_cao           INT,                                    -- [EN: height]
    thoi_luong_giay     INT,                                    -- [EN: duration_seconds]
    url_anh_thu_nho     TEXT,                                   -- [EN: thumbnail_url]
    trang_thai_xu_ly    VARCHAR(16) NOT NULL DEFAULT 'PENDING'  -- [EN: processing_status]
                        CHECK (trang_thai_xu_ly IN ('PENDING','PROCESSING','READY','FAILED')),
    trang_thai_truy_cap VARCHAR(16) NOT NULL DEFAULT 'UNKNOWN'  -- [EN: availability_status]
                        CHECK (trang_thai_truy_cap IN ('ACTIVE','PRIVATE','UNAVAILABLE','DELETED','UNKNOWN')),
    kiem_tra_cuoi_luc   TIMESTAMPTZ,                            -- [EN: last_checked_at]
    tao_luc             TIMESTAMPTZ NOT NULL DEFAULT now(),     -- [EN: created_at]
    sua_luc             TIMESTAMPTZ NOT NULL DEFAULT now(),     -- [EN: updated_at]
    CONSTRAINT chk_tai_len_co_duong_dan CHECK (loai_nguon <> 'UPLOAD' OR duong_dan_luu_tru IS NOT NULL),
    CONSTRAINT chk_nguon_ngoai_co_id   CHECK (loai_nguon = 'UPLOAD'
                                            OR (id_ngoai IS NOT NULL AND url_nguon IS NOT NULL))
);
CREATE UNIQUE INDEX uq_media_nguon_loai_nguon_id_ngoai ON media_nguon (loai_nguon, id_ngoai)
    WHERE id_ngoai IS NOT NULL;                 -- chống dán trùng link
CREATE UNIQUE INDEX uq_media_nguon_ma_bam_sha256 ON media_nguon (ma_bam_sha256)
    WHERE ma_bam_sha256 IS NOT NULL;             -- chống upload trùng file
CREATE INDEX idx_media_nguon_media_id ON media_nguon (media_id);

-- EN: media_links — links media to packages, materials, brands, knowledge docs
CREATE TABLE media_lien_ket (
    media_id       UUID NOT NULL REFERENCES media(id) ON DELETE CASCADE,  -- [EN: media_id]
    loai_doi_tuong VARCHAR(20) NOT NULL                    -- [EN: target_type]
                   CHECK (loai_doi_tuong IN ('PACKAGE','MATERIAL','BRAND','KNOWLEDGE_DOC')),
    ma_doi_tuong   VARCHAR(200) NOT NULL,                  -- [EN: target_key] id_nguon bên catalog (tron_goi.id slm_v2) hoặc doc_key
    vai_tro        VARCHAR(20) NOT NULL DEFAULT 'RELATED'  -- [EN: role]
                   CHECK (vai_tro IN ('MAIN_IMAGE','GALLERY','EXPLAINER','CASE_STUDY','GUIDE','RELATED')),
    thu_tu         INT NOT NULL DEFAULT 0,                 -- [EN: sort_order]
    PRIMARY KEY (media_id, loai_doi_tuong, ma_doi_tuong, vai_tro)
);
CREATE INDEX idx_media_lien_ket_loai_doi_tuong_ma_doi_tuong ON media_lien_ket (loai_doi_tuong, ma_doi_tuong);
-- Mỗi gói / vật tư chỉ một ảnh chính; ảnh thêm (kể cả ảnh slm_v2 khi đồng bộ catalog) là GALLERY
CREATE UNIQUE INDEX uq_media_lien_ket_anh_chinh ON media_lien_ket (loai_doi_tuong, ma_doi_tuong)
    WHERE vai_tro = 'MAIN_IMAGE';

SELECT gan_trigger_sua_luc(t) FROM (VALUES ('media'), ('media_nguon')) AS x(t);
