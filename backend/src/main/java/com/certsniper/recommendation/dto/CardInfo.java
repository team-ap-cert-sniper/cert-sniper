package com.certsniper.recommendation.dto;

import com.certsniper.examschedule.dto.ExamScheduleResponse;
import com.certsniper.qualification.dto.PassRateResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "추천 카드 표시값")
public record CardInfo(
        @Schema(description = "자격구분 코드 (T 국가기술 · S 국가전문)") String qualgbcd,
        @Schema(description = "자격구분명") String qualgbnm,
        @Schema(description = "등급") String gradeLevel,
        @Schema(description = "희망 직무명") String jobCategoryName,
        @Schema(description = "관심 분야명") String interestFieldName,
        @Schema(description = "한 줄 소개") String summary,
        @Schema(description = "최근 연도 합격률") PassRateLatest passRateLatest,
        @Schema(description = "다음 접수 회차") ExamScheduleResponse nextRegistration,
        @Schema(description = "가장 가까운 시험") ExamScheduleResponse nearestExam
) {

    @Schema(description = "최근 연도 합격률")
    public record PassRateLatest(
            @Schema(description = "연도") Short year,
            @Schema(description = "항목 목록") List<PassRateResponse> items
    ) {
    }
}
