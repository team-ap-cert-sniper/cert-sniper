package com.certsniper.memo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "메모 저장 요청")
public record MemoRequest(
        @Schema(description = "메모 텍스트") @NotBlank @Size(max = 500) String contentText,
        @Schema(description = "서식 본문") @NotNull String contentHtml,
        @Schema(description = "버전") Integer version
) {}
