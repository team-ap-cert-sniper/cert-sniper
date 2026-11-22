package com.certsniper.support.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "공지사항")
public record NoticeResponse(
        @Schema(description = "공지 ID") Long id,
        @Schema(description = "공지 유형") String noticeType,
        @Schema(description = "제목") String title,
        @Schema(description = "내용") String content,
        @Schema(description = "등록 일시") LocalDateTime createdAt
) {}
