package com.certsniper.planner.service;

import com.certsniper.aiplanner.dto.AiPlanResponse;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.planner.dto.BarRequest;
import com.certsniper.planner.dto.MilestoneRequest;
import com.certsniper.planner.dto.PersonalScheduleRequest;
import com.certsniper.planner.dto.PlannerResponse;
import com.certsniper.planner.dto.TodayScheduleResponse;
import com.certsniper.planner.entity.PersonalSchedule;
import com.certsniper.planner.entity.Planner;
import com.certsniper.planner.entity.PlannerBar;
import com.certsniper.planner.entity.PlannerMilestone;
import com.certsniper.planner.repository.PersonalScheduleRepository;
import com.certsniper.planner.repository.PlannerBarRepository;
import com.certsniper.planner.repository.PlannerMilestoneRepository;
import com.certsniper.planner.repository.PlannerRepository;
import com.certsniper.targetexam.dto.TargetExamResponse;
import com.certsniper.targetexam.repository.TargetExamRepository;
import com.certsniper.targetexam.service.TargetExamService;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private final TargetExamRepository targetExamRepository;
    private final ScheduleLimitValidator scheduleLimitValidator;

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

    /** 일정바 추가: 목표 자격증 또는 개인 일정 중 하나에만 속한다 (내 것만, 날짜 검사) */
    @Transactional
    public void createBar(Long userId, BarRequest request) {
        boolean forTargetExam = request.targetExamId() != null;
        if (forTargetExam == (request.personalScheduleId() != null)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "목표 자격증 또는 개인 일정 중 하나를 선택해 주세요.");
        }
        if (forTargetExam) {
            targetExamService.validateOwner(userId, request.targetExamId());
        } else {
            findOwnedPersonalSchedule(userId, request.personalScheduleId());
        }
        validateBarDates(request);
        plannerBarRepository.save(PlannerBar.create(userId, request.targetExamId(), request.personalScheduleId(),
                request.title().strip(), request.startDate(), request.endDate(), request.isSingleDay(), "MANUAL"));
    }

    /** 일정바 수정: 소속은 못 바꾼다. 기간을 줄이면 기간 밖 세부 일정은 같이 삭제 */
    @Transactional
    public void updateBar(Long userId, Long barId, BarRequest request) {
        PlannerBar bar = findOwnedBar(userId, barId);
        boolean otherTargetExam = request.targetExamId() != null && !request.targetExamId().equals(bar.getTargetExamId());
        boolean otherPersonal = request.personalScheduleId() != null
                && !request.personalScheduleId().equals(bar.getPersonalScheduleId());
        if (otherTargetExam || otherPersonal) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "일정바가 속한 자격증·개인 일정은 바꿀 수 없습니다.");
        }
        validateBarDates(request);
        // 일정바를 먼저 바꾸고 삭제한다 (삭제 쿼리가 바뀐 내용을 먼저 저장한 뒤 실행된다)
        bar.change(request.title().strip(), request.startDate(), request.endDate(), request.isSingleDay());
        plannerMilestoneRepository.deleteOutside(barId, request.startDate(), request.endDate());
    }

    /** 일정바 삭제: 세부 일정은 DB의 ON DELETE CASCADE로 같이 삭제 */
    @Transactional
    public void deleteBar(Long userId, Long barId) {
        plannerBarRepository.delete(findOwnedBar(userId, barId));
    }

    /** 세부 일정 추가: 날짜는 일정바 기간 안이어야 한다 */
    @Transactional
    public void createMilestone(Long userId, Long barId, MilestoneRequest request) {
        PlannerBar bar = findOwnedBar(userId, barId);
        validateInBar(bar, request.pointDate());
        plannerMilestoneRepository.save(PlannerMilestone.create(barId, userId, request.pointDate(),
                request.content().strip(), request.isRest(), "MANUAL"));
    }

    /** 세부 일정 수정: 날짜는 그 일정바 기간 안에서만 */
    @Transactional
    public void updateMilestone(Long userId, Long milestoneId, MilestoneRequest request) {
        PlannerMilestone milestone = findOwnedMilestone(userId, milestoneId);
        PlannerBar bar = findOwnedBar(userId, milestone.getBarId());
        validateInBar(bar, request.pointDate());
        milestone.change(request.pointDate(), request.content().strip(), request.isRest());
    }

    @Transactional
    public void deleteMilestone(Long userId, Long milestoneId) {
        plannerMilestoneRepository.delete(findOwnedMilestone(userId, milestoneId));
    }

    /** 내 일정바 찾기 (없으면 NOT_FOUND, 남의 것이면 FORBIDDEN) */
    private PlannerBar findOwnedBar(Long userId, Long barId) {
        PlannerBar bar = plannerBarRepository.findById(barId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "일정을 찾을 수 없습니다."));
        if (!bar.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return bar;
    }

    /** 내 세부 일정 찾기 (없으면 NOT_FOUND, 남의 것이면 FORBIDDEN) */
    private PlannerMilestone findOwnedMilestone(Long userId, Long milestoneId) {
        PlannerMilestone milestone = plannerMilestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "세부 일정을 찾을 수 없습니다."));
        if (!milestone.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return milestone;
    }

    private static void validateBarDates(BarRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "종료일이 시작일보다 빠릅니다.");
        }
        if (request.isSingleDay() && !request.startDate().equals(request.endDate())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "당일 일정은 시작일과 종료일이 같아야 합니다.");
        }
    }

    private static void validateInBar(PlannerBar bar, LocalDate date) {
        if (!bar.contains(date)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "세부 일정 날짜가 일정 기간 밖입니다.");
        }
    }

    /** 개인 일정 추가: 다른 일정과 같은 색 불가(409), 목표 자격증+개인 일정 6개 제한(409) */
    @Transactional
    public void createPersonalSchedule(Long userId, PersonalScheduleRequest request) {
        validateColorUnused(userId, request.displayColor(), null);
        scheduleLimitValidator.validateCanAdd(userId);
        personalScheduleRepository.save(
                PersonalSchedule.create(userId, request.name().strip(), request.displayColor()));
    }

    /** 개인 일정 이름·색 수정 (없음 404, 남의 것 403, 색 중복 409) */
    @Transactional
    public void updatePersonalSchedule(Long userId, Long scheduleId, PersonalScheduleRequest request) {
        PersonalSchedule schedule = findOwnedPersonalSchedule(userId, scheduleId);
        validateColorUnused(userId, request.displayColor(), scheduleId);
        schedule.change(request.name().strip(), request.displayColor());
    }

    /** 개인 일정 삭제: 그 일정의 일정바·세부 일정은 DB의 ON DELETE CASCADE로 같이 삭제 */
    @Transactional
    public void deletePersonalSchedule(Long userId, Long scheduleId) {
        personalScheduleRepository.delete(findOwnedPersonalSchedule(userId, scheduleId));
    }

    /** 내 개인 일정 찾기 (없으면 NOT_FOUND, 남의 것이면 FORBIDDEN) */
    private PersonalSchedule findOwnedPersonalSchedule(Long userId, Long scheduleId) {
        PersonalSchedule schedule = personalScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "개인 일정을 찾을 수 없습니다."));
        if (!schedule.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return schedule;
    }

    /** 색 중복 검사: 목표 자격증·다른 개인 일정에서 쓰는 색은 못 쓴다 (수정할 때는 자기 자신 제외) */
    private void validateColorUnused(Long userId, String color, Long exceptScheduleId) {
        boolean usedByTargetExam = targetExamRepository.existsByUserIdAndDisplayColor(userId, color);
        boolean usedByPersonal = exceptScheduleId == null
                ? personalScheduleRepository.existsByUserIdAndDisplayColor(userId, color)
                : personalScheduleRepository.existsByUserIdAndDisplayColorAndIdNot(userId, color, exceptScheduleId);
        if (usedByTargetExam || usedByPersonal) {
            throw new BusinessException(ErrorCode.DUPLICATE, "다른 일정에서 사용 중인 색입니다.");
        }
    }

    /** 그날의 일정: 그날에 걸친 일정바마다 그날 세부 일정 내용(없으면 일정바 제목)을 자격증·개인 일정별로 묶는다 */
    public TodayScheduleResponse getTodaySchedule(Long userId, LocalDate date) {
        List<PlannerBar> bars = plannerBarRepository.findOverlapping(userId, date, date);
        if (bars.isEmpty()) {
            return new TodayScheduleResponse(date, List.of());
        }
        Set<Long> barIds = bars.stream().map(PlannerBar::getId).collect(Collectors.toSet());
        Map<Long, List<String>> contentsByBar = plannerMilestoneRepository
                .findAllByBarIdInAndPointDateBetweenOrderByPointDateAscIdAsc(barIds, date, date).stream()
                .collect(Collectors.groupingBy(PlannerMilestone::getBarId,
                        Collectors.mapping(PlannerMilestone::getContent, Collectors.toList())));

        Map<Long, List<String>> itemsByTargetExam = new LinkedHashMap<>();
        Map<Long, List<String>> itemsByPersonal = new LinkedHashMap<>();
        for (PlannerBar bar : bars) {
            List<String> items = contentsByBar.getOrDefault(bar.getId(), List.of(bar.getTitle()));
            if (bar.getTargetExamId() != null) {
                itemsByTargetExam.computeIfAbsent(bar.getTargetExamId(), id -> new ArrayList<>()).addAll(items);
            } else {
                itemsByPersonal.computeIfAbsent(bar.getPersonalScheduleId(), id -> new ArrayList<>()).addAll(items);
            }
        }

        List<TodayScheduleResponse.Group> groups = new ArrayList<>();
        for (TargetExamResponse exam : targetExamService.getTargetExams(userId)) {
            List<String> items = itemsByTargetExam.get(exam.id());
            if (items != null) {
                groups.add(new TodayScheduleResponse.Group(exam.id(), null, exam.jmfldnm(), exam.displayColor(), items));
            }
        }
        for (PersonalSchedule schedule : personalScheduleRepository.findAllByUserIdOrderByIdAsc(userId)) {
            List<String> items = itemsByPersonal.get(schedule.getId());
            if (items != null) {
                groups.add(new TodayScheduleResponse.Group(null, schedule.getId(), schedule.getName(),
                        schedule.getDisplayColor(), items));
            }
        }
        return new TodayScheduleResponse(date, groups);
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
