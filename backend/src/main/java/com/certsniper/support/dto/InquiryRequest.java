package com.certsniper.support.dto;

import com.certsniper.support.enums.InquiryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "1:1 문의 등록 요청")
public record InquiryRequest(
        @Schema(description = "문의 유형") @NotNull InquiryType inquiryType,
        @Schema(description = "문의 제목") @NotBlank @Size(max = 50) String title,
        @Schema(description = "문의 내용") @NotBlank @Size(max = 1000) String content
) {}
