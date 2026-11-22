package com.certsniper.examschedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

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
        @Schema(description = "시험 상태") String examStatus,
        @Schema(description = "접수 상태") String registrationStatus,
        @Schema(description = "D-Day 표시") String ddayLabel
) {}
