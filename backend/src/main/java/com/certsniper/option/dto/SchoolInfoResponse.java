package com.certsniper.option.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "학력 표시명")
public record SchoolInfoResponse(
        @Schema(description = "대학명") String universityName,
        @Schema(description = "학부(단과대학)명") String collegeName,
        @Schema(description = "학과명") String majorName
) {}
