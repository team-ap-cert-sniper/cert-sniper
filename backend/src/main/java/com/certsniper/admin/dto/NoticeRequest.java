package com.certsniper.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "공지 등록·수정 요청")
public record NoticeRequest(
        @Schema(description = "공지 유형") @NotBlank String noticeType,
        @Schema(description = "제목") @NotBlank @Size(max = 100) String title,
        @Schema(description = "본문") @NotBlank String content
) {}
