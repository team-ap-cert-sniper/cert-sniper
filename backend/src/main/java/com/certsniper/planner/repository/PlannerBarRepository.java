package com.certsniper.planner.repository;

import com.certsniper.planner.entity.PlannerBar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlannerBarRepository extends JpaRepository<PlannerBar, Long> {

    // 탈퇴 시 벌크 삭제: 세부 일정(마일스톤)은 DB의 ON DELETE CASCADE로 같이 삭제된다
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from PlannerBar b where b.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
