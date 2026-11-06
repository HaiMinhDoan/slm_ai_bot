package vn.slmglobal.slmai.services.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.customer.entity.ChannelIdentity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChannelIdentityRepository extends JpaRepository<ChannelIdentity, UUID> {

    Optional<ChannelIdentity> findByChannelAndAccountIdAndExternalUserId(ChannelCode channel, String accountId,
                                                                          String externalUserId);

    /** Định danh chưa có tên kênh, khách nhắn gần nhất trước */
    @Query("select i.id from ChannelIdentity i where i.displayName is null order by i.lastInboundAt desc nulls last")
    List<UUID> findIdsWithoutDisplayName();

    /** Ghi tên kênh nếu chưa có (không đè tên đã có) */
    @Transactional
    @Modifying
    @Query("update ChannelIdentity i set i.displayName = :name where i.id = :id and i.displayName is null")
    int setDisplayNameIfMissing(@Param("id") UUID id, @Param("name") String name);
}
