package com.certsniper.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "신고 (관리자)")
public record AdminReportResponse(
        @Schema(description = "신고 ID") Long reportId,
        @Schema(description = "게시글 ID") Long storyId,
        @Schema(description = "자격 종목명") String jmfldnm,
        @Schema(description = "신고 회원 ID") Long reporterId,
        @Schema(description = "신고 사유") String reportType,
        @Schema(description = "처리 상태") String status,
        @Schema(description = "신고 일시") LocalDateTime createdAt
) {}
