package com.certsniper.planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "오늘의 일정")
public record TodayScheduleResponse(
        @Schema(description = "날짜") LocalDate date,
        @Schema(description = "항목 목록") List<TodayScheduleItem> items
) {

    @Schema(description = "오늘의 일정 항목")
    public record TodayScheduleItem(
            @Schema(description = "일정명") String title,
            @Schema(description = "일정 색상") String displayColor,
            @Schema(description = "당일 일정 여부") boolean isSingleDay
    ) {
    }
}
