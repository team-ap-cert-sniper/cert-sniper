package com.certsniper.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "추천 결과. 전공 맞춤은 recommendation_scope·needs_school_info·owned_excluded_count, 조건 선택은 mode·total_candidates")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RecommendationResponse(
        @Schema(description = "데이터 스키마 버전") String schemaVersion,
        @Schema(description = "추천 방식 (CONDITION: 조건 선택)") String mode,
        @Schema(description = "추천 범위 (MAJOR · FACULTY · NONE)") String recommendationScope,
        @Schema(description = "학교 정보 필요 여부") Boolean needsSchoolInfo,
        @Schema(description = "항목 목록") List<RecommendationItem> items,
        @Schema(description = "조건에 맞는 전체 후보 수 (조건 선택)") Integer totalCandidates,
        @Schema(description = "최대 추천 개수") int resultLimit,
        @Schema(description = "보유 자격으로 제외된 수") Integer ownedExcludedCount,
        @Schema(description = "점수 기준일") LocalDate scoreBasisDate
) {}
