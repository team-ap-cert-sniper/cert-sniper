package com.certsniper.targetexam;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.certsniper.examschedule.dto.ExamScheduleResponse;
import com.certsniper.examschedule.service.DdayCalculator;
import com.certsniper.examschedule.service.ExamScheduleService;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.global.util.DateUtils;
import com.certsniper.planner.service.ScheduleLimitValidator;
import com.certsniper.qualification.dto.QualificationDetailResponse;
import com.certsniper.qualification.service.QualificationService;
import com.certsniper.targetexam.dto.TargetExamRequest;
import com.certsniper.targetexam.dto.TargetExamResponse;
import com.certsniper.targetexam.entity.TargetExam;
import com.certsniper.targetexam.repository.TargetExamRepository;
import com.certsniper.targetexam.service.TargetExamService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class TargetExamServiceTest {

    private final TargetExamRepository repo = mock(TargetExamRepository.class);
    private final QualificationService qualificationService = mock(QualificationService.class);
    private final ExamScheduleService examScheduleService = mock(ExamScheduleService.class);
    private final DdayCalculator ddayCalculator = mock(DdayCalculator.class);
    private final ScheduleLimitValidator limitValidator = mock(ScheduleLimitValidator.class);
    private final TargetExamService service = new TargetExamService(
            repo, qualificationService, examScheduleService, ddayCalculator, limitValidator);

    /** 오늘 기준 30일 뒤 시험 (날짜가 지나도 테스트가 깨지지 않게) */
    private static final LocalDate EXAM_DATE = DateUtils.today().plusDays(30);

    /** 테스트용 시험 회차: 필요한 값(ID, 시험 시작일, 종료일)만 채운다 */
    private static ExamScheduleResponse schedule(Long id, LocalDate start, LocalDate end) {
        return new ExamScheduleResponse(id, "1회", "필기", null, null, null, null,
                start, end, null, "UPCOMING", "OPEN", "D-30");
    }

    @BeforeEach
    void setUp() {
        QualificationDetailResponse.QualificationInfo info = mock(QualificationDetailResponse.QualificationInfo.class);
        when(info.jmcd()).thenReturn("2290");
        when(info.jmfldnm()).thenReturn("SQL개발자");
        when(qualificationService.getInfo(any())).thenReturn(info);
        when(examScheduleService.getSchedules("2290")).thenReturn(List.of(schedule(7L, EXAM_DATE, EXAM_DATE)));
        when(ddayCalculator.label(any(), any())).thenReturn("D-30");
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void stored(Long targetExamId, Long ownerId) {
        TargetExam t = mock(TargetExam.class);
        when(t.getUserId()).thenReturn(ownerId);
        when(repo.findById(targetExamId)).thenReturn(Optional.of(t));
    }

    private BusinessException addFails(TargetExamRequest request) {
        BusinessException e = assertThrows(BusinessException.class, () -> service.addTargetExam(1L, request));
        verify(repo, never()).save(any());
        return e;
    }

    // ===== 소유자 확인, 개수, 전체 삭제 =====

    @Test
    void 내_목표_자격증이면_통과() {
        stored(10L, 1L);
        assertDoesNotThrow(() -> service.validateOwner(1L, 10L));
    }

    @Test
    void 다른_회원_목표_자격증이면_FORBIDDEN() {
        stored(10L, 2L);
        BusinessException e = assertThrows(BusinessException.class, () -> service.validateOwner(1L, 10L));
        assertEquals(ErrorCode.FORBIDDEN, e.getErrorCode());
    }

    @Test
    void 없는_목표_자격증이면_NOT_FOUND() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        BusinessException e = assertThrows(BusinessException.class, () -> service.validateOwner(1L, 99L));
        assertEquals(ErrorCode.NOT_FOUND, e.getErrorCode());
    }

    @Test
    void 개수와_전체_삭제는_리포지토리에_맡김() {
        when(repo.countByUserId(1L)).thenReturn(3L);
        assertEquals(3L, service.countByUser(1L));
        service.deleteAllByUser(1L);
        verify(repo).deleteAllByUserId(1L);
    }

    // ===== 직접 추가 =====

    @Test
    void 목표_자격증_추가하면_이름과_DDAY가_들어간_응답() {
        TargetExamResponse res = service.addTargetExam(1L, new TargetExamRequest("2290", 7L, EXAM_DATE, "#E5484D"));
        assertEquals("SQL개발자", res.jmfldnm());
        assertEquals("D-30", res.ddayLabel());
        assertEquals("#E5484D", res.displayColor());
        verify(limitValidator).validateCanAdd(1L);
    }

    @Test
    void 종목코드는_정리된_4자리로_저장() {
        service.addTargetExam(1L, new TargetExamRequest("QNET:2290", 7L, EXAM_DATE, "#E5484D"));
        ArgumentCaptor<TargetExam> saved = ArgumentCaptor.forClass(TargetExam.class);
        verify(repo).save(saved.capture());
        assertEquals("2290", saved.getValue().getJmcd());
    }

    @Test
    void 회차와_시험일_둘_다_비우면_통과() {
        service.addTargetExam(1L, new TargetExamRequest("2290", null, null, "#E5484D"));
        verify(repo).save(any());
    }

    @Test
    void 회차나_시험일_하나만_있으면_400() {
        assertEquals(ErrorCode.VALIDATION_ERROR,
                addFails(new TargetExamRequest("2290", 7L, null, "#E5484D")).getErrorCode());
        assertEquals(ErrorCode.VALIDATION_ERROR,
                addFails(new TargetExamRequest("2290", null, EXAM_DATE, "#E5484D")).getErrorCode());
    }

    @Test
    void 다른_자격증의_회차면_400() {
        assertEquals(ErrorCode.VALIDATION_ERROR,
                addFails(new TargetExamRequest("2290", 999L, EXAM_DATE, "#E5484D")).getErrorCode());
    }

    @Test
    void 시험일이_회차_기간_밖이면_400() {
        assertEquals(ErrorCode.VALIDATION_ERROR,
                addFails(new TargetExamRequest("2290", 7L, EXAM_DATE.plusDays(1), "#E5484D")).getErrorCode());
    }

    @Test
    void 이미_지난_시험이면_400() {
        LocalDate past = DateUtils.today().minusDays(10);
        when(examScheduleService.getSchedules("2290")).thenReturn(List.of(schedule(3L, past, past)));
        assertEquals(ErrorCode.VALIDATION_ERROR,
                addFails(new TargetExamRequest("2290", 3L, past, "#E5484D")).getErrorCode());
    }

    @Test
    void 이미_추가한_자격증이면_DUPLICATE() {
        when(repo.existsByUserIdAndJmcd(1L, "2290")).thenReturn(true);
        assertEquals(ErrorCode.DUPLICATE,
                addFails(new TargetExamRequest("2290", 7L, EXAM_DATE, "#E5484D")).getErrorCode());
    }

    @Test
    void 다른_목표와_같은_색이면_DUPLICATE() {
        when(repo.existsByUserIdAndDisplayColor(1L, "#E5484D")).thenReturn(true);
        assertEquals(ErrorCode.DUPLICATE,
                addFails(new TargetExamRequest("2290", 7L, EXAM_DATE, "#E5484D")).getErrorCode());
    }

    // ===== AI 플랜 저장 =====

    @Test
    void AI_저장_이미_목표면_시험일만_바꾸고_기존_ID() {
        TargetExam existing = mock(TargetExam.class);
        when(existing.getId()).thenReturn(5L);
        when(repo.findByUserIdAndJmcd(1L, "2290")).thenReturn(Optional.of(existing));

        assertEquals(5L, service.getOrCreateTargetExam(1L, "QNET:2290", 7L, EXAM_DATE));
        verify(existing).changeExam(7L, EXAM_DATE);
        verify(repo, never()).save(any());
    }

    @Test
    void AI_저장_새_목표면_안_쓰는_색으로_추가() {
        TargetExam used = mock(TargetExam.class);
        when(used.getDisplayColor()).thenReturn("#E5484D");
        when(repo.findByUserIdAndJmcd(1L, "2290")).thenReturn(Optional.empty());
        when(repo.findAllByUserIdOrderByIdAsc(1L)).thenReturn(List.of(used));

        service.getOrCreateTargetExam(1L, "2290", 7L, EXAM_DATE);

        ArgumentCaptor<TargetExam> saved = ArgumentCaptor.forClass(TargetExam.class);
        verify(repo).save(saved.capture());
        assertEquals("#F5A524", saved.getValue().getDisplayColor());
        verify(limitValidator).validateCanAdd(1L);
    }

    @Test
    void AI_저장_회차_기간_밖이면_400() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> service.getOrCreateTargetExam(1L, "2290", 7L, EXAM_DATE.plusDays(1)));
        assertEquals(ErrorCode.VALIDATION_ERROR, e.getErrorCode());
        verify(repo, never()).save(any());
    }
}
