package com.certsniper.user.repository;

import com.certsniper.user.entity.UserCertificate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserCertificateRepository extends JpaRepository<UserCertificate, Long> {

    List<UserCertificate> findByUserId(Long userId);

    // 벌크 삭제: 바로 실행되어야 같은 트랜잭션의 재등록이 UNIQUE(user_id, jmcd)에 걸리지 않는다
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from UserCertificate c where c.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
