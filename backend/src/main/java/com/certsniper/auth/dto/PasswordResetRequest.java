package com.certsniper.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "비밀번호 재설정 요청")
public record PasswordResetRequest(
        @Schema(description = "이메일") @NotBlank @Email String email,
        @Schema(description = "새 비밀번호") @NotBlank String newPassword
) {}
