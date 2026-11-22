package com.certsniper.recommendation.controller;

import com.certsniper.global.security.LoginUser;
import com.certsniper.recommendation.dto.ConditionRecommendationRequest;
import com.certsniper.recommendation.dto.RecommendationRequest;
import com.certsniper.recommendation.dto.RecommendationResponse;
import com.certsniper.recommendation.service.ConditionRecommendationService;
import com.certsniper.recommendation.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "04. 자격증 추천")
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final ConditionRecommendationService conditionRecommendationService;
    private final RecommendationService recommendationService;

    @Operation(summary = "전공 맞춤 추천 (회원정보 기준)")
    @PostMapping
    public RecommendationResponse recommend(
            @LoginUser Long userId,
            @Valid @RequestBody RecommendationRequest request) {
        return recommendationService.recommend(userId, request);
    }

    @Operation(summary = "조건 선택 추천")
    @PostMapping("/conditions")
    public RecommendationResponse recommendByCondition(
            @LoginUser Long userId,
            @Valid @RequestBody ConditionRecommendationRequest request) {
        return conditionRecommendationService.recommendByCondition(userId, request);
    }
}
