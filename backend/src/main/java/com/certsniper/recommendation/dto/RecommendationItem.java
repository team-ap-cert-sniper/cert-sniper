package com.certsniper.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "추천 자격증")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RecommendationItem(
        @Schema(description = "자격 종목코드") String jmcd,
        @Schema(description = "자격 종목명") String jmfldnm,
        @Schema(description = "연결 근거 (existing_direct_links · recommended · same_subfield · faculty_aggregate)") String basis,
        @Schema(description = "학부 합산 겹침 (FACULTY 추천만)") FacultyShare facultyShare,
        @Schema(description = "응시 시점") ExamWindow examWindow,
        @Schema(description = "일치한 조건") List<InterestMatch> interestMatches,
        @Schema(description = "점수") int score,
        @Schema(description = "항목별 점수") List<ScoreBreakdown> scoreBreakdown,
        @Schema(description = "카드 표시값") CardInfo card,
        @Schema(description = "찜 여부") boolean favorited
) {

    @Schema(description = "학부 합산 겹침")
    public record FacultyShare(
            @Schema(description = "이 자격증이 연결된 소속 학과 수") int majors,
            @Schema(description = "학부 소속 학과 수") int of
    ) {
    }

    @Schema(description = "응시 시점")
    public record ExamWindow(
            @Schema(description = "응시 시점 (NO_RESTRICTION · NOW · FROM_GRADE · AFTER_GRADUATION_EXPERIENCE · CHECK_QUALIFICATION)") String status,
            @Schema(description = "응시 가능 학년") Integer fromGrade,
            @Schema(description = "관련 학과 필요 여부") Boolean relatedMajorRequired,
            @Schema(description = "바로 응시 가능하게 하는 보유 자격") String viaOwnedQualification
    ) {
    }

    @Schema(description = "일치한 조건")
    public record InterestMatch(
            @Schema(description = "구분 (JOB_CATEGORY · INTEREST_FIELD)") String type,
            @Schema(description = "코드") String code
    ) {
    }
}
