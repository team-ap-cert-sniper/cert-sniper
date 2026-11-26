package com.certsniper.planner.entity;

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

/** 일정바 (planner_bars) */
@Entity
@Table(name = "planner_bars")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlannerBar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "target_exam_id")
    private Long targetExamId;

    @Column(name = "personal_schedule_id")
    private Long personalScheduleId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_single_day", nullable = false)
    private Boolean isSingleDay = false;

    @Column(name = "source", nullable = false, length = 10)
    private String source = "MANUAL";

    /** 일정바 생성. 목표 자격증 ID와 개인 일정 ID 중 하나만 넣는다 */
    public static PlannerBar create(Long userId, Long targetExamId, Long personalScheduleId, String title,
                                    LocalDate startDate, LocalDate endDate, boolean isSingleDay, String source) {
        PlannerBar bar = new PlannerBar();
        bar.userId = userId;
        bar.targetExamId = targetExamId;
        bar.personalScheduleId = personalScheduleId;
        bar.title = title;
        bar.startDate = startDate;
        bar.endDate = endDate;
        bar.isSingleDay = isSingleDay;
        bar.source = source;
        return bar;
    }

    /** 날짜가 이 일정바 기간 안인지 */
    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}
