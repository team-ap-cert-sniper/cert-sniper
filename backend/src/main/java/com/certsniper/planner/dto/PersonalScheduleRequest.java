package com.certsniper.planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "개인 일정 등록·수정 요청")
public record PersonalScheduleRequest(
        @Schema(description = "일정 이름") @NotBlank @Size(max = 20) String name,
        @Schema(description = "일정 색상") @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String displayColor
) {}
