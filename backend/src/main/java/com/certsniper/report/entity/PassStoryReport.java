package com.certsniper.report.entity;

import com.certsniper.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 합격수기 신고 (pass_story_reports) */
@Entity
@Table(name = "pass_story_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PassStoryReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "story_id", nullable = false)
    private Long storyId;

    @Column(name = "reporter_id", nullable = false)
    private Long reporterId;

    @Column(name = "report_type", nullable = false, length = 20)
    private String reportType;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";
}
