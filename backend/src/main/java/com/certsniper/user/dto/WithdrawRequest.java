package com.certsniper.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원 탈퇴 요청")
public record WithdrawRequest(
        @Schema(description = "비밀번호") @NotBlank String password
) {}
