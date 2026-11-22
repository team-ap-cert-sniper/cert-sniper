package com.certsniper.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원 정보 정정 요청")
public record ProfileCorrectionRequest(
        @Schema(description = "이름") @Size(min = 2, max = 4) String name,
        @Schema(description = "닉네임") @Size(min = 2, max = 10) String nickname,
        @Schema(description = "대학 ID") Long universityId,
        @Schema(description = "학부(단과대학) ID") Long collegeId,
        @Schema(description = "학과 ID") Long majorId,
        @Schema(description = "학년") String grade,
        @Schema(description = "정정 사유") @NotBlank @Size(max = 500) String reason
) {}
