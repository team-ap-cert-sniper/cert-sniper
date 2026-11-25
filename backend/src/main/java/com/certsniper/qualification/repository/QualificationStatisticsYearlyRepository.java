package com.certsniper.qualification.repository;

import com.certsniper.qualification.entity.QualificationStatisticsYearly;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QualificationStatisticsYearlyRepository extends JpaRepository<QualificationStatisticsYearly, Long> {

    List<QualificationStatisticsYearly> findByJmcdInOrderByIdAsc(Collection<String> jmcds);
}
