package com.certsniper.planner.service;

import com.certsniper.aiplanner.dto.AiPlanResponse;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.planner.dto.BarRequest;
import com.certsniper.planner.dto.MilestoneRequest;
import com.certsniper.planner.dto.PersonalScheduleRequest;
import com.certsniper.planner.dto.PlannerResponse;
import com.certsniper.planner.dto.TodayScheduleResponse;
import com.certsniper.planner.repository.PersonalScheduleRepository;
import com.certsniper.planner.repository.PlannerBarRepository;
import com.certsniper.planner.repository.PlannerMilestoneRepository;
import com.certsniper.planner.repository.PlannerRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlannerService {

    private final PlannerRepository plannerRepository;
    private final PlannerBarRepository plannerBarRepository;
    private final PlannerMilestoneRepository plannerMilestoneRepository;
    private final PersonalScheduleRepository personalScheduleRepository;

    public PlannerResponse getPlanner(Long userId, LocalDate from, LocalDate to) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void changeMode(Long userId, String mode) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void createBar(Long userId, BarRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void updateBar(Long userId, Long barId, BarRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void deleteBar(Long userId, Long barId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void createMilestone(Long userId, Long barId, MilestoneRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void updateMilestone(Long userId, Long milestoneId, MilestoneRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void deleteMilestone(Long userId, Long milestoneId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void createPersonalSchedule(Long userId, PersonalScheduleRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void updatePersonalSchedule(Long userId, Long scheduleId, PersonalScheduleRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Transactional
    public void deletePersonalSchedule(Long userId, Long scheduleId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    public TodayScheduleResponse getTodaySchedule(Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** aiplanner 플랜 저장: 기존 AI 일정 교체 후 AI_RENDER 모드로 전환 */
    @Transactional
    public void replaceAiSchedule(Long userId, Long targetExamId, AiPlanResponse plan) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    /** user 탈퇴 확정: 플래너·개인 일정 전체 삭제 */
    @Transactional
    public void deleteAllByUser(Long userId) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
