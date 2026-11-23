package com.certsniper.university.repository;

import com.certsniper.university.entity.College;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollegeRepository extends JpaRepository<College, Long> {

    /** 그 대학의 '(학부 전체)' 항목으로만 쓰는 학부 */
    List<College> findByUniversityIdAndIsListItemTrue(Long universityId);
}