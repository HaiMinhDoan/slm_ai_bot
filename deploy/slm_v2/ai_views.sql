-- View và tài khoản chỉ đọc cho hệ thống AI trên DB slm_v2 (kế hoạch mục 4.2.5).
--
-- Chạy bằng tài khoản quản trị của slm_v2, trong một transaction:
--   AI_SYNC_PASSWORD='<mật khẩu mạnh>' psql -h <host> -U postgres -d slm_v2 -v ON_ERROR_STOP=1 -f ai_views.sql
--
-- Chỉ TẠO view và role, không sửa bảng hay dữ liệu nào của slm_v2.
-- View chạy bằng quyền của chủ view, nên ai_sync chỉ có SELECT trên view, không có quyền trên bảng gốc:
-- giá nhập (giaNhap), biên lợi nhuận (gm, gm_tong) và giá vật tư trong gói không bao giờ ra khỏi slm_v2.

\getenv ai_sync_password AI_SYNC_PASSWORD
\if :{?ai_sync_password}
\else
  \echo 'Thiếu biến môi trường AI_SYNC_PASSWORD'
  \quit
\endif

BEGIN;

CREATE VIEW ai_v_nganh_hang AS
SELECT id, ma, ten, sdt_sale, sdt_tech, trang_thai FROM nganh_hang;

CREATE VIEW ai_v_co_so AS
SELECT id, ma, ten, dc_van_phong, sdt, trang_thai FROM co_so;

CREATE VIEW ai_v_thuong_hieu AS
SELECT id, ten, ten_quoc_te, quoc_gia, mo_ta, tep_tin_id, trang_thai FROM thuong_hieu;

CREATE VIEW ai_v_nhom_vat_tu AS                       -- bỏ gm
SELECT id, ma, ten, nghanh_hang_id, thuoc_tinh_rieng, vat_tu_chinh, trang_thai FROM nhom_vat_tu;

CREATE VIEW ai_v_vat_tu AS                            -- bỏ gm, sheet_link, mo_ta_hop_dong
SELECT id, ma, ten, nhom_vat_tu_id, thuong_hieu_id, don_vi, mo_ta_bao_gia,
       du_lieu_rieng, thoi_gian_bao_hanh, vat_tu_chinh, trang_thai
FROM vat_tu;

CREATE VIEW ai_v_gia_hien_hanh AS                     -- chỉ giá bán, chỉ dòng mới nhất (bỏ giaNhap)
SELECT DISTINCT ON (vat_tu_id) vat_tu_id, tao_luc AS hieu_luc_tu,
       (SELECT jsonb_agg(jsonb_build_object('maCoSo', e->>'maCoSo', 'giaBan', e->'giaBan'))
          FROM jsonb_array_elements(ds_gia) e) AS gia_ban
FROM thong_tin_gia
WHERE trang_thai = 1
ORDER BY vat_tu_id, tao_luc DESC, id DESC;

CREATE VIEW ai_v_anh_vat_tu AS
SELECT id, vat_tu_id, tep_tin_id, anh_chinh, trang_thai FROM anh_vat_tu;

CREATE VIEW ai_v_nhom_tron_goi AS
SELECT id, ten, nghanh_hang_id, thuong_hieu_tam_pin, thuong_hieu_inverter,
       thuong_hieu_pin_luu_tru, trang_thai
FROM nhom_tron_goi;

CREATE VIEW ai_v_tron_goi AS                          -- bỏ gm_tong
SELECT id, co_so_id, nhom_tron_goi_id, ten, tep_tin_id, loai_he_thong, loai_pha,
       cong_suat_he_thong, san_luong_toi_thieu, san_luong_toi_da, mo_ta,
       tong_gia, gia_khung_sat, ban_chay, trang_thai
FROM tron_goi;

CREATE VIEW ai_v_vat_tu_tron_goi AS                   -- bỏ gia, gm
SELECT id, tron_goi_id, vat_tu_id, so_luong, thoi_gian_bao_hanh, duoc_bao_hanh, duoc_xem, trang_thai
FROM vat_tu_tron_goi;

CREATE VIEW ai_v_tep_tin_anh AS                       -- chỉ file ảnh mà gói / vật tư / hãng dùng
SELECT t.id, t.duong_dan, t.loai_tep_tin, t.duoi_tep, t.kich_co, t.sua_luc
FROM tep_tin t
WHERE t.id IN (SELECT tep_tin_id FROM tron_goi
               UNION SELECT tep_tin_id FROM anh_vat_tu
               UNION SELECT tep_tin_id FROM thuong_hieu);

CREATE VIEW ai_v_mien AS                              -- tên miền web theo cơ sở: link sản phẩm theo miền khách
SELECT id, ten_mien, co_so_id, trang_thai FROM mien;

CREATE ROLE ai_sync LOGIN PASSWORD :'ai_sync_password';
GRANT CONNECT ON DATABASE slm_v2 TO ai_sync;
GRANT USAGE ON SCHEMA public TO ai_sync;
GRANT SELECT ON ai_v_nganh_hang, ai_v_co_so, ai_v_thuong_hieu, ai_v_nhom_vat_tu, ai_v_vat_tu,
                ai_v_gia_hien_hanh, ai_v_anh_vat_tu, ai_v_nhom_tron_goi, ai_v_tron_goi,
                ai_v_vat_tu_tron_goi, ai_v_tep_tin_anh, ai_v_mien
      TO ai_sync;

COMMIT;

-- Kiểm tra sau khi chạy (đăng nhập bằng ai_sync):
--   SELECT count(*) FROM ai_v_tron_goi;          -- được
--   SELECT count(*) FROM tron_goi;               -- phải báo "permission denied"
--   SELECT gm_tong FROM ai_v_tron_goi;           -- phải báo "column does not exist"
