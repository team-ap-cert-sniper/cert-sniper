package com.certsniper.qualification.repository;

import com.certsniper.qualification.entity.QualificationEligibilityRule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QualificationEligibilityRuleRepository extends JpaRepository<QualificationEligibilityRule, Long> {

    /** 응시 조건 규칙 (표시 순서) */
    List<QualificationEligibilityRule> findByRuleKeyOrderBySeqAsc(String ruleKey);
}
