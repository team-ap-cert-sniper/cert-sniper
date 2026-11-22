package com.certsniper.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "문의 답변 요청")
public record InquiryAnswerRequest(
        @Schema(description = "답변 내용") @NotBlank @Size(max = 1000) String content
) {}
