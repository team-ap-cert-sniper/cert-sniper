package com.certsniper.auth.repository;

import com.certsniper.auth.entity.UserToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {

    Optional<UserToken> findByTokenHash(String tokenHash);

    /** 탈퇴·비밀번호 재설정·정지 시 그 회원의 로그인 토큰 전부 폐기 */
    void deleteByUserId(Long userId);
}