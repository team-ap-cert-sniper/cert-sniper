package com.certsniper.aiplanner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "AI 플랜")
public record AiPlanResponse(
        @Schema(description = "플랜 요약") Meta meta,
        @Schema(description = "일정바 목록") List<TimelineBar> timelineBars,
        @Schema(description = "일일 항목") List<DailyPoint> dailyPoints
) {

    @Schema(description = "플랜 요약")
    public record Meta(
            @Schema(description = "자격 종목코드") String jmcd,
            @Schema(description = "시작일") LocalDate startDate,
            @Schema(description = "시험일") LocalDate examDate,
            @Schema(description = "전체 일수") int totalDays
    ) {
    }

    @Schema(description = "일정바")
    public record TimelineBar(
            @Schema(description = "제목") String title,
            @Schema(description = "시작일") LocalDate startDate,
            @Schema(description = "종료일") LocalDate endDate
    ) {
    }

    @Schema(description = "일일 항목")
    public record DailyPoint(
            @Schema(description = "날짜") LocalDate pointDate,
            @Schema(description = "내용") String content,
            @Schema(description = "휴식 여부") boolean isRest
    ) {
    }
}
