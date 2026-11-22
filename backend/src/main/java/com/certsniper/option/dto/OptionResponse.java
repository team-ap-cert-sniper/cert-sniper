package com.certsniper.option.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "선택 목록")
public record OptionResponse(
        @Schema(description = "목록 종류") String kind,
        @Schema(description = "항목 목록") List<OptionItem> items,
        @Schema(description = "전체 건수") long total,
        @Schema(description = "시작 위치") int offset,
        @Schema(description = "최대 건수") int limit
) {}
