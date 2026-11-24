package com.certsniper.recommendation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "전공 맞춤 추천 요청. 대학·학년과 학과 ID 또는 학부 ID(학부 전체 선택)를 함께 넣거나 모두 비움")
public record RecommendationRequest(
        @Schema(description = "대학 ID") Long universityId,
        @Schema(description = "학부 ID (선택 목록 majors의 scope가 FACULTY인 항목, 소속 학과 합산 추천)") Long collegeId,
        @Schema(description = "학과 ID (선택 목록 majors의 scope가 MAJOR인 항목)") Long majorId,
        @Schema(description = "학년") String grade,
        @Schema(description = "보유 자격 종목코드 목록") List<String> ownedJmcds
) {

    /** 학과 기준 요청 (학부 ID 없음) */
    public RecommendationRequest(Long universityId, Long majorId, String grade, List<String> ownedJmcds) {
        this(universityId, null, majorId, grade, ownedJmcds);
    }
}
