package com.certsniper.passstory.entity;

import com.certsniper.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 합격수기 (pass_stories) */
@Entity
@Table(name = "pass_stories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PassStory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "jmcd", nullable = false, length = 20)
    private String jmcd;

    @Column(name = "body", nullable = false, length = 1500)
    private String body;

    @Column(name = "examinee_base", length = 20)
    private String examineeBase;

    @Column(name = "prep_period", length = 20)
    private String prepPeriod;

    @Column(name = "attempt_band", length = 10)
    private String attemptBand;

    @Column(name = "textbook_name", length = 50)
    private String textbookName;

    @Column(name = "pass_score", precision = 6, scale = 2)
    private BigDecimal passScore;

    @Column(name = "exam_date")
    private LocalDate examDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PUBLISHED";

    @Column(name = "hidden_reason", length = 20)
    private String hiddenReason;

    @Column(name = "hidden_confirmed_at")
    private LocalDateTime hiddenConfirmedAt;
}
