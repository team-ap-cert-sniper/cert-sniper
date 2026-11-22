package com.certsniper.planner.dto;

import com.certsniper.planner.entity.PersonalSchedule;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "플래너")
public record PlannerResponse(
        @Schema(description = "모드") String mode,
        @Schema(description = "일정바 목록") List<Bar> bars,
        @Schema(description = "개인 일정 목록") List<PersonalSchedule> personalSchedules
) {

    @Schema(description = "일정바")
    public record Bar(
            @Schema(description = "일정바 ID") Long id,
            @Schema(description = "목표 자격증 ID") Long targetExamId,
            @Schema(description = "개인 일정 ID") Long personalScheduleId,
            @Schema(description = "일정명") String title,
            @Schema(description = "시작일") LocalDate startDate,
            @Schema(description = "종료일") LocalDate endDate,
            @Schema(description = "당일 일정 여부") boolean isSingleDay,
            @Schema(description = "생성 방식") String source,
            @Schema(description = "세부 일정 목록") List<Milestone> milestones
    ) {
    }

    @Schema(description = "세부 일정")
    public record Milestone(
            @Schema(description = "세부 일정 ID") Long id,
            @Schema(description = "날짜") LocalDate pointDate,
            @Schema(description = "내용") String content,
            @Schema(description = "휴식 여부") boolean isRest,
            @Schema(description = "생성 방식") String source
    ) {
    }

    @Schema(description = "개인 일정")
    public record PersonalSchedule(
            @Schema(description = "개인 일정 ID") Long id,
            @Schema(description = "일정 이름") String name,
            @Schema(description = "일정 색상") String displayColor
    ) {
    }
}
