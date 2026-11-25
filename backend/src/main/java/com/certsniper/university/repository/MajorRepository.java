package com.certsniper.university.repository;

import com.certsniper.university.entity.Major;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MajorRepository extends JpaRepository<Major, Long> {

    /** 그 대학의 전공 전체 */
    List<Major> findByUniversityId(Long universityId);

    /** 학부 소속 전공 */
    List<Major> findByCollegeId(Long collegeId);
}