package com.certsniper.planner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.certsniper.planner.service.PlannerService;
import com.certsniper.planner.service.ScheduleLimitValidator;
import com.certsniper.targetexam.dto.TargetExamResponse;
import com.certsniper.targetexam.repository.TargetExamRepository;
import com.certsniper.targetexam.service.TargetExamService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;

class PlannerServiceTest {

    private final PlannerRepository plannerRepo = mock(PlannerRepository.class);
    private final PlannerBarRepository barRepo = mock(PlannerBarRepository.class);
    private final PlannerMilestoneRepository milestoneRepo = mock(PlannerMilestoneRepository.class);
    private final PersonalScheduleRepository personalRepo = mock(PersonalScheduleRepository.class);
    private final TargetExamService targetExamService = mock(TargetExamService.class);
    private final TargetExamRepository targetExamRepo = mock(TargetExamRepository.class);
    private final ScheduleLimitValidator limitValidator = mock(ScheduleLimitValidator.class);
    private final PlannerService service = new PlannerService(plannerRepo, barRepo, milestoneRepo, personalRepo,
            targetExamService, targetExamRepo, limitValidator);
    private static final LocalDate D1 = LocalDate.of(2026, 12, 1);

    /** 저장하면 ID가 1, 2, 3… 순서로 붙는 것처럼 흉내 낸다 */
    @BeforeEach
    void setUp() {
        AtomicLong seq = new AtomicLong(1);
        when(barRepo.saveAll(anyList())).thenAnswer(invocation -> {
            List<PlannerBar> bars = invocation.getArgument(0);
            bars.forEach(b -> ReflectionTestUtils.setField(b, "id", seq.getAndIncrement()));
            return bars;
        });
    }

    private static PlannerBar bar(Long id, LocalDate start, LocalDate end) {
        PlannerBar b = PlannerBar.create(1L, 10L, null, "이론", start, end, false, "AI");
        ReflectionTestUtils.setField(b, "id", id);
        return b;
    }

    // ===== 조회 =====

