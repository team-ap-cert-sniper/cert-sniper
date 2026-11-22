package com.certsniper.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이메일 인증코드 발송 요청")
public record EmailCodeRequest(
        @Schema(description = "이메일") @NotBlank @Email String email,
        @Schema(description = "인증 용도 (REGISTER · PASSWORD_RESET · PASSWORD_CHANGE)") @NotBlank String purpose
) {}
