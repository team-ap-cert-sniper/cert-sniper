package com.certsniper.qualification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 연도·단계별 합격 통계 (qualification_statistics_yearly) */
@Entity
@Table(name = "qualification_statistics_yearly")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QualificationStatisticsYearly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "jmcd", nullable = false, length = 20)
    private String jmcd;

    @Column(name = "year", nullable = false)
    private Short year;

    @Column(name = "exam_stage", nullable = false, length = 10)
    private String examStage;

    @Column(name = "applicants")
    private Integer applicants;

    @Column(name = "examinees")
    private Integer examinees;

    @Column(name = "passers")
    private Integer passers;

    @Column(name = "pass_rate_pct", precision = 5, scale = 2, insertable = false, updatable = false)
    private BigDecimal passRatePct;

    @Column(name = "data_source", nullable = false, length = 30)
    private String dataSource;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;
}
