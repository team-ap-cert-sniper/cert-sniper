package com.certsniper.planner.service;

import com.certsniper.aiplanner.dto.AiPlanResponse;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.planner.dto.BarRequest;
import com.certsniper.planner.dto.MilestoneRequest;
import com.certsniper.planner.dto.PersonalScheduleRequest;
import com.certsniper.planner.dto.PlannerResponse;
import com.certsniper.planner.dto.TodayScheduleResponse;
import com.certsniper.planner.entity.Planner;
import com.certsniper.planner.entity.PlannerBar;
import com.certsniper.planner.entity.PlannerMilestone;
import com.certsniper.planner.repository.PersonalScheduleRepository;
import com.certsniper.planner.repository.PlannerBarRepository;
import com.certsniper.planner.repository.PlannerMilestoneRepository;
import com.certsniper.planner.repository.PlannerRepository;
import com.certsniper.targetexam.service.TargetExamService;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
    private final TargetExamService targetExamService;

    /** 한 번에 조회할 수 있는 최대 기간 (일) */
    static final long MAX_RANGE_DAYS = 366;
    /** 일정명·세부 일정 내용 최대 길이 (DB varchar(100)) */
    static final int MAX_TEXT = 100;
    static final String SOURCE_AI = "AI";

    /** 플래너 조회: 기간과 겹치는 일정바 + 그 기간 안의 세부 일정 + 개인 일정 목록. 플래너가 아직 없으면 MANUAL */
    public PlannerResponse getPlanner(Long userId, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "조회 시작일이 종료일보다 늦습니다.");
        }
        if (ChronoUnit.DAYS.between(from, to) > MAX_RANGE_DAYS) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "한 번에 1년까지만 조회할 수 있습니다.");
        }
        String mode = plannerRepository.findById(userId).map(Planner::getMode).orElse(Planner.MANUAL);

        List<PlannerBar> bars = plannerBarRepository.findOverlapping(userId, from, to);
        Set<Long> barIds = bars.stream().map(PlannerBar::getId).collect(Collectors.toSet());
        Map<Long, List<PlannerResponse.Milestone>> milestonesByBar = barIds.isEmpty() ? Map.of()
                : plannerMilestoneRepository
                        .findAllByBarIdInAndPointDateBetweenOrderByPointDateAscIdAsc(barIds, from, to).stream()
                        .collect(Collectors.groupingBy(PlannerMilestone::getBarId,
                                Collectors.mapping(this::toMilestone, Collectors.toList())));

        List<PlannerResponse.Bar> barResponses = bars.stream()
                .map(b -> new PlannerResponse.Bar(b.getId(), b.getTargetExamId(), b.getPersonalScheduleId(),
                        b.getTitle(), b.getStartDate(), b.getEndDate(), b.getIsSingleDay(), b.getSource(),
                        milestonesByBar.getOrDefault(b.getId(), List.of())))
                .toList();
        List<PlannerResponse.PersonalSchedule> personal = personalScheduleRepository
                .findAllByUserIdOrderByIdAsc(userId).stream()
                .map(p -> new PlannerResponse.PersonalSchedule(p.getId(), p.getName(), p.getDisplayColor()))
                .toList();
        return new PlannerResponse(mode, barResponses, personal);
    }

    /** 모드 전환 (AI_RENDER · MANUAL). 플래너가 없으면 만든다 */
    @Transactional
    public void changeMode(Long userId, String mode) {
        if (!Planner.AI_RENDER.equals(mode) && !Planner.MANUAL.equals(mode)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "모드는 AI_RENDER 또는 MANUAL만 가능합니다.");
        }
        plannerRepository.findById(userId).ifPresentOrElse(
                planner -> planner.changeMode(mode),
                () -> plannerRepository.save(Planner.create(userId, mode)));
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

    /**
     * aiplanner 플랜 저장: 이 자격증의 기존 AI 일정바(세부 일정 포함)를 지우고 새 플랜으로 채운 뒤 AI_RENDER 모드로 전환.
     * 사용자가 직접 만든(MANUAL) 일정바는 남긴다. 일일 항목은 날짜가 들어가는 일정바에 붙이고, 어느 일정바에도 안 들어가면 400
     */
    @Transactional
    public void replaceAiSchedule(Long userId, Long targetExamId, AiPlanResponse plan) {
        targetExamService.validateOwner(userId, targetExamId);
        if (plan.timelineBars() == null || plan.timelineBars().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "AI 플랜에 일정바가 없습니다.");
        }
        plannerBarRepository.deleteAiBarsByTargetExamId(targetExamId);

        List<PlannerBar> bars = new ArrayList<>();
        for (AiPlanResponse.TimelineBar t : plan.timelineBars()) {
            if (t.startDate() == null || t.endDate() == null || t.endDate().isBefore(t.startDate())) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "AI 플랜의 일정바 날짜가 올바르지 않습니다.");
            }
            bars.add(PlannerBar.create(userId, targetExamId, null, cut(t.title()),
                    t.startDate(), t.endDate(), false, SOURCE_AI));
        }
        bars = plannerBarRepository.saveAll(bars);

        List<PlannerMilestone> milestones = new ArrayList<>();
        List<AiPlanResponse.DailyPoint> points = plan.dailyPoints() == null ? List.of() : plan.dailyPoints();
        for (AiPlanResponse.DailyPoint point : points) {
            PlannerBar bar = bars.stream()
                    .filter(b -> point.pointDate() != null && b.contains(point.pointDate()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.VALIDATION_ERROR,
                            "AI 플랜의 일일 항목 날짜가 일정바 기간 밖입니다."));
            milestones.add(PlannerMilestone.create(bar.getId(), userId, point.pointDate(),
                    point.isRest() ? "휴식" : cut(point.content()), point.isRest(), SOURCE_AI));
        }
        plannerMilestoneRepository.saveAll(milestones);
        changeMode(userId, Planner.AI_RENDER);
    }

    /** user 탈퇴 확정: 일정바(세부 일정 포함) → 개인 일정 → 플래너 순서로 전체 삭제 */
    @Transactional
    public void deleteAllByUser(Long userId) {
        plannerBarRepository.deleteAllByUserId(userId);
        personalScheduleRepository.deleteAllByUserId(userId);
        plannerRepository.deleteAllByUserId(userId);
    }

    private PlannerResponse.Milestone toMilestone(PlannerMilestone m) {
        return new PlannerResponse.Milestone(m.getId(), m.getPointDate(), m.getContent(), m.getIsRest(), m.getSource());
    }

    /** 비어 있으면 "학습", 100자가 넘으면 자른다 (DB 길이 초과로 저장이 깨지지 않게) */
    private static String cut(String text) {
        if (text == null || text.isBlank()) {
            return "학습";
        }
        String trimmed = text.strip();
        return trimmed.length() > MAX_TEXT ? trimmed.substring(0, MAX_TEXT) : trimmed;
    }
}
