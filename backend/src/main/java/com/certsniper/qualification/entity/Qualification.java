package com.certsniper.qualification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 자격증 Master (qualifications) */
@Entity
@Table(name = "qualifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Qualification {

    @Id
    @Column(name = "jmcd", nullable = false, length = 20)
    private String jmcd;

    @Column(name = "jmfldnm", nullable = false, length = 100)
    private String jmfldnm;

    @Column(name = "eng_name", length = 200)
    private String engName;

    @Column(name = "qualgbcd", length = 5)
    private String qualgbcd;

    @Column(name = "grade_level", length = 10)
    private String gradeLevel;

    @Column(name = "obligfldcd", length = 10)
    private String obligfldcd;

    @Column(name = "mdobligfldcd", length = 10)
    private String mdobligfldcd;

    @Column(name = "related_ministry", length = 50)
    private String relatedMinistry;

    @Column(name = "summary", length = 200)
    private String summary;

    @Column(name = "summary_basis", length = 20)
    private String summaryBasis;

    @Column(name = "overview", columnDefinition = "text")
    private String overview;

    @Column(name = "job_description", columnDefinition = "text")
    private String jobDescription;

    @Column(name = "career_outlook", columnDefinition = "text")
    private String careerOutlook;

    @Column(name = "conducting_agency", length = 200)
    private String conductingAgency;

    @Column(name = "related_majors", columnDefinition = "text")
    private String relatedMajors;

    @Column(name = "exam_method", columnDefinition = "text")
    private String examMethod;

    @Column(name = "exam_subjects", columnDefinition = "text")
    private String examSubjects;

    @Column(name = "exam_standard", columnDefinition = "text")
    private String examStandard;

    @Column(name = "eligibility", columnDefinition = "text")
    private String eligibility;

    @Column(name = "eligibility_required", columnDefinition = "char(1)")
    private String eligibilityRequired;

    @Column(name = "eligibility_summary", length = 100)
    private String eligibilitySummary;

    @Column(name = "eligibility_rule_key", length = 30)
    private String eligibilityRuleKey;

    @Column(name = "fee_written")
    private Integer feeWritten;

    @Column(name = "fee_practical")
    private Integer feePractical;

    @Column(name = "fee_note", length = 300)
    private String feeNote;

    @Column(name = "schedule_type", nullable = false, length = 10)
    private String scheduleType = "정기";
}
