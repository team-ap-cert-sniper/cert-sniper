package com.certsniper.planner.repository;

import com.certsniper.planner.entity.Planner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlannerRepository extends JpaRepository<Planner, Long> {

    // 탈퇴 시 벌크 삭제
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Planner p where p.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
