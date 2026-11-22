package com.certsniper.planner.controller;

import com.certsniper.planner.dto.DailyIdiomResponse;
import com.certsniper.planner.service.DailyIdiomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "09. 플래너 다이어리")
@RestController
@RequestMapping("/api/v1/daily-idiom")
@RequiredArgsConstructor
public class DailyIdiomController {

    private final DailyIdiomService dailyIdiomService;

    @Operation(summary = "오늘의 사자성어")
    @GetMapping
    public DailyIdiomResponse getDailyIdiom() {
        return dailyIdiomService.getDailyIdiom();
    }
}
