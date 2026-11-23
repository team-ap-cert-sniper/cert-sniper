package com.certsniper.planner.service;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.planner.repository.PersonalScheduleRepository;
import com.certsniper.targetexam.repository.TargetExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 일정 6개 제한 (aiplanner 도 사용) */
@Component
@RequiredArgsConstructor
public class ScheduleLimitValidator {

    public static final int MAX_SCHEDULES = 6;

    private final TargetExamRepository targetExamRepository;
    private final PersonalScheduleRepository personalScheduleRepository;

    /** 목표 자격증 + 개인 일정이 6개면 LIMIT_EXCEEDED */
    public void validateCanAdd(Long userId) {
        long count = targetExamRepository.countByUserId(userId)
                + personalScheduleRepository.countByUserId(userId);
        if (count >= MAX_SCHEDULES) {
            throw new BusinessException(ErrorCode.LIMIT_EXCEEDED);
        }
    }
}
