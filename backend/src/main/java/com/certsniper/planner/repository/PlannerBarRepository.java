package com.certsniper.planner.repository;

import com.certsniper.planner.entity.PlannerBar;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlannerBarRepository extends JpaRepository<PlannerBar, Long> {

    /** 조회 기간과 겹치는 내 일정바 (시작일 <= to 이고 종료일 >= from) */
    @Query("select b from PlannerBar b where b.userId = :userId and b.startDate <= :to and b.endDate >= :from"
            + " order by b.startDate asc, b.id asc")
    List<PlannerBar> findOverlapping(@Param("userId") Long userId, @Param("from") LocalDate from,
                                     @Param("to") LocalDate to);

    /** AI 재생성: 이 목표 자격증의 AI 일정바만 삭제 (세부 일정은 DB의 ON DELETE CASCADE로 같이 삭제) */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from PlannerBar b where b.targetExamId = :targetExamId and b.source = 'AI'")
    void deleteAiBarsByTargetExamId(@Param("targetExamId") Long targetExamId);

    // 탈퇴 시 벌크 삭제: 세부 일정(마일스톤)은 DB의 ON DELETE CASCADE로 같이 삭제된다
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from PlannerBar b where b.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
