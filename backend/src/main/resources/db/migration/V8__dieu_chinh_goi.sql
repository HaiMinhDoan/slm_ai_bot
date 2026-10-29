-- Điều chỉnh gói trên admin (2026-10-29): sửa số liệu, tắt, xoá mềm một gói đồng bộ từ slm_v2.
-- dm_tron_goi vẫn là bản sao y nguyên slm_v2 (slm_v2 chỉ đọc); điều chỉnh của bot nằm riêng ở bảng này,
-- CatalogSyncJob không bao giờ ghi vào đây nên đồng bộ không đè mất. Đọc gói thì ghép hai bảng (CatalogRepository).
-- Cột số liệu NULL = dùng giá trị slm_v2; trường nhập trùng giá trị slm_v2 thì lưu NULL.

-- EN: catalog_package_overrides — admin edits on top of synced packages, never touched by the sync job
CREATE TABLE dm_tron_goi_dieu_chinh (
    id_nguon          INT PRIMARY KEY REFERENCES dm_tron_goi(id_nguon) ON DELETE CASCADE,  -- [EN: source_id]
    ten               VARCHAR(400),                                                -- [EN: name]
    loai_he_thong     VARCHAR(16) CHECK (loai_he_thong IN ('ON_GRID','HYBRID')),   -- [EN: system_type]
    so_pha            SMALLINT    CHECK (so_pha IN (1,3)),                         -- [EN: phase]
    cong_suat_kwp     NUMERIC(8,3) CHECK (cong_suat_kwp > 0),                      -- [EN: capacity_kwp]
    so_tam_pin        INT          CHECK (so_tam_pin > 0),                         -- [EN: panel_count]
    cong_suat_tam_wp  NUMERIC(8,1) CHECK (cong_suat_tam_wp > 0),                   -- [EN: panel_wp]
    luu_tru_kwh       NUMERIC(8,2) CHECK (luu_tru_kwh >= 0),                       -- [EN: storage_kwh]
    tong_gia          NUMERIC(14,0) CHECK (tong_gia > 0),                          -- [EN: total_price]
    ban_chay          BOOLEAN,                                                     -- [EN: best_seller]
    tat               BOOLEAN NOT NULL DEFAULT false,                              -- [EN: disabled] bot không gợi ý, không báo giá
    ghi_chu           TEXT,                                                        -- [EN: note] lý do sửa / tắt
    xoa_luc           TIMESTAMPTZ,                                                 -- [EN: deleted_at] xoá mềm: ẩn khỏi danh sách và bot, khôi phục được
    xoa_boi           VARCHAR(255),                                                -- [EN: deleted_by] email
    sua_boi           VARCHAR(255),                                                -- [EN: updated_by] email người sửa gần nhất
    tao_luc           TIMESTAMPTZ NOT NULL DEFAULT now(),                          -- [EN: created_at]
    sua_luc           TIMESTAMPTZ NOT NULL DEFAULT now()                           -- [EN: updated_at]
);

SELECT gan_trigger_sua_luc('dm_tron_goi_dieu_chinh');
