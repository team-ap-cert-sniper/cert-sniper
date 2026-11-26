package com.certsniper.targetexam.service;

import com.certsniper.examschedule.dto.ExamScheduleResponse;
import com.certsniper.examschedule.service.DdayCalculator;
import com.certsniper.examschedule.service.ExamScheduleService;
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
import java.util.Objects;
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
    private final ExamScheduleService examScheduleService;
    private final DdayCalculator ddayCalculator;
    private final ScheduleLimitValidator scheduleLimitValidator;

    public List<TargetExamResponse> getTargetExams(Long userId) {
        return targetExamRepository.findAllByUserIdOrderByIdAsc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    /** 플래너 검색 팝업에서 직접 추가 (없는 종목 404, 회차·시험일 오류 400, 같은 자격증·같은 색 409, 일정 6개 초과 409) */
    @Transactional
    public TargetExamResponse addTargetExam(Long userId, TargetExamRequest request) {
        String jmcd = qualificationService.getInfo(request.jmcd()).jmcd();
        validateExam(jmcd, request.examScheduleId(), request.examDate());
        if (targetExamRepository.existsByUserIdAndJmcd(userId, jmcd)) {
            throw new BusinessException(ErrorCode.DUPLICATE, "이미 목표로 추가한 자격증입니다.");
        }
        if (targetExamRepository.existsByUserIdAndDisplayColor(userId, request.displayColor())) {
            throw new BusinessException(ErrorCode.DUPLICATE, "다른 목표 자격증에서 사용 중인 색입니다.");
        }
        scheduleLimitValidator.validateCanAdd(userId);
        TargetExam saved = targetExamRepository.save(TargetExam.create(
                userId, jmcd, request.examScheduleId(), request.examDate(), request.displayColor()));
        return toResponse(saved);
    }

        /** 플래너에서 수정: 시험 회차·시험일·색상만 바꾼다. 자격증 자체는 바꿀 수 없다 (없음 404, 남의 것 403, 색 중복 409) */
    @Transactional
    public TargetExamResponse updateTargetExam(Long userId, Long targetExamId, TargetExamRequest request) {
        TargetExam targetExam = findOwned(userId, targetExamId);
        String jmcd = qualificationService.getInfo(request.jmcd()).jmcd();
        if (!jmcd.equals(targetExam.getJmcd())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "목표 자격증의 자격증은 바꿀 수 없습니다. 삭제 후 새로 추가해 주세요.");
        }
        // 회차·시험일이 실제로 바뀔 때만 검사한다 (지난 시험이어도 색만 바꾸는 건 허용)
        boolean examChanged = !Objects.equals(request.examScheduleId(), targetExam.getExamScheduleId())
                || !Objects.equals(request.examDate(), targetExam.getExamDate());
        if (examChanged) {
            validateExam(jmcd, request.examScheduleId(), request.examDate());
        }
        if (targetExamRepository.existsByUserIdAndDisplayColorAndIdNot(userId, request.displayColor(), targetExamId)) {
            throw new BusinessException(ErrorCode.DUPLICATE, "다른 목표 자격증에서 사용 중인 색입니다.");
        }
        targetExam.changeExam(request.examScheduleId(), request.examDate());
        targetExam.changeColor(request.displayColor());
        return toResponse(targetExam);
    }

    /** 플래너에서 삭제: 이 자격증의 일정바·세부 일정·메모는 DB의 ON DELETE CASCADE로 같이 삭제된다 (없음 404, 남의 것 403) */
    @Transactional
    public void deleteTargetExam(Long userId, Long targetExamId) {
        targetExamRepository.delete(findOwned(userId, targetExamId));
    }

    /** aiplanner 플랜 저장: 이미 목표면 시험일만 바꾸고, 없으면 안 쓰는 색으로 만들어 ID 반환 */
    @Transactional
    public Long getOrCreateTargetExam(Long userId, String rawJmcd, Long examScheduleId, LocalDate examDate) {
        String jmcd = qualificationService.getInfo(rawJmcd).jmcd();
        validateExam(jmcd, examScheduleId, examDate);
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
        findOwned(userId, targetExamId);
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

    /** 내 목표 자격증 찾기 (없으면 NOT_FOUND, 남의 것이면 FORBIDDEN) */
    private TargetExam findOwned(Long userId, Long targetExamId) {
        TargetExam targetExam = targetExamRepository.findById(targetExamId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "목표 자격증을 찾을 수 없습니다."));
        if (!targetExam.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return targetExam;
    }

    /** 회차와 시험일은 둘 다 비우거나 둘 다 넣는다. 넣으면 이 자격증의 회차이고, 회차 기간 안이고, 지나지 않은 날이어야 한다 */
    private void validateExam(String jmcd, Long examScheduleId, LocalDate examDate) {
        if (examScheduleId == null && examDate == null) {
            return;
        }
        if (examScheduleId == null || examDate == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "시험 회차와 시험일을 함께 선택해 주세요.");
        }
        ExamScheduleResponse schedule = examScheduleService.getSchedules(jmcd).stream()
                .filter(s -> s.scheduleId().equals(examScheduleId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR, "이 자격증의 시험 회차가 아닙니다."));
        if (examDate.isBefore(schedule.examStartDate()) || examDate.isAfter(schedule.examEndDate())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "시험일이 선택한 회차 기간이 아닙니다.");
        }
        if (examDate.isBefore(DateUtils.today())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "이미 지난 시험입니다.");
        }
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
