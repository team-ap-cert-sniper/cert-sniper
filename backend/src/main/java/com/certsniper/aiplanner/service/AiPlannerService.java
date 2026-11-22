package com.certsniper.aiplanner.service;

import com.certsniper.aiplanner.dto.AiPlanResponse;
import com.certsniper.aiplanner.dto.SurveyRequest;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiPlannerService {

    @Transactional
    public AiPlanResponse createAiPlan(Long userId, SurveyRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
