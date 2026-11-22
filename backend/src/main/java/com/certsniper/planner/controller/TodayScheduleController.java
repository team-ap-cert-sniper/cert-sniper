package com.certsniper.planner.controller;

import com.certsniper.global.security.LoginUser;
import com.certsniper.planner.dto.TodayScheduleResponse;
import com.certsniper.planner.service.PlannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "09. 플래너 다이어리")
@RestController
@RequestMapping("/api/v1/planner/today")
@RequiredArgsConstructor
public class TodayScheduleController {

    private final PlannerService plannerService;

    @Operation(summary = "오늘의 일정 (메인 패널)")
    @GetMapping
    public TodayScheduleResponse getTodaySchedule(@LoginUser Long userId) {
        return plannerService.getTodaySchedule(userId);
    }
}
