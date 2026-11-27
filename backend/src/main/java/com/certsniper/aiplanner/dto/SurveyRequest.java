package com.certsniper.aiplanner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

/** AI 플래너 7단계 설문 */
@Schema(description = "AI 플래너 설문")
public record SurveyRequest(
        @Schema(description = "1. 목표 자격증 종목코드", example = "2290") @NotBlank String jmcd,
        @Schema(description = "2. 선택한 시험 회차 ID") @NotNull Long examScheduleId,
        @Schema(description = "2. 시험일 (회차 기간 중 하루)") @NotNull LocalDate examDate,
        @Schema(description = "3. 공부 시작일") @NotNull LocalDate startDate,
        @Schema(description = "4. 베이스 (예: 처음 공부, 기초 있음, 재응시)") @NotBlank @Size(max = 50) String baseLevel,
        @Schema(description = "5. 하루 공부 가능 시간 (시간)", example = "2") @Min(1) @Max(12) int dailyHours,
        @Schema(description = "6. 공부 가능 요일", example = "[\"MONDAY\",\"WEDNESDAY\",\"FRIDAY\",\"SATURDAY\"]")
        @NotEmpty List<DayOfWeek> studyDays,
        @Schema(description = "7. 학습 스타일: 이론 비율 % (나머지는 문제 풀이)", example = "60") @Min(0) @Max(100) int theoryRatio
) {}
