package com.certsniper.qualification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "연도·단계별 합격률")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PassRateResponse(
        @Schema(description = "연도") Short year,
        @Schema(description = "시험 단계") String examStage,
        @Schema(description = "접수자 수") Integer applicants,
        @Schema(description = "응시자 수") Integer examinees,
        @Schema(description = "합격자 수") Integer passers,
        @Schema(description = "응시율(%)") BigDecimal attendanceRatePercent,
        @Schema(description = "합격률(%)") BigDecimal passRatePct,
        @Schema(description = "통계 출처") String dataSource,
        @Schema(description = "출처 URL") String sourceUrl
) {}
