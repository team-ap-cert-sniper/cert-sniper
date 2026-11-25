package com.certsniper.qualification.dto;

import com.certsniper.global.util.PercentUtils;
import com.certsniper.qualification.entity.QualificationStatisticsYearly;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Map;

@Schema(description = "연도·단계별 합격률")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PassRateResponse(
        @Schema(description = "연도") Short year,
        @Schema(description = "시험 단계") String examStage,
        @Schema(description = "접수자 수") Integer applicants,
        @Schema(description = "응시자 수") Integer examinees,
        @Schema(description = "합격자 수") Integer passers,
        @Schema(description = "응시율(%) = 응시자 / 접수자") BigDecimal attendanceRatePercent,
        @Schema(description = "합격률(%) = 합격자 / 실제 응시자") BigDecimal passRatePct,
        @Schema(description = "통계 출처") String dataSource,
        @Schema(description = "출처 URL") String sourceUrl
) {

    /** 같은 해 안에서 단계 표시 순서 (필기→실기, 1차→2차→3차→면접) */
    private static final Map<String, Integer> STAGE_ORDER =
            Map.of("필기", 0, "1차", 1, "2차", 2, "3차", 3, "실기", 4, "면접", 5, "시험", 6);

    public static int stageOrder(String examStage) {
        return STAGE_ORDER.getOrDefault(examStage, 99);
    }

    public static PassRateResponse of(QualificationStatisticsYearly r) {
        return new PassRateResponse(r.getYear(), r.getExamStage(), r.getApplicants(), r.getExaminees(), r.getPassers(),
                PercentUtils.percent(r.getExaminees(), r.getApplicants(), 1), r.getPassRatePct(),
                r.getDataSource(), r.getSourceUrl());
    }
}
