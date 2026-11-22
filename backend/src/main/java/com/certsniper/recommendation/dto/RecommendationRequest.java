package com.certsniper.recommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "전공 맞춤 추천 요청")
public record RecommendationRequest(
        @Schema(description = "대학 ID") Long universityId,
        @Schema(description = "학과 ID") Long majorId,
        @Schema(description = "학년") String grade,
        @Schema(description = "보유 자격 종목코드 목록") List<String> ownedJmcds
) {}
