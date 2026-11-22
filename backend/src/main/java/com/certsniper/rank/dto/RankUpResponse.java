package com.certsniper.rank.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "등급 승급 결과")
public record RankUpResponse(
        @Schema(description = "승급 여부") boolean rankedUp,
        @Schema(description = "이전 등급") String previousRank,
        @Schema(description = "새 등급") String newRank
) {}
