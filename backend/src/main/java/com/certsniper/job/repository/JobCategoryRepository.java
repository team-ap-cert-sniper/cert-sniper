package com.certsniper.job.repository;

import com.certsniper.job.entity.JobCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobCategoryRepository extends JpaRepository<JobCategory, String> {
}
