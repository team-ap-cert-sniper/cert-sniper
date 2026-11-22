package com.certsniper.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "관리자 대시보드")
public record DashboardResponse(
        @Schema(description = "공지 수") long noticeCount,
        @Schema(description = "미답변 문의 수") long unansweredInquiryCount,
        @Schema(description = "미처리 신고 수") long pendingReportCount,
        @Schema(description = "전체 회원 수") long userCount,
        @Schema(description = "등록 자격증 수") long qualificationCount
) {}
