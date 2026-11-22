package com.certsniper.qualification.service;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.qualification.dto.TrendResponse;
import com.certsniper.qualification.repository.QualificationStatisticsYearlyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrendService {

    private final QualificationStatisticsYearlyRepository qualificationStatisticsYearlyRepository;

    public TrendResponse getTrends(
            String qualgbcd,
            String gradeLevel,
            String interestFieldCode,
            Short year,
            int limit) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
