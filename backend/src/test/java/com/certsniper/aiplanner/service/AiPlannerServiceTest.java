package com.certsniper.aiplanner.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.certsniper.aiplanner.dto.AiDraft;
import com.certsniper.aiplanner.dto.AiPlanResponse;
import com.certsniper.aiplanner.dto.SurveyRequest;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.global.util.DateUtils;
import com.certsniper.planner.service.PlannerService;
import com.certsniper.qualification.dto.QualificationDetailResponse;
import com.certsniper.qualification.service.QualificationService;
import com.certsniper.targetexam.service.TargetExamService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

class AiPlannerServiceTest {

    private final QualificationService qualificationService = mock(QualificationService.class);
    private final TargetExamService targetExamService = mock(TargetExamService.class);
    private final PlannerService plannerService = mock(PlannerService.class);
    private final QwenClient qwenClient = mock(QwenClient.class);
    private final TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
    private final AiPlannerService service = new AiPlannerService(qualificationService, targetExamService,
            plannerService, qwenClient, new AiPlanValidator(), new DailyPointPlanner(), transactionTemplate);

    /** 내일부터 10일 공부, 매일 공부 가능 */
    private static final LocalDate START = DateUtils.today().plusDays(1);
    private static final LocalDate EXAM = START.plusDays(10);
    private static final List<DayOfWeek> EVERY_DAY = Arrays.asList(DayOfWeek.values());
    private final SurveyRequest request =
            new SurveyRequest("QNET:2290", 7L, EXAM, START, "처음 공부", 2, EVERY_DAY, 60);

    @BeforeEach
    void setUp() {
        QualificationDetailResponse.QualificationInfo info = mock(QualificationDetailResponse.QualificationInfo.class);
        when(info.jmcd()).thenReturn("2290");
        when(info.jmfldnm()).thenReturn("SQL개발자");
        when(qualificationService.getInfo(any())).thenReturn(info);
        when(qwenClient.requestDraft(any())).thenReturn(new AiDraft(List.of(
                new AiDraft.Bar("이론", 6, List.of("엔터티", "속성")),
                new AiDraft.Bar("기출", 4, List.of()))));
        when(targetExamService.getOrCreateTargetExam(1L, "2290", 7L, EXAM)).thenReturn(5L);
        // 트랜잭션은 테스트에서 그냥 바로 실행
        doAnswer(invocation -> {
            invocation.<Consumer<TransactionStatus>>getArgument(0).accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    @Test
    void AI_초안으로_플랜을_만들어_목표_자격증과_함께_저장() {
        AiPlanResponse plan = service.createAiPlan(1L, request);

        assertEquals("2290", plan.meta().jmcd());
        assertEquals(10, plan.meta().totalDays());
        assertEquals(2, plan.timelineBars().size());
        verify(targetExamService).validateBeforeAiPlan(1L, "2290", 7L, EXAM);
        verify(plannerService).replaceAiSchedule(1L, 5L, plan);
    }

    @Test
    void 설문_날짜가_틀리면_AI를_부르지_않는다() {
        SurveyRequest sameDay = new SurveyRequest("2290", 7L, START, START, "처음 공부", 2, EVERY_DAY, 60);

        assertThrows(BusinessException.class, () -> service.createAiPlan(1L, sameDay));
        verify(qwenClient, never()).requestDraft(any());
    }

    @Test
    void 회차나_6개_제한에_걸리면_AI를_부르지_않는다() {
        doThrow(new BusinessException(ErrorCode.LIMIT_EXCEEDED))
                .when(targetExamService).validateBeforeAiPlan(any(), any(), any(), any());

        assertThrows(BusinessException.class, () -> service.createAiPlan(1L, request));
        verify(qwenClient, never()).requestDraft(any());
    }

    @Test
    void AI가_실패하면_아무것도_저장하지_않는다() {
        when(qwenClient.requestDraft(any()))
                .thenThrow(new BusinessException(ErrorCode.INTERNAL_ERROR, "AI 서버에 연결하지 못했습니다."));

        assertThrows(BusinessException.class, () -> service.createAiPlan(1L, request));
        verify(targetExamService, never()).getOrCreateTargetExam(any(), any(), any(), any());
        verify(plannerService, never()).replaceAiSchedule(any(), any(), any());
    }

    @Test
    void 만드는_중에_같은_회원이_또_요청하면_429_끝나면_다시_가능() {
        // AI가 답하는 도중에 같은 회원이 한 번 더 누른 상황
        when(qwenClient.requestDraft(any())).thenAnswer(invocation -> {
            BusinessException e = assertThrows(BusinessException.class, () -> service.createAiPlan(1L, request));
            assertEquals(ErrorCode.TOO_MANY_REQUESTS, e.getErrorCode());
            return new AiDraft(List.of(new AiDraft.Bar("이론", 1, List.of("a"))));
        });
        service.createAiPlan(1L, request);

        // 끝난 뒤에는 다시 요청할 수 있다
        assertDoesNotThrow(() -> service.createAiPlan(1L, request));
    }
}