    @Test
    void 조회_시작일이_종료일보다_늦으면_400() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> service.getPlanner(1L, D1.plusDays(1), D1));
        assertEquals(ErrorCode.VALIDATION_ERROR, e.getErrorCode());
    }

    @Test
    void 조회_기간이_1년을_넘으면_400() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> service.getPlanner(1L, D1, D1.plusDays(400)));
        assertEquals(ErrorCode.VALIDATION_ERROR, e.getErrorCode());
    }

    @Test
    void 조회하면_일정바마다_세부_일정과_개인_일정이_담긴다() {
        when(plannerRepo.findById(1L)).thenReturn(Optional.empty());
        when(barRepo.findOverlapping(1L, D1, D1.plusDays(30)))
                .thenReturn(List.of(bar(1L, D1, D1.plusDays(9)), bar(2L, D1.plusDays(10), D1.plusDays(20))));
        when(milestoneRepo.findAllByBarIdInAndPointDateBetweenOrderByPointDateAscIdAsc(any(), any(), any()))
                .thenReturn(List.of(PlannerMilestone.create(1L, 1L, D1.plusDays(1), "1장", false, "AI")));
        PersonalSchedule personal = mock(PersonalSchedule.class);
        when(personal.getId()).thenReturn(5L);
        when(personal.getName()).thenReturn("알바");
        when(personal.getDisplayColor()).thenReturn("#0091FF");
        when(personalRepo.findAllByUserIdOrderByIdAsc(1L)).thenReturn(List.of(personal));

        PlannerResponse res = service.getPlanner(1L, D1, D1.plusDays(30));

        assertEquals("MANUAL", res.mode());
        assertEquals(2, res.bars().size());
        assertEquals(1, res.bars().get(0).milestones().size());
        assertEquals("1장", res.bars().get(0).milestones().get(0).content());
        assertTrue(res.bars().get(1).milestones().isEmpty());
        assertEquals("알바", res.personalSchedules().get(0).name());
    }

    // ===== 모드 =====

    @Test
    void 없는_모드면_400() {
        BusinessException e = assertThrows(BusinessException.class, () -> service.changeMode(1L, "EDIT"));
        assertEquals(ErrorCode.VALIDATION_ERROR, e.getErrorCode());
    }

    @Test
    void 플래너가_없으면_만들고_있으면_모드만_바꾼다() {
        when(plannerRepo.findById(1L)).thenReturn(Optional.empty());
        service.changeMode(1L, "AI_RENDER");
        ArgumentCaptor<Planner> saved = ArgumentCaptor.forClass(Planner.class);
        verify(plannerRepo).save(saved.capture());
        assertEquals("AI_RENDER", saved.getValue().getMode());

        Planner existing = Planner.create(2L, "AI_RENDER");
        when(plannerRepo.findById(2L)).thenReturn(Optional.of(existing));
        service.changeMode(2L, "MANUAL");
        assertEquals("MANUAL", existing.getMode());
    }

    // ===== AI 일정 교체 =====

    private static AiPlanResponse plan(List<AiPlanResponse.DailyPoint> points) {
        return new AiPlanResponse(
                new AiPlanResponse.Meta("2290", D1, D1.plusDays(19), 20),
                List.of(new AiPlanResponse.TimelineBar("이론", D1, D1.plusDays(9)),
                        new AiPlanResponse.TimelineBar("문제 풀이", D1.plusDays(10), D1.plusDays(19))),
                points);
    }

    @SuppressWarnings("unchecked")
    @Test
    void AI_일정_교체하면_기존_AI_일정을_지우고_새로_저장한_뒤_AI_RENDER() {
        when(plannerRepo.findById(1L)).thenReturn(Optional.empty());

        service.replaceAiSchedule(1L, 10L, plan(List.of(
                new AiPlanResponse.DailyPoint(D1, "1장 데이터 모델링", false),
                new AiPlanResponse.DailyPoint(D1.plusDays(12), "", true))));

        verify(targetExamService).validateOwner(1L, 10L);
        verify(barRepo).deleteAiBarsByTargetExamId(10L);

        ArgumentCaptor<List<PlannerMilestone>> saved = ArgumentCaptor.forClass(List.class);
        verify(milestoneRepo).saveAll(saved.capture());
        List<PlannerMilestone> milestones = saved.getValue();
        assertEquals(1L, milestones.get(0).getBarId());
        assertEquals("1장 데이터 모델링", milestones.get(0).getContent());
        assertEquals(2L, milestones.get(1).getBarId());
        assertEquals("휴식", milestones.get(1).getContent());

        ArgumentCaptor<Planner> planner = ArgumentCaptor.forClass(Planner.class);
        verify(plannerRepo).save(planner.capture());
        assertEquals("AI_RENDER", planner.getValue().getMode());
    }

    @Test
    void AI_일일_항목이_일정바_기간_밖이면_400() {
        BusinessException e = assertThrows(BusinessException.class, () -> service.replaceAiSchedule(1L, 10L,
                plan(List.of(new AiPlanResponse.DailyPoint(D1.plusDays(30), "범위 밖", false)))));
        assertEquals(ErrorCode.VALIDATION_ERROR, e.getErrorCode());
    }

    @Test
    void AI_일정바가_없으면_400이고_기존_일정은_안_지운다() {
        AiPlanResponse empty = new AiPlanResponse(null, List.of(), List.of());
        BusinessException e = assertThrows(BusinessException.class,
                () -> service.replaceAiSchedule(1L, 10L, empty));
        assertEquals(ErrorCode.VALIDATION_ERROR, e.getErrorCode());
        verify(barRepo, never()).deleteAiBarsByTargetExamId(any());
    }

    @Test
    void 남의_목표_자격증이면_AI_일정_교체_안_함() {
        doThrow(new BusinessException(ErrorCode.FORBIDDEN)).when(targetExamService).validateOwner(1L, 10L);
        assertThrows(BusinessException.class, () -> service.replaceAiSchedule(1L, 10L, plan(List.of())));
        verify(barRepo, never()).deleteAiBarsByTargetExamId(any());
    }

    @Test
    void 일정명이_100자를_넘으면_잘라서_저장() {
        when(plannerRepo.findById(1L)).thenReturn(Optional.empty());
        String longTitle = "가".repeat(150);
        AiPlanResponse p = new AiPlanResponse(null,
                List.of(new AiPlanResponse.TimelineBar(longTitle, D1, D1.plusDays(1))), List.of());
        service.replaceAiSchedule(1L, 10L, p);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PlannerBar>> saved = ArgumentCaptor.forClass(List.class);
        verify(barRepo).saveAll(saved.capture());
        assertEquals(100, saved.getValue().get(0).getTitle().length());
        assertEquals("AI", saved.getValue().get(0).getSource());
    }

    // ===== 탈퇴 =====

    @Test
    void 탈퇴하면_일정바_개인일정_플래너_순서로_삭제() {
        service.deleteAllByUser(1L);

        InOrder order = inOrder(barRepo, personalRepo, plannerRepo);
        order.verify(barRepo).deleteAllByUserId(1L);
        order.verify(personalRepo).deleteAllByUserId(1L);
        order.verify(plannerRepo).deleteAllByUserId(1L);
    }

    // ===== 개인 일정 =====

    private PersonalSchedule storedPersonal(Long id, Long ownerId) {
        PersonalSchedule schedule = PersonalSchedule.create(ownerId, "알바", "#0091FF");
        ReflectionTestUtils.setField(schedule, "id", id);
        when(personalRepo.findById(id)).thenReturn(Optional.of(schedule));
        return schedule;
    }

    @Test
    void 개인_일정_추가하면_6개_제한을_검사하고_저장() {
        service.createPersonalSchedule(1L, new PersonalScheduleRequest("  알바  ", "#0091FF"));

        verify(limitValidator).validateCanAdd(1L);
        ArgumentCaptor<PersonalSchedule> saved = ArgumentCaptor.forClass(PersonalSchedule.class);
        verify(personalRepo).save(saved.capture());
        assertEquals("알바", saved.getValue().getName());
        assertEquals("#0091FF", saved.getValue().getDisplayColor());
    }

    @Test
    void 목표_자격증이나_다른_개인_일정과_같은_색이면_DUPLICATE() {
        when(targetExamRepo.existsByUserIdAndDisplayColor(1L, "#E5484D")).thenReturn(true);
        BusinessException e = assertThrows(BusinessException.class,
                () -> service.createPersonalSchedule(1L, new PersonalScheduleRequest("알바", "#E5484D")));
        assertEquals(ErrorCode.DUPLICATE, e.getErrorCode());

        when(personalRepo.existsByUserIdAndDisplayColor(1L, "#30A46C")).thenReturn(true);
        assertThrows(BusinessException.class,
                () -> service.createPersonalSchedule(1L, new PersonalScheduleRequest("운동", "#30A46C")));
        verify(personalRepo, never()).save(any());
    }

    @Test
    void 일정이_6개면_개인_일정_추가_안_됨() {
        doThrow(new BusinessException(ErrorCode.LIMIT_EXCEEDED)).when(limitValidator).validateCanAdd(1L);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.createPersonalSchedule(1L, new PersonalScheduleRequest("알바", "#0091FF")));
        assertEquals(ErrorCode.LIMIT_EXCEEDED, e.getErrorCode());
        verify(personalRepo, never()).save(any());
    }

    @Test
    void 개인_일정_수정은_자기_색을_빼고_중복_검사() {
        PersonalSchedule schedule = storedPersonal(5L, 1L);

        service.updatePersonalSchedule(1L, 5L, new PersonalScheduleRequest("과외", "#0091FF"));

        verify(personalRepo).existsByUserIdAndDisplayColorAndIdNot(1L, "#0091FF", 5L);
        assertEquals("과외", schedule.getName());
    }

    @Test
    void 남의_개인_일정은_수정_삭제_FORBIDDEN() {
        storedPersonal(5L, 2L);

        BusinessException update = assertThrows(BusinessException.class,
                () -> service.updatePersonalSchedule(1L, 5L, new PersonalScheduleRequest("과외", "#0091FF")));
        BusinessException delete = assertThrows(BusinessException.class,
                () -> service.deletePersonalSchedule(1L, 5L));
        assertEquals(ErrorCode.FORBIDDEN, update.getErrorCode());
        assertEquals(ErrorCode.FORBIDDEN, delete.getErrorCode());
        verify(personalRepo, never()).delete(any());
    }

    @Test
    void 개인_일정_삭제() {
        PersonalSchedule schedule = storedPersonal(5L, 1L);
        service.deletePersonalSchedule(1L, 5L);
        verify(personalRepo).delete(schedule);
    }

    // ===== 일정바 =====

    private PlannerBar storedBar(Long id, Long ownerId) {
        PlannerBar bar = PlannerBar.create(ownerId, 10L, null, "이론", D1, D1.plusDays(9), false, "MANUAL");
        ReflectionTestUtils.setField(bar, "id", id);
        when(barRepo.findById(id)).thenReturn(Optional.of(bar));
        return bar;
    }

    @Test
    void 목표_자격증에_일정바_추가() {
        service.createBar(1L, new BarRequest(10L, null, "  1과목 이론  ", D1, D1.plusDays(4), false));

        verify(targetExamService).validateOwner(1L, 10L);
        ArgumentCaptor<PlannerBar> saved = ArgumentCaptor.forClass(PlannerBar.class);
        verify(barRepo).save(saved.capture());
        assertEquals("1과목 이론", saved.getValue().getTitle());
        assertEquals("MANUAL", saved.getValue().getSource());
    }

    @Test
    void 일정바는_목표_자격증과_개인_일정_중_하나에만_속한다() {
        assertThrows(BusinessException.class,
                () -> service.createBar(1L, new BarRequest(null, null, "이론", D1, D1, false)));
        assertThrows(BusinessException.class,
                () -> service.createBar(1L, new BarRequest(10L, 5L, "이론", D1, D1, false)));
        verify(barRepo, never()).save(any());
    }

    @Test
    void 남의_개인_일정에는_일정바_추가_안_됨() {
        storedPersonal(5L, 2L);
        BusinessException e = assertThrows(BusinessException.class,
                () -> service.createBar(1L, new BarRequest(null, 5L, "알바", D1, D1, true)));
        assertEquals(ErrorCode.FORBIDDEN, e.getErrorCode());
    }

    @Test
    void 날짜가_거꾸로거나_당일인데_날짜가_다르면_400() {
        BusinessException reversed = assertThrows(BusinessException.class,
                () -> service.createBar(1L, new BarRequest(10L, null, "이론", D1.plusDays(3), D1, false)));
        BusinessException single = assertThrows(BusinessException.class,
                () -> service.createBar(1L, new BarRequest(10L, null, "시험", D1, D1.plusDays(1), true)));
        assertEquals(ErrorCode.VALIDATION_ERROR, reversed.getErrorCode());
        assertEquals(ErrorCode.VALIDATION_ERROR, single.getErrorCode());
    }

    @Test
    void 일정바_기간을_줄이면_밖의_세부_일정을_지운다() {
        PlannerBar bar = storedBar(1L, 1L);

        service.updateBar(1L, 1L, new BarRequest(10L, null, "이론 (단축)", D1, D1.plusDays(4), false));

        assertEquals(D1.plusDays(4), bar.getEndDate());
        assertEquals("이론 (단축)", bar.getTitle());
        verify(milestoneRepo).deleteOutside(1L, D1, D1.plusDays(4));
    }

    @Test
    void 일정바의_소속은_바꿀_수_없다() {
        storedBar(1L, 1L);
        BusinessException e = assertThrows(BusinessException.class,
                () -> service.updateBar(1L, 1L, new BarRequest(11L, null, "이론", D1, D1, false)));
        assertEquals(ErrorCode.VALIDATION_ERROR, e.getErrorCode());
    }

    @Test
    void 남의_일정바는_수정_삭제_FORBIDDEN() {
        storedBar(1L, 2L);
        BusinessException update = assertThrows(BusinessException.class,
                () -> service.updateBar(1L, 1L, new BarRequest(10L, null, "이론", D1, D1, false)));
        BusinessException delete = assertThrows(BusinessException.class, () -> service.deleteBar(1L, 1L));
        assertEquals(ErrorCode.FORBIDDEN, update.getErrorCode());
        assertEquals(ErrorCode.FORBIDDEN, delete.getErrorCode());
        verify(barRepo, never()).delete(any());
    }

    // ===== 세부 일정 =====

    @Test
    void 세부_일정은_일정바_기간_안에만_추가() {
        storedBar(1L, 1L); // 12/01 ~ 12/10

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.createMilestone(1L, 1L, new MilestoneRequest(D1.plusDays(20), "기출", false)));
        assertEquals(ErrorCode.VALIDATION_ERROR, e.getErrorCode());

        service.createMilestone(1L, 1L, new MilestoneRequest(D1.plusDays(2), " 기출 1회 ", false));
        ArgumentCaptor<PlannerMilestone> saved = ArgumentCaptor.forClass(PlannerMilestone.class);
        verify(milestoneRepo).save(saved.capture());
        assertEquals("기출 1회", saved.getValue().getContent());
        assertEquals("MANUAL", saved.getValue().getSource());
    }

    @Test
    void 세부_일정_수정() {
        storedBar(1L, 1L);
        PlannerMilestone milestone = PlannerMilestone.create(1L, 1L, D1.plusDays(1), "1장", false, "AI");
        ReflectionTestUtils.setField(milestone, "id", 7L);
        when(milestoneRepo.findById(7L)).thenReturn(Optional.of(milestone));

        service.updateMilestone(1L, 7L, new MilestoneRequest(D1.plusDays(3), "휴식", true));

        assertEquals(D1.plusDays(3), milestone.getPointDate());
        assertEquals(true, milestone.getIsRest());
    }

    @Test
    void 남의_세부_일정은_수정_삭제_FORBIDDEN() {
        PlannerMilestone milestone = PlannerMilestone.create(1L, 2L, D1, "1장", false, "MANUAL");
        when(milestoneRepo.findById(7L)).thenReturn(Optional.of(milestone));

        BusinessException update = assertThrows(BusinessException.class,
                () -> service.updateMilestone(1L, 7L, new MilestoneRequest(D1, "2장", false)));
        BusinessException delete = assertThrows(BusinessException.class, () -> service.deleteMilestone(1L, 7L));
        assertEquals(ErrorCode.FORBIDDEN, update.getErrorCode());
        assertEquals(ErrorCode.FORBIDDEN, delete.getErrorCode());
        verify(milestoneRepo, never()).delete(any());
    }

    // ===== 그날의 일정 =====

    @Test
    void 그날_일정은_자격증과_개인_일정별로_묶인다() {
        PlannerBar study = bar(1L, D1, D1.plusDays(5)); // 목표 자격증 10번, 그날 세부 일정 있음
        PlannerBar work = PlannerBar.create(1L, null, 5L, "저녁 알바", D1, D1, true, "MANUAL"); // 세부 일정 없음
        ReflectionTestUtils.setField(work, "id", 2L);
        when(barRepo.findOverlapping(1L, D1, D1)).thenReturn(List.of(study, work));
        when(milestoneRepo.findAllByBarIdInAndPointDateBetweenOrderByPointDateAscIdAsc(any(), any(), any()))
                .thenReturn(List.of(PlannerMilestone.create(1L, 1L, D1, "1장 데이터 모델링", false, "AI")));
        when(targetExamService.getTargetExams(1L)).thenReturn(List.of(
                new TargetExamResponse(10L, "1320", "정보처리기사", 7L, D1.plusDays(30), "D-30", "#E5484D")));
        PersonalSchedule personal = PersonalSchedule.create(1L, "알바", "#0091FF");
        ReflectionTestUtils.setField(personal, "id", 5L);
        when(personalRepo.findAllByUserIdOrderByIdAsc(1L)).thenReturn(List.of(personal));

        TodayScheduleResponse res = service.getTodaySchedule(1L, D1);

        assertEquals(2, res.groups().size());
        assertEquals("정보처리기사", res.groups().get(0).name());
        assertEquals(List.of("1장 데이터 모델링"), res.groups().get(0).items());
        assertEquals("알바", res.groups().get(1).name());
        assertEquals(List.of("저녁 알바"), res.groups().get(1).items());
    }

    @Test
    void 그날_일정이_없으면_빈_목록() {
        when(barRepo.findOverlapping(1L, D1, D1)).thenReturn(List.of());

        TodayScheduleResponse res = service.getTodaySchedule(1L, D1);

        assertTrue(res.groups().isEmpty());
        verify(targetExamService, never()).getTargetExams(any());
    }
}
