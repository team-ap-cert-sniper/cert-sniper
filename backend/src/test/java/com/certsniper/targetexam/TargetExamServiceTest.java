package com.certsniper.targetexam;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.targetexam.entity.TargetExam;
import com.certsniper.targetexam.repository.TargetExamRepository;
import com.certsniper.targetexam.service.TargetExamService;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TargetExamServiceTest {

    private final TargetExamRepository repo = mock(TargetExamRepository.class);
    private final TargetExamService service = new TargetExamService(repo);

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
}
