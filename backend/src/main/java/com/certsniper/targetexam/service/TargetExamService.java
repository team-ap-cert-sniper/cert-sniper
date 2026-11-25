package com.certsniper.targetexam.service;

import com.certsniper.examschedule.service.DdayCalculator;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.global.util.DateUtils;
import com.certsniper.planner.service.ScheduleLimitValidator;
import com.certsniper.qualification.service.QualificationService;
import com.certsniper.targetexam.dto.TargetExamRequest;
import com.certsniper.targetexam.dto.TargetExamResponse;
import com.certsniper.targetexam.entity.TargetExam;
import com.certsniper.targetexam.repository.TargetExamRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TargetExamService {

    /** AI 플랜으로 추가할 때 자동으로 고르는 색 (일정 최대 6개라 6색) */
    static final List<String> PALETTE = List.of("#E5484D", "#F5A524", "#30A46C", "#0091FF", "#8E4EC6", "#E93D82");

    private final TargetExamRepository targetExamRepository;
    private final QualificationService qualificationService;
    private final DdayCalculator ddayCalculator;
    private final ScheduleLimitValidator scheduleLimitValidator;

    public List<TargetExamResponse> getTargetExams(Long userId) {
        return targetExamRepository.findAllByUserIdOrderByIdAsc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    /** 플래너 검색 팝업에서 직접 추가 (없는 종목 404, 같은 자격증·같은 색 409, 일정 6개 초과 409) */
    @Transactional
    public TargetExamResponse addTargetExam(Long userId, TargetExamRequest request) {
        qualificationService.getInfo(request.jmcd());
        if (targetExamRepository.existsByUserIdAndJmcd(userId, request.jmcd())) {
            throw new BusinessException(ErrorCode.DUPLICATE);
        }
        if (targetExamRepository.existsByUserIdAndDisplayColor(userId, request.displayColor())) {
            throw new BusinessException(ErrorCode.DUPLICATE);
        }
        scheduleLimitValidator.validateCanAdd(userId);
        TargetExam saved = targetExamRepository.save(TargetExam.create(
                userId, request.jmcd(), request.examScheduleId(), request.examDate(), request.displayColor()));
        return toResponse(saved);
    }

    @Transactional
    public TargetExamResponse updateTargetExam(Long userId, Long targetExamId, TargetExamRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void deleteTargetExam(Long userId, Long targetExamId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** aiplanner 플랜 저장: 이미 목표면 시험일만 바꾸고, 없으면 안 쓰는 색으로 만들어 ID 반환 */
    @Transactional
    public Long getOrCreateTargetExam(Long userId, String jmcd, Long examScheduleId, LocalDate examDate) {
        return targetExamRepository.findByUserIdAndJmcd(userId, jmcd)
                .map(existing -> {
                    existing.changeExam(examScheduleId, examDate);
                    return existing.getId();
                })
                .orElseGet(() -> {
                    scheduleLimitValidator.validateCanAdd(userId);
                    String color = pickUnusedColor(userId);
                    return targetExamRepository
                            .save(TargetExam.create(userId, jmcd, examScheduleId, examDate, color))
                            .getId();
                });
    }

    /** planner·memo: 내 목표 자격증인지 확인 (없으면 NOT_FOUND, 남의 것이면 FORBIDDEN) */
    public void validateOwner(Long userId, Long targetExamId) {
        TargetExam targetExam = targetExamRepository.findById(targetExamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (!targetExam.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    /** planner 일정 6개 제한 검사: 목표 자격증 수 */
    public long countByUser(Long userId) {
        return targetExamRepository.countByUserId(userId);
    }

    /** user 탈퇴 확정: 목표 자격증 전체 삭제 (일정바·메모는 DB에서 같이 삭제) */
    @Transactional
    public void deleteAllByUser(Long userId) {
        targetExamRepository.deleteAllByUserId(userId);
    }

    private String pickUnusedColor(Long userId) {
        Set<String> used = targetExamRepository.findAllByUserIdOrderByIdAsc(userId).stream()
                .map(t -> t.getDisplayColor().toUpperCase())
                .collect(Collectors.toSet());
        return PALETTE.stream()
                .filter(color -> !used.contains(color))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.LIMIT_EXCEEDED));
    }

    private TargetExamResponse toResponse(TargetExam t) {
        String name = qualificationService.getInfo(t.getJmcd()).jmfldnm();
        String dday = t.getExamDate() == null ? null : ddayCalculator.label(t.getExamDate(), DateUtils.today());
        return new TargetExamResponse(
                t.getId(), t.getJmcd(), name, t.getExamScheduleId(), t.getExamDate(), dday, t.getDisplayColor());
    }
}
