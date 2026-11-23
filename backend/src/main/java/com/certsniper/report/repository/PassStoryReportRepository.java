package com.certsniper.report.repository;

import com.certsniper.report.entity.PassStoryReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PassStoryReportRepository extends JpaRepository<PassStoryReport, Long> {

    boolean existsByStoryIdAndReporterId(Long storyId, Long reporterId);

    long countByStatus(String status);

    Page<PassStoryReport> findByStatus(String status, Pageable pageable);

    /** 마이페이지: 내 글에 들어온 신고 (신고자 정보는 반환하지 않음) */
    @Query("""
            select r from PassStoryReport r
            where r.storyId in (select p.id from PassStory p where p.userId = :userId)
            """)
    Page<PassStoryReport> findReportsOnUserStories(@Param("userId") Long userId, Pageable pageable);
}
