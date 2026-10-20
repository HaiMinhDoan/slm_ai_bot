package vn.slmglobal.slmai.services.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.slmglobal.slmai.services.auth.entity.UserRole;
import vn.slmglobal.slmai.services.auth.entity.UserStatus;

import java.util.List;
import java.util.UUID;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRole.Id> {

    @Query("SELECT ur.role.code FROM UserRole ur WHERE ur.user.id = :userId ORDER BY ur.role.code")
    List<String> findRoleCodesByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(ur) > 0 FROM UserRole ur WHERE ur.role.code = :roleCode AND ur.user.status = :status")
    boolean existsUserWithRole(@Param("roleCode") String roleCode, @Param("status") UserStatus status);

    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.user.id = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);
}
