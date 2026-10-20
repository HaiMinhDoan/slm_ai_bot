package vn.slmglobal.slmai.services.auth.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.slmglobal.slmai.services.auth.entity.User;
import vn.slmglobal.slmai.services.auth.entity.UserStatus;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    /** Tìm không phân biệt hoa thường qua cột sinh email_chuan_hoa (có unique index). */
    Optional<User> findByEmailNormalized(String emailNormalized);

    boolean existsByEmailNormalized(String emailNormalized);

    /** keyword đã chuẩn hóa dạng "%...%" chữ thường, hoặc null. */
    @Query("""
            SELECT u FROM User u
            WHERE (:keyword IS NULL OR LOWER(u.fullName) LIKE :keyword OR u.emailNormalized LIKE :keyword)
              AND (:status IS NULL OR u.status = :status)
            ORDER BY u.createdAt DESC
            """)
    Page<User> adminFilter(@Param("keyword") String keyword,
                           @Param("status") UserStatus status,
                           Pageable pageable);
}
