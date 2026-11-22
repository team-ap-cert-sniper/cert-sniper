package com.certsniper.recommendation.service;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.recommendation.dto.ConditionRecommendationRequest;
import com.certsniper.recommendation.dto.RecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConditionRecommendationService {

    @Transactional
    public RecommendationResponse recommendByCondition(Long userId, ConditionRecommendationRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
