package com.certsniper.user.repository;

import com.certsniper.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    /** 이메일 중복 확인 (이메일은 소문자로 정규화해 저장) */
    boolean existsByEmail(String email);

    /** 닉네임 중복 확인 (DB 정렬 규칙상 대소문자 구분 없이 비교) */
    boolean existsByNickname(String nickname);

    Optional<User> findByEmail(String email);
}