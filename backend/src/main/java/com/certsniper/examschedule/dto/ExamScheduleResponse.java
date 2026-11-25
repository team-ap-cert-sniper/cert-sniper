package com.certsniper.examschedule.dto;

import com.certsniper.examschedule.entity.QualificationExamSchedule;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Schema(description = "시험일정")
public record ExamScheduleResponse(
        @Schema(description = "시험 일정 ID") Long scheduleId,
        @Schema(description = "회차") String examRound,
        @Schema(description = "시험 단계") String examStage,
        @Schema(description = "정기 접수 시작일") LocalDate registrationStartDate,
        @Schema(description = "정기 접수 종료일") LocalDate registrationEndDate,
        @Schema(description = "빈자리 접수 시작일") LocalDate vacancyRegistrationStartDate,
        @Schema(description = "빈자리 접수 종료일") LocalDate vacancyRegistrationEndDate,
        @Schema(description = "시험 시작일") LocalDate examStartDate,
        @Schema(description = "시험 종료일") LocalDate examEndDate,
        @Schema(description = "합격 발표일") LocalDate passAnnouncementDate,
        @Schema(description = "시험 상태 (UPCOMING · IN_PROGRESS · ENDED)") String examStatus,
        @Schema(description = "접수 상태 (NOT_OPEN · OPEN · VACANCY_NOT_OPEN · VACANCY_OPEN · CLOSED · UNKNOWN)") String registrationStatus,
        @Schema(description = "D-Day 표시") String ddayLabel
) {

    public static final String ENDED = "ENDED";
    public static final String UPCOMING = "UPCOMING";
    public static final String CLOSED = "CLOSED";

    /** 기준일 대비 일정 상태. 지난 시험은 ENDED, D-Day는 예정 시험만 */
    public static ExamScheduleResponse of(QualificationExamSchedule s, LocalDate today) {
        String examStatus;
        String ddayLabel;
        if (s.getExamEndDate().isBefore(today)) {
            examStatus = ENDED;
            ddayLabel = "종료";
        } else if (!s.getExamStartDate().isAfter(today)) {
            examStatus = "IN_PROGRESS";
            ddayLabel = "시험 기간 중(~" + s.getExamEndDate() + ")";
        } else {
            examStatus = UPCOMING;
            ddayLabel = "D-" + ChronoUnit.DAYS.between(today, s.getExamStartDate());
        }
        return new ExamScheduleResponse(s.getId(), s.getExamRound(), s.getExamStage(),
                s.getRegistrationStartDate(), s.getRegistrationEndDate(),
                s.getVacancyRegistrationStartDate(), s.getVacancyRegistrationEndDate(),
                s.getExamStartDate(), s.getExamEndDate(), s.getPassAnnouncementDate(),
                examStatus, registrationStatus(s, today), ddayLabel);
    }

    private static String registrationStatus(QualificationExamSchedule s, LocalDate today) {
        LocalDate start = s.getRegistrationStartDate();
        LocalDate end = s.getRegistrationEndDate();
        LocalDate vacancyStart = s.getVacancyRegistrationStartDate();
        LocalDate vacancyEnd = s.getVacancyRegistrationEndDate();
        if (start == null || end == null) {
            return "UNKNOWN";
        }
        if (today.isBefore(start)) {
            return "NOT_OPEN";
        }
        if (!today.isAfter(end)) {
            return "OPEN";
        }
        if (vacancyStart != null && today.isBefore(vacancyStart)) {
            return "VACANCY_NOT_OPEN";
        }
        if (vacancyStart != null && vacancyEnd != null && !today.isAfter(vacancyEnd)) {
            return "VACANCY_OPEN";
        }
        return CLOSED;
    }
}
