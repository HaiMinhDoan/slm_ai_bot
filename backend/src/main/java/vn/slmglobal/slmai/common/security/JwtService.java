package vn.slmglobal.slmai.common.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;
import vn.slmglobal.slmai.common.config.AppProperties;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Ký và kiểm tra JWT (HS256).
 * Access token mang vai trò; refresh token chỉ mang id người dùng và được lưu dạng băm trong DB
 * để xoay vòng và thu hồi được.
 */
@Service
public class JwtService {

    private static final String ISSUER = "slm-ai";
    private static final String CLAIM_TYPE = "typ";
    private static final String TYPE_ACCESS = "ACCESS";
    private static final String TYPE_REFRESH = "REFRESH";

    private final byte[] signerKey;
    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;

    public JwtService(AppProperties appProperties) {
        AppProperties.Jwt jwt = appProperties.jwt();
        String key = jwt == null ? null : jwt.signerKey();
        if (key == null || key.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "Thiếu hoặc quá ngắn JWT_SIGNER_KEY (cần tối thiểu 32 byte). Sinh bằng: openssl rand -base64 48");
        }
        this.signerKey = key.getBytes(StandardCharsets.UTF_8);
        this.accessTokenTtl = jwt.accessTokenTtl() == null ? Duration.ofHours(8) : jwt.accessTokenTtl();
        this.refreshTokenTtl = jwt.refreshTokenTtl() == null ? Duration.ofDays(7) : jwt.refreshTokenTtl();
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public Duration getRefreshTokenTtl() {
        return refreshTokenTtl;
    }

    public String generateAccessToken(AuthInfo authInfo) {
        Instant now = Instant.now();
        return sign(new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .subject(authInfo.id().toString())
                .jwtID(UUID.randomUUID().toString())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(accessTokenTtl)))
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .claim("email", authInfo.email())
                .claim("name", authInfo.fullName())
                .claim("roles", List.copyOf(authInfo.roles()))
                .build());
    }

    /** Refresh token có jti ngẫu nhiên, nên hai lần đăng nhập trong cùng một giây vẫn ra hai token khác nhau. */
    public String generateRefreshToken(UUID userId) {
        Instant now = Instant.now();
        return sign(new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .subject(userId.toString())
                .jwtID(UUID.randomUUID().toString())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(refreshTokenTtl)))
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .build());
    }

    /**
     * Kiểm tra chữ ký, hạn dùng và loại token. Chỉ access token hợp lệ mới trả về thông tin người dùng;
     * refresh token không dùng thay access token được.
     */
    public Optional<VerifiedToken> verifyAccessToken(String token) {
        return verify(token, TYPE_ACCESS).map(claims -> {
            try {
                List<String> roles = claims.getStringListClaim("roles");
                AuthInfo authInfo = new AuthInfo(
                        UUID.fromString(claims.getSubject()),
                        claims.getStringClaim("email"),
                        claims.getStringClaim("name"),
                        roles == null ? Set.of() : new HashSet<>(roles));
                return new VerifiedToken(authInfo, claims.getJWTID(), claims.getExpirationTime().toInstant());
            } catch (ParseException | IllegalArgumentException e) {
                return null;
            }
        });
    }

    public Optional<UUID> verifyRefreshToken(String token) {
        return verify(token, TYPE_REFRESH).map(claims -> UUID.fromString(claims.getSubject()));
    }

    /** Lấy token từ header "Authorization: Bearer ..."; null nếu sai định dạng. */
    public static String extractBearer(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7).trim();
        return token.isEmpty() ? null : token;
    }

    private Optional<JWTClaimsSet> verify(String token, String expectedType) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm())
                    || !jwt.verify(new MACVerifier(signerKey))) {
                return Optional.empty();
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date exp = claims.getExpirationTime();
            if (exp == null || exp.toInstant().isBefore(Instant.now())) {
                return Optional.empty();
            }
            if (!ISSUER.equals(claims.getIssuer()) || !expectedType.equals(claims.getStringClaim(CLAIM_TYPE))) {
                return Optional.empty();
            }
            return Optional.of(claims);
        } catch (ParseException | JOSEException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private String sign(JWTClaimsSet claims) {
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(signerKey));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Không ký được JWT", e);
        }
    }

    public record VerifiedToken(AuthInfo authInfo, String jti, Instant expiresAt) {
    }
}
