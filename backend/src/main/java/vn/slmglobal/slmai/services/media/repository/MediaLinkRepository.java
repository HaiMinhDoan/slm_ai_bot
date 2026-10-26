package vn.slmglobal.slmai.services.media.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import vn.slmglobal.slmai.services.media.entity.LinkRole;
import vn.slmglobal.slmai.services.media.entity.LinkTargetType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * media_lien_ket: khoá chính gồm 4 cột và ràng buộc "mỗi đối tượng một ảnh chính", nên ghi bằng
 * {@code ON CONFLICT DO NOTHING} (lỗi trùng khoá trong PostgreSQL làm hỏng transaction đang chạy).
 */
@Repository
@RequiredArgsConstructor
public class MediaLinkRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public record Link(UUID mediaId, LinkTargetType targetType, String targetKey, LinkRole role, int sortOrder) {
    }

    /** Thêm liên kết; trả false nếu đã có (hoặc đối tượng đã có ảnh chính khác). */
    public boolean insertIfAbsent(UUID mediaId, LinkTargetType targetType, String targetKey, LinkRole role,
                                  int sortOrder) {
        return jdbc.update("""
                INSERT INTO media_lien_ket (media_id, loai_doi_tuong, ma_doi_tuong, vai_tro, thu_tu)
                VALUES (:mediaId, :type, :key, :role, :order)
                ON CONFLICT DO NOTHING""", new MapSqlParameterSource()
                .addValue("mediaId", mediaId)
                .addValue("type", targetType.name())
                .addValue("key", targetKey)
                .addValue("role", role.name())
                .addValue("order", sortOrder)) > 0;
    }

    /** Media đang là ảnh chính của đối tượng (nếu có) */
    public Optional<UUID> mainImageOf(LinkTargetType targetType, String targetKey) {
        return jdbc.queryForList("""
                SELECT media_id FROM media_lien_ket
                WHERE loai_doi_tuong = :type AND ma_doi_tuong = :key AND vai_tro = 'MAIN_IMAGE'""",
                new MapSqlParameterSource().addValue("type", targetType.name()).addValue("key", targetKey),
                UUID.class).stream().findFirst();
    }

    public List<Link> findByMediaIds(Collection<UUID> mediaIds) {
        if (mediaIds.isEmpty()) {
            return List.of();
        }
        return jdbc.query("""
                SELECT media_id, loai_doi_tuong, ma_doi_tuong, vai_tro, thu_tu FROM media_lien_ket
                WHERE media_id IN (:ids) ORDER BY loai_doi_tuong, ma_doi_tuong, thu_tu""",
                new MapSqlParameterSource("ids", mediaIds),
                (rs, i) -> new Link(rs.getObject("media_id", UUID.class),
                        LinkTargetType.valueOf(rs.getString("loai_doi_tuong")),
                        rs.getString("ma_doi_tuong"),
                        LinkRole.valueOf(rs.getString("vai_tro")),
                        rs.getInt("thu_tu")));
    }
}
