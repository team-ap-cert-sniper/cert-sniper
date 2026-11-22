package com.certsniper.planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "오늘의 사자성어")
public record DailyIdiomResponse(
        @Schema(description = "날짜") LocalDate date,
        @Schema(description = "순환 순서") int displayOrder,
        @Schema(description = "사자성어") String idiom,
        @Schema(description = "한자 표기") String hanja,
        @Schema(description = "뜻") String meaning,
        @Schema(description = "순환 주기(일)") int cycleDays
) {}
