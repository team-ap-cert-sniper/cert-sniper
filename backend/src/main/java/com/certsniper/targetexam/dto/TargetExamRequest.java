package com.certsniper.targetexam.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

@Schema(description = "목표 자격증 추가·수정 요청")
public record TargetExamRequest(
        @Schema(description = "자격 종목코드") @NotBlank String jmcd,
        @Schema(description = "선택한 시험 회차 ID") Long examScheduleId,
        @Schema(description = "목표 시험일") LocalDate examDate,
        @Schema(description = "일정 색상") @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String displayColor
) {}
