package com.certsniper.planner.controller;

import com.certsniper.global.security.LoginUser;
import com.certsniper.global.util.DateUtils;
import com.certsniper.planner.dto.TodayScheduleResponse;
import com.certsniper.planner.service.PlannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "09. 플래너 다이어리")
@RestController
@RequestMapping("/api/v1/planner/today")
@RequiredArgsConstructor
public class TodayScheduleController {

    private final PlannerService plannerService;

    @Operation(summary = "그날의 일정 (사이드바, 날짜를 안 보내면 오늘)")
    @GetMapping
    public TodayScheduleResponse getTodaySchedule(
            @LoginUser Long userId,
            @Parameter(description = "날짜 (기본 오늘)") @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return plannerService.getTodaySchedule(userId, date == null ? DateUtils.today() : date);
    }
}
