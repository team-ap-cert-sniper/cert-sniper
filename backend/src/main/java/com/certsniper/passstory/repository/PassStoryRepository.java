package com.certsniper.passstory.repository;

import com.certsniper.passstory.entity.PassStory;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PassStoryRepository extends JpaRepository<PassStory, Long> {

    Page<PassStory> findByStatus(String status, Pageable pageable);

    Page<PassStory> findByJmcdInAndStatus(Collection<String> jmcds, String status, Pageable pageable);

    Page<PassStory> findByUserIdAndStatusNot(Long userId, String status, Pageable pageable);

    /** admin 전체 글: 작성자 닉네임 부분 일치, 상태 일치 (각각 null이면 조건 없음, 삭제 글 포함) */
    @Query("""
            select p from PassStory p
            where (:status is null or p.status = :status)
              and (:nickname is null or p.userId in
                   (select u.id from User u where u.nickname like concat('%', :nickname, '%')))
            """)
    Page<PassStory> searchForAdmin(@Param("nickname") String nickname, @Param("status") String status, Pageable pageable);

    /** 저격 등급용: 공개 + 숨김 확정 전 글의 서로 다른 자격증 수 */
    @Query("""
            select count(distinct p.jmcd) from PassStory p
            where p.userId = :userId
              and (p.status = 'PUBLISHED' or (p.status = 'HIDDEN' and p.hiddenConfirmedAt is null))
            """)
    int countValidCerts(@Param("userId") Long userId);
}
