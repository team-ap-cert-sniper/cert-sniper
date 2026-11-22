package com.certsniper.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "이메일 인증코드 확인 요청")
public record EmailVerifyRequest(
        @Schema(description = "이메일") @NotBlank @Email String email,
        @Schema(description = "인증 용도 (REGISTER · PASSWORD_RESET · PASSWORD_CHANGE)") @NotBlank String purpose,
        @Schema(description = "코드") @NotBlank @Size(min = 6, max = 6) String code
) {}
