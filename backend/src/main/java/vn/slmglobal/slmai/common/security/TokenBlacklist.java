package vn.slmglobal.slmai.common.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Access token đã đăng xuất (JWT không tự thu hồi được), lưu theo jti tới lúc token hết hạn.
 * Lưu trong bộ nhớ, nên chỉ đúng khi chạy một instance; mất khi khởi động lại (token vẫn hết hạn theo TTL).
 * Khi chạy nhiều instance, chuyển sang Redis (mục 30).
 */
@Component
public class TokenBlacklist {

    private final Cache<String, Instant> revoked = Caffeine.newBuilder()
            .maximumSize(100_000)
            .expireAfter(new Expiry<String, Instant>() {
                @Override
                public long expireAfterCreate(String jti, Instant expiresAt, long currentTime) {
                    return Math.max(0, Duration.between(Instant.now(), expiresAt).toNanos());
                }

                @Override
                public long expireAfterUpdate(String jti, Instant expiresAt, long currentTime, long currentDuration) {
                    return expireAfterCreate(jti, expiresAt, currentTime);
                }

                @Override
                public long expireAfterRead(String jti, Instant expiresAt, long currentTime, long currentDuration) {
                    return currentDuration;
                }
            })
            .build();

    public void revoke(String jti, Instant expiresAt) {
        if (jti != null && expiresAt != null && expiresAt.isAfter(Instant.now())) {
            revoked.put(jti, expiresAt);
        }
    }

    public boolean isRevoked(String jti) {
        return jti != null && revoked.getIfPresent(jti) != null;
    }
}
