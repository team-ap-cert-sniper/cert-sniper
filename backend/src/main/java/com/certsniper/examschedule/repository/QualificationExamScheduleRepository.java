package com.certsniper.examschedule.repository;

import com.certsniper.examschedule.entity.QualificationExamSchedule;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QualificationExamScheduleRepository extends JpaRepository<QualificationExamSchedule, Long> {

    /** 종목별 시험일정 (시험일 순) */
    List<QualificationExamSchedule> findByJmcdInOrderByExamStartDateAscIdAsc(Collection<String> jmcds);
}
