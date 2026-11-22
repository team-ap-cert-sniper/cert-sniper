package com.certsniper.option.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "선택 목록 항목")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OptionItem(
        @Schema(description = "ID (대학 · 학과 · 학년)") String id,
        @Schema(description = "직무분야 코드") String code,
        @Schema(description = "이름") String name,
        @Schema(description = "캠퍼스명") String campusName,
        @Schema(description = "학교 유형") String schoolType,
        @Schema(description = "소속 표시명") String parentName,
        @Schema(description = "선택 범위 (MAJOR · FACULTY)") String scope,
        @Schema(description = "표시명") String label,
        @Schema(description = "종목 수") Integer count,
        @Schema(description = "자격 종목코드") String jmcd,
        @Schema(description = "자격 종목명") String jmfldnm,
        @Schema(description = "자격구분 코드 (T 국가기술 · S 국가전문)") String qualgbcd,
        @Schema(description = "자격구분명") String qualgbnm,
        @Schema(description = "등급") String gradeLevel
) {}
