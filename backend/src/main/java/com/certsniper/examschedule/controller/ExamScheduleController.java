package com.certsniper.examschedule.controller;

import com.certsniper.examschedule.dto.DdayResponse;
import com.certsniper.examschedule.service.ExamScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "06. 시험일정")
@RestController
@RequestMapping("/api/v1/exam-schedules")
@RequiredArgsConstructor
public class ExamScheduleController {

    private final ExamScheduleService examScheduleService;

    @Operation(summary = "시험 D-Day")
    @GetMapping("/{scheduleId}/dday")
    public DdayResponse getDday(
            @Parameter(description = "시험 일정 ID") @PathVariable Long scheduleId,
            @Parameter(description = "실제 응시일 (시험 기간이 여러 날이면 필수)") @RequestParam(name = "exam_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate examDate) {
        return examScheduleService.getDday(scheduleId, examDate);
    }
}
