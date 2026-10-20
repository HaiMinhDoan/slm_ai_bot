package vn.slmglobal.slmai.common.security;

import org.junit.jupiter.api.Test;
import vn.slmglobal.slmai.common.config.AppProperties;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String KEY = "0123456789abcdef0123456789abcdef-test-key";

    private static JwtService service(String key, Duration accessTtl) {
        return new JwtService(new AppProperties(
                new AppProperties.Jwt(key, accessTtl, Duration.ofDays(7)), null, null, null));
    }

    private static AuthInfo admin() {
        return new AuthInfo(UUID.randomUUID(), "admin@example.com", "Quản trị", Set.of(RoleCode.SYSTEM_ADMIN));
    }

    @Test
    void accessTokenRoundTrip() {
        JwtService jwt = service(KEY, Duration.ofHours(1));
        AuthInfo info = admin();

        JwtService.VerifiedToken verified = jwt.verifyAccessToken(jwt.generateAccessToken(info)).orElseThrow();

        assertThat(verified.authInfo()).isEqualTo(info);
        assertThat(verified.jti()).isNotBlank();
    }

    @Test
    void expiredAccessTokenIsRejected() {
        JwtService jwt = service(KEY, Duration.ofSeconds(-1));
        assertThat(jwt.verifyAccessToken(jwt.generateAccessToken(admin()))).isEmpty();
    }

    @Test
    void refreshTokenCannotBeUsedAsAccessToken() {
        JwtService jwt = service(KEY, Duration.ofHours(1));
        String refresh = jwt.generateRefreshToken(UUID.randomUUID());

        assertThat(jwt.verifyAccessToken(refresh)).isEmpty();
        assertThat(jwt.verifyRefreshToken(refresh)).isPresent();
    }

    @Test
    void tokenSignedWithOtherKeyIsRejected() {
        String token = service(KEY, Duration.ofHours(1)).generateAccessToken(admin());
        JwtService other = service("another-key-another-key-another-key-123", Duration.ofHours(1));

        assertThat(other.verifyAccessToken(token)).isEmpty();
    }

    @Test
    void refreshTokensAreUniqueEvenInSameSecond() {
        JwtService jwt = service(KEY, Duration.ofHours(1));
        UUID userId = UUID.randomUUID();
        assertThat(jwt.generateRefreshToken(userId)).isNotEqualTo(jwt.generateRefreshToken(userId));
    }

    @Test
    void shortSignerKeyFailsFast() {
        assertThatThrownBy(() -> service("too-short", Duration.ofHours(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void extractBearer() {
        assertThat(JwtService.extractBearer("Bearer abc")).isEqualTo("abc");
        assertThat(JwtService.extractBearer("Basic abc")).isNull();
        assertThat(JwtService.extractBearer("Bearer ")).isNull();
        assertThat(JwtService.extractBearer(null)).isNull();
    }
}
