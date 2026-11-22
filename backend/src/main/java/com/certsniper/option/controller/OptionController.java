package com.certsniper.option.controller;

import com.certsniper.option.dto.OptionResponse;
import com.certsniper.option.service.OptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "03. 선택 목록")
@RestController
@RequestMapping("/api/v1/options")
@RequiredArgsConstructor
public class OptionController {

    private final OptionService optionService;

    @Operation(summary = "선택 목록 (universities, majors, grades, interest_fields, job_categories, qualifications)")
    @GetMapping("/{kind}")
    public OptionResponse getOptions(
            @Parameter(description = "목록 종류 (universities · majors · grades · interest_fields · job_categories · qualifications)") @PathVariable String kind,
            @Parameter(description = "대학 ID (majors 필수)") @RequestParam(name = "university_id", required = false) Long universityId,
            @Parameter(description = "검색어 (이름 부분 일치)") @RequestParam(name = "query", defaultValue = "") String query,
            @Parameter(description = "시작 위치") @RequestParam(name = "offset", defaultValue = "0") int offset,
            @Parameter(description = "최대 건수 (1~100)") @RequestParam(name = "limit", defaultValue = "50") int limit,
            @Parameter(description = "자격구분 (T 국가기술 · S 국가전문)") @RequestParam(name = "qualgbcd", required = false) String qualgbcd,
            @Parameter(description = "등급 (국가기술만)") @RequestParam(name = "grade_level", required = false) String gradeLevel,
            @Parameter(description = "관심 분야 코드") @RequestParam(name = "interest_field_code", required = false) String interestFieldCode,
            @Parameter(description = "희망 직무 코드") @RequestParam(name = "job_category_code", required = false) String jobCategoryCode) {
        return optionService.getOptions(kind, universityId, query, offset, limit, qualgbcd, gradeLevel, interestFieldCode, jobCategoryCode);
    }
}
