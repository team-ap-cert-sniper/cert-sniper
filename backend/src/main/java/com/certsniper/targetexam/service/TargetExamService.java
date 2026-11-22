package com.certsniper.targetexam.service;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.targetexam.dto.TargetExamRequest;
import com.certsniper.targetexam.dto.TargetExamResponse;
import com.certsniper.targetexam.repository.TargetExamRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TargetExamService {

    private final TargetExamRepository targetExamRepository;

    public List<TargetExamResponse> getTargetExams(Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public TargetExamResponse addTargetExam(Long userId, TargetExamRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public TargetExamResponse updateTargetExam(Long userId, Long targetExamId, TargetExamRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void deleteTargetExam(Long userId, Long targetExamId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** aiplanner 플랜 저장: 목표 자격증이 없으면 만들고 ID 반환 */
    @Transactional
    public Long getOrCreateTargetExam(Long userId, String jmcd, Long examScheduleId, LocalDate examDate) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** planner·memo: 내 목표 자격증인지 확인 (아니면 FORBIDDEN) */
    public void validateOwner(Long userId, Long targetExamId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** planner 일정 6개 제한 검사: 목표 자격증 수 */
    public long countByUser(Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** user 탈퇴 확정: 목표 자격증 전체 삭제 (일정바·메모는 DB에서 같이 삭제) */
    @Transactional
    public void deleteAllByUser(Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
