package com.certsniper.recommendation.service;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.recommendation.dto.RecommendationRequest;
import com.certsniper.recommendation.dto.RecommendationResponse;
import com.certsniper.recommendation.repository.MajorQualificationLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final MajorQualificationLinkRepository majorQualificationLinkRepository;

    @Transactional
    public RecommendationResponse recommend(Long userId, RecommendationRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
