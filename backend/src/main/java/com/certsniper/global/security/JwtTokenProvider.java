package com.certsniper.global.security;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import javax.crypto.SecretKey;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Access Token(JWT) 발급·검증, Refresh Token 생성·해시 */
@Component
public class JwtTokenProvider {

    private static final String ROLE = "role";

    private final SecretKey key;
    @Getter
    private final Duration accessTokenValidity;
    @Getter
    private final Duration refreshTokenValidity;
    private final SecureRandom random = new SecureRandom();

    public JwtTokenProvider(@Value("${app.jwt.secret}") String secret,
                            @Value("${app.jwt.access-token-minutes}") long accessTokenMinutes,
                            @Value("${app.jwt.refresh-token-days}") long refreshTokenDays) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidity = Duration.ofMinutes(accessTokenMinutes);
        this.refreshTokenValidity = Duration.ofDays(refreshTokenDays);
    }

    public record TokenInfo(Long userId, String role) {
    }

    public String createAccessToken(Long userId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(ROLE, role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenValidity)))
                .signWith(key)
                .compact();
    }

    public TokenInfo parse(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return new TokenInfo(Long.valueOf(claims.getSubject()), claims.get(ROLE, String.class));
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    /** user_tokens 에는 원문 대신 hash() 값을 저장한다. */
    public String createRefreshToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
