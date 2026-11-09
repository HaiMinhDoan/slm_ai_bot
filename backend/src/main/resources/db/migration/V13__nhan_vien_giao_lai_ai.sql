-- Nhân viên bấm "Chuyển cho AI" (2026-11-09): lượt AI đầu tiên sau đó được báo là nhân viên vừa giao lại; AI vẫn
-- muốn chuyển nhân viên ở lượt đó thì thành nháp chờ duyệt thay vì chuyển ngay (tránh vòng chuyển qua lại).
-- Xoá khi lượt đó được chốt, như tu_tra_lai_luc.
ALTER TABLE hoi_thoai
    ADD COLUMN nv_giao_lai_luc TIMESTAMPTZ;                  -- [EN: staff_handback_at]
