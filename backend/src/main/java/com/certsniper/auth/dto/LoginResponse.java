package com.certsniper.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record LoginResponse(
        @Schema(description = "Access Token") String accessToken,
        @Schema(description = "Refresh Token") String refreshToken,
        @Schema(description = "회원 ID") Long userId,
        @Schema(description = "닉네임") String nickname,
        @Schema(description = "권한 (USER · ADMIN)") String role
) {}
