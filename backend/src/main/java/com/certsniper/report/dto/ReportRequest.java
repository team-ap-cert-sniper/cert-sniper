package com.certsniper.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "신고 요청")
public record ReportRequest(
        @Schema(description = "신고 사유") @NotBlank @Pattern(regexp = "IRRELEVANT|ABUSE|PRIVACY|AD|ETC") String reportType
) {}
