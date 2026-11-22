package com.certsniper.memo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메모")
public record MemoResponse(
        @Schema(description = "메모 ID") Long id,
        @Schema(description = "목표 자격증 ID") Long targetExamId,
        @Schema(description = "메모 텍스트") String contentText,
        @Schema(description = "서식 본문") String contentHtml,
        @Schema(description = "버전") Integer version
) {}
