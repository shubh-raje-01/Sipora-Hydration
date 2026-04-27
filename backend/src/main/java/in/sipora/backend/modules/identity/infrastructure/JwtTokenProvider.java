package in.sipora.backend.modules.identity.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Handles JWT access token creation and validation.
 *
 * Token subject = user UUID string (not email).
 * This avoids re-hashing the email on every request and makes
 * SecurityUtils.requireCurrentUserId() a pure UUID parse, no DB call.
 *
 * Refresh tokens are separate — stored in Redis with TTL.
 * They are opaque UUID strings, not JWTs.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long accessTokenExpiryMs;

    public JwtTokenProvider(
            @Value("${sipora.jwt.secret}") String secret,
            @Value("${sipora.jwt.access-token-expiry-ms}") long accessTokenExpiryMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiryMs = accessTokenExpiryMs;
    }

    // Generate ==>
    /**
     * Issues a signed JWT access token.
     *
     * @param userId  the user's UUID — stored as the subject
     * @param email   stored as a claim for convenience (display, debugging)
     * @param role    user's role, stored as "role" claim
     * @return signed compact JWT string
     */
    public String generateAccessToken(UUID userId, String email, String role) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiryMs);

        return Jwts.builder()
                .setSubject(userId.toString())
                .setClaims(Map.of(
                        "email", email,
                        "role",  role
                ))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Generates an opaque refresh token (UUID).
     * Stored in Redis with TTL — not a JWT so it can be revoked instantly.
     */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    // Parse & validate ==>
    /**
     * Extracts the user UUID from the token subject.
     *
     * @throws JwtException if the token is invalid or expired
     */
    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public String extractEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * Returns true if the token signature is valid and it has not expired.
     */
    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT invalid: {}", e.getMessage());
            return false;
        }
    }

    // Internal ==>

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}