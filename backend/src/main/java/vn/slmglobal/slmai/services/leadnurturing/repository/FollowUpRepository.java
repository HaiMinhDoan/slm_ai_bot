package vn.slmglobal.slmai.services.leadnurturing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import vn.slmglobal.slmai.services.leadnurturing.entity.FollowUp;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FollowUpRepository extends JpaRepository<FollowUp, UUID> {

    Optional<FollowUp> findFirstByConversationIdAndKindAndStatusOrderByCreatedAtDesc(UUID conversationId,
                                                                                    FollowUp.Kind kind,
                                                                                    FollowUp.Status status);

    long countByConversationIdAndKindAndWindowAnchor(UUID conversationId, FollowUp.Kind kind,
                                                     OffsetDateTime windowAnchor);

    long countByConversationIdAndKindAndWindowAnchorAndStatusIn(UUID conversationId, FollowUp.Kind kind,
                                                                OffsetDateTime windowAnchor,
                                                                Collection<FollowUp.Status> statuses);

    @Modifying
    @Query("""
            UPDATE FollowUp f SET f.status = :to, f.reason = :reason, f.processedAt = :at
            WHERE f.conversationId = :conversationId AND f.status = :from""")
    int updateStatus(UUID conversationId, FollowUp.Status from, FollowUp.Status to, String reason,
                     OffsetDateTime at);

    /** Huỷ lịch nhắc đang chờ của hội thoại (khách nhắn lại, nhân viên nhắn); cần transaction của nơi gọi */
    default int cancelPending(UUID conversationId, String reason) {
        return updateStatus(conversationId, FollowUp.Status.PENDING, FollowUp.Status.CANCELLED, reason,
                OffsetDateTime.now());
    }

    /** Tin nhắc đã ghi của một hội thoại (trang chat đánh dấu "Tin nhắc") */
    @Query("SELECT f.messageId FROM FollowUp f WHERE f.conversationId = :conversationId AND f.messageId IS NOT NULL")
    List<UUID> messageIds(UUID conversationId);

    /**
     * Hội thoại tới lúc nhắc (mục 56.9): AI đang phụ trách, không có lượt chờ, cửa sổ 24 giờ còn đủ lâu, tin cuối của
     * trang là tin AI đã gửi từ trước {@code sentBefore} (khách chưa trả lời), không có nháp chờ duyệt, trong cửa sổ
     * này chưa có lần nhắc đang chờ / bị bỏ / lỗi và chưa quá số lần ({@code maxCount} 0 = không giới hạn).
     *
     * @param windowStart tin cuối của khách phải sau mốc này (now − cửa sổ + khoảng dự phòng)
     */
    @Query(nativeQuery = true, value = """
            SELECT h.id FROM hoi_thoai h
            WHERE h.trang_thai = 'OPEN' AND h.nguoi_phu_trach = 'AI_ACTIVE' AND h.che_do_tra_loi = 'AUTO'
              AND h.kenh IN (:channels) AND h.han_tra_loi_luc IS NULL
              AND h.tin_khach_cuoi_luc > :windowStart
              AND (SELECT t.nguoi_gui = 'AI' AND t.gui_luc <= :sentBefore
                   FROM tin_nhan t
                   WHERE t.hoi_thoai_id = h.id AND t.chieu = 'OUT' AND t.trang_thai = 'SENT'
                     AND t.gui_luc >= h.tin_khach_cuoi_luc
                   ORDER BY t.gui_luc DESC LIMIT 1)
              AND NOT EXISTS (SELECT 1 FROM tin_nhan d
                              WHERE d.hoi_thoai_id = h.id AND d.chieu = 'OUT' AND d.trang_thai = 'DRAFT'
                                AND d.tao_luc >= h.tin_khach_cuoi_luc)
              AND NOT EXISTS (SELECT 1 FROM lich_nhac_khach f
                              WHERE f.hoi_thoai_id = h.id AND f.loai = 'NHAC_KHACH'
                                AND f.moc_tin_khach = h.tin_khach_cuoi_luc
                                AND f.trang_thai IN ('PENDING', 'SKIPPED', 'FAILED'))
              AND (:maxCount <= 0 OR (SELECT count(*) FROM lich_nhac_khach f
                                      WHERE f.hoi_thoai_id = h.id AND f.loai = 'NHAC_KHACH'
                                        AND f.moc_tin_khach = h.tin_khach_cuoi_luc
                                        AND f.trang_thai IN ('SENT', 'DRAFT')) < :maxCount)
            ORDER BY h.tin_khach_cuoi_luc
            LIMIT :limit""")
    List<UUID> findDueForNudge(Collection<String> channels, OffsetDateTime windowStart, OffsetDateTime sentBefore,
                               int maxCount, int limit);

    /**
     * Hội thoại vừa hết cửa sổ 24 giờ (tin cuối của khách trong khoảng {@code closedAfter}..{@code closedBefore}) mà
     * tin cuối của trang là của AI, khách chưa trả lời, chưa báo nhân viên cho cửa sổ này, và đáng theo: có số điện
     * thoại, đã đề xuất gói, mức WARM / HOT hoặc đã trả lời từ 2 câu.
     */
    @Query(nativeQuery = true, value = """
            SELECT h.id FROM hoi_thoai h
            JOIN khach_hang k ON k.id = h.khach_hang_id
            LEFT JOIN ho_so_tu_van p ON p.hoi_thoai_id = h.id
            WHERE h.trang_thai = 'OPEN' AND h.nguoi_phu_trach = 'AI_ACTIVE'
              AND h.tin_khach_cuoi_luc <= :closedBefore AND h.tin_khach_cuoi_luc > :closedAfter
              AND (SELECT t.nguoi_gui = 'AI'
                   FROM tin_nhan t
                   WHERE t.hoi_thoai_id = h.id AND t.chieu = 'OUT' AND t.trang_thai = 'SENT'
                     AND t.gui_luc >= h.tin_khach_cuoi_luc
                   ORDER BY t.gui_luc DESC LIMIT 1)
              AND NOT EXISTS (SELECT 1 FROM tin_nhan i
                              WHERE i.hoi_thoai_id = h.id AND i.chieu = 'IN' AND i.trang_thai = 'RECEIVED')
              AND (k.sdt IS NOT NULL OR cardinality(p.tron_goi_de_xuat_ids) > 0
                   OR p.muc_tiem_nang IN ('HOT', 'WARM') OR p.so_cau_da_tra_loi >= 2)
              AND NOT EXISTS (SELECT 1 FROM lich_nhac_khach f
                              WHERE f.hoi_thoai_id = h.id AND f.loai = 'BAO_NHAN_VIEN'
                                AND f.moc_tin_khach = h.tin_khach_cuoi_luc)
            LIMIT :limit""")
    List<UUID> findSilentAfterWindow(OffsetDateTime closedBefore, OffsetDateTime closedAfter, int limit);
}
