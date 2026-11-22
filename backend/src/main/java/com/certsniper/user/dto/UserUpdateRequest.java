package com.certsniper.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "회원정보 수정 요청")
public record UserUpdateRequest(
        @Schema(description = "닉네임") @NotBlank @Size(min = 2, max = 10) String nickname,
        @Schema(description = "대학 ID") Long universityId,
        @Schema(description = "학부(단과대학) ID") Long collegeId,
        @Schema(description = "학과 ID") Long majorId,
        @Schema(description = "학년") String grade,
        @Schema(description = "보유 자격 종목코드 목록") List<String> ownedJmcds,
        @Schema(description = "보유 자격증명(직접 입력) 목록") List<String> ownedCertNames
) {}
