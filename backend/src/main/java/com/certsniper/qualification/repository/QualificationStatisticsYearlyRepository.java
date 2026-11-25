package com.certsniper.qualification.repository;

import com.certsniper.qualification.entity.QualificationStatisticsYearly;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QualificationStatisticsYearlyRepository extends JpaRepository<QualificationStatisticsYearly, Long> {

    List<QualificationStatisticsYearly> findByJmcdInOrderByIdAsc(Collection<String> jmcds);

    List<QualificationStatisticsYearly> findByJmcdInAndYearAndExamStageInOrderByIdAsc(
            Collection<String> jmcds, Short year, Collection<String> examStages);

    /** 기준 연도 이전의 가장 최근 통계 (최근 완결 연도 확인용) */
    Optional<QualificationStatisticsYearly> findFirstByYearLessThanOrderByYearDesc(Short year);
}
