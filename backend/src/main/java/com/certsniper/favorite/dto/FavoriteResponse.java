package com.certsniper.favorite.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "관심 자격증")
public record FavoriteResponse(
        @Schema(description = "자격 종목코드") String jmcd,
        @Schema(description = "자격 종목명") String jmfldnm,
        @Schema(description = "자격구분명") String qualgbnm,
        @Schema(description = "등급") String gradeLevel,
        @Schema(description = "한 줄 소개") String summary,
        @Schema(description = "찜 일시") LocalDateTime createdAt
) {}
