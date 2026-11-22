package com.certsniper.support.dto;

import com.certsniper.support.enums.InquiryType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "1:1 문의")
public record InquiryResponse(
        @Schema(description = "문의 ID") Long id,
        @Schema(description = "작성 회원 ID") Long userId,
        @Schema(description = "작성자 닉네임") String nickname,
        @Schema(description = "문의 유형") InquiryType inquiryType,
        @Schema(description = "문의 제목") String title,
        @Schema(description = "문의 내용") String content,
        @Schema(description = "처리 상태 (WAITING 답변 대기 · ANSWERED 답변 완료)") String status,
        @Schema(description = "답변 내용") String answer,
        @Schema(description = "등록 일시") LocalDateTime createdAt
) {}
