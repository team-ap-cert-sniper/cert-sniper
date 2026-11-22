package com.certsniper.qualification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "응시 조건")
public record EligibilityResponse(
        @Schema(description = "응시 조건 필요 여부") Boolean required,
        @Schema(description = "한 줄 소개") String summary,
        @Schema(description = "조건 목록") List<Condition> conditions,
        @Schema(description = "출처 URL") String sourceUrl
) {

    @Schema(description = "응시 조건 항목")
    public record Condition(
            @Schema(description = "구분") String type,
            @Schema(description = "내용") String text
    ) {
    }
}
