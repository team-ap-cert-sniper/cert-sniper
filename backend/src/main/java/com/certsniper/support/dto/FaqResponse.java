package com.certsniper.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "FAQ")
public record FaqResponse(
        @Schema(description = "FAQ ID") Long id,
        @Schema(description = "질문") String question,
        @Schema(description = "답변") String answer,
        @Schema(description = "정렬 순서") Short sortOrder
) {}
