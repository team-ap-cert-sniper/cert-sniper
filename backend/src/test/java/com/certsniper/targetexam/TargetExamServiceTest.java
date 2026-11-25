package com.certsniper.targetexam;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.certsniper.examschedule.service.DdayCalculator;
import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
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
    private final DdayCalculator ddayCalculator = mock(DdayCalculator.class);
    private final ScheduleLimitValidator limitValidator = mock(ScheduleLimitValidator.class);
    private final TargetExamService service =
            new TargetExamService(repo, qualificationService, ddayCalculator, limitValidator);

    private static final LocalDate EXAM_DATE = LocalDate.of(2026, 12, 31);

    @BeforeEach
    void setUp() {
        QualificationDetailResponse.QualificationInfo info = mock(QualificationDetailResponse.QualificationInfo.class);
        when(info.jmfldnm()).thenReturn("SQL개발자");
        when(qualificationService.getInfo(any())).thenReturn(info);
        when(ddayCalculator.label(any(), any())).thenReturn("D-36");
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void stored(Long targetExamId, Long ownerId) {
        TargetExam t = mock(TargetExam.class);
        when(t.getUserId()).thenReturn(ownerId);
        when(repo.findById(targetExamId)).thenReturn(Optional.of(t));
    }

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

    @Test
    void 목표_자격증_추가하면_이름과_DDAY가_들어간_응답() {
        TargetExamResponse res = service.addTargetExam(1L, new TargetExamRequest("2290", 7L, EXAM_DATE, "#E5484D"));
        assertEquals("SQL개발자", res.jmfldnm());
        assertEquals("D-36", res.ddayLabel());
        assertEquals("#E5484D", res.displayColor());
        verify(limitValidator).validateCanAdd(1L);
    }

    @Test
    void 이미_추가한_자격증이면_DUPLICATE() {
        when(repo.existsByUserIdAndJmcd(1L, "2290")).thenReturn(true);
        BusinessException e = assertThrows(BusinessException.class,
                () -> service.addTargetExam(1L, new TargetExamRequest("2290", 7L, EXAM_DATE, "#E5484D")));
        assertEquals(ErrorCode.DUPLICATE, e.getErrorCode());
        verify(repo, never()).save(any());
    }

    @Test
    void 다른_목표와_같은_색이면_DUPLICATE() {
        when(repo.existsByUserIdAndDisplayColor(1L, "#E5484D")).thenReturn(true);
        BusinessException e = assertThrows(BusinessException.class,
                () -> service.addTargetExam(1L, new TargetExamRequest("2290", 7L, EXAM_DATE, "#E5484D")));
        assertEquals(ErrorCode.DUPLICATE, e.getErrorCode());
    }

    @Test
    void AI_저장_이미_목표면_시험일만_바꾸고_기존_ID() {
        TargetExam existing = mock(TargetExam.class);
        when(existing.getId()).thenReturn(5L);
        when(repo.findByUserIdAndJmcd(1L, "2290")).thenReturn(Optional.of(existing));

        assertEquals(5L, service.getOrCreateTargetExam(1L, "2290", 7L, EXAM_DATE));
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
}
