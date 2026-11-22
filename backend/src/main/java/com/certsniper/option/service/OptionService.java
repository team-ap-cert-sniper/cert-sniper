package com.certsniper.option.service;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.option.dto.OptionResponse;
import com.certsniper.option.dto.SchoolInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OptionService {

    public OptionResponse getOptions(
            String kind,
            Long universityId,
            String query,
            int offset,
            int limit,
            String qualgbcd,
            String gradeLevel,
            String interestFieldCode,
            String jobCategoryCode) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** auth·user: 대학·학부·전공·학년 묶음 검증 (셋 다 입력 또는 셋 다 비움) */
    public void validateSchoolInfo(Long universityId, Long collegeId, Long majorId, String grade) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** user 회원정보: 대학·학부·학과 표시명 */
    public SchoolInfoResponse getSchoolNames(Long universityId, Long collegeId, Long majorId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
