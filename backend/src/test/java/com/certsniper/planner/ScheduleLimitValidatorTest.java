package com.certsniper.planner;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.planner.repository.PersonalScheduleRepository;
import com.certsniper.planner.service.ScheduleLimitValidator;
import com.certsniper.targetexam.repository.TargetExamRepository;
import org.junit.jupiter.api.Test;

class ScheduleLimitValidatorTest {

    private final TargetExamRepository targetExamRepo = mock(TargetExamRepository.class);
    private final PersonalScheduleRepository personalRepo = mock(PersonalScheduleRepository.class);
    private final ScheduleLimitValidator validator = new ScheduleLimitValidator(targetExamRepo, personalRepo);

    @Test
    void 합계가_5개면_추가_가능() {
        when(targetExamRepo.countByUserId(1L)).thenReturn(3L);
        when(personalRepo.countByUserId(1L)).thenReturn(2L);
        assertDoesNotThrow(() -> validator.validateCanAdd(1L));
    }

    @Test
    void 합계가_6개면_LIMIT_EXCEEDED() {
        when(targetExamRepo.countByUserId(1L)).thenReturn(4L);
        when(personalRepo.countByUserId(1L)).thenReturn(2L);
        BusinessException e = assertThrows(BusinessException.class, () -> validator.validateCanAdd(1L));
        assertEquals(ErrorCode.LIMIT_EXCEEDED, e.getErrorCode());
    }
}
