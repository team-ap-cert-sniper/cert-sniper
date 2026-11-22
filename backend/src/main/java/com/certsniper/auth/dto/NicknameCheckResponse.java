package com.certsniper.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "닉네임 중복 확인 응답")
public record NicknameCheckResponse(
        @Schema(description = "닉네임") String nickname,
        @Schema(description = "사용 가능 여부") boolean available
) {}
