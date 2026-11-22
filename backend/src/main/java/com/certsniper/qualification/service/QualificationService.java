package com.certsniper.qualification.service;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.qualification.dto.QualificationDetailResponse;
import com.certsniper.qualification.repository.QualificationEligibilityRuleRepository;
import com.certsniper.qualification.repository.QualificationRepository;
import com.certsniper.qualification.repository.QualificationStatisticsYearlyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QualificationService {

    private final QualificationRepository qualificationRepository;
    private final QualificationStatisticsYearlyRepository qualificationStatisticsYearlyRepository;
    private final QualificationEligibilityRuleRepository qualificationEligibilityRuleRepository;

    public QualificationDetailResponse getQualificationDetail(String jmcd) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** favorite·passstory·aiplanner: 자격증 기본정보 (없으면 NOT_FOUND) */
    public QualificationDetailResponse.QualificationInfo getInfo(String jmcd) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** admin 대시보드: 등록 자격증 수 */
    public long countQualifications() {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
