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
import com.certsniper.planner.dto.PlannerResponse;
import com.certsniper.planner.entity.PersonalSchedule;
import com.certsniper.planner.entity.Planner;
import com.certsniper.planner.entity.PlannerBar;
import com.certsniper.planner.entity.PlannerMilestone;
import com.certsniper.planner.repository.PersonalScheduleRepository;
import com.certsniper.planner.repository.PlannerBarRepository;
import com.certsniper.planner.repository.PlannerMilestoneRepository;
import com.certsniper.planner.repository.PlannerRepository;
import com.certsniper.planner.service.PlannerService;
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
    private final PlannerService service =
            new PlannerService(plannerRepo, barRepo, milestoneRepo, personalRepo, targetExamService);

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
}
