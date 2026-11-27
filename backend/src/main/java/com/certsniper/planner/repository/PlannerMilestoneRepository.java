package com.certsniper.planner.repository;

import com.certsniper.planner.entity.PlannerMilestone;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlannerMilestoneRepository extends JpaRepository<PlannerMilestone, Long> {

    /** 일정바들의 세부 일정 중 조회 기간 안의 것 */
    List<PlannerMilestone> findAllByBarIdInAndPointDateBetweenOrderByPointDateAscIdAsc(
            Collection<Long> barIds, LocalDate from, LocalDate to);

    /** 일정바 기간을 줄였을 때 기간 밖으로 나간 세부 일정 삭제 */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from PlannerMilestone m where m.barId = :barId and (m.pointDate < :start or m.pointDate > :end)")
    int deleteOutside(@Param("barId") Long barId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
