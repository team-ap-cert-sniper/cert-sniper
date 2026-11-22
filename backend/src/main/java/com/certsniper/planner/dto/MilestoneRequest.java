package com.certsniper.planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "세부 일정 등록·수정 요청")
public record MilestoneRequest(
        @Schema(description = "날짜") @NotNull LocalDate pointDate,
        @Schema(description = "내용") @NotBlank @Size(max = 100) String content,
        @Schema(description = "휴식 여부") boolean isRest
) {}
