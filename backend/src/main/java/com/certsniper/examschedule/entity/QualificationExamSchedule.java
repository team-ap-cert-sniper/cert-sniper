package com.certsniper.examschedule.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 시험일정 (qualification_exam_schedules) */
@Entity
@Table(name = "qualification_exam_schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QualificationExamSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "jmcd", nullable = false, length = 20)
    private String jmcd;

    @Column(name = "exam_round", length = 20)
    private String examRound;

    @Column(name = "exam_stage", nullable = false, length = 10)
    private String examStage;

    @Column(name = "registration_start_date")
    private LocalDate registrationStartDate;

    @Column(name = "registration_end_date")
    private LocalDate registrationEndDate;

    @Column(name = "vacancy_registration_start_date")
    private LocalDate vacancyRegistrationStartDate;

    @Column(name = "vacancy_registration_end_date")
    private LocalDate vacancyRegistrationEndDate;

    @Column(name = "exam_start_date", nullable = false)
    private LocalDate examStartDate;

    @Column(name = "exam_end_date", nullable = false)
    private LocalDate examEndDate;

    @Column(name = "pass_announcement_date")
    private LocalDate passAnnouncementDate;
}
