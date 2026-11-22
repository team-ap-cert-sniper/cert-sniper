package com.certsniper.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "회원가입 요청")
public record RegisterRequest(
        @Schema(description = "이메일") @NotBlank @Email @Size(max = 254) String email,
        @Schema(description = "비밀번호") @NotBlank String password,
        @Schema(description = "이름") @NotBlank @Size(min = 2, max = 4) String name,
        @Schema(description = "닉네임") @NotBlank @Size(min = 2, max = 10) String nickname,
        @Schema(description = "대학 ID") Long universityId,
        @Schema(description = "학부(단과대학) ID") Long collegeId,
        @Schema(description = "학과 ID") Long majorId,
        @Schema(description = "학년") String grade,
        @Schema(description = "보유 자격 종목코드 목록") List<String> ownedJmcds,
        @Schema(description = "보유 자격증명(직접 입력) 목록") List<String> ownedCertNames
) {}
