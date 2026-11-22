package com.certsniper.qualification.controller;

import com.certsniper.qualification.dto.TrendResponse;
import com.certsniper.qualification.service.TrendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "05. 자격증 상세")
@RestController
@RequestMapping("/api/v1/trends")
@RequiredArgsConstructor
public class TrendController {

    private final TrendService trendService;

    @Operation(summary = "주목받는 자격증 트렌드")
    @GetMapping
    public TrendResponse getTrends(
            @Parameter(description = "자격구분 (T 국가기술 · S 국가전문)") @RequestParam(name = "qualgbcd", defaultValue = "T") String qualgbcd,
            @Parameter(description = "등급 (국가기술만)") @RequestParam(name = "grade_level", required = false) String gradeLevel,
            @Parameter(description = "관심 분야 코드") @RequestParam(name = "interest_field_code", required = false) String interestFieldCode,
            @Parameter(description = "기준 연도") @RequestParam(name = "year", required = false) Short year,
            @Parameter(description = "최대 건수 (1~100)") @RequestParam(name = "limit", defaultValue = "20") int limit) {
        return trendService.getTrends(qualgbcd, gradeLevel, interestFieldCode, year, limit);
    }
}
