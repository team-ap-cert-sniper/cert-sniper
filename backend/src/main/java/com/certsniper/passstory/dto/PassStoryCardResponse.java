package com.certsniper.passstory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "합격수기 카드")
public record PassStoryCardResponse(
        @Schema(description = "게시글 ID") Long id,
        @Schema(description = "자격 종목코드") String jmcd,
        @Schema(description = "자격 종목명") String jmfldnm,
        @Schema(description = "닉네임") String nickname,
        @Schema(description = "현재 등급") String currentRank,
        @Schema(description = "본문 미리보기") String bodyPreview,
        @Schema(description = "준비 기간") String prepPeriod,
        @Schema(description = "응시 횟수") String attemptBand,
        @Schema(description = "게시 상태") String status,
        @Schema(description = "등록 일시") LocalDateTime createdAt
) {}
