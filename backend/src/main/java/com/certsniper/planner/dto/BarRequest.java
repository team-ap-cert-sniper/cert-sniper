package com.certsniper.planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "일정바 등록·수정 요청")
public record BarRequest(
        @Schema(description = "목표 자격증 ID") Long targetExamId,
        @Schema(description = "개인 일정 ID") Long personalScheduleId,
        @Schema(description = "일정명") @NotBlank @Size(max = 100) String title,
        @Schema(description = "시작일") @NotNull LocalDate startDate,
        @Schema(description = "종료일") @NotNull LocalDate endDate,
        @Schema(description = "당일 일정 여부") boolean isSingleDay
) {}
