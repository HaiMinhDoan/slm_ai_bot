package vn.slmglobal.slmai.services.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.slmglobal.slmai.services.conversation.entity.ChannelCode;
import vn.slmglobal.slmai.services.customer.entity.ChannelIdentity;

import java.util.Optional;
import java.util.UUID;

public interface ChannelIdentityRepository extends JpaRepository<ChannelIdentity, UUID> {

    Optional<ChannelIdentity> findByChannelAndAccountIdAndExternalUserId(ChannelCode channel, String accountId,
                                                                          String externalUserId);
}
