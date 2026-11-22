package com.certsniper.passstory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "합격수기 상세")
public record PassStoryDetailResponse(
        @Schema(description = "게시글 ID") Long id,
        @Schema(description = "작성 회원 ID") Long userId,
        @Schema(description = "닉네임") String nickname,
        @Schema(description = "현재 등급") String currentRank,
        @Schema(description = "자격 종목코드") String jmcd,
        @Schema(description = "자격 종목명") String jmfldnm,
        @Schema(description = "상세 후기·실전 팁") String body,
        @Schema(description = "응시자 베이스") String examineeBase,
        @Schema(description = "준비 기간") String prepPeriod,
        @Schema(description = "응시 횟수") String attemptBand,
        @Schema(description = "사용 교재명") String textbookName,
        @Schema(description = "합격 점수") BigDecimal passScore,
        @Schema(description = "시험일") LocalDate examDate,
        @Schema(description = "게시 상태") String status,
        @Schema(description = "숨김 사유") String hiddenReason,
        @Schema(description = "내 글 여부") boolean mine,
        @Schema(description = "등록 일시") LocalDateTime createdAt
) {}
