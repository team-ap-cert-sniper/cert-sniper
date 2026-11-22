package com.certsniper.rank.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "저격 등급")
public record RankResponse(
        @Schema(description = "현재 등급") String currentRank,
        @Schema(description = "유효 합격 자격증 수") int validCertCount,
        @Schema(description = "다음 등급") String nextRank,
        @Schema(description = "다음 등급까지 남은 수") Integer remainingForNext
) {}
