package com.certsniper.planner.repository;

import com.certsniper.planner.entity.PersonalSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalScheduleRepository extends JpaRepository<PersonalSchedule, Long> {

    long countByUserId(Long userId);
}
