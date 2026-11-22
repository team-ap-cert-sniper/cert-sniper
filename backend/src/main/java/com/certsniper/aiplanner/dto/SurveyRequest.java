package com.certsniper.aiplanner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "AI 플래너 설문")
public record SurveyRequest(
        @Schema(description = "자격 종목코드") @NotBlank String jmcd,
        @Schema(description = "선택한 시험 회차 ID") Long examScheduleId,
        @Schema(description = "시험일") @NotNull LocalDate examDate,
        @Schema(description = "설문 답변") @NotNull List<Answer> answers
) {

    @Schema(description = "설문 답변")
    public record Answer(
            @Schema(description = "설문 단계") int step,
            @Schema(description = "답변") String value
    ) {
    }
}
