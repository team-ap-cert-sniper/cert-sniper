package com.certsniper.recommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "항목별 점수")
public record ScoreBreakdown(
        @Schema(description = "점수 항목") String factor,
        @Schema(description = "점수") int points,
        @Schema(description = "사유") String reason
) {}
