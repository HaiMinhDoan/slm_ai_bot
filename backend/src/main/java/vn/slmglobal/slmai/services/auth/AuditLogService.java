package vn.slmglobal.slmai.services.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.slmglobal.slmai.common.web.PageResponse;
import vn.slmglobal.slmai.services.auth.dto.AuditLogResponse;
import vn.slmglobal.slmai.services.auth.entity.AuditLog;
import vn.slmglobal.slmai.services.auth.repository.AuditLogRepository;
import vn.slmglobal.slmai.services.auth.repository.UserRepository;

import java.util.UUID;

/**
 * Ghi và tra cứu nhật ký thao tác nhạy cảm: đổi vai trò, khóa tài khoản, phát hành kiến thức,
 * bật gửi tự động, bật tùy chọn rủi ro cao... (mục 5.9, 34, 54.6).
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Ghi trong cùng transaction với thao tác chính (bắt buộc phải có transaction):
     * thao tác rollback thì nhật ký cũng không còn, và không ghi được nhật ký thì thao tác cũng không được lưu.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(UUID actorId, String action, String targetType, Object targetKey, Object before, Object after) {
        auditLogRepository.save(AuditLog.builder()
                .actor(actorId == null ? null : userRepository.getReferenceById(actorId))
                .action(action)
                .targetType(targetType)
                .targetKey(targetKey == null ? null : String.valueOf(targetKey))
                .beforeData(before == null ? null : objectMapper.valueToTree(before))
                .afterData(after == null ? null : objectMapper.valueToTree(after))
                .ipAddress(currentClientIp())
                .build());
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> filter(UUID actorId, String action, String targetType, Pageable pageable) {
        return PageResponse.of(
                auditLogRepository.adminFilter(actorId, blankToNull(action), blankToNull(targetType), pageable),
                a -> new AuditLogResponse(
                        a.getId(),
                        a.getActor() == null ? null : a.getActor().getId(),
                        a.getActor() == null ? null : a.getActor().getFullName(),
                        a.getAction(),
                        a.getTargetType(),
                        a.getTargetKey(),
                        a.getBeforeData(),
                        a.getAfterData(),
                        a.getIpAddress(),
                        a.getCreatedAt()));
    }

    /** server.forward-headers-strategy=framework đã thay remoteAddr bằng IP thật từ X-Forwarded-For. */
    private static String currentClientIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs.getRequest().getRemoteAddr();
        }
        return null;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
