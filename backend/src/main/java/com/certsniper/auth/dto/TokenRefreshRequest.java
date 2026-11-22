package com.certsniper.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "토큰 재발급·로그아웃 요청")
public record TokenRefreshRequest(
        @Schema(description = "Refresh Token") @NotBlank String refreshToken
) {}
