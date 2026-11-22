package com.certsniper.planner.service;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

/** 일정 6개 제한 (aiplanner 도 사용) */
@Component
public class ScheduleLimitValidator {

    /** 목표 자격증 + 개인 일정이 6개면 LIMIT_EXCEEDED */
    public void validateCanAdd(Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
