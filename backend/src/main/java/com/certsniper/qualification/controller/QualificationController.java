package com.certsniper.qualification.controller;

import com.certsniper.qualification.dto.QualificationDetailResponse;
import com.certsniper.qualification.service.QualificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "05. 자격증 상세")
@RestController
@RequestMapping("/api/v1/qualifications")
@RequiredArgsConstructor
public class QualificationController {

    private final QualificationService qualificationService;

    @Operation(summary = "자격증 상세 (기본정보·응시조건·합격률·시험일정)")
    @GetMapping("/{jmcd}")
    public QualificationDetailResponse getQualificationDetail(@Parameter(description = "자격 종목코드") @PathVariable String jmcd) {
        return qualificationService.getQualificationDetail(jmcd);
    }
}
