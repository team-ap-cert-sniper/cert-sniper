package com.certsniper.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "회원 상세 (관리자)")
public record AdminUserDetailResponse(
        @Schema(description = "회원 ID") Long id,
        @Schema(description = "이메일") String email,
        @Schema(description = "이름") String name,
        @Schema(description = "닉네임") String nickname,
        @Schema(description = "권한") String role,
        @Schema(description = "계정 상태") String status,
        @Schema(description = "대학 ID") Long universityId,
        @Schema(description = "학부(단과대학) ID") Long collegeId,
        @Schema(description = "학과 ID") Long majorId,
        @Schema(description = "학년") String grade,
        @Schema(description = "누적 숨김 횟수") int confirmedHiddenCount,
        @Schema(description = "정지 종료 일시") LocalDateTime suspendedUntil,
        @Schema(description = "가입 일시") LocalDateTime createdAt
) {}
