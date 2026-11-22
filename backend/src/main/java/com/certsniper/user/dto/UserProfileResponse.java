package com.certsniper.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "회원정보")
public record UserProfileResponse(
        @Schema(description = "회원 ID") Long id,
        @Schema(description = "이메일") String email,
        @Schema(description = "이름") String name,
        @Schema(description = "닉네임") String nickname,
        @Schema(description = "권한") String role,
        @Schema(description = "계정 상태") String status,
        @Schema(description = "대학 ID") Long universityId,
        @Schema(description = "대학명") String universityName,
        @Schema(description = "학부(단과대학) ID") Long collegeId,
        @Schema(description = "학과 ID") Long majorId,
        @Schema(description = "학과명") String majorName,
        @Schema(description = "학년") String grade,
        @Schema(description = "보유 자격증") List<OwnedCertificate> ownedCertificates,
        @Schema(description = "현재 등급") String currentRank,
        @Schema(description = "탈퇴 신청 일시") LocalDateTime withdrawalRequestedAt
) {

    @Schema(description = "보유 자격증")
    public record OwnedCertificate(
            @Schema(description = "자격 종목코드") String jmcd,
            @Schema(description = "자격 종목명") String jmfldnm,
            @Schema(description = "자격증명(직접 입력)") String certName
    ) {
    }
}
