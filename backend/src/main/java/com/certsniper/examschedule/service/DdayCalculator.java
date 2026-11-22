package com.certsniper.examschedule.service;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

/** D-n · D-Day · 종료 계산 (planner·targetexam 도 사용) */
@Component
public class DdayCalculator {

    /** 남은 일수 (지난 시험은 음수) */
    public long daysRemaining(LocalDate examDate, LocalDate today) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** D-n · D-Day · 종료 */
    public String label(LocalDate examDate, LocalDate today) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
