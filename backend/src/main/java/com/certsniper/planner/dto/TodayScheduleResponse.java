package com.certsniper.planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "그날의 일정 (사이드바)")
public record TodayScheduleResponse(
        @Schema(description = "날짜") LocalDate date,
        @Schema(description = "일정 묶음 (목표 자격증 먼저, 그다음 개인 일정)") List<Group> groups
) {

    @Schema(description = "자격증·개인 일정별 묶음")
    public record Group(
            @Schema(description = "목표 자격증 ID (개인 일정이면 null)") Long targetExamId,
            @Schema(description = "개인 일정 ID (목표 자격증이면 null)") Long personalScheduleId,
            @Schema(description = "이름 (자격증명 또는 개인 일정 이름)") String name,
            @Schema(description = "일정 색상") String displayColor,
            @Schema(description = "그날 할 일 (세부 일정 내용, 없으면 일정바 제목)") List<String> items
    ) {
    }
}
