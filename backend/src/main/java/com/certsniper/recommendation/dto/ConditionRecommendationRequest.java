package com.certsniper.recommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "조건 선택 추천 요청")
public record ConditionRecommendationRequest(
        @Schema(description = "자격구분 코드 (T 국가기술 · S 국가전문)") @Pattern(regexp = "^[TS]$") String qualgbcd,
        @Schema(description = "관심 분야 코드 목록 (최대 5개)") @Size(max = 5) List<String> interestFieldCodes,
        @Schema(description = "희망 직무 코드 목록 (최대 5개)") @Size(max = 5) List<String> jobCategoryCodes,
        @Schema(description = "보유 자격 종목코드 목록") List<String> ownedJmcds
) {}
