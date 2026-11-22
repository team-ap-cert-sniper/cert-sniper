package com.certsniper.aiplanner.controller;

import com.certsniper.aiplanner.dto.AiPlanResponse;
import com.certsniper.aiplanner.dto.SurveyRequest;
import com.certsniper.aiplanner.service.AiPlannerService;
import com.certsniper.global.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "11. AI 플래너")
@RestController
@RequestMapping("/api/v1/ai-planner")
@RequiredArgsConstructor
public class AiPlannerController {

    private final AiPlannerService aiPlannerService;

    @Operation(summary = "AI 플랜 생성·검증·저장")
    @PostMapping
    public AiPlanResponse createAiPlan(
            @LoginUser Long userId,
            @Valid @RequestBody SurveyRequest request) {
        return aiPlannerService.createAiPlan(userId, request);
    }
}
