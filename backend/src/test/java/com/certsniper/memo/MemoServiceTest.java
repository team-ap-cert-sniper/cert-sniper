package com.certsniper.memo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.certsniper.global.exception.BusinessException;
import com.certsniper.global.exception.ErrorCode;
import com.certsniper.memo.dto.MemoRequest;
import com.certsniper.memo.dto.MemoResponse;
import com.certsniper.memo.entity.Memo;
import com.certsniper.memo.repository.MemoRepository;
import com.certsniper.memo.service.MemoService;
import com.certsniper.targetexam.service.TargetExamService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class MemoServiceTest {

    private final MemoRepository memoRepo = mock(MemoRepository.class);
    private final TargetExamService targetExamService = mock(TargetExamService.class);
    private final MemoService service = new MemoService(memoRepo, targetExamService);

    @BeforeEach
    void setUp() {
        when(memoRepo.findByTargetExamId(10L)).thenReturn(Optional.empty());
        when(memoRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    /** 이미 저장된 메모 (버전 지정) */
    private Memo stored(int version) {
        Memo memo = Memo.create(10L, "예전 메모", "<p>예전 메모</p>");
        ReflectionTestUtils.setField(memo, "id", 3L);
        ReflectionTestUtils.setField(memo, "version", version);
        when(memoRepo.findByTargetExamId(10L)).thenReturn(Optional.of(memo));
        return memo;
    }

    private static MemoRequest request(String html, Integer version) {
        return new MemoRequest("화면에서 보낸 글자", html, version);
    }

    @Test
    void 메모가_없으면_빈_메모() {
        MemoResponse res = service.getMemo(1L, 10L);

        assertEquals("", res.contentText());
        assertEquals(0, res.version());
        verify(targetExamService).validateOwner(1L, 10L);
    }

    @Test
    void 저장하면_위험한_태그는_빼고_글자_수는_서식_없이_센다() {
        service.saveMemo(1L, 10L, request("<p><b>1과목</b> 복습</p><script>alert(1)</script>", null));

        ArgumentCaptor<Memo> saved = ArgumentCaptor.forClass(Memo.class);
        verify(memoRepo).save(saved.capture());
        assertEquals("1과목 복습", saved.getValue().getContentText());
        assertTrue(saved.getValue().getContentHtml().contains("<b>1과목</b>"));
        assertFalse(saved.getValue().getContentHtml().contains("script"));
    }

    @Test
    void 내용이_비었거나_500자를_넘으면_400() {
        BusinessException empty = assertThrows(BusinessException.class,
                () -> service.saveMemo(1L, 10L, request("<p>   </p>", null)));
        BusinessException tooLong = assertThrows(BusinessException.class,
                () -> service.saveMemo(1L, 10L, request("<p>" + "가".repeat(501) + "</p>", null)));
        assertEquals(ErrorCode.VALIDATION_ERROR, empty.getErrorCode());
        assertEquals(ErrorCode.VALIDATION_ERROR, tooLong.getErrorCode());
        verify(memoRepo, never()).save(any());
    }

    @Test
    void 덮어쓰면_버전이_오른다() {
        Memo memo = stored(2);

        MemoResponse res = service.saveMemo(1L, 10L, request("<p>새 메모</p>", 2));

        assertEquals("새 메모", memo.getContentText());
        assertEquals(3, res.version());
    }

    @Test
    void 다른_곳에서_먼저_저장했으면_409() {
        stored(2);

        BusinessException e = assertThrows(BusinessException.class,
                () -> service.saveMemo(1L, 10L, request("<p>새 메모</p>", 1)));
        assertEquals(ErrorCode.DUPLICATE, e.getErrorCode());
    }

    @Test
    void 삭제하면_지운_표시만_하고_조회는_빈_메모() {
        Memo memo = stored(2);

        service.deleteMemo(1L, 10L);

        assertTrue(memo.getIsDeleted());
        assertEquals("", service.getMemo(1L, 10L).contentText());
    }

    @Test
    void 지운_메모에_다시_쓰면_되살아난다() {
        Memo memo = stored(2);
        memo.delete();

        service.saveMemo(1L, 10L, request("<p>다시 쓴 메모</p>", 0));

        assertFalse(memo.getIsDeleted());
        assertEquals("다시 쓴 메모", memo.getContentText());
    }

    @Test
    void 남의_목표_자격증_메모는_403() {
        doThrow(new BusinessException(ErrorCode.FORBIDDEN)).when(targetExamService).validateOwner(1L, 10L);

        assertThrows(BusinessException.class, () -> service.getMemo(1L, 10L));
        assertThrows(BusinessException.class, () -> service.saveMemo(1L, 10L, request("<p>메모</p>", null)));
        verify(memoRepo, never()).save(any());
    }
}
