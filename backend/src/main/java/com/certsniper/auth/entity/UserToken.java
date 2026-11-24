package com.certsniper.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 로그인 유지 토큰 (user_tokens) */
@Entity
@Table(name = "user_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token_hash", nullable = false, columnDefinition = "char(64)")
    private String tokenHash;

    @Column(name = "token_family", nullable = false, columnDefinition = "char(36)")
    private String tokenFamily;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** 새 Refresh Token 저장 (원문 대신 해시만 보관). 같은 로그인에서 재발급된 토큰은 family가 같다. */
    public static UserToken issue(Long userId, String tokenHash, String tokenFamily, LocalDateTime expiresAt) {
        UserToken token = new UserToken();
        token.userId = userId;
        token.tokenHash = tokenHash;
        token.tokenFamily = tokenFamily;
        token.expiresAt = expiresAt;
        return token;
    }

    public boolean isExpired(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }
}