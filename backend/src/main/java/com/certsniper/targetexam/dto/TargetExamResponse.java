package com.certsniper.targetexam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "목표 자격증")
public record TargetExamResponse(
        @Schema(description = "목표 자격증 ID") Long id,
        @Schema(description = "자격 종목코드") String jmcd,
        @Schema(description = "자격 종목명") String jmfldnm,
        @Schema(description = "선택한 시험 회차 ID") Long examScheduleId,
        @Schema(description = "목표 시험일") LocalDate examDate,
        @Schema(description = "D-Day 표시") String ddayLabel,
        @Schema(description = "일정 색상") String displayColor
) {}
