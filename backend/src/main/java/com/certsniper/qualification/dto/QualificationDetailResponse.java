package com.certsniper.qualification.dto;

import com.certsniper.examschedule.dto.ExamScheduleResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "자격증 상세")
public record QualificationDetailResponse(
        @Schema(description = "자격 종목코드") String jmcd,
        @Schema(description = "자격증 기본정보") QualificationInfo qualification,
        @Schema(description = "등급") String gradeLevel,
        @Schema(description = "응시 조건") EligibilityResponse eligibility,
        @Schema(description = "시험일정 (진행·예정 회차 먼저)") List<ExamScheduleResponse> schedules,
        @Schema(description = "시험일정 자료 상태") String scheduleStatus,
        @Schema(description = "다음 접수 회차 ID") Long nextScheduleId,
        @Schema(description = "합격률 연도 목록") List<Short> passRateYears,
        @Schema(description = "연도·단계별 합격률") List<PassRateResponse> passRates,
        @Schema(description = "합격률 자료 상태") String passRateStatus,
        @Schema(description = "트렌드 (rank = 같은 자격구분 안 순위)") TrendResponse.TrendItem trend,
        @Schema(description = "같은 자격구분 트렌드 종목 수") Integer trendTypeTotal
) {

    @Schema(description = "자격증 기본정보")
    public record QualificationInfo(
            @Schema(description = "자격 종목코드") String jmcd,
            @Schema(description = "자격 종목명") String jmfldnm,
            @Schema(description = "영문명") String engName,
            @Schema(description = "자격구분 코드 (T 국가기술 · S 국가전문)") String qualgbcd,
            @Schema(description = "자격구분명") String qualgbnm,
            @Schema(description = "등급") String gradeLevel,
            @Schema(description = "관심 분야 코드") String obligfldcd,
            @Schema(description = "관심 분야명") String interestFieldName,
            @Schema(description = "희망 직무 코드") String mdobligfldcd,
            @Schema(description = "희망 직무명") String jobCategoryName,
            @Schema(description = "관련부처") String relatedMinistry,
            @Schema(description = "한 줄 소개") String summary,
            @Schema(description = "개요") String overview,
            @Schema(description = "수행직무") String jobDescription,
            @Schema(description = "진로 및 전망") String careerOutlook,
            @Schema(description = "시행처") String conductingAgency,
            @Schema(description = "관련학과") String relatedMajors,
            @Schema(description = "검정방법") String examMethod,
            @Schema(description = "시험과목") String examSubjects,
            @Schema(description = "합격 기준") String examStandard,
            @Schema(description = "필기 수수료") Integer feeWritten,
            @Schema(description = "실기 수수료") Integer feePractical,
            @Schema(description = "수수료 안내") String feeNote,
            @Schema(description = "시험 형태") String scheduleType
    ) {
    }
}
