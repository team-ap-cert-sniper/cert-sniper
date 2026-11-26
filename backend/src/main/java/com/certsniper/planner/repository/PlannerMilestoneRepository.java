package com.certsniper.planner.repository;

import com.certsniper.planner.entity.PlannerMilestone;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlannerMilestoneRepository extends JpaRepository<PlannerMilestone, Long> {

    /** 일정바들의 세부 일정 중 조회 기간 안의 것 */
    List<PlannerMilestone> findAllByBarIdInAndPointDateBetweenOrderByPointDateAscIdAsc(
            Collection<Long> barIds, LocalDate from, LocalDate to);
}
