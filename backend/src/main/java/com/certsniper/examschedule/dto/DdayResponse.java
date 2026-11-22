package com.certsniper.examschedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "시험 D-Day")
public record DdayResponse(
        @Schema(description = "시험 일정 ID") Long scheduleId,
        @Schema(description = "자격 종목코드") String jmcd,
        @Schema(description = "시험 단계") String examStage,
        @Schema(description = "시험일") LocalDate examDate,
        @Schema(description = "기준일") LocalDate today,
        @Schema(description = "남은 일수") long daysRemaining,
        @Schema(description = "표시명") String label,
        @Schema(description = "상태 (UPCOMING · TODAY · ENDED)") String state
) {}
