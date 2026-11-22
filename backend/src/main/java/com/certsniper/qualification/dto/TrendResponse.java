package com.certsniper.qualification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "주목받는 자격증 트렌드")
public record TrendResponse(
        @Schema(description = "기준 연도") Short year,
        @Schema(description = "자격구분 코드 (T 국가기술 · S 국가전문)") String qualgbcd,
        @Schema(description = "등급") String gradeLevel,
        @Schema(description = "관심 분야 코드") String interestFieldCode,
        @Schema(description = "순위 기준") String basis,
        @Schema(description = "전체 건수") int total,
        @Schema(description = "항목 목록") List<TrendItem> items
) {

    @Schema(description = "트렌드 항목")
    public record TrendItem(
            @Schema(description = "순위") int rank,
            @Schema(description = "자격 종목코드") String jmcd,
            @Schema(description = "자격 종목명") String jmfldnm,
            @Schema(description = "자격구분 코드 (T 국가기술 · S 국가전문)") String qualgbcd,
            @Schema(description = "등급") String gradeLevel,
            @Schema(description = "관심 분야 코드") String interestFieldCode,
            @Schema(description = "관심 분야명") String interestFieldName,
            @Schema(description = "연도") Short year,
            @Schema(description = "시험 단계") String examStage,
            @Schema(description = "응시자 수") Integer examinees,
            @Schema(description = "합격자 수") Integer passers,
            @Schema(description = "합격률(%)") BigDecimal passRatePct,
            @Schema(description = "최근 5년 응시자") List<YearExaminees> fiveYear,
            @Schema(description = "5년 증감률(%)") BigDecimal fiveYearChangePercent,
            @Schema(description = "트렌드") String trend
    ) {
    }

    @Schema(description = "연도별 응시자")
    public record YearExaminees(
            @Schema(description = "연도") Short year,
            @Schema(description = "응시자 수") Integer examinees
    ) {
    }
}
