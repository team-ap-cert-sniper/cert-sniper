package com.certsniper.passstory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "합격수기 등록·수정 요청")
public record PassStoryRequest(
        @Schema(description = "자격 종목코드") @NotBlank String jmcd,
        @Schema(description = "상세 후기·실전 팁") @NotBlank @Size(max = 1500) String body,
        @Schema(description = "응시자 베이스") @Pattern(regexp = "NO_BASE|BASIC|MAJOR|PRACTITIONER") String examineeBase,
        @Schema(description = "준비 기간") @Pattern(regexp = "UNDER_1M|M1_TO_3M|M3_TO_6M|OVER_6M") String prepPeriod,
        @Schema(description = "응시 횟수") @Pattern(regexp = "1|2|3|4_PLUS") String attemptBand,
        @Schema(description = "사용 교재명") @Size(max = 50) String textbookName,
        @Schema(description = "합격 점수") @DecimalMin("0") @Digits(integer = 4, fraction = 2) BigDecimal passScore,
        @Schema(description = "시험 응시일") LocalDate examDate
) {}
