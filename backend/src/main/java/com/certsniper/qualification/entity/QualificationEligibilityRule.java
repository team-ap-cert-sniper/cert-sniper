package com.certsniper.qualification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 응시 조건 목록 (qualification_eligibility_rules) */
@Entity
@Table(name = "qualification_eligibility_rules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QualificationEligibilityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "rule_key", nullable = false, length = 30)
    private String ruleKey;

    @Column(name = "seq", nullable = false)
    private Byte seq;

    @Column(name = "condition_type", nullable = false, length = 10)
    private String conditionType;

    @Column(name = "condition_text", nullable = false, length = 300)
    private String conditionText;
}
