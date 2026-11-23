package com.certsniper.user.repository;

import com.certsniper.user.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByNickname(String nickname);

    /** admin 회원 목록: 이메일·이름·닉네임 부분 일치, 상태 일치 (각각 null이면 조건 없음) */
    @Query("""
            select u from User u
            where (:keyword is null or u.email like concat('%', :keyword, '%')
                   or u.name like concat('%', :keyword, '%') or u.nickname like concat('%', :keyword, '%'))
              and (:status is null or u.status = :status)
            """)
    Page<User> search(@Param("keyword") String keyword, @Param("status") String status, Pageable pageable);
}
