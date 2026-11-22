package com.certsniper.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Schema(description = "계정 정지 요청")
public record SuspendRequest(
        @Schema(description = "정지 종료 일시") @NotNull LocalDateTime suspendedUntil,
        @Schema(description = "정지 사유") @NotBlank @Size(max = 500) String reason
) {}
