package com.certsniper.aiplanner.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.certsniper.aiplanner.dto.AiDraft;
import com.certsniper.aiplanner.dto.AiPlanResponse;
import com.certsniper.aiplanner.dto.SurveyRequest;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class DailyPointPlannerTest {

    private final DailyPointPlanner planner = new DailyPointPlanner();

    /** 2026-12-01은 화요일. 시험일 12/21 → 공부 기간은 12/01 ~ 12/20 (20일) */
    private static final LocalDate START = LocalDate.of(2026, 12, 1);
    private static final LocalDate EXAM = LocalDate.of(2026, 12, 21);
    private static final List<DayOfWeek> MON_WED_FRI_SAT =
            List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);

    private static SurveyRequest survey(LocalDate start, LocalDate exam, List<DayOfWeek> studyDays) {
        return new SurveyRequest("2290", 7L, exam, start, "처음 공부", 2, studyDays, 60);
    }

    /** 이론 8일 · SQL 8일 · 기출 4일 (기출은 주제 없음) */
    private AiPlanResponse samplePlan() {
        AiDraft draft = new AiDraft(List.of(
                new AiDraft.Bar("1과목 이론", 8, List.of("엔터티", "속성", "관계")),
                new AiDraft.Bar("2과목 SQL", 8, List.of("SELECT", "조인")),
                new AiDraft.Bar("기출 풀이", 4, List.of())));
        return planner.expand("2290", survey(START, EXAM, MON_WED_FRI_SAT), draft);
    }

    private static AiPlanResponse.DailyPoint pointOn(AiPlanResponse plan, LocalDate date) {
        return plan.dailyPoints().stream().filter(p -> p.pointDate().equals(date)).findFirst().orElseThrow();
    }

    @Test
    void 일정바가_시작일부터_시험_전날까지_빈틈없이_이어진다() {
        AiPlanResponse plan = samplePlan();

        assertEquals(20, plan.meta().totalDays());
        assertEquals(20, plan.dailyPoints().size());
        List<AiPlanResponse.TimelineBar> bars = plan.timelineBars();
        assertEquals(LocalDate.of(2026, 12, 1), bars.get(0).startDate());
        assertEquals(LocalDate.of(2026, 12, 8), bars.get(0).endDate());
        assertEquals(LocalDate.of(2026, 12, 9), bars.get(1).startDate());
        assertEquals(LocalDate.of(2026, 12, 16), bars.get(1).endDate());
        assertEquals(LocalDate.of(2026, 12, 17), bars.get(2).startDate());
        assertEquals(LocalDate.of(2026, 12, 20), bars.get(2).endDate());
    }

    @Test
    void 가능_요일이_아니면_휴식() {
        AiPlanResponse plan = samplePlan();

        AiPlanResponse.DailyPoint tuesday = pointOn(plan, LocalDate.of(2026, 12, 1));
        assertTrue(tuesday.isRest());
        assertEquals("휴식", tuesday.content());

        AiPlanResponse.DailyPoint wednesday = pointOn(plan, LocalDate.of(2026, 12, 2));
        assertFalse(wednesday.isRest());
        assertEquals("엔터티", wednesday.content());
    }

    @Test
    void 주제를_다_쓰면_복습으로_반복() {
        AiPlanResponse plan = samplePlan();
        // 이론 일정바의 공부일: 12/02 엔터티, 12/04 속성, 12/05 관계, 12/07 엔터티 복습
        assertEquals("관계", pointOn(plan, LocalDate.of(2026, 12, 5)).content());
        assertEquals("엔터티 복습", pointOn(plan, LocalDate.of(2026, 12, 7)).content());
    }

    @Test
    void 주제가_없으면_일정바_제목으로() {
        AiPlanResponse plan = samplePlan();
        assertEquals("기출 풀이", pointOn(plan, LocalDate.of(2026, 12, 18)).content());
        assertEquals("기출 풀이 복습", pointOn(plan, LocalDate.of(2026, 12, 19)).content());
    }

    @Test
    void 일정바가_기간보다_많으면_기간만큼만_하루씩() {
        List<AiDraft.Bar> five = List.of(
                new AiDraft.Bar("a", 1, List.of()), new AiDraft.Bar("b", 1, List.of()),
                new AiDraft.Bar("c", 1, List.of()), new AiDraft.Bar("d", 1, List.of()),
                new AiDraft.Bar("e", 1, List.of()));
        AiPlanResponse plan = planner.expand("2290",
                survey(START, START.plusDays(3), List.of(DayOfWeek.values())), new AiDraft(five));

        assertEquals(3, plan.timelineBars().size());
        assertEquals(START.plusDays(2), plan.timelineBars().get(2).endDate());
    }

    @Test
    void 일수_비율대로_나누고_합계는_정확히_맞춘다() {
        List<AiDraft.Bar> bars = List.of(new AiDraft.Bar("a", 3, List.of()), new AiDraft.Bar("b", 1, List.of()));
        assertArrayEquals(new int[] {15, 5}, DailyPointPlanner.splitDays(bars, 20));

        // AI가 0이나 음수를 줘도 최소 1일
        List<AiDraft.Bar> odd = List.of(new AiDraft.Bar("a", 0, List.of()), new AiDraft.Bar("b", -5, List.of()));
        assertArrayEquals(new int[] {1, 1}, DailyPointPlanner.splitDays(odd, 2));
    }
}
