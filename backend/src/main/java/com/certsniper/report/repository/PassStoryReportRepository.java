package com.certsniper.report.repository;

import com.certsniper.report.entity.PassStoryReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassStoryReportRepository extends JpaRepository<PassStoryReport, Long> {

    boolean existsByStoryIdAndReporterId(Long storyId, Long reporterId);

    long countByStatus(String status);

    Page<PassStoryReport> findByStatus(String status, Pageable pageable);
}
