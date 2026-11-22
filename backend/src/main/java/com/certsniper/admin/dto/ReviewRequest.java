package com.certsniper.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "신고 검토 요청")
public record ReviewRequest(
        @Schema(description = "조치 (CONFIRM_HIDE · DISMISS · UNHIDE · DELETE)") @NotBlank String action,
        @Schema(description = "관리자 메모") @Size(max = 500) String reasonText
) {}
