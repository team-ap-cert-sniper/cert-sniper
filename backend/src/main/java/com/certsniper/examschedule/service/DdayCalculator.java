package com.certsniper.examschedule.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

/** D-n · D-Day · 종료 계산 (planner·targetexam 도 사용) */
@Component
public class DdayCalculator {

    /** 남은 일수 (지난 시험은 음수) */
    public long daysRemaining(LocalDate examDate, LocalDate today) {
        return ChronoUnit.DAYS.between(today, examDate);
    }

    /** D-n · D-Day · 종료 */
    public String label(LocalDate examDate, LocalDate today) {
        long days = daysRemaining(examDate, today);
        return days > 0 ? "D-" + days : days == 0 ? "D-Day" : "종료";
    }
}
